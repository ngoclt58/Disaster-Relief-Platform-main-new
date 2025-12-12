package com.relief.media;

import com.relief.entity.Media;
import com.relief.entity.User;
import com.relief.repository.MediaRepository;
import com.relief.repository.UserRepository;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private MinioClient minioClient;

    @Value("${minio.bucket-name:relief-media}")
    private String bucketName;

    @Value("${spring.servlet.context-path:/api}")
    private String contextPath;

    @Value("${storage.type:filesystem}")
    private String storageType;

    @Value("${storage.filesystem.path:./storage/media}")
    private String filesystemPath;

    public String createPresignedPutUrl(String objectName, String contentType) {
        // Use file system storage if MinIO is not available
        if (minioClient == null || "filesystem".equals(storageType)) {
            return createFileSystemUploadUrl(objectName);
        }

        try {
            // Ensure bucket exists
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Generate presigned URL (expires in 1 hour by default)
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(io.minio.http.Method.PUT)
                            .bucket(bucketName)
                            .object(objectName)
                            .extraQueryParams(Map.of("Content-Type", contentType))
                            .build()
            );
        } catch (Exception e) {
            // Fallback to file system if MinIO fails
            return createFileSystemUploadUrl(objectName);
        }
    }

    private String createFileSystemUploadUrl(String objectName) {
        // Return direct upload endpoint for file system storage
        try {
            String encoded = java.net.URLEncoder.encode(objectName, java.nio.charset.StandardCharsets.UTF_8);
            return contextPath + "/media/upload-direct?objectName=" + encoded;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create file system upload URL: " + e.getMessage(), e);
        }
    }

    public String getPresignedUploadUrl(String objectName, String contentType, String userId) {
        // Add user-specific prefix to object name for organization
        String userObjectName = userId + "/" + objectName;
        return createPresignedPutUrl(userObjectName, contentType);
    }

    @Transactional
    public Media createMediaRecord(CreateMediaRequest request, UUID userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // Determine media type from content type or file extension
        String mediaType = determineMediaType(request.getContentType(), request.getFileName());
        
        // Build media URL from object name
        String mediaUrl = buildMediaUrl(request.getObjectName());

        Media.Builder mediaBuilder = Media.builder()
                .ownerUser(owner)
                .type(mediaType)
                .url(mediaUrl)
                .redacted(false);

        // Set taken_at if provided
        if (request.getTakenAt() != null && !request.getTakenAt().isEmpty()) {
            try {
                LocalDateTime takenAt = LocalDateTime.parse(request.getTakenAt(), 
                    DateTimeFormatter.ISO_DATE_TIME);
                mediaBuilder.takenAt(takenAt);
            } catch (Exception e) {
                // If parsing fails, ignore takenAt
            }
        }

        // Set location if provided
        if (request.getLocation() != null && 
            request.getLocation().getLatitude() != null && 
            request.getLocation().getLongitude() != null) {
            Point point = geometryFactory.createPoint(
                new Coordinate(
                    request.getLocation().getLongitude(),
                    request.getLocation().getLatitude()
                )
            );
            mediaBuilder.geomPoint(point);
        }

        return mediaRepository.save(mediaBuilder.build());
    }

    private String determineMediaType(String contentType, String fileName) {
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                return "image";
            } else if (contentType.startsWith("video/")) {
                return "video";
            } else if (contentType.startsWith("audio/")) {
                return "audio";
            } else if (contentType.contains("pdf") || 
                      contentType.contains("document") ||
                      contentType.contains("text")) {
                return "document";
            }
        }
        
        if (fileName != null) {
            String extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            if (extension.matches("jpg|jpeg|png|gif|webp|bmp|svg")) {
                return "image";
            } else if (extension.matches("mp4|avi|mov|wmv|flv|webm|mkv")) {
                return "video";
            } else if (extension.matches("mp3|wav|ogg|aac|flac")) {
                return "audio";
            } else {
                return "document";
            }
        }
        
        return "document"; // default
    }

    private String buildMediaUrl(String objectName) {
        // For file system storage, use the file endpoint
        if ("filesystem".equals(storageType) || minioClient == null) {
            return contextPath + "/media/file/" + objectName;
        }
        // For MinIO, use the object name as URL
        String basePath = contextPath.equals("/api") ? "" : contextPath.replace("/api", "");
        return basePath + "/media/" + objectName;
    }

    /**
     * Save file to file system storage
     */
    public String saveFileToFileSystem(MultipartFile file, String objectName, String userId) throws IOException {
        Path storageDir = Paths.get(filesystemPath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Create user-specific directory
        Path userDir = storageDir.resolve(userId);
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        // Save file
        Path filePath = userDir.resolve(objectName);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for URL
        return userId + "/" + objectName;
    }

    /**
     * Get file system URL for an object
     */
    public String getFileSystemUrl(String objectName) {
        return contextPath + "/media/file/" + objectName;
    }

    /**
     * Get file resource from file system
     */
    public Resource getFileSystemResource(String objectName) throws IOException {
        Path filePath = Paths.get(filesystemPath).resolve(objectName);
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new IOException("File not found: " + objectName);
        }
    }

    // Inner class for request DTO
    public static class CreateMediaRequest {
        private String objectName;
        private String contentType;
        private String fileName;
        private Long fileSize;
        private String takenAt;
        private LocationData location;

        // Getters and setters
        public String getObjectName() { return objectName; }
        public void setObjectName(String objectName) { this.objectName = objectName; }
        
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getTakenAt() { return takenAt; }
        public void setTakenAt(String takenAt) { this.takenAt = takenAt; }
        
        public LocationData getLocation() { return location; }
        public void setLocation(LocationData location) { this.location = location; }

        public static class LocationData {
            private Double latitude;
            private Double longitude;

            public Double getLatitude() { return latitude; }
            public void setLatitude(Double latitude) { this.latitude = latitude; }
            
            public Double getLongitude() { return longitude; }
            public void setLongitude(Double longitude) { this.longitude = longitude; }
        }
    }
}



