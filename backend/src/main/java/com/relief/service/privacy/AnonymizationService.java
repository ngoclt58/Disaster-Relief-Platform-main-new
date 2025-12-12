package com.relief.service.privacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnonymizationService {

    public Map<String, Object> kAnonymize(Map<String, Object> record, Set<String> quasiIdentifiers, int k) {
        Map<String, Object> out = new HashMap<>(record);
        for (String key : quasiIdentifiers) {
            Object v = out.get(key);
            if (v instanceof String s) {
                out.put(key, generalizeString(s));
            } else if (v instanceof Number n) {
                out.put(key, generalizeNumber(n));
            }
        }
        out.put("_k", Math.max(k, 2));
        return out;
    }

    public Map<String, Object> maskPII(Map<String, Object> record, Set<String> piiKeys) {
        Map<String, Object> out = new HashMap<>(record);
        for (String key : piiKeys) {
            if (out.containsKey(key)) {
                out.put(key, "***");
            }
        }
        return out;
    }

    private String generalizeString(String s) {
        if (s.length() <= 2) return "**";
        return s.substring(0, 2) + "***";
    }

    private String generalizeNumber(Number n) {
        long v = n.longValue();
        long bucket = (v / 10) * 10;
        return bucket + "+";
    }
}




