# リスト自動生成機能 設計書

このドキュメントは、テンプレートとユーザー回答から買い物リストの初期 item を自動生成する機能の設計をまとめる。対象は MVP の BBQ テンプレートで、現行の frontend / backend / DB 構成に合わせて記述する。

---

## 目的

OtsukaiList の基本体験は「自由入力できる軽い買い物リスト」である。この体験を壊さず、BBQ などのイベント向けに、テンプレートと簡単な入力条件から買い物リストの初期候補を作れるようにする。

自動生成はあくまで補助機能として扱う。生成された item は通常 item と同じように編集でき、ユーザーが編集した生成 item は再生成で上書きしない。

---

## 現行基盤

現時点で、ロジック以外の基盤はおおむね用意済み。

- DB には `item.item_type`、`item_quantified`、`list_generation_config` がある。
- Backend には `ItemType` / `BaseUnit` / `Origin` / `RegenerationPolicy` / `GenerationConfigType` / `ItemCategory` がある。
- Backend には `backend/src/main/java/com/atoook/otsukailist/generation` 配下の生成ルール定数と registry がある。
- Frontend には `frontend/src/types/list-generation.ts` と `frontend/src/lib/listGenerationConstants.ts` がある。
- Item API は `plain` / `quantified` の作成・更新に対応している。
- `generated + auto` item のカテゴリ自動解決と、ユーザー編集時の `locked` 遷移は backend の item 更新処理に入っている。

次に作るべきものは、主に以下。

- リスト画面上の「テンプレートから追加」導線
- BBQ テンプレート専用ページ
- BBQ 回答フォーム UI
- 生成ロジック module
- 生成結果 preview / 保存フロー
- `list_generation_config` の保存・取得 API
- 再生成フロー

---

## 用語

### plain item

数量を持たない通常 item。

```txt
紙皿
マシュマロ
氷
```

現行モデル:

- `item.item_type = plain`
- `item.name` が表示名
- `item_quantified` は持たない
- `item.category` は任意

### quantified item

数量・単位を持つ item。

```txt
牛肉 [1.2kg]
コーラ [3本]
```

現行モデル:

- `item.item_type = quantified`
- `item.name` が表示名
- `item.category` が分類
- `item_quantified.quantity` が基準単位の数量
- `item_quantified.base_unit` が保存単位

### generated item

テンプレート生成で作られた quantified item。

現行モデル:

- `item_quantified.origin = generated`
- `item_quantified.generator_key` は必須

### SuggestionItem

テンプレート生成とは別に、イベントの周辺アイテムをユーザーへ提案するための候補。

```txt
紙皿
ウェットティッシュ
アルミホイル
焼肉のたれ
```

現行モデルへの保存方針:

- 選択された SuggestionItem は `plain item` として追加する
- `item.item_type = plain`
- `item.name` が表示名
- `item.category` は SuggestionItem 定義から付与する
- `item_quantified` は持たない
- `generator_key` / `regeneration_policy` は使わない

SuggestionItem は「生成された item」ではなく「追加候補」である。再生成・自動更新・自動削除の対象にはしない。

### locked item

自動生成後にユーザーが編集したため、再生成から保護する item。

現行モデル:

- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = locked`
- `generator_key` は保持する

---

## 生成 item の状態モデル

### manual + none

ユーザーが手動で作った数量付き item。

- `item.item_type = quantified`
- `item_quantified.origin = manual`
- `item_quantified.regeneration_policy = none`
- `generator_key = null`
- 再生成対象外

### generated + auto

自動生成され、まだユーザー編集で保護されていない item。

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = auto`
- `generator_key` は必須
- 再生成時の更新対象

### generated + locked

自動生成後にユーザーが編集した item。

- `item.item_type = quantified`
- `item_quantified.origin = generated`
- `item_quantified.regeneration_policy = locked`
- `generator_key` は保持
- 再生成対象外

---

## ロック遷移

`generated + auto` item は、以下がユーザー操作で変更された場合に `generated + locked` へ遷移する。

- `quantity`
- `base_unit`
- `generator_key`

`category` は `generator_key` に紐づく属性として扱うため、generated item では `auto` / `locked` に関係なく編集不可とする。カテゴリを変えたい場合は別 item として扱うべきなので、既存 generated item を削除して手動追加または別生成候補で対応する。

現行 backend 実装では、名称変更だけでは locked にしない。`item.name` は通常の自由編集体験に寄せるため、生成 item でも編集しやすくしている。

今後、名称変更も保護対象にしたい場合は `ItemCommandService` の `updateItemName` と lock 判定を見直す。ただし MVP では現行挙動を採用する。

---

## テンプレート生成フロー

### 基本方針

テンプレートはリスト作成時に選ばない。リストは通常どおり作成し、作成後のリスト画面で「テンプレートからアイテムを追加」する。

理由:

- 既存のリスト作成フローを壊さない
- `POST /api/lists` を拡張しなくてよい
- 自動生成を「通常リストへの補助機能」として扱いやすい
- 既存リストにも後からテンプレートを適用できる
- 生成失敗時にリスト作成自体を失敗させずに済む
- 初回生成と再生成の UI を同じ専用ページに置ける

### Step 1: 通常どおりリスト作成

ユーザーは既存の `CreateListPage.vue` でリスト名とメンバーを入力し、空のリストを作成する。

既存 API:

```txt
POST /api/lists
```

`CreateItemListWithMembersRequest` は `name` と `memberNames` のままでよい。テンプレート情報は持たせない。

### Step 2: リスト画面で生成開始

`ItemListPage.vue` にはテンプレート生成の入口だけを置き、押下時に BBQ テンプレート専用ページへ遷移する。

専用ページ:

```txt
/lists/:id/templates/bbq
```

表示方針:

