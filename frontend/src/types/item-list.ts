import type { ItemId } from './item';
import { isItemId } from './item';
import type { MemberId } from './member';
import { isMemberId } from './member';

export interface ItemList {
  id: ItemListId;
  name: string;
  members: MemberId[];
  items: ItemId[];
}

export type ItemListId = string | number;

// ItemListIdの型ガード関数
export function isItemListId(value: any): value is ItemListId {
  return typeof value === 'string' || typeof value === 'number';
}

export function isList(obj: any): obj is ItemList {
  return (
    obj !== null &&
    typeof obj === 'object' &&
    'id' in obj &&
    isItemListId(obj.id) &&
    'name' in obj &&
    typeof obj.name === 'string' &&
    'members' in obj &&
    Array.isArray(obj.members) &&
    obj.members.every((member: any) => isMemberId(member)) &&
    'items' in obj &&
    Array.isArray(obj.items) &&
    obj.items.every((item: any) => isItemId(item))
  );
}
