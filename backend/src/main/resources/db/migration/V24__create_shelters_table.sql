-- Create shelters table
CREATE TABLE shelters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address TEXT NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 0,
    current_occupancy INTEGER NOT NULL DEFAULT 0,
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    facilities TEXT[], -- Array of facilities like ['wifi', 'medical', 'kitchen', 'parking']
    geom_point GEOMETRY(Point, 4326),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, FULL, CLOSED, MAINTENANCE
    is_emergency_shelter BOOLEAN NOT NULL DEFAULT true,
    accessibility_features TEXT[], -- ['wheelchair_accessible', 'hearing_loop', 'braille_signs']
    operating_hours VARCHAR(255) DEFAULT '24/7',
    manager_name VARCHAR(255),
    manager_phone VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_shelters_status ON shelters(status);
CREATE INDEX idx_shelters_location ON shelters USING GIST(geom_point);
CREATE INDEX idx_shelters_capacity ON shelters(capacity, current_occupancy);
CREATE INDEX idx_shelters_emergency ON shelters(is_emergency_shelter);

-- Add constraint to ensure occupancy doesn't exceed capacity
ALTER TABLE shelters ADD CONSTRAINT chk_occupancy_capacity 
    CHECK (current_occupancy >= 0 AND current_occupancy <= capacity);

-- Add constraint for valid status values
ALTER TABLE shelters ADD CONSTRAINT chk_shelter_status 
    CHECK (status IN ('ACTIVE', 'FULL', 'CLOSED', 'MAINTENANCE'));

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_shelter_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER shelter_updated_at_trigger
    BEFORE UPDATE ON shelters
    FOR EACH ROW
    EXECUTE FUNCTION update_shelter_updated_at();

-- Insert sample shelter data
INSERT INTO shelters (
    name, description, address, capacity, current_occupancy, 
    contact_phone, contact_email, facilities, 
    latitude, longitude, geom_point,
    status, manager_name, manager_phone, notes
) VALUES 
(
    'Trung tâm Cứu trợ Hà Nội',
    'Trung tâm cứu trợ khẩn cấp chính của thành phố Hà Nội',
    'Số 1 Phố Huế, Quận Hai Bà Trưng, Hà Nội',
    500, 120,
    '024-3825-1234', 'hanoi.shelter@relief.gov.vn',
    ARRAY['wifi', 'medical', 'kitchen', 'parking', 'security'],
    21.0285, 105.8542,
    ST_SetSRID(ST_MakePoint(105.8542, 21.0285), 4326),
    'ACTIVE', 'Nguyễn Văn An', '0912-345-678',
    'Trung tâm chính, có đầy đủ tiện nghi và nhân viên y tế'
),
(
    'Nhà thi đấu Quận Đống Đa',
    'Nhà thi đấu được chuyển đổi thành nơi trú ẩn khẩn cấp',
    'Phố Tây Sơn, Quận Đống Đa, Hà Nội',
    300, 85,
    '024-3851-9876', 'dongda.shelter@hanoi.gov.vn',
    ARRAY['wifi', 'kitchen', 'parking'],
    21.0245, 105.8302,
    ST_SetSRID(ST_MakePoint(105.8302, 21.0245), 4326),
    'ACTIVE', 'Trần Thị Bình', '0987-654-321',
    'Không gian rộng, phù hợp cho số lượng lớn'
),
(
    'Trường THPT Chu Văn An',
    'Trường học được sử dụng làm nơi trú ẩn tạm thời',
    'Phố Duy Tân, Quận Cầu Giấy, Hà Nội',
    200, 45,
    '024-3755-4321', 'chuvan.shelter@edu.hanoi.gov.vn',
    ARRAY['wifi', 'medical', 'parking'],
    21.0311, 105.7981,
    ST_SetSRID(ST_MakePoint(105.7981, 21.0311), 4326),
    'ACTIVE', 'Lê Văn Cường', '0901-234-567',
    'Có sân rộng, phòng học có thể chuyển đổi thành phòng nghỉ'
),
(
    'Trung tâm Văn hóa Hoàn Kiếm',
    'Trung tâm văn hóa phục vụ như nơi trú ẩn khẩn cấp',
    'Phố Lý Thái Tổ, Quận Hoàn Kiếm, Hà Nội',
    150, 150,
    '024-3828-7654', 'hoankiem.culture@hanoi.gov.vn',
    ARRAY['wifi', 'kitchen'],
    21.0278, 105.8525,
    ST_SetSRID(ST_MakePoint(105.8525, 21.0278), 4326),
    'FULL', 'Phạm Thị Dung', '0913-456-789',
    'Hiện tại đã đầy, cần chuyển hướng đến nơi khác'
),
(
    'Nhà Văn hóa Thanh Xuân',
    'Nhà văn hóa khu vực phía nam Hà Nội',
    'Đường Nguyễn Trãi, Quận Thanh Xuân, Hà Nội',
    250, 0,
    '024-3858-1111', 'thanhxuan.culture@hanoi.gov.vn',
    ARRAY['wifi', 'medical', 'kitchen', 'parking'],
    20.9955, 105.8077,
    ST_SetSRID(ST_MakePoint(105.8077, 20.9955), 4326),
    'MAINTENANCE', 'Hoàng Văn Đức', '0945-678-901',
    'Đang bảo trì hệ thống điện, dự kiến hoạt động lại sau 2 ngày'
);