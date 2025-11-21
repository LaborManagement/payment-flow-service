package com.example.paymentflow.worker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.paymentflow.worker.dao.WorkerPaymentQueryDao;
import com.example.paymentflow.worker.entity.WorkerPayment;
import com.example.paymentflow.worker.repository.WorkerPaymentRepository;
import com.shared.common.dao.BaseQueryDao.PageResult;
import com.shared.exception.ResourceNotFoundException; // Uncomment if exists

/**
 * Enhanced Worker Payment Service using query-based approach for reads and JPA
 * for writes.
 * This provides better control over read queries and easier debugging while
 * maintaining
 * JPA benefits for write operations and data consistency.
 */
@Service
@Transactional
public class EnhancedWorkerPaymentService {

    private static final Logger log = LoggerFactory.getLogger(EnhancedWorkerPaymentService.class);

    // JPA Repository for write operations (Create, Update, Delete)
    private final WorkerPaymentRepository writeRepository;

    // Query DAO for all read operations
    private final WorkerPaymentQueryDao readDao;

    public EnhancedWorkerPaymentService(WorkerPaymentRepository writeRepository,
            WorkerPaymentQueryDao readDao) {
        this.writeRepository = writeRepository;
        this.readDao = readDao;
    }

    // ===========================================
    // WRITE OPERATIONS - Using JPA Repository
    // ===========================================

    /**
     * Create a new worker payment using JPA for transaction management
     */
    public WorkerPayment create(WorkerPayment workerPayment) {
        log.info("Creating worker payment for workerRef={}", workerPayment.getWorkerRef());
        WorkerPayment saved = writeRepository.save(workerPayment);
        log.info("Created worker payment with id={}", saved.getId());
        return saved;
    }

    /**
     * Bulk create worker payments using JPA for transaction consistency
     */
    public List<WorkerPayment> createBulk(List<WorkerPayment> workerPayments) {
        log.info("Bulk creating {} worker payments", workerPayments.size());
        List<WorkerPayment> saved = writeRepository.saveAll(workerPayments);
        log.info("Bulk created {} worker payments", saved.size());
        return saved;
    }

    /**
     * Update worker payment using JPA for transaction management
     */
    public WorkerPayment update(Long id, WorkerPayment updated) {
        log.info("Updating worker payment with id={}", id);

        // First check if exists using read DAO
        Optional<WorkerPayment> existing = readDao.findById(id);
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Worker payment not found with id: " + id);
        }

        // Update using JPA
        updated.setCreatedAt(existing.get().getCreatedAt()); // Preserve creation time
        WorkerPayment saved = writeRepository.save(updated);
        log.info("Updated worker payment with id={}", saved.getId());
        return saved;
    }

    /**
     * Bulk update worker payments using JPA for transaction consistency
     */
    public List<WorkerPayment> updateBulk(List<WorkerPayment> workerPayments) {
        log.info("Bulk updating {} worker payments", workerPayments.size());
        List<WorkerPayment> saved = writeRepository.saveAll(workerPayments);
        log.info("Bulk updated {} worker payments", saved.size());
        return saved;
    }

    /**
     * Update status of worker payment
     */
    public WorkerPayment updateStatus(Long id, String newStatus) {
        log.info("Updating status of worker payment id={} to status={}", id, newStatus);

        Optional<WorkerPayment> existing = readDao.findById(id);
        if (existing.isEmpty()) {
            throw new ResourceNotFoundException("Worker payment not found with id: " + id);
        }

        WorkerPayment payment = existing.get();
        payment.setStatus(newStatus);
        return writeRepository.save(payment);
    }

    /**
     * Delete worker payment using JPA
     */
    public void delete(Long id) {
        log.info("Deleting worker payment with id={}", id);

        if (readDao.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Worker payment not found with id: " + id);
        }

        writeRepository.deleteById(id);
        log.info("Deleted worker payment with id={}", id);
    }

    // ===========================================
    // READ OPERATIONS - Using Custom Query DAO
    // ===========================================

    /**
     * Find worker payment by ID using custom query for better control
     */
    @Transactional(readOnly = true)
    public Optional<WorkerPayment> findById(Long id) {
        log.debug("Finding worker payment by id={}", id);
        return readDao.findById(id);
    }

    /**
     * Get worker payment by ID with exception if not found
     */
    @Transactional(readOnly = true)
    public WorkerPayment getById(Long id) {
        return findById(id).orElseThrow(() -> new ResourceNotFoundException("Worker payment not found with id: " + id));
    }

    /**
     * Find worker payments with complex filtering and pagination
     * All filtering logic is contained in the service layer for easier debugging
     */
    @Transactional(readOnly = true)
    public PageResult<WorkerPayment> findWithFilters(String status, String receiptNumber,
            String fileId, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) {
        log.info("Finding worker payments with filters - status={}, receiptNumber={}, fileId={}, " +
                "startDate={}, endDate={}, page={}, size={}",
                status, receiptNumber, fileId, startDate, endDate, page, size);

        return readDao.findWithFilters(status, receiptNumber, fileId, startDate, endDate, page, size);
    }

    /**
     * Find by status with pagination using custom query
     */
    @Transactional(readOnly = true)
    public PageResult<WorkerPayment> findByStatus(String status, int page, int size) {
        log.debug("Finding worker payments by status={}, page={}, size={}", status, page, size);
        return readDao.findByStatus(status, page, size);
    }

    /**
     * Find by receipt number using custom query
     */
    @Transactional(readOnly = true)
    public List<WorkerPayment> findByReceiptNumber(String receiptNumber) {
        log.debug("Finding worker payments by receiptNumber={}", receiptNumber);
        return readDao.findByReceiptNumber(receiptNumber);
    }

    /**
     * Find by file ID with pagination using custom query
     */
    @Transactional(readOnly = true)
    public PageResult<WorkerPayment> findByFileId(Long fileId, int page, int size) {
        log.debug("Finding worker payments by fileId={}, page={}, size={}", fileId, page, size);
        return readDao.findByFileId(fileId, page, size);
    }

    /**
     * Find by request reference number prefix using custom query
     */
    @Transactional(readOnly = true)
    public List<WorkerPayment> findByRequestReferencePrefix(String prefix) {
        log.debug("Finding worker payments by request reference prefix={}", prefix);
        return readDao.findByRequestReferenceNumberStartingWith(prefix);
    }

    /**
     * Find by date range with pagination using custom query
     */
    @Transactional(readOnly = true)
    public PageResult<WorkerPayment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        log.debug("Finding worker payments by date range from {} to {}, page={}, size={}",
                startDate, endDate, page, size);
        return readDao.findByDateRange(startDate, endDate, page, size);
    }

    /**
     * Get status counts for a file - useful for dashboard statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getStatusCountsByFileId(String fileId) {
        log.debug("Getting status counts for fileId={}", fileId);
        return readDao.getStatusCountsByFileId(fileId);
    }

    /**
     * Get payment summary statistics for a file
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentSummary(String fileId) {
        log.debug("Getting payment summary for fileId={}", fileId);
        return readDao.getPaymentSummary(fileId);
    }

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    /**
     * Check if worker payment exists by ID
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return readDao.findById(id).isPresent();
    }

    /**
     * Get total count with filters - useful for pagination
     */
    @Transactional(readOnly = true)
    public long countWithFilters(String status, String receiptNumber, String fileId,
            LocalDateTime startDate, LocalDateTime endDate) {
        // This would call a count query in the DAO
        PageResult<WorkerPayment> result = readDao.findWithFilters(status, receiptNumber, fileId,
                startDate, endDate, 0, 1);
        return result.getTotalElements();
    }
}
