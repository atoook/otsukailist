export type UUID = string;

export type MutationResponse<T> = {
  revision: number;
  data: T;
};

export type ApiError = {
  error: string;
  message: string;
  timestamp: string;
  details?: Record<string, unknown>;
};

export type Member = {
  id: UUID;
  displayName: string;
};

export type Item = {
  id: UUID;
  name: string;
  completed: boolean;
  completedByMemberId?: UUID | null;
  completedAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type ItemListSnapshot = {
  listId: UUID;
  name: string;
  revision: number;
  itemCount: number;
  serverTime?: string;
  members: Member[];
  items: Item[];
};

export type CreateItemListWithMembersResponse = {
  listId: UUID;
  name: string;
  revision: number;
  members: Member[];
};

export type ItemListResponse = {
  id: UUID;
  name: string;
  createdAt?: string;
  updatedAt?: string;
};

export type DeleteResponse = {
  deletedItemId?: UUID;
  deletedMemberId?: UUID;
};
