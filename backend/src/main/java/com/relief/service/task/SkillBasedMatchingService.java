package com.relief.service.task;

import com.relief.entity.Task;
import com.relief.entity.User;
import com.relief.repository.TaskRepository;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for skill-based matching of helpers with tasks
 */
@Service
@RequiredArgsConstructor
public class SkillBasedMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SkillBasedMatchingService.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyService taskDependencyService;

    /**
     * Find the best match for a task based on skills and availability
     */
    @Transactional
    public User findBestMatch(Task task) {
        log.info("Finding best match for task: {}", task.getId());
        
        // Get all available helpers
        List<User> availableHelpers = userRepository.findByRoleAndNotDisabled("HELPER");
        
        if (availableHelpers.isEmpty()) {
            log.warn("No available helpers found for task {}", task.getId());
            return null;
        }
        
        // Calculate match scores for each helper
        List<HelperMatch> matches = availableHelpers.stream()
            .map(helper -> calculateMatchScore(helper, task))
            .filter(match -> match.getScore() > 0.0) // Only consider helpers with some match
            .sorted(Comparator.comparing(HelperMatch::getScore).reversed())
            .collect(Collectors.toList());
        
        if (matches.isEmpty()) {
            log.warn("No suitable helpers found for task {}", task.getId());
            return null;
        }
        
        HelperMatch bestMatch = matches.get(0);
        log.info("Best match for task {}: {} with score {}", task.getId(), bestMatch.getHelper().getEmail(), bestMatch.getScore());
        
        return bestMatch.getHelper();
    }

    /**
     * Calculate match score between helper and task
     */
    private HelperMatch calculateMatchScore(User helper, Task task) {
        double score = 0.0;
        List<String> reasons = new ArrayList<>();
        
        // Skill matching (40% of score)
        double skillScore = calculateSkillScore(helper, task);
        score += skillScore * 0.4;
        if (skillScore > 0) {
            reasons.add(String.format("Skills: %.1f%%", skillScore * 100));
        }
        
        // Availability matching (30% of score)
        double availabilityScore = calculateAvailabilityScore(helper, task);
        score += availabilityScore * 0.3;
        if (availabilityScore > 0) {
            reasons.add(String.format("Availability: %.1f%%", availabilityScore * 100));
        }
        
        // Location proximity (20% of score)
        double locationScore = calculateLocationScore(helper, task);
        score += locationScore * 0.2;
        if (locationScore > 0) {
            reasons.add(String.format("Location: %.1f%%", locationScore * 100));
        }
        
        // Performance history (10% of score)
        double performanceScore = calculatePerformanceScore(helper, task);
        score += performanceScore * 0.1;
        if (performanceScore > 0) {
            reasons.add(String.format("Performance: %.1f%%", performanceScore * 100));
        }
        
        return new HelperMatch(helper, score, reasons);
    }

    /**
     * Calculate skill matching score
     */
    private double calculateSkillScore(User helper, Task task) {
        if (task.getRequiredSkills() == null || task.getRequiredSkills().trim().isEmpty()) {
            return 1.0; // No specific skills required
        }
        
        Set<String> requiredSkills = Arrays.stream(task.getRequiredSkills().split(","))
            .map(String::trim)
            .collect(Collectors.toSet());
        
        Set<String> helperSkills = getUserSkills(helper);
        
        if (helperSkills.isEmpty()) {
            return 0.0; // Helper has no skills
        }
        
        // Calculate skill overlap
        Set<String> matchingSkills = new HashSet<>(requiredSkills);
        matchingSkills.retainAll(helperSkills);
        
        return (double) matchingSkills.size() / requiredSkills.size();
    }

    /**
     * Calculate availability score
     */
    private double calculateAvailabilityScore(User helper, Task task) {
        // Check current workload
        long activeTaskCount = taskRepository.countByAssigneeAndStatusIn(
            helper, 
            Arrays.asList("assigned", "picked_up")
        );
        
        // Check if helper is overloaded
        if (activeTaskCount >= 5) {
            return 0.0; // Helper is overloaded
        }
        
        // Calculate availability based on workload
        double availability = 1.0 - (activeTaskCount / 5.0);
        
        // Check if helper has time for this task
        if (task.getEta() != null) {
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilDeadline = java.time.Duration.between(now, task.getEta()).toHours();
            
            if (hoursUntilDeadline < 2) {
                availability *= 0.5; // Reduce score for urgent tasks
            }
        }
        
        return Math.max(0.0, availability);
    }

    /**
     * Calculate location proximity score
     */
    private double calculateLocationScore(User helper, Task task) {
        if (task.getLocation() == null || helper.getGeomPoint() == null) {
            return 0.5; // Neutral score if no location data
        }
        
        // Calculate distance between helper and task location
        double distance = calculateDistance(helper.getGeomPoint(), task.getLocation());
        
        // Score based on distance (closer = higher score)
        if (distance <= 5.0) return 1.0;      // Within 5km
        if (distance <= 15.0) return 0.8;     // Within 15km
        if (distance <= 30.0) return 0.6;     // Within 30km
        if (distance <= 50.0) return 0.4;     // Within 50km
        return 0.2;                           // Beyond 50km
    }

    /**
     * Calculate performance score based on historical data
     */
    private double calculatePerformanceScore(User helper, Task task) {
        // Get helper's task completion history
        List<Task> completedTasks = taskRepository.findByAssigneeAndStatusIn(
            helper,
            Arrays.asList("delivered", "completed")
        );
        
        if (completedTasks.isEmpty()) {
            return 0.5; // Neutral score for new helpers
        }
        
        // Calculate completion rate
        long totalTasks = completedTasks.size() + 
            taskRepository.countByAssigneeAndStatusIn(helper, Arrays.asList("cancelled", "failed"));
        
        if (totalTasks == 0) {
            return 0.5;
        }
        
        double completionRate = (double) completedTasks.size() / totalTasks;
        
        // Calculate average completion time for similar tasks
        List<Task> similarTasks = completedTasks.stream()
            .filter(t -> t.getType().equals(task.getType()))
            .collect(Collectors.toList());
        
        if (!similarTasks.isEmpty()) {
            double avgCompletionTime = similarTasks.stream()
                .mapToDouble(t -> {
                    if (t.getUpdatedAt() != null && t.getCreatedAt() != null) {
                        return java.time.Duration.between(t.getCreatedAt(), t.getUpdatedAt()).toMinutes();
                    }
                    return 0;
                })
                .average()
                .orElse(0.0);
            
            // Bonus for fast completion
            if (avgCompletionTime > 0 && task.getEta() != null) {
                long estimatedTime = java.time.Duration.between(LocalDateTime.now(), task.getEta()).toMinutes();
                if (avgCompletionTime < estimatedTime * 0.8) {
                    completionRate += 0.2; // Bonus for fast completion
                }
            }
        }
        
        return Math.min(1.0, completionRate);
    }

    /**
     * Get user skills from profile or database
     */
    private Set<String> getUserSkills(User user) {
        // In real implementation, this would come from user profile or skills database
        // For now, return mock skills based on user role and experience
        Set<String> skills = new HashSet<>();
        
        // Add role-based skills
        switch (user.getRole()) {
            case "HELPER":
                skills.addAll(Arrays.asList("DELIVERY", "CUSTOMER_SERVICE", "DRIVING"));
                break;
            case "DISPATCHER":
                skills.addAll(Arrays.asList("COMMUNICATION", "LEADERSHIP", "COORDINATION"));
                break;
            case "ADMIN":
                skills.addAll(Arrays.asList("MANAGEMENT", "COMMUNICATION", "LEADERSHIP"));
                break;
        }
        
        // Add experience-based skills
        if (user.getCreatedAt() != null) {
            long daysSinceJoined = java.time.Duration.between(user.getCreatedAt(), LocalDateTime.now()).toDays();
            if (daysSinceJoined > 30) {
                skills.add("EXPERIENCED");
            }
            if (daysSinceJoined > 90) {
                skills.add("SENIOR");
            }
        }
        
        return skills;
    }

    /**
     * Calculate distance between two points in kilometers
     */
    private double calculateDistance(org.locationtech.jts.geom.Point point1, org.locationtech.jts.geom.Point point2) {
        double lat1 = Math.toRadians(point1.getY());
        double lon1 = Math.toRadians(point1.getX());
        double lat2 = Math.toRadians(point2.getY());
        double lon2 = Math.toRadians(point2.getX());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371.0 * c; // Earth radius in kilometers
    }

    /**
     * Auto-assign tasks to best matching helpers
     */
    @Transactional
    public void autoAssignTasks() {
        log.info("Starting auto-assignment of unassigned tasks");
        
        List<Task> unassignedTasks = taskRepository.findByAssigneeIsNullAndStatus("new");
        
        for (Task task : unassignedTasks) {
            User bestMatch = findBestMatch(task);
            if (bestMatch != null) {
                task.setAssignee(bestMatch);
                task.setStatus("assigned");
                task.setUpdatedAt(LocalDateTime.now());
                taskRepository.save(task);
                
                log.info("Auto-assigned task {} to helper {}", task.getId(), bestMatch.getEmail());
            }
        }
        
        log.info("Auto-assignment completed. Processed {} tasks", unassignedTasks.size());
    }

    /**
     * Helper match data class
     */
    public static class HelperMatch {
        private final User helper;
        private final double score;
        private final List<String> reasons;

        public HelperMatch(User helper, double score, List<String> reasons) {
            this.helper = helper;
            this.score = score;
            this.reasons = reasons;
        }

        public User getHelper() { return helper; }
        public double getScore() { return score; }
        public List<String> getReasons() { return reasons; }
    }
}


