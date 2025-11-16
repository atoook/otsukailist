-- otsukailistデータベースの初期化スクリプト

USE otsukailist;

-- お使いリストテーブル（ログイン不要、UUID使用）
CREATE TABLE IF NOT EXISTS shopping_list (
    id BINARY(16) PRIMARY KEY, -- UUID (BINARY形式で効率的保存)
    name VARCHAR(100) DEFAULT 'お買い物リスト', -- リスト名を追加
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- アイテムテーブル（シンプル設計）
CREATE TABLE IF NOT EXISTS item (
    id BINARY(16) PRIMARY KEY, -- UUID (BINARY形式で効率的保存)
    name VARCHAR(255) NOT NULL,
    is_checked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    list_id BINARY(16) NOT NULL, -- shopping_listへの外部キー
    FOREIGN KEY (list_id) REFERENCES shopping_list(id) ON DELETE CASCADE,
    INDEX idx_list_id (list_id),
    INDEX idx_list_checked (list_id, is_checked),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;