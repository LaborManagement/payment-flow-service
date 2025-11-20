package com.example.paymentflow.master.entity;

import java.time.LocalDateTime;
import java.util.Map;

import com.shared.entityaudit.annotation.EntityAuditEnabled;
import com.shared.entityaudit.descriptor.AbstractAuditableEntity;
import com.shared.entityaudit.listener.SharedEntityAuditListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@EntityAuditEnabled
@EntityListeners(SharedEntityAuditListener.class)
@Table(name = "employer_master")
public class EmployerMaster extends AbstractAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "serial_no", unique = true, length = 64)
    private String serialNo;

    @Column(name = "registration_number", nullable = false, unique = true, length = 64)
    private String registrationNo;

    @Column(name = "establishment_name", nullable = false, length = 200)
    @NotBlank(message = "Establishment name is required")
    @Size(max = 200, message = "Establishment name cannot exceed 200 characters")
    private String establishmentName;

    @Column(name = "employer_name", length = 200)
    private String employerName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "owner_name", length = 120)
    private String ownerName;

    @Column(name = "mobile_number", length = 15)
    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Column(name = "email_id", length = 150)
    @Email(message = "Email ID format is invalid")
    private String emailId;

    @Column(name = "aadhar_number", length = 12)
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be 12 digits")
    private String aadharNumber;

    @Column(name = "aadhaar_number", unique = true, length = 12)
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhaar number must be 12 digits")
    private String aadhaarNumber;

    @Column(name = "pan_number", length = 16)
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number format is invalid")
    private String panNumber;

    @Column(name = "tan_number", length = 16)
    private String tanNumber;

    @Column(name = "virtual_bank_account_number", length = 64)
    private String virtualBankAccountNumber;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public EmployerMaster() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getEstablishmentName() {
        return establishmentName;
    }

    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = normalizeAlphaNumeric(panNumber);
    }

    public String getTanNumber() {
        return tanNumber;
    }

    public void setTanNumber(String tanNumber) {
        this.tanNumber = normalizeAlphaNumeric(tanNumber);
    }

    public String getVirtualBankAccountNumber() {
        return virtualBankAccountNumber;
    }

    public void setVirtualBankAccountNumber(String virtualBankAccountNumber) {
        this.virtualBankAccountNumber = virtualBankAccountNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String entityType() {
        return "EMPLOYER_MASTER";
    }

    @Override
    public Map<String, Object> auditState() {
        return auditStateOf(
                "id", id,
                // removed employerId
                "serialNo", serialNo,
                "registrationNo", registrationNo,
                "establishmentName", establishmentName,
                "employerName", employerName,
                "address", address,
                "ownerName", ownerName,
                "mobileNumber", mobileNumber,
                "emailId", emailId,
                "aadharNumber", aadharNumber,
                "aadhaarNumber", aadhaarNumber,
                "panNumber", panNumber,
                "tanNumber", tanNumber,
                "virtualBankAccountNumber", virtualBankAccountNumber,
                "status", status,
                "createdAt", createdAt != null ? createdAt.toString() : null,
                "updatedAt", updatedAt != null ? updatedAt.toString() : null);
    }

    private String normalizeAlphaNumeric(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned.toUpperCase();
    }
}
