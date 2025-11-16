-- otsukailistデータベースの初期化スクリプト

USE otsukailist;

-- お使いリストテーブル（ログイン不要、UUID使用）
CREATE TABLE IF NOT EXISTS shopping_list (
    id CHAR(36) PRIMARY KEY, -- UUID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- アイテムテーブル（シンプル設計）
CREATE TABLE IF NOT EXISTS item (
    id CHAR(36) PRIMARY KEY, -- UUID
    name VARCHAR(255) NOT NULL,
    is_checked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    list_id CHAR(36) NOT NULL,
    FOREIGN KEY (list_id) REFERENCES shopping_list(id) ON DELETE CASCADE,
    INDEX idx_list_id (list_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;