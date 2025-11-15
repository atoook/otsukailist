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
│   │   │       ├── application.yml   # MySQL 接続設定
│   │   │       └── schema.sql        # 初期スキーマ（必要なら）
│   │   └── test/   # JUnit テスト
│   ├── build.gradle / pom.xml
│   └── Dockerfile
│
├── frontend/                 # Vue.js 3 + Vite
│   ├── src/
│   │   ├── components/       # Vue コンポーネント
│   │   ├── pages/            # 各ページ (/new, /list/:id)
│   │   ├── store/            # 状態管理 (Pinia など)
│   │   └── main.ts           # エントリポイント
│   ├── index.html
│   ├── vite.config.ts
│   ├── package.json
│   └── Dockerfile
│
├── db/                       # MySQL 用
│   ├── init.sql              # 初期化用SQL（ユーザー・DB作成など）
│   └── data/                 # 永続化ボリュームマウント先
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
