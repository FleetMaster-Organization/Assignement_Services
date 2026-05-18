CREATE TABLE assignments (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    vehicle_plate VARCHAR(20) NOT NULL,
    driver_id UUID NOT NULL,
    driver_name VARCHAR(100) NOT NULL,
    created_by_user_id UUID NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    initial_km DOUBLE PRECISION NOT NULL,
    final_km DOUBLE PRECISION
);
