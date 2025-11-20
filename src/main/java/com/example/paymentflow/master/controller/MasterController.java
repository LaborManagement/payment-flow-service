
package com.example.paymentflow.master.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.paymentflow.master.dto.BoardMasterDto;
import com.example.paymentflow.master.dto.EmployerMasterDto;
import com.example.paymentflow.master.dto.ToliMasterDto;
import com.example.paymentflow.master.dto.WorkerMasterDto;
import com.example.paymentflow.master.entity.BoardMaster;
import com.example.paymentflow.master.entity.EmployerMaster;
import com.example.paymentflow.master.entity.ToliMaster;
import com.example.paymentflow.master.entity.WorkerMaster;
import com.example.paymentflow.master.repository.BoardMasterRepository;
import com.example.paymentflow.master.repository.EmployerMasterRepository;
import com.example.paymentflow.master.repository.ToliMasterRepository;
import com.example.paymentflow.master.repository.WorkerMasterRepository;
import com.example.paymentflow.master.service.MasterUploadService;
import com.shared.common.annotation.Auditable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/master")
@Tag(name = "Master Data", description = "APIs for master data (workers, boards, employers, toli)")
public class MasterController {

    private final MasterUploadService masterUploadService;
    private final WorkerMasterRepository workerMasterRepository;
    private final BoardMasterRepository boardMasterRepository;
    private final EmployerMasterRepository employerMasterRepository;
    private final ToliMasterRepository toliMasterRepository;

    @Autowired
    public MasterController(MasterUploadService masterUploadService,
            WorkerMasterRepository workerMasterRepository,
            BoardMasterRepository boardMasterRepository,
            EmployerMasterRepository employerMasterRepository,
            ToliMasterRepository toliMasterRepository) {
        this.masterUploadService = masterUploadService;
        this.workerMasterRepository = workerMasterRepository;
        this.boardMasterRepository = boardMasterRepository;
        this.employerMasterRepository = employerMasterRepository;
        this.toliMasterRepository = toliMasterRepository;
    }

    // --- GET Workers ---
    @GetMapping("/workers")
    @Operation(summary = "Get all workers", description = "Returns all workers in the master table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of workers returned")
    })
    @Auditable(action = "GET_WORKERS", resourceType = "WORKER_MASTER")
    public ResponseEntity<List<WorkerMasterDto>> getAllWorkers() {
        List<WorkerMasterDto> workers = workerMasterRepository.findAll().stream().map(this::toWorkerDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(workers);
    }

    @GetMapping("/workers/{id}")
    @Operation(summary = "Get worker by ID", description = "Returns a worker by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Worker found"),
            @ApiResponse(responseCode = "404", description = "Worker not found")
    })
    @Auditable(action = "GET_WORKER_BY_ID", resourceType = "WORKER_MASTER")
    public ResponseEntity<WorkerMasterDto> getWorkerById(@PathVariable Long id) {
        Optional<WorkerMaster> worker = workerMasterRepository.findById(id);
        return worker.map(w -> ResponseEntity.ok(toWorkerDto(w)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GET Boards ---
    @GetMapping("/boards")
    @Operation(summary = "Get all boards", description = "Returns all boards in the master table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of boards returned")
    })
    @Auditable(action = "GET_BOARDS", resourceType = "BOARD_MASTER")
    public ResponseEntity<List<BoardMasterDto>> getAllBoards() {
        List<BoardMasterDto> boards = boardMasterRepository.findAll().stream().map(this::toBoardDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/boards/{id}")
    @Operation(summary = "Get board by ID", description = "Returns a board by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Board found"),
            @ApiResponse(responseCode = "404", description = "Board not found")
    })
    @Auditable(action = "GET_BOARD_BY_ID", resourceType = "BOARD_MASTER")
    public ResponseEntity<BoardMasterDto> getBoardById(@PathVariable Long id) {
        Optional<BoardMaster> board = boardMasterRepository.findById(id);
        return board.map(b -> ResponseEntity.ok(toBoardDto(b)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GET Employers ---
    @GetMapping("/employers")
    @Operation(summary = "Get all employers", description = "Returns all employers in the master table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of employers returned")
    })
    @Auditable(action = "GET_EMPLOYERS", resourceType = "EMPLOYER_MASTER")
    public ResponseEntity<List<EmployerMasterDto>> getAllEmployers() {
        List<EmployerMasterDto> employers = employerMasterRepository.findAll().stream().map(this::toEmployerDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(employers);
    }

    @GetMapping("/employers/{id}")
    @Operation(summary = "Get employer by ID", description = "Returns an employer by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer found"),
            @ApiResponse(responseCode = "404", description = "Employer not found")
    })
    @Auditable(action = "GET_EMPLOYER_BY_ID", resourceType = "EMPLOYER_MASTER")
    public ResponseEntity<EmployerMasterDto> getEmployerById(@PathVariable Long id) {
        Optional<EmployerMaster> employer = employerMasterRepository.findById(id);
        return employer.map(e -> ResponseEntity.ok(toEmployerDto(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- GET Toli ---
    @GetMapping("/toli")
    @Operation(summary = "Get all toli", description = "Returns all toli in the master table.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of toli returned")
    })
    @Auditable(action = "GET_TOLI", resourceType = "TOLI_MASTER")
    public ResponseEntity<List<ToliMasterDto>> getAllToli() {
        List<ToliMasterDto> toli = toliMasterRepository.findAll().stream().map(this::toToliDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(toli);
    }

    @GetMapping("/toli/{id}")
    @Operation(summary = "Get toli by ID", description = "Returns a toli by its ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Toli found"),
            @ApiResponse(responseCode = "404", description = "Toli not found")
    })
    @Auditable(action = "GET_TOLI_BY_ID", resourceType = "TOLI_MASTER")
    public ResponseEntity<ToliMasterDto> getToliById(@PathVariable Long id) {
        Optional<ToliMaster> toli = toliMasterRepository.findById(id);
        return toli.map(t -> ResponseEntity.ok(toToliDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Entity to DTO mappers ---
    private WorkerMasterDto toWorkerDto(WorkerMaster w) {
        WorkerMasterDto dto = new WorkerMasterDto();
        dto.setId(w.getId());
        dto.setBoardId(w.getBoardId());
        dto.setWorkerNameMarathi(w.getWorkerNameMarathi());
        return dto;
    }

    private BoardMasterDto toBoardDto(BoardMaster b) {
        BoardMasterDto dto = new BoardMasterDto();
        dto.setId(b.getId());
        dto.setBoardId(b.getBoardId());
        dto.setBoardCode(b.getBoardCode());
        dto.setBoardName(b.getBoardName());
        return dto;
    }

    private EmployerMasterDto toEmployerDto(EmployerMaster e) {
        EmployerMasterDto dto = new EmployerMasterDto();
        dto.setId(e.getId());
        dto.setBoardId(e.getBoardId());
        dto.setRegistrationNumber(e.getRegistrationNo());
        return dto;
    }

    private ToliMasterDto toToliDto(ToliMaster t) {
        ToliMasterDto dto = new ToliMasterDto();
        dto.setId(t.getId());
        dto.setBoardId(t.getBoardId());
        dto.setEmployerId(t.getEmployerId());
        dto.setRegistrationNumber(t.getRegistrationNumber());
        return dto;
    }

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
