# Model / DTO / Mapper Coverage

DB項目をどのDTO／マッパーが扱っているかをまとめた一覧。新しいカラムを追加するときや、既存DTOへ露出させたいときの影響範囲確認に使える。

## ItemList (`backend/src/main/java/com/atoook/otsukailist/model/ItemList.java`)

| Field       | DTO (読み/書き)                                                                                                                                                                         | Mapper                      | Notes                                                                                                      |
| ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `id`        | 読み: `ItemListResponse.id`, `CreateItemListWithMembersResponse.listId`, `ItemListSnapshotResponse.listId`                                                                              | `ItemListMapper.toResponse` | `CreateItemListWithMembersResponse`では `listId` 名で露出。                                                |
| `name`      | 書き: `CreateItemListRequest.name`, `CreateItemListWithMembersRequest.name`<br>読み: `ItemListResponse.name`, `CreateItemListWithMembersResponse.name`, `ItemListSnapshotResponse.name` | `ItemListMapper.toResponse` | DTOでは100文字制限。作成/更新時の `trim()` は `ListCommandService` が実施。                                |
| `revision`  | 読み: `ItemListSnapshotResponse.revision`                                                                                                                                               | （なし）                    | DBが管理。更新は `ListRevisionService` 経由。MutationResponse の `revision` としても返す。                 |
| `version`   | 読み: `ItemListResponse.version`, `CreateItemListWithMembersResponse.version`, `ItemListSnapshotResponse.version`                                                                       | `ItemListMapper.toResponse` | リスト名など list metadata の競合制御用。                                                                 |
| `createdAt` | 読み: `ItemListResponse.createdAt`                                                                                                                                                      | `ItemListMapper.toResponse` |                                                                                                            |
| `updatedAt` | 読み: `ItemListResponse.updatedAt`                                                                                                                                                      | `ItemListMapper.toResponse` |                                                                                                            |
| `items`     | 読み: `ItemListSnapshotResponse.items`（`ItemResponse` のList）                                                                                                                         | （なし）                    | 個々のItemは `ItemResponse` / `ItemMapper` で扱う前提。リスト単位でのアイテム展開は Service 層で制御する。 |
| `members`   | 読み: `CreateItemListWithMembersResponse.members`, `ItemListSnapshotResponse.members`（いずれも `MemberResponse` のList）                                                               | （なし）                    | リスト本体は Service 層で `MemberMapper` を用いて組み立て。                                                |

### Snapshot 専用フィールド（DB カラム外・Service 層で計算）

| フィールド           | DTO                                           | 計算元                                                   | Notes                                                                                                                          |
| -------------------- | --------------------------------------------- | -------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `lastItemActivityAt` | `ItemListSnapshotResponse.lastItemActivityAt` | `itemEntities.stream().map(Item::getUpdatedAt).max(...)` | 全アイテムの `updatedAt` の最大値。アイテム0件の場合は `null`。DB カラムではなく snapshot 取得時に `ListQueryService` で集計。 |

## Item (`backend/src/main/java/com/atoook/otsukailist/model/Item.java`)

