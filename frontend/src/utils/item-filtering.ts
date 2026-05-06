import type { Item } from '@/types/item';
import type { ItemCategory } from '@/types/item-category';
import type { MemberId } from '@/types/member';
import { normalizeForSearch } from './text-normalization';

export type ItemFilters = {
  searchQuery: string;
  memberId: MemberId | null;
  category: ItemCategory | null;
};

export function getItemFilterMemberId(item: Item): MemberId | null {
  return item.completed ? item.completedByMemberId : item.assignedMemberId;
}

export function filterItems(items: Item[], filters: ItemFilters): Item[] {
  const normalizedQuery = normalizeForSearch(filters.searchQuery);

  return items.filter((item) => {
    if (filters.memberId && getItemFilterMemberId(item) !== filters.memberId) {
      return false;
    }

    if (filters.category && item.category !== filters.category) {
      return false;
    }

    if (!normalizedQuery) {
      return true;
    }

    return normalizeForSearch(item.name).includes(normalizedQuery);
  });
}
