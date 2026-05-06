<template>
  <SwipeContainer :hiddenBgColor="'#fef7f0'">
    <div
      :id="`item-${item.id}`"
      class="flex items-center gap-2 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm focus:outline-none focus-within:ring-2 focus-within:ring-wood-300 focus-within:ring-opacity-60"
      role="listitem"
      :aria-label="`アイテム: ${item.name}. ${isCompleted ? '完了済み' : '未完了'}`"
    >
      <!-- カスタムチェックボックス -->
      <CheckBox
        :checked="isCompleted"
        :aria-label="`${item.name}を完了としてマーク`"
        @toggle="handleToggle(item)"
        @keydown="handleKeyDown"
      />

      <!-- アイテム名 -->
      <div v-if="!isCompleted" class="min-w-0 flex-1 flex flex-col" @focusin="handleInlineInputFocus">
        <TextInput
          :input-id="item.id"
          input-name="itemName"
          :model-value="newName"
          @update:model-value="handleModify"
          @enter="isModified && syncUpdate()"
          @blur="handleBlur"
          variant="inline"
        />
        <div v-if="quantifiedLabel || categoryLabel" class="mt-1 flex flex-wrap gap-1">
          <BadgeTag v-if="quantifiedLabel" :text="quantifiedLabel" size="small" variant="secondary" />
          <BadgeTag v-if="categoryLabel" :text="categoryLabel" size="small" variant="secondary" />
        </div>
        <p v-if="shouldShowAutosaveHint" class="text-xs text-charcoal-500 mt-1">変更は自動保存されます</p>
      </div>
      <div v-else class="min-w-0 flex-1 flex flex-col">
        <span class="truncate line-through text-charcoal-500">
          {{ item.name }}
        </span>
        <div v-if="quantifiedLabel || categoryLabel" class="mt-1 flex flex-wrap gap-1">
          <BadgeTag v-if="quantifiedLabel" :text="quantifiedLabel" size="small" variant="secondary" />
          <BadgeTag v-if="categoryLabel" :text="categoryLabel" size="small" variant="secondary" />
        </div>
      </div>
      <span v-if="showSaveIndicator" class="text-success-600 flex items-center" role="status" aria-label="保存済み"
        ><IconCheck
      /></span>
      <span v-if="memberBadgeText" class="relative inline-flex shrink-0">
        <button
          type="button"
          class="rounded-full focus:outline-none focus:ring-2 focus:ring-wood-300 disabled:cursor-default disabled:opacity-60"
          :disabled="!memberId"
          :aria-disabled="!memberId"
          :aria-label="`${memberName}で絞り込む`"
          @click="handleMemberFilter"
        >
          <BadgeTag :text="memberBadgeText" size="small" :variant="memberBadgeVariant" />
        </button>
        <IconButton
          v-if="memberFilterActive && memberId"
          class="absolute -right-1.5 -top-1.5 border border-ember-200 bg-wood-50"
          variant="danger"
          size="tiny"
          :aria-label="`${memberName}の絞り込みを解除`"
          @click.stop="handleMemberFilterClear"
        >
          <IconClose />
        </IconButton>
      </span>
      <IconButton variant="wood" size="small" :aria-label="`${item.name}を編集`" @click="handleEdit(item)">
        <IconEllipsisVertical />
      </IconButton>
    </div>

    <template #hiddenActions>
      <button @click="handleDelete(item.id)" :aria-label="`${item.name}を削除`" tabindex="-1" role="button">
        <BadgeTag text="削除" size="small" class="bg-ember-400 border-ember-600 text-white"
          ><template #icon><IconTrash /></template
        ></BadgeTag>
      </button>
    </template>
  </SwipeContainer>
</template>

<script lang="ts">
import CheckBox from './CheckBox.vue';
import TextInput from './TextInput.vue';
import SwipeContainer from './SwipeContainer.vue';
import BadgeTag from './BadgeTag.vue';
import IconButton from './IconButton.vue';
import IconCheck from './icons/IconCheck.vue';
import IconClose from './icons/IconClose.vue';
import IconEllipsisVertical from './icons/IconEllipsisVertical.vue';
import IconTrash from './icons/IconTrash.vue';
import type { Item, ItemId } from '../types/item';
import { isItem, isItemCompleted } from '../types/item';
import { normalizeText } from '../utils/text-normalization';
import { UNIT_DEFINITIONS } from '@/types/list-generation';
import { ITEM_CATEGORIES } from '@/types/item-category';

