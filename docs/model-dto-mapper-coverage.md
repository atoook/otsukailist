# Model / DTO / Mapper Coverage

DB項目をどのDTO／マッパーが扱っているかをまとめた一覧。新しいカラムを追加するときや、既存DTOへ露出させたいときの影響範囲確認に使える。

## ItemList (`backend/src/main/java/com/atoook/otsukailist/model/ItemList.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `ItemListResponse.id`, `CreateItemListWithMembersResponse.listId`, `ItemListSnapshotResponse.listId` | `ItemListMapper.toResponse` | `CreateItemListWithMembersResponse`では `listId` 名で露出。 |
| `name` | 書き: `CreateItemListRequest.name`, `CreateItemListWithMembersRequest.name`<br>読み: `ItemListResponse.name`, `CreateItemListWithMembersResponse.name`, `ItemListSnapshotResponse.name` | `ItemListMapper.toResponse` | DTOでは100文字制限。作成/更新時の `trim()` は `ListCommandService` が実施。 |
| `revision` | 読み: `ItemListResponse.revision`, `CreateItemListWithMembersResponse.revision`, `ItemListSnapshotResponse.revision` | `ItemListMapper.toResponse` | DBが管理。更新は `ListRevisionService` 経由。 |
| `createdAt` | 読み: `ItemListResponse.createdAt` | `ItemListMapper.toResponse` |  |
| `updatedAt` | 読み: `ItemListResponse.updatedAt` | `ItemListMapper.toResponse` |  |
| `items` | 読み: `ItemListSnapshotResponse.items`（`ItemResponse` のList） | （なし） | 個々のItemは `ItemResponse` / `ItemMapper` で扱う前提。リスト単位でのアイテム展開は Service 層で制御する。 |
| `members` | 読み: `CreateItemListWithMembersResponse.members`, `ItemListSnapshotResponse.members`（いずれも `MemberResponse` のList） | （なし） | リスト本体は Service 層で `MemberMapper` を用いて組み立て。 |

## Item (`backend/src/main/java/com/atoook/otsukailist/model/Item.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `ItemResponse.id` | `ItemMapper.toResponse` |  |
| `name` | 書き: `CreateItemRequest.name`, `UpdateItemRequest.name`<br>読み: `ItemResponse.name` | `ItemMapper.toResponse`, `ItemMapper.updateEntity` | 作成時は `ItemCommandService` が `trim()` と未完了初期化を担当。 |
| `completed` | 書き: `UpdateItemRequest.completed`<br>読み: `ItemResponse.completed` | `ItemMapper.toResponse` | 作成時は常に未完了。完了/未完了切替はサービス層の責務。 |
| `completedByMemberId` | 書き: `UpdateItemRequest.completedByMemberId`<br>読み: `ItemResponse.completedByMemberId` | `ItemMapper.toResponse` | 完了時のメンバー存在チェックは `ItemCommandService` で実施。 |
| `completedAt` | 読み: `ItemResponse.completedAt` | `ItemMapper.toResponse` |  |
| `createdAt` | 読み: `ItemResponse.createdAt` | `ItemMapper.toResponse` |  |
| `updatedAt` | 読み: `ItemResponse.updatedAt` | `ItemMapper.toResponse` |  |
| `itemList` | （DTOなし） | （なし） | 紐付きリストは `ItemCommandService` が確定させて直接設定。 |

## Member (`backend/src/main/java/com/atoook/otsukailist/model/Member.java`)

| Field | DTO (読み/書き) | Mapper | Notes |
| --- | --- | --- | --- |
| `id` | 読み: `MemberResponse.id` | `MemberMapper.toResponse` |  |
| `displayName` | 書き: `CreateMemberRequest.displayName`, `CreateItemListWithMembersRequest.memberNames`<br>読み: `MemberResponse.displayName` | `MemberMapper.toResponse`, `MemberMapper.updateEntity` | 追加・一括作成はサービス層で `trim()` 済み、更新時のみ Mapper を利用。 |
| `createdAt` | 読み: `MemberResponse.createdAt` | `MemberMapper.toResponse` |  |
| `updatedAt` | 読み: `MemberResponse.updatedAt` | `MemberMapper.toResponse` |  |
| `itemList` | （DTOなし） | （なし） | 親リストはサービス層で取得してセット。 |
