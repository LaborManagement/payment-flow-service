package com.example.paymentflow.master.service;

import org.springframework.stereotype.Component;

/**
 * Stub client for fetching boardId and employerId for a user from
 * user_tenant_acl (auth-service).
 * Replace with actual Feign client or REST call as per your architecture.
 */
@Component
public class UserTenantAclClient {
    public UserTenantAclInfo getAclForUser(Long userId) {
        // TODO: Replace with actual call to auth-service
        // For now, return mock data
        return new UserTenantAclInfo("mockBoardId", "mockEmployerId");
    }

    public static class UserTenantAclInfo {
        private final String boardId;
        private final String employerId;

        public UserTenantAclInfo(String boardId, String employerId) {
            this.boardId = boardId;
            this.employerId = employerId;
        }

        public String getBoardId() {
            return boardId;
        }

        public String getEmployerId() {
            return employerId;
        }
    }
}
