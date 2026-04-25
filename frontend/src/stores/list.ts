import { defineStore } from 'pinia';
import type { Item, ItemListSnapshot, Member, UUID } from '@/types/api';

type ListState = {
  listId: UUID | null;
  name: string;
  revision: number;
  itemCount: number;
  lastItemActivityAt: string | null;
  members: Member[];
  items: Item[];
};

export const useListStore = defineStore('list', {
  state: (): ListState => ({
    listId: null,
    name: '',
    revision: 0,
    itemCount: 0,
    lastItemActivityAt: null,
    members: [],
    items: []
  }),

  getters: {
    isInitialized: (state) => state.listId != null,
    memberMap: (state): Map<UUID, Member> => new Map(state.members.map((member) => [member.id, member] as const))
  },

  actions: {
    setRevision(revision: number) {
      this.revision = revision;
    },

    applySnapshot(snapshot: ItemListSnapshot) {
      this.listId = snapshot.listId;
      this.name = snapshot.name;
      this.revision = snapshot.revision;
      this.itemCount = snapshot.itemCount;
      this.lastItemActivityAt = snapshot.lastItemActivityAt;
      this.members = [...snapshot.members];
      this.items = [...snapshot.items];
    },

    upsertItem(item: Item) {
      const idx = this.items.findIndex((candidate) => candidate.id === item.id);

      if (idx >= 0) {
        this.items[idx] = { ...this.items[idx], ...item };
      } else {
        this.items.unshift(item);
        this.itemCount = Math.max(0, this.itemCount + 1);
      }
    },

    removeItem(itemId: UUID) {
      const before = this.items.length;
      this.items = this.items.filter((item) => item.id !== itemId);

      if (this.items.length !== before) {
        this.itemCount = Math.max(0, this.itemCount - 1);
      }
    },

    upsertMember(member: Member) {
      const idx = this.members.findIndex((candidate) => candidate.id === member.id);

      if (idx >= 0) {
        this.members[idx] = { ...this.members[idx], ...member };
      } else {
        this.members.push(member);
      }
    },

    removeMember(memberId: UUID) {
      this.members = this.members.filter((member) => member.id !== memberId);
      this.items = this.items.map((item) =>
        item.completedByMemberId === memberId ? { ...item, completedByMemberId: null } : item
      );
    },

    updateListDetails(payload: { name: string; members: Member[]; revision?: number }) {
      this.name = payload.name;
      this.members = [...payload.members];
      if (payload.revision !== undefined) {
        this.revision = payload.revision;
      }
    },

    reset() {
      this.listId = null;
      this.name = '';
      this.revision = 0;
      this.itemCount = 0;
      this.lastItemActivityAt = null;
      this.members = [];
      this.items = [];
    }
  }
});
