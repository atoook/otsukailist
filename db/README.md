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
    ├── 01_create_tables.sql        # 開発用スキーマ
    └── 01_create_tables_test.sql   # テスト用スキーマ
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

## データベーススキーマ

### item_list

| カラム名   | データ型     | 制約                         | 説明     |
| ---------- | ------------ | ---------------------------- | -------- |
| id         | UUID         | PRIMARY KEY                  | UUID     |
| name       | VARCHAR(100) | DEFAULT 'お買い物リスト'     | リスト名 |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP    | 作成日時 |
| updated_at | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時 |

### item

| カラム名     | データ型     | 制約                         | 説明                   |
| ------------ | ------------ | ---------------------------- | ---------------------- |
| id           | UUID         | PRIMARY KEY                  | UUID                   |
| name         | VARCHAR(255) | NOT NULL                     | アイテム名             |
| is_completed | BOOLEAN      | DEFAULT FALSE                | 購入済みフラグ         |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP    | 作成日時               |
| updated_at   | TIMESTAMP(3) | DEFAULT CURRENT_TIMESTAMP(3) | 更新日時               |
| list_id      | UUID         | NOT NULL, FOREIGN KEY        | item_list への外部キー |

**インデックス:**

- `idx_list_id` on `item(list_id)`
- `idx_created_at` on `item(created_at)`

## PostgreSQL 設定

- 公式 `postgres:16` イメージを利用
- 初期化 SQL は `init/*.sql` をコンテナ起動時に自動実行
- テスト環境は `tmpfs` を使い高速化

## 環境変数設定

```bash
# .envファイルをコピーして編集
cp .env.example .env
```

## 注意事項

- **本番環境では環境変数または秘匿情報管理システムを使用してください**
- 開発環境の認証情報を本番で使用しないでください
- 初期化スクリプトはコンテナ起動時に自動実行されます
