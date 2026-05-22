# 競合制御方針

このドキュメントは、今後導入する競合制御の設計方針を定義する。現行実装には item / member / list metadata の `version`、APIレスポンスの `ETag`、`If-Match` 検証、`412` / `428` 用のエラーコードと例外ハンドラはまだ存在しないため、実装時に合わせて追加する。

## 目的

複数端末・複数メンバーが同じリストを操作しても、古い画面状態を元にした上書きや削除を防ぐ。

この方針では、リスト全体の同期用 `revision` と、個別リソースの競合制御用 `version` を分けて扱う。

## 用語

| 用語 | 役割 |
| --- | --- |
| `revision` | リスト全体の snapshot 同期用の通し番号 |
| `version` | item / member / list metadata など、対象リソース単体の版 |
| `ETag` | APIが返す対象リソースの版表現 |
| `If-Match` | クライアントが「この版と一致する場合だけ変更して」と伝えるHTTPヘッダ |

`revision` は同期用であり、更新・削除の事前条件には使わない。別itemの追加や完了で `revision` が進むため、対象リソースが変わっていない操作まで過剰に拒否してしまうため。

## 基本方針

既存リソースへの上書き・破壊的操作は、対象リソースの `version` を使った楽観ロックで守る。

APIは対象リソース取得時に `version` 由来の `ETag` を返す。クライアントは更新・削除時に、その時点で見えていた `ETag` を `If-Match` で送る。

```http
DELETE /api/lists/{listId}/items/{itemId}
If-Match: "item-123-v7"
```

サーバはDBの現在 `version` と `If-Match` を比較する。

| 判定 | 結果 |
| --- | --- |
| 一致する | 変更を適用し、対象リソースの `version` とリスト `revision` を進める |
| 一致しない | 変更を適用せず `412 Precondition Failed` を返す |
| `If-Match` が必要なAPIで未指定 | `428 Precondition Required` を返す |

## 操作別方針

| 操作 | 競合制御 |
| --- | --- |
| item更新 | item `version` + `If-Match` |
| item完了 / 未完了 | item `version` + `If-Match` |
| item削除 | item `version` + `If-Match` |
| member名変更 | member `version` + `If-Match` |
| member削除 | member `version` + `If-Match` |
| list名変更 | list metadata `version` + `If-Match` |
| item追加 | `item_list` 行の悲観ロック + item数上限チェック |
| member追加 | `item_list` 行の悲観ロック + member数上限チェック |
| snapshot取得・同期 | 既存 `revision` を継続利用 |

## 悲観ロックを残す範囲

悲観ロックは、対象リソースがまだ存在しない新規追加時の集約制約に限定する。

例: item数上限が100件、現在99件の場合、2人が同時にitem追加すると、どちらも古い件数を見て追加できてしまう可能性がある。追加前のitemには `version` がないため、item単体の楽観ロックでは防げない。

このため、item追加・member追加では親の `item_list` 行を悲観ロックし、count検証と作成を同一トランザクション内で直列化する。

## エラー方針

stale state は `412 Precondition Failed` として扱う。

Response例:

```json
{
  "error": "precondition_failed",
  "message": "このアイテムは他のメンバーにより更新されています",
  "timestamp": "2026-05-23T00:00:00Z",
  "details": {
    "resourceType": "item",
    "resourceId": "item-uuid",
    "expectedVersion": 7,
    "currentVersion": 8
  }
}
```

フロントは `412` を受けたら最新 snapshot を再取得し、ユーザーに「既に他のメンバーが更新しています。最新の状態を表示しました。」という趣旨の短い通知を出す。削除や未完了化など破壊的な操作は、最新状態を表示した後にユーザーが再度明示的に操作した場合だけ実行する。

## item完了更新への適用

完了更新はトグルAPIではなく、ユーザー意図別APIに分ける。

| 表示上の状態 | ユーザー操作 | API |
| --- | --- | --- |
| 未完了 | 完了にする | `markCompleted` |
| 完了済み | 未完了に戻す | `markIncomplete` |

ただし、意図別APIであっても stale state のまま成功扱いにしない。item `version` と `If-Match` が一致した場合のみ変更し、不一致なら `412` を返す。

これにより、「他の人が完了済みだった状態を、自分が完了した」と誤認することを避ける。
