# AGENTS.md

このファイルは、Codex、GitHub Copilot、Claude Code、Gemini CLI など、任意の AI エージェントがこのリポジトリで作業するための共通入口です。実装前にこのファイルを読み、詳細はリンク先のドキュメントを正としてください。

> [!CAUTION]
> **必須:** コードやドキュメントを変更する前に、必ずこの `AGENTS.md` と `docs/README.md` を読んでください。
> そのうえで、変更対象に関係するドキュメントを全文確認してから作業してください。
> 関連ドキュメントが判断できない場合は、`docs/README.md` に載っている該当カテゴリを広めに読み、必要ならすべての横断ドキュメントを確認してください。
> 未確認の前提で実装・修正を進めてはいけません。

## 参照順序

1. [ドキュメント索引](docs/README.md)
2. [システム設計](docs/architecture/system-design.md)
3. [競合制御方針](docs/architecture/concurrency-control-policy.md)
4. [Backend コーディングガイド](backend/docs/CODING_GUIDELINES.md)
5. [Frontend アーキテクチャ](frontend/docs/ARCHITECTURE.md)
6. [DB README](db/README.md)

## 作業時の参照目安

- 全体設計・仕様変更: [システム設計](docs/architecture/system-design.md)
- 競合制御・更新系 API: [競合制御方針](docs/architecture/concurrency-control-policy.md)
- item 完了 API: [Item completion intent API](docs/api/item-completion-intent.md)
- リスト自動生成: [リスト自動生成機能 設計書](docs/features/list-generation-automation.md)
- Backend 実装: [Backend コーディングガイド](backend/docs/CODING_GUIDELINES.md)
- Frontend 実装: [Frontend アーキテクチャ](frontend/docs/ARCHITECTURE.md)
- UI・デザイン: [Design System](frontend/docs/DESIGN_SYSTEM.md)
- DB スキーマ変更: [DB README](db/README.md) と `backend/src/main/resources/db/migration/`

## プロジェクト概要

OtsukaiList は、ログイン不要で共有できる共同おつかいリストです。リストは UUID ベースの URL で共有され、UUID を知っていること自体をアクセス制御として扱います。バックエンドは Spring Boot REST API、フロントエンドは Vue 3 SPA です。

## リポジトリ構成

- `backend/`: Spring Boot API、Flyway マイグレーション、Java テスト、静的解析設定。
- `frontend/`: Vue 3 + Vite + TypeScript SPA。
- `db/`: ローカル開発・テスト・CI 用 PostgreSQL Docker Compose。
- `docs/`: プロダクト、横断設計、API 方針、機能設計。
- `backend/docs/`: バックエンド固有の実装規約、テスト、運用、DTO/Mapper 対応表。
- `frontend/docs/`: フロントエンド固有のアーキテクチャとデザインシステム。
- `.github/`: Issue テンプレート、プロンプト、GitHub Actions。

## よく使うコマンド

ルートディレクトリ:

```bash
./env.sh dev up
./env.sh dev down
./env.sh test up
./env.sh test down
```

`backend/` から実行:

```bash
./gradlew bootRun
./gradlew setupTestEnv
./gradlew test
./gradlew test --tests 'ClassName'
./gradlew cleanTestEnv
./gradlew check
./gradlew staticAnalysis
./gradlew spotlessApply
```

`frontend/` から実行:

```bash
npm run dev
npm run build
npm run test:run
npm run lint
npm run lint:fix
npm run format
```

## 実装ガードレール

- 「変更行数を最小にする」ことを主目的にせず、正しさ、保守性、型安全性、実運用時の競合耐性を優先する。
- UUID 共有モデルを崩さない。認証・権限モデルを追加する場合は、設計書を先に更新する。
- DB スキーマの正は `backend/src/main/resources/db/migration/` の Flyway SQL。`db/init/` は正式なスキーマ管理場所ではない。
- 更新系 API は `MutationResponse<T>` を返し、リスト全体の同期には `revision`、個別リソースの競合制御には `version` + `If-Match` を使う。
- 既存リソースの更新・削除・完了/未完了変更では、stale state を `412 precondition_failed`、`If-Match` 欠落を `428 precondition_required` として扱う。
- API エラー形式は `ApiError` に揃え、フロントエンドでは `frontend/src/lib/http.ts` で正規化する。
- ドキュメントを移動・追加した場合は、[ドキュメント索引](docs/README.md) と関連 README のリンクを同時に更新する。

## Backend 方針

- Controller は薄く保ち、Command/Query Service に委譲する。
- 書き込みは Service 層の `@Transactional` に閉じる。
- DTO と Entity は分離し、変換は `mapper/` の `@UtilityClass` に集約する。
- 入力 DTO は Bean Validation を使う。
- 例外は `ResourceNotFoundException`、`BadRequestException`、`PreconditionRequiredException`、`PreconditionFailedException` など既存の API 例外体系に寄せる。
- 新しいカラムや DTO 露出を追加するときは [Model / DTO / Mapper Coverage](backend/docs/MODEL_DTO_MAPPER_COVERAGE.md) を更新する。

## Frontend 方針

- 周辺コードのスタイルを優先する。多くの Vue コンポーネントは Options API、Pinia store は Options API style で実装されている。
- API 関数は `frontend/src/api/*.ts` にリソース単位で置き、`http.get<T>()` / `http.post<T>()` などの型付き呼び出しにする。
- 更新系 API は `MutationResponse<T>` を返し、UI からは `useMutation()` を通して revision gap とエラーを扱う。
- ID は `UUID = string` とし、API 型は `frontend/src/types/` に集約する。
- Tailwind class の条件結合は、既存コンポーネントと同じく `tailwind-merge` を使う。
- UI と設計トークンは [Design System](frontend/docs/DESIGN_SYSTEM.md) を参照する。

## Issue Automation Workflow

Issue をチャットから作成・分割する場合:

1. 利用可能なら MCP/GitHub connector の issue 操作を優先する。
2. 1 つの受け入れ範囲で完了できる作業は単一 issue にする。
3. 独立または順序依存の child task に分ける必要がある場合だけ orchestration を使う。
4. `.github/ISSUE_TEMPLATE/` のテンプレートを使う。
5. 手動で `gh` を使う場合、長い heredoc chain は避ける。
6. 端末が `heredoc>` 状態なら、`EOF` で閉じるか `Ctrl+C` で復帰してから続ける。
