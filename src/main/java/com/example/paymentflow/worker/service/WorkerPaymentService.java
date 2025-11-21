package com.example.paymentflow.worker.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.paymentflow.worker.dao.WorkerPaymentQueryDao;
import com.example.paymentflow.worker.entity.WorkerPayment;
import com.example.paymentflow.worker.repository.WorkerPaymentRepository;
import com.shared.common.annotation.Auditable;
import com.shared.common.dao.BaseQueryDao.PageResult; // Uncomment if exists
import com.shared.exception.ResourceNotFoundException;
import com.shared.utilities.logger.LoggerFactoryProvider; // Uncomment if exists

@Service
@Transactional
public class WorkerPaymentService {

    private static final Logger log = LoggerFactoryProvider.getLogger(WorkerPaymentService.class);

    private final WorkerPaymentRepository repository;
    private final WorkerPaymentQueryDao workerPaymentQueryDao;

    private static final int FETCH_BATCH_SIZE = 500;

    public WorkerPaymentService(WorkerPaymentRepository repository, WorkerPaymentQueryDao workerPaymentQueryDao) {
        this.repository = repository;
        this.workerPaymentQueryDao = workerPaymentQueryDao;
    }

    @Auditable(action = "CREATE_WORKER_PAYMENT", resourceType = "WORKER_PAYMENT", resourceId = "#result.id")
    public WorkerPayment create(WorkerPayment workerPayment) {
        log.info("Persisting worker payment for workerRef={}", workerPayment.getWorkerRef());
        WorkerPayment saved = repository.save(workerPayment);
        log.info("Persisted worker payment id={}", saved.getId());
        return saved;
    }

    private <T> List<T> collectAll(BiFunction<Integer, Integer, PageResult<T>> pageSupplier) {
        return collectAll(pageSupplier, null);
    }

    private <T> List<T> collectAll(BiFunction<Integer, Integer, PageResult<T>> pageSupplier,
            Predicate<T> filter) {
        List<T> results = new ArrayList<>();
        int page = 0;
        while (true) {
            PageResult<T> pageResult = pageSupplier.apply(page, FETCH_BATCH_SIZE);
            List<T> content = pageResult.getContent();
            if (filter != null) {
                content = content.stream().filter(filter).toList();
            }
            results.addAll(content);
            if (!pageResult.hasNext()) {
                break;
            }
            page++;
        }
        return results;
    }

    public List<WorkerPayment> createBulk(List<WorkerPayment> workerPayments) {
        log.info("Bulk persisting {} worker payments", workerPayments.size());
        List<WorkerPayment> saved = repository.saveAll(workerPayments);
        log.info("Bulk persisted {} worker payments", saved.size());
        return saved;
    }

    public List<WorkerPayment> updateBulk(List<WorkerPayment> workerPayments) {
        log.info("Bulk updating {} worker payments", workerPayments.size());
        List<WorkerPayment> saved = repository.saveAll(workerPayments);
        log.info("Bulk updated {} worker payments", saved.size());
        return saved;
    }

    // READ OPERATIONS - Using Query DAO
    @Transactional(readOnly = true)
    public List<WorkerPayment> findByRequestReferencePrefix(String prefix) {
        log.info("Finding worker payments with request reference starting with: {} using query DAO", prefix);
        return workerPaymentQueryDao.findByRequestReferenceNumberStartingWith(prefix);
    }

    @Transactional(readOnly = true)
    public List<WorkerPayment> findByStatus(String status) {
        log.info("Finding worker payments with status: {} using query DAO", status);
        return collectAll((page, size) -> workerPaymentQueryDao.findByStatus(status, page, size));
    }

    @Transactional(readOnly = true)
    public List<WorkerPayment> findByReferencePrefixAndStatus(String prefix, String status) {
        log.info("Finding worker payments with reference prefix: {} and status: {} using query DAO", prefix, status);
        if (prefix == null || prefix.isEmpty()) {
            return collectAll(
                    (page, size) -> workerPaymentQueryDao.findWithFilters(status, null, null, null, null, page, size));
        }
        Predicate<WorkerPayment> filter = wp -> wp.getRequestReferenceNumber() != null
                && wp.getRequestReferenceNumber().startsWith(prefix);
        return collectAll(
                (page, size) -> workerPaymentQueryDao.findWithFilters(status, null, null, null, null, page, size),
                filter);
    }

    @Transactional(readOnly = true)
    public List<WorkerPayment> findByFileId(Long fileId) {
        log.info("Finding worker payments for fileId: {} using query DAO", fileId);
        return collectAll((page, size) -> workerPaymentQueryDao.findByFileId(fileId, page, size));
    }

