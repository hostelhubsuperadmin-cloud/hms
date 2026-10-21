ALTER TABLE rooms
    ADD COLUMN pricing_category VARCHAR(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE rooms
    ADD CONSTRAINT chk_rooms_pricing_category
        CHECK (pricing_category IN ('NORMAL', 'AC'));