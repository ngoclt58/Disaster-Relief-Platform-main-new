-- Translate all shelter names and descriptions to English for US demo
UPDATE shelters SET 
    name = 'Hanoi Relief Center',
    description = 'Main emergency relief center of Hanoi city',
    notes = 'Main center with full facilities and medical staff'
WHERE name = 'Trung tâm Cứu trợ Hà Nội';

UPDATE shelters SET 
    name = 'Dong Da District Gymnasium',
    description = 'Gymnasium converted to emergency shelter',
    notes = 'Large space, suitable for large numbers of people'
WHERE name = 'Nhà thi đấu Quận Đống Đa';

UPDATE shelters SET 
    name = 'Chu Van An High School',
    description = 'School used as temporary shelter',
    notes = 'Has large courtyard, classrooms can be converted to sleeping areas'
WHERE name = 'Trường THPT Chu Văn An';

UPDATE shelters SET 
    name = 'Hoan Kiem Cultural Center',
    description = 'Cultural center serving as emergency shelter',
    notes = 'Currently at full capacity, need to redirect to other locations'
WHERE name = 'Trung tâm Văn hóa Hoàn Kiếm';

UPDATE shelters SET 
    name = 'Thanh Xuan Cultural House',
    description = 'Cultural house in southern Hanoi area',
    notes = 'Under electrical system maintenance, expected to resume operations in 2 days'
WHERE name = 'Nhà Văn hóa Thanh Xuân';

UPDATE shelters SET 
    name = 'My Dinh Sports Center',
    description = 'Large sports center converted to emergency shelter',
    notes = 'Large sports center with stadium and many facilities'
WHERE name = 'Trung tâm Thể thao Mỹ Đình';

UPDATE shelters SET 
    name = 'Dong Da General Hospital',
    description = 'Hospital temporary area for emergency shelter',
    notes = 'Has professional medical staff, suitable for patients and elderly'
WHERE name = 'Bệnh viện Đa khoa Đống Đa';

UPDATE shelters SET 
    name = 'National Convention Center',
    description = 'Large convention center used as temporary shelter',
    notes = 'Very spacious, can accommodate large numbers of people'
WHERE name = 'Trung tâm Hội nghị Quốc gia';

-- Update manager names to English
UPDATE shelters SET manager_name = 'John Smith' WHERE manager_name = 'Nguyễn Văn An';
UPDATE shelters SET manager_name = 'Sarah Johnson' WHERE manager_name = 'Trần Thị Bình';
UPDATE shelters SET manager_name = 'Michael Brown' WHERE manager_name = 'Lê Văn Cường';
UPDATE shelters SET manager_name = 'Emily Davis' WHERE manager_name = 'Phạm Thị Dung';
UPDATE shelters SET manager_name = 'David Wilson' WHERE manager_name = 'Hoàng Văn Đức';
UPDATE shelters SET manager_name = 'Lisa Anderson' WHERE manager_name = 'Nguyễn Thị Lan';
UPDATE shelters SET manager_name = 'Dr. Robert Miller' WHERE manager_name = 'Bác sĩ Trần Văn Minh';
UPDATE shelters SET manager_name = 'Jennifer Taylor' WHERE manager_name = 'Lê Thị Hương';