export default {
  name: 'ItemBox',
  components: {
    CheckBox,
    TextInput,
    SwipeContainer,
    BadgeTag,
    IconButton,
    IconCheck,
    IconClose,
    IconTrash,
    IconEllipsisVertical
  },
  data() {
    return {
      isModified: false,
      newName: '',
      isInputFocused: false,
      showSaveIndicator: false,
      saveIndicatorTimer: null as number | null
    };
  },
  props: {
    item: {
      type: Object as () => Item,
      required: true,
      validator: isItem
    },
    memberBadgeVariant: {
      type: String,
      default: 'primary',
      validator: (value: string) => ['default', 'primary', 'secondary'].includes(value)
    },
    memberName: {
      type: String,
      default: ''
    },
    memberId: {
      type: String,
      default: null
    },
    memberFilterActive: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle', 'info', 'delete', 'modify', 'edit', 'member-filter', 'clear-member-filter'],
  created() {
    this.newName = this.item.name;
  },
  beforeUnmount() {
    this.clearSaveIndicatorTimer();
  },
  computed: {
    isCompleted() {
      return isItemCompleted(this.item);
    },
    shouldShowAutosaveHint() {
      return this.isInputFocused && this.isModified;
    },
    memberBadgeText() {
      return this.memberName.trim().charAt(0);
    },
    quantifiedLabel() {
      if (this.item.itemType !== 'quantified' || !this.item.quantified) {
        return '';
      }
      const unitDefinition = UNIT_DEFINITIONS[this.item.quantified.baseUnit];
      const unitLabel = unitDefinition?.label ?? this.item.quantified.baseUnit ?? '';
      return `${this.item.quantified.quantity}${unitLabel}`;
    },
    categoryLabel() {
      if (!this.item.category) {
        return '';
      }
      return ITEM_CATEGORIES[this.item.category]?.label ?? this.item.category;
    }
  },
  watch: {
    item: {
      handler(newItem: Item) {
        // 親コンポーネントからitemが更新された場合、新しい名前を反映
        this.newName = newItem.name;
        this.isModified = false;
      },
      deep: true
    }
  },
  methods: {
    handleToggle(item: Item) {
      this.$emit('toggle', item);
    },
    handleInfo(item: Item) {
      this.$emit('info', item);
    },
    handleDelete(itemId: ItemId) {
      this.$emit('delete', itemId);
    },
    handleEdit(item: Item) {
      this.$emit('edit', item);
    },
    handleMemberFilter() {
      if (this.memberFilterActive) {
        this.handleMemberFilterClear();
        return;
      }
      if (!this.memberId) {
        return;
      }
      this.$emit('member-filter', this.memberId);
    },
    handleMemberFilterClear() {
      this.$emit('clear-member-filter');
    },
    handleKeyDown(event: KeyboardEvent) {
      // スペースキーまたはEnterキーでチェックボックストグル
      if (event.code === 'Space' || event.code === 'Enter') {
        event.preventDefault();
        this.handleToggle(this.item);
      }
      // DeleteキーまたはBackspaceキーで削除
      else if (event.code === 'Delete' || event.code === 'Backspace') {
        event.preventDefault();
        this.handleDelete(this.item.id);
      }
    },
    handleModify(newName: string) {
      if (newName === this.item.name) {
        this.isModified = false;
        this.newName = this.item.name;
        return;
      }
      this.isModified = true;
      this.newName = newName;
    },
    handleInlineInputFocus() {
      this.isInputFocused = true;
    },
    handleBlur() {
      this.isInputFocused = false;
      if (!this.isModified) {
        return;
      }
      this.syncUpdate();
    },
    resetToOriginal() {
      this.newName = this.item.name;
      this.isModified = false;
      this.hideSaveIndicator();
    },
    syncUpdate() {
      const normalizedName = normalizeText(this.newName);
      if (!normalizedName) {
        this.resetToOriginal();
        return;
      }
      const updatedItem = { ...this.item, name: normalizedName };
      this.$emit('modify', updatedItem);
      this.isModified = false;
      this.showSaveIndicatorTemporarily();
    },
    showSaveIndicatorTemporarily() {
      this.showSaveIndicator = true;
      this.clearSaveIndicatorTimer();
      this.saveIndicatorTimer = window.setTimeout(() => {
        this.hideSaveIndicator();
      }, 1500);
    },
    hideSaveIndicator() {
      this.showSaveIndicator = false;
      this.clearSaveIndicatorTimer();
    },
    clearSaveIndicatorTimer() {
      if (this.saveIndicatorTimer !== null) {
        clearTimeout(this.saveIndicatorTimer);
        this.saveIndicatorTimer = null;
      }
    }
  }
};
</script>
