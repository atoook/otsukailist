<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import TextInput from '../components/TextInput.vue';
import DropDown from '../components/DropDown.vue';
import ItemAddForm from '../components/ItemAddForm.vue';
import ItemGroupList from '../components/ItemGroupList.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import type { Item } from '../types/item';
import { normalizeInput, normalizeForSearch } from '../utils/text-normalization';
import type { Member, MemberId } from '@/types/member';
import { fetchSnapshot } from '@/api/list';
import { useListStore } from '@/stores/list';

export default defineComponent({
  name: 'ItemListPage',
  components: {
    ContentArea,
    TextInput,
    DropDown,
    ItemAddForm,
    ItemGroupList,
    LoadingSpinner
  },
  data(): {
    currentListId: string | null;
    searchQuery: string;
    selectedMemberId: MemberId | null;
    errorMessage: string;
    fallbackListName: string;
    snapshotLoading: boolean;
  } {
    return {
      currentListId: null,
      searchQuery: '',
      selectedMemberId: null,
      errorMessage: '',
      fallbackListName: '',
      snapshotLoading: false
    };
  },
  setup() {
    const listStore = useListStore();
    return { listStore };
  },
  async created() {
    const listId = this.$route.params.id as string | undefined;
    const fallbackName = listId ? `リスト${listId}` : '';

    console.log('[ItemListPage] created', listId);

    this.currentListId = listId ?? null;
    this.fallbackListName = fallbackName;

    if (!listId) {
      this.errorMessage = 'リストIDが指定されていません。';
      return;
    }

    await this.loadSnapshot(listId);
  },
  computed: {
    listName(): string {
      return this.listStore.name || this.fallbackListName || '買い物リスト';
    },
    members(): Member[] {
      return this.listStore.members;
    },
    items(): Item[] {
      return this.listStore.items;
    },
    filteredItems(): Item[] {
      const normalizedQuery = normalizeForSearch(this.searchQuery);
      if (!normalizedQuery) {
        return this.items;
      }
      return this.items.filter((item) => {
        const normalizedItemName = normalizeForSearch(item.name);
        return normalizedItemName.includes(normalizedQuery);
      });
    },
    memberNames(): string {
      return this.members.map((member) => member.displayName).join(' ・ ');
    },
    memberOptions(): Array<{ id: MemberId; name: string }> {
      return this.members.map((member) => ({
        id: member.id,
        name: member.displayName
      }));
    },
    itemSummary(): string {
      const incomplete = this.items.filter((item) => !item.completed).length;
      const completed = this.items.filter((item) => item.completed).length;
      if (completed === 0) {
        return `あと ${incomplete} 件`;
      }
      return `あと ${incomplete} 件 / 完了 ${completed} 件`;
    }
  },
  watch: {
    members(newMembers: Member[]) {
      if (!this.selectedMemberId && newMembers.length > 0) {
        this.selectedMemberId = newMembers[0]?.id ?? null;
      }
    }
  },
  methods: {
    async loadSnapshot(listId: string) {
      this.snapshotLoading = true;
      this.errorMessage = '';

      try {
        const snapshot = await fetchSnapshot(listId);
        this.listStore.applySnapshot(snapshot);
        if (!this.selectedMemberId && snapshot.members.length > 0) {
          this.selectedMemberId = snapshot.members[0]?.id ?? null;
        }
      } catch (err: any) {
        console.error('Failed to load snapshot', err);
        this.errorMessage = err?.message ?? 'リストの取得に失敗しました。';
      } finally {
        this.snapshotLoading = false;
      }
    },
    handleMemberSelect(selectedId: string) {
      this.selectedMemberId = selectedId;
    },
    onSearchInput(value: string): void {
      this.searchQuery = normalizeInput(value);
    },
    navigateToListEdit() {
      this.$router.push({
        name: 'ListEdit',
        params: { id: this.$route.params.id }
      });
    }
  }
});
</script>

<template>
  <ContentArea v-if="snapshotLoading" layout="center">
    <LoadingSpinner message="リストを読み込み中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="w-full">
      <!-- リストタイトル -->
      <div class="mb-8">
        <div class="flex flex-row justify-center space-x-2 items-center mb-1">
          <h2 class="text-2xl font-black text-charcoal-800 text-center mb-2">
            {{ listName }}
          </h2>
          <button
            type="button"
            @click="navigateToListEdit"
            aria-label="リスト名を編集"
            class="focus:outline-none focus:ring-2 focus:ring-charcoal-400 rounded"
          >
            <span class="text-charcoal-800" aria-hidden="true">✏️</span>
          </button>
        </div>
        <p class="text-sm text-charcoal-600 text-center">{{ memberNames }}</p>
      </div>

      <!-- エラーメッセージ -->
      <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
        {{ errorMessage }}
      </div>

      <!-- 新しいアイテム追加 -->
      <ItemAddForm @error="errorMessage = $event" />

      <!-- 検索ボックス -->
      <div v-if="items.length > 0" class="mb-4">
        <div class="flex px-2 py-2 border border-charcoal-200 bg-charcoal-100 rounded-md">
          <TextInput
            :type="'search'"
            :model-value="searchQuery"
            @update:model-value="onSearchInput"
            input-name="search"
            placeholder="検索..."
            aria-label="アイテムを検索"
            variant="inline"
          />
        </div>
      </div>
      <!-- チェック時に記録する購入者選択 + サマリー -->
      <div v-if="filteredItems.length > 0" class="w-full flex justify-between items-center mb-2">
        <span class="text-xs text-charcoal-400">{{ itemSummary }}</span>
        <div class="flex items-center gap-2 text-sm">
          <label for="memberSelect">
            <span class="text-charcoal-600 font-medium">買った人</span>
          </label>
          <DropDown
            selectId="memberSelect"
            selectName="member"
            :showArrow="true"
            :optionItems="memberOptions"
            width="fixed"
            v-model="selectedMemberId"
            @update:modelValue="handleMemberSelect"
          />
        </div>
      </div>
      <!-- アイテムリスト -->
      <ItemGroupList
        :filtered-items="filteredItems"
        :items="items"
        :search-query="searchQuery"
        :selected-member-id="selectedMemberId"
      />
    </div>
  </ContentArea>
</template>
