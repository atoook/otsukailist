-- otsukailist_testデータベースの初期化スクリプト（テスト環境専用）

USE otsukailist_test;

-- お使いリストテーブル（ログイン不要、UUID使用）
CREATE TABLE IF NOT EXISTS item_list (
    id BINARY(16) PRIMARY KEY,                          -- UUID (BINARY形式)
    name VARCHAR(100) NOT NULL,                         -- リスト名
    revision BIGINT UNSIGNED NOT NULL DEFAULT 0,        -- Socket差分/欠け検知用（リスト単位）
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    INDEX idx_item_list_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- メンバー（権限制御なし：リスト内のラベル用）
CREATE TABLE IF NOT EXISTS member (
    id BINARY(16) PRIMARY KEY,                 -- UUID (BINARY形式)
    list_id BINARY(16) NOT NULL,               -- item_list へのFK
    display_name VARCHAR(80) NOT NULL,         -- ドロップダウン表示名
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    FOREIGN KEY (list_id) REFERENCES item_list(id) ON DELETE CASCADE,

    INDEX idx_member_list_id (list_id)
    ,UNIQUE KEY uq_member_list_name (list_id, display_name)     -- 同一リスト内の重複名を防ぐためのユニーク制約
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- アイテムテーブル
CREATE TABLE IF NOT EXISTS item (
    id BINARY(16) PRIMARY KEY,                         -- UUID (BINARY形式)
    name VARCHAR(255) NOT NULL,
    is_completed TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    list_id BINARY(16) NOT NULL,                        -- item_listへの外部キー
    completed_by_member_id BINARY(16) NULL,             -- 誰が完了させたか（member.id）
    completed_at DATETIME(3) NULL,                      -- いつ完了させたか

    FOREIGN KEY (list_id) REFERENCES item_list(id) ON DELETE CASCADE,
    FOREIGN KEY (completed_by_member_id) REFERENCES member(id) ON DELETE SET NULL,

    INDEX idx_list_id (list_id),
    INDEX idx_list_completed (list_id, is_completed),
    INDEX idx_completed_by (completed_by_member_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;