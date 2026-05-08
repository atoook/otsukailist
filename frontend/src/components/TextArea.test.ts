import { describe, expect, it, vi } from 'vitest';
import TextArea from './TextArea.vue';

type TextAreaOptions = {
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
  watch: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
};

function createVm(overrides: Record<string, unknown> = {}) {
  const component = TextArea as unknown as TextAreaOptions;
  const vm: Record<string, unknown> = {
    autoResize: false,
    preventEnter: false,
    $emit: vi.fn(),
    $nextTick: (callback: () => void) => callback(),
    $refs: {},
    ...overrides
  };

  Object.entries(component.methods).forEach(([name, method]) => {
    vm[name] = method.bind(vm);
  });
  Object.entries(component.watch).forEach(([name, watcher]) => {
    vm[`watch:${name}`] = watcher.bind(vm);
  });

  return vm;
}

describe('TextArea', () => {
  it('input では update:modelValue だけを emit する', () => {
    const vm = createVm();
    const event = { target: { value: 'マシュマロ' } };

    (vm.handleInput as (event: unknown) => void)(event);

    expect(vm.$emit).toHaveBeenCalledWith('update:modelValue', 'マシュマロ');
  });

  it('preventEnter が有効な場合は Enter を抑止して enter を emit する', () => {
    const preventDefault = vi.fn();
    const vm = createVm({ preventEnter: true });
    const event = { key: 'Enter', isComposing: false, preventDefault };

    (vm.handleKeyDown as (event: unknown) => void)(event);

    expect(preventDefault).toHaveBeenCalledTimes(1);
    expect(vm.$emit).toHaveBeenCalledWith('enter');
  });

  it('preventEnter が無効な場合は Enter の通常動作を妨げない', () => {
    const preventDefault = vi.fn();
    const vm = createVm({ preventEnter: false });
    const event = { key: 'Enter', isComposing: false, preventDefault };

    (vm.handleKeyDown as (event: unknown) => void)(event);

    expect(preventDefault).not.toHaveBeenCalled();
    expect(vm.$emit).not.toHaveBeenCalled();
  });

  it('modelValue 変更時に autoResize の高さ調整を行う', () => {
    const textarea = { scrollHeight: 96, style: { height: '24px' } };
    const vm = createVm({
      autoResize: true,
      $refs: { textarea }
    });

    (vm['watch:modelValue'] as () => void)();

    expect(textarea.style.height).toBe('96px');
  });
});
