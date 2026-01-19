# OtsukaiList - プロジェクト構成

```
otsukailist/
├── backend/                  # Spring Boot (Java)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/atoook/otsukailist/
│   │   │   │   ├── OtsukaiListApplication.java   # 起動クラス
│   │   │   │   ├── controller/   # REST + WebSocket Controller
│   │   │   │   ├── service/      # ビジネスロジック
│   │   │   │   ├── model/        # JPAエンティティ
│   │   │   │   └── repository/   # Spring Data JPA
│   │   │   └── resources/
│   │   │       ├── application.yml / application.properties   # DB・ログ設定
│   │   │       ├── static/, templates/                        # Webリソース
│   │   │       └── env周りのサンプル                          # 必要に応じて追加
│   │   └── test/   # JUnit テスト
│   ├── build.gradle / pom.xml
│   └── Dockerfile
│
├── frontend/                 # Vue.js 3 + Vite
│   ├── src/
│   │   ├── components/       # Vue コンポーネント
│   │   ├── pages/            # 各ページ (/new, /list/:id)
│   │   ├── store/            # 状態管理（選定中）
│   │   └── main.ts           # エントリポイント
│   ├── index.html
│   ├── vite.config.ts
│   ├── package.json
│   └── Dockerfile
│
├── db/                       # MySQL 用
│   ├── docker-compose*.yml   # 開発/CI/本番向け設定
│   └── init/                 # 初期化SQL（テーブル定義・サンプルデータ）
│
├── docs/                     # ドキュメント類
│   ├── otsukailist企画書.md
│   ├── otsukailist設計書.md
│   └── otsukailistプロジェクト構成案.md
│
├── docker-compose.yml        # app + db 一括起動
├── README.md
└── LICENSE

```
