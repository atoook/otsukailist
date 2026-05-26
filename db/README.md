# PostgreSQL Database Setup (環境別構成)

このディレクトリには、otsukailist プロジェクト用の PostgreSQL データベース設定ファイルが含まれています。
開発・テスト・CI 環境それぞれに最適化された構成を提供します。

> **実行方法・環境管理**: [TEST_CONFIGURATION.md](../backend/docs/TEST_CONFIGURATION.md) を参照

## ファイル構成

```
db/
├── docker-compose.dev.yml      # 開発環境用設定
├── docker-compose.test.yml     # テスト環境用設定
├── docker-compose.ci.yml       # CI環境用設定
├── .env                        # 環境変数
└── init/
    └── 02_sample_data.sql      # サンプルデータ投入SQL（運用フロー検討中）

正式なスキーマ変更は backend の Flyway マイグレーションで管理します。

```
backend/src/main/resources/db/migration/
├── V1__create_tables.sql
├── V2__add_item_assigned_member_id.sql
├── V3__add_quantified_items.sql
├── V4__add_item_category.sql
├── V5__add_item_preparation_type.sql
└── V6__add_resource_versions.sql
```

## 環境別構成

| 環境   | ポート   | データ永続化       | 最適化       | 用途       |
| ------ | -------- | ------------------ | ------------ | ---------- |
| 開発   | 5432     | あり（ボリューム） | デバッグ重視 | 日常開発   |
| テスト | 5433     | なし（tmpfs）      | 速度重視     | テスト実行 |
| CI     | 設定可能 | なし               | 軽量起動     | 自動化     |

## データベース接続情報

環境変数は `.env` ファイルで管理されています：

| 項目       | 環境変数            | 説明                                |
| ---------- | ------------------- | ----------------------------------- |
| ホスト     | `POSTGRES_HOST`     | PostgreSQL サーバーのホスト名       |
| ポート     | `POSTGRES_PORT`     | 環境別ポート（dev:5432, test:5433） |
| DB 名      | `POSTGRES_DB`       | データベース名                      |
| ユーザー   | `POSTGRES_USER`     | 接続ユーザー名                      |
| パスワード | `POSTGRES_PASSWORD` | 接続パスワード                      |

## マイグレーション

Spring Boot 起動時に Flyway が `backend/src/main/resources/db/migration` 配下のSQLを順番に適用します。

開発・テスト用の Docker Compose は空の PostgreSQL を起動するだけです。`/docker-entrypoint-initdb.d` によるスキーマ作成は使いません。

新しいスキーマ変更を追加するときは、次の命名でSQLを追加します。

```text
backend/src/main/resources/db/migration/V{連番}__{説明}.sql
```

既存DBに対しては `spring.flyway.baseline-on-migrate=true` により、Flyway管理前のスキーマをベースライン化してから差分マイグレーションを適用します。

関連する全体設計は [システム設計](../docs/architecture/system-design.md)、実行コマンドは [TEST_CONFIGURATION.md](../backend/docs/TEST_CONFIGURATION.md) を参照してください。

## データベーススキーマ

ここでは主要テーブルの概要のみを示します。完全な DDL は `backend/src/main/resources/db/migration/` 配下の Flyway SQL を正とします。

### item_list

| カラム名   | データ型     | 制約                         | 説明     |
| ---------- | ------------ | ---------------------------- | -------- |
| id         | UUID         | PRIMARY KEY                  | UUID     |
| name       | VARCHAR(100) | NOT NULL                     | リスト名 |
| revision   | BIGINT       | DEFAULT 0                    | リスト同期用の通し番号 |
| version    | BIGINT       | DEFAULT 0                    | リストメタデータの競合制御用version |
| created_at | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 作成日時 |
| updated_at | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時 |

### member

| カラム名     | データ型     | 制約                  | 説明                            |
| ------------ | ------------ | --------------------- | ------------------------------- |
| id           | UUID         | PRIMARY KEY           | UUID                            |
| list_id      | UUID         | NOT NULL, FOREIGN KEY | item_list への外部キー          |
| display_name | VARCHAR(80)  | NOT NULL              | 表示名                          |
| version      | BIGINT       | DEFAULT 0             | メンバー単体の競合制御用version |
| created_at   | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 作成日時                |
| updated_at   | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時                |

### item

| カラム名     | データ型     | 制約                         | 説明                   |
| ------------ | ------------ | ---------------------------- | ---------------------- |
| id           | UUID         | PRIMARY KEY                  | UUID                   |
| name         | VARCHAR(255) | NOT NULL                     | アイテム名             |
| version      | BIGINT       | DEFAULT 0                    | アイテム単体の競合制御用version |
| item_type    | VARCHAR(20)  | NOT NULL DEFAULT 'plain'     | `plain` / `quantified` |
| category     | TEXT         | NULL                         | item分類               |
| preparation_type | TEXT    | NULL                         | 準備種別               |
| is_completed | BOOLEAN      | DEFAULT FALSE                | 購入済みフラグ         |
| created_at   | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 作成日時               |
| updated_at   | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時               |
| list_id      | UUID         | NOT NULL, FOREIGN KEY        | item_list への外部キー |
| assigned_member_id | UUID | NULL, FOREIGN KEY | 担当者 |
| completed_by_member_id | UUID | NULL, FOREIGN KEY | 完了者 |
| completed_at | TIMESTAMP(3) | NULL | 完了日時 |

**インデックス:**

- `idx_list_id` on `item(list_id)`
- `idx_list_completed` on `item(list_id, is_completed)`
- `idx_item_list_item_type` on `item(list_id, item_type)`
- `idx_item_list_category` on `item(list_id, category)`
- `idx_item_list_preparation_type` on `item(list_id, preparation_type)`
- `idx_created_at` on `item(created_at)`

### item_quantified

| カラム名              | データ型    | 制約                         | 説明                      |
| --------------------- | ----------- | ---------------------------- | ------------------------- |
| item_id               | UUID        | PRIMARY KEY, FOREIGN KEY     | `item.id` と同一の PK     |
| quantity              | BIGINT      | NOT NULL, CHECK >= 0         | 基準単位での数量          |
| base_unit             | VARCHAR(20) | NOT NULL                     | `g` / `ml` / `piece` / `pack` |
| origin                | VARCHAR(20) | NOT NULL                     | `manual` / `generated`    |
| regeneration_policy   | VARCHAR(20) | NOT NULL                     | `none` / `auto` / `locked` |
| generator_key         | VARCHAR(80) | NULL                         | 生成ルール ID             |

### list_generation_config

| カラム名    | データ型     | 制約                         | 説明              |
| ----------- | ------------ | ---------------------------- | ----------------- |
| id          | UUID         | PRIMARY KEY                  | 生成設定 ID       |
| list_id     | UUID         | NOT NULL, FOREIGN KEY        | item_list への外部キー |
| config_type | VARCHAR(40)  | NOT NULL                     | `bbq` などの設定種別 |
| config_json | JSONB        | NOT NULL                     | 生成条件 JSON     |
| created_at  | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 作成日時          |
| updated_at  | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時          |

`list_generation_config` は `(list_id, config_type)` でユニークです。

## PostgreSQL 設定

- 公式 `postgres:16` イメージを利用
- スキーマ変更は Flyway がアプリケーション起動時に適用
- テスト環境は `tmpfs` を使い高速化

## 環境変数設定

```bash
# .envファイルをコピーして編集
cp .env.example .env
```

## 注意事項

- **本番環境では環境変数または秘匿情報管理システムを使用してください**
- 開発環境の認証情報を本番で使用しないでください
- Docker Compose は空の PostgreSQL を起動します。正式なスキーマ作成・変更は Spring Boot 起動時の Flyway に任せます
- `init/02_sample_data.sql` はサンプルデータ検討用であり、スキーマ管理には使いません
