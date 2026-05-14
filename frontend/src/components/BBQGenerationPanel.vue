<script lang="ts">
import { defineComponent, type PropType } from 'vue';
import CheckBox from './CheckBox.vue';
import IconChevronDown from './icons/IconChevronDown.vue';
import InlineSpinner from './InlineSpinner.vue';
import MainButton from './MainButton.vue';
import {
  createDefaultBBQGenerationConfig,
  generateBBQItems,
  type BBQGenerationConfig,
  type BBQGenerationLevel,
  type BBQGenerationBalance,
  type BBQStapleLevel,
  type BBQAlcoholLevel,
  type BBQSeafoodLevel,
  type GeneratedItemCandidate
} from '@/lib/generation/bbqGenerator';
import { saveListGenerationConfig } from '@/api/listGenerationConfig';
import { syncGeneratedItems } from '@/api/item';
import { useListStore } from '@/stores/list';
import { useMutation } from '@/composables/useMutation';
import { getErrorMessage } from '@/lib/http';
import { BBQ_GENERATOR_KEYS } from '@/lib/listGenerationConstants';
import { formatQuantity } from '@/lib/quantityDisplay';
import type { Item } from '@/types/api';

const LEVEL_OPTIONS: Array<{ value: BBQGenerationLevel; label: string }> = [
  { value: 'small', label: '少なめ' },
  { value: 'normal', label: '普通' },
  { value: 'large', label: '多め' }
];

const BALANCE_OPTIONS: Array<{ value: BBQGenerationBalance; label: string }> = [
  { value: 'vegetables', label: '野菜多め' },
  { value: 'balanced', label: 'バランス' },
  { value: 'meat', label: '肉中心' }
];

const STAPLE_OPTIONS: Array<{ value: BBQStapleLevel; label: string }> = [
  { value: 'none', label: 'なし' },
  { value: 'light', label: '軽め' },
  { value: 'solid', label: 'しっかり' }
];

const ALCOHOL_OPTIONS: Array<{ value: BBQAlcoholLevel; label: string }> = [
  { value: 'none', label: 'なし' },
  { value: 'included', label: 'あり' }
];

const SEAFOOD_OPTIONS: Array<{ value: BBQSeafoodLevel; label: string }> = [
  { value: 'none', label: 'なし' },
  { value: 'included', label: 'あり' }
];

const ADJUSTMENT_TARGETS: Array<{ key: keyof BBQGenerationConfig['adjustments']; label: string }> = [
  { key: 'meat', label: '肉' },
  { key: 'seafood', label: '海鮮' },
  { key: 'vegetables', label: '野菜' },
  { key: 'drinks', label: '飲み物' },
  { key: 'staple', label: '主食' },
  { key: 'overall', label: '全体' }
];

type GenerationPreviewRow = {
  key: string;
  name: string;
  status: 'new' | 'update' | 'delete' | 'unchanged' | 'locked' | 'completed';
  beforeQuantity: string | null;
  afterQuantity: string | null;
  beforeRawQuantity: number | null;
  afterRawQuantity: number | null;
};

type GeneratedItemWithKey = Item & {
  quantified: NonNullable<Item['quantified']> & { generatorKey: string };
};

function isGeneratedItemWithKey(item: Item): item is GeneratedItemWithKey {
  return (
    item.itemType === 'quantified' &&
    item.quantified != null &&
    item.quantified.origin === 'generated' &&
    item.quantified.generatorKey != null
  );
}

function cloneBBQGenerationConfig(config: BBQGenerationConfig): BBQGenerationConfig {
  const defaults = createDefaultBBQGenerationConfig();
  const rawBalance = (config.answers as { balance?: string }).balance;
  const balance = rawBalance === 'seafood' ? defaults.answers.balance : config.answers.balance;
  return {
    version: 1,
    answers: {
      ...defaults.answers,
      ...config.answers,
      balance,
      alcoholLevel: config.answers.alcoholLevel,
      seafoodLevel: config.answers.seafoodLevel
    },
    adjustments: { ...defaults.adjustments, ...config.adjustments }
  };
}

