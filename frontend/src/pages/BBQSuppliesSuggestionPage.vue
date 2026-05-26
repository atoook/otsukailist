<script lang="ts">
import { defineComponent } from 'vue';
import ContentArea from '../components/ContentArea.vue';
import FixedBottomActionButton from '../components/FixedBottomActionButton.vue';
import IconCheck from '../components/icons/IconCheck.vue';
import IconChevronDown from '../components/icons/IconChevronDown.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import { createItem } from '@/api/item';
import { fetchSnapshot } from '@/api/list';
import { useMutation } from '@/composables/useMutation';
import { getErrorMessage } from '@/lib/http';
import { BBQ_SUPPLIES_SUGGESTION_PACK } from '@/lib/suggestions/bbqSuppliesSuggestions';
import { isSuggestionAlreadyAdded, toPlainItemPayload } from '@/lib/suggestions/suggestionUtils';
import { addOrUpdateListHistory } from '@/lib/userCache';
import { useListStore } from '@/stores/list';
import type { SuggestionItemDefinition } from '@/lib/suggestions/suggestionTypes';

type SuggestionSection = {
  key: string;
  label: string;
  defaultExpanded: boolean;
  items: SuggestionItemDefinition[];
};

export default defineComponent({
  name: 'BBQSuppliesSuggestionPage',
  components: {
    ContentArea,
    FixedBottomActionButton,
    IconCheck,
    IconChevronDown,
    LoadingSpinner
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  data(): {
    currentListId: string | null;
    selectedKeys: string[];
    collapsedGroupKeys: string[];
    loading: boolean;
    errorMessage: string;
  } {
    return {
      currentListId: null,
      selectedKeys: [],
      collapsedGroupKeys: BBQ_SUPPLIES_SUGGESTION_PACK.groups
        ?.filter((group) => group.defaultExpanded === false)
        .map((group) => group.key) ?? [],
      loading: false,
      errorMessage: ''
    };
  },
  async created() {
    const listId = this.$route.params.id as string | undefined;
    this.currentListId = listId ?? null;

    if (!listId) {
      this.errorMessage = 'リストIDが指定されていません。';
      return;
    }

    await this.loadPageData(listId);
  },
  computed: {
    pack(): typeof BBQ_SUPPLIES_SUGGESTION_PACK {
      return BBQ_SUPPLIES_SUGGESTION_PACK;
    },
    listName(): string {
      return this.listStore.name || '買い物リスト';
    },
    selectedCount(): number {
      return this.selectedKeys.length;
    },
    selectedSuggestions(): SuggestionItemDefinition[] {
      const selectedKeySet = new Set(this.selectedKeys);
      return this.pack.items.filter((item) => selectedKeySet.has(item.key) && !this.isAlreadyAdded(item));
    },
    sections(): SuggestionSection[] {
      const groups = this.pack.groups ?? [];
      const groupMap = new Map(groups.map((group) => [group.key, group.label]));
      const sections = groups.map((group) => ({
        key: group.key,
        label: group.label,
        defaultExpanded: group.defaultExpanded !== false,
        items: this.pack.items.filter((item) => item.group === group.key)
      }));
      const ungroupedItems = this.pack.items.filter((item) => item.group == null || !groupMap.has(item.group));

      if (ungroupedItems.length > 0) {
        sections.push({
          key: 'ungrouped',
          label: 'その他',
          defaultExpanded: true,
          items: ungroupedItems
        });
      }

      return sections.filter((section) => section.items.length > 0);
    },
    canAddSelected(): boolean {
      return this.selectedSuggestions.length > 0 && !this.mutationLoading;
    },
    addButtonLabel(): string {
      if (this.mutationLoading) {
        return '追加中...';
      }
      return this.selectedCount > 0 ? `選択した ${this.selectedCount} 件を追加` : 'アイテムを選択してください';
    }
  },
  methods: {
    async loadPageData(listId: string): Promise<void> {
      this.loading = true;
      this.errorMessage = '';

      try {
        const snapshot = await fetchSnapshot(listId);
        this.listStore.applySnapshot(snapshot);
        addOrUpdateListHistory({ listId, name: snapshot.name });
      } catch (err: unknown) {
        console.error('Failed to load suggestion page', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの取得に失敗しました。';
      } finally {
        this.loading = false;
      }
    },
    navigateToList(): void {
      if (!this.currentListId) {
        this.$router.push({ name: 'Welcome' });
        return;
      }
      this.$router.push({
        name: 'ItemList',
        params: { id: this.currentListId }
      });
    },
    isSelected(item: SuggestionItemDefinition): boolean {
      return this.selectedKeys.includes(item.key);
    },
    isAlreadyAdded(item: SuggestionItemDefinition): boolean {
      return isSuggestionAlreadyAdded(item, this.listStore.items);
    },
    isSectionCollapsed(section: SuggestionSection): boolean {
      return this.collapsedGroupKeys.includes(section.key);
    },
    toggleSection(section: SuggestionSection): void {
      if (this.isSectionCollapsed(section)) {
        this.collapsedGroupKeys = this.collapsedGroupKeys.filter((key) => key !== section.key);
        return;
      }
      this.collapsedGroupKeys = [...this.collapsedGroupKeys, section.key];
    },
    sectionSelectedCount(section: SuggestionSection): number {
      const selectedKeySet = new Set(this.selectedKeys);
      return section.items.filter((item) => selectedKeySet.has(item.key)).length;
    },
    sectionAddedCount(section: SuggestionSection): number {
      return section.items.filter((item) => this.isAlreadyAdded(item)).length;
    },
    toggleItem(item: SuggestionItemDefinition): void {
      if (this.isAlreadyAdded(item) || this.mutationLoading) {
        return;
      }
      if (this.isSelected(item)) {
        this.selectedKeys = this.selectedKeys.filter((key) => key !== item.key);
        return;
      }
      this.selectedKeys = [...this.selectedKeys, item.key];
    },
    tileClass(item: SuggestionItemDefinition): string[] {
      const selected = this.isSelected(item);
      const alreadyAdded = this.isAlreadyAdded(item);
      return [
        'relative flex aspect-square w-full flex-col items-center justify-center rounded-lg border p-3 text-center',
        'transition-[background-color,border-color,box-shadow,transform]',
        'focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300',
        alreadyAdded
          ? 'cursor-not-allowed border-charcoal-200 bg-charcoal-50 text-charcoal-400'
          : selected
            ? 'border-wood-500 bg-wood-100 text-charcoal-800 shadow-md'
            : 'border-wood-200 bg-white text-charcoal-700 shadow-sm hover:border-wood-300 hover:bg-wood-50 hover:shadow-md active:scale-[0.98]'
      ];
    },
    async addSelectedItems(): Promise<void> {
      const listId = this.currentListId;
      if (!listId || !this.canAddSelected) {
        return;
      }

      this.errorMessage = '';

      try {
        for (const suggestion of this.selectedSuggestions) {
          const result = await this.mutationRun(() => createItem(listId, toPlainItemPayload(suggestion)));
          if (result.applied) {
            this.listStore.upsertItem(result.data);
          }
        }
        this.selectedKeys = [];
        this.navigateToList();
      } catch (err: unknown) {
        console.error('Failed to add suggested items', err);
        this.errorMessage = getErrorMessage(err) ?? '周辺アイテムの追加に失敗しました。';
      }
    }
  }
});
</script>

<template>
  <ContentArea v-if="loading" layout="center">
    <LoadingSpinner message="周辺アイテムを読み込み中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="mx-auto flex min-h-full w-full max-w-3xl flex-col pb-24">
      <div class="mb-5">
        <button type="button" class="mb-3 text-sm font-medium text-charcoal-500 hover:text-charcoal-800" @click="navigateToList">
          ← リストに戻る
        </button>
        <h2 class="text-2xl font-black text-charcoal-800">{{ pack.label }}</h2>
        <p class="mt-1 text-sm text-charcoal-600">{{ listName }} に足したいものを選んでください</p>
      </div>

      <div v-if="errorMessage" class="mb-4 rounded-lg border border-ember-300 bg-ember-100 p-3 text-sm text-ember-700">
        {{ errorMessage }}
      </div>

      <div v-if="!errorMessage" class="space-y-6">
        <section v-for="section in sections" :key="section.key">
          <button
            type="button"
            class="mb-2 flex w-full items-center justify-between gap-3 rounded-md px-1 py-1 text-left text-sm font-bold text-charcoal-700 transition-colors hover:bg-wood-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300"
            :aria-expanded="!isSectionCollapsed(section)"
            @click="toggleSection(section)"
          >
            <span>{{ section.label }}</span>
            <span class="flex items-center gap-2 text-xs font-medium text-charcoal-500">
              <span v-if="sectionSelectedCount(section) > 0">選択 {{ sectionSelectedCount(section) }}</span>
              <span v-if="sectionAddedCount(section) > 0">追加済み {{ sectionAddedCount(section) }}</span>
              <IconChevronDown
                :class="[
                  'text-charcoal-500 transition-transform',
                  isSectionCollapsed(section) ? '-rotate-90' : 'rotate-0'
                ]"
              />
            </span>
          </button>
          <div v-if="!isSectionCollapsed(section)" class="grid grid-cols-2 gap-3 sm:grid-cols-3">
            <button
              v-for="item in section.items"
              :key="item.key"
              type="button"
              :class="tileClass(item)"
              :disabled="isAlreadyAdded(item) || mutationLoading"
              :aria-pressed="isSelected(item)"
              @click="toggleItem(item)"
            >
              <span
                v-if="isSelected(item)"
                class="absolute right-2 top-2 flex h-6 w-6 items-center justify-center rounded-full bg-wood-600 text-white"
              >
                <IconCheck />
              </span>
              <span
                v-if="isAlreadyAdded(item)"
                class="absolute right-2 top-2 rounded-full bg-charcoal-100 px-2 py-0.5 text-[11px] font-semibold text-charcoal-500"
              >
                追加済み
              </span>
              <span class="line-clamp-2 text-base font-bold leading-snug">
                {{ item.name }}
              </span>
            </button>
          </div>
        </section>
      </div>

      <FixedBottomActionButton
        v-if="!errorMessage"
        :disabled="!canAddSelected"
        @click="addSelectedItems"
      >
        {{ addButtonLabel }}
      </FixedBottomActionButton>
    </div>
  </ContentArea>
</template>
