-- Add a jsonb column to store UI and other station-specific configurations
ALTER TABLE petrol_stations ADD COLUMN configuration jsonb;