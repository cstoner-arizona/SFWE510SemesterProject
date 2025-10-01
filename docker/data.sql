-- Insert sample spots (using UUID format)
INSERT INTO spots (spot_id, name, founder_skater_id, address, latitude, longitude, difficulty_rating, surface_quality_rating, description, ideal_skate_time, created_at) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'Downtown Ledge', '650e8400-e29b-41d4-a716-446655440001', '123 Main St', 37.7749, -122.4194, 7, 9, 'Perfect marble ledge', 'Afternoon', CURRENT_TIMESTAMP),
    ('550e8400-e29b-41d4-a716-446655440002', 'Skate Park Rails', '650e8400-e29b-41d4-a716-446655440002', '456 Park Ave', 37.7849, -122.4294, 5, 8, 'Multiple rails and boxes', 'Morning', CURRENT_TIMESTAMP);

-- Insert spot types
INSERT INTO spot_types (spot_id, type) VALUES
   ('550e8400-e29b-41d4-a716-446655440001', 'LEDGE'),
   ('550e8400-e29b-41d4-a716-446655440001', 'STREET_GAP'),
   ('550e8400-e29b-41d4-a716-446655440002', 'RAIL'),
   ('550e8400-e29b-41d4-a716-446655440002', 'PARK');

-- Insert sample photos
INSERT INTO spot_photos (spot_id, photo_url) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', 'https://example.com/photos/spot1-1.jpg'),
    ('550e8400-e29b-41d4-a716-446655440001', 'https://example.com/photos/spot1-2.jpg');

-- Insert sample trick attempts
INSERT INTO trick_attempts (spot_id, skater_id, trick_name) VALUES
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440001', 'Kickflip'),
    ('550e8400-e29b-41d4-a716-446655440001', '650e8400-e29b-41d4-a716-446655440002', 'Backside 180'),
    ('550e8400-e29b-41d4-a716-446655440002', '650e8400-e29b-41d4-a716-446655440001', '50-50 Grind');