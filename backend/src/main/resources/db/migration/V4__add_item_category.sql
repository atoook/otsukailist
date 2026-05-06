ALTER TABLE item
ADD COLUMN IF NOT EXISTS category TEXT NULL;

CREATE INDEX IF NOT EXISTS idx_item_list_category ON item (list_id, category);