- item が 0 件のときは、空状態の主要アクションとして「テンプレートから追加」を表示する
- item が 1 件以上あるときは、通常の item 追加フォームの近くに控えめな操作として表示する
- 既存 item があっても、テンプレート生成は許可する
- BBQ の回答フォーム、量の微調整、preview は専用ページに置く

UI イメージ:

```txt
空リスト:
[テンプレートから追加] [手入力で追加]

通常リスト:
+ アイテム追加フォーム
[テンプレートから追加]
```

### Step 3: テンプレート選択

MVP で UI と生成ロジックを実装するテンプレートは BBQ のみ。

`GenerationConfigType` には将来候補として以下の値が既にある。

- BBQ
- キャンプ
- 鍋
- 旅行

### Step 4: BBQ 回答フォーム

ユーザーには「設定」ではなく、簡単なアンケートとして見せる。

MVP 入力項目:

- 大人の人数
- 子供の人数
- 食事量: `small` / `normal` / `large`
- バランス: `vegetables` / `balanced` / `meat`
- 海鮮: `none` / `included`
- 飲み物: `small` / `normal` / `large`
- アルコール: `none` / `included`
- アルコール量: `small` / `normal` / `large`
- 主食: `none` / `light` / `solid`

`adjustments` は回答フォームの主操作ではなく任意の補助設定として扱う。保存済み config がある再生成 UI では「量の微調整」をデフォルトで折りたたみ、必要に応じて展開してカテゴリ別に調整する。

UI 表示例:

```txt
Q. 大人は何人ですか？
[ 6 ]

Q. 子供は何人ですか？
[ 2 ]

Q. 食べる量は？
( ) 少なめ
(*) 普通
( ) 多め
```

### Step 5: Preview と追加

回答から生成 candidate を計算し、追加前に preview を表示する。

MVP では preview 上での細かい編集は必須にしない。生成後は通常 item として編集できるため、まずは「追加する item の確認」に留める。

### Step 6: 生成完了

生成 item を保存し、通常のリスト画面に戻る。保存後の item は通常 item と同じ一覧に混ざる。

---

## SuggestionItem による追加候補

### 基本方針

SuggestionItem は、テンプレート生成後に「あると便利だが、数量計算テンプレート本体へ入れると候補が増えすぎるもの」を提案するための仕組みとする。

BBQ の場合、テンプレート本体は食材・飲み物などの数量計算が主目的になる。一方で、紙皿・ウェットティッシュ・アルミホイル・調味料などは、人数から厳密な単位量を計算して再生成するよりも、ユーザーが必要なものを選んで追加する体験の方が自然である。

### Template と SuggestionItem の違い

| 観点 | Template | SuggestionItem |
| --- | --- | --- |
| 主な目的 | 条件から item を自動生成する | 入れ忘れやすい周辺 item を提案する |
| 代表例 | BBQ 食材、飲み物、焼きそば | 紙皿、紙コップ、ウェットティッシュ、アルミホイル |
| item type | `quantified` | `plain` |
| 数量 | 人数・回答・微調整から算出 | 持たない |
| カテゴリ | generation rule から決定 | suggestion 定義から決定 |
| 保存時の key | `generatorKey` を `item_quantified` に保存 | item には key を保存しない |
| 再生成 | あり | なし |
| 自動更新 | `generated + auto` は更新対象 | 対象外 |
| 自動削除 | scope 内で候補から外れた `generated + auto` は削除対象 | 対象外 |
| ユーザー編集後の保護 | `generated + locked` | 通常の plain item と同じ |

Template と SuggestionItem は同じ「おすすめ」導線から見えることはあるが、永続化・再生成・削除の扱いは分ける。

### ユースケース

BBQ テンプレート生成後、ユーザーに以下のような周辺アイテムを提案する。

- 食器: 紙皿、紙コップ、はし
- 衛生・片付け: ウェットティッシュ、キッチンペーパー、ゴミ袋
- 調理器具: アルミホイル、トング、キッチンばさみ、ナイフ、まな板
- 保冷: 保冷バッグ、保冷剤、氷
- 調味料: 食用油、塩胡椒、焼肉のたれ、バター、唐辛子
- 火おこし・燃料: 炭、着火剤、ライター、軍手、火ばさみ

これらは BBQ の食材候補とは別に表示する。テンプレート本体へ含めると、初回 preview が長くなり、食材の数量確認という主目的が薄くなるためである。

### UX 方針

テンプレート適用済みで、かつリストに item が存在する場合は、リスト画面から Suggestion Pack 専用ページへ遷移できる導線を常設する。周辺アイテムはテンプレート生成直後だけでなく、後から思い出して追加したくなることがあるためである。

リスト作成直後の空状態では、BBQ の文脈がまだリスト上に確定していないため、Suggestion Pack の導線は表示しない。まずテンプレートから BBQ の主要 item を追加してもらう。

リスト画面での表示方針:

- `list_generation_config` に BBQ config があり、item が 1 件以上ある場合だけ表示する
- item が 1 件以上ある通常リストでは、テンプレート再生成導線の近くに控えめな操作として置く
- item が 0 件の場合は表示しない

ボタン押下後は、Suggestion Pack 専用ページへ遷移する。モバイル利用を主対象にするため、テンプレート生成画面内へパネル展開せず、選択作業に集中できる画面として分ける。

専用ページ:

```txt
/lists/:id/suggestions/bbq-supplies
```

Suggestion Pack 専用ページでは、正方形タイルを敷き詰めたカタログ形式の選択 UI を表示する。買い物リスト本体とは違う「候補を軽く拾う」体験にするため、リスト行ではなくタイル選択 UI を採用する。

