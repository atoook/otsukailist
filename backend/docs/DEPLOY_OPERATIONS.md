# Backend Deploy Operations

このドキュメントは、バックエンドの Docker 検証と Render デプロイ時に必要な運用情報を集約したものです。

## 1. ローカル Docker 検証

前提: `backend/` で実行。

```bash
docker build -t otsukailist-backend:local .
```

```bash
docker run --rm \
  -e PORT=10000 \
  -e POSTGRES_HOST=host.docker.internal \
  -e POSTGRES_PORT=5432 \
  -e POSTGRES_DB=otsukailist \
  -e POSTGRES_USER=otsukailist_user \
  -e POSTGRES_PASSWORD=otsukailist_password \
  -e POSTGRES_SSL_MODE=disable \
  -e APP_CORS_ALLOWED_ORIGINS=http://localhost:5173 \
  -e APP_RATE_LIMIT_ENABLED=true \
  -p 10000:10000 \
  otsukailist-backend:local
```

起動確認:

```bash
curl -i http://localhost:10000/actuator/health/liveness
```

期待値:

- HTTP 200 が返る
- `{"status":"UP"}` を含む

## 2. Render デプロイ時の必須設定

### サービス設定

- Service Type: Web Service (Docker)
- Repository: `atoook/otsukailist`
- Branch: `main`
- Root Directory: `backend`
- Dockerfile: `backend/Dockerfile`
- Health Check Path: `/actuator/health/liveness`

### 環境変数

必須:

- `SPRING_PROFILES_ACTIVE=prod`
- `POSTGRES_HOST`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS`

任意 (必要時):

- `POSTGRES_PORT` (default: `5432`)
- `POSTGRES_SSL_MODE` (default: `require`)
- `POSTGRES_PARAMS`
- `DB_POOL_MINIMUM_IDLE` (default: `0`)
- `DB_POOL_IDLE_TIMEOUT_MS` (default: `60000`)
- `APP_ITEM_MAX_ITEMS_PER_LIST` (default: `100`)
- `APP_MEMBER_MAX_MEMBERS_PER_LIST` (default: `20`)
- `APP_RATE_LIMIT_ENABLED` (default: `true`)
- `APP_RATE_LIMIT_CAPACITY` (default: `120`)
- `APP_RATE_LIMIT_REFILL_TOKENS` (default: `120`)
- `APP_RATE_LIMIT_REFILL_PERIOD` (default: `PT1M`)
- `APP_RATE_LIMIT_MUTATION_CAPACITY` (default: `30`)
- `APP_RATE_LIMIT_MUTATION_REFILL_TOKENS` (default: `30`)
- `APP_RATE_LIMIT_MUTATION_REFILL_PERIOD` (default: `PT1M`)
- `APP_RATE_LIMIT_CACHE_TTL` (default: `PT15M`)
- `APP_RATE_LIMIT_MAX_CLIENTS` (default: `10000`)
- `APP_RATE_LIMIT_TRUSTED_PROXY_COUNT` (default: `0`)
- `APP_RATE_LIMIT_TRUSTED_PROXY_CIDRS` (default: empty; comma-separated trusted proxy IPs/CIDRs)

補足:

- `PORT` は Render が注入するため、通常は手動設定不要
- アプリ側は `server.port=${PORT:8080}` 前提
- Neon を inactive に戻せるよう、本番では DB pool の idle connection を保持しない設定
- レートリミットは読み取り系 API がIP単位で 120 req/min、更新系 API (`POST`/`PUT`/`PATCH`/`DELETE`) がIP単位で 30 req/min
- `X-Forwarded-For` は `APP_RATE_LIMIT_TRUSTED_PROXY_COUNT > 0` かつ `APP_RATE_LIMIT_TRUSTED_PROXY_CIDRS` に直前proxyのIPが含まれる場合のみ採用される
- レートリミットはメモリ内でIP単位に管理されるため、複数インスタンス化した場合は実効上限がインスタンス数倍になる

### レートリミットで使うクライアントIPについて

アプリが直接受け取る接続元IP (`remoteAddr`) は、Render などの reverse proxy 配下では利用者本人のIPではなく、直前の proxy のIPになることがあります。実クライアントIPは `X-Forwarded-For` に入る場合がありますが、このヘッダーはクライアントが偽装できるため、そのまま信頼すると攻撃者が任意のIPを名乗ってレートリミットを回避できます。

このため、デフォルトでは `APP_RATE_LIMIT_TRUSTED_PROXY_COUNT=0` として `X-Forwarded-For` を無視し、`remoteAddr` を使います。これは安全側の挙動です。ただし、proxy 配下では複数ユーザーが同じ proxy IP として扱われ、レートリミット枠を共有する可能性があります。

本番で実クライアントIP単位にしたい場合は、直前の proxy のIPまたはCIDRが運用基盤から確認できる場合に限り、`APP_RATE_LIMIT_TRUSTED_PROXY_CIDRS` にそのIP/CIDRを設定し、通常は `APP_RATE_LIMIT_TRUSTED_PROXY_COUNT=1` を設定します。確認できない場合は未設定のままにしてください。`0.0.0.0/0` や広すぎるCIDRを設定すると、誰からの `X-Forwarded-For` でも信頼する状態に近くなるため避けます。

## 3. デプロイ後のスモークチェック

```bash
curl -i https://<your-render-service>/actuator/health/liveness
```

```bash
curl -i https://<your-render-service>/api/lists/<listId>/snapshot
```

確認ポイント:

- Health が 200/UP
- Snapshot API が 200 でレスポンス JSON を返す
- Render のログに起動失敗・接続失敗が出ていない

## 4. トラブル時の初動

- 起動失敗: 環境変数の未設定/誤設定を優先確認
- DB 接続失敗: `POSTGRES_*` と `POSTGRES_SSL_MODE` を確認
- 5xx 継続: 直近変更を止めて、前回正常デプロイとの差分を確認

## 5. 関連ドキュメント

- [TEST_CONFIGURATION.md](TEST_CONFIGURATION.md)
- [システム設計](../../docs/architecture/system-design.md)
- [backend/README.md](../README.md)
- [AGENTS.md](../../AGENTS.md)
