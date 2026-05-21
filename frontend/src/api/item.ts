import { http } from '@/lib/http';
import type { DeleteResponse, Item, MutationResponse, UUID } from '@/types/api';
import type { ItemCategory } from '@/types/item-category';
import type { ItemPreparationType } from '@/types/item-preparation-type';
import type { ItemType, QuantifiedItem } from '@/types/list-generation';

export type UpdateItemPayload = {
  name?: string;
  itemType?: ItemType;
  category?: ItemCategory | null;
  preparationType?: ItemPreparationType | null;
  completed?: boolean;
  assignedMemberId?: UUID | null;
  completedByMemberId?: UUID | null;
  quantified?: QuantifiedItem | null;
};

export type MarkItemCompletedPayload = {
  completedByMemberId: UUID;
};

type CreateItemPayloadBase = {
  name: string;
  category?: ItemCategory | null;
  preparationType?: ItemPreparationType | null;
  assignedMemberId?: UUID | null;
};

type CreatePlainItemPayload = CreateItemPayloadBase & {
  itemType?: 'plain';
  quantified?: never;
};

type CreateQuantifiedItemPayload = CreateItemPayloadBase & {
  itemType: 'quantified';
  quantified: QuantifiedItem;
};

export type CreateItemPayload = CreatePlainItemPayload | CreateQuantifiedItemPayload;

export type SyncGeneratedItemsPayload = {
  generatorKeysInScope: string[];
  items: CreateQuantifiedItemPayload[];
};

export type SyncGeneratedItemsResponse = {
  items: Item[];
  deletedItemIds: UUID[];
};

export async function createItem(listId: UUID, payload: CreateItemPayload) {
  const res = await http.post<MutationResponse<Item>>(`/lists/${listId}/items`, payload);
  return res.data;
}

export async function updateItem(listId: UUID, itemId: UUID, payload: UpdateItemPayload) {
  const res = await http.patch<MutationResponse<Item>>(`/lists/${listId}/items/${itemId}`, payload);
  return res.data;
}

export async function markItemCompleted(listId: UUID, itemId: UUID, payload: MarkItemCompletedPayload) {
  const res = await http.patch<MutationResponse<Item>>(`/lists/${listId}/items/${itemId}/mark-completed`, payload);
  return res.data;
}

export async function markItemIncomplete(listId: UUID, itemId: UUID) {
  const res = await http.patch<MutationResponse<Item>>(`/lists/${listId}/items/${itemId}/mark-incomplete`);
  return res.data;
}

export async function deleteItem(listId: UUID, itemId: UUID) {
  const res = await http.delete<MutationResponse<DeleteResponse>>(`/lists/${listId}/items/${itemId}`);
  return res.data;
}

export async function syncGeneratedItems(listId: UUID, payload: SyncGeneratedItemsPayload) {
  const res = await http.post<MutationResponse<SyncGeneratedItemsResponse>>(
    `/lists/${listId}/generated-items/sync`,
    payload
  );
  return res.data;
}
