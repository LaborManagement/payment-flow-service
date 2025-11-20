package com.example.paymentflow.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Worker master data")
public class WorkerMasterDto {
    @Schema(description = "Worker ID", example = "1")
    private Long id;
    @Schema(description = "Board ID", example = "101")
    private Long boardId;
    @Schema(description = "Worker name (Marathi)", example = "राम")
    private String workerNameMarathi;

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

    public String getWorkerNameMarathi() {
        return workerNameMarathi;
    }

    public void setWorkerNameMarathi(String workerNameMarathi) {
        this.workerNameMarathi = workerNameMarathi;
    }
}
