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
  completedByMemberId: UUID | null;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ItemListSnapshot = {
  listId: UUID;
  name: string;
  revision: number;
  itemCount: number;
  serverTime?: string;
  lastItemActivityAt: string | null;
  members: Member[];
  items: Item[];
};

export type CreateItemListWithMembersResponse = {
  listId: UUID;
  name: string;
  members: Member[];
};

export type ItemListResponse = {
  id: UUID;
  name: string;
  createdAt?: string;
  updatedAt?: string;
};

/**
 * Delete API response invariant:
 * at least one deleted target id is always present.
 */
export type DeleteResponse =
  | {
      deletedItemId: UUID;
      deletedMemberId?: never;
    }
  | {
      deletedMemberId: UUID;
      deletedItemId?: never;
    };
