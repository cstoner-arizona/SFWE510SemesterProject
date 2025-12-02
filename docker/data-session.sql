-- Insert sample sessions (some active, some completed)
INSERT INTO sessions (session_id, skater_id, spot_id, start_time, end_time, notes, weather, rating, created_at) VALUES
    -- Completed sessions
    ('750e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', '2024-11-25 14:00:00', '2024-11-25 16:30:00', 'Great session! Landed some clean kickflips', 'Sunny, 72F', 8, '2024-11-25 14:00:00'),
    ('750e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', '2024-11-26 10:00:00', '2024-11-26 12:00:00', 'Working on rail tricks', 'Cloudy, 65F', 7, '2024-11-26 10:00:00'),
    ('750e8400-e29b-41d4-a716-446655440003', '650e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', '2024-11-27 15:00:00', '2024-11-27 17:00:00', 'Tried some new grind variations', 'Partly cloudy, 68F', 9, '2024-11-27 15:00:00'),
    ('750e8400-e29b-41d4-a716-446655440004', '650e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', '2024-11-28 09:00:00', '2024-11-28 11:30:00', 'Morning session felt good', 'Clear, 60F', 8, '2024-11-28 09:00:00'),
    -- Active session (no end_time)
    ('750e8400-e29b-41d4-a716-446655440005', '650e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', '2024-12-02 13:00:00', NULL, 'Current session in progress', 'Sunny, 70F', NULL, '2024-12-02 13:00:00');

-- Insert session tricks (detailed trick attempts during sessions, trick_number resets per session)
INSERT INTO session_tricks (session_id, trick_number, trick_name, attempts, lands, is_new_trick, notes, timestamp) VALUES
    -- Session 1 tricks (1-3)
    ('750e8400-e29b-41d4-a716-446655440001', 1, 'Kickflip', 15, 12, false, 'Really consistent today', '2024-11-25 14:30:00'),
    ('750e8400-e29b-41d4-a716-446655440001', 2, 'Heelflip', 10, 7, false, 'Getting better', '2024-11-25 15:00:00'),
    ('750e8400-e29b-41d4-a716-446655440001', 3, '360 Flip', 8, 2, false, 'Still working on this one', '2024-11-25 16:00:00'),

    -- Session 2 tricks (1-2)
    ('750e8400-e29b-41d4-a716-446655440002', 1, '50-50 Grind', 12, 8, false, 'Felt smooth', '2024-11-26 10:30:00'),
    ('750e8400-e29b-41d4-a716-446655440002', 2, 'Boardslide', 10, 5, false, 'Need more practice', '2024-11-26 11:00:00'),

    -- Session 3 tricks (1-3)
    ('750e8400-e29b-41d4-a716-446655440003', 1, 'Nosegrind', 15, 10, false, 'Locked in nicely', '2024-11-27 15:30:00'),
    ('750e8400-e29b-41d4-a716-446655440003', 2, 'Feeble Grind', 8, 3, true, 'First time trying this!', '2024-11-27 16:00:00'),
    ('750e8400-e29b-41d4-a716-446655440003', 3, '50-50 Grind', 12, 11, false, 'Super consistent', '2024-11-27 16:30:00'),

    -- Session 4 tricks (1-3)
    ('750e8400-e29b-41d4-a716-446655440004', 1, 'Kickflip', 20, 18, false, 'On fire today!', '2024-11-28 09:30:00'),
    ('750e8400-e29b-41d4-a716-446655440004', 2, 'Varial Flip', 15, 10, false, 'Getting more consistent', '2024-11-28 10:00:00'),
    ('750e8400-e29b-41d4-a716-446655440004', 3, 'Hardflip', 12, 4, false, 'Still challenging', '2024-11-28 10:45:00'),

    -- Session 5 tricks (1-2) (active session)
    ('750e8400-e29b-41d4-a716-446655440005', 1, 'Ollie', 10, 9, false, 'Warming up', '2024-12-02 13:15:00'),
    ('750e8400-e29b-41d4-a716-446655440005', 2, 'Kickflip', 8, 5, false, 'Working on consistency', '2024-12-02 13:30:00');
