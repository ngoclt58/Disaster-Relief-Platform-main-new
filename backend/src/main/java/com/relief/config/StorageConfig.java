package com.relief.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Storage Configuration
 * Supports both MinIO (production) and File System (development) storage
 */
@Configuration
public class StorageConfig {

    @Value("${storage.type:filesystem}")
    private String storageType;

    @Value("${storage.filesystem.path:./storage/media}")
    private String filesystemPath;

    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.type", havingValue = "filesystem", matchIfMissing = true)
    public StorageService fileSystemStorageService() {
        Path storagePath = Paths.get(filesystemPath);
        storagePath.toFile().mkdirs();
        return new FileSystemStorageService(storagePath);
    }

    /**
     * Storage service interface
     */
    public interface StorageService {
        String generateUploadUrl(String objectName, String contentType);
        String getFileUrl(String objectName);
        void saveFile(String objectName, byte[] data) throws Exception;
        boolean fileExists(String objectName);
    }

    /**
     * File System Storage Implementation (for development)
     */
    public static class FileSystemStorageService implements StorageService {
        private final Path storagePath;

        public FileSystemStorageService(Path storagePath) {
            this.storagePath = storagePath;
            storagePath.toFile().mkdirs();
        }

        @Override
        public String generateUploadUrl(String objectName, String contentType) {
            // For file system, return a direct upload endpoint
            // The MediaController will handle the actual upload
            return "/api/media/upload-direct?objectName=" + 
                   java.net.URLEncoder.encode(objectName, java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public String getFileUrl(String objectName) {
            return "/api/media/file/" + 
                   java.net.URLEncoder.encode(objectName, java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public void saveFile(String objectName, byte[] data) throws Exception {
            Path filePath = storagePath.resolve(objectName);
            filePath.getParent().toFile().mkdirs();
            java.nio.file.Files.write(filePath, data);
        }

        @Override
        public boolean fileExists(String objectName) {
            return storagePath.resolve(objectName).toFile().exists();
        }
    }
}


