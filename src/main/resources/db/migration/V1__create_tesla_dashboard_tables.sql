CREATE TABLE tesla_client (
    id BIGINT NOT NULL AUTO_INCREMENT,
    client_name VARCHAR(255),
    client_id VARCHAR(255),
    client_secret VARCHAR(255),
    email VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tesla_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    access_token TEXT,
    refresh_token TEXT,
    token_type VARCHAR(255),
    expires_at DATETIME(6),
    scope TEXT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    client_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_tesla_tokens_client FOREIGN KEY (client_id) REFERENCES tesla_client (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tesla_vehicle (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vehicle_id BIGINT,
    vin VARCHAR(17) NOT NULL,
    display_name VARCHAR(255),
    state VARCHAR(255),
    color VARCHAR(255),
    access_type VARCHAR(255),
    in_service BIT NOT NULL,
    calendar_enabled BIT NOT NULL,
    api_version INT,
    client_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_tesla_vehicle_vin UNIQUE (vin),
    CONSTRAINT fk_tesla_vehicle_client FOREIGN KEY (client_id) REFERENCES tesla_client (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
