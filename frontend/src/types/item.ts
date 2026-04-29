import type { Item as ApiItem, UUID } from './api';

export type Item = ApiItem;
export type ItemId = UUID;

export function isItem(value: unknown): value is Item {
  if (value == null || typeof value !== 'object') {
    return false;
  }

  const candidate = value as Item;

  return (
    isItemId(candidate.id) &&
    typeof candidate.name === 'string' &&
    typeof candidate.completed === 'boolean' &&
    (candidate.assignedMemberId === null || isItemId(candidate.assignedMemberId)) &&
    (candidate.completedByMemberId === null || isItemId(candidate.completedByMemberId)) &&
    (candidate.completedAt === null || typeof candidate.completedAt === 'string') &&
    typeof candidate.createdAt === 'string' &&
    typeof candidate.updatedAt === 'string'
  );
}

export function isItemId(value: unknown): value is ItemId {
  return typeof value === 'string';
}

export function isItemCompleted(item: Item): boolean {
  return item.completed === true;
}
