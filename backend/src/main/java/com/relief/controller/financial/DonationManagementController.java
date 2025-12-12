package com.relief.controller.financial;

import com.relief.service.financial.DonationManagementService;
import com.relief.service.financial.DonationManagementService.Donation;
import com.relief.entity.User;
import com.relief.repository.UserRepository;
import com.relief.service.financial.DonationManagementService.Donor;
import com.relief.service.financial.DonationManagementService.DonationSummary;
import com.relief.service.financial.DonationManagementService.DonationAnalytics;
import com.relief.service.financial.DonationManagementService.DonationType;
import com.relief.service.financial.DonationManagementService.DonationStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Donation management controller
 */
@RestController
@RequestMapping("/donation-management")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Donation Management", description = "Donation tracking and management APIs")
public class DonationManagementController {

    private final DonationManagementService donationManagementService;
    private final UserRepository userRepository;

    @PostMapping("/donations")
    @Operation(summary = "Record a new donation")
    public ResponseEntity<?> recordDonation(
            @RequestBody RecordDonationRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        try {
            String username = principal.getUsername();
            UUID userId;
            try {
                userId = UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                User user = userRepository.findByEmail(username)
                        .orElseGet(() -> userRepository.findByPhone(username)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
                userId = user.getId();
            }
            
            // Get user details for recording
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
            // Create or update donor information
            Donor donor = new Donor();
            donor.setName(request.getDonorName());
            donor.setEmail(request.getDonorEmail());
            donor.setPhone(request.getDonorPhone());
            
            // Record the donation
            Donation donation = donationManagementService.recordDonation(
                request.getDonorName(),
                request.getDonorEmail(),
                request.getDonorPhone(),
                request.getAmount(),
                request.getCurrency() != null ? request.getCurrency() : "USD",
                request.getPaymentMethod() != null ? request.getPaymentMethod() : "Other",
                request.getCampaignId(),
                request.getNotes(),
                userId
            );
            
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            log.error("Error recording donation", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to record donation: " + e.getMessage()));
        }
    }

    @PostMapping("/donors")
    @Operation(summary = "Register a new donor")
    public ResponseEntity<?> registerDonor(
            @RequestBody RegisterDonorRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        
        try {
            String username = principal.getUsername();
            UUID userId;
            try {
                userId = UUID.fromString(username);
            } catch (IllegalArgumentException e) {
                User user = userRepository.findByEmail(username)
                        .orElseGet(() -> userRepository.findByPhone(username)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username)));
                userId = user.getId();
            }
            
            // Get user details for recording
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Update donor information using the service
            Donor donor = donationManagementService.updateDonor(
                request.getId() != null ? request.getId() : UUID.randomUUID().toString(),
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                request.getOrganization()
            );
            
            return ResponseEntity.ok(donor);
        } catch (Exception e) {
            log.error("Error registering donor", e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to register donor: " + e.getMessage()));
        }
    }

    @GetMapping("/donations/{donationId}")
    @Operation(summary = "Get donation details")
    public ResponseEntity<?> getDonation(@PathVariable String donationId) {
        try {
            if (donationId == null || donationId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Donation ID is required"));
            }
            
            Donation donation = donationManagementService.getDonation(donationId);
            if (donation == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Donation not found with ID: " + donationId));
            }
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            log.error("Error fetching donation with ID: " + donationId, e);
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching donation details"));
        }
    }

    @GetMapping("/donors/{donorId}")
    @Operation(summary = "Get donor details")
    public ResponseEntity<?> getDonor(@PathVariable String donorId) {
        try {
            if (donorId == null || donorId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Donor ID is required"));
            }
            
            Donor donor = donationManagementService.getDonor(donorId);
            if (donor == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Donor not found with ID: " + donorId));
            }
            return ResponseEntity.ok(donor);
        } catch (Exception e) {
            log.error("Error fetching donor with ID: " + donorId, e);
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while fetching donor details"));
        }
    }

    @GetMapping("/donations")
    @Operation(summary = "Get donations with filters")
    public ResponseEntity<?> getDonations(
            @RequestParam(required = false) String donorId,
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) DonationType type,
            @RequestParam(required = false) DonationStatus status,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            // Validate limit parameter
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Limit must be between 1 and 1000"));
            }
            
            // Get donations based on filters
            List<Donation> donations;
            if (donorId != null) {
                donations = donationManagementService.getDonationsByDonor(donorId);
            } else if (campaignId != null) {
                donations = donationManagementService.getCampaignDonations(campaignId, limit);
            } else {
                // Get all donations with limit
                donations = donationManagementService.getAllDonations(limit);
            }
            
            // Apply additional filters
            if (type != null) {
                donations = donations.stream()
                    .filter(d -> type.equals(d.getType()))
                    .collect(Collectors.toList());
            }
            
            if (status != null) {
                donations = donations.stream()
                    .filter(d -> status.equals(d.getStatus()))
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            log.error("Error fetching donations", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while fetching donations: " + e.getMessage()));
        }
    }

    @GetMapping("/donors")
    @Operation(summary = "Get donors with filters")
    public ResponseEntity<?> getDonors(
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            // Validate limit parameter
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Limit must be between 1 and 1000"));
            }
            
            // Get all donors (since we don't have a direct method to filter by organization/type)
            List<Donor> allDonors = donationManagementService.getDonors(null, null, 1000);
            
