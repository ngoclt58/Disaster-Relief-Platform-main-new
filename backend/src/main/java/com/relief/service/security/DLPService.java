package com.relief.service.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DLPService {

    private static final Logger log = LoggerFactory.getLogger(DLPService.class);

    private static final Pattern SSN = Pattern.compile("\\b(?!000|666)[0-8][0-9]{2}-?(?!00)[0-9]{2}-?(?!0000)[0-9]{4}\\b");
    private static final Pattern CREDIT_CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}");

    public DLPScanResult scan(String content, Map<String, Object> options) {
        DLPScanResult result = new DLPScanResult();
        result.setId(UUID.randomUUID().toString());
        result.setScannedAt(LocalDateTime.now());
        result.setFindings(new ArrayList<>());

        if (content == null || content.isEmpty()) {
            result.setRiskLevel("LOW");
            return result;
        }

        checkPattern(content, SSN, "SSN", result);
        checkPattern(content, CREDIT_CARD, "CREDIT_CARD", result);
        checkPattern(content, EMAIL, "EMAIL", result);

        String level = result.getFindings().isEmpty() ? "LOW" : result.getFindings().size() > 3 ? "HIGH" : "MEDIUM";
        result.setRiskLevel(level);
        return result;
    }

    private void checkPattern(String content, Pattern pattern, String type, DLPScanResult result) {
        var m = pattern.matcher(content);
        while (m.find()) {
            var f = new DLPFinding();
            f.setType(type);
            f.setExcerpt(excerpt(content, m.start(), m.end()));
            f.setStart(m.start());
            f.setEnd(m.end());
            result.getFindings().add(f);
        }
    }

    private String excerpt(String content, int start, int end) {
        int s = Math.max(0, start - 10);
        int e = Math.min(content.length(), end + 10);
        return content.substring(s, e);
    }

    @lombok.Data
    public static class DLPScanResult {
        private String id;
        private String riskLevel;
        private List<DLPFinding> findings;
        private LocalDateTime scannedAt;
    }

    @lombok.Data
    public static class DLPFinding {
        private String type;
        private String excerpt;
        private int start;
        private int end;
    }
}




