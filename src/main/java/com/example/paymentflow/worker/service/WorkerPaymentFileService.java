package com.example.paymentflow.worker.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.paymentflow.utilities.file.FileStorageUtil;
import com.example.paymentflow.utilities.file.UploadedFile;
import com.example.paymentflow.utilities.file.UploadedFileRepository;
import com.example.paymentflow.worker.entity.WorkerPayment;
import com.example.paymentflow.worker.entity.WorkerPaymentReceipt;
import com.shared.common.dao.TenantAccessDao;
import com.shared.utilities.logger.LoggerFactoryProvider;

@Service
public class WorkerPaymentFileService {
    private static final Logger log = LoggerFactoryProvider.getLogger(WorkerPaymentFileService.class);

    @Autowired
    private FileStorageUtil fileStorageUtil;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private WorkerPaymentService workerPaymentService;

    @Autowired
    private WorkerPaymentReceiptService receiptService;

    @Autowired
    private WorkerUploadedDataService workerUploadedDataService;

    @Autowired
    private TenantAccessDao tenantAccessDao;

    @Autowired
    private DbProcedureExecutor dbProcedureExecutor;

    private TenantAccessDao.TenantAccess requireTenantAccess() {
        TenantAccessDao.TenantAccess tenantAccess = tenantAccessDao.getFirstAccessibleTenant();
        if (tenantAccess == null || tenantAccess.boardId == null) {
            throw new IllegalStateException("User has no tenant access assigned for uploads");
        }
        return tenantAccess;
    }

