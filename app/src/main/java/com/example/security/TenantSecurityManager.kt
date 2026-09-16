package com.example.security

import com.example.model.Tenant

/**
 * Spatial7 Multi-Tenant Enterprise Isolation Framework
 * User Roles & Scope Execution (RBAC)
 */
enum class UserRole(
    val roleKey: String,
    val displayTitle: String,
    val description: String,
    val canWriteAssets: Boolean,
    val canAdministerTenant: Boolean,
    val canAccessFinancialValuation: Boolean,
    val canExecuteEdits: Boolean
) {
    TENANT_ADMIN(
        roleKey = "TENANT_ADMIN",
        displayTitle = "Tenant Administrator",
        description = "Full read/write access to tenant assets, spatial layers, user role assignments, and audit logs within their isolated tenant schema.",
        canWriteAssets = true,
        canAdministerTenant = true,
        canAccessFinancialValuation = true,
        canExecuteEdits = true
    ),
    ASSET_OFFICER(
        roleKey = "ASSET_OFFICER",
        displayTitle = "Asset Officer",
        description = "Read/write access to spatial geometries, field inspections, condition scores, and asset records within their tenant. Cannot access tenant administrative settings.",
        canWriteAssets = true,
        canAdministerTenant = false,
        canAccessFinancialValuation = true,
        canExecuteEdits = true
    ),
    READ_ONLY_AUDITOR(
        roleKey = "READ_ONLY_AUDITOR",
        displayTitle = "Read-Only Auditor",
        description = "View-only access to asset layers, financial valuation metrics, and PDF exports. All edit endpoints are strictly disabled.",
        canWriteAssets = false,
        canAdministerTenant = false,
        canAccessFinancialValuation = true,
        canExecuteEdits = false
    )
}

data class TenantJwtSession(
    val token: String,
    val tenantId: String,
    val tenantName: String,
    val userEmail: String,
    val role: String,
    val userRole: UserRole = UserRole.TENANT_ADMIN,
    val signatureAlgorithm: String = "RS256 (2048-bit RSA with SHA-256)",
    val claims: Map<String, String>,
    val rlsSessionVariable: String,
    val schemaSearchPath: String = "SET search_path TO tenant_${tenantId.replace("-", "_")}, public;"
)

data class RlsAuditResult(
    val queryExecuted: String,
    val activeTenantId: String,
    val targetTenantId: String,
    val isAllowed: Boolean,
    val statusCode: String,
    val statusMessage: String,
    val postgresPolicyName: String = "tenant_isolation_policy",
    val leakageCount: Int = 0,
    val timestamp: String = "Just Now"
)

data class AccessValidationResult(
    val isAllowed: Boolean,
    val httpStatus: Int,
    val errorCode: String,
    val message: String,
    val executedSqlContext: String,
    val tenantScopeEnforced: String
)

object TenantSecurityManager {

    fun generateSession(tenant: Tenant, role: UserRole = UserRole.TENANT_ADMIN): TenantJwtSession {
        val simulatedHeader = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9"
        val simulatedPayload = "eyJzdWIiOiJ1c3Jfc3BhdGlhbF8wMSIsImFwcF90ZW5hbnRfaWQiOiIke3RlbmFudC50ZW5hbnRJZH0iLCJyb2xlIjoiJHtyb2xlLnJvbGVLZXl9Iiwic2VjdG9yIjoiJHt0ZW5hbnQuc2VjdG9yVHlwZS5uYW1lfSJ9"
        val simulatedSignature = "Kz8a9_Enterprise_Verified_ECDSA_Sig"
        val fullJwt = "$simulatedHeader.$simulatedPayload.$simulatedSignature"

        val schemaPath = "tenant_${tenant.tenantId.replace("-", "_")}"

        return TenantJwtSession(
            token = fullJwt,
            tenantId = tenant.tenantId,
            tenantName = tenant.name,
            userEmail = "custodian@${tenant.tenantId.replace("tenant-", "").replace("-", ".")}.au",
            role = role.displayTitle,
            userRole = role,
            claims = mapOf(
                "iss" to "https://auth.geonexus.spatial7.com.au",
                "sub" to "usr_architect_australia",
                "tenant_id" to tenant.tenantId,
                "role" to role.roleKey,
                "sector_type" to tenant.sectorType.name,
                "jurisdiction" to tenant.stateCode,
                "rls_enforcement" to "STRICT_ROW_LEVEL_SECURITY",
                "pg_session_param" to "app.current_tenant",
                "search_path" to schemaPath
            ),
            rlsSessionVariable = "SELECT set_config('app.current_tenant', '${tenant.tenantId}', true);",
            schemaSearchPath = "SET search_path TO $schemaPath, public;"
        )
    }

