<template>
  <SwipeContainer :hiddenBgColor="'#fef7f0'" @swipe-state-change="handleDeleteActionStateChange">
    <div
      :id="`item-${item.id}`"
      class="flex items-center gap-2 p-3 border rounded-lg shadow-sm focus:outline-none focus-within:ring-2 focus-within:ring-wood-300 focus-within:ring-opacity-60 transition-[background-color,border-color,box-shadow,opacity] duration-200"
      :class="[
        isDeleteActionOpen ? 'bg-wood-200 border-wood-300 shadow-none' : 'bg-wood-100 border-wood-200',
        isToggleLoading ? 'opacity-80' : ''
      ]"
      role="listitem"
      :aria-busy="isToggleLoading"
      :aria-label="`アイテム: ${item.name}. ${isCompleted ? '完了済み' : '未完了'}`"
    >
      <!-- カスタムチェックボックス -->
      <div class="relative h-6 w-6 shrink-0">
        <CheckBox
          :checked="isCompleted"
          :disabled="isItemActionDisabled"
          :aria-label="`${item.name}を${isCompleted ? '未完了' : '完了'}としてマーク`"
          @toggle="handleToggle(item)"
          @keydown="handleKeyDown"
        />
        <span
          v-if="isToggleLoading"
          class="absolute inset-0 flex items-center justify-center rounded-md bg-wood-100/80"
          role="status"
          aria-label="更新中"
        >
          <InlineSpinner size="sm" tone="wood" />
        </span>
      </div>

      <!-- アイテム名 -->
      <div v-if="!isCompleted" class="min-w-0 flex-1 flex flex-col" @focusin="handleInlineInputFocus">
        <TextInput
          :input-id="item.id"
          input-name="itemName"
          :model-value="newName"
          :disabled="isItemActionDisabled"
          @update:model-value="handleModify"
          @enter="isModified && syncUpdate()"
          @blur="handleBlur"
          variant="inline"
        />
        <div v-if="quantifiedLabel || categoryLabel || preparationTypeLabel" class="mt-1 flex flex-wrap gap-1">
          <FilterBadgeButton
            v-if="preparationTypeLabel"
            :text="preparationTypeLabel"
            variant="secondary"
            :active="preparationTypeFilterActive"
            :disabled="isItemActionDisabled"
            :filter-label="`${preparationTypeLabel}で絞り込む`"
            :clear-label="`${preparationTypeLabel}の絞り込みを解除`"
            @filter="handlePreparationTypeFilter"
            @clear="handlePreparationTypeFilterClear"
          />
          <BadgeTag v-if="quantifiedLabel" :text="quantifiedLabel" size="small" variant="default" />
          <FilterBadgeButton
            v-if="categoryLabel"
            :text="categoryLabel"
            variant="secondary"
            :active="categoryFilterActive"
            :disabled="isItemActionDisabled"
            :filter-label="`${categoryLabel}カテゴリーで絞り込む`"
            :clear-label="`${categoryLabel}カテゴリーの絞り込みを解除`"
            @filter="handleCategoryFilter"
            @clear="handleCategoryFilterClear"
          />
        </div>
        <p v-if="shouldShowAutosaveHint" class="text-xs text-charcoal-500 mt-1">変更は自動保存されます</p>
      </div>
      <div v-else class="min-w-0 flex-1 flex flex-col">
        <span class="truncate line-through text-charcoal-500">
          {{ item.name }}
        </span>
        <div v-if="quantifiedLabel || categoryLabel || preparationTypeLabel" class="mt-1 flex flex-wrap gap-1">
          <FilterBadgeButton
            v-if="preparationTypeLabel"
            :text="preparationTypeLabel"
            variant="secondary"
            :active="preparationTypeFilterActive"
            :disabled="isItemActionDisabled"
            :filter-label="`${preparationTypeLabel}で絞り込む`"
            :clear-label="`${preparationTypeLabel}の絞り込みを解除`"
            @filter="handlePreparationTypeFilter"
            @clear="handlePreparationTypeFilterClear"
          />
          <BadgeTag v-if="quantifiedLabel" :text="quantifiedLabel" size="small" variant="default" />
          <FilterBadgeButton
            v-if="categoryLabel"
            :text="categoryLabel"
            variant="secondary"
            :active="categoryFilterActive"
            :disabled="isItemActionDisabled"
            :filter-label="`${categoryLabel}カテゴリーで絞り込む`"
            :clear-label="`${categoryLabel}カテゴリーの絞り込みを解除`"
            @filter="handleCategoryFilter"
            @clear="handleCategoryFilterClear"
          />
        </div>
      </div>
      <span v-if="showSaveIndicator" class="text-success-600 flex items-center" role="status" aria-label="保存済み"
        ><IconCheck
      /></span>
      <FilterBadgeButton
        v-if="memberBadgeText"
        class="shrink-0"
        :text="memberBadgeText"
        :variant="memberBadgeVariant"
        :active="memberFilterActive"
        :disabled="!memberId || isItemActionDisabled"
        :filter-label="`${memberName}で絞り込む`"
        :clear-label="`${memberName}の絞り込みを解除`"
        @filter="handleMemberFilter"
        @clear="handleMemberFilterClear"
      />
      <IconButton
        variant="wood"
        size="small"
        :disabled="isItemActionDisabled"
        :aria-label="`${item.name}を編集`"
        @click="handleEdit(item)"
      >
        <IconEllipsisVertical />
      </IconButton>
    </div>

    <template #hiddenActions>
      <SwipeContainerAction
        @activate="handleDelete(item.id)"
        :disabled="isDeleteLoading"
        :aria-busy="isDeleteLoading"
        :aria-label="`${item.name}を削除`"
        tabindex="-1"
        class="disabled:cursor-wait disabled:opacity-80"
      >
        <BadgeTag
          :text="isDeleteLoading ? '削除中' : '削除'"
          size="small"
          class="bg-ember-400 border-ember-600 text-white"
        >
          <template #icon>
            <InlineSpinner v-if="isDeleteLoading" size="xs" tone="inverse" />
            <IconTrash v-else />
          </template>
        </BadgeTag>
      </SwipeContainerAction>
    </template>
  </SwipeContainer>
