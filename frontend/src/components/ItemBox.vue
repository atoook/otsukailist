<template>
  <SwipeContainer :hiddenBgColor="'#fef7f0'" role="listitem">
    <div
      :id="`item-${item.id}`"
      class="flex items-center gap-3 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm focus-within:ring-2 focus-within:ring-wood-300 focus-within:ring-opacity-60"
      role="group"
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
      <span
        :class="{
          'line-through text-charcoal-500': isCompleted,
          'text-charcoal-800': !isCompleted
        }"
        class="flex-1"
      >
        {{ item.name }}
      </span>
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
import SwipeContainer from './SwipeContainer.vue';
import BadgeTag from './BadgeTag.vue';
import type { Item, ItemId } from '../types/item';
import { isItem, isCompletedStatus } from '../types/item';

export default {
  name: 'ItemBox',
  components: {
    CheckBox,
    SwipeContainer,
    BadgeTag
  },
  props: {
    item: {
      type: Object as () => Item,
      required: true,
      validator: isItem
    }
  },
  emits: ['toggle', 'info', 'delete'],
  computed: {
    isCompleted() {
      return isCompletedStatus(this.item.status);
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
    }
  }
};
</script>