- SuggestionItem を正方形タイルのグリッドとして並べる
- モバイルでは 2 列グリッドを基本にする
- タイル全体をタップ対象にする
- 選択済みタイルは背景色・枠色・チェックアイコンで区別する
- タイル内には item 名、選択状態、追加済みバッジだけを置く
- group はタイル内に入れず、必要に応じてセクション見出しとして表示する
- group は開閉可能にし、会場・機材条件によって不要になりやすい group は初期状態で閉じられるようにする
- 既にリストに存在する item は `追加済み` 表示にし、選択不可にする
- 下部に `選択した N 件を追加` ボタンを固定表示する
- 追加完了後はリスト画面へ戻る

UI イメージ:

```txt
BBQ 周辺アイテム

食器
[ 紙皿 ] [ 紙コップ ] [ はし ]

衛生・片付け
[ ウェットティッシュ ] [ キッチンペーパー ] [ ゴミ袋 ]

調理器具
[ アルミホイル ] [ トング ] [ キッチンばさみ ] [ ナイフ ] [ まな板 ]

保冷
[ 保冷バッグ ] [ 保冷剤 ] [ 氷 ]

調味料
[ 食用油 ] [ 塩胡椒 ] [ 焼肉のたれ ] [ バター ] [ 唐辛子 ]

火おこし・燃料
[ 閉じる/開く ]
[ 炭 ] [ 着火剤 ] [ ライター ] [ 軍手 ] [ 火ばさみ ]

[選択した 4 件を追加]
```

### 重複判定

初期実装では、SuggestionItem は通常の `plain item` として追加するため、追加済み判定は item 名の正規化後の完全一致で行う。

```ts
normalizeForSearch(existingItem.name) === normalizeForSearch(suggestion.name)
```

判定方針:

- スペース・全角半角・大小文字ゆれは吸収する
- 部分一致では判定しない
- 一致した SuggestionItem は `追加済み` として選択不可にする

部分一致を使わない理由:

- `たれ` と `焼肉のたれ`
- `塩` と `塩胡椒`
- `コップ` と `紙コップ`
- `油` と `食用油`

上記のような誤判定が起きやすい。類似 item の警告が必要になった場合は、将来 `似たアイテムあり` のような弱い表示として別途設計する。

同じ item を複数追加したいケースは、SuggestionItem の再追加ではなく、将来作成する item 複製機能で対応する。

### 保存方針

選択された SuggestionItem は通常の item 作成 API で `plain item` として追加する。

```txt
POST /api/lists/{listId}/items
```

Request イメージ:

```json
{
  "name": "紙皿",
  "itemType": "plain",
  "category": "supplies"
}
```

SuggestionItem の追加では、以下を使わない。

- `POST /api/lists/{listId}/generated-items/sync`
- `list_generation_config`
- `item_quantified`
- `origin`
- `regeneration_policy`
- `generator_key`

理由は、SuggestionItem は再生成対象ではなく、ユーザーが選択して通常 item として追加する候補だからである。

### Frontend 定義案

SuggestionItem は frontend の定数として定義する。初期実装では backend で suggestion registry を持たない。

配置案:

```txt
frontend/src/lib/suggestions/
  bbqSuppliesSuggestions.ts
```

型イメージ:

```ts
export type SuggestionGroupDefinition = {
  key: string;
  label: string;
  defaultExpanded?: boolean;
};

export type SuggestionGroupKey = SuggestionGroupDefinition['key'];

export type SuggestionItemDefinition = {
  key: string;
  name: string;
  category: ItemCategory;
  group?: SuggestionGroupKey;
};

export type SuggestionPackDefinition = {
  id: string;
  label: string;
  relatedTemplateId?: string;
  groups?: SuggestionGroupDefinition[];
  items: SuggestionItemDefinition[];
};
```

`key` は UI 上の識別と selection state に使う。初期実装では DB に保存しないが、将来 suggestion の利用状況分析や厳密な重複管理を行う場合に備え、安定した値にしておく。

`group` は表示分類のための任意メタデータであり、SuggestionItem の保存や重複判定には使わない。pack 内で分類表示したい場合だけ `groups` と item 側の `group` を定義する。`defaultExpanded` は表示上の初期開閉だけを制御する。

例:

```ts
export const BBQ_SUPPLIES_SUGGESTION_PACK: SuggestionPackDefinition = {
  id: 'bbq_supplies',
  label: 'BBQ 周辺アイテム',
  relatedTemplateId: 'bbq',
  groups: [
    { key: 'tableware', label: '食器' },
    { key: 'cleanup', label: '衛生・片付け' },
    { key: 'cooking', label: '調理器具' },
    { key: 'cooling', label: '保冷' },
    { key: 'seasonings', label: '調味料' },
    { key: 'fire', label: '火おこし・燃料', defaultExpanded: false }
  ],
  items: [
    { key: 'bbq_supply_paper_plates', name: '紙皿', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_paper_cups', name: '紙コップ', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_chopsticks', name: 'はし', category: 'supplies', group: 'tableware' },
    { key: 'bbq_supply_wet_tissues', name: 'ウェットティッシュ', category: 'daily_goods', group: 'cleanup' },
    { key: 'bbq_supply_kitchen_paper', name: 'キッチンペーパー', category: 'daily_goods', group: 'cleanup' },
    { key: 'bbq_supply_aluminum_foil', name: 'アルミホイル', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_kitchen_scissors', name: 'キッチンばさみ', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_knife', name: 'ナイフ', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_cutting_board', name: 'まな板', category: 'daily_goods', group: 'cooking' },
    { key: 'bbq_supply_cooler_bag', name: '保冷バッグ', category: 'daily_goods', group: 'cooling' },
    { key: 'bbq_supply_ice_pack', name: '保冷剤', category: 'daily_goods', group: 'cooling' },
    { key: 'bbq_supply_ice', name: '氷', category: 'drinks', group: 'cooling' },
    { key: 'bbq_supply_oil', name: '食用油', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_salt_pepper', name: '塩胡椒', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_sauce', name: '焼肉のたれ', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_butter', name: 'バター', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_chili_pepper', name: '唐辛子', category: 'seasonings', group: 'seasonings' },
    { key: 'bbq_supply_charcoal', name: '炭', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_fire_starter', name: '着火剤', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_lighter', name: 'ライター', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_work_gloves', name: '軍手', category: 'daily_goods', group: 'fire' },
    { key: 'bbq_supply_fire_tongs', name: '火ばさみ', category: 'daily_goods', group: 'fire' }
  ]
};
```

