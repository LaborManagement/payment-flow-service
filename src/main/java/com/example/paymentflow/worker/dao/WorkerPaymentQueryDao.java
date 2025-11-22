package com.example.paymentflow.worker.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.paymentflow.common.sql.SqlTemplateLoader;
import com.example.paymentflow.worker.entity.WorkerPayment;
import com.shared.common.dao.BaseQueryDao;

/**
 * DAO for Worker Payment read operations using custom queries.
 * All read logic is centralized here for better control and debugging.
 */
@Repository
public class WorkerPaymentQueryDao extends BaseQueryDao {

    private static final String BASE_SELECT_TEMPLATE = "sql/worker/worker_payments_base_select.sql";
    private static final String BASE_COUNT_TEMPLATE = "sql/worker/worker_payments_count.sql";

    private final DSLContext dsl;
    private final SqlTemplateLoader sqlTemplates;

    public WorkerPaymentQueryDao(DSLContext dsl, SqlTemplateLoader sqlTemplates) {
        this.dsl = dsl;
        this.sqlTemplates = sqlTemplates;
    }

    private String baseSelect() {
        return sqlTemplates.load(BASE_SELECT_TEMPLATE);
    }

    private String baseCount() {
        return sqlTemplates.load(BASE_COUNT_TEMPLATE);
    }

    /**
     * Find worker payment by ID
     */
    public Optional<WorkerPayment> findById(Long id) {
        String sql = baseSelect() + " WHERE id = :id";
        Map<String, Object> params = Map.of("id", id);
        return queryForObject(sql, params, this::mapWorkerPayment);
    }

    /**
     * Find worker payments with filters and pagination
     */
    public PageResult<WorkerPayment> findWithFilters(String status, String receiptNumber,
            String fileId, LocalDateTime startDate,
            LocalDateTime endDate, int page, int size) {

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        // Build dynamic where clause
        if (status != null && !status.trim().isEmpty()) {
            whereClause.append(" AND status = :status");
            params.put("status", status);
        }

        if (receiptNumber != null && !receiptNumber.trim().isEmpty()) {
            whereClause.append(" AND receipt_number = :receiptNumber");
            params.put("receiptNumber", receiptNumber);
        }

        if (fileId != null && !fileId.trim().isEmpty()) {
            whereClause.append(" AND file_id = :fileId");
            params.put("fileId", fileId);
        }

        if (startDate != null) {
            whereClause.append(" AND created_at >= :startDate");
            params.put("startDate", startDate);
        }

        if (endDate != null) {
            whereClause.append(" AND created_at <= :endDate");
            params.put("endDate", endDate);
        }

        String baseSql = baseSelect() + whereClause + " ORDER BY created_at DESC";
        String countSql = baseCount() + whereClause;

        return queryForPage(baseSql, countSql, params, page, size, this::mapWorkerPayment);
    }

    /**
     * Find by status with pagination
     */
    public PageResult<WorkerPayment> findByStatus(String status, int page, int size) {
        String sql = baseSelect() + " WHERE status = :status ORDER BY created_at DESC";
        String countSql = baseCount() + " WHERE status = :status";
        Map<String, Object> params = Map.of("status", status);

        return queryForPage(sql, countSql, params, page, size, this::mapWorkerPayment);
    }

    /**
     * Find by receipt number
     */
    public List<WorkerPayment> findByReceiptNumber(String receiptNumber) {
        String sql = baseSelect() + " WHERE receipt_number = :receiptNumber ORDER BY created_at DESC";
        Map<String, Object> params = Map.of("receiptNumber", receiptNumber);
        return queryForList(sql, params, this::mapWorkerPayment);
    }

    /**
     * Find by file ID with pagination
     */
    public PageResult<WorkerPayment> findByFileId(Long fileId, int page, int size) {
        String sql = baseSelect() + " WHERE file_id = :fileId ORDER BY created_at DESC";
        String countSql = baseCount() + " WHERE file_id = :fileId";
        Map<String, Object> params = Map.of("fileId", fileId);

        return queryForPage(sql, countSql, params, page, size, this::mapWorkerPayment);
    }

    /**
     * Get status counts for a file
     */
    public Map<String, Long> getStatusCountsByFileId(String fileId) {
        String sql = sqlTemplates.load("sql/worker/worker_payment_status_counts.sql");
        return dsl.resultQuery(sql, fileId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        record -> {
                            Integer statusId = record.get("status", Integer.class);
                            return statusId != null ? String.valueOf(statusId) : "UNKNOWN";
                        },
                        record -> {
                            Number count = record.get("count", Number.class);
                            return count != null ? count.longValue() : 0L;
                        },
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new));
    }

    /**
     * Find payments by request reference number prefix
     */
    public List<WorkerPayment> findByRequestReferenceNumberStartingWith(String prefix) {
        String sql = baseSelect() + " WHERE request_reference_number LIKE :prefix ORDER BY created_at DESC";
        Map<String, Object> params = Map.of("prefix", prefix + "%");
        return queryForList(sql, params, this::mapWorkerPayment);
    }

    /**
     * Find payments by date range
     */
    public PageResult<WorkerPayment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            int page, int size) {
        String sql = baseSelect() + " WHERE created_at BETWEEN :startDate AND :endDate ORDER BY created_at DESC";
        String countSql = baseCount() + " WHERE created_at BETWEEN :startDate AND :endDate";

        Map<String, Object> params = Map.of(
                "startDate", startDate,
                "endDate", endDate);

        return queryForPage(sql, countSql, params, page, size, this::mapWorkerPayment);
    }

    /**
     * Get summary statistics
     */
    public Map<String, Object> getPaymentSummary(String fileId) {
        String sql = sqlTemplates.load("sql/worker/worker_payment_summary.sql");
        Map<String, Object> result = dsl.resultQuery(sql, fileId).fetchOneMap();
        return result != null ? result : Map.of();
    }

    /**
     * Map ResultSet to WorkerPayment entity
     */
    private WorkerPayment mapWorkerPayment(ResultSet rs, int rowNum) throws SQLException {
        WorkerPayment payment = new WorkerPayment();

        // Map all fields according to the entity structure
        Long id = rs.getObject("id", Long.class);
        if (id != null) {
            payment.setId(id);
        }
        payment.setWorkerId(rs.getObject("worker_id", Long.class));
        payment.setToliId(rs.getObject("toli_id", Long.class));
        payment.setEmployerId(rs.getObject("employer_id", Long.class));
        payment.setBoardId(rs.getObject("board_id", Long.class));
        payment.setMonth(rs.getString("month"));
        payment.setTotalDays(rs.getObject("total_days", Integer.class));
        payment.setBasicWages(rs.getBigDecimal("basic_wages"));
        payment.setAdvance(rs.getBigDecimal("advance"));
        payment.setGrossWages(rs.getBigDecimal("gross_wages"));
        payment.setLevy(rs.getBigDecimal("levy"));
        payment.setNetWagesPayable(rs.getBigDecimal("net_wages_payable"));
        payment.setPaymentType(rs.getString("payment_type"));
        payment.setTxnRef(rs.getString("txn_ref"));
        payment.setReceiptNmbr(rs.getString("receipt_nmbr"));
        payment.setStatusId(rs.getObject("status_id", Integer.class));

        return payment;
    }
}
