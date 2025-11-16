package com.example.paymentflow.master.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface MasterUploadService {
    ResponseEntity<?> uploadEmployerMaster(MultipartFile file);

    ResponseEntity<?> uploadToliMaster(MultipartFile file);

    ResponseEntity<?> uploadWorkerMaster(MultipartFile file);

    ResponseEntity<?> uploadBoardMaster(MultipartFile file);
}
