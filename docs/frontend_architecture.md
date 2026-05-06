# Vue Frontend Architecture (Pinia + MutationResponse + ApiError)

このドキュメントは、以下の前提で Vue フロントエンドの状態管理と API 連携の設計をまとめたものです。

- 状態管理: **Pinia**
- 更新系 API: **MutationResponse<T>**
- 初期ロード: **Snapshot API**
- エラーハンドリング: **ApiError**
- Backend: **Spring Boot**
- 開発環境: **CORS 設定で直接接続（proxy未使用）**

---

# 1. API 契約

## MutationResponse（更新系）

更新系 API はすべて以下の形式を返す。

```json
{
  "revision": 12,
  "data": { ... }
}
```

TypeScript 型:

```ts
export type MutationResponse<T> = {
  revision: number;
  data: T;
};
```

### 目的

- revision をフロントが保持
- 将来 WebSocket 差分同期に対応

---

## Snapshot（初期ロード）

```json
{
  "listId": "uuid",
  "name": "買い物リスト",
  "revision": 12,
  "itemCount": 3,
  "members": [],
  "items": []
}
```

---

## ApiError

```json
{
  "error": "validation_error",
  "message": "入力内容に誤りがあります",
  "timestamp": "2025-01-01T12:00:00Z",
  "details": {
    "fieldErrors": {
      "name": "必須です"
    }
  }
}
```

TypeScript 型:

```ts
export type ApiError = {
  error: string;
  message: string;
  timestamp: string;
  details?: Record<string, any>;
};
```

---

# 2. TypeScript 型定義

`src/types/api.ts`

```ts
export type UUID = string;

export type MutationResponse<T> = {
  revision: number;
  data: T;
};

export type ApiError = {
  error: string;
  message: string;
  timestamp: string;
  details?: Record<string, any>;
};

export type Member = {
  id: UUID;
  displayName: string;
};

export type Item = {
  id: UUID;
  name: string;
  itemType: "plain" | "quantified";
  category: ItemCategory | null;
  quantified: QuantifiedItem | null;
  completed: boolean;
  assignedMemberId: UUID | null;
  completedByMemberId: UUID | null;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type QuantifiedItem = {
  quantity: number;
  baseUnit: "g" | "ml" | "piece" | "pack";
  origin: "manual" | "generated";
  regenerationPolicy: "none" | "auto" | "locked";
  generatorKey: string | null;
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
  revision: number;
  members: Member[];
};

export type DeleteResponse = {
  deletedItemId?: UUID;
  deletedMemberId?: UUID;
};
```

数量付き item とテンプレート自動生成の方針は `docs/list-generation-automation.md` を参照。

---

# 3. HTTP クライアント

`src/lib/http.ts`

```ts
import axios from "axios";
import type { ApiError } from "@/types/api";

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  headers: { "Content-Type": "application/json" },
  withCredentials: false,
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err?.response?.data;

    if (data?.error && data?.message) {
      return Promise.reject(data as ApiError);
    }

    return Promise.reject({
      error: "network_error",
      message: err?.message ?? "Network error",
      timestamp: new Date().toISOString(),
      details: { status: err?.response?.status },
    } satisfies ApiError);
  },
);
```

`.env.local`

```
VITE_API_BASE_URL=http://localhost:8080
```

---

# 4. Mutation composable

MutationResponse を処理する共通ロジック。  
責務は以下の3点。

1. API 例外を ApiError に正規化
2. `revision` の単調増加を保証（古いレスポンスは破棄）
3. `revision` が飛んだ場合の Snapshot 取得トリガー

`src/composables/useMutation.ts`

```ts
import { computed, ref } from "vue";
import type { ApiError, MutationResponse } from "@/types/api";
import { useListStore } from "@/stores/list";
import { fetchSnapshot } from "@/api/list";

type MutationRunResult<T> = {
  data: T;
  applied: boolean;
};

export function useMutation() {
  const listStore = useListStore();

  const loading = ref(false);
  const error = ref<ApiError | null>(null);

  const hasError = computed(() => error.value != null);

  async function run<T>(
    fn: () => Promise<MutationResponse<T>>,
  ): Promise<MutationRunResult<T>> {
    loading.value = true;
    error.value = null;

    try {
      const res = await fn();
      const currentRevision = listStore.revision;
      const nextRevision = res.revision;
      const gap = nextRevision - currentRevision;

      if (nextRevision <= currentRevision) {
        // 既に新しい状態を保持しているため反映不要
        return { data: res.data, applied: false };
      }

      listStore.setRevision(nextRevision);

      if (gap > 1 && listStore.listId) {
        const snapshot = await fetchSnapshot(listStore.listId);
        listStore.applySnapshot(snapshot);
        return { data: res.data, applied: false };
      }

      return { data: res.data, applied: true };
    } catch (e: any) {
      error.value = e as ApiError;
      throw e;
    } finally {
      loading.value = false;
    }
  }

  return { run, loading, error, hasError };
}
```

---

# 5. Pinia Store（revision前提）

`src/stores/list.ts`

