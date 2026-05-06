import type { Item as ApiItem, UUID } from './api';
import { ITEM_CATEGORY_VALUES, type ItemCategory } from './item-category';
import {
  BASE_UNIT_VALUES,
  ITEM_ORIGIN_VALUES,
  ITEM_TYPE_VALUES,
  REGENERATION_POLICY_VALUES,
  type BaseUnit,
  type ItemOrigin,
  type ItemType,
  type RegenerationPolicy
} from './list-generation';

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
    isItemType(candidate.itemType) &&
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

function isItemType(value: unknown): value is ItemType {
  return typeof value === 'string' && ITEM_TYPE_VALUES.includes(value as ItemType);
}

function isBaseUnit(value: unknown): value is BaseUnit {
  return typeof value === 'string' && BASE_UNIT_VALUES.includes(value as BaseUnit);
}

function isItemOrigin(value: unknown): value is ItemOrigin {
  return typeof value === 'string' && ITEM_ORIGIN_VALUES.includes(value as ItemOrigin);
}

function isRegenerationPolicy(value: unknown): value is RegenerationPolicy {
  return typeof value === 'string' && REGENERATION_POLICY_VALUES.includes(value as RegenerationPolicy);
}

function isQuantifiedItemForType(item: Item): boolean {
  if (item.itemType === 'plain') {
    return item.quantified === null;
  }
  if (item.quantified === null || typeof item.quantified !== 'object') {
    return false;
  }

  return (
    typeof item.quantified.quantity === 'number' &&
    isBaseUnit(item.quantified.baseUnit) &&
    isItemOrigin(item.quantified.origin) &&
    isRegenerationPolicy(item.quantified.regenerationPolicy) &&
    (item.quantified.generatorKey === null || typeof item.quantified.generatorKey === 'string')
  );
}

export function isItemId(value: unknown): value is ItemId {
  return typeof value === 'string';
}

export function isItemCompleted(item: Item): boolean {
  return item.completed === true;
}
