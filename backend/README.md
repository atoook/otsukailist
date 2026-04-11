# OtsukaiList Backend

Spring Boot ベースのお使いリスト共有アプリケーションのバックエンド API

## 🚀 クイックスタート

```bash
# 環境変数設定
cp .env.example .env  # または cp ../db/.env .env

# データベース起動
cd ../db && docker-compose up -d

# アプリケーション起動
./gradlew bootRun

# テスト実行
./gradlew test
```

## 📋 主要機能

- **リスト管理**: 買い物リストの作成・更新・削除
- **アイテム管理**: リストアイテムの追加・更新・削除・チェック
- **UUID 共有**: ログイン不要のシンプルな URL 共有
- **リアルタイム同期**: WebSocket による即座な状態同期

## 🏗️ アーキテクチャ

### 技術スタック

- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA (Hibernate)
- **Build**: Gradle
- **Java**: 17+

### 設計原則

- **RESTful API**: リソース指向の明確な設計
- **DTO パターン**: Entity と API の分離
- **セキュリティファースト**: リスト ID による厳格なアクセス制御
- **トランザクション統合**: データ一貫性の保証

## 📚 ドキュメント

| ドキュメント                                                | 説明                           |
| ----------------------------------------------------------- | ------------------------------ |
| [📋 docs/CODING_GUIDELINES.md](./docs/CODING_GUIDELINES.md) | コーディング規約・設計パターン |
| [🚢 ../docs/backend-deploy-operations.md](../docs/backend-deploy-operations.md) | Docker/Render 運用チェック     |
| [🏢 企画書](../docs/otsukailist企画書.md)                   | プロジェクト概要・要件定義     |
| [🎨 設計書](../docs/otsukailist設計書.md)                   | システム設計・API 仕様         |

## 🛠️ 開発環境設定

### 必要なソフトウェア

- Java 17+
- Docker & Docker Compose
- Gradle 8+

### 環境変数設定

```bash
# テンプレートからコピー
cp .env.example .env

# またはデータベース設定を流用
cp ../db/.env .env
```

spring-dotenv により .env ファイルが自動読み込みされます。

## 🧪 テスト

```bash
# 全テスト実行
./gradlew test

# 特定のテストクラス実行
./gradlew test --tests ItemServiceTest

# テストレポート表示
open build/reports/tests/test/index.html
```

## 🚦 API エンドポイント

### Item List

```
GET    /api/lists/{id}           # リスト取得
POST   /api/lists                # リスト作成
PUT    /api/lists/{id}           # リスト更新
DELETE /api/lists/{id}           # リスト削除
```

### Item

```
GET    /api/lists/{listId}/items              # アイテム一覧
GET    /api/lists/{listId}/items/{itemId}     # アイテム取得
POST   /api/lists/{listId}/items              # アイテム作成
PUT    /api/lists/{listId}/items/{itemId}     # アイテム更新
DELETE /api/lists/{listId}/items/{itemId}     # アイテム削除
PATCH  /api/lists/{listId}/items/{itemId}/toggle  # チェック状態切り替え
```

## 🔧 設定

### application.properties

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/otsukailist
spring.datasource.username=${POSTGRES_USER:otsukailist_user}
spring.datasource.password=${POSTGRES_PASSWORD:otsukailist_password}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# WebSocket
spring.websocket.allowed-origins=http://localhost:3000
```

## 🤝 開発ガイド

### 新機能開発

1. [CODING_GUIDELINES.md](./docs/CODING_GUIDELINES.md) を確認
2. DTO → Service → Repository → Controller の順で実装
3. セキュリティチェック（リスト ID スコープ）を必ず実装
4. 単体テストを作成
5. コードレビューチェックリストで確認

### コミット規約

```
feat: 新機能追加
fix: バグ修正
docs: ドキュメント更新
refactor: リファクタリング
test: テスト追加
style: フォーマット変更
```

## 🐛 トラブルシューティング

### よくある問題

**Q: データベース接続エラー**

```bash
# PostgreSQL コンテナの状態確認
cd ../db && docker compose ps

# ログ確認
cd ../db && docker compose logs postgres
```

**Q: ビルドエラー**

```bash
# 依存関係のクリーンアップ
./gradlew clean build --refresh-dependencies
```

**Q: テスト失敗**

```bash
# 詳細なテストログ表示
./gradlew test --info
```

## 📄 ライセンス

Private Project - OtsukaiList Development Team

---

**最終更新: 2025-11-16**
