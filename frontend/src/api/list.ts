import { http } from '@/lib/http';
import type { CreateItemListWithMembersResponse, ItemListSnapshot, MutationResponse, UUID } from '@/types/api';

export async function createItemList(payload: { name: string; memberNames: string[] }) {
  const res = await http.post<MutationResponse<CreateItemListWithMembersResponse>>('/lists', payload);
  return res.data;
}

export async function fetchSnapshot(listId: UUID) {
  const res = await http.get<ItemListSnapshot>(`/lists/${listId}/snapshot`);
  return res.data;
}
