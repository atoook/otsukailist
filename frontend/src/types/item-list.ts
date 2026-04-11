import type { ItemListSnapshot, UUID } from './api';

export type ItemList = ItemListSnapshot;
export type ItemListId = UUID;

export function isItemListId(value: unknown): value is ItemListId {
  return typeof value === 'string';
}

export function isItemList(value: unknown): value is ItemList {
  if (
    value == null ||
    typeof value !== 'object' ||
    !('listId' in value) ||
    !isItemListId((value as ItemList).listId)
  ) {
    return false;
  }

  const snapshot = value as ItemList;

  return (
    typeof snapshot.name === 'string' &&
    typeof snapshot.revision === 'number' &&
    typeof snapshot.itemCount === 'number' &&
    Array.isArray(snapshot.members) &&
    Array.isArray(snapshot.items)
  );
}
