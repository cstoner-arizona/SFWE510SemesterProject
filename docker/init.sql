-- Create spots table
CREATE TABLE spots (
   spot_id VARCHAR(36) PRIMARY KEY,  -- UUID as string
   name VARCHAR(255) NOT NULL,
   founder_skater_id VARCHAR(36) NOT NULL,  -- UUID as string
   address VARCHAR(500),
   latitude DECIMAL(10, 8),
   longitude DECIMAL(11, 8),
   difficulty_rating INTEGER CHECK (difficulty_rating BETWEEN 1 AND 10),
   surface_quality_rating INTEGER CHECK (surface_quality_rating BETWEEN 1 AND 10),
   description VARCHAR(2000),
   ideal_skate_time VARCHAR(100),
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP
);

-- Create spot_types table (for @ElementCollection of SpotType enum)
CREATE TABLE spot_types (
    spot_id VARCHAR(36) NOT NULL,
    type VARCHAR(50) NOT NULL,
    FOREIGN KEY (spot_id) REFERENCES spots(spot_id) ON DELETE CASCADE
);

-- Create spot_photos table (for @ElementCollection of photo URLs)
CREATE TABLE spot_photos (
     spot_id VARCHAR(36) NOT NULL,
     photo_url VARCHAR(500) NOT NULL,
     FOREIGN KEY (spot_id) REFERENCES spots(spot_id) ON DELETE CASCADE
);

-- Create trick_attempts table
CREATE TABLE trick_attempts (
    id BIGSERIAL PRIMARY KEY,
    spot_id VARCHAR(36) NOT NULL,
    skater_id VARCHAR(36) NOT NULL,  -- UUID as string
    trick_name VARCHAR(255) NOT NULL,
    FOREIGN KEY (spot_id) REFERENCES spots(spot_id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_spots_founder_skater_id ON spots(founder_skater_id);
CREATE INDEX idx_spot_types_spot_id ON spot_types(spot_id);
CREATE INDEX idx_spot_photos_spot_id ON spot_photos(spot_id);
CREATE INDEX idx_trick_attempts_skater_id ON trick_attempts(skater_id);
CREATE INDEX idx_trick_attempts_spot_id ON trick_attempts(spot_id);