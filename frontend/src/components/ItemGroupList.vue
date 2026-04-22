<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import ItemBox from './ItemBox.vue';
import IconChevronDown from './icons/IconChevronDown.vue';
import IconMeat from './icons/IconMeat.vue';
import IconSearch from './icons/IconSearch.vue';
import type { Item, ItemId } from '../types/item';
import type { MemberId } from '../types/member';
import { normalizeText } from '../utils/text-normalization';
import { deleteItem as deleteItemApi, updateItem } from '@/api/item';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { groupItems } from '@/utils/item-grouping';
import type { GroupDefinition, ItemGroup } from '@/utils/item-grouping';
import { getErrorMessage } from '@/lib/http';

const ITEM_GROUP_DEFINITIONS: GroupDefinition<Item>[] = [
  {
    key: 'incomplete',
    label: '未完了',
    predicate: (item) => !item.completed,
    comparator: (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
    showHeader: false,
    collapsible: false,
    defaultCollapsed: false
  },
  {
    key: 'completed',
    label: '完了',
    predicate: (item) => item.completed,
    comparator: (a, b) => {
      const bTime = b.completedAt ? new Date(b.completedAt).getTime() : 0;
      const aTime = a.completedAt ? new Date(a.completedAt).getTime() : 0;
      return bTime - aTime;
    },
    showHeader: true,
    collapsible: true,
    defaultCollapsed: false
  }
];

export default defineComponent({
  name: 'ItemGroupList',
  components: { ItemBox, IconChevronDown, IconMeat, IconSearch },
  props: {
    filteredItems: {
      type: Array as PropType<Item[]>,
      required: true
    },
    items: {
      type: Array as PropType<Item[]>,
      required: true
    },
    searchQuery: {
      type: String,
      required: true
    },
    selectedMemberId: {
      type: String as PropType<MemberId | null>,
      default: null
    }
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  data(): {
    collapsedGroups: Record<string, boolean>;
    errorMessage: string;
  } {
    return {
      collapsedGroups: Object.fromEntries(
        ITEM_GROUP_DEFINITIONS.map((def) => [def.key, def.defaultCollapsed ?? false])
      ) as Record<string, boolean>,
      errorMessage: ''
    };
  },
  computed: {
    groupedFilteredItems(): ItemGroup<Item>[] {
      return groupItems(this.filteredItems, ITEM_GROUP_DEFINITIONS);
    },
    memberMap() {
      return this.listStore.memberMap;
    }
  },
  methods: {
    getMemberBadgeVariant(item: Item): string {
      if (item.completed && this.selectedMemberId && item.completedByMemberId === this.selectedMemberId) {
        return 'primary';
      }
      return 'secondary';
    },
    getCompletedMemberName(item: Item): string | null {
      if (!item.completed || !item.completedByMemberId) {
        return null;
      }
      return this.memberMap.get(item.completedByMemberId)?.displayName ?? null;
    },
    showErrorFeedback() {
      if (this.errorMessage) {
        alert(this.errorMessage);
        setTimeout(() => {
          this.errorMessage = '';
        }, 3000);
      }
    },
    async toggleItem(item: Item) {
      this.errorMessage = '';
      const listId = this.listStore.listId;
      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        this.showErrorFeedback();
        return;
      }
      const wasCompleted = item.completed;
      const updatedItem: Partial<Item> = {
        completed: !wasCompleted,
        completedByMemberId: wasCompleted ? null : (this.selectedMemberId ?? null)
      };
      try {
        const result = await this.mutationRun(() => updateItem(listId, item.id, updatedItem));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
        }
      } catch (err: unknown) {
        console.error('Failed to update item', err);
        this.errorMessage = getErrorMessage(err) ?? 'アイテムの更新に失敗しました。';
        this.showErrorFeedback();
      }
    },
    async deleteItem(itemId: ItemId) {
      const listId = this.listStore.listId;
      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        this.showErrorFeedback();
        return;
      }
      try {
        const result = await this.mutationRun(() => deleteItemApi(listId, itemId));
        if (result.applied) {
          this.listStore.removeItem(itemId);
        }
      } catch (err: unknown) {
        console.error('Failed to delete item', err);
        this.errorMessage = getErrorMessage(err) ?? 'アイテムの削除に失敗しました。';
        this.showErrorFeedback();
      }
    },
    async modifyItem(updatedItem: Item) {
      const normalizedItemName = normalizeText(updatedItem.name);
      if (!normalizedItemName) {
        this.errorMessage = 'アイテム名が空のため、更新できません。';
        this.showErrorFeedback();
        return;
      }
      const listId = this.listStore.listId;
      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        this.showErrorFeedback();
        return;
      }
      try {
        const result = await this.mutationRun(() => updateItem(listId, updatedItem.id, { name: normalizedItemName }));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
        }
      } catch (err: unknown) {
        console.error('Failed to rename item', err);
        this.errorMessage = getErrorMessage(err) ?? 'アイテムの更新に失敗しました。';
        this.showErrorFeedback();
      }
    },
    toggleGroupCollapse(key: string): void {
      this.collapsedGroups[key] = !this.collapsedGroups[key];
    },
    isGroupCollapsed(key: string): boolean {
      return this.collapsedGroups[key] ?? false;
    }
  }
});
</script>

