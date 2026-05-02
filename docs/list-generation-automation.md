# リスト自動生成基盤の方針

このドキュメントは、BBQ などのテンプレートから買い物リストを自動生成するための基盤方針をまとめる。MVP では DB マスタを作らず、カテゴリ・単位・生成ルールはコード定数で管理する。

---

## 基本方針

- 通常の買い物リスト体験は `plain` item として維持する。
- 数量を持つ item は `quantified` item として扱い、数量情報を `item_quantified` に分離する。
- `item.name` は一覧・検索・通常編集で使う表示名とする。
- `item_quantified.name` は数量付き item の構造化された品目名として残す。
- `quantified` の場合、`item.name` と `item_quantified.name` は backend で同期する。
- 数量と単位は `item_quantified.quantity` / `item_quantified.base_unit` に保存し、一覧では item 名とは別の補助ラベルとして表示する。
- `generator_key` はカテゴリではなく、自動生成ルール ID として扱う。
- `category` は `generator_key` とは別概念で、plain / quantified 共通の分類として `item.category` に持つ。

---

## 状態モデル

### plain

- `item.item_type = plain`
- `item.name` が真実
- `item_quantified` は持たない
- `category` は任意

### manual + none

- `item.item_type = quantified`
- `item_quantified.origin = manual`
- `item_quantified.regeneration_policy = none`
- 自動生成処理の更新対象ではない
- `generator_key` は不要

### generated + auto

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = auto`
- 再生成時の更新対象
- `generator_key` は必須
- `category` は生成ルールから決定する

### generated + locked

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = locked`
- 自動生成後にユーザーが意図的に変更した状態
- 再生成時の更新対象外
- `generator_key` は保持する

---

## 更新ルール

- `plain -> quantified` は、数量・単位が指定されたときに行う。
- `quantified -> plain` は、数量付き情報を外すときに `item_quantified` を削除する。
- `quantified` の詳細更新では、`item.name = item_quantified.name` に同期する。
- `quantified` の name だけを通常編集した場合も、`item_quantified.name` に同期する。
- `quantity` / `base_unit` / `item_quantified.name` / `generator_key` が変わった場合、`generated` item は `locked` に遷移する。
- 再生成で更新できるのは `generated + auto` のみ。
- `manual` と `locked` は再生成では更新しない。

---

## カテゴリ・単位・生成ルール

MVP では以下を DB マスタ化しない。

- 単位定義
- item カテゴリ
- BBQ 生成ルール

理由:

- 設定追加・変更箇所をファイル単位で明確にするため
- マスタ移行時に差分を追いやすくするため
- 他機能への影響範囲を小さく保つため

管理対象:

- Backend: `backend/src/main/java/com/atoook/otsukailist/generation`
- Frontend: `frontend/src/lib/itemCategoryConstants.ts`
- Frontend: `frontend/src/lib/listGenerationConstants.ts`

---

## いまできていること

- `item.item_type` による `plain` / `quantified` の区別
- `item_quantified` による数量付き item 詳細の保存
- `list_generation_config` による将来の生成条件保存先の確保
- `item.category` による plain / quantified 共通カテゴリの保存
- `generated` item に対する `generator_key` 必須バリデーション
- `generated + auto` のカテゴリを生成ルールから決定する処理
- 数量付き item 更新時の `locked` 遷移
- `quantified -> plain` への戻し処理
- API レスポンスでの `itemType` / `category` / `quantified` 返却
- Frontend 型定義での `ItemType` / `BaseUnit` / `ItemCategory` / `QuantifiedItem` 分離

---

## これからすること

- テンプレート選択 UI を追加する
- BBQ 生成条件入力 UI を追加する
- `list_generation_config.config_json` に生成条件を保存する
- 生成条件から item 候補を作成する service を追加する
- `generated + auto` の item だけを再生成で更新する
- `generated + locked` / `manual + none` を再生成対象外として保護する
- Frontend の実 UI からデバッグ用表示を除き、数量・単位・カテゴリの入力体験を整える
- 単位表示を必要に応じて `base_unit` から表示用単位へ変換する
- DB マスタ化が必要になった段階で、コード定数から master table へ移行する

---

## 将来のマスタ化候補

- `item_category_master`
- `unit_master`
- `generation_rule_master`

現時点ではテーブルを作らず、コード定数で一元管理する。