### Backend 対応方針

初期実装では、SuggestionItem 専用の backend API は追加しない。

理由:

- 保存形式が通常の `plain item` と同じ
- 重複判定は frontend が現在の list item を見れば判断できる
- 再生成・自動削除の整合性管理が不要
- backend に suggestion 定義を持たせる必要がまだない

複数 item の一括追加を効率化したくなった場合は、将来 `POST /api/lists/{listId}/items/bulk` のような通常 item 用 bulk API を検討する。ただしその場合も SuggestionItem を generated item として扱わない。

---

## 生成条件の保存

生成条件は `list_generation_config` に保存する。

DB:

- `list_generation_config.list_id`
- `list_generation_config.config_type`
- `list_generation_config.config_json`

MVP の `config_type`:

```txt
bbq
```

`config_json`:

```json
{
  "version": 1,
  "answers": {
    "adultCount": 6,
    "childCount": 2,
    "appetite": "normal",
    "balance": "meat",
    "seafoodLevel": "included",
    "drinkLevel": "normal",
    "alcoholLevel": "included",
    "alcoholAmount": "normal",
    "stapleLevel": "light"
  },
  "adjustments": {
    "meat": "normal",
    "seafood": "normal",
    "vegetables": "normal",
    "drinks": "normal",
    "staple": "normal",
    "overall": "normal"
  }
}
```

`version` は将来の config schema 変更に備えて入れる。

### answers

初回生成のベース条件。

### adjustments

生成後のカテゴリ別微調整パラメータ。ユーザーが「肉を多め」「海鮮を少なめ」「飲み物を少なめ」などを選んだ状態として扱う。

`adjustments` は回答そのものを書き換えるものではなく、生成条件に重ねる UI 状態として扱う。

---

## Frontend 生成ロジック

MVP では生成処理を frontend 側で実行する。ただし Vue component に数量計算を直接書かず、純粋関数の service / module に分離する。

配置案:

```txt
frontend/src/lib/generation/
  bbqGenerator.ts
  bbqGenerationRules.ts
  generationTypes.ts
```

既存の `frontend/src/lib/listGenerationConstants.ts` は、BBQ ルールの定数置き場として使える。ロジックが増えた段階で `generation/` 配下へ移す。

### 入力型

```ts
export type BBQGenerationAnswers = {
  adultCount: number;
  childCount: number;
  appetite: 'small' | 'normal' | 'large';
  balance: 'vegetables' | 'balanced' | 'meat';
  drinkLevel: 'small' | 'normal' | 'large';
  alcoholLevel: 'none' | 'included';
  alcoholAmount: 'small' | 'normal' | 'large';
  seafoodLevel: 'none' | 'included';
  stapleLevel: 'none' | 'light' | 'solid';
};

export type BBQGenerationAdjustments = {
  meat: 'small' | 'normal' | 'large';
  seafood: 'small' | 'normal' | 'large';
  vegetables: 'small' | 'normal' | 'large';
  drinks: 'small' | 'normal' | 'large';
  staple: 'small' | 'normal' | 'large';
  overall: 'small' | 'normal' | 'large';
};
```

### 出力型

```ts
export type GeneratedItemCandidate = {
  generatorKey: string;
  name: string;
  category: ItemCategory;
  quantity: number;
  baseUnit: BaseUnit;
  displayUnit: UnitCode;
};
```

`quantity` は必ず `baseUnit` 基準で返す。`displayUnit` は UI 表示用。

### 呼び出しイメージ

```ts
const candidates = generateBBQItems({
  answers,
  adjustments
});
```

### 生成ルール定数

現行 frontend には以下の BBQ ルールがある。

```ts
export const BBQ_GENERATION_RULES = {
  beef: {
    generatorKey: 'beef',
    category: 'meat',
    baseUnit: 'g',
    displayUnit: 'g'
  },
  pork: {
    generatorKey: 'pork',
    category: 'meat',
    baseUnit: 'g',
    displayUnit: 'g'
  },
  chicken: {
    generatorKey: 'chicken',
    category: 'meat',
    baseUnit: 'g',
    displayUnit: 'g'
  },
  sausage: {
    generatorKey: 'sausage',
    category: 'meat',
    baseUnit: 'g',
    displayUnit: 'g'
  },
  vegetableOnion: {
    generatorKey: 'vegetable_onion',
    category: 'vegetables',
    baseUnit: 'piece',
    displayUnit: 'piece'
  },
  vegetableBellPepper: {
    generatorKey: 'vegetable_bell_pepper',
    category: 'vegetables',
    baseUnit: 'piece',
    displayUnit: 'piece'
  },
  vegetableCorn: {
    generatorKey: 'vegetable_corn',
    category: 'vegetables',
    baseUnit: 'piece',
    displayUnit: 'piece'
  },
  vegetablePotato: {
    generatorKey: 'vegetable_potato',
    category: 'vegetables',
    baseUnit: 'piece',
    displayUnit: 'piece'
  },
  vegetableMushrooms: {
    generatorKey: 'vegetable_mushrooms',
    category: 'vegetables',
    baseUnit: 'pack',
    displayUnit: 'pack'
  },
  seafoodShrimp: {
    generatorKey: 'seafood_shrimp',
    category: 'seafood',
    baseUnit: 'pack',
    displayUnit: 'pack'
  },
  seafoodScallop: {
    generatorKey: 'seafood_scallop',
    category: 'seafood',
    baseUnit: 'pack',
    displayUnit: 'pack'
  },
  seafoodSquid: {
    generatorKey: 'seafood_squid',
    category: 'seafood',
    baseUnit: 'pack',
    displayUnit: 'pack'
  },
  softDrinks: {
    generatorKey: 'soft_drinks',
    category: 'drinks',
    baseUnit: 'ml',
    displayUnit: 'l'
  },
  alcohol: {
    generatorKey: 'alcohol',
    category: 'drinks',
    baseUnit: 'piece',
    displayUnit: 'piece'
  },
  yakisoba: {
    generatorKey: 'yakisoba',
    category: 'staple',
    baseUnit: 'piece',
    displayUnit: 'piece'
  }
};
```

