import type { ItemType, QuantifiedItem } from './list-generation';
import type { ItemCategory } from './item-category';
import type { ItemPreparationType } from './item-preparation-type';

export type UUID = string;

export type MutationResponse<T> = {
  revision: number;
  changed?: boolean;
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
  version: number;
};

export type Item = {
  id: UUID;
  name: string;
  version: number;
  itemType: ItemType;
  category: ItemCategory | null;
  preparationType: ItemPreparationType | null;
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
  version: number;
  itemCount: number;
  serverTime?: string;
  lastItemActivityAt: string | null;
  members: Member[];
  items: Item[];
};

export type CreateItemListWithMembersResponse = {
  listId: UUID;
  name: string;
  version: number;
  members: Member[];
};

export type ItemListResponse = {
  id: UUID;
  name: string;
  version: number;
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
