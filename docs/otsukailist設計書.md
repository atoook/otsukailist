# OtsukaiList - 設計書

## 技術スタック

- **Frontend**: Vue.js 3, Vite, Tailwind CSS
- **Backend**: Spring Boot (Java), WebSocket (Spring Messaging)
- **Database**: MySQL 8.x + JPA (Hibernate)
- **Infra**: Docker / docker-compose

---

## アーキテクチャ概要

- フロント（Vue.js）から Spring Boot API を呼び出して MySQL に保存。
- WebSocket (Spring Boot + STOMP) を使い、同じリスト ID を持つクライアント間でリアルタイム更新。
- URL がそのまま「共有鍵」となるシンプル設計。

---

## ルーティング

- `/new`
  - 新しいリスト作成（UUID 生成 → DB 保存）
  - `/list/:id` にリダイレクト
- `/list/:id`
  - アイテム追加/削除/チェック
  - リアルタイム同期表示

---

## API 設計（REST）

- `POST /api/lists` → 新規作成
- `GET /api/lists/:id` → リスト取得
- `POST /api/lists/:id/items` → アイテム追加
- `PATCH /api/lists/:id/items/:itemId` → アイテム更新
- `DELETE /api/lists/:id/items/:itemId` → アイテム削除

---

## WebSocket 設計

- ルーム: `list:{id}`
- イベント例:
  - `item:add`, `item:update`, `item:delete`
- サーバーで DB 更新後、room 内全員にブロードキャスト

---

## データモデル (JPA + MySQL)

```java
@Entity
@Table(name = "shopping_list")
public class ShoppingList {
    @Id
    private String id;  // UUID

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();
}

@Entity
@Table(name = "item")
public class Item {
    @Id
    private String id;  // UUID

    private String name;

    @Column(name = "is_checked")
    private boolean isChecked = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "list_id")
    private ShoppingList list;
}
```

### データベーステーブル構成

**詳細**: `db/init/01_create_tables.sql`

- **shopping_list**: id(UUID), created_at, updated_at
- **item**: id(UUID), name, is_checked, created_at, updated_at, list_id(FK)

---

## UI 設計（Vue + Tailwind）

- **入力欄＋追加ボタン**
- **リスト表示**
  - チェックボックス
  - アイテム名
  - 削除ボタン（×）
  - 購入済みは `line-through text-gray-400`

---

## セキュリティ方針

**基本思想**: 機密情報を扱わない前提のシンプル設計

- **UUID ベース URL**: 推測困難なリンクによる一時的なアクセス制御
- **URL 保有者＝編集権限**: ログイン不要での簡単共有を実現
- **機密情報非対応**: 個人情報や重要データの保存は想定外
- **一時利用想定**: BBQ・旅行等の短期間イベント向け
- **入力サニタイズ**: XSS 対策（基本的な Web セキュリティ）
- **レート制限**: 簡易 DoS 対策（サービス安定性向上）

**⚠️ 注意**: 機密情報や個人情報の記載は避けてください

---

## Docker 構成

### 開発環境構成

開発効率を重視した構成：

- `db/docker-compose.yml` - MySQL 専用（開発用）
- **Backend**: ローカル実行（IDE/Gradle 直接実行でデバッグ・ホットリロード）
- **Frontend**: ローカル実行（Vite 開発サーバー）
- `docker-compose.yml` - 全サービス統合（本番・デプロイ用）

### 開発フロー

1. **DB 起動**: `cd db && docker-compose up -d`
2. **Backend 起動**: IDE または `./gradlew bootRun`
3. **Frontend 起動**: `npm run dev` (Vite 開発サーバー)

### MySQL 設定

- **設定ファイル**: `db/docker-compose.yml`
- **初期化スクリプト**: `db/init/*.sql`
- **MySQL 設定**: `db/my.cnf` (UTF-8、メモリ設定等)

### Spring Boot 接続設定

- **設定ファイル**: `backend/src/main/resources/application.properties`
- **接続先**: localhost:3306 (Docker MySQL)
- **データベース**: otsukailist
- **JPA 設定**: hibernate.ddl-auto=validate (本番安全)

### 開発環境のメリット

- **Backend**: デバッガ接続・ホットリロード・ログ確認が容易
- **Frontend**: Vite の高速 HMR（Hot Module Replacement）
- **Database**: Docker で環境統一・データ永続化
- **切り分け**: 各レイヤーの問題を独立して解決可能

---

## ロードマップ

- **Phase 0**: UI プロト（Vue + Tailwind, state 管理のみ）
- **Phase 1**: API + DB 連携（Spring Boot + MySQL）
- **Phase 2**: リアルタイム同期（WebSocket / STOMP）
- **Phase 3**: OSS 公開（README 整備, Docker Compose 対応）