MVP の最初の生成対象は、現行定数に合わせて以下から始める。

- `beef`
- `pork`
- `chicken`
- `sausage`
- `vegetable_onion`
- `vegetable_bell_pepper`
- `vegetable_corn`
- `vegetable_potato`
- `vegetable_mushrooms`
- `seafood_shrimp`
- `seafood_scallop`
- `seafood_squid`
- `soft_drinks`
- `alcohol`
- `yakisoba`

生成 candidate には `name` が必要になる。現行の frontend / backend 生成ルールは label を持っていないため、`generateBBQItems` 実装時に frontend の BBQ ルールへ `label` を追加するか、生成 module 内に `generatorKey -> label` の対応を持たせる。MVP では frontend 実行なので、backend の生成ルールには label を追加しなくてもよい。

備品・調味料は、カテゴリと単位の定義があるため後から追加する。

---

## 数量計算方針

MVP では、厳密なレシピ計算ではなく「一般的な目安に近い初期候補」を作る。保存数量は必ず `baseUnit` 基準にする。

基本人数:

```txt
effectivePeople = adultCount + childCount * 0.6
```

子供は大人の 60% として換算する。大人 250g 前後、子供 150g 前後の肉量になるようにするための係数である。

通常設定の基準量:

```txt
牛肉: effectivePeople * 110g
豚肉: effectivePeople * 80g
鶏肉: effectivePeople * 60g
ソーセージ: effectivePeople * 40g
海鮮ありの場合:
  海鮮カテゴリ基準量: effectivePeople * 120g
  480g あたり:
    えび 1パック
    ホタテ 0.75パック
    イカ 0.75パック
  肉類: 0.85倍
野菜カテゴリ基準量: effectivePeople * 150g
  600g あたり:
    玉ねぎ 1個
    ピーマン 2個
    とうもろこし 1本
    じゃがいも 2個
    きのこ 1袋
ソフトドリンク: effectivePeople * 500ml
アルコール(350ml): adultCount * 500ml * alcoholAmount係数 / 350ml を切り上げ
焼きそば:
  なし: 0玉
  軽め: effectivePeople * 0.65玉
  しっかり: effectivePeople * 1玉
```

通常設定の肉量は、牛肉 110g + 豚肉 80g + 鶏肉 60g + ソーセージ 40g とする。肉・ソーセージ込みで 290g 前後になり、一般的な BBQ の大人 1 人前として説明しやすい水準に置く。ソーセージはパック購入を想定し、個数ではなく g で保存する。

海鮮 120g は「海鮮メイン」ではなく追加枠の軽め想定である。生成時は `seafood` という集合 item は作らず、内部のカテゴリ基準量を `えび` / `ホタテ` / `イカ` に展開する。480g を 4人前相当の基準量として、えび 1パック、ホタテ 0.75パック、イカ 0.75パックを係数計算し、買い物しやすいようにパック単位で切り上げる。海鮮ありの場合は海鮮を追加し、肉類を 0.85 倍にして全体が過剰になりすぎないようにする。

野菜 150g は、焼きやすい野菜を複数種類入れたカテゴリ基準量である。生成時は `vegetables` という集合 item は作らず、内部のカテゴリ基準量を `玉ねぎ` / `ピーマン` / `とうもろこし` / `じゃがいも` / `きのこ` に展開する。600g を 4人前相当の基準量として、玉ねぎ 1個、ピーマン 2個、とうもろこし 1本、じゃがいも 2個、きのこ 1袋を係数計算し、個数・袋単位で切り上げる。

このように、カテゴリ基準量を直接 item 化せず、必要なカテゴリだけ品目レベルへ展開する。MVP では野菜と海鮮を展開対象にするが、将来的には `beef` を `牛カルビ` / `牛タン` などへ細分化する場合にも同じ breakdown rule を使う。

食事量係数:

```txt
small: 0.8
normal: 1.0
large: 1.2
```

調整係数:

```txt
small: 0.8
normal: 1.0
large: 1.25
```

例:

```txt
牛肉 quantity(g)
= effectivePeople
  * 肉の1人あたり基準量
  * appetite係数
  * balance係数
  * adjustments.meat係数
  * adjustments.overall係数
```

海鮮・野菜・飲み物・主食も同様に、それぞれ `adjustments.seafood` / `adjustments.vegetables` / `adjustments.drinks` / `adjustments.staple` を持つ。`adjustments.overall` は全カテゴリにかかる全体補正として扱う。

端数処理:

- `g` は 50g または 100g 単位に丸める
- `piece` / `pack` は整数に切り上げる
- `ml` は 100ml または 500ml 単位に丸める

---

## 初回生成保存フロー

初回生成は、既に存在する list に対して実行する。

### Step 1: 生成開始

リスト画面で「テンプレートから追加」を押し、BBQ テンプレート専用ページへ遷移する。

### Step 2: BBQ 回答と preview

回答フォームから `answers` と初期 `adjustments` を作り、frontend の `generateBBQItems` で candidate を計算する。

### Step 3: 生成 config 保存

追加 API 案:

```txt
PUT /api/lists/{listId}/generation-configs/bbq
GET /api/lists/{listId}/generation-configs/bbq
```

`PUT` は upsert とする。`list_generation_config` には `(list_id, config_type)` の unique index があるため、同一 list / type では 1 件だけ保持する。

