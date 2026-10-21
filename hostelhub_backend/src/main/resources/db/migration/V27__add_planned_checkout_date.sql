ALTER TABLE allocations
    ADD COLUMN planned_checkout_date DATE;

ALTER TABLE allocations
    ADD CONSTRAINT chk_allocations_planned_checkout
        CHECK (
            planned_checkout_date IS NULL
                OR planned_checkout_date >= start_date
            );