            // Apply filters
            List<Donor> filteredDonors = allDonors.stream()
                .filter(donor -> organization == null || 
                    (donor.getOrganization() != null && donor.getOrganization().contains(organization)))
                .filter(donor -> type == null || 
                    (donor.getType() != null && donor.getType().equals(type)))
                .limit(limit)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(filteredDonors);
        } catch (Exception e) {
            log.error("Error fetching donors", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while fetching donors: " + e.getMessage()));
        }
    }

    @PutMapping("/donations/{donationId}/status")
    @Operation(summary = "Update donation status")
    public ResponseEntity<?> updateDonationStatus(
            @PathVariable String donationId,
            @RequestBody UpdateDonationStatusRequest request) {
        
        try {
            // Validate input
            if (donationId == null || donationId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Donation ID is required"));
            }
            
            if (request.getStatus() == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
            }
            
            // Update donation status
            Donation donation = donationManagementService.updateDonationStatus(
                donationId, 
                request.getStatus(), 
                request.getNotes()
            );
            
            if (donation == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Donation not found with ID: " + donationId));
            }
            
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            log.error("Error updating donation status for ID: " + donationId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while updating donation status: " + e.getMessage()));
        }
    }

    @PutMapping("/donors/{donorId}")
    @Operation(summary = "Update donor information")
    public ResponseEntity<?> updateDonor(
            @PathVariable String donorId,
            @RequestBody UpdateDonorRequest request) {
        
        try {
            // Validate input
            if (donorId == null || donorId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Donor ID is required"));
            }
            
            // Update donor information
            Donor donor = donationManagementService.updateDonor(
                donorId,
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress(),
                request.getOrganization()
            );
            
            if (donor == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Donor not found with ID: " + donorId));
            }
            
            return ResponseEntity.ok(donor);
        } catch (Exception e) {
            log.error("Error updating donor with ID: " + donorId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while updating donor: " + e.getMessage()));
        }
    }

    @GetMapping("/campaigns/{campaignId}/donations")
    @Operation(summary = "Get donations for a campaign")
    public ResponseEntity<?> getCampaignDonations(
            @PathVariable String campaignId,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            if (campaignId == null || campaignId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Campaign ID is required"));
            }
            
            if (limit <= 0 || limit > 1000) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Limit must be between 1 and 1000"));
            }
            
            List<Donation> donations = donationManagementService.getCampaignDonations(campaignId, limit);
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            log.error("Error fetching donations for campaign: " + campaignId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while fetching campaign donations: " + e.getMessage()));
        }
    }

    @GetMapping("/donors/{donorId}/donations")
    @Operation(summary = "Get donations by donor")
    public ResponseEntity<?> getDonorDonations(
            @PathVariable String donorId,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            if (donorId == null || donorId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Donor ID is required"));
            }
            
            List<Donation> donations = donationManagementService.getDonationsByDonor(donorId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(donations);
        } catch (Exception e) {
            log.error("Error fetching donations for donor: " + donorId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while fetching donor donations: " + e.getMessage()));
        }
    }
    
    // Request and Response DTOs
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordDonationRequest {
        private String donorName;
        private String donorEmail;
        private String donorPhone;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String campaignId;
        private String notes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDonorRequest {
        private String id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String organization;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDonorRequest {
        private String name;
        private String email;
        private String phone;
        private String address;
        private String organization;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDonationStatusRequest {
        private DonationStatus status;
        private String notes;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get donation summary")
    public ResponseEntity<?> getDonationSummary(
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        try {
            // Validate date range if both dates are provided
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "End date must be after start date"));
            }
            
            DonationSummary summary = donationManagementService.getDonationSummary(
                campaignId, startDate, endDate);
                
            if (summary == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "No data found for the specified criteria"));
            }
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error generating donation summary", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while generating the donation summary: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get donation analytics")
    public ResponseEntity<?> getDonationAnalytics(
            @RequestParam(required = false) String campaignId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        
        try {
            // Validate date range if both dates are provided
            if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "End date must be after start date"));
            }
            
            DonationAnalytics analytics = donationManagementService.getDonationAnalytics(
                campaignId, startDate, endDate
            );
            
            if (analytics == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "No analytics data found for the specified criteria"));
            }
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error generating donation analytics", e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while generating donation analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/donors/{donorId}/summary")
    @Operation(summary = "Get donor summary")
    public ResponseEntity<?> getDonorSummary(@PathVariable String donorId) {
        try {
            if (donorId == null || donorId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Donor ID is required"));
            }
            
            DonationSummary summary = donationManagementService.getDonorSummary(donorId);
            
            if (summary == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "No summary found for donor with ID: " + donorId));
            }
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error fetching donor summary for ID: " + donorId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while fetching donor summary: " + e.getMessage()));
        }
    }

    @PostMapping("/donations/{donationId}/refund")
    @Operation(summary = "Process donation refund")
    public ResponseEntity<?> processRefund(
            @PathVariable String donationId,
            @RequestBody ProcessRefundRequest request) {
        
        try {
            if (donationId == null || donationId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Donation ID is required"));
            }
            
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "A valid refund amount is required"));
            }
            
            Donation donation = donationManagementService.processRefund(
                donationId, request.getAmount(), request.getReason()
            );
            
            if (donation == null) {
                return ResponseEntity.status(404)
                    .body(Map.of("error", "Donation not found with ID: " + donationId));
            }
            
            return ResponseEntity.ok(donation);
        } catch (Exception e) {
            log.error("Error processing refund for donation ID: " + donationId, e);
            return ResponseEntity.status(500)
                .body(Map.of("error", "An error occurred while processing the refund: " + e.getMessage()));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessRefundRequest {
        private BigDecimal amount;
        private String reason;
    }
}