    @Transactional(readOnly = true)
    public List<WorkerPayment> findByFileIdAndStatus(String fileId, String status) {
        log.info("Finding worker payments for fileId: {} with status: {} using query DAO", fileId, status);
        return collectAll(
                (page, size) -> workerPaymentQueryDao.findWithFilters(status, null, fileId, null, null, page, size));
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByFileIdPaginated(Long fileId, Pageable pageable) {
        log.info("Finding worker payments with fileId: {} (paginated) using query DAO", fileId);
        var result = workerPaymentQueryDao.findByFileId(fileId, pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByFileIdAndStatusPaginated(String fileId, String status, Pageable pageable) {
        log.info("Finding worker payments with fileId: {} and status: {} (paginated) using query DAO", fileId, status);
        var result = workerPaymentQueryDao.findWithFilters(status, null, fileId, null, null,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public List<WorkerPayment> findAll() {
        log.info("Retrieving all worker payments using query DAO");
        return collectAll(
                (page, size) -> workerPaymentQueryDao.findWithFilters(null, null, null, null, null, page, size));
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findAllPaginated(Pageable pageable) {
        log.info("Retrieving all worker payments (paginated) using query DAO");
        var result = workerPaymentQueryDao.findWithFilters(null, null, null, null, null,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByStatusPaginated(String status, Pageable pageable) {
        log.info("Finding worker payments with status: {} (paginated) using query DAO", status);
        var result = workerPaymentQueryDao.findByStatus(status, pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByReceiptNumber(String receiptNumber, Pageable pageable) {
        log.info("Finding worker payments with receipt number: {} (paginated) using query DAO", receiptNumber);
        var result = workerPaymentQueryDao.findWithFilters(null, receiptNumber, null, null, null,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByStatusAndReceiptNumber(
            String status,
            String receiptNumber,
            Pageable pageable) {
        log.info("Finding worker payments with status: {}, receipt number: {} (paginated) using query DAO",
                status, receiptNumber);
        var result = workerPaymentQueryDao.findWithFilters(status, receiptNumber, null, null, null,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public WorkerPayment findById(Long id) {
        log.info("Retrieving worker payment id={}", id);
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Worker payment id={} not found", id);
                    return new ResourceNotFoundException("Worker payment not found for id=" + id);
                });
    }

    public WorkerPayment update(Long id, WorkerPayment updated) {
        log.info("Updating worker payment id={}", id);
        WorkerPayment existing = findById(id);
        existing.setWorkerRef(updated.getWorkerRef());
        existing.setRegId(updated.getRegId());
        existing.setName(updated.getName());
        existing.setToli(updated.getToli());
        existing.setAadhar(updated.getAadhar());
        existing.setPan(updated.getPan());
        existing.setBankAccount(updated.getBankAccount());
        existing.setPaymentAmount(updated.getPaymentAmount());
        if (updated.getRequestReferenceNumber() != null && !updated.getRequestReferenceNumber().isBlank()) {
            existing.setRequestReferenceNumber(updated.getRequestReferenceNumber());
        }
        WorkerPayment saved = repository.save(existing);
        log.info("Updated worker payment id={}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        log.info("Deleting worker payment id={}", id);
        if (!repository.existsById(id)) {
            log.warn("Cannot delete worker payment id={} because it does not exist", id);
            throw new ResourceNotFoundException("Worker payment not found for id=" + id);
        }
        repository.deleteById(id);
        log.info("Deleted worker payment id={}", id);
    }

    public WorkerPayment save(WorkerPayment workerPayment) {
        log.info("Saving worker payment with id={}", workerPayment.getId());
        WorkerPayment saved = repository.save(workerPayment);
        log.info("Saved worker payment with id={}", saved.getId());
        return saved;
    }

    // All read methods now use WorkerPaymentQueryDao
    // These methods are removed as they used the old repository approach

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByStatusAndReceiptNumberAndDateRange(
            String status,
            String receiptNumber,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        log.info(
                "Finding worker payments with status: {}, receipt number: {}, date range: {} to {} (paginated) using query DAO",
                status, receiptNumber, startDate, endDate);
        var result = workerPaymentQueryDao.findWithFilters(status, receiptNumber, null, startDate, endDate,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    @Transactional(readOnly = true)
    public Page<WorkerPayment> findByUploadedFileRefPaginated(String uploadedFileRef, Pageable pageable) {
        log.info("Finding worker payments by uploaded file ref: {} (paginated) using query DAO", uploadedFileRef);
        var result = workerPaymentQueryDao.findWithFilters(null, null, uploadedFileRef, null, null,
                pageable.getPageNumber(), pageable.getPageSize());
        return createPageFromPageResult(result, pageable);
    }

    // Utility method to convert PageResult to Spring Page
    private Page<WorkerPayment> createPageFromPageResult(PageResult<WorkerPayment> pageResult, Pageable pageable) {
        return new PageImpl<>(pageResult.getContent(), pageable, pageResult.getTotalElements());
    }
}
