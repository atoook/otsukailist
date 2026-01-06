<template>
  <div
    :id="`item-${item.id}`"
    class="flex items-center gap-3 p-3 bg-wood-100 border border-wood-200 rounded-lg shadow-sm"
  >
    <!-- カスタムチェックボックス -->
    <CheckBox :checked="isCompleted" :aria-label="`${item.name}を完了としてマーク`" @toggle="handleToggle(item)" />

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

    <!-- 削除ボタン -->
    <button
      @click="handleDelete(item.id)"
      :aria-label="`${item.name}を削除`"
      class="text-ember-500 hover:text-ember-600 text-sm font-medium transition-colors"
    >
      <badge-tag
        text="削除"
        icon="🗑️"
        size="small"
        class="bg-ember-500 border-ember-600 text-white hover:bg-ember-600"
      />
    </button>
  </div>
</template>

<script lang="ts">
import CheckBox from './CheckBox.vue';
import BadgeTag from './BadgeTag.vue';
import type { Item, ItemId } from '../types/item';
import { isItem, isCompletedStatus } from '../types/item';

export default {
  name: 'ItemBox',
  components: {
    CheckBox,
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
    }
  }
};
</script>
