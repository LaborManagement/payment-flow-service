package com.example.paymentflow.utilities.file;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "uploaded_files")
public class UploadedFile {

    @Column(name = "board_id")
    private Integer boardId;

    @Column(name = "employer_id")
    private Integer employerId;

    @Column(name = "toli_id")
    private Integer toliId;

    public Integer getBoardId() {
        return boardId;
    }

    public void setBoardId(Integer boardId) {
        this.boardId = boardId;
    }

    public Integer getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Integer employerId) {
        this.employerId = employerId;
    }

    public Integer getToliId() {
        return toliId;
    }

    public void setToliId(Integer toliId) {
        this.toliId = toliId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime uploadDate;

    @Column(name = "uploaded_by", nullable = true, length = 100)
    private String uploadedBy;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords = 0;

    @Column(name = "success_count", nullable = false)
    private Integer successCount = 0;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount = 0;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "file_reference_number", nullable = true, unique = true, length = 100)
    private String fileReferenceNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public void setStoredPath(String storedPath) {
        this.storedPath = storedPath;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Integer getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(Integer totalRecords) {
        this.totalRecords = totalRecords;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Integer failureCount) {
        this.failureCount = failureCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileReferenceNumber() {
        return fileReferenceNumber;
    }

    public void setFileReferenceNumber(String fileReferenceNumber) {
        this.fileReferenceNumber = fileReferenceNumber;
    }

}
