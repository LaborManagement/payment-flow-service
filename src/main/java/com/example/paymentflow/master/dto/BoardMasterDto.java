package com.example.paymentflow.master.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Board master data")
public class BoardMasterDto {
    @Schema(description = "ID (PK)", example = "1")
    private Long id;
    @Schema(description = "Board ID", example = "101")
    private Long boardId;
    @Schema(description = "Board code", example = "BRD001")
    private String boardCode;
    @Schema(description = "Board name", example = "Maharashtra Board")
    private String boardName;

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

    public String getBoardCode() {
        return boardCode;
    }

    public void setBoardCode(String boardCode) {
        this.boardCode = boardCode;
    }

    public String getBoardName() {
        return boardName;
    }

    public void setBoardName(String boardName) {
        this.boardName = boardName;
    }
}
