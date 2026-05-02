# OtsukaiList - 設計書

OtsukaiList は「ログイン不要で共有できる共同おつかいリスト」を提供する OSS プロジェクト。ここでは最新の PR 方針を反映したうえで、開発チーム内で共有すべき前提を簡潔にまとめる。

---

## 技術スタック

- **Frontend**: Vue.js 3, Vite, Tailwind CSS
- **Backend**: Spring Boot (Java 17), Spring Data JPA
- **Database**: PostgreSQL 16
- **Infra**: Docker / docker-compose

---

## アーキテクチャ概要

- フロント → REST API → DB の 3 層構成。更新系 API は `MutationResponse<T>` で `revision` を返し、フロントは `revision` を使って整合性を保つ。
- Command と Query のサービスを分離し、Mapper は DTO と Entity の変換に特化。業務ルールは Service 層で吸収する。
- 初期ロードは `GET /api/lists/{listId}/snapshot` で全体状態を取得し、その後は更新系 API のレスポンスで状態を更新する。

---

## ルーティング

- `/` : ウェルカム画面。
- `/create-list` : 新規リスト作成。
- `/share-list/:id` : 共有URL表示。
- `/lists/:id` : メイン画面。スナップショット表示とアイテム CRUD を担当し、メンバー操作は文脈内の軽量タスクに限定する。
- `/lists/:id/edit` : 編集専用画面。リストメタデータ更新とメンバー管理（追加/削除/表示名変更）を担当する。

---

## REST API（概要）

| Method                                          | Path                                                         | 主な役割 |
| ----------------------------------------------- | ------------------------------------------------------------ | -------- |
| `POST /api/lists`                               | リストと初期メンバーをまとめて作成する。                     |
| `GET /api/lists/{listId}/snapshot`              | リスト、メンバー、アイテムをまとめたスナップショットを返す。 |
| `PATCH /api/lists/{listId}`                     | リスト名を変更し、`revision` を更新する。                    |
| `POST /api/lists/{listId}/members`              | メンバーを追加する。                                         |
| `PATCH /api/lists/{listId}/members/{memberId}`  | メンバー名を変更する。                                       |
| `DELETE /api/lists/{listId}/members/{memberId}` | メンバーを削除する。                                         |
| `POST /api/lists/{listId}/items`                | アイテムを追加する（作成時は未完了固定）。                   |
| `PATCH /api/lists/{listId}/items/{itemId}`      | アイテム名や完了状態を更新する。                             |
| `DELETE /api/lists/{listId}/items/{itemId}`     | アイテムを削除する。                                         |

---

## リアルタイム同期

- 現在は WebSocket 未導入。
- `revision` を前提にした API 契約を維持し、将来的な差分同期導入に備える。

---

## データモデル

- `ItemList` : リスト本体。`revision` を持ち、`Item` と `Member` を束ねる。
- `Member` : 表示名のみを管理し、権限は持たない。リスト内で `display_name` がユニーク。
- `Item` : 名前・完了フラグ・完了者 ID・完了日時を保持する。`plain` / `quantified` を `item_type` で区別し、カテゴリは plain / quantified 共通で `category` に保持する。完了時は必ずメンバー存在チェックを行う。
- `ItemQuantified` : 数量付き item の詳細情報を `item` と 1:1 で保持する。`name` / `quantity` / `base_unit` / `origin` / `regeneration_policy` / `generator_key` を持つ。
- `ListGenerationConfig` : テンプレート生成条件をリスト単位で保持する将来拡張用テーブル。生成条件は `item_list` へ直接持たせず分離する。
- 数量付き item の設計方針は `docs/list-generation-automation.md` を参照。
- 正式な DDL は `backend/src/main/resources/db/migration` 配下の Flyway SQL を参照（UUID は `UUID` 型）。
- 監査系タイムスタンプ（`created_at` / `updated_at`）は **Hibernate 側で更新を管理** し、DDL では `DEFAULT CURRENT_TIMESTAMP(3)` のみを使う。`ON UPDATE CURRENT_TIMESTAMP` のような DB 依存の自動更新句は採用しない。

---

## UI / 体験

- アイテム入力欄と登録ボタン、アイテム一覧（チェックボックス + 名前 + 削除）が基本構成。
- 完了済みアイテムは打ち消し線で表示。メンバー名はドロップダウンで選択予定。
- Vue + Tailwind を前提に、デザインシステムは `docs/design-system.md` を参照。

---

## セキュリティと利用方針

- URL 共有型でログイン不要。UUID を知る人が編集可能な軽量運用を前提とする。
- 個人情報や機密データは扱わない。レート制限や XSS 対策は基本的な範囲で実施。

---

## Docker / 開発フロー

1. `cd db && docker compose up -d` で PostgreSQL を起動（初期化 SQL 自動実行）。
2. `cd backend && ./gradlew bootRun` で API を起動。
3. `cd frontend && npm install && npm run dev` でフロントを起動。
4. 静的解析: `./gradlew checkstyleMain pmdMain spotbugsMain`。

---

## ロードマップ

- **Phase 0**: Backend とは切り離した状態で UI プロトタイプを作成し、主要画面の体験を固める。
- **Phase 1**: WebSocket 前提のデータモデルと REST API を完成させた暫定版を提供し、以降の実装基盤とする。
- **Phase 2**: WebSocket を用いたリアルタイム同期を導入し、同時に UI/UX を磨き込む。
- **Phase 3**: README や Docker Compose を含む周辺ドキュメントと環境整備を整え、OSS 公開可能な品質へ仕上げる。

上記レベルを現状の正とし、必要に応じて各ドキュメントや実装を同期していく。
