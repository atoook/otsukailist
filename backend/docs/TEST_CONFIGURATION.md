# 環境別実行コマンド

バックエンドのテストとローカル実行に必要な DB 環境の操作をまとめる。`env.sh` はリポジトリルートから実行する前提です。

## 開発環境

リポジトリルート:

```bash
./env.sh dev up
```

`backend/`:

```bash
./gradlew bootRun
```

リポジトリルート:

```bash
./env.sh dev down
```

## テスト環境

`backend/`:

```bash
./gradlew setupTestEnv
./gradlew test
./gradlew cleanTestEnv
```

特定テストのみ実行:

```bash
./gradlew test --tests ItemCommandServiceTest
./gradlew test --tests 'ItemCommandServiceTest.methodName'
```

手動で DB を管理する場合は、リポジトリルートで次を実行する。

```bash
./env.sh test up
./env.sh test down
```

## CI 環境

リポジトリルート:

```bash
./env.sh ci up
```

`backend/`:

```bash
./gradlew check
```

リポジトリルート:

```bash
./env.sh ci down
```

## 環境の詳細

| 環境 | ポート | データ永続化 | 用途 |
| --- | --- | --- | --- |
| dev | 5432 | Docker volume | 日常開発 |
| test | 5433 | tmpfs | テスト実行 |
| ci | 5432 | なし | CI |

## 構成ファイル

```text
env.sh
db/docker-compose.dev.yml
db/docker-compose.test.yml
db/docker-compose.ci.yml
backend/gradle/test-db.gradle
```

## トラブルシューティング

リポジトリルート:

```bash
./env.sh dev status
./env.sh test status
./env.sh dev logs
./env.sh test logs
```

ポート確認:

```bash
lsof -i :5432
lsof -i :5433
```

品質チェック:

```bash
./gradlew check
./gradlew staticAnalysis
```
