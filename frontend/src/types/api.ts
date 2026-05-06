import type { ItemType, QuantifiedItem } from './list-generation';
import type { ItemCategory } from './item-category';

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
  itemType: ItemType;
  category: ItemCategory | null;
  quantified: QuantifiedItem | null;
  completed: boolean;
  assignedMemberId: UUID | null;
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

export type ListMetaItem = {
  listId: UUID;
  name: string;
  itemCount: number;
  incompleteCount: number;
  lastItemActivityAt: string | null;
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
