# OtsukaiList - 設計書

## 技術スタック
- **Frontend**: Vue.js 3, Vite, Tailwind CSS
- **Backend**: Spring Boot (Java), WebSocket (Spring Messaging)
- **Database**: MySQL 8.x + JPA (Hibernate)
- **Infra**: Docker / docker-compose

---

## アーキテクチャ概要
- フロント（Vue.js）から Spring Boot API を呼び出して MySQL に保存。
- WebSocket (Spring Boot + STOMP) を使い、同じリストIDを持つクライアント間でリアルタイム更新。
- URLがそのまま「共有鍵」となるシンプル設計。

---

## ルーティング
- `/new`  
  - 新しいリスト作成（UUID生成 → DB保存）
  - `/list/:id` にリダイレクト
- `/list/:id`  
  - アイテム追加/削除/チェック
  - リアルタイム同期表示

---

## API設計（REST）
- `POST /api/lists` → 新規作成
- `GET /api/lists/:id` → リスト取得
- `POST /api/lists/:id/items` → アイテム追加
- `PATCH /api/lists/:id/items/:itemId` → アイテム更新
- `DELETE /api/lists/:id/items/:itemId` → アイテム削除

---

## WebSocket設計
- ルーム: `list:{id}`
- イベント例:
  - `item:add`, `item:update`, `item:delete`
- サーバーでDB更新後、room内全員にブロードキャスト

---

## データモデル (JPA + MySQL)

```java
@Entity
public class ShoppingList {
    @Id
    private String id;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();
}

@Entity
public class Item {
    @Id
    private String id;

    private String name;

    private boolean isChecked = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "list_id")
    private ShoppingList list;
}
```

---

## UI設計（Vue + Tailwind）
- **入力欄＋追加ボタン**
- **リスト表示**
  - チェックボックス
  - アイテム名
  - 削除ボタン（×）
  - 購入済みは `line-through text-gray-400`

---

## セキュリティ方針
- UUIDベースURL → 推測困難
- URL保有者＝編集権限
- 入力サニタイズ（XSS対策）
- レート制限（簡易DoS対策）

---

## ロードマップ
- **Phase 0**: UIプロト（Vue + Tailwind, state管理のみ）
- **Phase 1**: API + DB連携（Spring Boot + MySQL）
- **Phase 2**: リアルタイム同期（WebSocket / STOMP）
- **Phase 3**: OSS公開（README整備, Docker Compose対応）
