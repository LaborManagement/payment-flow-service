package com.example.paymentflow.worker.service;

import com.example.paymentflow.worker.entity.WorkerPayment;
import com.example.paymentflow.worker.entity.WorkerPaymentReceipt;
import com.example.paymentflow.worker.repository.WorkerPaymentReceiptRepository;
import com.example.paymentflow.worker.dao.WorkerPaymentReceiptQueryDao;
import com.shared.common.dao.BaseQueryDao.PageResult;
import org.slf4j.Logger;
import com.shared.utilities.logger.LoggerFactoryProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Service
@Transactional
public class WorkerPaymentReceiptService {
    
    private static final Logger log = LoggerFactoryProvider.getLogger(WorkerPaymentReceiptService.class);
    
    private final WorkerPaymentReceiptRepository repository;
    private final WorkerPaymentReceiptQueryDao queryDao;

    public WorkerPaymentReceiptService(WorkerPaymentReceiptRepository repository, WorkerPaymentReceiptQueryDao queryDao) {
        this.repository = repository;
        this.queryDao = queryDao;
    }

    private static final int FETCH_BATCH_SIZE = 500;

    private <T> List<T> collectAll(BiFunction<Integer, Integer, PageResult<T>> pageSupplier) {
        return collectAll(pageSupplier, null);
    }