### Step 4: item 同期

生成 candidate と、そのテンプレートが管理する `generatorKeysInScope` を generated item sync API の payload に変換する。

同期 API:

```txt
POST /api/lists/{listId}/generated-items/sync
```

この API は生成 candidate の同期専用とする。

責務:

- 既存の `generated + auto` item があれば lock せずに数量・単位・カテゴリを更新する
- 既存の `generated + auto` item の `name` は上書きしない
- 一致する `generated + auto` item がなければ新規 item として追加する
- `generatorKeysInScope` に含まれるが今回 candidate に存在しない `generated + auto` item は削除する
- `plain` / `manual + none` / `generated + locked` は更新しない

通常の item 更新 API はユーザー編集として `locked` 遷移を行うため、再生成更新には使わない。

```json
{
  "generatorKeysInScope": ["beef", "pork", "yakisoba"],
  "items": [
    {
      "name": "牛肉",
      "itemType": "quantified",
      "category": "meat",
      "quantified": {
        "quantity": 1200,
        "baseUnit": "g",
        "origin": "generated",
        "regenerationPolicy": "auto",
        "generatorKey": "beef"
      }
    }
  ]
}
```

Backend では `generated` item の場合、`auto` / `locked` に関係なく `generatorKey` から category を解決する。Frontend からも category を送るが、生成ルールと矛盾する場合は backend の生成ルールが優先される。

### Step 5: 既存 item がある場合

初回生成時に既存 item があっても、自動生成は既存 item を変更しない。生成 candidate は新規 item として追加する。

ただし、同じ list に同じ BBQ config が既にある場合は、初回生成ではなく再生成フローとして扱う。

---

## 再生成フロー

再生成は「生成対象 item の全置換」ではなく、既存 item との突合更新として扱う。

再生成 UI は、対象 list に `list_generation_config` が存在する場合に表示する。BBQ config が保存されている list では「条件を調整して再生成」を出せる。

### Step 1: adjustments 更新

ユーザーが必要に応じて「量の微調整」を展開し、カテゴリ別の `adjustments` を変更する。未展開のままなら既存または初期値の `normal` を使う。

### Step 2: config 保存

`list_generation_config.config_json` を更新する。

### Step 3: 新しい生成結果を計算

Frontend の `generateBBQItems` を再実行する。

### Step 4: `generatorKey` で突合

既存 item のうち、以下だけを更新対象にする。

- `item.itemType = quantified`
- `item.quantified.origin = generated`
- `item.quantified.regenerationPolicy = auto`
- `item.quantified.generatorKey` が candidate と一致

更新対象外:

- `plain`
- `manual + none`
- `generated + locked`
- `generatorKey` が一致しない item

### Step 5: 不足 candidate を追加

新しい生成結果に含まれるが既存 item にない `generatorKey` は、generated item sync API が新規 item として追加する。

### Step 6: 消えた candidate の扱い

前回生成に存在して今回生成に存在しない item は、`generatorKeysInScope` に含まれる `generated + auto` のみ削除する。

例:

- 海鮮ありから海鮮なしに変更した場合、`seafood_shrimp` / `seafood_scallop` / `seafood_squid` は削除候補になる。
- アルコールありからなしに変更した場合、`alcohol` は削除候補になる。
- 主食ありからなしに変更した場合、`yakisoba` は削除候補になる。

ただし、削除対象は `generated + auto` に限定する。`generated + locked` は candidate から外れても削除せず、preview では「変更対象外」として表示する。

---

## 表示ルール

一覧では `item.name` と数量バッジを分けて表示する。

```txt
牛肉           [1.2kg]
焼きそば       [4個]
紙皿
```

重要:

- `item.name` に数量・単位を含めない
- 数量は `item.quantified.quantity` と `baseUnit` から表示変換する
- 表示用単位は frontend の数量表示 helper で `category + unit` から解決する
- `g` 保存 item は、1000g 未満は `g`、1000g 以上は `kg` で表示する
- `pack` の通常表示は `袋` とし、カテゴリが `meat` / `seafood` の `pack` は買い物単位として `パック` と表示する

---

## 編集方針

### plain item

通常テキスト編集を維持する。

### quantified item

詳細編集 UI で以下を編集できるようにする。

- `item.name`
- `quantity`
- `baseUnit`
- `category`
- `preparationType`

ただし generated item の `category` は `generatorKey` 由来で固定し、詳細編集 UI でも編集不可にする。

MVP では通常リスト体験を優先し、一覧上のクイック操作は複雑にしない。

---

## Backend 責務

Backend は生成エンジンではなく、永続化と整合性維持を担う。

責務:

- list / item / member の永続化
- revision 管理
- `list_generation_config` の保存・取得
- `generated` item の generator key バリデーション
- `generated` item の category 解決
- ユーザー編集時の `locked` 遷移
- 共有リストとしての整合性維持

MVP では数量計算は frontend で行うため、backend に BBQ の人数計算ロジックは置かない。

---

## Frontend 責務

Frontend は生成体験と preview を担う。

責務:

- リスト画面上のテンプレート追加導線
- テンプレート選択 UI
- テンプレート専用ページ
- Suggestion Pack 専用ページ
- BBQ 回答フォーム UI
- 生成 candidate の計算
- preview 表示
- 生成 config 保存 API 呼び出し
- item 作成 API 呼び出し
- 再生成時の突合更新
- 数量の表示単位変換
- SuggestionItem の選択 UI と追加済み判定
- 選択された SuggestionItem の plain item 追加

---

## 将来の Workers / Pages Functions 化

以下の状態になったら、生成ロジックを Cloudflare Workers / Pages Functions へ切り出す。

- テンプレート種類が増える
- 生成ルールが複雑化する
- frontend bundle が肥大化する
- 複数クライアントで生成ロジックを共有したくなる
- frontend deploy なしで生成ルールを改善したくなる

