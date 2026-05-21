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
  "data": {
    "id": "item-uuid",
    "completed": true,
    "completedByMemberId": "member-uuid",
    "completedAt": "2024-01-01T00:00:00Z"
  }
}
```

状態遷移:

| 現在状態 | 結果 | revision |
| --- | --- | --- |
| `completed=false` | `completed=true` に更新し、`completedByMemberId` と `completedAt` を設定 | 進める |
| `completed=true` | no-op。既存の `completedByMemberId` と `completedAt` を保持 | 進めない |

### markIncomplete

`PATCH /api/lists/{listId}/items/{itemId}/mark-incomplete`

Request body: なし

Response:

```json
{
  "revision": 13,
  "data": {
    "id": "item-uuid",
    "completed": false,
    "completedByMemberId": null,
    "completedAt": null
  }
}
```

状態遷移:

| 現在状態 | 結果 | revision |
| --- | --- | --- |
| `completed=true` | `completed=false` に更新し、完了者と完了日時をクリア | 進める |
| `completed=false` | no-op | 進めない |

## エラー方針

| ケース | HTTP | `error` | 備考 |
| --- | --- | --- | --- |
| アイテムまたはリストが存在しない | 404 | `not_found` | 既存のAPIエラー形式に合わせる |
| `completedByMemberId` 未指定 | 400 | `bad_request` | `markCompleted` で `completed=false -> true` に遷移する場合 |
| `completedByMemberId` がリスト外メンバー | 400 | `bad_request` | `markCompleted` で `completed=false -> true` に遷移する場合 |
| DB制約違反 | 409 | `conflict` | 既存ハンドラに合わせる |

この対応では、誤反転防止は意図別APIの冪等性で担保する。明示的なリビジョン前提条件を導入する場合は、`If-Match` または request body の `expectedRevision` を使い、不一致時は `412 precondition_failed` として `details.expectedRevision` / `details.currentRevision` を返す方針にする。

実装上は完了状態の判定前に対象アイテム行を更新ロックで取得し、同時に届いた2つ目の意図別リクエストが先行コミット後の状態を見て no-op 判定できるようにする。

## フロント修正方針

一覧のチェック操作は、押下時に見えている状態からユーザー意図を決め、以下を呼び分ける。

| 表示上の状態 | ユーザー操作の意図 | 呼ぶAPI |
| --- | --- | --- |
| 未完了 | 完了にする | `markItemCompleted(listId, itemId, { completedByMemberId })` |
| 完了済み | 未完了に戻す | `markItemIncomplete(listId, itemId)` |

`PATCH /items/{itemId}` の `completed` 更新は既存互換として残すが、一覧のチェック操作では使わない。レスポンスrevisionにギャップがある場合は既存の `useMutation` が snapshot を取り直す。

## 競合テスト観点

1. 他ユーザーが先に `true -> false` へ戻した後、古い `true` 表示の端末がチェック解除する。
   - 期待: `markIncomplete` が no-op になり、`false -> true` へ戻らない。
2. 他ユーザーが先に `false -> true` へ完了した後、古い `false` 表示の端末がチェックする。
   - 期待: `markCompleted` が no-op になり、既存の完了者と完了日時を上書きしない。
3. 2端末がほぼ同時に `markCompleted` と `markIncomplete` を送る。
   - 期待: 最後にコミットされた意図が最終状態になる。どちらのリクエストもトグル値を送らないため、古い状態由来の逆反転は起きない。
4. `markCompleted` にリスト外メンバーを指定する。
   - 期待: 400 `bad_request`。