    public Map<String, Object> handleFileUpload(MultipartFile file) {
        log.info("Received file upload: name={}, size={} bytes", file.getOriginalFilename(), file.getSize());

        try {
            TenantAccessDao.TenantAccess tenantAccess = requireTenantAccess();
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Long boardId = tenantAccess.boardId != null ? tenantAccess.boardId.longValue() : null;
            Long employerId = tenantAccess.employerId != null ? tenantAccess.employerId.longValue() : null;
            Long toliId = tenantAccess.toliId != null ? tenantAccess.toliId.longValue() : null;

            // Use the new method that returns the entity directly to avoid lookup issues
            UploadedFile uploadedFile = fileStorageUtil.storeFileAndReturnEntity(file, "workerpayments", fileName,
                    tenantAccess.boardId, tenantAccess.employerId, tenantAccess.toliId);
            String storedPath = uploadedFile.getStoredPath();
            Long fileId = uploadedFile.getId();

            log.info("File saved to {} with fileId: {}", storedPath, fileId);

            // Parse the file and extract worker uploaded data
            File fileToRead = new File(storedPath);
            List<com.example.paymentflow.worker.entity.WorkerUploadedData> uploadedDataList = parseFileToUploadedData(
                    fileToRead, file.getOriginalFilename(), fileId, boardId, employerId, toliId);

            // Update the uploaded file record with parsing results
            uploadedFile.setTotalRecords(uploadedDataList.size());
            uploadedFile.setSuccessCount(0); // Will be updated after validation
            uploadedFile.setFailureCount(0);
            uploadedFile.setStatus("UPLOADED");
            uploadedFileRepository.save(uploadedFile);

            // Save uploaded data to WorkerUploadedData table
            List<com.example.paymentflow.worker.entity.WorkerUploadedData> savedData = workerUploadedDataService
                    .saveAll(uploadedDataList);

            log.info("File {} parsed and {} records saved to WorkerUploadedData (fileId={})",
                    file.getOriginalFilename(), savedData.size(), fileId);

            // Run DB-side validation and payments
            DbProcedureExecutor.ValidationOutcome validationOutcome = null;
            String receiptNumber = null;
            String procError = null;
            try {
                validationOutcome = dbProcedureExecutor.validateUploadedData(fileId);
                uploadedFile.setSuccessCount(validationOutcome.validCount);
                uploadedFile.setFailureCount(validationOutcome.invalidCount);
                uploadedFile.setStatus(validationOutcome.allValid ? "VALIDATED" : "VALIDATION_FAILED");
                uploadedFileRepository.save(uploadedFile);

                if (validationOutcome.allValid && validationOutcome.validCount > 0) {
                    receiptNumber = dbProcedureExecutor.createPayments(fileId);
                    uploadedFile.setStatus("REQUEST_GENERATED");
                    uploadedFileRepository.save(uploadedFile);
                }
            } catch (Exception procEx) {
                log.error("Proc execution failed for fileId={}", fileId, procEx);
                procError = procEx.getMessage();
                uploadedFile.setStatus("VALIDATION_FAILED");
                uploadedFileRepository.save(uploadedFile);
            }

            // Create response map step by step to identify any null values
            Map<String, Object> response = new HashMap<>();
            response.put("fileId", fileId);
            response.put("message",
                    "File uploaded successfully. Validation executed in DB.");
            response.put("path", storedPath);
            response.put("recordCount", savedData.size());
            if (validationOutcome != null) {
                response.put("validation", Map.of(
                        "total", validationOutcome.totalRecords,
                        "valid", validationOutcome.validCount,
                        "invalid", validationOutcome.invalidCount,
                        "raw", validationOutcome.rawJson));
            }
            if (receiptNumber != null) {
                response.put("receiptNumber", receiptNumber);
                response.put("status", "REQUEST_GENERATED");
            } else {
                response.put("status", uploadedFile.getStatus());
            }
            if (procError != null) {
                response.put("procError", procError);
            }

            log.info("Returning response: {}", response);
            return response;

        } catch (Exception e) {
            log.error("Failed to process uploaded file", e);
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = e.getClass().getSimpleName() + " - " + e.toString();
            }
            return Map.of("error", "Failed to process uploaded file: " + errorMessage);
        }
    }

    public Map<String, Object> validateFileRecords(String fileId) {
        log.info("Validating records for fileId={}", fileId);

        try {
            Long uploadedFileId = Long.parseLong(fileId);
            Optional<UploadedFile> uploadedFileOpt = uploadedFileRepository.findById(uploadedFileId);

            if (uploadedFileOpt.isEmpty()) {
                return Map.of("error", "File not found");
            }

            // Validate uploaded data using the new service
            workerUploadedDataService.validateUploadedData(fileId);

            // Get validation summary
            Map<String, Integer> summary = workerUploadedDataService.getFileStatusSummary(fileId);
            int passedCount = summary.getOrDefault("VALIDATED", 0);
            int failedCount = summary.getOrDefault("REJECTED", 0);

            // Update the uploaded file record with validation results
            UploadedFile uploadedFile = uploadedFileOpt.get();
            uploadedFile.setSuccessCount(passedCount);
            uploadedFile.setFailureCount(failedCount);
            uploadedFile.setStatus("COMPLETED");
            uploadedFileRepository.save(uploadedFile);

            log.info("Validation complete for fileId={}: {} passed, {} failed", fileId, passedCount, failedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("passed", passedCount);
            response.put("failed", failedCount);
            response.put("status", "COMPLETED");
            response.put("nextAction", "GENERATE_REQUEST");
            response.put("message",
                    "Validation completed. " + passedCount + " records passed validation. Ready to generate request.");

            return response;

        } catch (Exception e) {
            log.error("Error validating records for fileId={}", fileId, e);
            return Map.of("error", "Validation failed: " + e.getMessage());
        }
    }

    public Map<String, Object> getValidationResults(String fileId) {
        log.info("Fetching validation results for fileId={}", fileId);

        try {
            // Get all uploaded data for this file
            List<com.example.paymentflow.worker.entity.WorkerUploadedData> uploadedDataList = workerUploadedDataService
                    .findByFileId(fileId);

            List<Map<String, Object>> passedRecords = new ArrayList<>();
            List<Map<String, Object>> failedRecords = new ArrayList<>();

            for (com.example.paymentflow.worker.entity.WorkerUploadedData data : uploadedDataList) {
                Map<String, Object> record = createUploadedDataSummary(data);

                if ("2".equals(data.getStatusId())) {
                    passedRecords.add(record);
                } else if ("3".equals(data.getStatusId())) {
                    failedRecords.add(record);
                }
            }

            return Map.of(
                    "passedRecords", passedRecords,
                    "failedRecords", failedRecords);

        } catch (Exception e) {
            log.error("Error fetching validation results for fileId={}", fileId, e);
            return Map.of("error", "Failed to fetch results: " + e.getMessage());
        }
    }

    private Map<String, Object> createUploadedDataSummary(
            com.example.paymentflow.worker.entity.WorkerUploadedData data) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", data.getId());
        summary.put("workerId", data.getWorkerId());
        summary.put("employeeName", data.getEmployeeName());
        summary.put("EmployerRegNo", data.getEmployerRegNo());
        summary.put("ToliRegNo", data.getToliRegNo());
        summary.put("month", data.getMonth());
        for (int i = 1; i <= 31; i++) {
            summary.put("day" + i, getDayValue(data, i));
        }
        summary.put("totalDays", data.getTotalDays());
        summary.put("amount", data.getAmount());
        summary.put("advance", data.getAdvance());
        summary.put("netPayable", data.getNetPayable());
        summary.put("paymentType", data.getPaymentType());
        summary.put("txnRef", data.getTxnRef());
        // Optionally add status, createdAt, etc. if you have them in the entity
        return summary;
    }

    // Helper to get day1-day31 values
    private Integer getDayValue(com.example.paymentflow.worker.entity.WorkerUploadedData data, int day) {
        switch (day) {
            case 1:
                return data.getDay1();
            case 2:
                return data.getDay2();
            case 3:
                return data.getDay3();
            case 4:
                return data.getDay4();
            case 5:
                return data.getDay5();
            case 6:
                return data.getDay6();
            case 7:
                return data.getDay7();
            case 8:
                return data.getDay8();
            case 9:
                return data.getDay9();
            case 10:
                return data.getDay10();
            case 11:
                return data.getDay11();
            case 12:
                return data.getDay12();
            case 13:
                return data.getDay13();
            case 14:
                return data.getDay14();
            case 15:
                return data.getDay15();
            case 16:
                return data.getDay16();
            case 17:
                return data.getDay17();
            case 18:
                return data.getDay18();
            case 19:
                return data.getDay19();
            case 20:
                return data.getDay20();
            case 21:
                return data.getDay21();
            case 22:
                return data.getDay22();
            case 23:
                return data.getDay23();
            case 24:
                return data.getDay24();
            case 25:
                return data.getDay25();
            case 26:
                return data.getDay26();
            case 27:
                return data.getDay27();
            case 28:
                return data.getDay28();
            case 29:
                return data.getDay29();
            case 30:
                return data.getDay30();
            case 31:
                return data.getDay31();
            default:
                return null;
        }
    }

    public Map<String, Object> getValidationResultsPaginated(String fileId, int page, int size,
            String status, String startDate, String endDate, String sortBy, String sortDir) {
        log.info(
                "Fetching paginated validation results for fileId={} with filters - page: {}, size: {}, status: {}",
                fileId, page, size, status);

        try {
            // Create pageable with sorting
            org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? org.springframework.data.domain.Sort.by(sortBy).descending()
                    : org.springframework.data.domain.Sort.by(sortBy).ascending();
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                    size, sort);

            org.springframework.data.domain.Page<com.example.paymentflow.worker.entity.WorkerUploadedData> dataPage;

            dataPage = workerUploadedDataService.findByFileIdPaginated(fileId, pageable);

            // Convert to summary format
            List<Map<String, Object>> records = new ArrayList<>();
            for (com.example.paymentflow.worker.entity.WorkerUploadedData data : dataPage.getContent()) {
                records.add(createUploadedDataSummary(data));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("records", records);
            response.put("totalElements", dataPage.getTotalElements());
            response.put("totalPages", dataPage.getTotalPages());
            response.put("currentPage", dataPage.getNumber());
            response.put("pageSize", dataPage.getSize());
            response.put("hasNext", dataPage.hasNext());
            response.put("hasPrevious", dataPage.hasPrevious());
            response.put("fileId", fileId);

            // Add filtering info
            response.put("appliedFilters", Map.of(
                    "status", status != null ? status : "all"));

            return response;

        } catch (Exception e) {
            log.error("Error fetching paginated validation results for fileId={}", fileId, e);
            return Map.of("error", "Failed to fetch results: " + e.getMessage());
        }
    }

    public Map<String, Object> generateRequest(String fileId) {
        log.info("Generating request for validated records in fileId={}", fileId);

        try {
            Long uploadedFileId = Long.parseLong(fileId);
            Optional<UploadedFile> uploadedFileOpt = uploadedFileRepository.findById(uploadedFileId);

            if (uploadedFileOpt.isEmpty()) {
                return Map.of("error", "File not found");
            }

            UploadedFile uploadedFile = uploadedFileOpt.get();
            String uploadedFileRef = uploadedFile.getFileReferenceNumber();

            // Generate request for validated data (keep data in WorkerUploadedData with
            // receipt number)
            int processedCount = workerUploadedDataService.generateRequestForValidatedData(fileId, uploadedFileRef);

            if (processedCount == 0) {
                return Map.of("error", "No validated records found to generate request");
            }

            // Update the uploaded file status
            uploadedFile.setStatus("REQUEST_GENERATED");
            uploadedFileRepository.save(uploadedFile);

            log.info("Request generated successfully for fileId={}: {} records processed with receipt numbers", fileId,
                    processedCount);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Request generated successfully");
            response.put("fileId", fileId);
            response.put("requestReference", uploadedFileRef);
            response.put("processedRecords", processedCount);
            response.put("status", "REQUEST_GENERATED");
            response.put("nextAction", "VIEW_REQUESTS");

            return response;

        } catch (Exception e) {
            log.error("Error generating request for fileId={}", fileId, e);
            return Map.of("error", "Request generation failed: " + e.getMessage());
        }
    }

    public Map<String, Object> processValidRecords(Long fileId) {
        log.info("Processing valid records for fileId={}", fileId);

        try {
            List<WorkerPayment> payments = workerPaymentService.findByFileId(fileId);

            int processedCount = 0;
            List<WorkerPayment> toUpdate = new ArrayList<>();

            for (WorkerPayment payment : payments) {
                // Only process records that passed validation
                if ("VALIDATED".equals(payment.getStatus())) {
                    payment.setStatus("PAYMENT_REQUESTED");
                    toUpdate.add(payment);
                    processedCount++;
                }
            }

            // Update statuses in bulk
            String receiptNumber = null;
            if (!toUpdate.isEmpty()) {
                // First, create the receipt for all processed payments
                WorkerPaymentReceipt receipt = receiptService.createReceipt(toUpdate);
                receiptNumber = receipt.getReceiptNumber();

                // Assign the receipt number to all processed payments
                for (WorkerPayment payment : toUpdate) {
                    payment.setReceiptNumber(receiptNumber);
                }

                // Update payments with new status and receipt number assignment
                workerPaymentService.updateBulk(toUpdate);

                log.info("Generated receipt {} for {} processed payments", receipt.getReceiptNumber(), processedCount);
            }

            // Keep uploaded file status as UPLOADED - do not change it to PROCESSED
            // The uploaded file status should remain UPLOADED only
            log.info("Keeping uploaded file status as UPLOADED for fileId={}", fileId);

            log.info("Receipt generation complete for fileId={}: {} records updated to PAID status", fileId,
                    processedCount);

            Map<String, Object> result = new HashMap<>();
            result.put("processed", processedCount);
            if (receiptNumber != null) {
                result.put("receiptNumber", receiptNumber);
                result.put("status", "PROCESSED");
                result.put("nextAction", "RECEIPT_GENERATED");
                result.put("message",
                        "Generated receipt for " + processedCount + " valid records. Receipt: " + receiptNumber);
            } else {
                result.put("status", "NO_VALID_RECORDS");
                result.put("nextAction", "VALIDATION_REQUIRED");
                result.put("message", "No valid records found to generate receipt.");
            }

            return result;

        } catch (Exception e) {
            log.error("Error processing records for fileId={}", fileId, e);
            return Map.of("error", "Processing failed: " + e.getMessage());
        }
    }

    public Map<String, Object> reuploadFailedRecords(String fileId, MultipartFile file) {
        log.info("Re-uploading failed records for fileId={} with file {}", fileId, file.getOriginalFilename());

        try {
            // Process the new file similar to initial upload
            Map<String, Object> result = handleFileUpload(file);

            if (result.containsKey("error")) {
                return result;
            }

            String newFileId = (String) result.get("fileId");

            // You could implement logic here to merge or replace failed records
            // For now, we'll just return success
            log.info("Re-upload complete: new fileId={}", newFileId);
            return Map.of("message", "Re-uploaded successfully with new file ID: " + newFileId);

        } catch (Exception e) {
            log.error("Error re-uploading file for fileId={}", fileId, e);
            return Map.of("error", "Re-upload failed: " + e.getMessage());
        }
    }

    public Map<String, Object> getFileStatusSummary(Long fileId) {
        log.info("Getting status summary for fileId={}", fileId);

        try {
            List<WorkerPayment> payments = workerPaymentService.findByFileId(fileId);

            Map<String, Integer> statusCounts = new HashMap<>();

            // Initialize counts
            String[] statuses = { "UPLOADED", "VALIDATED", "FAILED", "PROCESSED", "PAYMENT_REQUESTED",
                    "PAYMENT_INITIATED", "PAYMENT_PROCESSED", "PAYMENT_RECONCILED", "GENERATED", "ERROR" };
            for (String status : statuses) {
                statusCounts.put(status, 0);
            }

            // Count by status
            for (WorkerPayment payment : payments) {
                String status = payment.getStatus();
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileId", fileId);
            result.put("totalRecords", payments.size());
            result.put("uploadedCount", statusCounts.get("UPLOADED"));
            result.put("validatedCount", statusCounts.get("VALIDATED"));
            result.put("failedCount", statusCounts.get("FAILED"));
            result.put("processedCount", statusCounts.get("PROCESSED"));
            result.put("paymentRequestedCount", statusCounts.get("PAYMENT_REQUESTED"));
            result.put("paymentInitiatedCount", statusCounts.get("PAYMENT_INITIATED"));
            result.put("paymentProcessedCount", statusCounts.get("PAYMENT_PROCESSED"));
            result.put("generatedCount", statusCounts.get("GENERATED"));
            result.put("errorCount", statusCounts.get("ERROR"));

            // Determine workflow status and next action
            String workflowStatus = determineWorkflowStatus(statusCounts);
            String nextAction = determineNextAction(workflowStatus, statusCounts);

            result.put("workflowStatus", workflowStatus);
            result.put("nextAction", nextAction);

            return result;

        } catch (Exception e) {
            log.error("Error getting status summary for fileId={}", fileId, e);
            return Map.of("error", "Failed to get status summary: " + e.getMessage());
        }
    }

    private String determineWorkflowStatus(Map<String, Integer> statusCounts) {
        // Check if any records have been processed (receipts generated)
        if (statusCounts.get("PAYMENT_REQUESTED") > 0 ||
                statusCounts.get("GENERATED") > 0) {
            return "PROCESSED";
        }

        // Check if validation is complete
        if (statusCounts.get("VALIDATED") > 0 ||
                statusCounts.get("FAILED") > 0) {
            return "VALIDATED";
        }

        // Default to uploaded status
        if (statusCounts.get("UPLOADED") > 0) {
            return "UPLOADED";
        }

        return "UNKNOWN";
    }

    private String determineNextAction(String workflowStatus, Map<String, Integer> statusCounts) {
        switch (workflowStatus) {
            case "UPLOADED":
                return "START_VALIDATION";
            case "VALIDATED":
                // Only show generate receipt if there are validated records
                if (statusCounts.get("VALIDATED") > 0) {
                    return "GENERATE_RECEIPT";
                } else {
                    return "START_VALIDATION"; // All failed validation
                }
            case "PROCESSED":
                return "RECEIPT_GENERATED";
            default:
                return "START_VALIDATION";
        }
    }

    // Debug method to get worker payments by fileId
    public List<WorkerPayment> getWorkerPaymentsByFileId(Long fileId) {
        return workerPaymentService.findByFileId(fileId);
    }

    private List<com.example.paymentflow.worker.entity.WorkerUploadedData> parseFileToUploadedData(
            File file, String originalFilename, Long fileId, Long boardId, Long employerId, Long toliId)
            throws java.io.IOException {
        log.info("Parsing file {} to WorkerUploadedData format", originalFilename);

        String extension = getFileExtension(originalFilename);
        if ("csv".equalsIgnoreCase(extension)) {
            return parseCsvToUploadedData(file, fileId, boardId, employerId, toliId);
        }
        if ("xls".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension)) {
            return parseExcelToUploadedData(file, fileId, boardId, employerId, toliId);
        }

        throw new java.io.IOException("Unsupported file type: " + extension);
    }

    private List<com.example.paymentflow.worker.entity.WorkerUploadedData> parseCsvToUploadedData(
            File file, Long fileId, Long boardId, Long employerId, Long toliId) throws java.io.IOException {
        List<com.example.paymentflow.worker.entity.WorkerUploadedData> uploadedDataList = new ArrayList<>();

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String header = br.readLine();
            if (header == null) {
                throw new java.io.IOException("File is empty or invalid");
            }

            int rowNumber = 1;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    com.example.paymentflow.worker.entity.WorkerUploadedData uploadedData = parseCSVLineToUploadedData(
                            line, fileId, rowNumber++, boardId, employerId, toliId);
                    uploadedDataList.add(uploadedData);
                } catch (Exception e) {
                    log.error("Error parsing CSV line {}: {}", rowNumber, e.getMessage());
                }
            }
        }

        log.info("Parsed {} records from CSV file", uploadedDataList.size());
        return uploadedDataList;
    }

    private List<com.example.paymentflow.worker.entity.WorkerUploadedData> parseExcelToUploadedData(
            File file, Long fileId, Long boardId, Long employerId, Long toliId) throws java.io.IOException {
        List<com.example.paymentflow.worker.entity.WorkerUploadedData> uploadedDataList = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new java.io.IOException("No sheet found in uploaded workbook");
            }

            boolean isHeader = true;
            int rowNumber = 1;
            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] fields = extractExcelRow(row, formatter);
                if (isRowEmpty(fields)) {
                    continue;
                }

                try {
                    com.example.paymentflow.worker.entity.WorkerUploadedData uploadedData = populateUploadedDataFromFields(
                            fields, fileId, rowNumber++, boardId, employerId, toliId);
                    uploadedDataList.add(uploadedData);
                } catch (Exception e) {
                    log.error("Error parsing Excel row {}: {}", rowNumber, e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new java.io.IOException("Failed to read Excel file: " + e.getMessage(), e);
        }

        log.info("Parsed {} records from Excel file", uploadedDataList.size());
        return uploadedDataList;
    }

    private com.example.paymentflow.worker.entity.WorkerUploadedData parseCSVLineToUploadedData(
            String csvLine, Long fileId, int rowNumber, Long boardId, Long employerId, Long toliId) {
        String[] fields = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        return populateUploadedDataFromFields(fields, fileId, rowNumber, boardId, employerId, toliId);
    }

    private String cleanField(String field) {
        if (field == null)
            return null;
        // Remove quotes and trim whitespace
        field = field.trim();
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        return field.isEmpty() ? null : field;
    }

    // Helper methods for parsing fields safely
    private Long parseLongField(String value) {
        try {
            if (value == null || value.trim().isEmpty())
                return null;
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseIntField(String value) {
        try {
            if (value == null || value.trim().isEmpty())
                return null;
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private java.math.BigDecimal parseBigDecimalField(String value) {
        try {
            if (value == null || value.trim().isEmpty())
                return null;
            return new java.math.BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private com.example.paymentflow.worker.entity.WorkerUploadedData populateUploadedDataFromFields(
            String[] rawFields, Long fileId, int rowNumber, Long boardId, Long employerId, Long toliId) {
        com.example.paymentflow.worker.entity.WorkerUploadedData uploadedData = new com.example.paymentflow.worker.entity.WorkerUploadedData();
        uploadedData.setFileId(fileId);
        uploadedData.setBoardId(boardId);
        uploadedData.setEmployerId(employerId);
        uploadedData.setToliId(toliId);
        uploadedData.setStatusId(1);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        uploadedData.setCreatedAt(now);
        uploadedData.setUpdatedAt(now);

        // Defensive: check for null or insufficient fields
        if (rawFields == null || rawFields.length < 44) {
            log.error("Row {}: insufficient columns for new worker_uploaded_data schema (expected >=44, got {})",
                    rowNumber, (rawFields == null ? 0 : rawFields.length));
            return uploadedData;
        }

        try {
            // Map all columns by index (assuming strict order as per new schema)
            uploadedData.setWorkerId(parseLongField(rawFields[0]));
            uploadedData.setEmployeeName(cleanField(rawFields[1]));
            uploadedData.setEmployerRegNo((rawFields[2]));
            uploadedData.setToliRegNo((rawFields[3]));
            uploadedData.setMonth(cleanField(rawFields[4]));
            uploadedData.setDay1(parseIntField(rawFields[5]));
            uploadedData.setDay2(parseIntField(rawFields[6]));
            uploadedData.setDay3(parseIntField(rawFields[7]));
            uploadedData.setDay4(parseIntField(rawFields[8]));
            uploadedData.setDay5(parseIntField(rawFields[9]));
            uploadedData.setDay6(parseIntField(rawFields[10]));
            uploadedData.setDay7(parseIntField(rawFields[11]));
            uploadedData.setDay8(parseIntField(rawFields[12]));
            uploadedData.setDay9(parseIntField(rawFields[13]));
            uploadedData.setDay10(parseIntField(rawFields[14]));
            uploadedData.setDay11(parseIntField(rawFields[15]));
            uploadedData.setDay12(parseIntField(rawFields[16]));
            uploadedData.setDay13(parseIntField(rawFields[17]));
            uploadedData.setDay14(parseIntField(rawFields[18]));
            uploadedData.setDay15(parseIntField(rawFields[19]));
            uploadedData.setDay16(parseIntField(rawFields[20]));
            uploadedData.setDay17(parseIntField(rawFields[21]));
            uploadedData.setDay18(parseIntField(rawFields[22]));
            uploadedData.setDay19(parseIntField(rawFields[23]));
            uploadedData.setDay20(parseIntField(rawFields[24]));
            uploadedData.setDay21(parseIntField(rawFields[25]));
            uploadedData.setDay22(parseIntField(rawFields[26]));
            uploadedData.setDay23(parseIntField(rawFields[27]));
            uploadedData.setDay24(parseIntField(rawFields[28]));
            uploadedData.setDay25(parseIntField(rawFields[29]));
            uploadedData.setDay26(parseIntField(rawFields[30]));
            uploadedData.setDay27(parseIntField(rawFields[31]));
            uploadedData.setDay28(parseIntField(rawFields[32]));
            uploadedData.setDay29(parseIntField(rawFields[33]));
            uploadedData.setDay30(parseIntField(rawFields[34]));
            uploadedData.setDay31(parseIntField(rawFields[35]));
            uploadedData.setTotalDays(parseIntField(rawFields[36]));
            uploadedData.setAmount(parseBigDecimalField(rawFields[37]));
            uploadedData.setAdvance(parseBigDecimalField(rawFields[38]));
            uploadedData.setNetPayable(parseBigDecimalField(rawFields[39]));
            uploadedData.setPaymentType(cleanField(rawFields[40]));
            uploadedData.setTxnRef(cleanField(rawFields[41]));
            // Optionally: set any additional fields if schema grows
        } catch (Exception e) {
            log.error("Error mapping uploaded data fields for row {}: {}", rowNumber, e.getMessage());
            throw new RuntimeException("Failed to map row fields: " + e.getMessage(), e);
        }

        return uploadedData;
    }

    // Helper methods for parsing fields safely

    private String[] extractExcelRow(Row row, DataFormatter formatter) {
        // Support both old format (13 fields) and new format (15 fields)
        int maxColumns = Math.max(15, row.getLastCellNum());
        String[] fields = new String[maxColumns];

        for (int i = 0; i < maxColumns; i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                fields[i] = null;
                continue;
            }

            // Handle date formatting for work_date column
            // In new format: work_date is at index 7
            // In old format: work_date is at index 5
            if ((i == 7 || i == 5) && cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                java.time.LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                fields[i] = date.toString();
            } else {
                fields[i] = formatter.formatCellValue(cell);
            }
        }
        return fields;
    }

    private boolean isRowEmpty(String[] fields) {
        if (fields == null) {
            return true;
        }
        for (String field : fields) {
            if (field != null && !field.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot >= 0 ? filename.substring(lastDot + 1) : "";
    }

}
