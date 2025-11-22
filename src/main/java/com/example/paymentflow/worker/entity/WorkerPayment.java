package com.example.paymentflow.worker.entity;

import java.math.BigDecimal;
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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@EntityAuditEnabled
@EntityListeners(SharedEntityAuditListener.class)
@Table(name = "worker_payments")
public class WorkerPayment extends AbstractAuditableEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "toli_id")
    private Long toliId;

    @Column(name = "employer_id")
    private Long employerId;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "month")
    private String month;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "basic_wages", precision = 15, scale = 2)
    @NotNull(message = "Basic wages is required")
    @DecimalMin(value = "0.00", message = "Basic wages must be non-negative")
    private BigDecimal basicWages;

    @Column(name = "advance", precision = 15, scale = 2)
    private BigDecimal advance;

    @Column(name = "gross_wages", precision = 15, scale = 2)
    private BigDecimal grossWages;

    @Column(name = "levy", precision = 10, scale = 2)
    private BigDecimal levy;

    @Column(name = "net_wages_payable", precision = 15, scale = 2)
    private BigDecimal netWagesPayable;

    @Column(name = "payment_type")
    private String paymentType;

    @Column(name = "txn_ref")
    private String txnRef;

    @Column(name = "receipt_nmbr")
    private String receiptNmbr;

    @Column(name = "status_id")
    private Integer statusId;

    @Transient
    private String status = "ACTIVE";

    // Legacy/transient fields kept for compatibility with older code paths
    @Transient
    private String workerRef;

    @Transient
    private String regId;

    @Transient
    private String name;

    @Transient
    private String toli;

    @Transient
    private String aadhar;

    @Transient
    private String pan;

    @Transient
    private String bankAccount;

    @Transient
    private BigDecimal paymentAmount;

    @Transient
    private String fileId;

    @Transient
    private String uploadedFileRef;

    @Transient
    private String requestReferenceNumber;

    @Transient
    private String receiptNumber;

    @Transient
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerRef() {
        return workerRef;
    }

    public void setWorkerRef(String workerRef) {
        this.workerRef = workerRef;
    }

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(Long workerId) {
        this.workerId = workerId;
    }

    public Long getToliId() {
        return toliId;
    }

    public void setToliId(Long toliId) {
        this.toliId = toliId;
    }

    public Long getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public BigDecimal getBasicWages() {
        return basicWages;
    }

    public void setBasicWages(BigDecimal basicWages) {
        this.basicWages = basicWages;
    }

    public BigDecimal getAdvance() {
        return advance;
    }

    public void setAdvance(BigDecimal advance) {
        this.advance = advance;
    }

    public BigDecimal getGrossWages() {
        return grossWages;
    }

    public void setGrossWages(BigDecimal grossWages) {
        this.grossWages = grossWages;
    }

    public BigDecimal getLevy() {
        return levy;
    }

    public void setLevy(BigDecimal levy) {
        this.levy = levy;
    }

    public BigDecimal getNetWagesPayable() {
        return netWagesPayable;
    }

    public void setNetWagesPayable(BigDecimal netWagesPayable) {
        this.netWagesPayable = netWagesPayable;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getTxnRef() {
        return txnRef;
    }

    public void setTxnRef(String txnRef) {
        this.txnRef = txnRef;
    }

    public String getReceiptNmbr() {
        return receiptNmbr;
    }

    public void setReceiptNmbr(String receiptNmbr) {
        this.receiptNmbr = receiptNmbr;
    }

    public BigDecimal getPaymentAmount() {
        return basicWages;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.basicWages = paymentAmount;
    }

    public String getToli() {
        return toli;
    }

    public void setToli(String toli) {
        this.toli = toli;
    }

    public String getAadhar() {
        return aadhar;
    }

    public void setAadhar(String aadhar) {
        this.aadhar = aadhar;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getUploadedFileRef() {
        return uploadedFileRef;
    }

    public void setUploadedFileRef(String uploadedFileRef) {
        this.uploadedFileRef = uploadedFileRef;
    }

    public String getRequestReferenceNumber() {
        return requestReferenceNumber;
    }

    public void setRequestReferenceNumber(String requestReferenceNumber) {
        this.requestReferenceNumber = requestReferenceNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiptNumber() {
        return receiptNmbr;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNmbr = receiptNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String entityType() {
        return "WORKER_PAYMENT";
    }

    @Override
    public Map<String, Object> auditState() {
        return auditStateOf(
                "id", id,
                "workerId", workerId,
                "employerId", employerId,
                "boardId", boardId,
                "toliId", toliId,
                "month", month,
                "totalDays", totalDays,
                "basicWages", basicWages,
                "advance", advance,
                "grossWages", grossWages,
                "levy", levy,
                "netWagesPayable", netWagesPayable,
                "paymentType", paymentType,
                "txnRef", txnRef,
                "receiptNmbr", receiptNmbr);
    }

    public void setStatusId(Integer object) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatusId'");
    }
}
