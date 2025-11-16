# MySQL Database Setup

このディレクトリには、otsukailist プロジェクト用の MySQL データベース設定ファイルが含まれています。

## ファイル構成

```
db/
├── docker-compose.yml      # Docker Compose設定ファイル
├── my.cnf                  # MySQL設定ファイル
├── init/                   # データベース初期化スクリプト
│   ├── 01_create_tables.sql # テーブル作成SQL
│   └── 02_sample_data.sql   # サンプルデータ投入SQL
└── README.md              # このファイル
```

## 使用方法

### 1. 環境変数の設定

```bash
# .envファイルをコピー
cp .env.example .env

# 必要に応じて .env ファイルを編集
```

### 2. データベースの起動

```bash
cd db
docker-compose up -d
```

### 3. データベースの停止

```bash
cd db
docker-compose down
```

### 4. データベースの完全削除（ボリュームも含む）

```bash
cd db
docker-compose down -v
```

## データベース接続情報

環境変数は `.env` ファイルで管理されています。

- **ホスト**: `${MYSQL_HOST}`
- **ポート**: `${MYSQL_PORT}`
- **データベース名**: `${MYSQL_DATABASE}`
- **ユーザー名**: `${MYSQL_USER}`
- **パスワード**: `${MYSQL_PASSWORD}`

設定例は `.env.example` ファイルを参照してください。

## Spring Boot 設定

`backend/src/main/resources/application.properties`に以下の設定を追加してください：

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3306}/${MYSQL_DATABASE:otsukailist}?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=utf8mb4
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## テーブル構成

### shopping_list

| カラム名   | データ型  | 制約                                                  | 説明     |
| ---------- | --------- | ----------------------------------------------------- | -------- |
| id         | CHAR(36)  | PRIMARY KEY                                           | UUID     |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP                             | 作成日時 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新日時 |

### item

| カラム名   | データ型     | 制約                                                  | 説明                       |
| ---------- | ------------ | ----------------------------------------------------- | -------------------------- |
| id         | CHAR(36)     | PRIMARY KEY                                           | UUID                       |
| name       | VARCHAR(255) | NOT NULL                                              | アイテム名                 |
| is_checked | BOOLEAN      | DEFAULT FALSE                                         | 購入済みフラグ             |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP                             | 作成日時                   |
| updated_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新日時                   |
| list_id    | CHAR(36)     | NOT NULL, FOREIGN KEY                                 | shopping_list への外部キー |

**インデックス:**

- `idx_list_id` on `item(list_id)`
- `idx_created_at` on `item(created_at)`

## 注意事項

- **本番環境では環境変数または秘匿情報管理システムを使用してください**
- 開発環境の認証情報を本番で使用しないでください
- `my.cnf`の設定は開発環境用に最適化されています
- 初期化スクリプトはコンテナ起動時に自動実行されます
