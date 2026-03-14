import { http } from '@/lib/http';
import type { DeleteResponse, Item, MutationResponse, UUID } from '@/types/api';

export async function createItem(listId: UUID, payload: { name: string }) {
  const res = await http.post<MutationResponse<Item>>(`/lists/${listId}/items`, payload);
  return res.data;
}

export async function updateItem(listId: UUID, itemId: UUID, payload: Partial<Item>) {
  const res = await http.patch<MutationResponse<Item>>(`/lists/${listId}/items/${itemId}`, payload);
  return res.data;
}

export async function deleteItem(listId: UUID, itemId: UUID) {
  const res = await http.delete<MutationResponse<DeleteResponse>>(`/lists/${listId}/items/${itemId}`);
  return res.data;
}
