# Item completed intent API

## 背景

一覧画面のチェック操作で `completed: !wasCompleted` を送ると、クライアントが古い状態を持っている場合に意図しない逆反転が起きる。

例:

1. 端末Aは `completed=true` の表示を保持している。
2. 端末Bが先に未完了へ戻し、サーバ状態は `completed=false` になる。
3. 端末Aが古い表示を元にチェック解除すると、旧実装では `completed=false` のつもりがトグル値として `true` を送る可能性がある。

この問題を避けるため、一覧操作は現在値トグルではなくユーザー意図別APIを呼び分ける。

## API契約

### markCompleted

`PATCH /api/lists/{listId}/items/{itemId}/mark-completed`

Header:

```http
If-Match: "item-uuid-v7"
```

Request:

```json
{
  "completedByMemberId": "member-uuid"
}
```

Response:

```json
{
  "revision": 12,
  "changed": true,
  "data": {
    "id": "item-uuid",
    "completed": true,
    "completedByMemberId": "member-uuid",
    "completedAt": "2024-01-01T00:00:00Z"
  }
}
```

状態遷移:

| 事前条件 | 現在状態 | 結果 | revision |
| --- | --- | --- | --- |
| `If-Match` 一致 | `completed=false` | `completed=true` に更新し、`completedByMemberId` と `completedAt` を設定。`changed=true` | 進める |
| `If-Match` 一致 | `completed=true` | no-op。既存の `completedByMemberId` と `completedAt` を保持。`changed=false` | 進めない |
| `If-Match` 不一致 | 任意 | `412 precondition_failed`。最新状態を再取得してもらう | 進めない |

### markIncomplete

`PATCH /api/lists/{listId}/items/{itemId}/mark-incomplete`

Header:

```http
If-Match: "item-uuid-v7"
```

Request body: なし

Response:

```json
{
  "revision": 13,
  "changed": true,
  "data": {
    "id": "item-uuid",
    "completed": false,
    "completedByMemberId": null,
    "completedAt": null
  }
}
```

状態遷移:

| 事前条件 | 現在状態 | 結果 | revision |
| --- | --- | --- | --- |
| `If-Match` 一致 | `completed=true` | `completed=false` に更新し、完了者と完了日時をクリア。`changed=true` | 進める |
| `If-Match` 一致 | `completed=false` | no-op。`changed=false` | 進めない |
| `If-Match` 不一致 | 任意 | `412 precondition_failed`。最新状態を再取得してもらう | 進めない |

## エラー方針

| ケース | HTTP | `error` | 備考 |
| --- | --- | --- | --- |
| アイテムまたはリストが存在しない | 404 | `not_found` | 既存のAPIエラー形式に合わせる |
| `completedByMemberId` 未指定 | 400 | `bad_request` | `markCompleted` で `completed=false -> true` に遷移する場合 |
| `completedByMemberId` がリスト外メンバー | 400 | `bad_request` | `markCompleted` で `completed=false -> true` に遷移する場合 |
| `If-Match` 未指定 | 428 | `precondition_required` | stale state 防止のため必須 |
| `If-Match` 不一致 | 412 | `precondition_failed` | 最新状態を再取得して再操作 |
| DB制約違反 | 409 | `conflict` | 既存ハンドラに合わせる |

競合制御の全体方針は [競合制御方針](./concurrency-control-policy.md) に従う。item完了更新も item `version` + `If-Match` による楽観ロックを使い、stale state の場合は `412 precondition_failed` を返す。

## フロント修正方針

一覧のチェック操作は、押下時に見えている状態からユーザー意図を決め、以下を呼び分ける。

| 表示上の状態 | ユーザー操作の意図 | 呼ぶAPI |
| --- | --- | --- |
| 未完了 | 完了にする | `markItemCompleted(listId, itemId, { completedByMemberId })` |
| 完了済み | 未完了に戻す | `markItemIncomplete(listId, itemId)` |

`PATCH /items/{itemId}` の `completed` 更新は既存互換として残すが、一覧のチェック操作では使わない。item完了APIには画面で保持していた item `ETag` を `If-Match` で送る。`412` の場合は snapshot を取り直し、最新状態を表示する。

`changed=false` の場合は、ユーザーの操作で状態が変わったわけではない。フロントは「この操作では変更されず、既に更新済みだった状態を表示した」ことを通知し、自分の操作として誤認されないようにする。

## 競合テスト観点

1. 他ユーザーが先に `true -> false` へ戻した後、古い `true` 表示の端末がチェック解除する。
   - 期待: `markIncomplete` が `412 precondition_failed` になり、削除・未完了化などの操作は適用されない。
2. 他ユーザーが先に `false -> true` へ完了した後、古い `false` 表示の端末がチェックする。
   - 期待: `markCompleted` が `412 precondition_failed` になり、既存の完了者と完了日時を上書きしない。
3. 2端末がほぼ同時に `markCompleted` と `markIncomplete` を送る。
   - 期待: 先に version 一致で適用された操作だけが成功し、後続は `412 precondition_failed` になる。
4. `markCompleted` にリスト外メンバーを指定する。
   - 期待: 400 `bad_request`。
