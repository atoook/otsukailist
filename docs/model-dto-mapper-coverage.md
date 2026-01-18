# Model / DTO / Mapper Coverage

DB項目をどのDTO／マッパーが扱っているかをまとめた一覧。新しいカラムを追加するときや、既存DTOへ露出させたいときの影響範囲確認に使える。

## ItemList (`backend/src/main/java/com/atoook/otsukailist/model/ItemList.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `ItemListResponse.id`, `CreateItemListWithMembersResponse.listId`, `ItemListSnapshotResponse.listId` | `ItemListMapper.toResponse` | `CreateItemListWithMembersResponse`では `listId` 名で露出。 |
| `name` | 書き: `CreateItemListRequest.name`, `CreateItemListWithMembersRequest.name`<br>読み: `ItemListResponse.name`, `CreateItemListWithMembersResponse.name`, `ItemListSnapshotResponse.name` | `ItemListMapper.toResponse`, `toEntity`, `updateEntity` | DTOでは100文字制限、Mapperで `trim()` して保存。 |
| `revision` | 読み: `ItemListResponse.revision`, `CreateItemListWithMembersResponse.revision`, `ItemListSnapshotResponse.revision` | `ItemListMapper.toResponse` | DBが管理。更新系は取り扱わない。 |
| `createdAt` | 読み: `ItemListResponse.createdAt` | `ItemListMapper.toResponse` |  |
| `updatedAt` | 読み: `ItemListResponse.updatedAt` | `ItemListMapper.toResponse` |  |
| `items` | 読み: `ItemListSnapshotResponse.items`（`ItemResponse` のList） | （なし） | 個々のItemは `ItemResponse` / `ItemMapper` で扱う前提。リスト単位でのアイテム展開は Service 層で制御する。 |
| `members` | 読み: `CreateItemListWithMembersResponse.members`, `ItemListSnapshotResponse.members`（いずれも `MemberResponse` のList） | （なし） | リスト本体は Service 層で `MemberMapper` を用いて組み立て。 |

## Item (`backend/src/main/java/com/atoook/otsukailist/model/Item.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `ItemResponse.id` | `ItemMapper.toResponse` |  |
| `name` | 書き: `CreateItemRequest.name`, `UpdateItemRequest.name`<br>読み: `ItemResponse.name` | `ItemMapper.toResponse`, `toEntity`, `updateEntity` | 作成時／更新時ともに Mapper 側で `trim()` して保存。 |
| `completed` | 書き: `CreateItemRequest.completed`, `UpdateItemRequest.completed`<br>読み: `ItemResponse.completed` | `ItemMapper.toResponse`, `toEntity` | 完了状態の業務整合性は Service で担保。 |
| `completedByMemberId` | 書き: `UpdateItemRequest.completedByMemberId`<br>読み: `ItemResponse.completedByMemberId` | `ItemMapper.toResponse` | `toEntity` では未設定。Service 側で制御する想定。 |
| `completedAt` | 読み: `ItemResponse.completedAt` | `ItemMapper.toResponse` |  |
| `createdAt` | 読み: `ItemResponse.createdAt` | `ItemMapper.toResponse` |  |
| `updatedAt` | 読み: `ItemResponse.updatedAt` | `ItemMapper.toResponse` |  |
| `itemList` | （DTOなし） | `ItemMapper.toEntity` | 紐付きリストは Service で解決して `ItemMapper.toEntity` に渡す。 |

## Member (`backend/src/main/java/com/atoook/otsukailist/model/Member.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `MemberResponse.id` | `MemberMapper.toResponse` |  |
| `displayName` | 書き: `CreateMemberRequest.displayName`, `CreateItemListWithMembersRequest.memberNames`<br>読み: `MemberResponse.displayName` | `MemberMapper.toResponse`, `toEntity`, `updateEntity` | Mapperで常に `trim()` 済み。 |
| `createdAt` | 読み: `MemberResponse.createdAt` | `MemberMapper.toResponse` |  |
| `updatedAt` | 読み: `MemberResponse.updatedAt` | `MemberMapper.toResponse` |  |
| `itemList` | （DTOなし） | `MemberMapper.toEntity` | 親リストは Service で取得してセット。 |
