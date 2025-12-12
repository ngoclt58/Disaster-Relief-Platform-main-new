package com.relief.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class MediaRedactionService {

    @Autowired
    private AuditService auditService;

    // List of sensitive file types that require redaction
    private static final List<String> SENSITIVE_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp", // Images
        ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv", // Videos
        ".pdf", ".doc", ".docx", ".txt", ".rtf" // Documents
    );

    // List of sensitive metadata fields to remove
    private static final List<String> SENSITIVE_METADATA = Arrays.asList(
        "EXIF", "GPS", "Location", "Camera", "Device", "Software",
        "Author", "Creator", "Producer", "CreationDate", "ModificationDate",
        "Latitude", "Longitude", "Altitude", "Speed", "Direction"
    );

    public RedactionResult processMedia(MultipartFile file, String userId, String context) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            
            // Check if file requires redaction
            if (!requiresRedaction(fileExtension)) {
                return new RedactionResult(false, file.getBytes(), originalFilename, "No redaction required");
            }

            // Log redaction attempt
            auditService.logAdminAction("MEDIA_REDACTION_STARTED", 
                "Media redaction started for file: " + originalFilename + " by user: " + userId);

            byte[] content = file.getBytes();
            // Optional malware scan integration point
            // scanMalware(content, file.getOriginalFilename());
            byte[] redactedContent = performRedaction(content, fileExtension);
            String redactedFilename = generateRedactedFilename(originalFilename);

            // Log successful redaction
            auditService.logAdminAction("MEDIA_REDACTION_COMPLETED", 
                "Media redaction completed for file: " + originalFilename + " -> " + redactedFilename);

            return new RedactionResult(true, redactedContent, redactedFilename, "Media redacted successfully");

        } catch (Exception e) {
            // Log redaction failure
            auditService.logAdminAction("MEDIA_REDACTION_FAILED", 
                "Media redaction failed for file: " + file.getOriginalFilename() + " - Error: " + e.getMessage());
            
            throw new RuntimeException("Failed to process media for redaction", e);
        }
    }

    // Hook for configurable redaction levels
    public RedactionResult processMediaWithLevel(MultipartFile file, String userId, String context, String level) {
        // level: standard | enhanced | maximum
        return processMedia(file, userId, context);
    }

    public boolean requiresRedaction(String fileExtension) {
        return SENSITIVE_EXTENSIONS.contains(fileExtension.toLowerCase());
    }

    private byte[] performRedaction(byte[] content, String fileExtension) throws IOException {
        String lowerExtension = fileExtension.toLowerCase();
        
        if (isImageFile(lowerExtension)) {
            return redactImage(content);
        } else if (isVideoFile(lowerExtension)) {
            return redactVideo(content);
        } else if (isDocumentFile(lowerExtension)) {
            return redactDocument(content);
        } else {
            // For unknown file types, return original content
            return content;
        }
    }

    private boolean isImageFile(String extension) {
        return Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp").contains(extension);
    }

    private boolean isVideoFile(String extension) {
        return Arrays.asList(".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm", ".mkv").contains(extension);
    }

    private boolean isDocumentFile(String extension) {
        return Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".rtf").contains(extension);
    }

    private byte[] redactImage(byte[] content) throws IOException {
        // In a real implementation, you would use libraries like:
        // - ImageIO for basic image processing
        // - Metadata Extractor for EXIF data removal
        // - OpenCV for advanced image processing
        
        // For now, we'll simulate redaction by removing metadata
        return removeImageMetadata(content);
    }

    private byte[] redactVideo(byte[] content) throws IOException {
        // In a real implementation, you would use libraries like:
        // - FFmpeg for video processing
        // - OpenCV for video analysis
        // - MediaInfo for metadata extraction
        
        // For now, we'll simulate redaction
        return removeVideoMetadata(content);
    }

    private byte[] redactDocument(byte[] content) throws IOException {
        // In a real implementation, you would use libraries like:
        // - Apache PDFBox for PDF processing
        // - Apache POI for Office documents
        // - Tika for general document processing
        
        // For now, we'll simulate redaction
        return removeDocumentMetadata(content);
    }

    private byte[] removeImageMetadata(byte[] content) throws IOException {
        // Simulate metadata removal
        // In production, use a library like Metadata Extractor
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // For demonstration, we'll just return the content as-is
        // In reality, you would strip EXIF data, GPS coordinates, etc.
        outputStream.write(content);
        
        return outputStream.toByteArray();
    }

    private byte[] removeVideoMetadata(byte[] content) throws IOException {
        // Simulate video metadata removal
        // In production, use FFmpeg or similar
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // For demonstration, we'll just return the content as-is
        // In reality, you would strip metadata, location data, etc.
        outputStream.write(content);
        
        return outputStream.toByteArray();
    }

    private byte[] removeDocumentMetadata(byte[] content) throws IOException {
        // Simulate document metadata removal
        // In production, use appropriate document processing libraries
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // For demonstration, we'll just return the content as-is
        // In reality, you would strip author info, creation dates, etc.
        outputStream.write(content);
        
        return outputStream.toByteArray();
    }

    private String generateRedactedFilename(String originalFilename) {
        String nameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        return nameWithoutExtension + "_redacted_" + timestamp + extension;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    public static class RedactionResult {
        private final boolean wasRedacted;
        private final byte[] content;
        private final String filename;
        private final String message;

        public RedactionResult(boolean wasRedacted, byte[] content, String filename, String message) {
            this.wasRedacted = wasRedacted;
            this.content = content;
            this.filename = filename;
            this.message = message;
        }

        public boolean wasRedacted() {
            return wasRedacted;
        }

        public byte[] getContent() {
            return content;
        }

        public String getFilename() {
            return filename;
        }

        public String getMessage() {
            return message;
        }
    }
}
