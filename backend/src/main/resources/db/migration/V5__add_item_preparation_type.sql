ALTER TABLE item
ADD COLUMN IF NOT EXISTS preparation_type TEXT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_item_preparation_type'
    ) THEN
        ALTER TABLE item
        ADD CONSTRAINT ck_item_preparation_type
        CHECK (preparation_type IN ('bring'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_item_list_preparation_type ON item (list_id, preparation_type);
