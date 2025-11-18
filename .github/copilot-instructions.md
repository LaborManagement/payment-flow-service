# Copilot Instructions for Payment Flow Service

## Project Overview

Spring Boot microservice managing payment processing and reconciliation for the platform. Implements payment initiation, status tracking, worker/employer workflows, and external gateway integration.

**Stack:** Java 17 | Spring Boot 3.2.5 | PostgreSQL | jOOQ | JWT

**Setup:** Follow `documentation/LBE/guides/local-environment.md`

## Code Organization

```
com.example.paymentflow/
├── config/           # Spring configuration (JPA, jOOQ, etc.)
├── controller/       # REST API endpoints for payments and reconciliation
├── dao/              # Data Access Objects for complex queries (jOOQ-based)
├── dto/              # Data Transfer Objects (requests, responses)
├── entity/           # JPA entities (Payment, Worker, Employer, etc.)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic (payment processing, reconciliation)
└── util/             # Utility classes (SqlTemplateLoader, helpers)
```

## API Documentation Standards (Swagger/OpenAPI)

**All REST endpoints and DTOs must be documented using Swagger/OpenAPI annotations.**

### Controller Guidelines

- Annotate each controller class with `@Tag` to describe the API group.
- Annotate each endpoint method with:
  - `@Operation` (summary, description, tags, security, etc.)
  - `@ApiResponses` and one or more `@ApiResponse` for all possible HTTP responses (success, error, not found, etc.)
  - `@Parameter` for path/query/header parameters if not obvious from method signature.
- Use `@Schema` on DTO fields for field-level documentation, including descriptions and examples.
- Document all request/response bodies, path variables, and query parameters.
- Keep documentation up to date with code changes.

#### Example (Controller)

```java
@RestController
@RequestMapping("/api/example")
@Tag(name = "Example", description = "Example API endpoints")
public class ExampleController {

  @GetMapping("/{id}")
  @Operation(summary = "Get example by ID", description = "Returns an example resource by its ID.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resource found"),
    @ApiResponse(responseCode = "404", description = "Resource not found")
  })
  public ResponseEntity<ExampleDto> getExample(@PathVariable Long id) {
    // ...
  }
}
```

#### Example (DTO)

```java
import io.swagger.v3.oas.annotations.media.Schema;

public class ExampleDto {
  @Schema(description = "Unique identifier", example = "123")
  private Long id;

  @Schema(description = "Example name", example = "Sample")
  private String name;
  // ...
}
```

### Best Practices

- Always update Swagger annotations when changing endpoints or DTOs.
- Use meaningful summaries and descriptions.
- Include all possible response codes.
- Use `@Schema` for DTO fields, especially for request/response bodies.
- Review generated Swagger UI to ensure clarity and completeness.

**Reference:** See `springdoc-openapi` documentation and existing controllers for patterns.

- Follow Spring Boot conventions and existing patterns
- Use constructor injection for dependencies
- Add JavaDoc for public APIs
- Use meaningful variable names (`tenantId`, `paymentId`, `workerId`)
- Return DTOs from controllers, not entities
- Document endpoints with OpenAPI annotations

## Database Access Patterns ⭐ CRITICAL

**ALWAYS read `documentation/LBE/guides/data-access-patterns.md` before writing database code.**

| Pattern              | Use For                               | Examples                                |
| -------------------- | ------------------------------------- | --------------------------------------- |
| **JPA Repository**   | CRUD, writes, simple reads            | `PaymentRepository`, `WorkerRepository` |
| **jOOQ DSL**         | Complex queries, multi-joins, filters | `WorkerQueryDao`, `PaymentQueryDao`     |
| **jOOQ + SQL Files** | Analyst reports, aggregations, CTEs   | `sql/worker/worker_payment_summary.sql` |

### Rules for ALL Patterns

� **Security:** Always set RLS context: `SELECT auth.set_user_context(:userId)`  
🔄 **Transactions:** Use `@Transactional` for writes, `@Transactional(readOnly=true)` for reads  
✅ **Testing:** Test with multiple personas, verify RLS isolation

**Details:** See `documentation/LBE/guides/data-access-patterns.md`

## Security Guidelines

### Authorization & Data Access

- Validate all user input with **Bean Validation** annotations
- Check authorization before accessing resources:
  - Consult `documentation/LBE/reference/policy-matrix.md` for required policies
  - Use appropriate `@PreAuthorize` annotations
- **Never log sensitive data** (payment details, API keys, personal information)
- Implement **CORS** configuration properly for production

### RLS & Multi-Tenancy

- **Always** set tenant context before queries
- Test multi-tenancy isolation thoroughly
- Always include `tenantId` in audit logs
- Follow patterns in `documentation/LBE/foundations/data-guardrails-101.md`

## Audit Logging Guidelines ⭐ CRITICAL

**Read:** `documentation/LBE/architecture/audit-design.md` | `documentation/LBE/reference/audit-quick-reference.md`

### Configuration (DO NOT CHANGE)

```yaml
shared-lib:
  audit:
    enabled: true
    service-name: payment-flow-service
    source-schema: payment_flow
  entity-audit:
    enabled: true
```

### 1. API-Level Auditing with @Auditable

```java
@PostMapping
@Auditable(
    action = "PAYMENT_INITIATED",
    entityType = "PAYMENT",
    description = "Worker initiated payment request"
)
public ResponseEntity<Payment> createPayment(@RequestBody PaymentRequest request) {
    // Audit logged automatically
}
```

### 2. Entity-Level Auditing

```java
@Entity
@EntityListeners(SharedEntityAuditListener.class)
public class Payment {
    // All changes tracked with before/after values + hash chain
}
```

### Best Practices

**DO:**

