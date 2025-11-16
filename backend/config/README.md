# 静的解析設定

このディレクトリには、コード品質を保つための静的解析ツールの設定ファイルが含まれています。

## 📋 含まれるツール

### 1. **Checkstyle** - コーディングスタイルチェック

- **設定ファイル**: `checkstyle/checkstyle.xml`
- **除外設定**: `checkstyle/checkstyle-suppressions.xml`
- **チェック内容**: 命名規約、インデント、インポート順序など

### 2. **PMD** - 静的コード解析

- **設定ファイル**: `pmd/pmd-ruleset.xml`
- **チェック内容**: ベストプラクティス、設計問題、パフォーマンス問題など

### 3. **SpotBugs** - バグ検出

- **設定**: build.gradle 内で設定
- **チェック内容**: 潜在的なバグ、セキュリティ問題など

## 🚀 使用方法

### 個別実行

```bash
# Checkstyle実行
./gradlew checkstyleMain checkstyleTest

# PMD実行
./gradlew pmdMain pmdTest

# SpotBugs実行
./gradlew spotbugsMain spotbugsTest
```

### 一括実行

```bash
# すべての静的解析を実行
./gradlew staticAnalysis

# 軽い静的解析（checkstyle + PMD）のみ
./gradlew check
```

### レポート確認

```bash
# レポートディレクトリを開く
open build/reports/
```

レポートは以下の場所に生成されます：

- Checkstyle: `build/reports/checkstyle/`
- PMD: `build/reports/pmd/`
- SpotBugs: `build/reports/spotbugs/`

## ⚙️ カスタマイズ

### Spring Boot 向けの調整済み設定

- **Lombok アノテーション**: `@Getter`, `@Setter`等を考慮
- **テストクラス**: JUnit テストに対する設定緩和
- **DTO クラス**: Javadoc 要件の緩和
- **設定クラス**: Spring Boot 設定クラスの除外

### 設定の変更

各設定ファイルを編集することで、ルールをカスタマイズできます：

1. **Checkstyle**: `checkstyle/checkstyle.xml`の`<module>`要素を編集
2. **PMD**: `pmd/pmd-ruleset.xml`の`<rule>`要素を編集
3. **SpotBugs**: `build.gradle`の`spotbugs`ブロックを編集

## 🎯 推奨ワークフロー

### 開発時

```bash
# コミット前に軽い静的解析
./gradlew check

# 問題があれば修正後に再実行
./gradlew staticAnalysis
```

### CI/CD

```yaml
# GitHub Actions例
- name: Run Static Analysis
  run: ./gradlew staticAnalysis
```

## 🔧 トラブルシューティング

### よくある問題

1. **Checkstyle 警告が多すぎる**

   - `checkstyle-suppressions.xml`で除外設定を追加

2. **PMD 誤検知**

   - `pmd-ruleset.xml`から該当ルールを除外

3. **SpotBugs 実行時間が長い**
   - `build.gradle`の`effort`を`default`に変更

### 設定の無効化

一時的に静的解析を無効化したい場合：

```bash
# 静的解析をスキップしてビルド
./gradlew build -x check -x staticAnalysis
```

---

**更新日**: 2025-11-16  
**設定バージョン**:

- Checkstyle: 10.12.5
- PMD: 7.0.0
- SpotBugs: 4.8.3
