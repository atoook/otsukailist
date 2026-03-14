import type { Item as ApiItem, UUID } from './api';

export type Item = ApiItem;
export type ItemId = UUID;

export function isItem(value: unknown): value is Item {
  return (
    value != null &&
    typeof value === 'object' &&
    'id' in value &&
    isItemId((value as Item).id) &&
    'name' in value &&
    typeof (value as Item).name === 'string' &&
    'completed' in value &&
    typeof (value as Item).completed === 'boolean'
  );
}

export function isItemId(value: unknown): value is ItemId {
  return typeof value === 'string';
}

export function isItemCompleted(item: Item): boolean {
  return item.completed === true;
}