- ✅ Use `@Auditable` on payment endpoints (create, approve, reject, cancel)
- ✅ Use `@EntityListeners` on Payment, Worker, Employer entities
- ✅ Log file uploads with metadata (size, type, worker_id)
- ✅ Track payment status transitions

**DON'T:**

- ❌ Log bank account numbers, card details
- ❌ Skip audit for failed payments (log failures too)
- ❌ Use generic action names (be specific: PAYMENT_APPROVED)

**Troubleshooting:** Check `shared-lib.audit.enabled=true` | Verify DB grants | See audit-design.md

## Common Tasks

### Adding a New API Endpoint

**Step 1:** Consult `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§13-16), `policy-matrix.md`, `data-access-patterns.md`  
**Step 2:** Choose pattern: JPA (simple), jOOQ DSL (complex), jOOQ+SQL (reports)  
**Step 3:** Implement: DTO → DAO/Repository → Service → Controller with `@PreAuthorize`  
**Step 4:** Register: Migration → `auth.endpoints` + `auth.endpoint_policies`  
**Step 5:** Update `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` + `policy-matrix.md`  
**Step 6:** Test: Authorization + RLS isolation

### Adding SQL Template

1. Create: `src/main/resources/sql/<domain>/<template>.sql`
2. Load: `SqlTemplateLoader.load("sql/<domain>/<template>.sql")`
3. Test: Template loading + execution
4. Document: README with parameters

### Debugging Payment Issues

1. Check payment status and gateway responses
2. Review reconciliation records
3. Check audit logs
4. Verify RLS context: `SELECT current_setting('app.current_user_id')`
5. Consult `playbooks/troubleshoot-auth.md`

## Important Considerations

- **RLS:** Always use `RLSContext` for tenant isolation. Test multi-tenancy thoroughly.
- **Performance:** Use pagination, caching, proper indexes. Profile jOOQ queries.
- **Migrations:** SQL scripts only. Test on production copies. Document in `TABLE_NAMES_REFERENCE.md`.

---

# Payment Flow Service — Documentation Reference 📚

**Source of Truth:** `documentation/LBE/` - Always consult before coding

## Essential Reading 🎯

**Start Here:**

- `documentation/LBE/README.md` – Guided journey through auth system
- `documentation/LBE/architecture/overview.md` – System topology and flows
- `documentation/LBE/architecture/data-map.md` – Table relationships
- `documentation/LBE/architecture/audit-design.md` – Audit system ⭐

**Foundations:**

- `documentation/LBE/foundations/access-control-101.md` – RBAC fundamentals
- `documentation/LBE/foundations/data-guardrails-101.md` – RLS primer

## Implementation Guides 💻

**Data Access (CRITICAL):**

- `documentation/LBE/guides/data-access-patterns.md` ⭐ – **Read before ANY database code**

**Workflows:**

- `documentation/LBE/guides/login-to-data.md` – Login → JWT → RLS flow
- `documentation/LBE/guides/setup/rbac.md` – RBAC setup
- `documentation/LBE/guides/setup/vpd.md` – RLS/VPD setup
- `documentation/LBE/guides/extend-access.md` – Adding policies
- `documentation/LBE/guides/verify-permissions.md` – Testing

## Quick Reference 📖

- `documentation/LBE/reference/role-catalog.md` – All roles
- `documentation/LBE/reference/policy-matrix.md` – Policy mappings
- `documentation/LBE/reference/audit-quick-reference.md` – Audit guide
- `documentation/LBE/reference/TABLE_NAMES_REFERENCE.md` – Schema reference
- `documentation/LBE/reference/recent-updates.md` – Latest changes

## Troubleshooting 🔧

- `documentation/LBE/playbooks/troubleshoot-auth.md` – Auth issues
- `documentation/LBE/reference/postgres-operations.md` – Database ops

## Maintenance Checklist ✅

**Adding Endpoint:**

1. Choose data pattern (`data-access-patterns.md`)
2. Implement: DTO → DAO → Service → Controller
3. Register: `auth.endpoints` + `auth.endpoint_policies`
4. Update: `PHASE5_ENDPOINT_POLICY_MAPPINGS.md` (§13-16) + `policy-matrix.md`
5. Test: Authorization + RLS

**Modifying Roles/Policies:**

1. SQL migration
2. Update: `policy-matrix.md` + `role-catalog.md`
3. Test with personas
4. Document in `recent-updates.md`

**Schema Changes:**

1. Migration script
2. Update: `data-map.md` + `TABLE_NAMES_REFERENCE.md`
3. Test RLS
4. Document in `recent-updates.md`

**Audit Changes:**

1. Match `audit-quick-reference.md`
2. Update `audit-design.md` (Payment Flow section)
3. Ensure compliance

## Key Principles 🎯

- 🔒 **Security:** Never bypass RLS | Always validate JWT | Set session context | Check authorization | No sensitive logging
- 📝 **Documentation:** Read docs first | Update with code | Keep in sync
- 🧪 **Testing:** Multiple personas | Tenant isolation | RBAC | Error scenarios

## Quick Links 🔗

| Task               | Documentation                           |
| ------------------ | --------------------------------------- |
| Local setup        | `guides/local-environment.md`           |
| Architecture       | `architecture/overview.md`              |
| **Data access**    | **`guides/data-access-patterns.md`** ⭐ |
| Add endpoint       | `guides/extend-access.md`               |
| Create role/policy | `guides/setup/rbac.md`                  |
| Debug auth         | `playbooks/troubleshoot-auth.md`        |
| RLS                | `foundations/data-guardrails-101.md`    |
| PostgreSQL ops     | `reference/postgres-operations.md`      |
| Recent changes     | `reference/recent-updates.md`           |

---

**Remember:** `documentation/LBE/` is the single source of truth. Consult before changing, update with changes.
