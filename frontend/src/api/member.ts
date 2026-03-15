import { http } from '@/lib/http';
import type { DeleteResponse, Member, MutationResponse, UUID } from '@/types/api';

export async function createMember(listId: UUID, payload: { displayName: string }) {
  const res = await http.post<MutationResponse<Member>>(`/lists/${listId}/members`, payload);
  return res.data;
}

export async function updateMember(listId: UUID, memberId: UUID, payload: { displayName: string }) {
  const res = await http.patch<MutationResponse<Member>>(`/lists/${listId}/members/${memberId}`, payload);
  return res.data;
}

export async function deleteMember(listId: UUID, memberId: UUID) {
  const res = await http.delete<MutationResponse<DeleteResponse>>(`/lists/${listId}/members/${memberId}`);
  return res.data;
}
