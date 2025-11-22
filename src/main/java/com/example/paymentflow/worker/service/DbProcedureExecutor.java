package com.example.paymentflow.worker.service;

import org.slf4j.Logger;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shared.utilities.logger.LoggerFactoryProvider;

/**
 * Thin wrapper to execute database procedures used by the worker upload flow.
 * Keeps proc-specific parsing close to the service without pushing boilerplate into shared-lib.
 */
@Component
public class DbProcedureExecutor {

    private static final Logger log = LoggerFactoryProvider.getLogger(DbProcedureExecutor.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DbProcedureExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ValidationOutcome validateUploadedData(Long fileId) {
        log.info("Calling payment_flow.validate_uploaded_data for fileId={}", fileId);
        return jdbcTemplate.execute((java.sql.Connection con) -> {
            setUserContext(con);
            try (java.sql.CallableStatement cs = con.prepareCall("CALL payment_flow.validate_uploaded_data(?, ?)")) {
                cs.setLong(1, fileId);
                cs.registerOutParameter(2, java.sql.Types.OTHER);
                cs.execute();
                Object jsonObj = cs.getObject(2);
                String jsonString = jsonObj != null ? jsonObj.toString() : "{}";
                try {
                    JsonNode node = objectMapper.readTree(jsonString);
                    int total = node.path("total_records").asInt(0);
                    int valid = node.path("valid_count").asInt(0);
                    int invalid = node.path("invalid_count").asInt(0);
                    return new ValidationOutcome(total, valid, invalid, invalid == 0, jsonString);
                } catch (Exception ex) {
                    log.error("Failed to parse validation_result json: {}", jsonString, ex);
                    return new ValidationOutcome(0, 0, 0, false, jsonString);
                }
            }
        });
    }

    public String createPayments(Long fileId) {
        log.info("Calling payment_flow.create_payments for fileId={}", fileId);
        return jdbcTemplate.execute((java.sql.Connection con) -> {
            setUserContext(con);
            try (java.sql.CallableStatement cs = con.prepareCall("CALL payment_flow.create_payments(?, ?)")) {
                cs.setLong(1, fileId);
                cs.registerOutParameter(2, java.sql.Types.VARCHAR);
                cs.execute();
                return cs.getString(2);
            }
        });
    }

    private void setUserContext(java.sql.Connection con) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.warn("Skipping set_user_context because authentication is null/unauthenticated");
            return;
        }

        String userId = extractUserId(auth);
        if (userId == null || userId.isBlank()) {
            log.warn("Skipping set_user_context because no numeric userId could be extracted");
            return;
        }

        try (java.sql.PreparedStatement ps = con.prepareStatement("SELECT auth.set_user_context(?)")) {
            ps.setString(1, userId);
            ps.execute();
            log.debug("set_user_context executed for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to set user context for userId={}", userId, e);
        }
    }

    private String extractUserId(Authentication auth) {
        // Prefer JWT details userId
        Object details = auth.getDetails();
        if (details instanceof com.shared.security.JwtAuthenticationDetails jwtDetails) {
            Long id = jwtDetails.getUserId();
            if (id != null) {
                return id.toString();
            }
        }

        // Try principal getId()
        Object principal = auth.getPrincipal();
        if (principal != null) {
            try {
                java.lang.reflect.Method m = principal.getClass().getMethod("getId");
                Object id = m.invoke(principal);
                if (id != null) {
                    return id.toString();
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback to authentication name only if numeric
        String name = auth.getName();
        if (name != null && name.matches("\\d+")) {
            return name;
        }
        return null;
    }

    public static class ValidationOutcome {
        public final int totalRecords;
        public final int validCount;
        public final int invalidCount;
        public final boolean allValid;
        public final String rawJson;

        public ValidationOutcome(int totalRecords, int validCount, int invalidCount, boolean allValid, String rawJson) {
            this.totalRecords = totalRecords;
            this.validCount = validCount;
            this.invalidCount = invalidCount;
            this.allValid = allValid;
            this.rawJson = rawJson;
        }
    }
}
