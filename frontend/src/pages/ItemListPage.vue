<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import CheckBox from '../components/CheckBox.vue';
import MainButton from '../components/MainButton.vue';
import TextInput from '../components/TextInput.vue';
import ItemBox from '../components/ItemBox.vue';
import DropDown from '../components/DropDown.vue';
import type { Item, ItemId } from '../types/item';
import { normalizeText, normalizeInput, normalizeForSearch } from '../utils/text-normalization';
import type { Member, MemberId } from '@/types/member';
import { fetchSnapshot } from '@/api/list';
import { createItem, deleteItem as deleteItemApi, updateItem } from '@/api/item';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';

export default defineComponent({
  name: 'ItemListPage',
  components: {
    ContentArea,
    CheckBox,
    MainButton,
    TextInput,
    ItemBox,
    DropDown
  },
  data(): {
    currentListId: string | null;
    newItemName: string;
    searchQuery: string;
    selectedMemberId: MemberId | null;
    errorMessage: string;
    fallbackListName: string;
    snapshotLoading: boolean;
  } {
    return {
      currentListId: null,
      newItemName: '',
      searchQuery: '',
      selectedMemberId: null,
      errorMessage: '',
      fallbackListName: '',
      snapshotLoading: false
    };
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();

    return {
      listStore,
      mutationRun: run,
      mutationLoading: loading
    };
  },
  async created() {
    const listId = this.$route.params.id as string | undefined;
    const fallbackName = listId ? `リスト${listId}` : '';

    this.currentListId = listId ?? null;
    this.fallbackListName = fallbackName;

    if (!listId) {
      this.errorMessage = 'リストIDが指定されていません。';
      return;
    }

    await this.loadSnapshot(listId);
  },
  computed: {
    isLoading(): boolean {
      return this.snapshotLoading || this.mutationLoading;
    },
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
    memberMap(): Map<MemberId, Member> {
      return this.listStore.memberMap;
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
    getMemberBadgeVariant(item: Item): string {
      if (item.completed && this.selectedMemberId && item.completedByMemberId === this.selectedMemberId) {
        return 'primary';
      }
      return 'secondary';
    },
    handleMemberSelect(selectedId: string) {
      this.selectedMemberId = selectedId;
    },
    async addItem() {
      const normalizedName = normalizeText(this.newItemName);
      const listId = this.listStore.listId;

      if (!normalizedName) {
        return;
      }

      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        return;
      }

      try {
        const result = await this.mutationRun(() => createItem(listId, { name: normalizedName }));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.newItemName = '';
        }
      } catch (err: any) {
        console.error('Failed to create item', err);
        this.errorMessage = err?.message ?? 'アイテムの作成に失敗しました。';
      }
    },
    onItemNameInput(value: string): void {
      this.newItemName = normalizeInput(value);
    },
    onSearchInput(value: string): void {
      this.searchQuery = normalizeInput(value);
    },
    async toggleItem(item: Item) {
      this.errorMessage = '';

      const listId = this.listStore.listId;
      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        return;
      }

      const wasCompleted = item.completed;
      const updatedItem: Partial<Item> = {
        completed: !wasCompleted,
        completedByMemberId: wasCompleted ? null : (this.selectedMemberId ?? null),
        completedAt: wasCompleted ? null : new Date().toISOString()
      };

      try {
        const result = await this.mutationRun(() => updateItem(listId, item.id, updatedItem));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
        }
      } catch (err: any) {
        console.error('Failed to update item', err);
        this.errorMessage = err?.message ?? 'アイテムの更新に失敗しました。';
        this.showErrorFeedback();
      }
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
    async deleteItem(itemId: ItemId) {
      const listId = this.listStore.listId;

      if (!listId) {
        this.errorMessage = 'リストが初期化されていません。';
        return;
      }

      try {
        const result = await this.mutationRun(() => deleteItemApi(listId, itemId));
        if (result.applied) {
          this.listStore.removeItem(itemId);
        }
      } catch (err: any) {
        console.error('Failed to delete item', err);
        this.errorMessage = err?.message ?? 'アイテムの削除に失敗しました。';
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
        return;
      }

      try {
        const result = await this.mutationRun(() => updateItem(listId, updatedItem.id, { name: normalizedItemName }));
        if (result.applied) {
          this.listStore.upsertItem(result.data);
        }
      } catch (err: any) {
        console.error('Failed to rename item', err);
        this.errorMessage = err?.message ?? 'アイテムの更新に失敗しました。';
        this.showErrorFeedback();
      }
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
  <ContentArea>
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
      <div class="mb-6">
        <div class="flex gap-2 px-3 py-3 border border-wood-300 bg-wood-100 rounded-lg shadow-sm">
          <TextInput
            :model-value="newItemName"
            @update:model-value="onItemNameInput"
            @enter="addItem"
            input-name="newItem"
            placeholder="アイテムを追加..."
            variant="inline"
          />
          <MainButton @click="addItem" :disabled="!newItemName.trim() || isLoading"> 追加 </MainButton>
        </div>
      </div>

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
      <!-- チェック時に記録する購入者選択 -->
      <div v-if="filteredItems.length > 0" class="w-full flex justify-end items-center mb-2">
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
      <div class="space-y-3">
        <ItemBox
          v-for="item in filteredItems"
          :key="item.id"
          :item="item"
          :memberBadgeVariant="getMemberBadgeVariant(item)"
          :completedMemberName="getCompletedMemberName(item) || ''"
          @toggle="toggleItem"
          @delete="deleteItem"
          @modify="modifyItem"
        />

        <!-- アイテムがない場合 -->
        <div v-if="items.length === 0" class="text-center text-charcoal-600 py-8">
          <div class="text-4xl mb-3">🍖</div>
          まだアイテムがありません。<br />
          上のフォームからアイテムを追加してください。
        </div>

        <!-- 検索結果がない場合 -->
        <div v-else-if="filteredItems.length === 0" class="text-center text-charcoal-600 py-8">
          <div class="text-4xl mb-3">🔍</div>
          「{{ searchQuery }}」に一致するアイテムが見つかりませんでした。
        </div>
      </div>
    </div>
  </ContentArea>
</template>