export default defineComponent({
  name: 'BBQGenerationPanel',
  components: { CheckBox, IconChevronDown, InlineSpinner, MainButton },
  props: {
    initialConfig: {
      type: Object as PropType<BBQGenerationConfig | null>,
      default: null
    },
    hasExistingConfig: {
      type: Boolean,
      default: false
    },
    showHeader: {
      type: Boolean,
      default: true
    }
  },
  emits: ['completed', 'cancel', 'error'],
  setup() {
    const listStore = useListStore();
    const { run, loading } = useMutation();
    return { listStore, mutationRun: run, mutationLoading: loading };
  },
  data(): {
    config: BBQGenerationConfig;
    showAdjustmentPanel: boolean;
    showOnlyChangedRows: boolean;
  } {
    return {
      config: cloneBBQGenerationConfig(this.initialConfig ?? createDefaultBBQGenerationConfig()),
      showAdjustmentPanel: false,
      showOnlyChangedRows: true
    };
  },
  computed: {
    levelOptions(): typeof LEVEL_OPTIONS {
      return LEVEL_OPTIONS;
    },
    balanceOptions(): typeof BALANCE_OPTIONS {
      return BALANCE_OPTIONS;
    },
    stapleOptions(): typeof STAPLE_OPTIONS {
      return STAPLE_OPTIONS;
    },
    alcoholOptions(): typeof ALCOHOL_OPTIONS {
      return ALCOHOL_OPTIONS;
    },
    seafoodOptions(): typeof SEAFOOD_OPTIONS {
      return SEAFOOD_OPTIONS;
    },
    adjustmentTargets(): typeof ADJUSTMENT_TARGETS {
      return ADJUSTMENT_TARGETS;
    },
    templateGeneratorKeys(): string[] {
      return BBQ_GENERATOR_KEYS;
    },
    candidates(): GeneratedItemCandidate[] {
      return generateBBQItems({
        answers: this.config.answers,
        adjustments: this.config.adjustments
      });
    },
    existingGeneratedItemsByGeneratorKey(): Map<string, GeneratedItemWithKey> {
      return new Map(
        this.listStore.items.filter(isGeneratedItemWithKey).map((item) => [item.quantified.generatorKey, item])
      );
    },
    applicableCandidates(): GeneratedItemCandidate[] {
      return this.candidates.filter((candidate) => {
        const existingItem = this.existingGeneratedItemsByGeneratorKey.get(candidate.generatorKey);
        return !existingItem?.completed && existingItem?.quantified?.regenerationPolicy !== 'locked';
      });
    },
    previewRows(): GenerationPreviewRow[] {
      if (!this.hasExistingConfig) {
        return this.candidates.map((candidate) => ({
          key: candidate.generatorKey,
          name: candidate.name,
          status: 'new',
          beforeQuantity: null,
          afterQuantity: this.displayQuantity(candidate),
          beforeRawQuantity: null,
          afterRawQuantity: candidate.quantity
        }));
      }

      const candidateKeys = new Set(this.candidates.map((candidate) => candidate.generatorKey));
      const candidateRows = this.candidates.map((candidate): GenerationPreviewRow => {
        const existingItem = this.existingGeneratedItemsByGeneratorKey.get(candidate.generatorKey);
        if (!existingItem?.quantified) {
          return {
            key: candidate.generatorKey,
            name: candidate.name,
            status: 'new',
            beforeQuantity: null,
            afterQuantity: this.displayQuantity(candidate),
            beforeRawQuantity: null,
            afterRawQuantity: candidate.quantity
          };
        }

        if (existingItem.completed || existingItem.quantified.regenerationPolicy === 'locked') {
          return {
            key: candidate.generatorKey,
            name: existingItem.name,
            status: existingItem.completed ? 'completed' : 'locked',
            beforeQuantity: this.displayExistingQuantity(existingItem),
            afterQuantity: null,
            beforeRawQuantity: existingItem.quantified.quantity,
            afterRawQuantity: null
          };
        }

        const beforeQuantity = this.displayExistingQuantity(existingItem);
        const afterQuantity = this.displayQuantity(candidate);
        return {
          key: candidate.generatorKey,
          name: existingItem.name,
          status: beforeQuantity === afterQuantity ? 'unchanged' : 'update',
          beforeQuantity,
          afterQuantity,
          beforeRawQuantity: existingItem.quantified.quantity,
          afterRawQuantity: candidate.quantity
        };
      });

      const templateGeneratorKeys = new Set(this.templateGeneratorKeys);
      const staleGeneratedRows = Array.from(this.existingGeneratedItemsByGeneratorKey.entries())
        .filter((entry): entry is [string, GeneratedItemWithKey] => {
          const [generatorKey] = entry;
          return !candidateKeys.has(generatorKey) && templateGeneratorKeys.has(generatorKey);
        })
        .map(([generatorKey, item]): GenerationPreviewRow => ({
          key: generatorKey,
          name: item.name,
          status: item.completed ? 'completed' : item.quantified.regenerationPolicy === 'locked' ? 'locked' : 'delete',
          beforeQuantity: this.displayExistingQuantity(item),
          afterQuantity: null,
          beforeRawQuantity: item.quantified.quantity,
          afterRawQuantity: null
        }));

      return [...candidateRows, ...staleGeneratedRows];
    },
    visiblePreviewRows(): GenerationPreviewRow[] {
      if (!this.hasExistingConfig || !this.showOnlyChangedRows) {
        return this.previewRows;
      }
      return this.previewRows.filter((row) => row.status === 'update' || row.status === 'new' || row.status === 'delete');
    },
    canApply(): boolean {
      return (
        this.config.answers.adultCount + this.config.answers.childCount > 0 &&
        (this.candidates.length > 0 || this.previewRows.some((row) => row.status === 'delete')) &&
        !this.mutationLoading
      );
    },
    primaryButtonLabel(): string {
      return this.hasExistingConfig ? '再生成する' : 'リストに追加';
    },
    previewTitle(): string {
      return this.hasExistingConfig ? '変更されるアイテム' : '追加されるアイテム';
    },
    emptyPreviewMessage(): string {
      if (this.hasExistingConfig && this.showOnlyChangedRows && this.previewRows.length > 0) {
        return '変更があるアイテムはありません';
      }
      return '人数を入力すると候補が表示されます';
    },
    panelClass(): string {
      return this.showHeader ? 'mb-6 rounded-lg border border-wood-300 bg-wood-50 p-4 shadow-sm' : 'mb-6';
    }
  },
  watch: {
    initialConfig: {
      handler(value: BBQGenerationConfig | null) {
        this.config = cloneBBQGenerationConfig(value ?? createDefaultBBQGenerationConfig());
      },
      deep: true
    }
  },
  methods: {
    setLevel(target: 'appetite' | 'drinkLevel' | 'alcoholAmount', value: BBQGenerationLevel): void {
      this.config.answers[target] = value;
    },
    setBalance(value: BBQGenerationBalance): void {
      this.config.answers.balance = value;
    },
    setStapleLevel(value: BBQStapleLevel): void {
      this.config.answers.stapleLevel = value;
    },
    setAlcoholLevel(value: BBQAlcoholLevel): void {
      this.config.answers.alcoholLevel = value;
    },
    setSeafoodLevel(value: BBQSeafoodLevel): void {
      this.config.answers.seafoodLevel = value;
    },
    setAdjustment(target: keyof BBQGenerationConfig['adjustments'], value: BBQGenerationLevel): void {
      this.config.adjustments[target] = value;
    },
    toggleAdjustmentPanel(): void {
      this.showAdjustmentPanel = !this.showAdjustmentPanel;
    },
    toggleShowOnlyChangedRows(): void {
      this.showOnlyChangedRows = !this.showOnlyChangedRows;
    },
    displayQuantity(item: GeneratedItemCandidate): string {
      return formatQuantity(item);
    },
    displayExistingQuantity(item: GeneratedItemWithKey): string {
      return formatQuantity({
        quantity: item.quantified.quantity,
        baseUnit: item.quantified.baseUnit,
        category: item.category
      });
    },
    previewStatusLabel(status: GenerationPreviewRow['status']): string {
      if (status === 'new') {
        return '追加';
      }
      if (status === 'update') {
        return '変更';
      }
      if (status === 'delete') {
        return '削除';
      }
      if (status === 'locked') {
        return '変更対象外';
      }
      if (status === 'completed') {
        return '完了済';
      }
      return '変更なし';
    },
    previewStatusClass(status: GenerationPreviewRow['status']): string {
      if (status === 'new') {
        return 'bg-amber-100 text-amber-800';
      }
      if (status === 'update') {
        return 'bg-wood-100 text-charcoal-800';
      }
      if (status === 'delete') {
        return 'border border-red-200 bg-white text-red-700';
      }
      if (status === 'locked') {
        return 'bg-charcoal-100 text-charcoal-600';
      }
      if (status === 'completed') {
        return 'bg-charcoal-100 text-charcoal-600';
      }
      return 'bg-transparent text-charcoal-500';
    },
    quantityBadgeClass(row: GenerationPreviewRow, position: 'before' | 'after'): string[] {
      const baseClass = ['rounded px-2 py-0.5'];
      if (row.status === 'delete' && position === 'before') {
        return [...baseClass, 'bg-red-50 text-red-700'];
      }
      if (row.status !== 'update' || position !== 'after') {
        return [...baseClass, position === 'after' ? 'bg-wood-100' : 'bg-charcoal-50'];
      }
      if (row.beforeRawQuantity != null && row.afterRawQuantity != null && row.afterRawQuantity > row.beforeRawQuantity) {
        return [...baseClass, 'bg-amber-100 text-amber-800'];
      }
      if (row.beforeRawQuantity != null && row.afterRawQuantity != null && row.afterRawQuantity < row.beforeRawQuantity) {
        return [...baseClass, 'bg-charcoal-100 text-charcoal-700'];
      }
      return [...baseClass, 'bg-wood-100'];
    },
    optionButtonClass(isActive: boolean, size: 'default' | 'compact' = 'default'): string[] {
      const sizeClass = size === 'compact' ? 'px-2 py-2 text-sm' : 'px-3 py-2.5 text-sm';
      return [
        'rounded-full border font-medium transition-[background-color,border-color,color,box-shadow]',
        'focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300',
        sizeClass,
        isActive
          ? 'border-wood-500 bg-wood-100 text-charcoal-800 shadow-sm'
          : 'border-wood-200 bg-white text-charcoal-600 hover:border-wood-300 hover:bg-wood-50'
      ];
    },
    async applyTemplate(): Promise<void> {
      const listId = this.listStore.listId;
      if (!listId) {
        this.$emit('error', 'リストが初期化されていません。');
        return;
      }
      if (!this.canApply) {
        return;
      }

      try {
        const normalizedConfig = cloneBBQGenerationConfig(this.config);
        await this.mutationRun(() => saveListGenerationConfig(listId, 'bbq', normalizedConfig));
        const result = await this.mutationRun(() =>
          syncGeneratedItems(listId, {
            generatorKeysInScope: this.templateGeneratorKeys,
            items: this.applicableCandidates.map((candidate) => ({
              name: candidate.name,
              itemType: 'quantified' as const,
              category: candidate.category,
              quantified: {
                quantity: candidate.quantity,
                baseUnit: candidate.baseUnit,
                origin: 'generated' as const,
                regenerationPolicy: 'auto' as const,
                generatorKey: candidate.generatorKey
              }
            }))
          })
        );
        if (result.applied) {
          for (const itemId of result.data.deletedItemIds) {
            this.listStore.removeItem(itemId);
          }
          for (const item of result.data.items) {
            this.listStore.upsertItem(item);
          }
        }
        this.$emit('completed', normalizedConfig);
      } catch (err: unknown) {
        console.error('Failed to apply list generation template', err);
        this.$emit('error', getErrorMessage(err) ?? 'テンプレートからの生成に失敗しました。');
      }
    }
  }
});
</script>

