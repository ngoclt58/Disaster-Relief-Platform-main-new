package com.relief.service.financial;

import com.relief.entity.User;
import com.relief.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for donation management and funding tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DonationManagementService {

    private static final Logger log = LoggerFactory.getLogger(DonationManagementService.class);

    private final UserRepository userRepository;
    private final Map<String, Donation> donations = new ConcurrentHashMap<>();
    private final Map<String, List<DonationAllocation>> donationAllocations = new ConcurrentHashMap<>();
    private final Map<String, Donor> donors = new ConcurrentHashMap<>();
    private final Map<String, Campaign> campaigns = new ConcurrentHashMap<>();

    /**
     * Record a new donation
     */
    @Transactional
    public Donation recordDonation(String donorName, String donorEmail, String donorPhone,
                                 BigDecimal amount, String currency, String paymentMethod,
                                 String campaignId, String notes, UUID recordedBy) {
        log.info("Recording donation: {} from {}", amount, donorName);
        
        User recorder = userRepository.findById(recordedBy)
            .orElseThrow(() -> new IllegalArgumentException("Recorder not found"));
        
        // Create or update donor
        Donor donor = findOrCreateDonor(donorName, donorEmail, donorPhone);
        
        Donation donation = new Donation();
        donation.setId(UUID.randomUUID().toString());
        donation.setDonorId(donor.getId());
        donation.setAmount(amount);
        donation.setCurrency(currency);
        donation.setPaymentMethod(paymentMethod);
        donation.setCampaignId(campaignId);
        donation.setNotes(notes);
        donation.setStatus(DonationStatus.PENDING);
        donation.setRecordedBy(recordedBy);
        donation.setRecordedByName(recorder.getFullName());
        donation.setDonationDate(LocalDateTime.now());
        donation.setCreatedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        
        donations.put(donation.getId(), donation);
        donationAllocations.put(donation.getId(), new ArrayList<>());
        
        // Update donor statistics
        updateDonorStatistics(donor, amount);
        
        log.info("Donation recorded: {} with ID: {}", donation.getId(), donation.getId());
        return donation;
    }

    /**
     * Process donation (mark as confirmed)
     */
    @Transactional
    public Donation processDonation(String donationId, String transactionId, String notes) {
        Donation donation = donations.get(donationId);
        if (donation == null) {
            throw new IllegalArgumentException("Donation not found");
        }
        
        donation.setStatus(DonationStatus.CONFIRMED);
        donation.setTransactionId(transactionId);
        donation.setProcessedAt(LocalDateTime.now());
        donation.setProcessedNotes(notes);
        donation.setUpdatedAt(LocalDateTime.now());
        
        log.info("Donation processed: {}", donationId);
        return donation;
    }

    /**
     * Allocate donation to specific budget or relief effort
     */
    @Transactional
    public DonationAllocation allocateDonation(String donationId, String budgetId, 
                                             BigDecimal amount, String purpose, 
                                             String notes, UUID allocatedBy) {
        log.info("Allocating donation {} to budget {}: {}", donationId, budgetId, amount);
        
        Donation donation = donations.get(donationId);
        if (donation == null) {
            throw new IllegalArgumentException("Donation not found");
        }
        
        if (donation.getStatus() != DonationStatus.CONFIRMED) {
            throw new IllegalStateException("Donation must be confirmed before allocation");
        }
        
        // Check if allocation amount is valid
        BigDecimal totalAllocated = donationAllocations.getOrDefault(donationId, new ArrayList<>())
            .stream()
            .map(DonationAllocation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAllocated.add(amount).compareTo(donation.getAmount()) > 0) {
            throw new IllegalArgumentException("Allocation amount exceeds donation amount");
        }
        
        User allocator = userRepository.findById(allocatedBy)
            .orElseThrow(() -> new IllegalArgumentException("Allocator not found"));
        
        DonationAllocation allocation = new DonationAllocation();
        allocation.setId(UUID.randomUUID().toString());
        allocation.setDonationId(donationId);
        allocation.setBudgetId(budgetId);
        allocation.setAmount(amount);
        allocation.setPurpose(purpose);
        allocation.setNotes(notes);
        allocation.setAllocatedBy(allocatedBy);
        allocation.setAllocatedByName(allocator.getFullName());
        allocation.setAllocatedAt(LocalDateTime.now());
        allocation.setCreatedAt(LocalDateTime.now());
        
        donationAllocations.get(donationId).add(allocation);
        
        log.info("Donation allocated: {} to budget {}", allocation.getId(), budgetId);
        return allocation;
    }

    /**
     * Create a donation campaign
     */
    @Transactional
    public Campaign createCampaign(String name, String description, BigDecimal targetAmount,
                                 String currency, String category, LocalDateTime startDate,
                                 LocalDateTime endDate, UUID createdBy) {
        log.info("Creating donation campaign: {}", name);
        
        User creator = userRepository.findById(createdBy)
            .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        
        Campaign campaign = new Campaign();
        campaign.setId(UUID.randomUUID().toString());
        campaign.setName(name);
        campaign.setDescription(description);
        campaign.setTargetAmount(targetAmount);
        campaign.setCurrency(currency);
        campaign.setCategory(category);
        campaign.setStartDate(startDate);
        campaign.setEndDate(endDate);
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreatedBy(createdBy);
        campaign.setCreatedByName(creator.getFullName());
        campaign.setCreatedAt(LocalDateTime.now());
        campaign.setUpdatedAt(LocalDateTime.now());
        
        campaigns.put(campaign.getId(), campaign);
        
        log.info("Campaign created: {} with ID: {}", campaign.getName(), campaign.getId());
        return campaign;
    }

    /**
     * Get donation details
     */
    @Transactional(readOnly = true)
    public Donation getDonation(String donationId) {
        Donation donation = donations.get(donationId);
        if (donation == null) {
            throw new IllegalArgumentException("Donation not found");
        }
        return donation;
    }

    /**
     * Get donation allocations
     */
    @Transactional(readOnly = true)
    public List<DonationAllocation> getDonationAllocations(String donationId) {
        return donationAllocations.getOrDefault(donationId, new ArrayList<>());
    }

    /**
     * Get donor details
     */
    @Transactional(readOnly = true)
    public Donor getDonor(String donorId) {
        Donor donor = donors.get(donorId);
        if (donor == null) {
            throw new IllegalArgumentException("Donor not found");
        }
        return donor;
    }

    /**
     * Get campaign details
     */
    @Transactional(readOnly = true)
    public Campaign getCampaign(String campaignId) {
        Campaign campaign = campaigns.get(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign not found");
        }
        return campaign;
    }

    /**
     * Get donations by campaign
     */
    @Transactional(readOnly = true)
    public List<Donation> getDonationsByCampaign(String campaignId) {
        return donations.values().stream()
            .filter(donation -> campaignId.equals(donation.getCampaignId()))
            .sorted(Comparator.comparing(Donation::getDonationDate).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get all donations
     */
    @Transactional(readOnly = true)
    public List<Donation> getAllDonations(int limit) {
        return donations.values().stream()
            .sorted(Comparator.comparing(Donation::getDonationDate).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Get donations by donor
     */
    @Transactional(readOnly = true)
    public List<Donation> getDonationsByDonor(String donorId) {
        if (donorId == null) {
            return new ArrayList<>();
        }
        return donations.values().stream()
            .filter(donation -> donorId.equals(donation.getDonorId()))
            .sorted(Comparator.comparing(Donation::getDonationDate).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Get campaign summary
     */
    @Transactional(readOnly = true)
    public CampaignSummary getCampaignSummary(String campaignId) {
        Campaign campaign = campaigns.get(campaignId);
        if (campaign == null) {
            throw new IllegalArgumentException("Campaign not found");
        }
        
        List<Donation> campaignDonations = getDonationsByCampaign(campaignId);
        
        CampaignSummary summary = new CampaignSummary();
        summary.setCampaign(campaign);
        summary.setTotalDonations(campaignDonations.size());
        summary.setTotalAmount(campaignDonations.stream()
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setConfirmedAmount(campaignDonations.stream()
            .filter(d -> d.getStatus() == DonationStatus.CONFIRMED)
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setPendingAmount(campaignDonations.stream()
            .filter(d -> d.getStatus() == DonationStatus.PENDING)
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setProgressPercentage(calculateProgressPercentage(campaign, campaignDonations));
        summary.setDaysRemaining(calculateDaysRemaining(campaign));
        summary.setAverageDonationAmount(calculateAverageDonationAmount(campaignDonations));
        summary.setTopDonors(calculateTopDonors(campaignDonations));
        
        return summary;
    }

    /**
     * Get donor statistics
     */
    @Transactional(readOnly = true)
    public DonorStatistics getDonorStatistics(String donorId) {
        Donor donor = donors.get(donorId);
        if (donor == null) {
            throw new IllegalArgumentException("Donor not found");
        }
        
        List<Donation> donorDonations = getDonationsByDonor(donorId);
        
        DonorStatistics stats = new DonorStatistics();
        stats.setDonor(donor);
        stats.setTotalDonations(donorDonations.size());
        stats.setTotalAmount(donorDonations.stream()
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
        stats.setAverageDonationAmount(calculateAverageDonationAmount(donorDonations));
        stats.setFirstDonationDate(donorDonations.stream()
            .map(Donation::getDonationDate)
            .min(LocalDateTime::compareTo)
            .orElse(null));
        stats.setLastDonationDate(donorDonations.stream()
            .map(Donation::getDonationDate)
            .max(LocalDateTime::compareTo)
            .orElse(null));
        stats.setDonationFrequency(calculateDonationFrequency(donorDonations));
        stats.setPreferredCampaigns(calculatePreferredCampaigns(donorDonations));
        
        return stats;
    }

    /**
     * Find or create donor
     */
    private Donor findOrCreateDonor(String name, String email, String phone) {
        // Try to find existing donor by email
        Optional<Donor> existingDonor = donors.values().stream()
            .filter(donor -> email.equals(donor.getEmail()))
            .findFirst();
        
        if (existingDonor.isPresent()) {
            return existingDonor.get();
        }
        
        // Create new donor
        Donor donor = new Donor();
        donor.setId(UUID.randomUUID().toString());
        donor.setName(name);
        donor.setEmail(email);
        donor.setPhone(phone);
        donor.setTotalDonations(0);
        donor.setTotalAmount(BigDecimal.ZERO);
        donor.setFirstDonationDate(null);
        donor.setLastDonationDate(null);
        donor.setCreatedAt(LocalDateTime.now());
        donor.setUpdatedAt(LocalDateTime.now());
        
        donors.put(donor.getId(), donor);
        return donor;
    }

    /**
     * Update donor statistics
     */
    private void updateDonorStatistics(Donor donor, BigDecimal amount) {
        donor.setTotalDonations(donor.getTotalDonations() + 1);
        donor.setTotalAmount(donor.getTotalAmount().add(amount));
        
        if (donor.getFirstDonationDate() == null) {
            donor.setFirstDonationDate(LocalDateTime.now());
        }
        donor.setLastDonationDate(LocalDateTime.now());
        donor.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Calculate progress percentage
     */
    private double calculateProgressPercentage(Campaign campaign, List<Donation> donations) {
        if (campaign.getTargetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        
        BigDecimal totalAmount = donations.stream()
            .filter(d -> d.getStatus() == DonationStatus.CONFIRMED)
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAmount.divide(campaign.getTargetAmount(), 4, BigDecimal.ROUND_HALF_UP)
            .doubleValue();
    }

    /**
     * Calculate days remaining
     */
    private long calculateDaysRemaining(Campaign campaign) {
        if (campaign.getEndDate() == null) {
            return -1; // No end date
        }
        
        return java.time.Duration.between(LocalDateTime.now(), campaign.getEndDate()).toDays();
    }

    /**
     * Calculate average donation amount
     */
    private BigDecimal calculateAverageDonationAmount(List<Donation> donations) {
        if (donations.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalAmount = donations.stream()
            .map(Donation::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAmount.divide(BigDecimal.valueOf(donations.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Calculate top donors
     */
    private List<Donor> calculateTopDonors(List<Donation> donations) {
        Map<String, BigDecimal> donorAmounts = donations.stream()
            .collect(Collectors.groupingBy(
                Donation::getDonorId,
                Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add)
            ));
        
        return donorAmounts.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(10)
            .map(entry -> donors.get(entry.getKey()))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Calculate donation frequency
     */
    private String calculateDonationFrequency(List<Donation> donations) {
        if (donations.size() < 2) {
            return "One-time";
        }
        
        long daysBetween = java.time.Duration.between(
            donations.get(donations.size() - 1).getDonationDate(),
            donations.get(0).getDonationDate()
        ).toDays();
        
        double frequency = (double) donations.size() / daysBetween * 365;
        
        if (frequency >= 12) {
            return "Monthly+";
        } else if (frequency >= 4) {
            return "Quarterly";
        } else if (frequency >= 1) {
            return "Yearly";
        } else {
            return "Occasional";
        }
    }

    /**
     * Calculate preferred campaigns
     */
    private Map<String, Long> calculatePreferredCampaigns(List<Donation> donations) {
        return donations.stream()
            .filter(d -> d.getCampaignId() != null)
            .collect(Collectors.groupingBy(
                Donation::getCampaignId,
                Collectors.counting()
            ));
    }

    // Data classes
    public static class Donation {
        private String id;
        private String donorId;
        private BigDecimal amount;
        private String currency;
        private String paymentMethod;
        private String campaignId;
        private String notes;
        private DonationStatus status;
        private String transactionId;
        private UUID recordedBy;
        private String recordedByName;
        private LocalDateTime donationDate;
        private LocalDateTime processedAt;
        private String processedNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private DonationType type;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDonorId() { return donorId; }
        public void setDonorId(String donorId) { this.donorId = donorId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public String getCampaignId() { return campaignId; }
        public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public DonationStatus getStatus() { return status; }
        public void setStatus(DonationStatus status) { this.status = status; }

        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

        public UUID getRecordedBy() { return recordedBy; }
        public void setRecordedBy(UUID recordedBy) { this.recordedBy = recordedBy; }

        public String getRecordedByName() { return recordedByName; }
        public void setRecordedByName(String recordedByName) { this.recordedByName = recordedByName; }

        public LocalDateTime getDonationDate() { return donationDate; }
        public void setDonationDate(LocalDateTime donationDate) { this.donationDate = donationDate; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

        public String getProcessedNotes() { return processedNotes; }
        public void setProcessedNotes(String processedNotes) { this.processedNotes = processedNotes; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public DonationType getType() { return type; }
        public void setType(DonationType type) { this.type = type; }
    }

    public static class DonationAllocation {
        private String id;
        private String donationId;
        private String budgetId;
        private BigDecimal amount;
        private String purpose;
        private String notes;
        private UUID allocatedBy;
        private String allocatedByName;
        private LocalDateTime allocatedAt;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDonationId() { return donationId; }
        public void setDonationId(String donationId) { this.donationId = donationId; }

        public String getBudgetId() { return budgetId; }
        public void setBudgetId(String budgetId) { this.budgetId = budgetId; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public UUID getAllocatedBy() { return allocatedBy; }
        public void setAllocatedBy(UUID allocatedBy) { this.allocatedBy = allocatedBy; }

        public String getAllocatedByName() { return allocatedByName; }
        public void setAllocatedByName(String allocatedByName) { this.allocatedByName = allocatedByName; }

        public LocalDateTime getAllocatedAt() { return allocatedAt; }
        public void setAllocatedAt(LocalDateTime allocatedAt) { this.allocatedAt = allocatedAt; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class Donor {
        private String id;
        private String name;
        private String email;
        private String phone;
        private int totalDonations;
        private BigDecimal totalAmount;
        private LocalDateTime firstDonationDate;
        private LocalDateTime lastDonationDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String organization;
        private String type;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public int getTotalDonations() { return totalDonations; }
        public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public LocalDateTime getFirstDonationDate() { return firstDonationDate; }
        public void setFirstDonationDate(LocalDateTime firstDonationDate) { this.firstDonationDate = firstDonationDate; }

        public LocalDateTime getLastDonationDate() { return lastDonationDate; }
        public void setLastDonationDate(LocalDateTime lastDonationDate) { this.lastDonationDate = lastDonationDate; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

        public String getOrganization() { return organization; }
        public void setOrganization(String organization) { this.organization = organization; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class Campaign {
        private String id;
        private String name;
        private String description;
        private BigDecimal targetAmount;
        private String currency;
        private String category;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private CampaignStatus status;
        private UUID createdBy;
        private String createdByName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getTargetAmount() { return targetAmount; }
        public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

        public CampaignStatus getStatus() { return status; }
        public void setStatus(CampaignStatus status) { this.status = status; }

        public UUID getCreatedBy() { return createdBy; }
        public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }

        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class CampaignSummary {
        private Campaign campaign;
        private int totalDonations;
        private BigDecimal totalAmount;
        private BigDecimal confirmedAmount;
        private BigDecimal pendingAmount;
        private double progressPercentage;
        private long daysRemaining;
        private BigDecimal averageDonationAmount;
        private List<Donor> topDonors;

        // Getters and setters
        public Campaign getCampaign() { return campaign; }
        public void setCampaign(Campaign campaign) { this.campaign = campaign; }

        public int getTotalDonations() { return totalDonations; }
        public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getConfirmedAmount() { return confirmedAmount; }
        public void setConfirmedAmount(BigDecimal confirmedAmount) { this.confirmedAmount = confirmedAmount; }

        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }

        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }

        public long getDaysRemaining() { return daysRemaining; }
        public void setDaysRemaining(long daysRemaining) { this.daysRemaining = daysRemaining; }

        public BigDecimal getAverageDonationAmount() { return averageDonationAmount; }
        public void setAverageDonationAmount(BigDecimal averageDonationAmount) { this.averageDonationAmount = averageDonationAmount; }

        public List<Donor> getTopDonors() { return topDonors; }
        public void setTopDonors(List<Donor> topDonors) { this.topDonors = topDonors; }
    }

    public static class DonorStatistics {
        private Donor donor;
        private int totalDonations;
        private BigDecimal totalAmount;
        private BigDecimal averageDonationAmount;
        private LocalDateTime firstDonationDate;
        private LocalDateTime lastDonationDate;
        private String donationFrequency;
        private Map<String, Long> preferredCampaigns;

        // Getters and setters
        public Donor getDonor() { return donor; }
        public void setDonor(Donor donor) { this.donor = donor; }

        public int getTotalDonations() { return totalDonations; }
        public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getAverageDonationAmount() { return averageDonationAmount; }
        public void setAverageDonationAmount(BigDecimal averageDonationAmount) { this.averageDonationAmount = averageDonationAmount; }

        public LocalDateTime getFirstDonationDate() { return firstDonationDate; }
        public void setFirstDonationDate(LocalDateTime firstDonationDate) { this.firstDonationDate = firstDonationDate; }

        public LocalDateTime getLastDonationDate() { return lastDonationDate; }
        public void setLastDonationDate(LocalDateTime lastDonationDate) { this.lastDonationDate = lastDonationDate; }

        public String getDonationFrequency() { return donationFrequency; }
        public void setDonationFrequency(String donationFrequency) { this.donationFrequency = donationFrequency; }

        public Map<String, Long> getPreferredCampaigns() { return preferredCampaigns; }
        public void setPreferredCampaigns(Map<String, Long> preferredCampaigns) { this.preferredCampaigns = preferredCampaigns; }
    }

    public enum DonationStatus {
        PENDING, CONFIRMED, FAILED, REFUNDED
    }

    public enum CampaignStatus {
        ACTIVE, COMPLETED, CANCELLED, SUSPENDED
    }

    /**
     * Summary statistics for donations over a period or campaign.
     */
    public static class DonationSummary {
        private int totalDonations;
        private BigDecimal totalAmount;
        private BigDecimal averageAmount;
        private BigDecimal confirmedAmount;
        private BigDecimal pendingAmount;
        private Map<String, BigDecimal> amountByCurrency;

        public int getTotalDonations() { return totalDonations; }
        public void setTotalDonations(int totalDonations) { this.totalDonations = totalDonations; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public BigDecimal getAverageAmount() { return averageAmount; }
        public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }

        public BigDecimal getConfirmedAmount() { return confirmedAmount; }
        public void setConfirmedAmount(BigDecimal confirmedAmount) { this.confirmedAmount = confirmedAmount; }

        public BigDecimal getPendingAmount() { return pendingAmount; }
        public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }

        public Map<String, BigDecimal> getAmountByCurrency() { return amountByCurrency; }
        public void setAmountByCurrency(Map<String, BigDecimal> amountByCurrency) { this.amountByCurrency = amountByCurrency; }
    }

    /**
     * Time-series trend data for donations.
     */
    public static class DonationTrend {
        private LocalDateTime timestamp;
        private BigDecimal amount;

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    /**
     * Donor retention metrics.
     */
    public static class DonorRetention {
        private int totalDonors;
        private int returningDonors;
        private double retentionRate;

        public int getTotalDonors() { return totalDonors; }
        public void setTotalDonors(int totalDonors) { this.totalDonors = totalDonors; }

        public int getReturningDonors() { return returningDonors; }
        public void setReturningDonors(int returningDonors) { this.returningDonors = returningDonors; }

        public double getRetentionRate() { return retentionRate; }
        public void setRetentionRate(double retentionRate) { this.retentionRate = retentionRate; }
    }

    /**
     * Per-campaign performance metrics.
     */
    public static class CampaignPerformance {
        private String campaignId;
        private String campaignName;
        private BigDecimal totalAmount;
        private double progressPercentage;

        public String getCampaignId() { return campaignId; }
        public void setCampaignId(String campaignId) { this.campaignId = campaignId; }

        public String getCampaignName() { return campaignName; }
        public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public double getProgressPercentage() { return progressPercentage; }
        public void setProgressPercentage(double progressPercentage) { this.progressPercentage = progressPercentage; }
    }

    /**
     * Geographic distribution of donations (e.g. by region/country).
     */
    public static class GeographicDistribution {
        private String region;
        private BigDecimal totalAmount;
        private int donationCount;

        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public int getDonationCount() { return donationCount; }
        public void setDonationCount(int donationCount) { this.donationCount = donationCount; }
    }

    public static class DonationAnalytics {
        private List<DonationTrend> donationTrends;
        private DonorRetention donorRetention;
        private List<CampaignPerformance> campaignPerformance;
        private List<GeographicDistribution> geographicDistribution;

        // Getters and setters
        public List<DonationTrend> getDonationTrends() { return donationTrends; }
        public void setDonationTrends(List<DonationTrend> donationTrends) { this.donationTrends = donationTrends; }

        public DonorRetention getDonorRetention() { return donorRetention; }
        public void setDonorRetention(DonorRetention donorRetention) { this.donorRetention = donorRetention; }

        public List<CampaignPerformance> getCampaignPerformance() { return campaignPerformance; }
        public void setCampaignPerformance(List<CampaignPerformance> campaignPerformance) { this.campaignPerformance = campaignPerformance; }

        public List<GeographicDistribution> getGeographicDistribution() { return geographicDistribution; }
        public void setGeographicDistribution(List<GeographicDistribution> geographicDistribution) { this.geographicDistribution = geographicDistribution; }
    }

    public enum DonationType {
        CASH, IN_KIND, SERVICES, EQUIPMENT, FOOD, MEDICAL, OTHER
    }

    public List<Donor> getDonors(String organization, String type, int limit) {
        // Implementation for getting donors with filters
        return Collections.emptyList();
    }

    public Donation updateDonationStatus(String donationId, DonationStatus status, String notes) {
        // Implementation for updating donation status
        return new Donation();
    }

    public Donor updateDonor(String donorId, String name, String email, String phone, String address, String organization) {
        // Implementation for updating donor
        return new Donor();
    }

    public List<Donation> getCampaignDonations(String campaignId, int limit) {
        // Implementation for getting campaign donations
        return Collections.emptyList();
    }

    public List<Donation> getDonorDonations(String donorId, int limit) {
        // Implementation for getting donor donations
        return Collections.emptyList();
    }

    public DonationSummary getDonationSummary(String campaignId, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting donation summary
        return new DonationSummary();
    }

    public DonationAnalytics getDonationAnalytics(String campaignId, LocalDateTime startDate, LocalDateTime endDate) {
        // Implementation for getting donation analytics
        return new DonationAnalytics();
    }

    public DonationSummary getDonorSummary(String donorId) {
        // Implementation for getting donor summary
        return new DonationSummary();
    }

    public Donation processRefund(String donationId, BigDecimal amount, String reason) {
        // Implementation for processing refund
        return new Donation();
    }
}
