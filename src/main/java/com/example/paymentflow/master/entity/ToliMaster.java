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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@EntityAuditEnabled
@EntityListeners(SharedEntityAuditListener.class)
@Table(name = "toli_master")
public class ToliMaster extends AbstractAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "employer_id", nullable = false, length = 64)
    private String employerId;

    @Column(name = "registration_number", nullable = false, unique = true, length = 64)
    @NotBlank(message = "Registration number is required")
    @Size(max = 64, message = "Registration number cannot exceed 64 characters")
    private String registrationNumber;

    @Column(name = "employer_name_marathi", nullable = false, length = 200)
    @NotBlank(message = "Employer name (Marathi) is required")
    @Size(max = 200, message = "Employer name (Marathi) cannot exceed 200 characters")
    private String employerNameMarathi;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "employer_name_english", length = 200)
    @Size(max = 200, message = "Employer name (English) cannot exceed 200 characters")
    private String employerNameEnglish;

    @Column(name = "mobile_number", length = 15)
    @Size(max = 15, message = "Mobile number cannot exceed 15 characters")
    private String mobileNumber;

    @Column(name = "email_id", length = 150)
    @Size(max = 150, message = "Email ID cannot exceed 150 characters")
    private String emailId;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public ToliMaster() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

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

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getEmployerNameMarathi() {
        return employerNameMarathi;
    }

    public void setEmployerNameMarathi(String employerNameMarathi) {
        this.employerNameMarathi = employerNameMarathi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmployerNameEnglish() {
        return employerNameEnglish;
    }

    public void setEmployerNameEnglish(String employerNameEnglish) {
        this.employerNameEnglish = employerNameEnglish;
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
        return "TOLI_MASTER";
    }

    @Override
    public Map<String, Object> auditState() {
        return auditStateOf(
                "id", id,
                "boardId", boardId,
                "employerId", employerId,
                "registrationNumber", registrationNumber,
                "employerNameMarathi", employerNameMarathi,
                "address", address,
                "employerNameEnglish", employerNameEnglish,
                "mobileNumber", mobileNumber,
                "emailId", emailId,
                "status", status,
                "createdAt", createdAt != null ? createdAt.toString() : null,
                "updatedAt", updatedAt != null ? updatedAt.toString() : null);
    }
}