</template>

<script lang="ts">
import CheckBox from './CheckBox.vue';
import InlineSpinner from './InlineSpinner.vue';
import TextInput from './TextInput.vue';
import SwipeContainer from './SwipeContainer.vue';
import SwipeContainerAction from './SwipeContainerAction.vue';
import BadgeTag from './BadgeTag.vue';
import FilterBadgeButton from './FilterBadgeButton.vue';
import IconButton from './IconButton.vue';
import IconCheck from './icons/IconCheck.vue';
import IconEllipsisVertical from './icons/IconEllipsisVertical.vue';
import IconTrash from './icons/IconTrash.vue';
import type { Item, ItemId } from '../types/item';
import { isItem, isItemCompleted } from '../types/item';
import { normalizeText } from '../utils/text-normalization';
import { UNIT_DEFINITIONS } from '@/types/list-generation';
import { ITEM_CATEGORIES, type ItemCategory } from '@/types/item-category';
import { ITEM_PREPARATION_TYPES, type ItemPreparationType } from '@/types/item-preparation-type';

export default {
  name: 'ItemBox',
  components: {
    CheckBox,
    InlineSpinner,
    TextInput,
    SwipeContainer,
    SwipeContainerAction,
    BadgeTag,
    FilterBadgeButton,
    IconButton,
    IconCheck,
    IconTrash,
    IconEllipsisVertical
  },
  data() {
    return {
      isModified: false,
      newName: '',
      isInputFocused: false,
      showSaveIndicator: false,
      saveIndicatorTimer: null as number | null,
      isDeleteActionOpen: false
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
    },
    categoryFilterActive: {
      type: Boolean,
      default: false
    },
    preparationTypeFilterActive: {
      type: Boolean,
      default: false
    },
    isDeleteLoading: {
      type: Boolean,
      default: false
    },
    isToggleLoading: {
      type: Boolean,
      default: false
    }
  },
  emits: [
    'toggle',
    'info',
    'delete',
    'modify',
    'edit',
    'member-filter',
    'clear-member-filter',
    'category-filter',
    'clear-category-filter',
    'preparation-type-filter',
    'clear-preparation-type-filter'
  ],
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
    isItemActionDisabled() {
      return this.isDeleteActionOpen || this.isToggleLoading;
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
    },
    preparationTypeLabel() {
      if (!this.item.preparationType) {
        return '';
      }
      return ITEM_PREPARATION_TYPES[this.item.preparationType]?.label ?? this.item.preparationType;
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
      if (this.isItemActionDisabled) {
        return;
      }
      this.$emit('toggle', item);
    },
    handleInfo(item: Item) {
      this.$emit('info', item);
    },
    handleDeleteActionStateChange(isOpen: boolean) {
      this.isDeleteActionOpen = isOpen;
      if (!isOpen) {
        return;
      }
      const activeElement = document.activeElement;
      if (activeElement instanceof HTMLElement && this.$el?.contains(activeElement)) {
        activeElement.blur();
      }
    },
    handleDelete(itemId: ItemId) {
      this.$emit('delete', itemId);
    },
    handleEdit(item: Item) {
      this.$emit('edit', item);
    },
    handleMemberFilter() {
      if (!this.memberId) {
        return;
      }
      this.$emit('member-filter', this.memberId);
    },
    handleMemberFilterClear() {
      this.$emit('clear-member-filter');
    },
    handleCategoryFilter() {
      if (!this.item.category) {
        return;
      }
      this.$emit('category-filter', this.item.category as ItemCategory);
    },
    handleCategoryFilterClear() {
      this.$emit('clear-category-filter');
    },
    handlePreparationTypeFilter() {
      if (!this.item.preparationType) {
        return;
      }
      this.$emit('preparation-type-filter', this.item.preparationType as ItemPreparationType);
    },
    handlePreparationTypeFilterClear() {
      this.$emit('clear-preparation-type-filter');
    },
    handleKeyDown(event: KeyboardEvent) {
      // スペースキーまたはEnterキーでチェックボックストグル
      if (event.code === 'Space' || event.code === 'Enter') {
        event.preventDefault();
        if (this.isItemActionDisabled) {
          return;
        }
        this.handleToggle(this.item);
      }
      // DeleteキーまたはBackspaceキーで削除
      else if (event.code === 'Delete' || event.code === 'Backspace') {
        event.preventDefault();
        if (this.isItemActionDisabled) {
          return;
        }
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
      if (this.isItemActionDisabled) {
        return;
      }
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
