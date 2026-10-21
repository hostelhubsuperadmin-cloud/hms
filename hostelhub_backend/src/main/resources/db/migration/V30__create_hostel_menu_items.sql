
CREATE TABLE hostel_menu_items (
                                   id BIGSERIAL PRIMARY KEY,

                                   hostel_id BIGINT NOT NULL,

                                   name VARCHAR(120) NOT NULL,
                                   meal_type VARCHAR(30) NOT NULL,
                                   description VARCHAR(1000),

                                   price NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
                                   is_available BOOLEAN NOT NULL DEFAULT TRUE,

                                   created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_hostel_menu_items_hostel
                                       FOREIGN KEY (hostel_id)
                                           REFERENCES hostels(id)
                                           ON DELETE CASCADE,

                                   CONSTRAINT chk_hostel_menu_items_price
                                       CHECK (price >= 0),

                                   CONSTRAINT chk_hostel_menu_items_meal_type
                                       CHECK (
                                           meal_type IN (
                                                         'BREAKFAST',
                                                         'LUNCH',
                                                         'DINNER',
                                                         'SNACKS',
                                                         'OTHER'
                                               )
                                           )
);

CREATE INDEX idx_hostel_menu_items_hostel
    ON hostel_menu_items(hostel_id);

CREATE INDEX idx_hostel_menu_items_public
    ON hostel_menu_items(hostel_id, is_available);
