<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import TextInput from '../components/TextInput.vue';
import DropDown from '../components/DropDown.vue';
import ItemAddForm from '../components/ItemAddForm.vue';
import ItemGroupList from '../components/ItemGroupList.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import IconButton from '../components/IconButton.vue';
import PillActionButton from '../components/PillActionButton.vue';
import IconEdit from '../components/icons/IconEdit.vue';
import IconRefresh from '../components/icons/IconRefresh.vue';
import IconCelebration from '../components/icons/IconCelebration.vue';
import IconSparkles from '../components/icons/IconSparkles.vue';
import IconTools from '../components/icons/IconTools.vue';
import type { Item } from '../types/item';
import type { ItemCategory } from '../types/item-category';
import type { ItemPreparationType } from '../types/item-preparation-type';
import { normalizeInput } from '../utils/text-normalization';
import { formatActivityAt } from '../utils/date-format';
import type { Member, MemberId } from '@/types/member';
import { fetchSnapshot } from '@/api/list';
import { useListStore } from '@/stores/list';
import { getErrorMessage } from '@/lib/http';
import { getSelectedMemberId, setSelectedMemberId, addOrUpdateListHistory } from '@/lib/userCache';
import { filterItems } from '@/utils/item-filtering';
import { PRIMARY_LIST_TEMPLATE } from '@/lib/listTemplateRegistry';

