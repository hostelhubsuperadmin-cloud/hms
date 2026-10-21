CREATE TABLE hostels (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(150) NOT NULL,
                         code VARCHAR(50) NOT NULL UNIQUE,
                         description VARCHAR(2000),
                         address_line VARCHAR(255) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         state VARCHAR(100) NOT NULL,
                         country VARCHAR(100) NOT NULL DEFAULT 'India',
                         postal_code VARCHAR(20),
                         contact_phone VARCHAR(20),
                         contact_email VARCHAR(254),
                         status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_hostels_status
                             CHECK (status IN ('DRAFT', 'ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_hostels_city_status
    ON hostels (city, status);

CREATE INDEX idx_hostels_status
    ON hostels (status);