<template>
  <div class="space-y-3">
    <template v-for="group in groupedFilteredItems" :key="group.key">
      <!-- グループヘッダー（showHeader=trueのグループのみ） -->
      <button
        v-if="group.showHeader && group.collapsible"
        type="button"
        @click="toggleGroupCollapse(group.key)"
        :aria-expanded="!isGroupCollapsed(group.key)"
        :aria-label="`${group.label} ${group.items.length}件を${isGroupCollapsed(group.key) ? '展開' : '折りたたむ'}`"
        class="w-full flex items-center gap-2 mt-1 py-1 text-left focus:outline-none focus:ring-2 focus:ring-charcoal-400 rounded"
      >
        <div class="flex-1 h-px bg-charcoal-200"></div>
        <span class="text-xs text-charcoal-500 whitespace-nowrap">{{ group.label }} {{ group.items.length }}件</span>
        <span
          class="text-charcoal-500 text-xs transition-transform duration-200"
          :class="{ '-rotate-90': isGroupCollapsed(group.key) }"
          aria-hidden="true"
          ><IconChevronDown
        /></span>
      </button>
      <div v-else-if="group.showHeader" class="flex items-center gap-2 mt-1 py-1">
        <div class="flex-1 h-px bg-charcoal-200"></div>
        <span class="text-xs text-charcoal-500 whitespace-nowrap">{{ group.label }} {{ group.items.length }}件</span>
      </div>

      <!-- グループ内アイテム -->
      <template v-if="!isGroupCollapsed(group.key)">
        <ItemBox
          v-for="item in group.items"
          :key="item.id"
          :item="item"
          :memberBadgeVariant="getMemberBadgeVariant(item)"
          :completedMemberName="getCompletedMemberName(item) || ''"
          @toggle="toggleItem"
          @delete="deleteItem"
          @modify="modifyItem"
        />
      </template>
    </template>

    <!-- アイテムがない場合 -->
    <div v-if="items.length === 0" class="text-center text-charcoal-600 py-8">
      <div class="text-4xl mb-3"><IconMeat /></div>
      まだアイテムがありません。<br />
      上のフォームからアイテムを追加してください。
    </div>

    <!-- 検索結果がない場合 -->
    <div v-else-if="filteredItems.length === 0" class="text-center text-charcoal-600 py-8">
      <div class="text-4xl mb-3"><IconSearch /></div>
      「{{ searchQuery }}」に一致するアイテムが見つかりませんでした。
    </div>
  </div>
</template>