Workers 化した場合の責務:

- テンプレート生成
- 数量計算
- カテゴリ決定
- candidate item 作成

Frontend は入力 UI / preview / 保存 API 呼び出しに集中する。

将来 API イメージ:

```txt
POST /api/templates/bbq/preview
```

Request:

```json
{
  "answers": {
    "adultCount": 6,
    "childCount": 2,
    "appetite": "normal",
    "balance": "meat",
    "seafoodLevel": "included",
    "drinkLevel": "normal",
    "alcoholLevel": "included",
    "alcoholAmount": "normal",
    "stapleLevel": "light"
  },
  "adjustments": {
    "meat": "normal",
    "seafood": "normal",
    "vegetables": "normal",
    "drinks": "normal",
    "staple": "normal",
    "overall": "normal"
  }
}
```

Response:

```json
{
  "items": [
    {
      "generatorKey": "beef",
      "name": "牛肉",
      "quantity": 1200,
      "baseUnit": "g",
      "displayUnit": "kg",
      "category": "meat"
    }
  ]
}
```

---

## コード定数管理

MVP では以下を DB マスタ化しない。

- item category
- unit
- generation rule

理由:

- MVP の実装速度を優先する
- ファイル単位で差分管理しやすい
- 将来 master table へ移行しやすい

現行の主な管理場所:

- Frontend unit / origin / regeneration policy: `frontend/src/types/list-generation.ts`
- Frontend category: `frontend/src/types/item-category.ts`
- Frontend BBQ generation rules: `frontend/src/lib/listGenerationConstants.ts`
- Backend generation rules: `backend/src/main/java/com/atoook/otsukailist/generation`
- Backend model enum: `backend/src/main/java/com/atoook/otsukailist/model`

Frontend と Backend の generation rule は、MVP では二重管理になる。実装時は `generatorKey` / `category` / `baseUnit` の不一致が起きないよう、テストで最低限検出する。

---

## 新しいテンプレートを追加する場合のガイド

BBQ は最初のテンプレート実装であり、今後のテンプレート追加時も「テンプレート固有の入力・生成ロジック」と「共通の保存・同期基盤」を分ける。

### 追加時の基本方針

- `generated item` の永続化は既存の `POST /api/lists/{listId}/generated-items/sync` を使う。
- `list_generation_config` の保存・取得は既存の `/generation-configs/{configType}` を使う。
- 新テンプレート固有の数量計算は Vue component に書かず、`frontend/src/lib/generation` 配下の pure function に閉じ込める。
- リスト画面の導線は `frontend/src/lib/listTemplateRegistry.ts` にテンプレート定義を追加して広げる。
- `generatorKey` / `category` / `baseUnit` は frontend と backend の両方に登録する。
- 生成 candidate は必ず `name` / `generatorKey` / `category` / `quantity` / `baseUnit` / `displayUnit` を持つ。
- 再生成対象は `generated + auto` のみで、`generated + locked` は更新・削除しない。

### Backend で追加するもの

1. `GenerationConfigType` に新しい `configType` を追加する。
2. `backend/src/main/java/com/atoook/otsukailist/generation` 配下に新テンプレート用の generation rule 定数を追加する。
3. `GenerationRules` に新テンプレートの rule を統合する。
4. `generatorKey` は全テンプレート横断で一意にする。
5. `category` と `baseUnit` は frontend の定義と一致させる。
6. 必要に応じて `ItemCategory` / `BaseUnit` を追加する。ただし DB マスタ化は MVP では行わない。
7. `GeneratedItemCommandServiceTest` に、新テンプレートの代表 `generatorKey` が sync できること、未知 key / scope 外 key が拒否されることを追加する。

Backend はテンプレート固有の回答内容を解釈しない。回答 JSON は `list_generation_config.config_json` に保存し、生成 candidate の整合性は `generatorKey` と generation rule で検証する。

### Frontend で追加するもの

1. `frontend/src/lib/generation/{template}Generator.ts` を追加する。
2. `createDefault{Template}GenerationConfig` / `generate{Template}Items` / `is{Template}GenerationConfig` を用意する。
3. `frontend/src/lib/listGenerationConstants.ts` に新テンプレートの generation rule を追加する。
4. `GENERATION_RULES` に新テンプレートの rule を統合する。
5. `frontend/src/lib/listTemplateRegistry.ts` にテンプレート定義を追加する。
6. 専用ページ `frontend/src/pages/{Template}GenerationPage.vue` を追加する。
7. 専用パネル `frontend/src/components/{Template}GenerationPanel.vue` を追加する。
8. `frontend/src/router/index.ts` に専用ページの route を追加する。
9. `syncGeneratedItems` に渡す `generatorKeysInScope` は、そのテンプレートが管理する全 `generatorKey` を渡す。
10. preview では `new` / `update` / `delete` / `locked` を区別する。

テンプレート専用 UI は、`BBQGenerationPanel` のようにテンプレート名を含めた component 名にする。`ListGenerationPanel` のような汎用名にすると、テンプレート固有ロジックが共通 UI に見えやすくなるため避ける。

### 生成ロジック追加時の注意点

- 人数や回答値から算出する基準量は、コメントまたは設計書に根拠を残す。
- `quantity` は保存用の `baseUnit` 基準にする。
- 表示変換は `formatQuantity` / `resolveUnitLabel` を使い、component 内で独自ラベルを持たない。
- 集合カテゴリをそのまま item 化すると買い物チェックリストとして弱い場合は、breakdown rule で品目へ展開する。
- 「なし」にした回答で candidate から外れる item は、sync preview で `delete` として表示し、保存時に `generated + auto` だけ削除する。
- 既存 `generated + locked` item は、candidate から外れても削除対象にしない。

### テスト追加の目安

Frontend:

