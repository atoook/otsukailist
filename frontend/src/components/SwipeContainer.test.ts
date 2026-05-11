import { describe, expect, it, vi } from 'vitest';
import SwipeContainer from './SwipeContainer.vue';

type SwipeContainerOptions = {
  data: () => Record<string, unknown>;
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
};

function createVm(overrides: Record<string, unknown> = {}) {
  const component = SwipeContainer as unknown as SwipeContainerOptions;
  const vm: Record<string, unknown> = {
    threshold: 60,
    maxSwipe: 100,
    ...component.data(),
    $emit: vi.fn(),
    ...overrides
  };

  Object.entries(component.methods).forEach(([name, method]) => {
    vm[name] = method.bind(vm);
  });

  return vm;
}

describe('SwipeContainer', () => {
  it('閉じている状態では右スワイプで offset を増やさない', () => {
    const vm = createVm();

    (vm.startDrag as (clientX: number, clientY: number) => void)(100, 0);
    (vm.updateDrag as (clientX: number) => void)(140);

    expect(vm.swipeOffset).toBe(0);
  });

  it('開いている状態では右スワイプで offset を戻せる', () => {
    const vm = createVm({ swipeOffset: -100 });

    (vm.startDrag as (clientX: number, clientY: number) => void)(100, 0);
    (vm.updateDrag as (clientX: number) => void)(140);

    expect(vm.swipeOffset).toBe(-60);
  });

  it('開いている状態から右へ戻しきると閉じる', () => {
    const vm = createVm({ swipeOffset: -100 });

    (vm.startDrag as (clientX: number, clientY: number) => void)(100, 0);
    (vm.updateDrag as (clientX: number) => void)(240);
    (vm.endDrag as () => void)();

    expect(vm.swipeOffset).toBe(0);
  });
});
