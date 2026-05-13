ALTER TABLE item
ADD COLUMN IF NOT EXISTS item_type VARCHAR(20) NOT NULL DEFAULT 'plain';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_item_item_type'
    ) THEN
        ALTER TABLE item
        ADD CONSTRAINT ck_item_item_type
        CHECK (item_type IN ('plain', 'quantified'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_item_list_item_type ON item (list_id, item_type);

CREATE TABLE IF NOT EXISTS item_quantified (
        item_id UUID PRIMARY KEY,
        quantity BIGINT NOT NULL CHECK (quantity >= 0),
        base_unit VARCHAR(20) NOT NULL,
        origin VARCHAR(20) NOT NULL,
        regeneration_policy VARCHAR(20) NOT NULL,
        generator_key VARCHAR(80),

        CONSTRAINT fk_item_quantified_item_id
            FOREIGN KEY (item_id) REFERENCES item(id) ON DELETE CASCADE,
        CONSTRAINT ck_item_quantified_base_unit
            CHECK (base_unit IN ('g', 'ml', 'piece', 'pack')),
        CONSTRAINT ck_item_quantified_origin
            CHECK (origin IN ('manual', 'generated')),
        CONSTRAINT ck_item_quantified_regeneration_policy
            CHECK (regeneration_policy IN ('none', 'auto', 'locked'))
);

CREATE INDEX IF NOT EXISTS idx_item_quantified_generator_key
    ON item_quantified (generator_key);

CREATE TABLE IF NOT EXISTS list_generation_config (
        id UUID PRIMARY KEY,
        list_id UUID NOT NULL,
        config_type VARCHAR(40) NOT NULL,
        config_json JSONB NOT NULL,
        created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
        updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

        CONSTRAINT fk_list_generation_config_list_id
            FOREIGN KEY (list_id) REFERENCES item_list(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_list_generation_config_list_id
    ON list_generation_config (list_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_list_generation_config_list_type
    ON list_generation_config (list_id, config_type);
