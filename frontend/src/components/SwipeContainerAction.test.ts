import { describe, expect, it, vi } from 'vitest';
import SwipeContainerAction from './SwipeContainerAction.vue';

type SwipeContainerActionOptions = {
  data: () => Record<string, unknown>;
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
};

function createVm(overrides: Record<string, unknown> = {}) {
  const component = SwipeContainerAction as unknown as SwipeContainerActionOptions;
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

function createMouseEvent(overrides: Partial<MouseEvent> = {}) {
  return {
    button: 0,
    ...overrides
  } as MouseEvent;
}

describe('SwipeContainerAction', () => {
  it('pointerup で activate を1回だけ emit し、直後の click は無視する', () => {
    const vm = createVm();

    (vm.handlePointerStart as (event: PointerEvent) => void)(createPointerEvent());
    (vm.handlePointerEnd as (event: PointerEvent) => void)(createPointerEvent());
    (vm.handleClick as (event: MouseEvent) => void)(createMouseEvent());

    expect(vm.$emit).toHaveBeenCalledTimes(1);
    expect(vm.$emit).toHaveBeenCalledWith('activate');
  });

  it('非 primary button では activate を emit しない', () => {
    const vm = createVm();
    const secondaryPointer = createPointerEvent({ button: 2 });

    (vm.handlePointerStart as (event: PointerEvent) => void)(secondaryPointer);
    (vm.handlePointerEnd as (event: PointerEvent) => void)(secondaryPointer);

    expect(vm.$emit).not.toHaveBeenCalled();
  });

  it('非 primary click では activate を emit しない', () => {
    const vm = createVm();

    (vm.handleClick as (event: MouseEvent) => void)(createMouseEvent({ button: 1 }));

    expect(vm.$emit).not.toHaveBeenCalled();
  });

  it('pointer capture を失った場合は次の pointerup で activate しない', () => {
    const vm = createVm();

    (vm.handlePointerStart as (event: PointerEvent) => void)(createPointerEvent());
    (vm.handlePointerCancel as () => void)();
    (vm.handlePointerEnd as (event: PointerEvent) => void)(createPointerEvent());

    expect(vm.$emit).not.toHaveBeenCalled();
  });
});
