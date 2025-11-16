# Backend Environment Setup

## .env ファイルの設定

以下のいずれかの方法で環境変数を設定してください：

### 方法 1: テンプレートからコピー

```bash
cp .env.example .env
```

### 方法 2: データベース設定を流用

```bash
cp ../db/.env .env
```

## 起動方法

```bash
# データベース起動（別ターミナル）
cd ../db && docker-compose up -d

# バックエンド起動
./gradlew bootRun
```

spring-dotenv により .env ファイルが自動読み込みされます。
