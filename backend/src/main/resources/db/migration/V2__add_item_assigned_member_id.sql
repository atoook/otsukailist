ALTER TABLE item
ADD COLUMN IF NOT EXISTS assigned_member_id UUID NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_item_assigned_member_id'
    ) THEN
        ALTER TABLE item
        ADD CONSTRAINT fk_item_assigned_member_id
        FOREIGN KEY (assigned_member_id) REFERENCES member(id) ON DELETE SET NULL;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_assigned_member ON item (assigned_member_id);
