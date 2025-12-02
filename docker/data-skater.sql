-- Insert sample skaters (matching the skater_ids used in spot data)
INSERT INTO skaters (skater_id, username, email, display_name, bio, skill_level, stance, hometown, profile_photo_url, created_at, updated_at) VALUES
    ('650e8400-e29b-41d4-a716-446655440001', 'tony_hawk', 'tony@skateapp.com', 'Tony H', 'Just here to shred and have fun!', 'PRO', 'GOOFY', 'San Diego, CA', 'https://example.com/profiles/tony.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440002', 'sk8ergirl', 'sarah@skateapp.com', 'Sarah K', 'Learning new tricks every day', 'INTERMEDIATE', 'REGULAR', 'Los Angeles, CA', 'https://example.com/profiles/sarah.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440003', 'grind_master', 'mike@skateapp.com', 'Mike R', 'Rails and ledges are my jam', 'ADVANCED', 'REGULAR', 'San Francisco, CA', 'https://example.com/profiles/mike.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440004', 'flip_wizard', 'emma@skateapp.com', 'Emma L', 'Flip tricks all day!', 'INTERMEDIATE', 'GOOFY', 'Oakland, CA', 'https://example.com/profiles/emma.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('650e8400-e29b-41d4-a716-446655440005', 'newbie_skate', 'alex@skateapp.com', 'Alex T', 'Just started skating, loving it!', 'BEGINNER', 'REGULAR', 'Berkeley, CA', 'https://example.com/profiles/alex.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample tricks for skaters (trick_number resets per skater)
INSERT INTO tricks (skater_id, trick_number, name, difficulty, learned_at) VALUES
    -- Tony H tricks (1-4)
    ('650e8400-e29b-41d4-a716-446655440001', 1, 'Kickflip', 5, '2020-01-15'),
    ('650e8400-e29b-41d4-a716-446655440001', 2, 'Heelflip', 5, '2020-02-20'),
    ('650e8400-e29b-41d4-a716-446655440001', 3, '360 Flip', 8, '2020-05-10'),
    ('650e8400-e29b-41d4-a716-446655440001', 4, 'Backside 180', 4, '2019-11-05'),
    -- Sarah K tricks (1-3)
    ('650e8400-e29b-41d4-a716-446655440002', 1, 'Ollie', 3, '2022-03-12'),
    ('650e8400-e29b-41d4-a716-446655440002', 2, 'Kickflip', 5, '2022-08-20'),
    ('650e8400-e29b-41d4-a716-446655440002', 3, 'Pop Shove-it', 4, '2022-06-15'),
    -- Mike R tricks (1-3)
    ('650e8400-e29b-41d4-a716-446655440003', 1, '50-50 Grind', 6, '2021-04-10'),
    ('650e8400-e29b-41d4-a716-446655440003', 2, 'Boardslide', 6, '2021-05-22'),
    ('650e8400-e29b-41d4-a716-446655440003', 3, 'Nosegrind', 7, '2021-07-18'),
    -- Emma L tricks (1-3)
    ('650e8400-e29b-41d4-a716-446655440004', 1, 'Kickflip', 5, '2023-01-10'),
    ('650e8400-e29b-41d4-a716-446655440004', 2, 'Varial Flip', 6, '2023-03-25'),
    ('650e8400-e29b-41d4-a716-446655440004', 3, 'Hardflip', 7, '2023-06-14'),
    -- Alex T tricks (1)
    ('650e8400-e29b-41d4-a716-446655440005', 1, 'Ollie', 3, '2024-11-01');

-- Insert trick categories (using composite key: skater_id + trick_number)
INSERT INTO trick_categories (skater_id, trick_number, category) VALUES
    -- Tony H trick categories
    ('650e8400-e29b-41d4-a716-446655440001', 1, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440001', 2, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440001', 3, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440001', 3, 'SPIN'),
    ('650e8400-e29b-41d4-a716-446655440001', 4, 'SPIN'),
    -- Sarah K trick categories
    ('650e8400-e29b-41d4-a716-446655440002', 1, 'FLATGROUND'),
    ('650e8400-e29b-41d4-a716-446655440002', 2, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440002', 3, 'SPIN'),
    -- Mike R trick categories
    ('650e8400-e29b-41d4-a716-446655440003', 1, 'GRIND'),
    ('650e8400-e29b-41d4-a716-446655440003', 2, 'SLIDE'),
    ('650e8400-e29b-41d4-a716-446655440003', 3, 'GRIND'),
    -- Emma L trick categories
    ('650e8400-e29b-41d4-a716-446655440004', 1, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440004', 2, 'FLIP'),
    ('650e8400-e29b-41d4-a716-446655440004', 3, 'FLIP'),
    -- Alex T trick categories
    ('650e8400-e29b-41d4-a716-446655440005', 1, 'FLATGROUND');
