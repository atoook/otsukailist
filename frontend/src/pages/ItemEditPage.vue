<script lang="ts">
import { defineComponent } from 'vue';
import { NavigationFailureType, isNavigationFailure } from 'vue-router';
import ContentArea from '../components/ContentArea.vue';
import MainButton from '../components/MainButton.vue';
import TextInput from '../components/TextInput.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import DropDown from '../components/DropDown.vue';
import IconTools from '../components/icons/IconTools.vue';
import IconUsers from '../components/icons/IconUsers.vue';
import type { Item } from '../types/item';
import { ITEM_CATEGORIES, type ItemCategory } from '../types/item-category';
import type { Member, MemberId } from '../types/member';
import { UNIT_DEFINITIONS, type BaseUnit, type ItemOrigin, type RegenerationPolicy } from '../types/list-generation';
import { normalizeText } from '../utils/text-normalization';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { updateItem, type UpdateItemPayload } from '@/api/item';
import { fetchSnapshot } from '@/api/list';
import { getErrorMessage } from '@/lib/http';
import { BBQ_GENERATION_RULES } from '@/lib/listGenerationConstants';

const UNASSIGNED_MEMBER_VALUE = '';
const UNCATEGORIZED_VALUE = '';
const UNSET_GENERATOR_KEY_VALUE = '';
const UNSELECTED_BASE_UNIT_VALUE = '';

type ItemEditMode = 'plain' | 'manual_none' | 'generated_auto' | 'generated_locked';

