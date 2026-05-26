# Documentation Index

このディレクトリは、OtsukaiList の横断的なプロダクト・設計・API・機能ドキュメントを置く場所です。AI エージェント向けの作業入口は [AGENTS.md](../AGENTS.md) を参照してください。

## Product

- [企画書](product/planning.md)

## Architecture

- [システム設計](architecture/system-design.md)
- [競合制御方針](architecture/concurrency-control-policy.md)

## API

- [Item completion intent API](api/item-completion-intent.md)

## Features

- [リスト自動生成機能 設計書](features/list-generation-automation.md)

## Area-Specific Docs

- [Backend README](../backend/README.md)
- [Backend コーディングガイド](../backend/docs/CODING_GUIDELINES.md)
- [Backend テスト設定](../backend/docs/TEST_CONFIGURATION.md)
- [Backend デプロイ運用](../backend/docs/DEPLOY_OPERATIONS.md)
- [Model / DTO / Mapper Coverage](../backend/docs/MODEL_DTO_MAPPER_COVERAGE.md)
- [Frontend README](../frontend/README.md)
- [Frontend Architecture](../frontend/docs/ARCHITECTURE.md)
- [Frontend Design System](../frontend/docs/DESIGN_SYSTEM.md)
- [Database README](../db/README.md)

## Placement Rules

- `docs/`: 複数領域にまたがるプロダクト、全体設計、API 契約、機能設計。
- `backend/docs/`: Java/Spring Boot 実装、テスト、デプロイ、DTO/Mapper などバックエンド固有の文書。
- `frontend/docs/`: Vue/TypeScript 実装、UI 設計、フロントエンド固有の文書。
- `db/README.md`: ローカル DB 起動、環境別 Docker Compose、Flyway との関係。
- `.github/`: GitHub Actions、Issue テンプレート、GitHub 固有のプロンプトやスキル。
