package com.relief.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class TimeSeriesData {
    private LocalDateTime timestamp;
    private String label;
    private long value;
    private String category;
    private Map<String, Object> metadata;
    
    // Constructors
    public TimeSeriesData() {}
    
    public TimeSeriesData(LocalDateTime timestamp, String label, long value) {
        this.timestamp = timestamp;
        this.label = label;
        this.value = value;
    }
    
    public TimeSeriesData(LocalDateTime timestamp, String label, long value, String category) {
        this.timestamp = timestamp;
        this.label = label;
        this.value = value;
        this.category = category;
    }
    
    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public long getValue() {
        return value;
    }
    
    public void setValue(long value) {
        this.value = value;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}