    private <T> List<T> collectAll(BiFunction<Integer, Integer, PageResult<T>> pageSupplier,
                                   Predicate<T> filter) {
        List<T> results = new ArrayList<>();
        int page = 0;
        while (true) {
            var pageResult = pageSupplier.apply(page, FETCH_BATCH_SIZE);
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

    public WorkerPaymentReceipt createReceipt(List<WorkerPayment> processedPayments) {
        log.info("Creating receipt for {} payments", processedPayments.size());
        
        if (processedPayments.isEmpty()) {
            throw new IllegalArgumentException("Cannot create receipt for empty payment list");
        }
        
        // Calculate total amount using new wage fields
        BigDecimal totalAmount = processedPayments.stream()
                .map(wp -> wp.getNetWagesPayable() != null ? wp.getNetWagesPayable()
                        : (wp.getGrossWages() != null ? wp.getGrossWages() : wp.getBasicWages()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Generate receipt number
        String receiptNumber = generateReceiptNumber();
        
        // Get employer_id and toli_id from the first payment (all payments in a batch should have the same employer/toli)
        WorkerPayment firstPayment = processedPayments.get(0);
        String employerId = firstPayment.getEmployerId() != null ? firstPayment.getEmployerId().toString() : null;
        String toliId = firstPayment.getToliId() != null ? firstPayment.getToliId().toString() : null;
        
        // Create receipt
        WorkerPaymentReceipt receipt = new WorkerPaymentReceipt();
        receipt.setReceiptNumber(receiptNumber);
        receipt.setEmployerId(employerId);
        receipt.setToliId(toliId);
        receipt.setCreatedAt(LocalDateTime.now());
        receipt.setTotalRecords(processedPayments.size());
        receipt.setTotalAmount(totalAmount);
        receipt.setStatus("PROCESSED"); // Changed from GENERATED to match constraint
        
        // Save receipt first to get ID
        WorkerPaymentReceipt savedReceipt = repository.save(receipt);
        
        log.info("Created receipt {} with {} payments totaling {}", receiptNumber, processedPayments.size(), totalAmount);
        
        return savedReceipt;
    }
    
    private String generateReceiptNumber() {
        // Generate receipt number in format: RCP-YYYYMMDD-HHMMSS-XXX
        // Use retry mechanism to ensure uniqueness
        String receiptNumber;
        int maxAttempts = 10;
        int attempt = 0;
        
        do {
            LocalDateTime now = LocalDateTime.now();
            String dateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            
            // Use a combination of current nanos and a random component for better uniqueness
            long nanos = System.nanoTime();
            int randomComponent = (int) (Math.random() * 1000);
            String sequence = String.format("%03d", (nanos % 1000 + randomComponent) % 1000);
            
            receiptNumber = "RCP-" + dateTime + "-" + sequence;
            attempt++;
            
            // Check if this receipt number already exists in database
            if (queryDao.findByReceiptNumber(receiptNumber).isEmpty()) {
                log.info("Generated unique receipt number: {} (attempt {})", receiptNumber, attempt);
                return receiptNumber;
            }
            
            log.warn("Receipt number {} already exists, retrying... (attempt {})", receiptNumber, attempt);
            
            // No artificial delay needed - the next iteration will use a different nanoTime()
            // and random component, ensuring uniqueness without blocking
            
        } while (attempt < maxAttempts);
        
        throw new RuntimeException("Failed to generate unique receipt number after " + maxAttempts + " attempts");
    }

    public List<WorkerPaymentReceipt> findByStatus(String status) {
        log.info("Finding worker payment receipts with status: {}", status);
        return collectAll((page, size) -> queryDao.findByStatus(status, page, size));
    }

    public List<WorkerPaymentReceipt> findAll() {
        log.info("Finding all worker payment receipts");
        return collectAll(queryDao::findAll);
    }

    public Optional<WorkerPaymentReceipt> findByReceiptNumber(String receiptNumber) {
        log.info("Finding worker payment receipt by receipt number: {}", receiptNumber);
        return queryDao.findByReceiptNumber(receiptNumber);
    }

    public org.springframework.data.domain.Page<WorkerPaymentReceipt> findByStatusPaginated(String status, org.springframework.data.domain.Pageable pageable) {
        log.info("Finding worker payment receipts with status: {} (paginated)", status);
        var pageResult = queryDao.findByStatus(status, pageable.getPageNumber(), pageable.getPageSize());
        return new org.springframework.data.domain.PageImpl<>(pageResult.getContent(), pageable, pageResult.getTotalElements());
    }

    public org.springframework.data.domain.Page<WorkerPaymentReceipt> findAllPaginated(org.springframework.data.domain.Pageable pageable) {
        log.info("Finding all worker payment receipts (paginated)");
        var pageResult = queryDao.findAll(pageable.getPageNumber(), pageable.getPageSize());
        return new org.springframework.data.domain.PageImpl<>(pageResult.getContent(), pageable, pageResult.getTotalElements());
    }

    public org.springframework.data.domain.Page<WorkerPaymentReceipt> findByStatusAndDateRangePaginated(
            String status, LocalDateTime startDate, LocalDateTime endDate, org.springframework.data.domain.Pageable pageable) {
        log.info("Finding worker payment receipts with status: {} between {} and {} (paginated)", status, startDate, endDate);
        var pageResult = queryDao.findByStatusAndDateRange(status, startDate, endDate,
                pageable.getPageNumber(), pageable.getPageSize());
        return new org.springframework.data.domain.PageImpl<>(pageResult.getContent(), pageable, pageResult.getTotalElements());
    }

    public org.springframework.data.domain.Page<WorkerPaymentReceipt> findByDateRangePaginated(
            LocalDateTime startDate, LocalDateTime endDate, org.springframework.data.domain.Pageable pageable) {
        log.info("Finding worker payment receipts between {} and {} (paginated)", startDate, endDate);
        var pageResult = queryDao.findByDateRange(startDate, endDate,
                pageable.getPageNumber(), pageable.getPageSize());
        return new org.springframework.data.domain.PageImpl<>(pageResult.getContent(), pageable, pageResult.getTotalElements());
    }

    public WorkerPaymentReceipt updateStatus(String receiptNumber, String newStatus) {
        log.info("Updating status of worker payment receipt {} to {}", receiptNumber, newStatus);
        
        // Use JPA repository to fetch the entity in a managed state to ensure proper UPDATE
        WorkerPaymentReceipt receipt = repository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new RuntimeException("Worker payment receipt not found with number: " + receiptNumber));
        
        receipt.setStatus(newStatus);
        return repository.save(receipt);
    }
}
