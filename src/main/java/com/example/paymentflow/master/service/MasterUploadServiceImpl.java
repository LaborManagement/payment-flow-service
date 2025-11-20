package com.example.paymentflow.master.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.paymentflow.master.entity.BoardMaster;
import com.example.paymentflow.master.entity.EmployerMaster;
import com.example.paymentflow.master.entity.ToliMaster;
import com.example.paymentflow.master.entity.WorkerMaster;
import com.example.paymentflow.master.repository.BoardMasterRepository;
import com.example.paymentflow.master.repository.EmployerMasterRepository;
import com.example.paymentflow.master.repository.ToliMasterRepository;
import com.example.paymentflow.master.repository.WorkerMasterRepository;
import com.example.paymentflow.master.util.MasterFileParser;
import com.shared.common.dao.TenantAccessDao;
import com.shared.security.JwtAuthenticationDetails;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Placeholder for shared-lib upload utility import
// import com.shared.common.upload.GenericUploadService;

@Service

public class MasterUploadServiceImpl implements MasterUploadService {
    private final TenantAccessDao tenantAccessDao;
    private final EmployerMasterRepository employerMasterRepository;
    private final ToliMasterRepository toliMasterRepository;
    private final WorkerMasterRepository workerMasterRepository;
    private final BoardMasterRepository boardMasterRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public MasterUploadServiceImpl(
            TenantAccessDao tenantAccessDao,
            EmployerMasterRepository employerMasterRepository,
            ToliMasterRepository toliMasterRepository,
            WorkerMasterRepository workerMasterRepository,
            BoardMasterRepository boardMasterRepository) {
        this.tenantAccessDao = tenantAccessDao;
        this.employerMasterRepository = employerMasterRepository;
        this.toliMasterRepository = toliMasterRepository;
        this.workerMasterRepository = workerMasterRepository;
        this.boardMasterRepository = boardMasterRepository;
    }

    // Inject the shared-lib upload utility/service here when available
    // private final GenericUploadService genericUploadService;

    // @Autowired
    // public MasterUploadServiceImpl(GenericUploadService genericUploadService) {
    // this.genericUploadService = genericUploadService;
    // }

    private UserContext getUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object details = authentication.getDetails();
        Long userId = null;
        if (details instanceof JwtAuthenticationDetails jwtDetails) {
            userId = jwtDetails.getUserId();
        }
        if (userId == null) {
            throw new IllegalStateException("User ID not found in authentication context");
        }
        TenantAccessDao.TenantAccess tenantAccess = tenantAccessDao.getFirstAccessibleTenant();
        if (tenantAccess == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User has no tenant access assigned");
        }
        String boardId = tenantAccess.boardId != null ? tenantAccess.boardId.toString() : null;
        String employerId = tenantAccess.employerId != null ? tenantAccess.employerId.toString() : null;
        return new UserContext(userId.toString(), boardId, employerId);
    }

    @Override
    public ResponseEntity<?> uploadEmployerMaster(MultipartFile file) {
        UserContext userContext = getUserContext();
        String filename = file.getOriginalFilename();
        try {
            List<EmployerMaster> entities;
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                entities = MasterFileParser.parseEmployerCsv(file, userContext.getBoardId(),
                        userContext.getEmployerId());
            } else if (filename != null
                    && (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx"))) {
                entities = MasterFileParser.parseEmployerXls(file, userContext.getBoardId(),
                        userContext.getEmployerId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
            }
            employerMasterRepository.saveAll(entities);
            return ResponseEntity.ok("Employer master upload successful: " + entities.size() + " records");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> uploadToliMaster(MultipartFile file) {
        UserContext userContext = getUserContext();
        String filename = file.getOriginalFilename();
        try {
            List<ToliMaster> entities;
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                entities = MasterFileParser.parseToliCsv(file, userContext.getBoardId(), userContext.getEmployerId());
            } else if (filename != null
                    && (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx"))) {
                entities = MasterFileParser.parseToliXls(file, userContext.getBoardId(), userContext.getEmployerId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
            }
            toliMasterRepository.saveAll(entities);
            return ResponseEntity.ok("Toli master upload successful: " + entities.size() + " records");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public ResponseEntity<?> uploadWorkerMaster(MultipartFile file) {
        UserContext userContext = getUserContext();
        String filename = file.getOriginalFilename();
        try {
            List<WorkerMaster> entities;
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                entities = MasterFileParser.parseWorkerCsv(file, userContext.getBoardId(), userContext.getEmployerId());
            } else if (filename != null
                    && (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx"))) {
                entities = MasterFileParser.parseWorkerXls(file, userContext.getBoardId(), userContext.getEmployerId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
            }
            workerMasterRepository.saveAll(entities);
            return ResponseEntity.ok("Worker master upload successful: " + entities.size() + " records");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> uploadBoardMaster(MultipartFile file) {
        UserContext userContext = getUserContext();
        String boardId = requireBoardId(userContext);
        setBoardContext(boardId);
        String filename = file.getOriginalFilename();
        try {
            List<BoardMaster> entities;
            if (filename != null && filename.toLowerCase().endsWith(".csv")) {
                entities = MasterFileParser.parseBoardCsv(file, boardId, userContext.getEmployerId());
            } else if (filename != null
                    && (filename.toLowerCase().endsWith(".xls") || filename.toLowerCase().endsWith(".xlsx"))) {
                entities = MasterFileParser.parseBoardXls(file, boardId, userContext.getEmployerId());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
            }
            boardMasterRepository.saveAll(entities);
            return ResponseEntity.ok("Board master upload successful: " + entities.size() + " records");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed: " + e.getMessage(), e);
        }
    }

    // Simple user context holder for demonstration
    private static class UserContext {
        private final String userId;
        private final String boardId;
        private final String employerId;

        public UserContext(String userId, String boardId, String employerId) {
            this.userId = userId;
            this.boardId = boardId;
            this.employerId = employerId;
        }

        public String getUserId() {
            return userId;
        }

        public String getBoardId() {
            return boardId;
        }

        public String getEmployerId() {
            return employerId;
        }
    }

    private void setBoardContext(String boardId) {
        entityManager.createNativeQuery("SELECT set_config('app.current_board_id', :boardId, false)")
                .setParameter("boardId", boardId)
                .getSingleResult();
    }

    private String requireBoardId(UserContext userContext) {
        String boardId = userContext.getBoardId();
        if (boardId == null || boardId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Board context is required to upload board master data");
        }
        return boardId;
    }
}
