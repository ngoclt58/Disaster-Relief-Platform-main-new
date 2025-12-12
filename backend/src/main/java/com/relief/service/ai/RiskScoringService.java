package com.relief.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoringService {

    private final Map<String, RiskScore> scores = new ConcurrentHashMap<>();

    public RiskScore calculateRiskScore(RiskInput input) {
        RiskScore score = new RiskScore();
        score.setId(UUID.randomUUID().toString());
        score.setCalculatedAt(LocalDateTime.now());
        score.setInput(input);
        
        // Calculate overall risk score
        double overallRisk = calculateOverallRisk(input);
        score.setOverallRisk(overallRisk);
        score.setRiskLevel(determineRiskLevel(overallRisk));
        
        // Calculate category-specific risks
        score.setGeographicRisk(calculateGeographicRisk(input));
        score.setWeatherRisk(calculateWeatherRisk(input));
        score.setPopulationRisk(calculatePopulationRisk(input));
        score.setInfrastructureRisk(calculateInfrastructureRisk(input));
        score.setHistoricalRisk(calculateHistoricalRisk(input));
        
        // Generate risk factors
        score.setRiskFactors(generateRiskFactors(input, overallRisk));
        score.setRecommendations(generateRecommendations(score));
        
        scores.put(score.getId(), score);
        
        log.info("Risk score calculated: overall={}, level={}", overallRisk, score.getRiskLevel());
        return score;
    }

    public List<RiskScore> getRiskScores(LocalDateTime startTime, LocalDateTime endTime) {
        return scores.values().stream()
                .filter(s -> s.getCalculatedAt().isAfter(startTime) && s.getCalculatedAt().isBefore(endTime))
                .sorted((a, b) -> b.getCalculatedAt().compareTo(a.getCalculatedAt()))
                .collect(Collectors.toList());
    }

    public RiskScore getRiskScore(String scoreId) {
        return scores.get(scoreId);
    }

    public RiskComparison compareRiskScores(List<String> scoreIds) {
        RiskComparison comparison = new RiskComparison();
        comparison.setComparedAt(LocalDateTime.now());
        
        List<RiskScore> scoresToCompare = scoreIds.stream()
                .map(scores::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (scoresToCompare.isEmpty()) {
            return comparison;
        }
        
        comparison.setHighestRisk(scoresToCompare.stream()
                .max(Comparator.comparing(RiskScore::getOverallRisk))
                .orElse(null));
        
        comparison.setLowestRisk(scoresToCompare.stream()
                .min(Comparator.comparing(RiskScore::getOverallRisk))
                .orElse(null));
        
        comparison.setAverageRisk(scoresToCompare.stream()
                .mapToDouble(RiskScore::getOverallRisk)
                .average()
                .orElse(0.0));
        
        return comparison;
    }

    private double calculateOverallRisk(RiskInput input) {
        double risk = 0.0;
        
        risk += calculateGeographicRisk(input) * 0.3;
        risk += calculateWeatherRisk(input) * 0.25;
        risk += calculatePopulationRisk(input) * 0.2;
        risk += calculateInfrastructureRisk(input) * 0.15;
        risk += calculateHistoricalRisk(input) * 0.1;
        
        return Math.min(100.0, Math.max(0.0, risk));
    }

    private double calculateGeographicRisk(RiskInput input) {
        double risk = 0.0;
        
        if (input.getGeographicData() != null) {
            // Elevation risk
            if (input.getGeographicData().containsKey("elevation")) {
                int elevation = (int) input.getGeographicData().get("elevation");
                if (elevation < 50) risk += 20; // Low elevation (flood risk)
                else if (elevation < 200) risk += 10;
            }
            
            // Coastal risk
            if (input.getGeographicData().containsKey("isCoastal")) {
                if ((boolean) input.getGeographicData().get("isCoastal")) risk += 25;
            }
            
            // Fault line proximity
            if (input.getGeographicData().containsKey("nearFaultLine")) {
                if ((boolean) input.getGeographicData().get("nearFaultLine")) risk += 30;
            }
            
            // Slope risk
            if (input.getGeographicData().containsKey("slopeAngle")) {
                int slope = (int) input.getGeographicData().get("slopeAngle");
                if (slope > 30) risk += 15; // Steep slope (landslide risk)
            }
        }
        
        return Math.min(100.0, risk);
    }

    private double calculateWeatherRisk(RiskInput input) {
        double risk = 0.0;
        
        if (input.getWeatherData() != null) {
            // Precipitation
            if (input.getWeatherData().containsKey("precipitation")) {
                double precip = (double) input.getWeatherData().get("precipitation");
                if (precip > 100) risk += 30; // Heavy rain
                else if (precip > 50) risk += 20;
            }
            
            // Wind speed
            if (input.getWeatherData().containsKey("windSpeed")) {
                int windSpeed = (int) input.getWeatherData().get("windSpeed");
                if (windSpeed > 60) risk += 40; // Hurricane force winds
                else if (windSpeed > 40) risk += 25;
                else if (windSpeed > 25) risk += 15;
            }
            
            // Temperature extremes
            if (input.getWeatherData().containsKey("temperature")) {
                int temp = (int) input.getWeatherData().get("temperature");
                if (temp > 40 || temp < -20) risk += 20;
            }
        }
        
        return Math.min(100.0, risk);
    }

    private double calculatePopulationRisk(RiskInput input) {
        double risk = 0.0;
        
        if (input.getPopulationData() != null) {
            // Population density
            if (input.getPopulationData().containsKey("density")) {
                int density = (int) input.getPopulationData().get("density");
                if (density > 1000) risk += 30;
                else if (density > 500) risk += 20;
                else if (density > 100) risk += 10;
            }
            
            // Vulnerable populations
            if (input.getPopulationData().containsKey("vulnerablePercentage")) {
                double vulnerable = (double) input.getPopulationData().get("vulnerablePercentage");
                risk += vulnerable * 0.5;
            }
        }
        
        return Math.min(100.0, risk);
    }

    private double calculateInfrastructureRisk(RiskInput input) {
        double risk = 0.0;
        
        if (input.getInfrastructureData() != null) {
            // Critical facilities
            if (input.getInfrastructureData().containsKey("criticalFacilities")) {
                int facilities = (int) input.getInfrastructureData().get("criticalFacilities");
                risk += facilities * 2;
            }
            
            // Age of infrastructure
            if (input.getInfrastructureData().containsKey("averageAge")) {
                int age = (int) input.getInfrastructureData().get("averageAge");
                if (age > 50) risk += 20;
                else if (age > 30) risk += 10;
            }
        }
        
        return Math.min(100.0, risk);
    }

    private double calculateHistoricalRisk(RiskInput input) {
        double risk = 0.0;
        
        if (input.getHistoricalData() != null) {
            // Past incidents
            if (input.getHistoricalData().containsKey("pastIncidents")) {
                int incidents = (int) input.getHistoricalData().get("pastIncidents");
                risk += incidents * 3;
            }
            
            // Last incident recency
            if (input.getHistoricalData().containsKey("lastIncidentYearsAgo")) {
                int years = (int) input.getHistoricalData().get("lastIncidentYearsAgo");
                if (years < 1) risk += 30;
                else if (years < 5) risk += 20;
                else if (years < 10) risk += 10;
            }
        }
        
        return Math.min(100.0, risk);
    }

    private String determineRiskLevel(double riskScore) {
        if (riskScore >= 80) return "CRITICAL";
        if (riskScore >= 60) return "HIGH";
        if (riskScore >= 40) return "MODERATE";
        if (riskScore >= 20) return "LOW";
        return "VERY_LOW";
    }

    private List<RiskFactor> generateRiskFactors(RiskInput input, double overallRisk) {
        List<RiskFactor> factors = new ArrayList<>();
        
        if (calculateGeographicRisk(input) > 40) {
            factors.add(new RiskFactor("Geographic", calculateGeographicRisk(input), "High geographic risk detected"));
        }
        
        if (calculateWeatherRisk(input) > 40) {
            factors.add(new RiskFactor("Weather", calculateWeatherRisk(input), "Severe weather conditions"));
        }
        
        if (calculatePopulationRisk(input) > 40) {
            factors.add(new RiskFactor("Population", calculatePopulationRisk(input), "High population density"));
        }
        
        if (calculateInfrastructureRisk(input) > 40) {
            factors.add(new RiskFactor("Infrastructure", calculateInfrastructureRisk(input), "Aging or inadequate infrastructure"));
        }
        
        return factors;
    }

    private List<String> generateRecommendations(RiskScore score) {
        List<String> recommendations = new ArrayList<>();
        
        if (score.getOverallRisk() >= 80) {
            recommendations.add("Issue immediate evacuation warnings");
            recommendations.add("Deploy emergency response teams");
            recommendations.add("Activate emergency operations center");
        } else if (score.getOverallRisk() >= 60) {
            recommendations.add("Prepare for potential emergency");
            recommendations.add("Review evacuation plans");
            recommendations.add("Stock up emergency supplies");
        } else if (score.getOverallRisk() >= 40) {
            recommendations.add("Monitor situation closely");
            recommendations.add("Update emergency plans");
        } else {
            recommendations.add("Maintain normal operations");
            recommendations.add("Continue routine monitoring");
        }
        
        return recommendations;
    }

    // Data classes
    public static class RiskScore {
        private String id;
        private RiskInput input;
        private LocalDateTime calculatedAt;
        private double overallRisk;
        private String riskLevel;
        private double geographicRisk;
        private double weatherRisk;
        private double populationRisk;
        private double infrastructureRisk;
        private double historicalRisk;
        private List<RiskFactor> riskFactors;
        private List<String> recommendations;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public RiskInput getInput() { return input; }
        public void setInput(RiskInput input) { this.input = input; }

        public LocalDateTime getCalculatedAt() { return calculatedAt; }
        public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }

        public double getOverallRisk() { return overallRisk; }
        public void setOverallRisk(double overallRisk) { this.overallRisk = overallRisk; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public double getGeographicRisk() { return geographicRisk; }
        public void setGeographicRisk(double geographicRisk) { this.geographicRisk = geographicRisk; }

        public double getWeatherRisk() { return weatherRisk; }
        public void setWeatherRisk(double weatherRisk) { this.weatherRisk = weatherRisk; }

        public double getPopulationRisk() { return populationRisk; }
        public void setPopulationRisk(double populationRisk) { this.populationRisk = populationRisk; }

        public double getInfrastructureRisk() { return infrastructureRisk; }
        public void setInfrastructureRisk(double infrastructureRisk) { this.infrastructureRisk = infrastructureRisk; }

        public double getHistoricalRisk() { return historicalRisk; }
        public void setHistoricalRisk(double historicalRisk) { this.historicalRisk = historicalRisk; }

        public List<RiskFactor> getRiskFactors() { return riskFactors; }
        public void setRiskFactors(List<RiskFactor> riskFactors) { this.riskFactors = riskFactors; }

        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    }

    public static class RiskInput {
        private Map<String, Object> geographicData;
        private Map<String, Object> weatherData;
        private Map<String, Object> populationData;
        private Map<String, Object> infrastructureData;
        private Map<String, Object> historicalData;

        // Getters and setters
        public Map<String, Object> getGeographicData() { return geographicData; }
        public void setGeographicData(Map<String, Object> geographicData) { this.geographicData = geographicData; }

        public Map<String, Object> getWeatherData() { return weatherData; }
        public void setWeatherData(Map<String, Object> weatherData) { this.weatherData = weatherData; }

        public Map<String, Object> getPopulationData() { return populationData; }
        public void setPopulationData(Map<String, Object> populationData) { this.populationData = populationData; }

        public Map<String, Object> getInfrastructureData() { return infrastructureData; }
        public void setInfrastructureData(Map<String, Object> infrastructureData) { this.infrastructureData = infrastructureData; }

        public Map<String, Object> getHistoricalData() { return historicalData; }
        public void setHistoricalData(Map<String, Object> historicalData) { this.historicalData = historicalData; }
    }

    public static class RiskFactor {
        private String category;
        private double score;
        private String description;

        public RiskFactor(String category, double score, String description) {
            this.category = category;
            this.score = score;
            this.description = description;
        }

        // Getters and setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class RiskComparison {
        private LocalDateTime comparedAt;
        private RiskScore highestRisk;
        private RiskScore lowestRisk;
        private double averageRisk;

        // Getters and setters
        public LocalDateTime getComparedAt() { return comparedAt; }
        public void setComparedAt(LocalDateTime comparedAt) { this.comparedAt = comparedAt; }

        public RiskScore getHighestRisk() { return highestRisk; }
        public void setHighestRisk(RiskScore highestRisk) { this.highestRisk = highestRisk; }

        public RiskScore getLowestRisk() { return lowestRisk; }
        public void setLowestRisk(RiskScore lowestRisk) { this.lowestRisk = lowestRisk; }

        public double getAverageRisk() { return averageRisk; }
        public void setAverageRisk(double averageRisk) { this.averageRisk = averageRisk; }
    }
}

