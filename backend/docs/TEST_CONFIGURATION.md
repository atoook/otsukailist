# 環境別実行コマンド

## 🚀 実行コマンド一覧（backend/ から実行）

### 開発環境

```bash
# 開発DB起動 & アプリ実行
../env.sh dev up
./gradlew bootRun

# 停止
../env.sh dev down
```

### テスト環境

```bash
# テスト実行（推奨）
./gradlew test              # DB自動起動・停止

# 手動管理
../env.sh test up        # DB起動
./gradlew test --offline    # テスト実行
../env.sh test down         # DB停止
```

### CI 環境（将来用）

```bash
../env.sh ci up
./gradlew check
../env.sh ci down
```

---

## 📋 環境の詳細

### 環境分離

- **開発**: port 5432、データ永続化
- **テスト**: port 5433、tmpfs（高速・一時データ）
- **CI**: 今後実装予定

### 構成ファイル

```
../env.sh                     # 環境管理スクリプト
../db/docker-compose.*.yml    # 環境別DB設定
gradle/test-db.gradle         # テスト自動化
```

### トラブルシューティング

```bash
# 環境状態確認
../env.sh [dev|test] status

# 環境リセット
../env.sh [dev|test] down
../env.sh [dev|test] up

# ポート確認
lsof -i :5432  # 開発
lsof -i :5433  # テスト
```

### よくある操作

```bash
# 開発中のワークフロー
../env.sh dev up && ./gradlew bootRun

# テスト + 静的解析
./gradlew check

# 品質チェック全実行
./gradlew staticAnalysis
```
