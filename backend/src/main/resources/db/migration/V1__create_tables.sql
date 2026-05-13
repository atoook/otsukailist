CREATE TABLE IF NOT EXISTS item_list (
        id UUID PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        revision BIGINT NOT NULL DEFAULT 0 CHECK (revision >= 0),
        created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
        updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

CREATE INDEX IF NOT EXISTS idx_item_list_updated_at ON item_list (updated_at);

CREATE TABLE IF NOT EXISTS member (
        id UUID PRIMARY KEY,
        list_id UUID NOT NULL,
        display_name VARCHAR(80) NOT NULL,
        created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
        updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

        CONSTRAINT fk_member_list_id
            FOREIGN KEY (list_id) REFERENCES item_list(id) ON DELETE CASCADE,
        CONSTRAINT uq_member_list_name
            UNIQUE (list_id, display_name)
);

CREATE INDEX IF NOT EXISTS idx_member_list_id ON member (list_id);

CREATE TABLE IF NOT EXISTS item (
        id UUID PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        is_completed BOOLEAN NOT NULL DEFAULT FALSE,
        created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
        updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

        list_id UUID NOT NULL,
        completed_by_member_id UUID NULL,
        completed_at TIMESTAMP(3) NULL,

        CONSTRAINT fk_item_list_id
            FOREIGN KEY (list_id) REFERENCES item_list(id) ON DELETE CASCADE,
        CONSTRAINT fk_item_completed_by_member_id
            FOREIGN KEY (completed_by_member_id) REFERENCES member(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_list_id ON item (list_id);
CREATE INDEX IF NOT EXISTS idx_list_completed ON item (list_id, is_completed);
CREATE INDEX IF NOT EXISTS idx_completed_by ON item (completed_by_member_id);
CREATE INDEX IF NOT EXISTS idx_created_at ON item (created_at);
