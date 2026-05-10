import { describe, expect, it, vi } from 'vitest';
import ItemBox from './ItemBox.vue';

type ItemBoxOptions = {
  data: () => Record<string, unknown>;
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
};

function createVm(overrides: Record<string, unknown> = {}) {
  const component = ItemBox as unknown as ItemBoxOptions;
  const vm: Record<string, unknown> = {
    ...component.data(),
    $emit: vi.fn(),
    ...overrides
  };

  Object.entries(component.methods).forEach(([name, method]) => {
    vm[name] = method.bind(vm);
  });

  return vm;
}

function createPointerEvent(overrides: Partial<PointerEvent> = {}) {
  return {
    button: 0,
    isPrimary: true,
    ...overrides
  } as PointerEvent;
}

describe('ItemBox', () => {
  describe('削除ボタンの pointer 操作', () => {
    it('pointerup で delete を1回だけ emit し、直後の click は無視する', () => {
      const vm = createVm();
      const itemId = 'item-1';

      (vm.handleDeletePointerStart as (event: PointerEvent) => void)(createPointerEvent());
      (vm.handleDeletePointerEnd as (event: PointerEvent, itemId: string) => void)(createPointerEvent(), itemId);
      (vm.handleDeleteClick as (itemId: string) => void)(itemId);

      expect(vm.$emit).toHaveBeenCalledTimes(1);
      expect(vm.$emit).toHaveBeenCalledWith('delete', itemId);
    });

    it('非 primary button では delete を emit しない', () => {
      const vm = createVm();
      const itemId = 'item-1';
      const secondaryPointer = createPointerEvent({ button: 2 });

      (vm.handleDeletePointerStart as (event: PointerEvent) => void)(secondaryPointer);
      (vm.handleDeletePointerEnd as (event: PointerEvent, itemId: string) => void)(secondaryPointer, itemId);

      expect(vm.$emit).not.toHaveBeenCalled();
    });
  });
});
