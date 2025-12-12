package com.relief.dto;

import lombok.Data;

@Data
public class RequestsFilter {
    private String status; // new/assigned/...
    private String type; // food/water/...
    private Integer minSeverity;
    private String from; // ISO date
    private String to;   // ISO date
    private String bbox; // POLYGON WKT or bbox "minx,miny,maxx,maxy"

    // Explicit getters and setters for Lombok compatibility
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getMinSeverity() { return minSeverity; }
    public void setMinSeverity(Integer minSeverity) { this.minSeverity = minSeverity; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getBbox() { return bbox; }
    public void setBbox(String bbox) { this.bbox = bbox; }
}





