import { http } from '@/lib/http';
import { ifMatchHeaders } from '@/api/preconditions';
import type {
  CreateItemListWithMembersResponse,
  ItemListResponse,
  ItemListSnapshot,
  ListMetaItem,
  MutationResponse,
  UUID
} from '@/types/api';

export async function createItemList(payload: { name: string; memberNames: string[] }) {
  const res = await http.post<MutationResponse<CreateItemListWithMembersResponse>>('/lists', payload);
  return res.data;
}

export async function fetchSnapshot(listId: UUID) {
  const res = await http.get<ItemListSnapshot>(`/lists/${listId}/snapshot`);
  return res.data;
}

export async function renameList(listId: UUID, payload: { name: string }, version: number) {
  const res = await http.patch<MutationResponse<ItemListResponse>>(
    `/lists/${listId}`,
    payload,
    ifMatchHeaders(version)
  );
  return res.data;
}

export async function fetchListsMeta(listIds: UUID[]) {
  const params = new URLSearchParams();
  for (const id of listIds) {
    params.append('listIds', id);
  }
  const res = await http.get<ListMetaItem[]>(`/lists/meta?${params.toString()}`);
  return res.data;
}
