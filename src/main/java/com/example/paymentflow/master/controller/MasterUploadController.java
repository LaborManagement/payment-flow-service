package com.example.paymentflow.master.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.paymentflow.master.service.MasterUploadService;
import com.shared.common.annotation.Auditable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;

@RestController
@RequestMapping("/api/master/uploads")
public class MasterUploadController {

    private final MasterUploadService masterUploadService;

    public MasterUploadController(MasterUploadService masterUploadService) {
        this.masterUploadService = masterUploadService;
    }

    @PostMapping(value = "/employers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Employer Master", description = "Upload employer master data as a file")
    @Auditable(action = "EMPLOYER_MASTER_UPLOAD", resourceType = "EMPLOYER_MASTER")
    public ResponseEntity<?> uploadEmployerMaster(
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file) {
        return masterUploadService.uploadEmployerMaster(file);
    }

    @PostMapping(value = "/toli", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Toli Master", description = "Upload toli master data as a file")
    @Auditable(action = "TOLI_MASTER_UPLOAD", resourceType = "TOLI_MASTER")
    public ResponseEntity<?> uploadToliMaster(
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file) {
        return masterUploadService.uploadToliMaster(file);
    }

    @PostMapping(value = "/workers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Worker Master", description = "Upload worker master data as a file")
    @Auditable(action = "WORKER_MASTER_UPLOAD", resourceType = "WORKER_MASTER")
    public ResponseEntity<?> uploadWorkerMaster(
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file) {
        return masterUploadService.uploadWorkerMaster(file);
    }

    @PostMapping(value = "/boards", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload Board Master", description = "Upload board master data as a file")
    @Auditable(action = "BOARD_MASTER_UPLOAD", resourceType = "BOARD_MASTER")
    public ResponseEntity<?> uploadBoardMaster(
            @Parameter(description = "File to upload", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)) @RequestParam("file") MultipartFile file) {
        return masterUploadService.uploadBoardMaster(file);
    }
}
