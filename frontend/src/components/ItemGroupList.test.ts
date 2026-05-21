import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { Item } from '@/types/item';

vi.mock('@/api/item', () => ({
  deleteItem: vi.fn(),
  markItemCompleted: vi.fn(),
  markItemIncomplete: vi.fn(),
  updateItem: vi.fn()
}));

import { markItemCompleted, markItemIncomplete, updateItem } from '@/api/item';
import ItemGroupList from './ItemGroupList.vue';

type ItemGroupListOptions = {
  data: () => Record<string, unknown>;
  methods: Record<string, (this: Record<string, unknown>, ...args: unknown[]) => unknown>;
};

function createItem(overrides: Partial<Item> = {}): Item {
  return {
    id: 'item-1',
    name: '牛乳',
    itemType: 'plain',
    category: null,
    preparationType: null,
    quantified: null,
    completed: false,
    assignedMemberId: null,
    completedByMemberId: null,
    completedAt: null,
    createdAt: '2024-01-01T00:00:00Z',
    updatedAt: '2024-01-01T00:00:00Z',
    ...overrides
  };
}

function createVm(overrides: Record<string, unknown> = {}) {
  const component = ItemGroupList as unknown as ItemGroupListOptions;
  const upsertItem = vi.fn();
  const mutationRun = vi.fn(async (fn: () => Promise<unknown>) => {
    await fn();
    return {
      applied: true,
      data: createItem({ completed: true, completedByMemberId: 'member-1' }),
      revision: 2
    };
  });
  const vm: Record<string, unknown> = {
    ...component.data(),
    selectedMemberId: 'member-1',
    listStore: {
      listId: 'list-1',
      items: [],
      memberMap: new Map(),
      upsertItem
    },
    mutationRun,
    ...overrides
  };

  Object.entries(component.methods).forEach(([name, method]) => {
    vm[name] = method.bind(vm);
  });

  return vm;
}

describe('ItemGroupList', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal('alert', vi.fn());
    vi.mocked(markItemCompleted).mockResolvedValue({
      revision: 2,
      data: createItem({ completed: true, completedByMemberId: 'member-1' })
    });
    vi.mocked(markItemIncomplete).mockResolvedValue({
      revision: 2,
      data: createItem({ completed: false })
    });
  });

  it('未完了アイテムのチェックはmarkItemCompletedを呼ぶ', async () => {
    const item = createItem({ completed: false });
    const vm = createVm();

    await (vm.toggleItem as (item: Item) => Promise<void>)(item);

    expect(markItemCompleted).toHaveBeenCalledWith('list-1', 'item-1', {
      completedByMemberId: 'member-1'
    });
    expect(markItemIncomplete).not.toHaveBeenCalled();
    expect(updateItem).not.toHaveBeenCalled();
  });

  it('完了済みアイテムのチェック解除はmarkItemIncompleteを呼ぶ', async () => {
    const item = createItem({
      completed: true,
      completedByMemberId: 'member-2',
      completedAt: '2024-01-01T00:10:00Z'
    });
    const vm = createVm({
      mutationRun: vi.fn(async (fn: () => Promise<unknown>) => {
        await fn();
        return { applied: true, data: createItem({ completed: false }), revision: 3 };
      })
    });

    await (vm.toggleItem as (item: Item) => Promise<void>)(item);

    expect(markItemIncomplete).toHaveBeenCalledWith('list-1', 'item-1');
    expect(markItemCompleted).not.toHaveBeenCalled();
    expect(updateItem).not.toHaveBeenCalled();
  });

  it('既に他の更新が反映済みだった場合はno-opとして通知する', async () => {
    const item = createItem({ completed: false });
    const vm = createVm({
      mutationRun: vi.fn(async (fn: () => Promise<unknown>) => {
        await fn();
        return {
          applied: true,
          changed: false,
          data: createItem({ completed: true, completedByMemberId: 'member-2' }),
          revision: 3
        };
      })
    });

    await (vm.toggleItem as (item: Item) => Promise<void>)(item);

    expect(markItemCompleted).toHaveBeenCalledWith('list-1', 'item-1', {
      completedByMemberId: 'member-1'
    });
    expect(alert).toHaveBeenCalledWith(
      'この操作では変更されませんでした。既に完了済みだったため、最新の完了者を表示しました。'
    );
  });
});