export default defineComponent({
  name: 'ItemEditPage',
  components: {
    ContentArea,
    MainButton,
    TextInput,
    LoadingSpinner,
    DropDown,
    IconTools,
    IconUsers
  },
  data(): {
    currentListId: string | null;
    itemName: string;
    members: Member[];
    isCompleted: boolean;
    originalAssignedMemberId: MemberId | null;
    selectedMemberId: MemberId | typeof UNASSIGNED_MEMBER_VALUE;
    currentItemId: string | null;
    errorMessage: string;
    snapshotLoading: boolean;
    selectedCategory: ItemCategory | typeof UNCATEGORIZED_VALUE;
    itemEditMode: ItemEditMode;
    quantifiedQuantity: string;
    quantifiedBaseUnit: BaseUnit | typeof UNSELECTED_BASE_UNIT_VALUE;
    quantifiedGeneratorKey: string | typeof UNSET_GENERATOR_KEY_VALUE;
  } {
    return {
      currentListId: null,
      itemName: '',
      members: [],
      isCompleted: false,
      originalAssignedMemberId: null,
      selectedMemberId: UNASSIGNED_MEMBER_VALUE,
      currentItemId: null,
      errorMessage: '',
      snapshotLoading: false,
      selectedCategory: UNCATEGORIZED_VALUE,
      itemEditMode: 'plain',
      quantifiedQuantity: '',
      quantifiedBaseUnit: UNSELECTED_BASE_UNIT_VALUE,
      quantifiedGeneratorKey: UNSET_GENERATOR_KEY_VALUE
    };
  },
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  async created() {
    const listId = this.$route.params.id as string | null;
    const itemId = this.$route.params.itemId as string | undefined;
    this.currentListId = listId ?? null;
    this.currentItemId = itemId ?? null;

    if (!listId || !itemId) {
      this.errorMessage = 'アイテムIDが無効です';
      return;
    }

    if (this.listStore.listId !== listId || !this.findCurrentItem()) {
      await this.loadSnapshot(listId);
    }

    if (this.errorMessage) {
      return;
    }

    this.applyCurrentItem();
  },
  methods: {
    findCurrentItem(): Item | undefined {
      return this.listStore.items.find((i: Item) => i.id === this.currentItemId);
    },
    applyCurrentItem(): void {
      const item = this.findCurrentItem();
      if (!item) {
        this.errorMessage = 'アイテムが見つかりませんでした。';
        return;
      }
      this.itemName = item.name;
      this.isCompleted = item.completed;
      this.originalAssignedMemberId = item.assignedMemberId ?? null;
      this.selectedMemberId =
        (item.completed ? item.completedByMemberId : item.assignedMemberId) ?? UNASSIGNED_MEMBER_VALUE;
      this.selectedCategory = item.category ?? UNCATEGORIZED_VALUE;
      this.members = this.listStore.members.map((m) => ({ ...m }));
      this.applyQuantifiedItem(item);
    },
    applyQuantifiedItem(item: Item): void {
      if (item.itemType !== 'quantified' || !item.quantified) {
        this.itemEditMode = 'plain';
        this.quantifiedQuantity = '';
        this.quantifiedBaseUnit = UNSELECTED_BASE_UNIT_VALUE;
        this.quantifiedGeneratorKey = UNSET_GENERATOR_KEY_VALUE;
        return;
      }

      this.itemEditMode = this.resolveItemEditMode(item.quantified.origin, item.quantified.regenerationPolicy);
      this.quantifiedQuantity = String(item.quantified.quantity);
      this.quantifiedBaseUnit = item.quantified.baseUnit;
      this.quantifiedGeneratorKey = item.quantified.generatorKey ?? UNSET_GENERATOR_KEY_VALUE;
      if (this.usesGeneratedCategory && this.selectedGeneratorRule) {
        this.selectedCategory = this.selectedGeneratorRule.category;
      }
    },
    resolveItemEditMode(origin: ItemOrigin, regenerationPolicy: RegenerationPolicy): ItemEditMode {
      if (origin === 'generated' && regenerationPolicy === 'auto') {
        return 'generated_auto';
      }
      if (origin === 'generated' && regenerationPolicy === 'locked') {
        return 'generated_locked';
      }
      return 'manual_none';
    },
    async loadSnapshot(listId: string): Promise<void> {
      this.snapshotLoading = true;
      this.errorMessage = '';

      try {
        const snapshot = await fetchSnapshot(listId);
        this.listStore.applySnapshot(snapshot);
      } catch (err: unknown) {
        console.error('Failed to load snapshot', err);
        this.errorMessage = getErrorMessage(err) ?? 'リストの取得に失敗しました。';
      } finally {
        this.snapshotLoading = false;
      }
    },
    onItemNameInput(value: string): void {
      this.itemName = value;
    },
    getCurrentSelectedMemberId(): MemberId | null {
      return this.selectedMemberId || null;
    },
    getSelectedCategory(): ItemCategory | null {
      if (this.hasSelectedBaseUnit && this.usesGeneratedCategory && this.selectedGeneratorRule) {
        return this.selectedGeneratorRule.category;
      }
      return this.selectedCategory || null;
    },
    setSelectedCategory(value: string): void {
      this.selectedCategory = value as ItemCategory | typeof UNCATEGORIZED_VALUE;
    },
    setQuantifiedBaseUnit(value: string): void {
      this.quantifiedBaseUnit = value as BaseUnit | typeof UNSELECTED_BASE_UNIT_VALUE;
      if (this.quantifiedBaseUnit === UNSELECTED_BASE_UNIT_VALUE) {
        this.quantifiedQuantity = '';
      }
    },
    buildUpdatePayload(normalizedName: string): UpdateItemPayload {
      const payload: UpdateItemPayload = {
        name: normalizedName,
        category: this.getSelectedCategory()
      };

      if (this.hasSelectedBaseUnit) {
        payload.itemType = 'quantified';
        payload.quantified = {
          quantity: this.parsedQuantifiedQuantity,
          baseUnit: this.quantifiedBaseUnit as BaseUnit,
          origin: this.currentOrigin,
          regenerationPolicy: this.currentRegenerationPolicy,
          generatorKey: this.isGeneratedMode ? this.quantifiedGeneratorKey || null : null
        };
      } else {
        payload.itemType = 'plain';
      }

      if (this.isCompleted) {
        payload.completed = true;
        payload.completedByMemberId = this.getCurrentSelectedMemberId();
        return payload;
      }

      const currentAssignedMemberId = this.getCurrentSelectedMemberId();
      if (currentAssignedMemberId !== this.originalAssignedMemberId) {
        payload.assignedMemberId = currentAssignedMemberId;
      }

      return payload;
    },
    async updateItem(): Promise<void> {
      if (!this.currentListId || !this.currentItemId) {
        this.errorMessage = 'アイテムIDが無効です';
        return;
      }
      const normalizedName = this.normalizedItemName;
      if (!normalizedName) {
        this.errorMessage = 'アイテム名を入力してください。';
        return;
      }
      if (this.isCompleted && !this.getCurrentSelectedMemberId()) {
        this.errorMessage = '買った人を選択してください。';
        return;
      }
      if (this.hasQuantifiedDraftInput && !this.hasValidQuantifiedInput) {
        this.errorMessage = '数量付きアイテムの各項目を正しく入力してください。';
        return;
      }
      try {
        const result = await this.mutationRun(() =>
          updateItem(this.currentListId!, this.currentItemId!, this.buildUpdatePayload(normalizedName))
        );
        if (result.applied) {
          this.listStore.upsertItem(result.data);
          this.errorMessage = '';
          await this.navigateToItemList('アイテム一覧画面への移動に失敗しました。');
        } else {
          this.errorMessage = 'アイテムの更新が反映されませんでした。時間をおいて再試行してください。';
        }
      } catch (err: unknown) {
        console.error('Failed to update item', err);
        this.errorMessage = getErrorMessage(err) ?? 'アイテムの更新に失敗しました。';
      }
    },
    async cancelUpdate(): Promise<void> {
      if (this.isLoading) {
        return;
      }

      await this.navigateToItemList('リスト画面への移動に失敗しました。');
    },
    async navigateToItemList(fallbackMessage: string): Promise<void> {
      if (!this.currentListId) {
        this.errorMessage = 'リストIDが無効です';
        return;
      }

      try {
        const failure = await this.$router.push({ name: 'ItemList', params: { id: this.currentListId } });
        if (isNavigationFailure(failure, NavigationFailureType.duplicated)) {
          return;
        }
        if (failure) {
          throw failure;
        }
      } catch (err: unknown) {
        if (this.isIgnoredNavigationError(err)) {
          return;
        }
        console.error('Failed to navigate to item list', err);
        this.errorMessage = getErrorMessage(err) ?? fallbackMessage;
      }
    },
    isIgnoredNavigationError(err: unknown): boolean {
      if (isNavigationFailure(err, NavigationFailureType.duplicated)) {
        return true;
      }

      return err instanceof Error && err.name === 'NavigationDuplicated';
    }
  },
  computed: {
    normalizedItemName(): string {
      return normalizeText(this.itemName);
    },
    normalizedQuantifiedQuantity(): string {
      return normalizeText(this.quantifiedQuantity);
    },
    parsedQuantifiedQuantity(): number {
      return Number(this.normalizedQuantifiedQuantity);
    },
    hasRequiredInput(): boolean {
      return (
        !!this.normalizedItemName &&
        (!this.isCompleted || !!this.getCurrentSelectedMemberId()) &&
        (!this.hasQuantifiedDraftInput || this.hasValidQuantifiedInput)
      );
    },
    hasQuantifiedDraftInput(): boolean {
      return this.hasSelectedBaseUnit || !!this.normalizedQuantifiedQuantity;
    },
    hasValidQuantifiedInput(): boolean {
      return (
        !!this.normalizedItemName &&
        !!this.normalizedQuantifiedQuantity &&
        Number.isInteger(this.parsedQuantifiedQuantity) &&
        Number.isSafeInteger(this.parsedQuantifiedQuantity) &&
        this.parsedQuantifiedQuantity >= 0 &&
        !!this.quantifiedBaseUnit &&
        (!this.requiresGeneratorKey || !!this.quantifiedGeneratorKey)
      );
    },
    hasSelectedBaseUnit(): boolean {
      return !!this.quantifiedBaseUnit;
    },
    isGeneratedMode(): boolean {
      return this.itemEditMode === 'generated_auto' || this.itemEditMode === 'generated_locked';
    },
    usesGeneratedCategory(): boolean {
      return this.itemEditMode === 'generated_auto';
    },
    requiresGeneratorKey(): boolean {
      return this.isGeneratedMode;
    },
    currentOrigin(): ItemOrigin {
      return this.isGeneratedMode ? 'generated' : 'manual';
    },
    currentRegenerationPolicy(): RegenerationPolicy {
      if (this.itemEditMode === 'generated_auto') {
        return 'auto';
      }
      if (this.itemEditMode === 'generated_locked') {
        return 'locked';
      }
      return 'none';
    },
    currentCategoryLabel(): string {
      const category = this.getSelectedCategory();
      if (!category) {
        return '未分類';
      }
      return Object.values(ITEM_CATEGORIES).find((itemCategory) => itemCategory.code === category)?.label ?? category;
    },
    selectedGeneratorRule(): (typeof BBQ_GENERATION_RULES)[keyof typeof BBQ_GENERATION_RULES] | undefined {
      if (!this.quantifiedGeneratorKey) {
        return undefined;
      }
      return Object.values(BBQ_GENERATION_RULES).find((rule) => rule.generatorKey === this.quantifiedGeneratorKey);
    },
    isLoading(): boolean {
      return this.mutationLoading || this.snapshotLoading;
    },
    memberLabel(): string {
      return this.isCompleted ? '買った人' : '買う人';
    },
    categoryOptions(): Array<{ id: string; name: string }> {
      return [
        { id: UNCATEGORIZED_VALUE, name: '未分類' },
        ...Object.values(ITEM_CATEGORIES)
          .sort((a, b) => a.sortOrder - b.sortOrder)
          .map((category) => ({ id: category.code, name: category.label }))
      ];
    },
    baseUnitOptions(): Array<{ id: string; name: string }> {
      return [
        { id: UNSELECTED_BASE_UNIT_VALUE, name: '未選択' },
        ...Object.values(UNIT_DEFINITIONS)
          .filter((definition) => definition.code === definition.baseUnit)
          .map((definition) => ({ id: definition.baseUnit, name: definition.label }))
      ];
    }
  }
});
</script>

