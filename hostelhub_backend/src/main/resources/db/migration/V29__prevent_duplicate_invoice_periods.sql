CREATE UNIQUE INDEX uq_invoice_allocation_period
    ON invoices(allocation_id, period_start, period_end);