export default defineComponent({
  name: 'ItemListPage',
  components: {
    ContentArea,
    TextInput,
    DropDown,
    ItemAddForm,
    ItemGroupList,
    LoadingSpinner,
    IconButton,
    PillActionButton,
    IconEdit,
    IconRefresh,
    IconCelebration,
    IconSparkles,
    IconTools
  },
  data(): {
    currentListId: string | null;
    searchQuery: string;
    selectedMemberId: MemberId | null;
    memberFilterId: MemberId | null;
    categoryFilter: ItemCategory | null;
    preparationTypeFilter: ItemPreparationType | null;
    errorMessage: string;
    fallbackListName: string;
    snapshotLoading: boolean;
    hasGenerationConfig: boolean;
    generationConfigLoading: boolean;
  } {
    return {
      currentListId: null,
      searchQuery: '',
      selectedMemberId: null,
      memberFilterId: null,
      categoryFilter: null,
      preparationTypeFilter: null,
      errorMessage: '',
      fallbackListName: '',
      snapshotLoading: false,
      hasGenerationConfig: false,
      generationConfigLoading: false
    };
  },
  setup() {
    const listStore = useListStore();
    return { listStore };
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
      return filterItems(this.items, {
        searchQuery: this.searchQuery,
        memberId: this.memberFilterId,
        category: this.categoryFilter,
        preparationType: this.preparationTypeFilter
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
      if (incomplete === 0) {
        return '全て完了';
      }
      return `あと ${incomplete} 件 ・ 完了 ${completed} 件`;
    },
    allCompleted(): boolean {
      return this.items.length > 0 && this.items.every((item) => item.completed);
    },
    emptyFilterMessage(): string {
      if (this.searchQuery) {
        return `「${this.searchQuery}」に一致するアイテムが見つかりませんでした。`;
      }
      return '条件に一致するアイテムが見つかりませんでした。';
    },
    formattedLastItemActivityAt(): string | null {
      return formatActivityAt(this.listStore.lastItemActivityAt);
    },
    generationButtonLabel(): string {
      return this.hasGenerationConfig ? PRIMARY_LIST_TEMPLATE.updateLabel : PRIMARY_LIST_TEMPLATE.createLabel;
    },
    generationButtonDescription(): string {
      return this.hasGenerationConfig ? '条件を見直して再生成できます' : PRIMARY_LIST_TEMPLATE.description;
    },
    shouldShowSuggestionLink(): boolean {
      return this.items.length > 0 && this.hasGenerationConfig && !this.generationConfigLoading;
    }
  },
  watch: {
    members(newMembers: Member[]) {
      if (!this.selectedMemberId && newMembers.length > 0) {
        this.selectedMemberId = newMembers[0]?.id ?? null;
      }
      if (this.memberFilterId && !newMembers.some((member) => member.id === this.memberFilterId)) {
        this.memberFilterId = null;
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

        // キャッシュから selectedMemberId を復元し、メンバー一覧で検証する
        const cachedMemberId = getSelectedMemberId(listId);
        const memberIds = snapshot.members.map((m) => m.id);
        if (cachedMemberId && memberIds.includes(cachedMemberId)) {
          this.selectedMemberId = cachedMemberId;
        } else if (snapshot.members.length > 0) {
          this.selectedMemberId = snapshot.members[0]?.id ?? null;
        }

        addOrUpdateListHistory({ listId, name: snapshot.name });
        await this.loadGenerationConfig(listId);
      } catch (err: unknown) {
        console.error('Failed to load snapshot', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの取得に失敗しました。';
      } finally {
        this.snapshotLoading = false;
      }
    },
    async loadGenerationConfig(listId: string) {
      this.generationConfigLoading = true;

      try {
        this.hasGenerationConfig = await PRIMARY_LIST_TEMPLATE.hasConfig(listId);
      } catch (err: unknown) {
        console.error('Failed to load generation config', err);
        this.errorMessage = getErrorMessage(err) ?? '生成設定の取得に失敗しました。';
      } finally {
        this.generationConfigLoading = false;
      }
    },
    openGenerationPage(): void {
      this.errorMessage = '';
      this.$router.push({
        name: PRIMARY_LIST_TEMPLATE.routeName,
        params: { id: this.currentListId ?? this.$route.params.id }
      });
    },
    openSuggestionPage(): void {
      this.errorMessage = '';
      this.$router.push({
        name: 'BBQSuppliesSuggestion',
        params: { id: this.currentListId ?? this.$route.params.id }
      });
    },
    handleMemberSelect(selectedId: string) {
      this.selectedMemberId = selectedId;
      if (this.currentListId) {
        setSelectedMemberId(this.currentListId, selectedId);
      }
    },
    onSearchInput(value: string): void {
      this.searchQuery = normalizeInput(value);
    },
    handleMemberFilter(memberId: MemberId): void {
      this.memberFilterId = memberId;
    },
    clearMemberFilter(): void {
      this.memberFilterId = null;
    },
    handleCategoryFilter(category: ItemCategory): void {
      this.categoryFilter = category;
    },
    clearCategoryFilter(): void {
      this.categoryFilter = null;
    },
    handlePreparationTypeFilter(preparationType: ItemPreparationType): void {
      this.preparationTypeFilter = preparationType;
    },
    clearPreparationTypeFilter(): void {
      this.preparationTypeFilter = null;
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
      <div :class="items.length > 0 ? 'mb-4' : 'mb-8'">
        <div class="flex flex-row justify-center space-x-2 items-center mb-1">
          <h2 class="text-2xl font-black text-charcoal-800 text-center">
            {{ listName }}
          </h2>
          <IconButton @click="navigateToListEdit" aria-label="リスト名を編集" variant="ghost" size="small">
            <IconEdit />
          </IconButton>
        </div>
        <p class="text-sm text-charcoal-600 text-center">{{ memberNames }}</p>
        <div v-if="items.length > 0" class="mt-2 flex flex-wrap justify-end gap-2">
          <PillActionButton
            v-if="shouldShowSuggestionLink"
            aria-label="周辺アイテムを確認"
            title="周辺アイテムを確認"
            @click="openSuggestionPage"
          >
            <template #icon><IconTools /></template>
            <span>周辺アイテム</span>
          </PillActionButton>
          <PillActionButton
            :disabled="generationConfigLoading"
            :aria-label="generationConfigLoading ? '生成設定を確認中' : generationButtonLabel"
            :title="generationConfigLoading ? '生成設定を確認中' : generationButtonLabel"
            @click="openGenerationPage"
          >
            <template #icon><IconSparkles /></template>
            <span>{{ generationConfigLoading ? '確認中...' : generationButtonLabel }}</span>
          </PillActionButton>
        </div>
      </div>

      <!-- エラーメッセージ -->
      <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
        {{ errorMessage }}
      </div>

      <!-- 新しいアイテム追加 -->
      <ItemAddForm
        :member-filter-id="memberFilterId"
        :category-filter="categoryFilter"
        :preparation-type-filter="preparationTypeFilter"
        @error="errorMessage = $event"
      />

      <div v-if="items.length === 0" class="-mt-3 mb-4">
        <button
          type="button"
          class="group flex w-full items-center gap-3 rounded-lg border border-wood-200 bg-white px-3 py-2 text-left shadow-sm transition-[background-color,border-color,box-shadow] hover:border-wood-300 hover:bg-wood-50 hover:shadow-md focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300 disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="generationConfigLoading"
          @click="openGenerationPage"
        >
          <span class="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-wood-100 text-ember-700">
            <IconSparkles />
          </span>
          <span class="min-w-0 flex-1">
            <span class="block truncate text-sm font-semibold text-charcoal-800">
              {{ generationConfigLoading ? '確認中...' : generationButtonLabel }}
            </span>
            <span class="mt-0.5 block truncate text-xs text-charcoal-500">
              {{ generationConfigLoading ? '生成設定を確認しています' : generationButtonDescription }}
            </span>
          </span>
          <span class="shrink-0 text-lg leading-none text-charcoal-400 transition-transform group-hover:translate-x-0.5">
            &gt;
          </span>
        </button>
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
      <!-- チェック時に記録する購入者選択 + サマリー -->
      <div
        v-if="filteredItems.length > 0"
        class="@container flex w-full flex-wrap items-center justify-between gap-x-3 gap-y-2 mb-2"
      >
        <div class="flex min-w-max flex-col gap-0.5 @max-[19rem]:mx-auto">
          <span class="inline-flex whitespace-nowrap text-xs text-charcoal-600"
            >{{ itemSummary }}<IconCelebration v-if="allCompleted" class="ml-1"
          /></span>
          <span
            v-if="formattedLastItemActivityAt"
            class="inline-flex items-center gap-1 whitespace-nowrap text-xs text-charcoal-500"
          >
            最終更新: {{ formattedLastItemActivityAt }}
            <IconButton
              @click="currentListId && loadSnapshot(currentListId)"
              :disabled="snapshotLoading"
              aria-label="リストを再読み込み"
              variant="muted"
              size="xsmall"
              :icon-class="{ 'animate-spin': snapshotLoading }"
            >
              <IconRefresh />
            </IconButton>
          </span>
        </div>
        <div class="ml-auto flex shrink-0 items-center gap-2 text-sm">
          <label for="memberSelect">
            <span class="whitespace-nowrap text-charcoal-600 font-medium">買った人</span>
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
        :empty-result-message="emptyFilterMessage"
        :selected-member-id="selectedMemberId"
        :member-filter-id="memberFilterId"
        :category-filter="categoryFilter"
        :preparation-type-filter="preparationTypeFilter"
        @member-filter="handleMemberFilter"
        @clear-member-filter="clearMemberFilter"
        @category-filter="handleCategoryFilter"
        @clear-category-filter="clearCategoryFilter"
        @preparation-type-filter="handlePreparationTypeFilter"
        @clear-preparation-type-filter="clearPreparationTypeFilter"
      />
    </div>
  </ContentArea>
</template>
