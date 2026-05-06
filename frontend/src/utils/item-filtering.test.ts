import { describe, expect, it } from 'vitest';
import type { Item } from '@/types/item';
import { filterItems, getItemFilterMemberId } from './item-filtering';

const baseItem: Item = {
  id: 'item-1',
  name: '牛肉',
  itemType: 'plain',
  category: 'meat',
  quantified: null,
  completed: false,
  assignedMemberId: null,
  completedByMemberId: null,
  completedAt: null,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z'
};

function item(overrides: Partial<Item>): Item {
  return { ...baseItem, ...overrides };
}

describe('item-filtering', () => {
  describe('getItemFilterMemberId', () => {
    it('未完了アイテムは担当者で判定する', () => {
      expect(getItemFilterMemberId(item({ completed: false, assignedMemberId: 'member-1' }))).toBe('member-1');
    });

    it('完了アイテムは購入者で判定する', () => {
      expect(
        getItemFilterMemberId(
          item({
            completed: true,
            assignedMemberId: 'member-1',
            completedByMemberId: 'member-2'
          })
        )
      ).toBe('member-2');
    });
  });

  describe('filterItems', () => {
    const items: Item[] = [
      item({ id: 'meat-1', name: '牛肉', category: 'meat', assignedMemberId: 'member-1' }),
      item({ id: 'drink-1', name: 'お茶', category: 'drinks', assignedMemberId: 'member-2' }),
      item({ id: 'meat-2', name: '豚肉', category: 'meat', completed: true, completedByMemberId: 'member-2' })
    ];

    it('カテゴリーで絞り込む', () => {
      expect(
        filterItems(items, { searchQuery: '', memberId: null, category: 'meat' }).map((candidate) => candidate.id)
      ).toEqual(['meat-1', 'meat-2']);
    });

    it('member とカテゴリーを組み合わせて絞り込む', () => {
      expect(
        filterItems(items, { searchQuery: '', memberId: 'member-2', category: 'meat' }).map((candidate) => candidate.id)
      ).toEqual(['meat-2']);
    });

    it('検索語も他のフィルターと組み合わせる', () => {
      expect(
        filterItems(items, { searchQuery: '茶', memberId: 'member-2', category: 'drinks' }).map(
          (candidate) => candidate.id
        )
      ).toEqual(['drink-1']);
    });
  });
});
