-- resources/db/migration/V1__Initial_schema.sql

-- Create Petrol Station Table
CREATE TABLE petrol_stations (
    id BIGSERIAL PRIMARY KEY,
    station_name VARCHAR(255) NOT NULL,
    station_code VARCHAR(50) NOT NULL UNIQUE,
    dealer_name VARCHAR(255),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(500),
    created_at DATE
);

-- Create User Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    petrol_station_id BIGINT,
    CONSTRAINT fk_user_petrol_station FOREIGN KEY (petrol_station_id) REFERENCES petrol_stations(id)
);

-- Create DSR Entry Table
CREATE TABLE dsr_entry (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    shift_type VARCHAR(20) NOT NULL,
    json_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_dsr_date_shift UNIQUE (date, shift_type)
);

-- Create Nozzle Readings Table
CREATE TABLE nozzle_readings (
    id BIGSERIAL PRIMARY KEY,
    nozzle_id VARCHAR(100) NOT NULL,
    starting_reading DOUBLE PRECISION,
    ending_reading DOUBLE PRECISION
);

-- Create indexes for performance
CREATE INDEX idx_dsr_date ON dsr_entry(date);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_petrol_station_code ON petrol_stations(station_code);