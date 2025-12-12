-- Add 3 more shelters to have total of 4 shelters
INSERT INTO shelters (
    name, description, address, capacity, current_occupancy, 
    contact_phone, contact_email, facilities, 
    latitude, longitude, geom_point,
    status, manager_name, manager_phone, notes
) VALUES 
(
    'Trung tâm Thể thao Mỹ Đình',
    'Trung tâm thể thao lớn được chuyển đổi thành nơi trú ẩn khẩn cấp',
    'Đường Lê Đức Thọ, Quận Nam Từ Liêm, Hà Nội',
    800, 200,
    '024-3768-9999', 'mydinh.shelter@hanoi.gov.vn',
    ARRAY['wifi', 'medical', 'kitchen', 'parking', 'security', 'sports_facilities'],
    21.0285, 105.7650,
    ST_SetSRID(ST_MakePoint(105.7650, 21.0285), 4326),
    'ACTIVE', 'Nguyễn Thị Lan', '0988-123-456',
    'Trung tâm thể thao lớn, có sân vận động và nhiều tiện nghi'
),
(
    'Bệnh viện Đa khoa Đống Đa',
    'Khu vực tạm thời của bệnh viện dành cho trú ẩn khẩn cấp',
    'Phố Kim Liên, Quận Đống Đa, Hà Nội',
    150, 80,
    '024-3852-3456', 'dongda.hospital@health.gov.vn',
    ARRAY['medical', 'wifi', 'emergency_care'],
    21.0167, 105.8372,
    ST_SetSRID(ST_MakePoint(105.8372, 21.0167), 4326),
    'ACTIVE', 'Bác sĩ Trần Văn Minh', '0912-789-123',
    'Có đội ngũ y tế chuyên nghiệp, phù hợp cho người bệnh và người già'
),
(
    'Trung tâm Hội nghị Quốc gia',
    'Trung tâm hội nghị lớn được sử dụng làm nơi trú ẩn tạm thời',
    'Đường Thăng Long, Quận Nam Từ Liêm, Hà Nội',
    1000, 0,
    '024-3756-8888', 'conference.shelter@gov.vn',
    ARRAY['wifi', 'kitchen', 'parking', 'security', 'conference_rooms'],
    21.0520, 105.7980,
    ST_SetSRID(ST_MakePoint(105.7980, 21.0520), 4326),
    'ACTIVE', 'Lê Thị Hương', '0903-456-789',
    'Không gian rất rộng, có thể chứa số lượng lớn người dân'
);