<template>
  <ContentArea v-if="snapshotLoading" layout="center">
    <LoadingSpinner message="リストを読み込み中..." />
  </ContentArea>
  <ContentArea v-else>
    <div class="text-center mb-6">
      <div class="text-5xl mb-3 flex justify-center"><IconTools /></div>
      <h2 class="text-2xl font-bold text-charcoal-800">アイテムを編集</h2>
    </div>

    <div class="mb-8 rounded-xl border border-wood-200 bg-wood-50 p-4">
      <div class="grid gap-5">
        <div>
          <div class="mb-2">
            <label for="itemName" class="text-sm font-medium text-charcoal-700">
              アイテム名 <span class="text-ember-600">*</span>
            </label>
          </div>
          <TextInput
            input-id="itemName"
            placeholder="例：マシュマロ"
            :model-value="itemName"
            @update:model-value="onItemNameInput"
          />
        </div>

        <div>
          <label for="itemCategory" class="mb-2 block text-sm font-medium text-charcoal-700">
            カテゴリ
            <span v-if="hasSelectedBaseUnit && usesGeneratedCategory" class="text-xs font-normal text-charcoal-500">
              （自動設定）
            </span>
          </label>
          <template v-if="hasSelectedBaseUnit && usesGeneratedCategory">
            <p class="rounded-lg border border-wood-200 bg-white px-3 py-2 text-sm font-semibold text-charcoal-700">
              {{ currentCategoryLabel }}
            </p>
          </template>
          <DropDown
            v-else
            select-id="itemCategory"
            select-name="itemCategory"
            :model-value="selectedCategory"
            :option-items="categoryOptions"
            @update:model-value="setSelectedCategory"
          />
        </div>

        <div>
          <label for="quantifiedQuantity" class="mb-2 block text-sm font-medium text-charcoal-700"> 数量(単位) </label>
          <div class="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-2">
            <TextInput
              input-id="quantifiedQuantity"
              placeholder="例：1000"
              :model-value="quantifiedQuantity"
              @update:model-value="quantifiedQuantity = $event"
            />
            <DropDown
              select-id="quantifiedBaseUnit"
              select-name="quantifiedBaseUnit"
              :model-value="quantifiedBaseUnit"
              :option-items="baseUnitOptions"
              width="fixed"
              :show-arrow="true"
              @update:model-value="setQuantifiedBaseUnit"
            />
          </div>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="mb-4 p-3 bg-ember-100 border border-ember-300 text-ember-700 rounded-lg text-sm">
      {{ errorMessage }}
    </div>

    <div class="mb-8">
      <label for="assignedMember" class="flex items-center gap-1 text-sm font-medium text-charcoal-700 mb-2"
        ><IconUsers /> {{ memberLabel }}</label
      >
      <div class="grid grid-cols-2 gap-2 rounded-lg border border-wood-200 bg-wood-50 px-3 py-3">
        <label v-if="!isCompleted" class="min-w-0 flex items-center gap-2 text-sm text-charcoal-700">
          <input
            type="radio"
            name="assignedMember"
            value=""
            v-model="selectedMemberId"
            class="h-4 w-4 accent-wood-500"
          />
          <span class="min-w-0 truncate">未指定</span>
        </label>
        <label
          v-for="member in members"
          :key="member.id"
          class="min-w-0 flex items-center gap-2 text-sm text-charcoal-700"
        >
          <input
            type="radio"
            name="assignedMember"
            :value="member.id"
            v-model="selectedMemberId"
            class="h-4 w-4 accent-wood-500"
          />
          <span class="min-w-0 truncate">{{ member.displayName }}</span>
        </label>
      </div>
    </div>

    <div class="flex flex-col gap-3">
      <MainButton @click="updateItem" :disabled="!hasRequiredInput || isLoading" variant="primary"> 更新 </MainButton>
      <MainButton @click="cancelUpdate" :disabled="isLoading" variant="secondary"> キャンセル </MainButton>
    </div>
  </ContentArea>
</template>