```ts
import { defineStore } from "pinia";
import type { Item, Member, UUID } from "@/types/api";

type ListState = {
  listId: UUID | null;
  name: string;
  revision: number;
  itemCount: number;
  lastItemActivityAt: string | null;
  members: Member[];
  items: Item[];
};

export const useListStore = defineStore("list", {
  state: (): ListState => ({
    listId: null,
    name: "",
    revision: 0,
    itemCount: 0,
    lastItemActivityAt: null,
    members: [],
    items: [],
  }),

  getters: {
    isInitialized: (s) => s.listId != null,
    memberMap: (s) => new Map(s.members.map((m) => [m.id, m] as const)),
  },

  actions: {
    setRevision(rev: number) {
      this.revision = rev;
    },

    applySnapshot(payload: {
      listId: UUID;
      name: string;
      revision: number;
      itemCount: number;
      members: Member[];
      items: Item[];
    }) {
      this.listId = payload.listId;
      this.name = payload.name;
      this.revision = payload.revision;
      this.itemCount = payload.itemCount;
      this.members = payload.members;
      this.items = payload.items;
    },

    upsertItem(item: Item) {
      const idx = this.items.findIndex((i) => i.id === item.id);

      if (idx >= 0) {
        this.items[idx] = { ...this.items[idx], ...item };
      } else {
        this.items.unshift(item);
        this.itemCount += 1;
      }
    },

    removeItem(itemId: UUID) {
      const before = this.items.length;
      this.items = this.items.filter((i) => i.id !== itemId);

      if (this.items.length !== before) {
        this.itemCount = Math.max(0, this.itemCount - 1);
      }
    },

    upsertMember(member: Member) {
      const idx = this.members.findIndex((m) => m.id === member.id);

      if (idx >= 0) {
        this.members[idx] = { ...this.members[idx], ...member };
      } else {
        this.members.push(member);
      }
    },

    removeMember(memberId: UUID) {
      this.members = this.members.filter((m) => m.id !== memberId);

      this.items = this.items.map((i) =>
        i.completedByMemberId === memberId
          ? { ...i, completedByMemberId: null }
          : i,
      );
    },
  },
});
```

---

# 6. API 呼び出し

## List API

```ts
import { http } from "@/lib/http";
import type {
  CreateItemListWithMembersResponse,
  ItemListSnapshot,
  MutationResponse,
} from "@/types/api";

export async function createItemList(payload: {
  name: string;
  memberNames: string[];
}) {
  const res = await http.post<
    MutationResponse<CreateItemListWithMembersResponse>
  >("/lists", payload);

  return res.data;
}

export async function fetchSnapshot(listId: string) {
  const res = await http.get<ItemListSnapshot>(`/lists/${listId}/snapshot`);

  return res.data;
}
```

---

## Item API

```ts
import { http } from "@/lib/http";
import type { Item, MutationResponse, UUID, DeleteResponse } from "@/types/api";

export async function createItem(listId: UUID, payload: { name: string }) {
  const res = await http.post<MutationResponse<Item>>(
    `/lists/${listId}/items`,
    payload,
  );
  return res.data;
}

export async function updateItem(listId: UUID, itemId: UUID, payload: any) {
  const res = await http.patch<MutationResponse<Item>>(
    `/lists/${listId}/items/${itemId}`,
    payload,
  );
  return res.data;
}

export async function deleteItem(listId: UUID, itemId: UUID) {
  const res = await http.delete<MutationResponse<DeleteResponse>>(
    `/lists/${listId}/items/${itemId}`,
  );
  return res.data;
}
```

---

# 7. エラーハンドリング

`src/lib/error.ts`

```ts
import type { ApiError } from "@/types/api";

export function getFieldErrors(err: ApiError): Record<string, string> {
  const fe = err?.details?.fieldErrors;

  if (fe && typeof fe === "object") {
    return fe;
  }

  return {};
}
```

---

# 8. 使用例（Item作成）

```ts
import { useMutation } from "@/composables/useMutation";
import { useListStore } from "@/stores/list";
import { createItem } from "@/api/item";

const listStore = useListStore();
const { run } = useMutation();

async function onAdd(name: string) {
  const result = await run(() => createItem(listStore.listId!, { name }));

  if (result.applied) {
    listStore.upsertItem(result.data);
  }
}
```

---

# 9. 今後の拡張

## Socket 対応

- WebSocket も REST と同じ `MutationResponse<T>` を publish
  - `revision` が現在と同じ/古い → 破棄
  - `revision` がちょうど +1 → `applied: true` で store action に渡す
  - `revision` が 2 以上飛んだ → `fetchSnapshot` で再同期
- 取りこぼし検知は revision ギャップのみ。差分は管理しないライトな戦略。
- Socket 受信時も `useMutation` と同じ処理（もしくは共通 util）を通して単調増加を保証。

---

## UI エラーハンドリング

- validation_error → フォーム表示
- conflict → toast
- internal_error → generic error

---

# まとめ

この設計のメリット:

- MutationResponse による **revision一元管理**
- composable による **更新ロジック共通化**
- Pinia による **シンプルな状態管理**
- ApiError による **統一されたエラーハンドリング**
