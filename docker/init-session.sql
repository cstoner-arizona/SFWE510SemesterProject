CREATE TABLE sessions (
    session_id VARCHAR(36) PRIMARY KEY,
    skater_id VARCHAR(36) NOT NULL,
    spot_id VARCHAR(36) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    notes VARCHAR(2000),
    weather VARCHAR(100),
    rating INTEGER CHECK (rating BETWEEN 1 AND 10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE session_tricks (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL,
    trick_name VARCHAR(100) NOT NULL,
    attempts INTEGER,
    lands INTEGER,
    is_new_trick BOOLEAN DEFAULT FALSE,
    notes VARCHAR(500),
    timestamp TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE
);

CREATE INDEX idx_sessions_skater_id ON sessions(skater_id);
CREATE INDEX idx_sessions_spot_id ON sessions(spot_id);
CREATE INDEX idx_sessions_tricks_session_id ON session_tricks(session_id);
