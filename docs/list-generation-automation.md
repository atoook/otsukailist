# リスト自動生成基盤の方針

このドキュメントは、BBQ などのテンプレートから買い物リストを自動生成するための基盤方針をまとめる。MVP では DB マスタを作らず、カテゴリ・単位・生成ルールはコード定数で管理する。

---

## 基本方針

- 通常の買い物リスト体験は `plain` item として維持する。
- 数量を持つ item は `quantified` item として扱い、数量情報を `item_quantified` に分離する。
- `item.name` は一覧・検索・通常編集・詳細編集すべてで使う**唯一の名称フィールド**とする。
- `item_quantified` は名称を持たず、数量・単位・生成制御のみを担う。
- 数量と単位は `item_quantified.quantity` / `item_quantified.base_unit` に保存し、一覧では item 名とは別の補助ラベル（バッジ）として表示する。
- `generator_key` はカテゴリではなく、自動生成ルール ID として扱う。
- `category` は `generator_key` とは別概念で、plain / quantified 共通の分類として `item.category` に持つ。
- 自動生成アイテムの名称は生成ルールの label を初期値として `item.name` に設定する。
- ユーザーが名称を編集した場合は `item.name` のみ変更し、生成由来の情報は保持する。

---

## 状態モデル

### plain

- `item.item_type = plain`
- `item.name` が真実
- `item_quantified` は持たない
- `category` は任意

---

### manual + none

- `item.item_type = quantified`
- `item_quantified.origin = manual`
- `item_quantified.regeneration_policy = none`
- 自動生成処理の更新対象ではない
- `generator_key` は不要
- `item.name` が名称の真実

---

### generated + auto

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = auto`
- 再生成時の更新対象
- `generator_key` は必須
- `category` は生成ルールから決定する
- `item.name` は生成ルール label を初期値とする

---

### generated + locked

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = locked`
- 自動生成後にユーザーが意図的に変更した状態
- 再生成時の更新対象外
- `generator_key` は保持する
- `item.name` はユーザー編集された名称を保持する

---

## 更新ルール

- `plain -> quantified` は、数量・単位が指定されたときに行う。
- `quantified -> plain` は、数量付き情報を外すときに `item_quantified` を削除する。
- 名称の編集は常に `item.name` のみを更新し、`generated + auto` のまま維持する。
- `item_quantified` に名称フィールドは持たないため、同期処理は不要。
- `quantity` / `base_unit` / `generator_key` / `category` が変更された場合、`generated + auto` item は `generated + locked` に遷移する。
- 再生成で更新できるのは `generated + auto` のみ。
- `manual` と `locked` は再生成では更新しない。

---

## カテゴリ・単位・生成ルール

MVP では以下を DB マスタ化しない。

- 単位定義
- item カテゴリ
- item 準備方法
- BBQ 生成ルール

理由:

- 設定追加・変更箇所をファイル単位で明確にするため
- マスタ移行時に差分を追いやすくするため
- 他機能への影響範囲を小さく保つため

管理対象:

- Backend: `backend/src/main/java/com/atoook/otsukailist/generation`
- Frontend category values/labels/sort order: `frontend/src/types/item-category.ts`
- Frontend preparation type values/labels/sort order: `frontend/src/types/item-preparation-type.ts`
- Frontend unit/generation values: `frontend/src/types/list-generation.ts`

---

## 表示ルール

- 一覧表示では `item.name` を表示する
- 数量・単位は `item_quantified` から取得し、バッジとして表示する

例:

```txt
牛肉          [1kg]
コーラ        [3本]
紙皿
```

- `item.name` に数量・単位は含めない

---

## generator_key の役割

- 自動生成ロジックの識別子
- 再生成時の突合キー
- 表示名の決定には直接使わない
- 表示名は生成ルール定義の label を初期値として `item.name` にコピーする

---

## 生成ルールの拡張パターン

生成ルールは **Strategy を Registry で管理する構成（table-driven）** として扱う。

- `GenerationRule` を共通インターフェースとし、各生成ルールはこの Strategy を実装する。
- `GenerationRules` は `generator_key -> GenerationRule` の対応を一元管理する Registry とする。
- Service 層は BBQ などの具体ルールクラスに依存せず、`GenerationRules` 経由で key 検証・カテゴリ解決を行う。
- BBQ 以外のテンプレートを追加する場合は、具体ルール定義を追加し、Registry に集約する。
- 生成ルールの選択は Map ベースで行い、Service 層にテンプレート別の条件分岐を増やさない。

---

## いまできていること

- `item.item_type` による `plain` / `quantified` の区別
- `item_quantified` による数量付き item 詳細の保存
- `list_generation_config` による将来の生成条件保存先の確保
- `item.category` による plain / quantified 共通カテゴリの保存
- `item.preparationType` による持参物の保存（`null` は購入扱い、`bring` は持参）
- `generated` item に対する `generator_key` 必須バリデーション
- `generated + auto` のカテゴリを生成ルールから決定する処理
- `generated + auto` item 更新時の `locked` 遷移
- `quantified -> plain` への戻し処理
- API レスポンスでの `itemType` / `category` / `preparationType` / `quantified` 返却
- Frontend 型定義での `ItemType` / `BaseUnit` / `ItemCategory` / `ItemPreparationType` / `QuantifiedItem` 分離

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

## 将来の拡張

- `generator_key` 内での細分化が必要になった場合は `generator_item_key` を追加する
- 部位分けや複数明細生成に対応する

例:

```txt
generator_key = beef
generator_item_key = beef_karubi
item.name = 牛肉カルビ
```

---

## 将来のマスタ化候補

- `item_category_master`
- `unit_master`
- `generation_rule_master`

現時点ではテーブルを作らず、コード定数で一元管理する。

---

## 最終方針まとめ

- 名称は `item.name` に一本化する
- `item_quantified` は数量・単位・生成制御のみを持つ
- 表示は「名称 + 数量バッジ」で構成する
- `generator_key` は表示ではなくロジック用途
- ユーザー編集は常に `item.name` に対して行う
- 自動生成後の数量・単位・生成ルール・カテゴリ編集は `locked` として保護する
- 将来の細分化は `generator_item_key` で拡張する
