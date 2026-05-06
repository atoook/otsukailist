ALTER TABLE item
ADD COLUMN IF NOT EXISTS preparation_type TEXT NULL;

CREATE INDEX IF NOT EXISTS idx_item_list_preparation_type ON item (list_id, preparation_type);
