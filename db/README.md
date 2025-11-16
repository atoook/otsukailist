# MySQL Database Setup (環境別構成)

このディレクトリには、otsukailist プロジェクト用の MySQL データベース設定ファイルが含まれています。
開発・テスト・CI 環境それぞれに最適化された構成を提供します。

> **実行方法・環境管理**: [TEST_CONFIGURATION.md](../backend/docs/TEST_CONFIGURATION.md) を参照

## ファイル構成

```
db/
├── docker-compose.dev.yml      # 開発環境用設定
├── docker-compose.test.yml     # テスト環境用設定
├── docker-compose.ci.yml       # CI環境用設定
├── my.cnf                      # 開発用MySQL設定
├── my-test.cnf                 # テスト用軽量MySQL設定
├── .env                        # 環境変数
└── init/
    ├── 01_create_tables.sql        # 開発用スキーマ
    └── 01_create_tables_test.sql   # テスト用スキーマ
```

## 環境別構成

| 環境   | ポート   | データ永続化       | 最適化       | 用途       |
| ------ | -------- | ------------------ | ------------ | ---------- |
| 開発   | 3306     | あり（ボリューム） | デバッグ重視 | 日常開発   |
| テスト | 3307     | なし（tmpfs）      | 速度重視     | テスト実行 |
| CI     | 設定可能 | なし               | 軽量起動     | 自動化     |

## データベース接続情報

環境変数は `.env` ファイルで管理されています：

| 項目       | 環境変数         | 説明                                |
| ---------- | ---------------- | ----------------------------------- |
| ホスト     | `MYSQL_HOST`     | MySQL サーバーのホスト名            |
| ポート     | `MYSQL_PORT`     | 環境別ポート（dev:3306, test:3307） |
| DB 名      | `MYSQL_DATABASE` | データベース名                      |
| ユーザー   | `MYSQL_USER`     | 接続ユーザー名                      |
| パスワード | `MYSQL_PASSWORD` | 接続パスワード                      |

## データベーススキーマ

### shopping_list

| カラム名   | データ型     | 制約                                                  | 説明     |
| ---------- | ------------ | ----------------------------------------------------- | -------- |
| id         | BINARY(16)   | PRIMARY KEY                                           | UUID     |
| name       | VARCHAR(100) | DEFAULT 'お買い物リスト'                              | リスト名 |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                             | 作成日時 |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新日時 |

### item

| カラム名   | データ型     | 制約                                                  | 説明                       |
| ---------- | ------------ | ----------------------------------------------------- | -------------------------- |
| id         | BINARY(16)   | PRIMARY KEY                                           | UUID                       |
| name       | VARCHAR(255) | NOT NULL                                              | アイテム名                 |
| is_checked | BOOLEAN      | DEFAULT FALSE                                         | 購入済みフラグ             |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                             | 作成日時                   |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新日時                   |
| list_id    | BINARY(16)   | NOT NULL, FOREIGN KEY                                 | shopping_list への外部キー |

**インデックス:**

- `idx_list_id` on `item(list_id)`
- `idx_created_at` on `item(created_at)`

## MySQL 設定

### 開発環境 (`my.cnf`)

- 本番同等の設定
- デバッグ・パフォーマンス重視

### テスト環境 (`my-test.cnf`)

- 軽量化設定
- メモリ使用量最小化
- 高速起動・実行

## 環境変数設定

```bash
# .envファイルをコピーして編集
cp .env.example .env
```

## 注意事項

- **本番環境では環境変数または秘匿情報管理システムを使用してください**
- 開発環境の認証情報を本番で使用しないでください
- 初期化スクリプトはコンテナ起動時に自動実行されます
