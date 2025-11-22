// (Removed all misplaced method definitions at the top of the file. Only package/imports and the class remain.)
package com.example.paymentflow.worker.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.paymentflow.utilities.file.UploadedFile;
import com.example.paymentflow.utilities.file.UploadedFileRepository;
import com.example.paymentflow.worker.entity.WorkerPayment;
import com.example.paymentflow.worker.entity.WorkerPaymentReceipt;
import com.example.paymentflow.worker.entity.WorkerUploadedData;
import com.example.paymentflow.worker.repository.WorkerUploadedDataRepository;
import com.shared.utilities.logger.LoggerFactoryProvider;

@Service
@Transactional
public class WorkerUploadedDataService {

    private static final Logger log = LoggerFactoryProvider.getLogger(WorkerUploadedDataService.class);

    private final WorkerUploadedDataRepository repository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @Autowired
    private WorkerPaymentService workerPaymentService;

    @Autowired
    private WorkerPaymentReceiptService workerPaymentReceiptService;

    public WorkerUploadedDataService(WorkerUploadedDataRepository repository) {
        this.repository = repository;
    }

    private Long parseFileId(String fileId) {
        try {
            return Long.valueOf(fileId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid fileId, expected numeric value", e);
        }
    }

    public WorkerUploadedData save(WorkerUploadedData uploadedData) {
        log.debug("Saving worker uploaded data for workerId: {}",
                uploadedData.getWorkerId());
        return repository.save(uploadedData);
    }

    public List<WorkerUploadedData> saveAll(List<WorkerUploadedData> uploadedDataList) {
        log.info("Saving {} worker uploaded data records", uploadedDataList.size());
        return repository.saveAll(uploadedDataList);
    }

    public List<WorkerUploadedData> findByFileId(String fileId) {
        log.info("Finding worker uploaded data for fileId: {}", fileId);
        return repository.findByFileId(parseFileId(fileId));
    }

    public List<WorkerUploadedData> findByFileIdAndStatus(String fileId, Integer statusId) {
        log.info("Finding worker uploaded data for fileId: {} with statusId: {}", fileId, statusId);
        if (statusId == null) {
            return repository.findByFileId(parseFileId(fileId));
        }
        List<WorkerUploadedData> filtered = repository.findByFileIdAndStatusId(parseFileId(fileId), statusId);
        // If status is not yet populated in DB, fallback to all records to avoid empty results
        return filtered.isEmpty() ? repository.findByFileId(parseFileId(fileId)) : filtered;
    }

    public Page<WorkerUploadedData> findByFileIdPaginated(String fileId, Pageable pageable) {
        log.info("Finding worker uploaded data for fileId: {} (paginated)", fileId);
        return repository.findByFileId(parseFileId(fileId), pageable);
    }

    public Map<String, Integer> getFileStatusSummary(String fileId) {
        log.info("Getting status summary for fileId: {}", fileId);

        List<Object[]> statusCounts = repository.getStatusCountsByFileId(parseFileId(fileId));
        Map<String, Integer> summary = new HashMap<>();

        for (Object[] result : statusCounts) {
            Integer statusId = result[0] != null ? (Integer) result[0] : null;
            Long count = (Long) result[1];
            summary.put(statusId != null ? String.valueOf(statusId) : "UNKNOWN", count.intValue());
        }

        return summary;
    }

    private String determineOverallFileStatus(Map<String, Integer> statusSummary) {
        int totalRecords = statusSummary.values().stream().mapToInt(Integer::intValue).sum();
        if (totalRecords == 0) {
            return "EMPTY";
        }
        // Without a mapped status column, return a generic marker
        return "UNKNOWN";
    }

    public Map<String, Object> getComprehensiveFileSummary(String fileId) {
        log.info("Getting comprehensive summary for fileId: {}", fileId);

        try {
            // Get file metadata
            Long uploadedFileId = Long.parseLong(fileId);
            Optional<UploadedFile> uploadedFileOpt = uploadedFileRepository.findById(uploadedFileId);

            if (uploadedFileOpt.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "File not found");
                return error;
            }

            UploadedFile uploadedFile = uploadedFileOpt.get();

            // Get status summary
            Map<String, Integer> statusSummary = getFileStatusSummary(fileId);

            // Calculate validated count and total amount for validated records
            List<WorkerUploadedData> validatedRecords = findByFileIdAndStatus(fileId, 1);
            int validatedCount = validatedRecords.size();

            BigDecimal totalValidatedAmount = validatedRecords.stream()
                    .map(record -> record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Build comprehensive response
            Map<String, Object> summary = new HashMap<>();
            summary.put("fileId", fileId);
            summary.put("fileName", uploadedFile.getFilename());
            summary.put("uploadDate", uploadedFile.getUploadDate());
            summary.put("totalRecords", uploadedFile.getTotalRecords());
            summary.put("validatedCount", validatedCount);
            summary.put("totalValidatedAmount", totalValidatedAmount);
            summary.put("statusSummary", statusSummary);
            summary.put("fileStatus", uploadedFile.getStatus());

            // Add ready for payment flag
            boolean readyForPayment = validatedCount > 0 &&
                    (statusSummary.getOrDefault("REJECTED", 0) == 0 ||
                            statusSummary.getOrDefault("VALIDATED", 0) > 0);
            summary.put("readyForPayment", readyForPayment);

            log.info("Comprehensive summary generated for fileId: {} - {} validated records, total amount: {}",
                    fileId, validatedCount, totalValidatedAmount);

            return summary;

        } catch (NumberFormatException e) {
            log.error("Invalid fileId format: {}", fileId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Invalid file ID format");
            return error;
        } catch (Exception e) {
            log.error("Error getting comprehensive summary for fileId: {}", fileId, e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get file summary: " + e.getMessage());
            return error;
        }
    }

    public Map<String, Object> getPaginatedFileSummaries(
            int page, int size, String fileId, String status,
            String startDate, String endDate, String sortBy, String sortDir) {

        log.info("Getting aggregated file summaries - page: {}, size: {}, fileId: {}, status: {}",
                page, size, fileId, status);

        try {
            // Get distinct file IDs from worker_uploaded_data table with filters
            List<Long> distinctFileIds = repository.findDistinctFileIds();

            // Apply fileId filter if provided
            if (fileId != null && !fileId.trim().isEmpty()) {
                Long filterId = parseFileId(fileId.trim());
                distinctFileIds = distinctFileIds.stream()
                        .filter(id -> id.equals(filterId))
                        .collect(java.util.stream.Collectors.toList());
            }

            // Build summary for each file and apply filters
            java.util.List<Map<String, Object>> allFileSummaries = new java.util.ArrayList<>();

            for (Long currentFileId : distinctFileIds) {
                try {
                    // Get file metadata from uploaded_files table
                    Optional<UploadedFile> uploadedFileOpt = uploadedFileRepository.findById(currentFileId);

                    if (uploadedFileOpt.isEmpty()) {
                        log.warn("File metadata not found for fileId: {}", currentFileId);
                        continue;
                    }

                    UploadedFile file = uploadedFileOpt.get();

                    // Apply date range filter
                    if (startDate != null && endDate != null) {
                        java.time.LocalDateTime startDateTime = java.time.LocalDate.parse(startDate).atStartOfDay();
                        java.time.LocalDateTime endDateTime = java.time.LocalDate.parse(endDate).atTime(23, 59, 59);

                        if (file.getUploadDate().isBefore(startDateTime) || file.getUploadDate().isAfter(endDateTime)) {
                            continue;
                        }
                    }

                    // Get aggregated data from worker_uploaded_data
                    Map<String, Integer> statusSummary = getFileStatusSummary(currentFileId.toString());
                    int totalRecords = statusSummary.values().stream().mapToInt(Integer::intValue).sum();

                    // Skip if no records found
                    if (totalRecords == 0) {
                        continue;
                    }

                    // Apply status filter based on majority status or specific criteria
                    if (status != null && !status.trim().isEmpty()) {
                        String filterStatus = status.trim().toUpperCase();
                        // Only include if the file has records with the requested status
                        if (!statusSummary.containsKey(filterStatus) || statusSummary.get(filterStatus) == 0) {
                            continue;
                        }
                    }

                    // Calculate validated amount
                    List<WorkerUploadedData> validatedRecords = findByFileIdAndStatus(currentFileId.toString(), 1);
                    int validatedCount = validatedRecords.size();

                    BigDecimal totalValidatedAmount = validatedRecords.stream()
                            .map(record -> record.getAmount() != null ? record.getAmount() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Determine overall file status based on aggregated data
                    String overallStatus = determineOverallFileStatus(statusSummary);

                    // Build file summary
                    Map<String, Object> fileSummary = new HashMap<>();
                    fileSummary.put("fileId", currentFileId.toString());
                    fileSummary.put("fileName", file.getFilename());
                    fileSummary.put("uploadDate", file.getUploadDate());
                    fileSummary.put("totalRecords", totalRecords);
                    fileSummary.put("validatedCount", validatedCount);
                    fileSummary.put("totalValidatedAmount", totalValidatedAmount);
                    fileSummary.put("statusSummary", statusSummary);
                    fileSummary.put("overallStatus", overallStatus);

                    // Add ready for payment flag
                    boolean readyForPayment = validatedCount > 0;
                    fileSummary.put("readyForPayment", readyForPayment);

                    // Add upload timestamp for sorting
                    fileSummary.put("uploadTimestamp", file.getUploadDate());

                    allFileSummaries.add(fileSummary);

                } catch (NumberFormatException e) {
                    log.warn("Invalid fileId format: {}", currentFileId);
                } catch (Exception e) {
                    log.error("Error processing fileId: {}", currentFileId, e);
                }
            }

            // Sort the results
            if ("uploadDate".equals(sortBy)) {
                allFileSummaries.sort((a, b) -> {
                    java.time.LocalDateTime dateA = (java.time.LocalDateTime) a.get("uploadTimestamp");
                    java.time.LocalDateTime dateB = (java.time.LocalDateTime) b.get("uploadTimestamp");
                    return "desc".equalsIgnoreCase(sortDir) ? dateB.compareTo(dateA) : dateA.compareTo(dateB);
                });
            } else if ("totalRecords".equals(sortBy)) {
                allFileSummaries.sort((a, b) -> {
                    Integer countA = (Integer) a.get("totalRecords");
                    Integer countB = (Integer) b.get("totalRecords");
                    return "desc".equalsIgnoreCase(sortDir) ? countB.compareTo(countA) : countA.compareTo(countB);
                });
            }

            // Apply pagination manually
            int totalElements = allFileSummaries.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);

            List<Map<String, Object>> paginatedSummaries = start < totalElements ? allFileSummaries.subList(start, end)
                    : new ArrayList<>();

            // Build paginated response
            Map<String, Object> response = new HashMap<>();
            response.put("data", paginatedSummaries);
            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", page < totalPages - 1);
            response.put("hasPrevious", page > 0);

            log.info("Retrieved {} file summaries (page {} of {}) from {} total files",
                    paginatedSummaries.size(), page + 1, totalPages, totalElements);

            return response;

        } catch (Exception e) {
            log.error("Error getting paginated file summaries", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get file summaries: " + e.getMessage());
            return error;
        }
    }

    @Transactional
    public void validateUploadedData(String fileId) {
        log.info("Starting validation for fileId: {}", fileId);

        List<WorkerUploadedData> uploadedRecords = findByFileIdAndStatus(fileId, 1);
        log.info("Found {} uploaded records to validate", uploadedRecords.size());

        for (WorkerUploadedData record : uploadedRecords) {
            try {
                validateRecord(record);
                // No validatedAt field in entity, skip setting it
            } catch (Exception e) {
                log.error("Error validating record for workerId {} in fileId: {}", record.getWorkerId(), fileId, e);
                record.setRejectionReason("Validation error: " + e.getMessage());
            }
        }

        repository.saveAll(uploadedRecords);
        log.info("Validation completed for fileId: {}", fileId);
    }

    private void validateRecord(WorkerUploadedData record) {
        StringBuilder errors = new StringBuilder();

        // Required field validations
        if (record.getWorkerId() == null) {
            errors.append("Worker ID is required. ");
        }
        if (record.getEmployeeName() == null || record.getEmployeeName().trim().isEmpty()) {
            errors.append("Employee name is required. ");
        }
        if (record.getAmount() == null || record.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.append("Valid amount greater than 0 is required. ");
        }
        // No bank account or work date fields in entity, skip those checks

        // Field length validations
        if (record.getEmployeeName() != null && record.getEmployeeName().length() > 255) {
            errors.append("Employee name must not exceed 255 characters. ");
        }
        // No companyName, department, position, email, phone fields in entity, skip
        // those checks

        // Amount validation
        if (record.getAmount() != null && record.getAmount().compareTo(new java.math.BigDecimal("1000000")) > 0) {
            errors.append("Amount seems unreasonably high (max 1,000,000). ");
        }

        if (errors.length() > 0) {
            record.setRejectionReason(errors.toString().trim());
        } else {
        }
    }

    @Transactional
    public int generateRequestForValidatedData(String fileId, String uploadedFileRef) {
        log.info("Generating request for validated data in fileId: {}", fileId);

        List<WorkerUploadedData> validatedRecords = findByFileIdAndStatus(fileId, 1);
        log.info("Found {} validated records to process", validatedRecords.size());

        if (validatedRecords.isEmpty()) {
            return 0;
        }

        try {
            // Step 1: Convert WorkerUploadedData to WorkerPayment objects
            List<WorkerPayment> workerPayments = new ArrayList<>();
            for (WorkerUploadedData uploadedData : validatedRecords) {
                WorkerPayment payment = convertUploadedDataToPayment(uploadedData);
                WorkerPayment savedPayment = workerPaymentService.save(payment);
                workerPayments.add(savedPayment);
                log.debug("Created WorkerPayment record for worker: {}", uploadedData.getWorkerId());
            }

            // Step 2: Create WorkerPaymentReceipt using the receipt service
            WorkerPaymentReceipt receipt = workerPaymentReceiptService.createReceipt(workerPayments);
            log.info("Created WorkerPaymentReceipt with number: {}", receipt.getReceiptNumber());

            // Step 3: Update WorkerPayment records with receipt number (IMPORTANT LINK!)
            for (WorkerPayment payment : workerPayments) {
                try {
                    payment.setReceiptNumber(receipt.getReceiptNumber());
                    workerPaymentService.save(payment);
                    log.debug("Updated WorkerPayment {} with receipt number: {}", payment.getId(),
                            receipt.getReceiptNumber());
                } catch (Exception e) {
                    log.error("Error updating WorkerPayment {} with receipt number", payment.getId(), e);
                }
            }

            // Step 4: Update uploaded data records with receipt info and status
            int processedCount = 0;
            for (WorkerUploadedData validatedData : validatedRecords) {
                try {
                    // No setReceiptNumber or setProcessedAt in entity, skip these
                    repository.save(validatedData);
                    processedCount++;
                } catch (Exception e) {
                    log.error("Error updating uploaded data record {} after payment creation", validatedData.getId(),
                            e);
                }
            }

            log.info("Successfully generated request for {} records with receipt: {}", processedCount,
                    receipt.getReceiptNumber());
            return processedCount;

        } catch (Exception e) {
            log.error("Error generating payment request for fileId: {}", fileId, e);
            throw new RuntimeException("Failed to generate payment request: " + e.getMessage(), e);
        }
    }

    private WorkerPayment convertUploadedDataToPayment(WorkerUploadedData uploadedData) {
        WorkerPayment payment = new WorkerPayment();

        // Map fields from WorkerUploadedData to WorkerPayment based on available fields
        payment.setWorkerId(uploadedData.getWorkerId());
        payment.setEmployerId(uploadedData.getEmployerId());
        payment.setToliId(uploadedData.getToliId());
        payment.setBoardId(uploadedData.getBoardId());
        payment.setMonth(uploadedData.getMonth());
        payment.setTotalDays(uploadedData.getTotalDays());
        payment.setBasicWages(uploadedData.getAmount());
        payment.setAdvance(uploadedData.getAdvance());
        payment.setGrossWages(uploadedData.getNetPayable());
        payment.setLevy(BigDecimal.ZERO);
        payment.setNetWagesPayable(uploadedData.getNetPayable());
        payment.setPaymentType(uploadedData.getPaymentType());
        payment.setTxnRef(uploadedData.getTxnRef());
        return payment;
    }

    public void deleteByFileId(String fileId) {
        log.info("Deleting all uploaded data for fileId: {}", fileId);
        repository.deleteByFileId(parseFileId(fileId));
    }

    public List<WorkerUploadedData> findRejectedRecords(String fileId) {
        log.info("Finding rejected records for fileId: {}", fileId);
        return findByFileIdAndStatus(fileId, 3);
    }

    public List<WorkerUploadedData> findRequestGeneratedRecords(String fileId) {
        log.info("Finding request generated records for fileId: {}", fileId);
        return findByFileIdAndStatus(fileId, 2);
    }

    public Page<WorkerUploadedData> findByFileIdAndDateRangePaginated(String fileId,
            java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, Pageable pageable) {
        log.info("Finding records by fileId: {} and date range: {} to {} (paginated)", fileId, startDate, endDate);
        Long parsedFileId = parseFileId(fileId);
        return repository.findByFileIdAndCreatedAtBetween(parsedFileId, startDate, endDate, pageable);
    }

    // Find all by createdAt between (paginated)
    public Page<WorkerUploadedData> findByDateRangePaginated(java.time.LocalDateTime startDate,
            java.time.LocalDateTime endDate, Pageable pageable) {
        log.info("Finding all records by date range: {} to {} (paginated)", startDate, endDate);
        return repository.findByCreatedAtBetween(startDate, endDate, pageable);
    }

}
