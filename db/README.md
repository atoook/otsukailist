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

### 1. データベースの起動

```bash
cd db
docker-compose up -d
```

### 2. データベースの停止

```bash
cd db
docker-compose down
```

### 3. データベースの完全削除（ボリュームも含む）

```bash
cd db
docker-compose down -v
```

## データベース接続情報

- **ホスト**: localhost
- **ポート**: 3306
- **データベース名**: otsukailist
- **ユーザー名**: otsukailist_user
- **パスワード**: otsukailist_password
- **ルートパスワード**: rootpassword

## Spring Boot 設定

`backend/src/main/resources/application.properties`に以下の設定を追加してください：

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/otsukailist?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=utf8mb4
spring.datasource.username=otsukailist_user
spring.datasource.password=otsukailist_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

## テーブル構成

### shopping_list

- **id**: VARCHAR(36) PRIMARY KEY (UUID)
- **created_at**: TIMESTAMP (作成日時)

### item

- **id**: VARCHAR(36) PRIMARY KEY (UUID)
- **list_id**: VARCHAR(36) (shopping_list への外部キー)
- **name**: VARCHAR(255) (アイテム名)
- **is_checked**: BOOLEAN (購入済みフラグ)
- **created_at**: TIMESTAMP (作成日時)

## 注意事項

- 本番環境では、パスワードを適切に変更してください
- `my.cnf`の設定は開発環境用に最適化されています
- 初期化スクリプトはコンテナ起動時に自動実行されます