| Field                 | DTO (読み/書き)                                                                           | Mapper                                             | Notes                                                            |
| --------------------- | ----------------------------------------------------------------------------------------- | -------------------------------------------------- | ---------------------------------------------------------------- |
| `id`                  | 読み: `ItemResponse.id`                                                                   | `ItemMapper.toResponse`                            |                                                                  |
| `name`                | 書き: `CreateItemRequest.name`, `UpdateItemRequest.name`<br>読み: `ItemResponse.name`     | `ItemMapper.toResponse`                            | 作成・更新時は `ItemCommandService` が `trim()` と未完了初期化を担当。`plain` / `quantified` 共通の唯一の名称フィールド。 |
| `version`             | 読み: `ItemResponse.version`                                                              | `ItemMapper.toResponse`                            | item 更新・削除・完了/未完了 API の `If-Match` に使う。             |
| `itemType`            | 書き: `CreateItemRequest.itemType`, `UpdateItemRequest.itemType`<br>読み: `ItemResponse.itemType` | `ItemMapper.toResponse`                            | `plain` / `quantified` を区別する。未指定時は `plain`。 |
| `category`            | 書き: `CreateItemRequest.category`, `UpdateItemRequest.category`<br>読み: `ItemResponse.category` | `ItemMapper.toResponse`                            | plain / quantified 共通の分類。更新時は `ItemCommandService` が生成ルール補正と `locked` 判定を含めて反映する。`generated + auto` のルール由来カテゴリはユーザー編集扱いにしない。 |
| `preparationType`     | 書き: `CreateItemRequest.preparationType`, `UpdateItemRequest.preparationType`<br>読み: `ItemResponse.preparationType` | `ItemMapper.toResponse`                            | 用途・準備状態の分類。                                               |
| `completed`           | 書き: `UpdateItemRequest.completed`<br>読み: `ItemResponse.completed`                     | `ItemMapper.toResponse`                            | 作成時は常に未完了。完了/未完了切替はサービス層の責務。          |
| `assignedMemberId`    | 書き: `CreateItemRequest.assignedMemberId`, `UpdateItemRequest.assignedMemberId`<br>読み: `ItemResponse.assignedMemberId` | `ItemMapper.toResponse`                            | 担当者。リスト内メンバー存在チェックは Service 層で実施。             |
| `completedByMemberId` | 書き: `UpdateItemRequest.completedByMemberId`<br>読み: `ItemResponse.completedByMemberId` | `ItemMapper.toResponse`                            | 完了時のメンバー存在チェックは `ItemCommandService` で実施。     |
| `completedAt`         | 読み: `ItemResponse.completedAt`                                                          | `ItemMapper.toResponse`                            |                                                                  |
| `createdAt`           | 読み: `ItemResponse.createdAt`                                                            | `ItemMapper.toResponse`                            |                                                                  |
| `updatedAt`           | 読み: `ItemResponse.updatedAt`                                                            | `ItemMapper.toResponse`                            |                                                                  |
| `itemList`            | （DTOなし）                                                                               | （なし）                                           | 紐付きリストは `ItemCommandService` が確定させて直接設定。       |
| `quantified`          | 書き: `CreateItemRequest.quantified`, `UpdateItemRequest.quantified`<br>読み: `ItemResponse.quantified` | `ItemMapper.toQuantifiedEntity`, `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | `ItemQuantified` と 1:1。`plain` の場合は `null`。作成・更新フローの制御は `ItemCommandService` が担当する。 |

## ItemQuantified (`backend/src/main/java/com/atoook/otsukailist/model/ItemQuantified.java`)

| Field                  | DTO (読み/書き)                                                                 | Mapper                                                     | Notes |
| ---------------------- | ------------------------------------------------------------------------------- | ---------------------------------------------------------- | ----- |
| `itemId`               | （DTOなし）                                                                     | （なし）                                                   | `item.id` と同一の PK。`@MapsId` で `Item` と 1:1。 |
| `item`                 | （DTOなし）                                                                     | （なし）                                                   | 親 item。`Item.setQuantified()` で双方向関連を同期する。 |
| `quantity`             | 書き: `QuantifiedItemRequest.quantity`<br>読み: `QuantifiedItemResponse.quantity` | `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | `baseUnit` 基準で保存する。 |
| `baseUnit`             | 書き: `QuantifiedItemRequest.baseUnit`<br>読み: `QuantifiedItemResponse.baseUnit` | `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | `g` / `ml` / `piece` / `pack`。 |
| `origin`               | 書き: `QuantifiedItemRequest.origin`<br>読み: `QuantifiedItemResponse.origin`   | `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | `manual` / `generated`。 |
| `regenerationPolicy`   | 書き: `QuantifiedItemRequest.regenerationPolicy`<br>読み: `QuantifiedItemResponse.regenerationPolicy` | `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | `none` / `auto` / `locked`。再生成対象は `generated + auto`。 |
| `generatorKey`         | 書き: `QuantifiedItemRequest.generatorKey`<br>読み: `QuantifiedItemResponse.generatorKey` | `ItemMapper.updateQuantifiedEntity`, `ItemMapper.toQuantifiedResponse` | 生成ルール ID。`generated` の場合は必須。 |

## ListGenerationConfig (`backend/src/main/java/com/atoook/otsukailist/model/ListGenerationConfig.java`)

| Field        | DTO (読み/書き) | Mapper | Notes |
| ------------ | --------------- | ------ | ----- |
| `id`         | 読み: `ListGenerationConfigResponse.id`         | （なし） | 生成設定レコード ID。 |
| `itemList`   | 読み: `ListGenerationConfigResponse.listId`     | （なし） | 親リスト。 |
| `configType` | 書き: path variable<br>読み: `ListGenerationConfigResponse.configType` | （なし） | `bbq` / `camping` / `hotpot` / `travel`。 |
| `configJson` | 書き: `ListGenerationConfigRequest.configJson`<br>読み: `ListGenerationConfigResponse.configJson` | （なし） | テンプレート生成条件を JSONB で保存する。 |
| `createdAt`  | 読み: `ListGenerationConfigResponse.createdAt`  | （なし） | 監査用。 |
| `updatedAt`  | 読み: `ListGenerationConfigResponse.updatedAt`  | （なし） | 監査用。 |

## Member (`backend/src/main/java/com/atoook/otsukailist/model/Member.java`)

| Field         | DTO (読み/書き)                                                                                                               | Mapper                                                 | Notes                                                                  |
| ------------- | ----------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------ | ---------------------------------------------------------------------- |
| `id`          | 読み: `MemberResponse.id`                                                                                                     | `MemberMapper.toResponse`                              |                                                                        |
| `displayName` | 書き: `CreateMemberRequest.displayName`, `CreateItemListWithMembersRequest.memberNames`<br>読み: `MemberResponse.displayName` | `MemberMapper.toResponse`, `MemberMapper.updateEntity` | 追加・一括作成はサービス層で `trim()` 済み、更新時のみ Mapper を利用。 |
| `version`     | 読み: `MemberResponse.version`                                                                                                | `MemberMapper.toResponse`                              | member 名変更・削除 API の `If-Match` に使う。                         |
| `createdAt`   | 読み: `MemberResponse.createdAt`                                                                                              | `MemberMapper.toResponse`                              |                                                                        |
| `updatedAt`   | 読み: `MemberResponse.updatedAt`                                                                                              | `MemberMapper.toResponse`                              |                                                                        |
| `itemList`    | （DTOなし）                                                                                                                   | （なし）                                               | 親リストはサービス層で取得してセット。                                 |
