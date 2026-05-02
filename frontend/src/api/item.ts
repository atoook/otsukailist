import { http } from '@/lib/http';
import type { DeleteResponse, Item, MutationResponse, UUID } from '@/types/api';
import type { ItemCategory } from '@/types/item-category';
import type { ItemType, QuantifiedItem } from '@/types/list-generation';

export type UpdateItemPayload = {
  name?: string;
  itemType?: ItemType;
  category?: ItemCategory | null;
  completed?: boolean;
  assignedMemberId?: UUID | null;
  completedByMemberId?: UUID | null;
  quantified?: QuantifiedItem | null;
};

export type CreateItemPayload = {
  name: string;
  itemType?: ItemType;
  category?: ItemCategory | null;
  quantified?: QuantifiedItem;
};

export async function createItem(listId: UUID, payload: CreateItemPayload) {
  const res = await http.post<MutationResponse<Item>>(`/lists/${listId}/items`, payload);
  return res.data;
}

export async function updateItem(listId: UUID, itemId: UUID, payload: UpdateItemPayload) {
  const res = await http.patch<MutationResponse<Item>>(`/lists/${listId}/items/${itemId}`, payload);
  return res.data;
}

export async function deleteItem(listId: UUID, itemId: UUID) {
  const res = await http.delete<MutationResponse<DeleteResponse>>(`/lists/${listId}/items/${itemId}`);
  return res.data;
}
