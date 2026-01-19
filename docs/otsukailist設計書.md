# OtsukaiList - 設計書

OtsukaiList は「ログイン不要で共有できる共同おつかいリスト」を提供する OSS プロジェクト。ここでは最新の PR 方針を反映したうえで、開発チーム内で共有すべき前提を簡潔にまとめる。

---

## 技術スタック

- **Frontend**: Vue.js 3, Vite, Tailwind CSS
- **Backend**: Spring Boot (Java 17), Spring Data JPA, Spring Messaging (WebSocket)
- **Database**: MySQL 8.x
- **Infra**: Docker / docker-compose

---

## アーキテクチャ概要

- フロント → REST API → DB の 3 層構成。更新系 API は `MutationResponse<T>` で `revision` を返し、WebSocket で同じ payload を配信する。
- Command と Query のサービスを分離し、Mapper は DTO と Entity の変換に特化。業務ルールは Service 層で吸収する。
- WebSocket はリスト単位の room を用い、`revision` を基準にクライアント側で差分適用する。

---

## ルーティング

- `/new` : 新規リストの作成。リスト名と初期メンバー（1件以上）を入力→作成成功後に `/list/:id` へ遷移。
- `/list/:id` : リストのスナップショットを取得し、アイテム・メンバーの追加/編集/削除を行うメイン画面。

---

## REST API（概要）

| Method                                          | Path                                                         | 主な役割 |
| ----------------------------------------------- | ------------------------------------------------------------ | -------- |
| `POST /api/lists`                               | リストと初期メンバーをまとめて作成する。                     |
| `GET /api/lists/{listId}`                       | リスト、メンバー、アイテムをまとめたスナップショットを返す。 |
| `PATCH /api/lists/{listId}`                     | リスト名を変更し、`revision` を更新する。                    |
| `POST /api/lists/{listId}/members`              | メンバーを追加する。                                         |
| `PATCH /api/lists/{listId}/members/{memberId}`  | メンバー名を変更する。                                       |
| `DELETE /api/lists/{listId}/members/{memberId}` | メンバーを削除する。                                         |
| `POST /api/lists/{listId}/items`                | アイテムを追加する（作成時は未完了固定）。                   |
| `PATCH /api/lists/{listId}/items/{itemId}`      | アイテム名や完了状態を更新する。                             |
| `DELETE /api/lists/{listId}/items/{itemId}`     | アイテムを削除する。                                         |

---

## WebSocket

- ルーム: `list:{id}`
- REST の更新完了後に、同じ `MutationResponse` をルーム内へ配信。
- クライアントは `revision` を比較して重複適用を避ける。イベント種別は payload 内の type 追加で拡張予定。

---

## データモデル

- `ItemList` : リスト本体。`revision` を持ち、`Item` と `Member` を束ねる。
- `Member` : 表示名のみを管理し、権限は持たない。リスト内で `display_name` がユニーク。
- `Item` : 名前・完了フラグ・完了者 ID・完了日時を保持する。完了時は必ずメンバー存在チェックを行う。
- 正式な DDL は `db/init/01_create_tables.sql` を参照（UUID は `BINARY(16)`、FK や Sample Data も同ディレクトリにあり）。

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

1. `cd db && docker-compose up -d` で MySQL を起動（初期化 SQL 自動実行）。
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
