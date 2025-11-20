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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@EntityAuditEnabled
@EntityListeners(SharedEntityAuditListener.class)
@Table(name = "worker_master")
public class WorkerMaster extends AbstractAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "worker_name_marathi", nullable = false, length = 120)
    @NotBlank(message = "Worker name (Marathi) is required")
    @Size(max = 120, message = "Worker name (Marathi) cannot exceed 120 characters")
    private String workerNameMarathi;

    @Column(name = "worker_name_english", length = 120)
    @Size(max = 120, message = "Worker name (English) cannot exceed 120 characters")
    private String workerNameEnglish;

    @Column(name = "witness_name_1", length = 120)
    private String witnessName1;

    @Column(name = "witness_name_2", length = 120)
    private String witnessName2;

    @Column(name = "toli_number", length = 64)
    private String toliNumber;

    @Column(name = "registration_number", nullable = false, unique = true, length = 64)
    @NotBlank(message = "Registration number is required")
    @Size(max = 64, message = "Registration number cannot exceed 64 characters")
    private String registrationNumber;

    @Column(name = "pan_number", length = 16)
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "PAN number format is invalid")
    private String panNumber;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "mother_name", length = 120)
    private String motherName;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "mobile_number_1", length = 15)
    private String mobileNumber1;

    @Column(name = "marital_status", length = 30)
    private String maritalStatus;

    @Column(name = "ifsc_code", length = 11)
    private String ifscCode;

    @Column(name = "branch_address", length = 255)
    private String branchAddress;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "age")
    @Min(value = 0, message = "Age must be greater than or equal to 0")
    private Integer age;

    @Column(name = "address1", length = 255)
    private String address1;

    @Column(name = "address2", length = 255)
    private String address2;

    @Column(name = "account_number", length = 64)
    private String accountNumber;

    @Column(name = "aadhar_number", nullable = false, unique = true, length = 12)
    @NotBlank(message = "Aadhar number is required")
    @Pattern(regexp = "^[0-9]{12}$", message = "Aadhar number must be 12 digits")
    private String aadharNumber;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public WorkerMaster() {
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

    public String getWorkerNameMarathi() {
        return workerNameMarathi;
    }

    public void setWorkerNameMarathi(String workerNameMarathi) {
        this.workerNameMarathi = workerNameMarathi;
    }

    public String getWorkerNameEnglish() {
        return workerNameEnglish;
    }

    public void setWorkerNameEnglish(String workerNameEnglish) {
        this.workerNameEnglish = workerNameEnglish;
    }

    public String getWitnessName1() {
        return witnessName1;
    }

    public void setWitnessName1(String witnessName1) {
        this.witnessName1 = witnessName1;
    }

    public String getWitnessName2() {
        return witnessName2;
    }

    public void setWitnessName2(String witnessName2) {
        this.witnessName2 = witnessName2;
    }

    public String getToliNumber() {
        return toliNumber;
    }

    public void setToliNumber(String toliNumber) {
        this.toliNumber = toliNumber;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMobileNumber1() {
        return mobileNumber1;
    }

    public void setMobileNumber1(String mobileNumber1) {
        this.mobileNumber1 = mobileNumber1;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
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

    @Deprecated(forRemoval = false)
    public String getWorkerName() {
        return workerNameMarathi;
    }

    @Deprecated(forRemoval = false)
    public void setWorkerName(String workerName) {
        this.workerNameMarathi = workerName;
    }

    @Deprecated(forRemoval = false)
    public String getRegistrationNo() {
        return registrationNumber;
    }

    @Deprecated(forRemoval = false)
    public void setRegistrationNo(String registrationNo) {
        this.registrationNumber = registrationNo;
    }

    @Deprecated(forRemoval = false)
    public String getAadhaarNumber() {
        return aadharNumber;
    }

    @Deprecated(forRemoval = false)
    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadharNumber = aadhaarNumber;
    }

    @Override
    public String entityType() {
        return "WORKER_MASTER";
    }

    @Override
    public Map<String, Object> auditState() {
        return auditStateOf(
                "id", id,
                "boardId", boardId,
                "workerNameMarathi", workerNameMarathi,
                "workerNameEnglish", workerNameEnglish,
                "witnessName1", witnessName1,
                "witnessName2", witnessName2,
                "toliNumber", toliNumber,
                "registrationNumber", registrationNumber,
                "panNumber", panNumber,
                "nationality", nationality,
                "motherName", motherName,
                "mobileNumber", mobileNumber,
                "mobileNumber1", mobileNumber1,
                "maritalStatus", maritalStatus,
                "ifscCode", ifscCode,
                "branchAddress", branchAddress,
                "bankName", bankName,
                "age", age,
                "address1", address1,
                "address2", address2,
                "accountNumber", accountNumber,
                "aadharNumber", aadharNumber,
                "status", status,
                "createdAt", createdAt != null ? createdAt.toString() : null,
                "updatedAt", updatedAt != null ? updatedAt.toString() : null);
    }
}