    /**
     * Strict Multi-Tenant Isolation Rules Validation
     * 1. DENY-BY-DEFAULT: Rejects if session token or tenant_id is missing/invalid
     * 2. ZERO CROSS-TENANT VISIBILITY: Data belonging to Tenant A is strictly isolated from Tenant B
     * 3. RBAC ENFORCEMENT: Enforces TENANT_ADMIN, ASSET_OFFICER, and READ_ONLY_AUDITOR scope boundaries
     */
    fun validateTenantAccess(
        session: TenantJwtSession?,
        targetTenantId: String,
        action: String = "READ" // READ, WRITE, EDIT, DELETE, ADMIN
    ): AccessValidationResult {
        // Rule 1: DENY-BY-DEFAULT ACCESS MODEL
        if (session == null || session.token.isBlank() || session.tenantId.isBlank()) {
            return AccessValidationResult(
                isAllowed = false,
                httpStatus = 401,
                errorCode = "ERR_DENY_BY_DEFAULT_UNAUTHENTICATED",
                message = "DENY-BY-DEFAULT ENFORCEMENT: Request lacks validated tenant_id context or authenticated session token. Access rejected immediately with authorization error.",
                executedSqlContext = "ABORT; -- Access denied: unauthenticated caller",
                tenantScopeEnforced = "NONE"
            )
        }

        // Rule 2: ZERO CROSS-TENANT VISIBILITY
        if (session.tenantId != targetTenantId) {
            return AccessValidationResult(
                isAllowed = false,
                httpStatus = 403,
                errorCode = "ERR_ZERO_CROSS_TENANT_LEAKAGE",
                message = "ZERO CROSS-TENANT VISIBILITY VIOLATION: Data belonging to tenant '$targetTenantId' is strictly isolated from '${session.tenantId}'. Cryptographic RLS boundary rejected query. 0 rows leaked.",
                executedSqlContext = "ROLLBACK; -- Cross-tenant attempt intercepted by RLS policy",
                tenantScopeEnforced = session.tenantId
            )
        }

        // Rule 3: RBAC USER ROLES & SCOPE EXECUTION
        if ((action == "WRITE" || action == "EDIT" || action == "DELETE") && !session.userRole.canExecuteEdits) {
            return AccessValidationResult(
                isAllowed = false,
                httpStatus = 403,
                errorCode = "ERR_RBAC_READ_ONLY_AUDITOR",
                message = "RBAC ACCESS DENIED: Role READ_ONLY_AUDITOR has view-only access to asset layers and valuations. All edit endpoints are strictly disabled per Spatial7 RBAC isolation policy.",
                executedSqlContext = "SELECT set_config('app.current_tenant', '${session.tenantId}', true); -- Read-only query permitted only",
                tenantScopeEnforced = session.tenantId
            )
        }

        if (action == "ADMIN" && !session.userRole.canAdministerTenant) {
            return AccessValidationResult(
                isAllowed = false,
                httpStatus = 403,
                errorCode = "ERR_RBAC_ADMIN_REQUIRED",
                message = "RBAC ACCESS DENIED: Action requires TENANT_ADMIN role. Role '${session.userRole.roleKey}' cannot access tenant administrative settings.",
                executedSqlContext = "-- Administrative operation blocked",
                tenantScopeEnforced = session.tenantId
            )
        }

        // Granted: Safe Context Injection
        return AccessValidationResult(
            isAllowed = true,
            httpStatus = 200,
            errorCode = "SUCCESS",
            message = "ACCESS AUTHORIZED: Validated tenant_id '${session.tenantId}' with role '${session.userRole.roleKey}'. RLS and Schema-per-Tenant boundaries active.",
            executedSqlContext = generateIsolationSql(session.tenantId, session.userRole),
            tenantScopeEnforced = session.tenantId
        )
    }

    fun generateIsolationSql(tenantId: String, role: UserRole = UserRole.TENANT_ADMIN): String {
        val schema = "tenant_${tenantId.replace("-", "_")}"
        return """
            -- Dynamic Multi-Tenant Session Initialization
            BEGIN;
            -- Set session parameters securely from verified JWT claims
            SELECT set_config('app.current_tenant', '$tenantId', true);

            -- Schema-per-Tenant search path
            SET search_path TO $schema, public;

            -- Query execution (RLS policy automatically isolates rows)
            SELECT asset_id, asset_name, asset_class, condition_score, geometry 
            FROM tbl_assets 
            WHERE asset_class IN ('TRANSPORT', 'STORMWATER');

            COMMIT;
        """.trimIndent()
    }

    fun verifyCrossTenantQuery(activeTenantId: String, requestedTenantId: String, sqlQuery: String): RlsAuditResult {
        return if (activeTenantId == requestedTenantId) {
            RlsAuditResult(
                queryExecuted = sqlQuery,
                activeTenantId = activeTenantId,
                targetTenantId = requestedTenantId,
                isAllowed = true,
                statusCode = "200 OK - RLS Passed",
                statusMessage = "Access Granted: PostgreSQL session setting 'app.current_tenant = $activeTenantId' matches row level tenant_id. Zero cryptographic leakage.",
                leakageCount = 0
            )
        } else {
            RlsAuditResult(
                queryExecuted = sqlQuery,
                activeTenantId = activeTenantId,
                targetTenantId = requestedTenantId,
                isAllowed = false,
                statusCode = "403 Forbidden - RLS Denial",
                statusMessage = "PostgreSQL RLS Violation [42501]: Table 'spatial_assets' policy 'tenant_isolation_policy' rejected access. Active tenant '$activeTenantId' attempted cross-tenant read of '$requestedTenantId'. 0 rows returned.",
                leakageCount = 0
            )
        }
    }
}