- default config が valid と判定される
- 代表入力から期待 candidate が出る
- optional 回答を `none` にすると candidate が出ない
- adjustments が対象カテゴリにだけ効く
- `baseUnit` と `displayUnit` が期待どおりになる
- breakdown 対象カテゴリが品目単位に展開される

Backend:

- 新 `generatorKey` の `category` / `baseUnit` が受け入れられる
- scope 内の `generated + auto` は更新される
- scope 内で candidate から外れた `generated + auto` は削除される
- `generated + locked` は更新・削除されない
- scope 外 key / unknown key / baseUnit 不一致は reject される

### 追加前チェックリスト

- `configType` が route / registry / API 呼び出し / backend enum で一致している
- `generatorKey` が全体で一意
- frontend/backend の `category` と `baseUnit` が一致
- `generatorKeysInScope` にテンプレート管理下の全 key が含まれている
- 保存済み config がある場合、ボタン文言が「生成条件を調整」になる
- `generated + locked` の preview が「変更対象外」として見える
- 新テンプレートの pure function に unit test がある

---

## 新しい Suggestion Pack を追加する場合のガイド

Suggestion Pack は、テンプレート本体に含めるほどではない周辺 item の提案として追加する。数量計算・再生成・自動削除が必要なものは Suggestion Pack ではなく Template として扱う。

### 追加時の基本方針

- SuggestionItem は `plain item` として追加する。
- SuggestionItem には `category` を持たせる。
- `item_quantified` は作らない。
- `generatorKey` / `regenerationPolicy` / `list_generation_config` は使わない。
- 追加済み判定は `normalizeForSearch(name)` の完全一致で行う。
- 既に追加済みの item は選択不可にする。
- 同じ item を複数追加したい場合は、SuggestionItem ではなく item 複製機能で対応する。

### Frontend で追加するもの

1. `frontend/src/lib/suggestions/{template}Suggestions.ts` を追加する。
2. `SuggestionPackDefinition` に `id` / `label` / `relatedTemplateId` / `groups` / `items` を定義する。
3. item の `key` は UI state 用の安定 ID として、pack 内で一意にする。
4. item の `name` と `category` を定義する。
5. テンプレート適用済みで item が 1 件以上あるリスト画面に `周辺アイテム` の導線を表示する。
6. 専用ページ `frontend/src/pages/{SuggestionPack}SuggestionPage.vue` を追加する。
7. 専用ページの route を `frontend/src/router/index.ts` に追加する。
8. 正方形タイルの 2 列グリッドを基本にした選択 UI を用意する。
9. 追加済み判定済みの item は `追加済み` と表示し、選択対象から外す。
10. 選択された item を通常の item 作成 API で `plain` として追加する。
11. 追加完了後はリスト画面へ戻る。

### Backend で追加するもの

初期実装ではなし。

通常 item 作成 API で保存できるため、Suggestion Pack 専用 API や registry は不要とする。将来、suggestion 定義をサーバー管理したい、A/B テストしたい、利用状況を分析したい、複数 item を atomic に一括追加したい、といった要件が出た場合に backend 拡張を検討する。

### テスト追加の目安

Frontend:

- suggestion 定義の `key` が pack 内で重複していない
- suggestion 定義の `name` が空ではない
- suggestion 定義の `category` が既存カテゴリに含まれている
- 既存 item と正規化後に完全一致する suggestion が `追加済み` になる
- 部分一致だけでは `追加済み` にならない
- 選択した suggestion が `plain item` 作成 request に変換される

---

## テスト方針

### Frontend

生成ロジックは pure function として unit test を書く。

確認対象:

- 人数・食事量から期待数量が出る
- `adjustments` が数量に反映される
- `baseUnit` 基準で quantity が返る
- `displayUnit` が表示用に保持される
- `stapleLevel = none` で主食 candidate が出ない

### Backend

追加する `list_generation_config` API の service / controller test を書く。

確認対象:

- config を保存できる
- 同じ list / config type で upsert される
- list が存在しない場合は 404
- 他 list の config を誤って取得しない

既存 item 更新テストでは以下を維持する。

- `generated + auto` の数量・単位変更で `locked` になる
- `manual + none` は再生成対象にならない
- 未知の `generatorKey` は reject される

---

## 実装順序

1. Frontend に BBQ answers / adjustments / candidate の型を追加する。
2. `generateBBQItems` を pure function として実装する。
3. 生成ロジックの unit test を追加する。
4. `ItemListPage.vue` に「テンプレートから追加」導線を追加する。
5. BBQ テンプレート専用ページに回答フォームと preview UI を追加する。
6. generated item sync API を使って生成候補を保存する。
7. Backend に `list_generation_config` 保存・取得 API を追加する。
8. 保存済み config がある list 向けに再生成 UI と突合更新処理を追加する。
9. 必要に応じて備品・調味料の generation rule を増やす。

---

## 将来拡張

### generator_item_key

将来的に 1 つの `generator_key` をさらに細分化したくなった場合は `generator_item_key` を追加する。

```txt
generator_key = beef
generator_item_key = beef_karubi
item.name = 牛カルビ
```

MVP では追加しない。

### マスタ化候補

- `item_category_master`
- `unit_master`
- `generation_rule_master`

現時点ではテーブルを作らず、コード定数で一元管理する。

---

## 最終方針

- 自動生成は「初期候補生成」として扱う。
- 通常の自由入力リスト体験を壊さない。
- 名称は `item.name` に一本化する。
- `item_quantified` は数量・単位・生成制御のみ持つ。
- `generated + auto` のみ再生成対象にする。
- ユーザー編集された生成 item は `locked` として保護する。
- `category` は item の分類、`generator_key` は生成ロジック識別子として分ける。
- 数量は必ず `baseUnit` 基準で保存する。
- MVP では生成ロジックを frontend の pure function として実装する。
- Vue component に数量計算を書かず、将来 API 化しやすい module 境界にする。