<template>
  <section :class="panelClass">
    <div v-if="showHeader" class="mb-4 flex items-start justify-between gap-3">
      <div>
        <h3 class="text-lg font-bold text-charcoal-800">BBQ テンプレート</h3>
        <p class="mt-1 text-sm text-charcoal-600">人数と好みに合わせて買い物候補を作ります</p>
      </div>
      <button
        type="button"
        class="shrink-0 rounded px-2 py-1 text-sm text-charcoal-500 hover:bg-wood-100 hover:text-charcoal-700"
        :disabled="mutationLoading"
        @click="$emit('cancel')"
      >
        閉じる
      </button>
    </div>

    <div class="space-y-4">
      <div class="rounded-lg border border-wood-100 bg-white p-4">
        <p class="mb-3 text-sm font-semibold text-charcoal-800">何人で食べますか？</p>
        <div class="grid grid-cols-2 gap-3">
          <label class="block">
            <span class="mb-1 block text-xs font-medium text-charcoal-500">大人</span>
            <input
              v-model.number="config.answers.adultCount"
              type="number"
              min="0"
              inputmode="numeric"
              class="w-full rounded-lg border border-wood-200 bg-wood-50 px-3 py-2 text-base text-charcoal-800 focus:border-wood-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-wood-100"
            />
          </label>
          <label class="block">
            <span class="mb-1 block text-xs font-medium text-charcoal-500">子供</span>
            <input
              v-model.number="config.answers.childCount"
              type="number"
              min="0"
              inputmode="numeric"
              class="w-full rounded-lg border border-wood-200 bg-wood-50 px-3 py-2 text-base text-charcoal-800 focus:border-wood-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-wood-100"
            />
          </label>
        </div>
      </div>

      <div class="rounded-lg border border-wood-100 bg-white p-4">
        <p class="mb-3 text-sm font-semibold text-charcoal-800">食べる量はどれくらいですか？</p>
        <div class="grid grid-cols-3 gap-2">
          <button
            v-for="option in levelOptions"
            :key="option.value"
            type="button"
            :class="optionButtonClass(config.answers.appetite === option.value)"
            @click="setLevel('appetite', option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="rounded-lg border border-wood-100 bg-white p-4">
        <p class="mb-3 text-sm font-semibold text-charcoal-800">肉と野菜のバランスは？</p>
        <div class="grid grid-cols-3 gap-2">
          <button
            v-for="option in balanceOptions"
            :key="option.value"
            type="button"
            :class="optionButtonClass(config.answers.balance === option.value)"
            @click="setBalance(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="rounded-lg border border-wood-100 bg-white p-4">
        <p class="mb-3 text-sm font-semibold text-charcoal-800">海鮮も用意しますか？</p>
        <div class="grid grid-cols-2 gap-2">
          <button
            v-for="option in seafoodOptions"
            :key="option.value"
            type="button"
            :class="optionButtonClass(config.answers.seafoodLevel === option.value)"
            @click="setSeafoodLevel(option.value)"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="space-y-4">
        <div class="rounded-lg border border-wood-100 bg-white p-4">
          <p class="mb-3 text-sm font-semibold text-charcoal-800">飲み物はどれくらいですか？</p>
          <div class="grid grid-cols-3 gap-2">
            <button
              v-for="option in levelOptions"
              :key="option.value"
              type="button"
              :class="optionButtonClass(config.answers.drinkLevel === option.value, 'compact')"
              @click="setLevel('drinkLevel', option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>

        <div class="rounded-lg border border-wood-100 bg-white p-4">
          <p class="mb-3 text-sm font-semibold text-charcoal-800">アルコールは用意しますか？</p>
          <div class="grid grid-cols-2 gap-2">
            <button
              v-for="option in alcoholOptions"
              :key="option.value"
              type="button"
              :class="optionButtonClass(config.answers.alcoholLevel === option.value, 'compact')"
              @click="setAlcoholLevel(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
          <div v-if="config.answers.alcoholLevel === 'included'" class="mt-3">
            <p class="mb-2 text-xs font-medium text-charcoal-500">アルコールの量</p>
            <div class="grid grid-cols-3 gap-2">
              <button
                v-for="option in levelOptions"
                :key="option.value"
                type="button"
                :class="optionButtonClass(config.answers.alcoholAmount === option.value, 'compact')"
                @click="setLevel('alcoholAmount', option.value)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-wood-100 bg-white p-4">
          <p class="mb-3 text-sm font-semibold text-charcoal-800">焼きそばなどの主食は？</p>
          <div class="grid grid-cols-3 gap-2">
            <button
              v-for="option in stapleOptions"
              :key="option.value"
              type="button"
              :class="optionButtonClass(config.answers.stapleLevel === option.value, 'compact')"
              @click="setStapleLevel(option.value)"
            >
              {{ option.label }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="hasExistingConfig" class="rounded-lg border border-wood-100 bg-white">
        <button
          type="button"
          class="flex w-full items-center justify-between gap-3 px-4 py-3 text-left hover:bg-wood-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-wood-300"
          :aria-expanded="showAdjustmentPanel"
          aria-controls="bbq-adjustment-panel"
          @click="toggleAdjustmentPanel"
        >
          <span>
            <span class="block text-sm font-medium text-charcoal-700">量の微調整</span>
            <span class="mt-0.5 block text-xs text-charcoal-500">必要に応じてカテゴリ別に増減できます</span>
          </span>
          <span
            class="flex shrink-0 items-center text-charcoal-500 transition-transform duration-200"
            :class="{ '-rotate-90': !showAdjustmentPanel }"
            aria-hidden="true"
          >
            <IconChevronDown />
          </span>
        </button>
        <div
          v-if="showAdjustmentPanel"
          id="bbq-adjustment-panel"
          class="grid gap-3 border-t border-wood-100 p-4"
        >
          <div v-for="target in adjustmentTargets" :key="target.key">
            <p class="mb-1 text-xs font-medium text-charcoal-600">
              {{ target.label }}
            </p>
            <div class="grid grid-cols-3 gap-1">
              <button
                v-for="option in levelOptions"
                :key="option.value"
                type="button"
                :class="optionButtonClass(config.adjustments[target.key] === option.value, 'compact')"
                @click="setAdjustment(target.key, option.value)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-wood-100 bg-white p-4">
        <div class="mb-2 flex flex-wrap items-center justify-between gap-2">
          <p class="text-sm font-semibold text-charcoal-800">{{ previewTitle }}</p>
          <div v-if="hasExistingConfig" class="flex items-center gap-2 text-xs font-medium text-charcoal-600">
            <CheckBox
              :checked="showOnlyChangedRows"
              aria-label="変更ありだけ表示"
              @toggle="toggleShowOnlyChangedRows"
            />
            <button type="button" class="text-left hover:text-charcoal-800" @click="toggleShowOnlyChangedRows">
              変更ありだけ表示
            </button>
          </div>
        </div>
        <div class="divide-y divide-wood-100 rounded-lg border border-wood-100 bg-white">
          <div
            v-for="row in visiblePreviewRows"
            :key="row.key"
            class="grid gap-2 px-3 py-2 sm:grid-cols-[minmax(0,1fr)_auto]"
            :class="{ 'bg-charcoal-50/50': row.status === 'unchanged' }"
          >
            <div class="flex min-w-0 items-center gap-2">
              <span
                class="min-w-0 truncate text-sm"
                :class="row.status === 'unchanged' ? 'text-charcoal-500' : 'text-charcoal-800'"
              >
                {{ row.name }}
              </span>
              <span
                class="shrink-0 rounded px-2 py-0.5 text-[11px] font-medium"
                :class="previewStatusClass(row.status)"
              >
                {{ previewStatusLabel(row.status) }}
              </span>
            </div>
            <div class="flex flex-wrap items-center gap-1 text-xs font-medium text-charcoal-700 sm:justify-end">
              <span v-if="row.status === 'unchanged'" class="text-charcoal-500">
                {{ row.beforeQuantity }}
              </span>
              <template v-else-if="row.beforeQuantity && row.afterQuantity">
                <span :class="quantityBadgeClass(row, 'before')">{{ row.beforeQuantity }}</span>
                <span class="text-charcoal-400">→</span>
                <span :class="quantityBadgeClass(row, 'after')">{{ row.afterQuantity }}</span>
              </template>
              <span v-else-if="row.afterQuantity" :class="quantityBadgeClass(row, 'after')">
                {{ row.afterQuantity }}
              </span>
              <span v-else-if="row.beforeQuantity" :class="quantityBadgeClass(row, 'before')">
                {{ row.beforeQuantity }}
              </span>
            </div>
          </div>
          <p v-if="visiblePreviewRows.length === 0" class="px-3 py-3 text-sm text-charcoal-600">
            {{ emptyPreviewMessage }}
          </p>
        </div>
      </div>
    </div>

    <div class="mt-5 flex flex-col-reverse gap-2">
      <MainButton variant="cancel" :disabled="mutationLoading" @click="$emit('cancel')">キャンセル</MainButton>
      <MainButton :disabled="!canApply" :aria-busy="mutationLoading" @click="applyTemplate">
        <span class="relative inline-grid min-w-[6em] place-items-center">
          <span :class="{ 'opacity-0': mutationLoading }">{{ primaryButtonLabel }}</span>
          <InlineSpinner v-if="mutationLoading" class="absolute inset-0 m-auto" size="md" tone="primary" />
        </span>
      </MainButton>
    </div>
  </section>
</template>
