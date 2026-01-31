-- V1__Initial_schema.sql

-- Create Petrol Station Table
CREATE TABLE petrol_stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_name VARCHAR(255) NOT NULL,
    station_code VARCHAR(50) NOT NULL UNIQUE,
    dealer_name VARCHAR(255),
    contact_number VARCHAR(20),
    email VARCHAR(255),
    address VARCHAR(500)
);

-- Create User Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    petrol_station_id BIGINT,
    CONSTRAINT fk_user_petrol_station FOREIGN KEY (petrol_station_id) REFERENCES petrol_stations(id)
);

-- Create DSR Entry Table
CREATE TABLE dsr_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    starting_reading DECIMAL(12, 3) DEFAULT 0,
    ending_reading DECIMAL(12, 3) DEFAULT 0,
    sales DECIMAL(12, 3) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_dsr_date ON dsr_entry(date);
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_petrol_station_code ON petrol_stations(station_code);