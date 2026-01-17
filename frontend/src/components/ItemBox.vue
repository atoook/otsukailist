<template>
  <SwipeContainer :hiddenBgColor="'#fef7f0'">
    <div
      :id="`item-${item.id}`"
      class="flex items-center gap-3 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm focus:outline-none focus-within:ring-2 focus-within:ring-wood-300 focus-within:ring-opacity-60"
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
      <div v-if="!isCompleted" class="flex-1 flex flex-col" @focusin="handleInlineInputFocus">
        <TextInput
          :input-id="item.id"
          input-name="itemName"
          :model-value="newName"
          @update:model-value="handleModify"
          @enter="isModified && syncUpdate()"
          @blur="handleBlur"
          variant="inline"
        />
        <p v-if="shouldShowAutosaveHint" class="text-xs text-charcoal-500 mt-1">変更は自動保存されます</p>
      </div>
      <span v-else class="line-through text-charcoal-500 flex-1">
        {{ item.name }}
      </span>
      <span v-if="showSaveIndicator" class="text-success-600 text-lg">✔︎</span>
      <BadgeTag v-if="displayMember" :text="displayMember.name" icon="👤" size="small" :variant="memberBadgeVariant" />
    </div>

    <template #hiddenActions>
      <button @click="handleDelete(item.id)" :aria-label="`${item.name}を削除`" tabindex="-1" role="button">
        <BadgeTag text="削除" icon="🗑️" size="small" class="bg-ember-400 border-ember-600 text-white" />
      </button>
    </template>
  </SwipeContainer>
</template>

<script lang="ts">
import CheckBox from './CheckBox.vue';
import TextInput from './TextInput.vue';
import SwipeContainer from './SwipeContainer.vue';
import BadgeTag from './BadgeTag.vue';
import type { Item, ItemId } from '../types/item';
import { isItem, isCompletedStatus } from '../types/item';
import { normalizeText } from '../utils/text-normalization';

export default {
  name: 'ItemBox',
  components: {
    CheckBox,
    TextInput,
    SwipeContainer,
    BadgeTag
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
    }
  },
  emits: ['toggle', 'info', 'delete', 'modify'],
  created() {
    this.newName = this.item.name;
  },
  beforeUnmount() {
    this.clearSaveIndicatorTimer();
  },
  computed: {
    isCompleted() {
      return isCompletedStatus(this.item.status);
    },
    displayMember() {
      // 完了済みアイテム: assignedMember（連携されたメンバー）を表示
      if (this.isCompleted && this.item.assignedMember) {
        return this.item.assignedMember;
      }
      return null;
    },
    shouldShowAutosaveHint() {
      return this.isInputFocused && this.isModified;
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
