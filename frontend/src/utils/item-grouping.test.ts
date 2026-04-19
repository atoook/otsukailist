import { describe, it, expect } from 'vitest';
import { groupItems } from './item-grouping';
import type { GroupDefinition } from './item-grouping';

type TestItem = { id: number; value: string; completed: boolean };

const incomplete: TestItem = { id: 1, value: 'a', completed: false };
const complete1: TestItem = { id: 2, value: 'b', completed: true };
const complete2: TestItem = { id: 3, value: 'c', completed: true };

const baseDefinitions: GroupDefinition<TestItem>[] = [
  {
    key: 'incomplete',
    label: '未完了',
    predicate: (item) => !item.completed
  },
  {
    key: 'completed',
    label: '完了',
    predicate: (item) => item.completed
  }
];

describe('groupItems', () => {
  describe('基本のグルーピング', () => {
    it('predicate に従ってアイテムをグループに振り分ける', () => {
      const result = groupItems([incomplete, complete1], baseDefinitions);
      const [first, second] = result;
      expect(result).toHaveLength(2);
      expect(first?.key).toBe('incomplete');
      expect(first?.items).toEqual([incomplete]);
      expect(second?.key).toBe('completed');
      expect(second?.items).toEqual([complete1]);
    });

    it('空のグループは結果から除外される', () => {
      const result = groupItems([incomplete], baseDefinitions);
      const [first] = result;
      expect(result).toHaveLength(1);
      expect(first?.key).toBe('incomplete');
    });

    it('全アイテムが空の場合は空配列を返す', () => {
      const result = groupItems([], baseDefinitions);
      expect(result).toHaveLength(0);
    });

    it('定義順を保持する', () => {
      const result = groupItems([complete1, incomplete], baseDefinitions);
      const [first, second] = result;
      expect(first?.key).toBe('incomplete');
      expect(second?.key).toBe('completed');
    });
  });

  describe('comparator によるソート', () => {
    it('comparator が指定されている場合、グループ内を並び替える', () => {
      const defs: GroupDefinition<TestItem>[] = [
        {
          key: 'completed',
          label: '完了',
          predicate: (item) => item.completed,
          comparator: (a, b) => a.id - b.id
        }
      ];
      const result = groupItems([complete2, complete1], defs);
      const [first] = result;
      expect(first?.items).toEqual([complete1, complete2]);
    });

    it('comparator が指定されていない場合、元の順序を保持する', () => {
      const defs: GroupDefinition<TestItem>[] = [
        {
          key: 'completed',
          label: '完了',
          predicate: (item) => item.completed
        }
      ];
      const result = groupItems([complete2, complete1], defs);
      const [first] = result;
      expect(first?.items).toEqual([complete2, complete1]);
    });

    it('comparator はオリジナル配列を変更しない', () => {
      const items = [complete2, complete1];
      const defs: GroupDefinition<TestItem>[] = [
        {
          key: 'completed',
          label: '完了',
          predicate: (item) => item.completed,
          comparator: (a, b) => a.id - b.id
        }
      ];
      groupItems(items, defs);
      expect(items).toEqual([complete2, complete1]);
    });
  });

  describe('デフォルト値のフォールバック', () => {
    it('showHeader は未指定時 true になる', () => {
      const [first] = groupItems([incomplete], baseDefinitions);
      expect(first?.showHeader).toBe(true);
    });

    it('collapsible は未指定時 false になる', () => {
      const [first] = groupItems([incomplete], baseDefinitions);
      expect(first?.collapsible).toBe(false);
    });

    it('defaultCollapsed は未指定時 false になる', () => {
      const [first] = groupItems([incomplete], baseDefinitions);
      expect(first?.defaultCollapsed).toBe(false);
    });

    it('明示的に指定した値が反映される', () => {
      const defs: GroupDefinition<TestItem>[] = [
        {
          key: 'completed',
          label: '完了',
          predicate: (item) => item.completed,
          showHeader: false,
          collapsible: true,
          defaultCollapsed: true
        }
      ];
      const [first] = groupItems([complete1], defs);
      expect(first?.showHeader).toBe(false);
      expect(first?.collapsible).toBe(true);
      expect(first?.defaultCollapsed).toBe(true);
    });
  });

  describe('ラベルの伝播', () => {
    it('定義のラベルがグループに引き継がれる', () => {
      const [first] = groupItems([incomplete], baseDefinitions);
      expect(first?.label).toBe('未完了');
    });
  });
});
