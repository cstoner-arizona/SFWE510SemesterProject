CREATE TABLE skaters (
    skater_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(100),
    bio VARCHAR(160),
    skill_level VARCHAR(20),
    stance VARCHAR(10),
    hometown VARCHAR(100),
    profile_photo_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tricks (
  skater_id VARCHAR(36) NOT NULL,
  trick_number INTEGER NOT NULL,
  name VARCHAR(100) NOT NULL,
  difficulty INTEGER CHECK (difficulty BETWEEN 1 AND 10),
  learned_at DATE,
  PRIMARY KEY (skater_id, trick_number),
  FOREIGN KEY (skater_id) REFERENCES skaters(skater_id) ON DELETE CASCADE
);

CREATE TABLE trick_categories (
    skater_id VARCHAR(36) NOT NULL,
    trick_number INTEGER NOT NULL,
    category VARCHAR(50) NOT NULL,
    PRIMARY KEY (skater_id, trick_number, category),
    FOREIGN KEY (skater_id, trick_number) REFERENCES tricks(skater_id, trick_number) ON DELETE CASCADE
);

CREATE INDEX idx_skaters_username ON skaters(username);
CREATE INDEX idx_tricks_skater_id ON tricks(skater_id);
CREATE INDEX idx_trick_categories_skater_id ON trick_categories(skater_id);
