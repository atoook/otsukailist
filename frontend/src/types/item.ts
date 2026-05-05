import type { Item as ApiItem, UUID } from './api';
import { ITEM_CATEGORY_VALUES, type ItemCategory } from './item-category';

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
    (candidate.itemType === 'plain' || candidate.itemType === 'quantified') &&
    isItemCategory(candidate.category) &&
    isQuantifiedItemForType(candidate) &&
    typeof candidate.completed === 'boolean' &&
    (candidate.assignedMemberId === null || isItemId(candidate.assignedMemberId)) &&
    (candidate.completedByMemberId === null || isItemId(candidate.completedByMemberId)) &&
    (candidate.completedAt === null || typeof candidate.completedAt === 'string') &&
    typeof candidate.createdAt === 'string' &&
    typeof candidate.updatedAt === 'string'
  );
}

function isItemCategory(value: unknown): value is ItemCategory | null {
  return value === null || (typeof value === 'string' && ITEM_CATEGORY_VALUES.includes(value as ItemCategory));
}

function isQuantifiedItemForType(item: Item): boolean {
  if (item.itemType === 'plain') {
    return item.quantified === null;
  }
  if (item.quantified === null || typeof item.quantified !== 'object') {
    return false;
  }

  return (
    typeof item.quantified.name === 'string' &&
    typeof item.quantified.quantity === 'number' &&
    ['g', 'ml', 'piece', 'pack'].includes(item.quantified.baseUnit) &&
    ['manual', 'generated'].includes(item.quantified.origin) &&
    ['none', 'auto', 'locked'].includes(item.quantified.regenerationPolicy) &&
    (item.quantified.generatorKey === null || typeof item.quantified.generatorKey === 'string')
  );
}

export function isItemId(value: unknown): value is ItemId {
  return typeof value === 'string';
}

export function isItemCompleted(item: Item): boolean {
  return item.completed === true;
}
