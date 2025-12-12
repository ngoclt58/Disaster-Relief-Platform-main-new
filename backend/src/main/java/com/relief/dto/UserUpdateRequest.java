package com.relief.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be valid")
    private String phone;
    
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String dateOfBirth; // ISO date
    private String gender;
    private String preferredLanguage;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Integer householdSize;
    private String specialNeeds;
    private Double latitude;
    private Double longitude;
    private Boolean consentToContact;
    private Boolean consentToShare;

    // Explicit getters for Lombok compatibility
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}


