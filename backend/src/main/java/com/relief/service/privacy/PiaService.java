package com.relief.service.privacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PiaService {

    private final Map<String, Pia> pias = new ConcurrentHashMap<>();

    public Pia create(String name, String description, Map<String, Object> context) {
        Pia p = new Pia();
        p.setId(UUID.randomUUID().toString());
        p.setName(name);
        p.setDescription(description);
        p.setContext(context != null ? context : new HashMap<>());
        p.setRiskScore(assessRisk(p.getContext()));
        p.setRecommendations(generateRecommendations(p.getRiskScore()))
;        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        pias.put(p.getId(), p);
        return p;
    }

    public Pia update(String id, Map<String, Object> context) {
        Pia p = pias.get(id);
        if (p == null) throw new IllegalArgumentException("PIA not found");
        p.setContext(context != null ? context : new HashMap<>());
        p.setRiskScore(assessRisk(p.getContext()));
        p.setRecommendations(generateRecommendations(p.getRiskScore()));
        p.setUpdatedAt(LocalDateTime.now());
        return p;
    }

    public List<Pia> list() {
        return new ArrayList<>(pias.values());
    }

    private double assessRisk(Map<String, Object> ctx) {
        double base = 0.3;
        if (Boolean.TRUE.equals(ctx.get("usesPII"))) base += 0.4;
        if (Boolean.TRUE.equals(ctx.get("thirdParties"))) base += 0.2;
        if (Boolean.TRUE.equals(ctx.get("crossBorder"))) base += 0.1;
        return Math.min(1.0, base);
    }

    private List<String> generateRecommendations(double risk) {
        List<String> r = new ArrayList<>();
        if (risk >= 0.7) {
            r.add("Perform DPIA and consult DPO");
            r.add("Minimize data collection and apply strong encryption");
        } else if (risk >= 0.5) {
            r.add("Implement pseudonymization and data minimization");
            r.add("Review vendor contracts for data protection obligations");
        } else {
            r.add("Ensure consent and purpose limitation are enforced");
        }
        return r;
    }

    @lombok.Data
    public static class Pia {
        private String id;
        private String name;
        private String description;
        private Map<String, Object> context;
        private double riskScore;
        private List<String> recommendations;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}




