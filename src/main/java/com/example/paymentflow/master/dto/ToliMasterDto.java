package com.example.paymentflow.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Toli master data")
public class ToliMasterDto {
    @Schema(description = "Toli ID", example = "1")
    private Long id;
    @Schema(description = "Board ID", example = "101")
    private Long boardId;
    @Schema(description = "Employer ID", example = "EMP001")
    private String employerId;
    @Schema(description = "Registration number", example = "REGTOLI123")
    private String registrationNumber;

    // ... add other fields as needed
    // Getters and setters
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

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }
}
