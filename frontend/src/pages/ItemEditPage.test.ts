import { describe, expect, it } from 'vitest';
import ItemEditPage from './ItemEditPage.vue';

type ItemEditPageOptions = {
  data: () => Record<string, unknown>;
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
  computed: Record<string, (this: Record<string, unknown>) => unknown>;
};

function createVm(overrides: Record<string, unknown> = {}) {
  const component = ItemEditPage as unknown as ItemEditPageOptions;
  const vm: Record<string, unknown> = {
    ...component.data(),
    ...overrides
  };

  Object.entries(component.methods).forEach(([name, method]) => {
    vm[name] = method.bind(vm);
  });
  Object.entries(component.computed).forEach(([name, getter]) => {
    Object.defineProperty(vm, name, {
      get: () => getter.call(vm)
    });
  });

  return vm;
}

describe('ItemEditPage', () => {
  describe('数量付き入力のバリデーション', () => {
    it('数量だけ入力されて単位が未選択の場合は更新不可にする', () => {
      const vm = createVm({
        itemName: '牛肉',
        quantifiedQuantity: '1000',
        quantifiedBaseUnit: ''
      });

      expect(vm.hasQuantifiedDraftInput).toBe(true);
      expect(vm.hasValidQuantifiedInput).toBe(false);
      expect(vm.hasRequiredInput).toBe(false);
    });

    it('単位だけ選択されて数量が空の場合は更新不可にする', () => {
      const vm = createVm({
        itemName: '牛肉',
        quantifiedQuantity: '',
        quantifiedBaseUnit: 'g'
      });

      expect(vm.hasQuantifiedDraftInput).toBe(true);
      expect(vm.hasValidQuantifiedInput).toBe(false);
      expect(vm.hasRequiredInput).toBe(false);
    });

    it('数量0と単位が入力されている場合は更新可能にする', () => {
      const vm = createVm({
        itemName: '牛肉',
        quantifiedQuantity: '0',
        quantifiedBaseUnit: 'g'
      });

      expect(vm.hasQuantifiedDraftInput).toBe(true);
      expect(vm.hasValidQuantifiedInput).toBe(true);
      expect(vm.hasRequiredInput).toBe(true);
    });
  });

  describe('数量付きアイテムの更新payload', () => {
    it('plain は通常アイテムとして送信する', () => {
      const vm = createVm({
        itemName: '牛肉',
        selectedCategory: 'meat',
        itemEditMode: 'plain',
        quantifiedQuantity: '',
        quantifiedBaseUnit: ''
      });

      const payload = (vm.buildUpdatePayload as (name: string) => Record<string, unknown>)('牛肉');

      expect(payload).toMatchObject({
        name: '牛肉',
        category: 'meat',
        itemType: 'plain'
      });
      expect(payload.quantified).toBeUndefined();
    });

    it('manual_none は手入力の数量付きアイテムとして送信する', () => {
      const vm = createVm({
        itemName: '牛肉',
        selectedCategory: 'meat',
        itemEditMode: 'manual_none',
        quantifiedQuantity: '1000',
        quantifiedBaseUnit: 'g',
        quantifiedGeneratorKey: ''
      });

      const payload = (vm.buildUpdatePayload as (name: string) => Record<string, unknown>)('牛肉');

      expect(payload.category).toBe('meat');
      expect(payload.itemType).toBe('quantified');
      expect(payload.quantified).toMatchObject({
        quantity: 1000,
        baseUnit: 'g',
        origin: 'manual',
        regenerationPolicy: 'none',
        generatorKey: null
      });
    });

    it('generated_auto は生成ルール由来カテゴリを送信する', () => {
      const vm = createVm({
        itemName: '牛肉',
        selectedCategory: 'sweets',
        itemEditMode: 'generated_auto',
        quantifiedQuantity: '1000',
        quantifiedBaseUnit: 'g',
        quantifiedGeneratorKey: 'beef'
      });

      const payload = (vm.buildUpdatePayload as (name: string) => Record<string, unknown>)('牛肉');

      expect(payload.category).toBe('meat');
      expect(payload.itemType).toBe('quantified');
      expect(payload.quantified).toMatchObject({
        quantity: 1000,
        baseUnit: 'g',
        origin: 'generated',
        regenerationPolicy: 'auto',
        generatorKey: 'beef'
      });
    });

    it('generated_locked は既存の選択カテゴリを保持して送信する', () => {
      const vm = createVm({
        itemName: '牛肉',
        selectedCategory: 'sweets',
        itemEditMode: 'generated_locked',
        quantifiedQuantity: '1000',
        quantifiedBaseUnit: 'g',
        quantifiedGeneratorKey: 'beef'
      });

      const payload = (vm.buildUpdatePayload as (name: string) => Record<string, unknown>)('牛肉');

      expect(payload.category).toBe('sweets');
      expect(payload.itemType).toBe('quantified');
      expect(payload.quantified).toMatchObject({
        quantity: 1000,
        baseUnit: 'g',
        origin: 'generated',
        regenerationPolicy: 'locked',
        generatorKey: 'beef'
      });
    });
  });
});
