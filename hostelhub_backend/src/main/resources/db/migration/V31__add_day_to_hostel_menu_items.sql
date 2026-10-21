ALTER TABLE hostel_menu_items
    ADD COLUMN day_of_week VARCHAR(10);

-- Existing items need a valid day before the column becomes mandatory.
-- Assign them Monday so existing data remains readable and editable.
UPDATE hostel_menu_items
SET day_of_week = 'MONDAY'
WHERE day_of_week IS NULL;

ALTER TABLE hostel_menu_items
    ALTER COLUMN day_of_week SET NOT NULL;

ALTER TABLE hostel_menu_items
DROP CONSTRAINT IF EXISTS hostel_menu_items_day_of_week_check;

ALTER TABLE hostel_menu_items
    ADD CONSTRAINT hostel_menu_items_day_of_week_check
        CHECK (
            day_of_week IN (
                            'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
                            'FRIDAY', 'SATURDAY', 'SUNDAY'
                )
            );

CREATE INDEX IF NOT EXISTS idx_menu_hostel_day
    ON hostel_menu_items(hostel_id, day_of_week);