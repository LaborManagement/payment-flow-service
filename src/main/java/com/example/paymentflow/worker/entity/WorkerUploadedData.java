
package com.example.paymentflow.worker.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "worker_uploaded_data", schema = "payment_flow")
public class WorkerUploadedData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "employer_id")
    private Long employerId;

    @Column(name = "toli_id")
    private Long toliId;

    @Column(name = "worker_regno", length = 50)
    private String workerRegno;

    @Column(name = "employee_name", length = 255)
    private String employeeName;

    @Column(name = "employer_reg_no")
    private String employerRegNo;

    @Column(name = "toli_reg_no")
    private String toliRegNo;

    @Column(name = "year_month", length = 50)
    private String month;

    @Column(name = "day1")
    private Integer day1;
    @Column(name = "day2")
    private Integer day2;
    @Column(name = "day3")
    private Integer day3;
    @Column(name = "day4")
    private Integer day4;
    @Column(name = "day5")
    private Integer day5;
    @Column(name = "day6")
    private Integer day6;
    @Column(name = "day7")
    private Integer day7;
    @Column(name = "day8")
    private Integer day8;
    @Column(name = "day9")
    private Integer day9;
    @Column(name = "day10")
    private Integer day10;
    @Column(name = "day11")
    private Integer day11;
    @Column(name = "day12")
    private Integer day12;
    @Column(name = "day13")
    private Integer day13;
    @Column(name = "day14")
    private Integer day14;
    @Column(name = "day15")
    private Integer day15;
    @Column(name = "day16")
    private Integer day16;
    @Column(name = "day17")
    private Integer day17;
    @Column(name = "day18")
    private Integer day18;
    @Column(name = "day19")
    private Integer day19;
    @Column(name = "day20")
    private Integer day20;
    @Column(name = "day21")
    private Integer day21;
    @Column(name = "day22")
    private Integer day22;
    @Column(name = "day23")
    private Integer day23;
    @Column(name = "day24")
    private Integer day24;
    @Column(name = "day25")
    private Integer day25;
    @Column(name = "day26")
    private Integer day26;
    @Column(name = "day27")
    private Integer day27;
    @Column(name = "day28")
    private Integer day28;
    @Column(name = "day29")
    private Integer day29;
    @Column(name = "day30")
    private Integer day30;
    @Column(name = "day31")
    private Integer day31;

    @Column(name = "total_days")
    private Integer totalDays;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "advance", precision = 10, scale = 2)
    private BigDecimal advance;

    @Column(name = "net_payable", precision = 10, scale = 2)
    private BigDecimal netPayable;

    @Column(name = "payment_type", length = 100)
    private String paymentType;

    @Column(name = "txn_ref", length = 255)
    private String txnRef;

    @Column(name = "status_id")
    private Integer statusId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.time.LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public Long getEmployerId() {
        return employerId;
    }

    public void setEmployerId(Long employerId) {
        this.employerId = employerId;
    }

    public Long getToliId() {
        return toliId;
    }

    public void setToliId(Long toliId) {
        this.toliId = toliId;
    }

    // Getters and setters for all fields
    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public WorkerUploadedData() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWorkerRegno() {
        return workerRegno;
    }

    public void setWorkerRegno(String workerRegno) {
        this.workerRegno = workerRegno;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployerRegNo() {
        return employerRegNo;
    }

    public void setEmployerRegNo(String employerRegNo) {
        this.employerRegNo = employerRegNo;
    }

    public String getToliRegNo() {
        return toliRegNo;
    }

    public void setToliRegNo(String toliRegNo) {
        this.toliRegNo = toliRegNo;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getDay1() {
        return day1;
    }

    public void setDay1(Integer day1) {
        this.day1 = day1;
    }

    public Integer getDay2() {
        return day2;
    }

    public void setDay2(Integer day2) {
        this.day2 = day2;
    }

    public Integer getDay3() {
        return day3;
    }

    public void setDay3(Integer day3) {
        this.day3 = day3;
    }

    public Integer getDay4() {
        return day4;
    }

    public void setDay4(Integer day4) {
        this.day4 = day4;
    }

    public Integer getDay5() {
        return day5;
    }

    public void setDay5(Integer day5) {
        this.day5 = day5;
    }

    public Integer getDay6() {
        return day6;
    }

    public void setDay6(Integer day6) {
        this.day6 = day6;
    }

    public Integer getDay7() {
        return day7;
    }

    public void setDay7(Integer day7) {
        this.day7 = day7;
    }

    public Integer getDay8() {
        return day8;
    }

    public void setDay8(Integer day8) {
        this.day8 = day8;
    }

    public Integer getDay9() {
        return day9;
    }

    public void setDay9(Integer day9) {
        this.day9 = day9;
    }

    public Integer getDay10() {
        return day10;
    }

    public void setDay10(Integer day10) {
        this.day10 = day10;
    }

    public Integer getDay11() {
        return day11;
    }

    public void setDay11(Integer day11) {
        this.day11 = day11;
    }

    public Integer getDay12() {
        return day12;
    }

    public void setDay12(Integer day12) {
        this.day12 = day12;
    }

    public Integer getDay13() {
        return day13;
    }

    public void setDay13(Integer day13) {
        this.day13 = day13;
    }

    public Integer getDay14() {
        return day14;
    }

    public void setDay14(Integer day14) {
        this.day14 = day14;
    }

    public Integer getDay15() {
        return day15;
    }

    public void setDay15(Integer day15) {
        this.day15 = day15;
    }

    public Integer getDay16() {
        return day16;
    }

    public void setDay16(Integer day16) {
        this.day16 = day16;
    }

    public Integer getDay17() {
        return day17;
    }

    public void setDay17(Integer day17) {
        this.day17 = day17;
    }

    public Integer getDay18() {
        return day18;
    }

    public void setDay18(Integer day18) {
        this.day18 = day18;
    }

    public Integer getDay19() {
        return day19;
    }

    public void setDay19(Integer day19) {
        this.day19 = day19;
    }

    public Integer getDay20() {
        return day20;
    }

    public void setDay20(Integer day20) {
        this.day20 = day20;
    }

    public Integer getDay21() {
        return day21;
    }

    public void setDay21(Integer day21) {
        this.day21 = day21;
    }

    public Integer getDay22() {
        return day22;
    }

    public void setDay22(Integer day22) {
        this.day22 = day22;
    }

    public Integer getDay23() {
        return day23;
    }

    public void setDay23(Integer day23) {
        this.day23 = day23;
    }

    public Integer getDay24() {
        return day24;
    }

    public void setDay24(Integer day24) {
        this.day24 = day24;
    }

    public Integer getDay25() {
        return day25;
    }

    public void setDay25(Integer day25) {
        this.day25 = day25;
    }

    public Integer getDay26() {
        return day26;
    }

    public void setDay26(Integer day26) {
        this.day26 = day26;
    }

    public Integer getDay27() {
        return day27;
    }

    public void setDay27(Integer day27) {
        this.day27 = day27;
    }

    public Integer getDay28() {
        return day28;
    }

    public void setDay28(Integer day28) {
        this.day28 = day28;
    }

    public Integer getDay29() {
        return day29;
    }

    public void setDay29(Integer day29) {
        this.day29 = day29;
    }

    public Integer getDay30() {
        return day30;
    }

    public void setDay30(Integer day30) {
        this.day30 = day30;
    }

    public Integer getDay31() {
        return day31;
    }

    public void setDay31(Integer day31) {
        this.day31 = day31;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAdvance() {
        return advance;
    }

    public void setAdvance(BigDecimal advance) {
        this.advance = advance;
    }

    public BigDecimal getNetPayable() {
        return netPayable;
    }

    public void setNetPayable(BigDecimal netPayable) {
        this.netPayable = netPayable;
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
}
