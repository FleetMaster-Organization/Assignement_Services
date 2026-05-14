CREATE TABLE vehicles (
    id BINARY(16) PRIMARY KEY,
    plate VARCHAR(20) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL,
    current_km DOUBLE NOT NULL,
    soat_expiry_date DATETIME NOT NULL,
    techno_expiry_date DATETIME NOT NULL
);

CREATE TABLE drivers (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    license_expiry_date DATETIME NOT NULL
);

CREATE TABLE assignments (
    id BINARY(16) PRIMARY KEY,
    vehicle_id BINARY(16) NOT NULL,
    driver_id BINARY(16) NOT NULL,
    created_by_user_id BINARY(16) NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME,
    initial_km DOUBLE NOT NULL,
    final_km DOUBLE,
    CONSTRAINT fk_assignment_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_assignment_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
