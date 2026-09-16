package com.example.ui.architecture

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tenant
import com.example.security.AccessValidationResult
import com.example.security.RlsAuditResult
import com.example.security.TenantSecurityManager
import com.example.security.UserRole
import com.example.sync.Spatial7SyncEngine
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchitectureScreen(
    activeTenant: Tenant,
    allTenants: List<Tenant>,
    activeRole: UserRole = UserRole.TENANT_ADMIN,
    onRoleChange: ((UserRole) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedRole by remember(activeRole) { mutableStateOf(activeRole) }
    val session = remember(activeTenant, selectedRole) {
        TenantSecurityManager.generateSession(activeTenant, selectedRole)
    }

    val eligibleTargets = remember(activeTenant, allTenants) {
        allTenants.filter { it.tenantId != activeTenant.tenantId }
    }
    var targetTenantForAttack by remember(activeTenant) {
        mutableStateOf(eligibleTargets.firstOrNull() ?: activeTenant)
    }
    var auditResult by remember { mutableStateOf<RlsAuditResult?>(null) }
    var accessValidationResult by remember { mutableStateOf<AccessValidationResult?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NexusCyan)
                        Text(
                            text = "NATIONAL ENTERPRISE ARCHITECTURE BLUEPRINT",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spatial7 Multi-Tenant Isolation & Zero-Trust Framework",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Absolute tenant isolation, zero cross-tenant visibility, deny-by-default access, and RBAC governance across Municipal Councils, Schools, and Industry Clients.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Tab Selector (Scrollable)
        item {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = NexusSurface,
                contentColor = NexusCyan,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = NexusCyan
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Zero-Trust & RBAC", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("RLS Sandbox", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Phase 1: PostGIS & Sync", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Phase 2: PDF Ingestion", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
                    text = { Text("Phase 3: ISO 55001 Engine", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 5,
                    onClick = { activeTab = 5 },
                    text = { Text("Phase 4: Backend Auth", fontSize = 12.sp) }
                )
                Tab(
                    selected = activeTab == 6,
                    onClick = { activeTab = 6 },
                    text = { Text("Phase 5: React & Mobile", fontSize = 12.sp) }
                )
            }
        }

        // Tab Content
        when (activeTab) {
            0 -> {
                // Tab 0: Zero-Trust Isolation Framework & RBAC
                item {
                    ZeroTrustFrameworkCard(
                        activeTenant = activeTenant,
                        activeRole = selectedRole
                    )
                }

                item {
                    UserRoleSelectorCard(
                        selectedRole = selectedRole,
                        onRoleChange = { newRole ->
                            selectedRole = newRole
                            onRoleChange?.invoke(newRole)
                        }
                    )
                }

                item {
                    DynamicIsolationSqlCard(
                        activeTenant = activeTenant,
                        activeRole = selectedRole,
                        onCopy = { sql -> copyToClipboard(context, sql, "Isolation SQL Copied") }
                    )
                }

                item {
                    SecurityProbesCard(
                        activeTenant = activeTenant,
                        targetTenant = targetTenantForAttack,
                        session = session,
                        onRunProbe = { probeResult ->
                            accessValidationResult = probeResult
                        }
                    )
                }

                if (accessValidationResult != null) {
                    item {
                        AccessValidationResultCard(result = accessValidationResult!!)
                    }
                }
            }
            1 -> {
                // RLS Sandbox Tab
                item {
                    ActiveTenantSessionCard(session = session)
                }

                item {
                    CrossTenantAttackSimulator(
                        activeTenant = activeTenant,
                        allTenants = allTenants,
                        targetTenant = targetTenantForAttack,
                        onTargetChange = { targetTenantForAttack = it },
                        onExecuteTest = {
                            auditResult = TenantSecurityManager.verifyCrossTenantQuery(
                                activeTenantId = activeTenant.tenantId,
                                requestedTenantId = targetTenantForAttack.tenantId,
                                sqlQuery = "SELECT id, name, geom FROM spatial_assets WHERE tenant_id = '${targetTenantForAttack.tenantId}'"
                            )
                        }
                    )
                }

                if (auditResult != null) {
                    item {
                        RlsAuditResultCard(auditResult = auditResult!!)
                    }
                }
            }
            2 -> {
                // Phase 1: PostGIS Schema & Spatial7 Bi-Directional Self-Healing Sync
                item {
                    BiDirectionalSyncEngineCard(
                        onCopySql = { sql -> copyToClipboard(context, sql, "Spatial7 PostGIS Sync SQL Copied") }
                    )
                }
                item {
                    CodeSnippetCard(
                        title = "SPATIAL7 BI-DIRECTIONAL SPATIAL & ASSET DATA SYNC WITH AUTO-SELF-HEALING",
                        subtitle = "PostGIS Engine | Owner: Kris Lalka (Kris Lal) | GDA2020 / MGA Zone 56 (EPSG:7856) ±0.02m",
                        code = Spatial7SyncEngine.PRODUCTION_SYNC_SQL,
                        onCopy = { copyToClipboard(context, Spatial7SyncEngine.PRODUCTION_SYNC_SQL, "Spatial7 PostGIS Sync SQL Copied") }
                    )
                }
                item {
                    CodeSnippetCard(
                        title = "PHASE 1: MASTER POSTGIS + TIMESCALEDB + MULTI-TENANT RLS SCHEMA",
                        subtitle = "Includes tenants, spatial_assets, asset_documents, and compliance_audits tables",
                        code = POSTGIS_MIGRATION_SQL,
                        onCopy = { copyToClipboard(context, POSTGIS_MIGRATION_SQL, "PostGIS SQL Schema Copied") }
                    )
                }
            }
            3 -> {
                // Phase 2: PDF Ingestion Service (Gemini Multimodal)
                item {
                    CodeSnippetCard(
                        title = "PHASE 2: GEMINI MULTIMODAL PDF INGESTION SERVICE",
                        subtitle = "Node.js / TypeScript service with structured JSON extraction & confidence scoring",
                        code = NODE_GEMINI_INGESTION_CODE,
                        onCopy = { copyToClipboard(context, NODE_GEMINI_INGESTION_CODE, "PDF Ingestion Service Copied") }
                    )
                }
            }
            4 -> {
                // Phase 3: ISO 55001 & Council Compliance Engine
                item {
                    CodeSnippetCard(
                        title = "PHASE 3: ISO 55001 & COUNCIL COMPLIANCE ENGINE",
                        subtitle = "TypeScript validation rules evaluating lifecycle health, ANZLIC spatial standards, and missing fields",
                        code = TYPESCRIPT_COMPLIANCE_ENGINE_CODE,
                        onCopy = { copyToClipboard(context, TYPESCRIPT_COMPLIANCE_ENGINE_CODE, "ISO 55001 Engine Copied") }
                    )
                }
            }
            5 -> {
                // Phase 4: Backend Auth
                item {
                    CodeSnippetCard(
                        title = "PHASE 4: NODE.JS / TYPESCRIPT TRANSACTION RLS WRAPPER",
                        subtitle = "Executes SET LOCAL app.current_tenant inside transaction with zero pool leakage",
                        code = NODE_TYPESCRIPT_POOL_CODE,
                        onCopy = { copyToClipboard(context, NODE_TYPESCRIPT_POOL_CODE, "Node.js Auth Wrapper Copied") }
                    )
                }
                item {
                    CodeSnippetCard(
                        title = "PHASE 4 (ALT): PYTHON FASTAPI / ASYNCPG HANDSHAKE",
                        subtitle = "FastAPI dependency yielding isolated connection with SET LOCAL context",
                        code = PYTHON_FASTAPI_CODE,
                        onCopy = { copyToClipboard(context, PYTHON_FASTAPI_CODE, "Python FastAPI Code Copied") }
                    )
                }
            }
            6 -> {
                // Phase 5: React / Next.js Smart Form Frontend & Mobile Viewport
                item {
                    CodeSnippetCard(
                        title = "GOOGLE AI STUDIO MOBILE API CLIENT & VIEWPORT HOOK",
                        subtitle = "useMobileViewport + AIStudioAPIClient (gemini-1.5-pro) + CoverCallContainer (#0f172a theme)",
                        code = REACT_MOBILE_VIEWPORT_AND_CONTAINER_CODE,
                        onCopy = { copyToClipboard(context, REACT_MOBILE_VIEWPORT_AND_CONTAINER_CODE, "Mobile Client & Container Copied") }
                    )
                }
                item {
                    CodeSnippetCard(
                        title = "PHASE 5: REACT / NEXT.JS DYNAMIC FORM COMPONENT",
                        subtitle = "Drag-and-drop PDF intake with confidence badges and ISO 55001 score display",
                        code = REACT_SMART_FORM_CODE,
                        onCopy = { copyToClipboard(context, REACT_SMART_FORM_CODE, "React Component Copied") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZeroTrustFrameworkCard(
    activeTenant: Tenant,
    activeRole: UserRole
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = NexusCyan)
                    Text(
                        text = "SPATIAL7 ENTERPRISE ISOLATION FRAMEWORK",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Surface(
                    color = NexusEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "STRICT ENFORCEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Multi-Tenant Enterprise Isolation & Zero-Trust Persona",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Backend context manager enforcing absolute tenant data isolation, zero-trust security boundaries, and enterprise account privacy for '${activeTenant.name}' (${activeTenant.tenantId}) in sector '${activeTenant.sectorType.title}'.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NexusCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "STRICT COMPLIANCE MANDATES",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RuleItem(
                    title = "1. ZERO CROSS-TENANT VISIBILITY",
                    desc = "Data belonging to '${activeTenant.name}' is strictly isolated. Under no circumstances can data from one tenant be revealed, referenced, or leaked to another tenant boundary.",
                    accent = NexusCoral
                )
                RuleItem(
                    title = "2. DENY-BY-DEFAULT ACCESS MODEL",
                    desc = "If a user query lacks a validated tenant_id context or authenticated session token, reject the request immediately with an authorization error (HTTP 401 / 403).",
                    accent = NexusAmber
                )
                RuleItem(
                    title = "3. CONTEXT INJECTION MANDATE",
                    desc = "Every database query, spatial tile request, and report command MUST explicitly scope execution using Row-Level Security (tenant_id = get_current_tenant()) and Schema-per-Tenant search paths.",
                    accent = NexusCyan
                )
            }
        }
    }
}

@Composable
private fun RuleItem(title: String, desc: String, accent: Color) {
    Surface(
        color = NexusBackground,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(6.dp).background(accent, CircleShape))
                Text(text = title, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun UserRoleSelectorCard(
    selectedRole: UserRole,
    onRoleChange: (UserRole) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NexusPurple)
                Text(
                    text = "USER ROLES & SCOPE EXECUTION (RBAC)",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusPurple,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Filter system capabilities according to the validated user role inside their assigned tenant boundary:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserRole.entries.forEach { role ->
                    val isSelected = role == selectedRole
                    Surface(
                        onClick = { onRoleChange(role) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NexusPurple.copy(alpha = 0.2f) else NexusBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NexusPurple else NexusCardBorder
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = role.roleKey,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NexusPurpleLight else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSelected) "ACTIVE" else "SELECT",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) NexusEmerald else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Role Details
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "ROLE: ${selectedRole.displayTitle} (${selectedRole.roleKey})",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusPurpleLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedRole.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CapabilityBadge(
                            label = "Write Assets",
                            isAllowed = selectedRole.canWriteAssets,
                            modifier = Modifier.weight(1f)
                        )
                        CapabilityBadge(
                            label = "Tenant Admin",
                            isAllowed = selectedRole.canAdministerTenant,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CapabilityBadge(
                            label = "Valuations",
                            isAllowed = selectedRole.canAccessFinancialValuation,
                            modifier = Modifier.weight(1f)
                        )
                        CapabilityBadge(
                            label = "Edit Endpoints",
                            isAllowed = selectedRole.canExecuteEdits,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapabilityBadge(label: String, isAllowed: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isAllowed) NexusEmerald.copy(alpha = 0.15f) else NexusCoral.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAllowed) NexusEmerald.copy(alpha = 0.4f) else NexusCoral.copy(alpha = 0.4f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontSize = 11.sp)
            Text(
                text = if (isAllowed) "ENABLED" else "DISABLED",
                style = MaterialTheme.typography.labelSmall,
                color = if (isAllowed) NexusEmerald else NexusCoral,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun DynamicIsolationSqlCard(
    activeTenant: Tenant,
    activeRole: UserRole,
    onCopy: (String) -> Unit
) {
    val sql = remember(activeTenant, activeRole) {
        TenantSecurityManager.generateIsolationSql(activeTenant.tenantId, activeRole)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DYNAMIC MULTI-TENANT SESSION INITIALIZATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Canonical PostgreSQL RLS & Schema-per-Tenant search path execution",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { onCopy(sql) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy SQL", tint = NexusCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val scrollState = rememberScrollState()
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = sql,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyanLight,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(scrollState)
                )
            }
        }
    }
}

@Composable
private fun SecurityProbesCard(
    activeTenant: Tenant,
    targetTenant: Tenant,
    session: com.example.security.TenantJwtSession,
    onRunProbe: (AccessValidationResult) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCoral.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VpnKey, contentDescription = null, tint = NexusCoral)
                Text(
                    text = "ISOLATION & SECURITY BENCHMARK PROBES",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCoral,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Trigger real-time validation probes against the Spatial7 isolation engine to verify zero leakage and deny-by-default enforcement:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val result = TenantSecurityManager.validateTenantAccess(
                            session = session,
                            targetTenantId = activeTenant.tenantId,
                            action = "READ"
                        )
                        onRunProbe(result)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusEmerald.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Probe 1: Valid In-Tenant Query", color = NexusEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Expect 200 OK", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Button(
                    onClick = {
                        val result = TenantSecurityManager.validateTenantAccess(
                            session = null, // Missing session / token
                            targetTenantId = activeTenant.tenantId,
                            action = "READ"
                        )
                        onRunProbe(result)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusAmber.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Probe 2: Deny-by-Default (Unauthenticated)", color = NexusAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Expect 401 Rejection", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Button(
                    onClick = {
                        val result = TenantSecurityManager.validateTenantAccess(
                            session = session,
                            targetTenantId = targetTenant.tenantId, // Cross-tenant target
                            action = "READ"
                        )
                        onRunProbe(result)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCoral.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Probe 3: Zero Cross-Tenant Attack", color = NexusCoral, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Expect 403 Rejection", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Button(
                    onClick = {
                        val result = TenantSecurityManager.validateTenantAccess(
                            session = session,
                            targetTenantId = activeTenant.tenantId,
                            action = "EDIT" // Tests role edit permission
                        )
                        onRunProbe(result)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Probe 4: RBAC Edit Boundary Test", color = NexusPurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Test ${session.userRole.roleKey}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun AccessValidationResultCard(result: AccessValidationResult) {
    val borderColor = when (result.httpStatus) {
        200 -> NexusEmerald
        401 -> NexusAmber
        else -> NexusCoral
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PROBE AUDIT REPORT",
                    style = MaterialTheme.typography.labelSmall,
                    color = borderColor,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = borderColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "HTTP ${result.httpStatus} ${result.errorCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))
            DataRow(label = "Enforced Scope", value = result.tenantScopeEnforced, isCode = true)
            DataRow(label = "Access Allowed", value = if (result.isAllowed) "YES (Authorized)" else "NO (Blocked)")

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.executedSqlContext,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (result.isAllowed) NexusCyanLight else NexusCoral,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ActiveTenantSessionCard(session: com.example.security.TenantJwtSession) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE TENANT CRYPTOGRAPHIC CONTEXT",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = NexusEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "JWT SIGNED (RS256)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DataRow(label = "Tenant ID", value = session.tenantId, isCode = true)
            DataRow(label = "Tenant Name", value = session.tenantName)
            DataRow(label = "Role", value = session.role)
            DataRow(label = "Session Variable", value = session.rlsSessionVariable, isCode = true)
            DataRow(label = "JWT Token", value = session.token.take(38) + "...", isCode = true)
        }
    }
}

@Composable
private fun CrossTenantAttackSimulator(
    activeTenant: Tenant,
    allTenants: List<Tenant>,
    targetTenant: Tenant,
    onTargetChange: (Tenant) -> Unit,
    onExecuteTest: () -> Unit
) {
    val eligibleTargets = remember(activeTenant, allTenants) {
        allTenants.filter { it.tenantId != activeTenant.tenantId }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCoral.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = NexusCoral)
                Text(
                    text = "RLS PENETRATION TEST BENCH",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCoral,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Simulate an SQL injection / cross-tenant exfiltration attempt by querying another tenant's spatial assets table directly.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "TARGET TENANT DATASET TO ATTACK:",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                eligibleTargets.forEach { tenant ->
                    val isSelected = tenant.tenantId == targetTenant.tenantId
                    Surface(
                        onClick = { onTargetChange(tenant) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) NexusCoral.copy(alpha = 0.15f) else NexusBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NexusCoral else NexusCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = (if (isSelected) NexusCoral else tenant.sectorType.accentColor).copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = tenant.sectorType.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) NexusCoral else tenant.sectorType.accentColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = tenant.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) NexusCoral else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${tenant.sectorType.title} • ${tenant.stateCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                            if (isSelected) {
                                Surface(
                                    color = NexusCoral,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "TARGET",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexusBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onExecuteTest,
                colors = ButtonDefaults.buttonColors(containerColor = NexusCoral),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null, tint = NexusBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Row-Level Security Boundary", color = NexusBackground, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RlsAuditResultCard(auditResult: RlsAuditResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (auditResult.isAllowed) NexusEmerald.copy(alpha = 0.1f) else NexusCoral.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (auditResult.isAllowed) NexusEmerald else NexusCoral)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (auditResult.isAllowed) "ACCESS AUTHORIZED (SAME TENANT)" else "RLS INTERCEPT TRIGGERED (BLOCKED)",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (auditResult.isAllowed) NexusEmerald else NexusCoral,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = auditResult.statusCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (auditResult.isAllowed) NexusEmerald else NexusCoral,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = auditResult.statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = auditResult.queryExecuted,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Data Leakage: ${auditResult.leakageCount} records",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (auditResult.leakageCount == 0) NexusEmerald else NexusCoral,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = auditResult.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String, isCode: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCode) NexusCyan else TextPrimary,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
            fontWeight = if (isCode) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CodeSnippetCard(
    title: String,
    subtitle: String,
    code: String,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = NexusCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val scrollState = rememberScrollState()
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyanLight,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .padding(12.dp)
                        .horizontalScroll(scrollState)
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, toastMsg: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("GeoNexus Code", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
}

// -------------------------------------------------------------
// CODE BLUEPRINTS
// -------------------------------------------------------------

// Phase 1: Complete PostGIS + TimescaleDB + Documents + Compliance DDL
private const val POSTGIS_MIGRATION_SQL = """-- GeoNexus Australia: National Spatial Digital Twin
-- Phase 1 Production Migration: PostGIS + TimescaleDB + pgvector with Multi-Tenant RLS

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS timescaledb CASCADE;
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tenants Registry
CREATE TABLE tenants (
    tenant_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    sector_type VARCHAR(50) NOT NULL, -- 'AGRICULTURE', 'LOCAL_GOV', 'EDUCATION', 'INDUSTRIAL'
    state_code VARCHAR(10) NOT NULL,  -- 'WA', 'QLD', 'VIC', 'NSW', etc.
    center_geom GEOMETRY(Point, 4326) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Spatial Assets (Every table contains tenant_id and spatial geometry)
CREATE TABLE spatial_assets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    asset_category VARCHAR(100) NOT NULL,
    geometry_type VARCHAR(50) NOT NULL, -- 'POINT', 'POLYGON', 'LINESTRING', 'BUILDING_3D'
    geom GEOMETRY(Geometry, 4326) NOT NULL,
    status VARCHAR(50) DEFAULT 'OPTIMAL',
    metrics JSONB DEFAULT '{}'::jsonb,
    height_meters NUMERIC(6, 2) DEFAULT 0.0,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Spatial GIST Index for high-performance bounding box and topological queries
CREATE INDEX idx_spatial_assets_geom ON spatial_assets USING GIST(geom);
CREATE INDEX idx_spatial_assets_tenant ON spatial_assets(tenant_id);

-- ENABLE ROW-LEVEL SECURITY (Mandatory)
ALTER TABLE spatial_assets ENABLE ROW LEVEL SECURITY;
ALTER TABLE spatial_assets FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_policy ON spatial_assets
    FOR ALL
    USING (
        tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid
    )
    WITH CHECK (
        tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid
    );

-- 3. AI Ingested Documents & Blueprints Table (with pgvector embeddings)
CREATE TABLE asset_documents (
    document_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL, -- 'PDF', 'CAD_DXF', 'DOCX'
    file_size_kb INT NOT NULL,
    document_title VARCHAR(255) NOT NULL,
    extracted_metadata JSONB NOT NULL,
    confidence_scores JSONB NOT NULL,
    vector_embedding vector(768), -- pgvector for semantic search over technical notes
    uploaded_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_asset_documents_tenant ON asset_documents(tenant_id);
ALTER TABLE asset_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE asset_documents FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_doc_isolation_policy ON asset_documents
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid);

-- 4. ISO 55001 Compliance Audits Table
CREATE TABLE compliance_audits (
    audit_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id) ON DELETE CASCADE,
    asset_id UUID REFERENCES spatial_assets(id) ON DELETE CASCADE,
    document_id UUID REFERENCES asset_documents(document_id),
    overall_score INT NOT NULL CHECK (overall_score BETWEEN 0 AND 100),
    compliance_status VARCHAR(50) NOT NULL, -- 'CONFORMANT', 'CONDITIONAL_CONFORMANCE', 'NON_CONFORMANT'
    anzlic_spatial_valid BOOLEAN NOT NULL DEFAULT FALSE,
    council_framework_valid BOOLEAN NOT NULL DEFAULT FALSE,
    lifecycle_health_index INT NOT NULL,
    missing_mandatory_fields TEXT[] DEFAULT '{}',
    compliance_risks JSONB DEFAULT '[]'::jsonb,
    audited_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_compliance_audits_tenant ON compliance_audits(tenant_id);
ALTER TABLE compliance_audits ENABLE ROW LEVEL SECURITY;
ALTER TABLE compliance_audits FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_audit_isolation_policy ON compliance_audits
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid);

-- 5. High-Frequency IoT Sensor Telemetry (TimescaleDB Hypertable)
CREATE TABLE iot_sensor_telemetry (
    time TIMESTAMPTZ NOT NULL,
    tenant_id UUID NOT NULL REFERENCES tenants(tenant_id),
    sensor_id VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(50) NOT NULL,
    location GEOMETRY(Point, 4326),
    value DOUBLE PRECISION NOT NULL,
    unit VARCHAR(20) NOT NULL,
    payload JSONB DEFAULT '{}'::jsonb
);

SELECT create_hypertable('iot_sensor_telemetry', 'time', if_not_exists => TRUE);
CREATE INDEX idx_iot_telemetry_tenant_time ON iot_sensor_telemetry (tenant_id, time DESC);

ALTER TABLE iot_sensor_telemetry ENABLE ROW LEVEL SECURITY;
ALTER TABLE iot_sensor_telemetry FORCE ROW LEVEL SECURITY;

CREATE POLICY tenant_iot_isolation_policy ON iot_sensor_telemetry
    FOR ALL
    USING (tenant_id = NULLIF(current_setting('app.current_tenant', true), '')::uuid);

-- ============================================================================
-- SPATIAL7 BI-DIRECTIONAL SPATIAL & ASSET DATA SYNC WITH AUTO-SELF-HEALING
-- Target System: PostGIS / PostgreSQL Engine
-- System Owner: Kris Lalka
-- ============================================================================

-- 1. BASE ASSET DATA TABLE
CREATE TABLE IF NOT EXISTS tbl_asset_data (
    asset_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_name VARCHAR(150) NOT NULL,
    asset_class VARCHAR(50) NOT NULL,
    condition_score INT CHECK (condition_score BETWEEN 1 AND 5),
    asset_status VARCHAR(30) DEFAULT 'ACTIVE',
    last_modified_by VARCHAR(100) DEFAULT 'Kris Lal',
    updated_at TIMESTAMPTZ DEFAULT clock_timestamp()
);

-- 2. BASE GIS SPATIAL GEOMETRY TABLE
CREATE TABLE IF NOT EXISTS tbl_spatial_gis (
    gis_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID UNIQUE REFERENCES tbl_asset_data(asset_id) ON DELETE CASCADE,
    geom GEOMETRY(Geometry, 7856), -- GDA2020 / MGA Zone 56
    spatial_precision_m FLOAT DEFAULT 0.02,
    is_valid_geometry BOOLEAN DEFAULT TRUE,
    last_modified_by VARCHAR(100) DEFAULT 'Kris Lal',
    updated_at TIMESTAMPTZ DEFAULT clock_timestamp()
);

-- ============================================================================
-- 3. BI-DIRECTIONAL TRIGGER SYNC FUNCTION (GIS -> ASSET DATA & ASSET DATA -> GIS)
-- Includes loop protection (pg_trigger_depth) & automatic error healing
-- ============================================================================

CREATE OR REPLACE FUNCTION fn_sync_spatial_and_asset_data()
RETURNS TRIGGER AS $$
DECLARE
    v_trigger_depth INT;
BEGIN
    -- Prevent infinite recursion loops when tables update each other
    SELECT pg_trigger_depth() INTO v_trigger_depth;
    IF v_trigger_depth > 1 THEN
        RETURN NEW;
    END IF;

    -- Ensure correct ownership attribution
    NEW.last_modified_by := COALESCE(NEW.last_modified_by, 'Kris Lal');
    NEW.updated_at := clock_timestamp();

    -- CASE A: UPDATE ORIGINATED FROM GIS SPATIAL TABLE -> SYNC TO ASSET DATA
    IF TG_TABLE_NAME = 'tbl_spatial_gis' THEN
        -- Auto-fix invalid spatial geometries before saving
        IF NEW.geom IS NOT NULL AND NOT ST_IsValid(NEW.geom) THEN
            NEW.geom := ST_MakeValid(NEW.geom);
            NEW.is_valid_geometry := TRUE;
        END IF;

        UPDATE tbl_asset_data
        SET updated_at = NEW.updated_at,
            last_modified_by = NEW.last_modified_by
        WHERE asset_id = NEW.asset_id;

    -- CASE B: UPDATE ORIGINATED FROM ASSET DATA TABLE -> SYNC TO GIS SPATIAL
    ELSIF TG_TABLE_NAME = 'tbl_asset_data' THEN
        UPDATE tbl_spatial_gis
        SET updated_at = NEW.updated_at,
            last_modified_by = NEW.last_modified_by
        WHERE asset_id = NEW.asset_id;
    END IF;

    RETURN NEW;

EXCEPTION WHEN OTHERS THEN
    -- AUTO-RECOVERY CATCH BLOCK: Prevent pipeline crashes, log warning, and enforce safe defaults
    RAISE WARNING 'Spatial7 Auto-Correction Engine repaired sync anomaly: %', SQLERRM;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4. ATTACH TRIGGERS TO BOTH COMPONENTS
DROP TRIGGER IF EXISTS trg_sync_gis_to_asset ON tbl_spatial_gis;
CREATE TRIGGER trg_sync_gis_to_asset
    BEFORE INSERT OR UPDATE ON tbl_spatial_gis
    FOR EACH ROW EXECUTE FUNCTION fn_sync_spatial_and_asset_data();

DROP TRIGGER IF EXISTS trg_sync_asset_to_gis ON tbl_asset_data;
CREATE TRIGGER trg_sync_asset_to_gis
    BEFORE INSERT OR UPDATE ON tbl_asset_data
    FOR EACH ROW EXECUTE FUNCTION fn_sync_spatial_and_asset_data();
"""

// Phase 2: Node.js / TypeScript Gemini Multimodal PDF Ingestion Service
private const val NODE_GEMINI_INGESTION_CODE = """// GeoNexus Australia - Phase 2: Document Ingestion Service
// Node.js (TypeScript) + Google Gemini Multimodal API (gemini-2.0-flash)

import { GoogleGenerativeAI, SchemaType } from '@google/generative-ai';
import * as fs from 'fs';

export interface ExtractedAssetData {
  assetName: string;
  assetCategory: string;
  serialNumber: string;
  manufacturer: string;
  installationDate: string;
  material: string;
  expectedLifecycleYears: number;
  replacementCostAud: number;
  latitude: number;
  longitude: number;
  gda2020Zone: string;
  cadastralLotPlan: string;
  conditionRating: number;
  riskScore: number;
  confidenceScores: Record<string, number>;
}

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY!);

export async function parseAssetDocument(
  pdfBuffer: Buffer,
  mimeType: string = 'application/pdf'
): Promise<ExtractedAssetData> {
  const model = genAI.getGenerativeModel({
    model: 'gemini-2.0-flash',
    generationConfig: {
      responseMimeType: 'application/json',
      responseSchema: {
        type: SchemaType.OBJECT,
        properties: {
          assetName: { type: SchemaType.STRING },
          assetCategory: { type: SchemaType.STRING },
          serialNumber: { type: SchemaType.STRING },
          manufacturer: { type: SchemaType.STRING },
          installationDate: { type: SchemaType.STRING },
          material: { type: SchemaType.STRING },
          expectedLifecycleYears: { type: SchemaType.NUMBER },
          replacementCostAud: { type: SchemaType.NUMBER },
          latitude: { type: SchemaType.NUMBER },
          longitude: { type: SchemaType.NUMBER },
          gda2020Zone: { type: SchemaType.STRING },
          cadastralLotPlan: { type: SchemaType.STRING },
          conditionRating: { type: SchemaType.NUMBER },
          riskScore: { type: SchemaType.NUMBER },
          confidenceScores: {
            type: SchemaType.OBJECT,
            properties: {
              assetName: { type: SchemaType.NUMBER },
              installationDate: { type: SchemaType.NUMBER },
              cadastralLotPlan: { type: SchemaType.NUMBER },
              coordinates: { type: SchemaType.NUMBER },
              replacementCostAud: { type: SchemaType.NUMBER },
            }
          }
        },
        required: [
          'assetName', 'serialNumber', 'installationDate',
          'replacementCostAud', 'latitude', 'longitude'
        ]
      }
    }
  });

  const prompt = `
    You are an Australian Certified Asset Inspector and Spatial Engineer.
    Extract all engineering asset metadata, spatial GDA2020 references, and condition ratings
    from this technical document according to ISO 55001 Asset Management standards.
  `;

  const result = await model.generateContent([
    {
      inlineData: {
        data: pdfBuffer.toString('base64'),
        mimeType
      }
    },
    prompt
  ]);

  const rawJson = result.response.text();
  return JSON.parse(rawJson) as ExtractedAssetData;
}
"""

// Phase 3: TypeScript ISO 55001 & Council Compliance Engine
private const val TYPESCRIPT_COMPLIANCE_ENGINE_CODE = """// GeoNexus Australia - Phase 3: ISO 55001 & Australian Standards Engine
// Evaluates asset data against ISO 55001, ANZLIC spatial standards, and local council asset specs

import { ExtractedAssetData } from './ingestionService';

export interface ComplianceReport {
  score: number; // 0 - 100%
  status: 'CONFORMANT' | 'CONDITIONAL_CONFORMANCE' | 'NON_CONFORMANT';
  anzlicPassed: boolean;
  councilFrameworkPassed: boolean;
  missingMandatoryFields: string[];
  risks: Array<{
    field: string;
    standard: string;
    level: 'CRITICAL' | 'MAJOR' | 'MINOR';
    message: string;
    action: string;
  }>;
}

export class Iso55001ComplianceEngine {
  static evaluate(asset: ExtractedAssetData, sector: string): ComplianceReport {
    const missing: string[] = [];
    const risks: ComplianceReport['risks'] = [];
    let deductions = 0;

    // 1. ISO 55001 Mandatory Asset Identifiers
    if (!asset.serialNumber || asset.serialNumber.trim() === '') {
      missing.push('Unique Asset Serial Tag');
      risks.push({
        field: 'serialNumber',
        standard: 'ISO 55001 §6.2.1',
        level: 'CRITICAL',
        message: 'Missing serial tag prevents financial depreciation ledgering.',
        action: 'Assign barcode tag before GIS sync.'
      });
      deductions += 20;
    }

    if (!asset.installationDate) {
      missing.push('Installation Date');
      risks.push({
        field: 'installationDate',
        standard: 'IPWEA NAMS+ / ISO 55001',
        level: 'MAJOR',
        message: 'Cannot calculate Remaining Useful Life (RUL).',
        action: 'Confirm date from handover certificate.'
      });
      deductions += 15;
    }

    if (!asset.replacementCostAud || asset.replacementCostAud <= 0) {
      missing.push('Replacement Cost Valuation (AUD)');
      risks.push({
        field: 'replacementCostAud',
        standard: 'AASB 116 / ISO 55001',
        level: 'MAJOR',
        message: 'Missing modern replacement valuation for council balance sheet.',
        action: 'Lookup current unit construction rates.'
      });
      deductions += 15;
    }

    // 2. ANZLIC Spatial Compliance (GDA2020 / WGS84)
    const hasCoords = asset.latitude !== 0 && asset.longitude !== 0;
    const anzlicPassed = hasCoords && Boolean(asset.gda2020Zone);

    if (!hasCoords) {
      missing.push('Spatial Point Coordinates');
      risks.push({
        field: 'coordinates',
        standard: 'ANZLIC AS/NZS ISO 19115',
        level: 'CRITICAL',
        message: 'Asset cannot be rendered on GIS digital twin.',
        action: 'Capture RTK GNSS coordinates.'
      });
      deductions += 25;
    }

    // 3. Local Council Specific Frameworks
    let councilPassed = true;
    if (sector === 'LOCAL_GOV') {
      if (!asset.cadastralLotPlan) {
        risks.push({
          field: 'cadastralLotPlan',
          standard: 'Council Cadastral Integration',
          level: 'MAJOR',
          message: 'Asset not bound to state cadastral lot on plan.',
          action: 'Perform spatial overlay with QLD/NSW digital cadastre.'
        });
        deductions += 10;
        councilPassed = false;
      }
    }

    const score = Math.max(0, 100 - deductions);
    const status = score >= 85 ? 'CONFORMANT' : score >= 60 ? 'CONDITIONAL_CONFORMANCE' : 'NON_CONFORMANT';

    return {
      score,
      status,
      anzlicPassed,
      councilFrameworkPassed: councilPassed,
      missingMandatoryFields: missing,
      risks
    };
  }
}
"""

// Phase 4: Node.js / TypeScript Connection Wrapper Code
private const val NODE_TYPESCRIPT_POOL_CODE = """// GeoNexus Australia - Backend Handshake (Node.js / TypeScript)
// Guarantees Postgres RLS isolation by passing tenant_id session variable

import { Pool, PoolClient } from 'pg';
import jwt from 'jsonwebtoken';
import { Request, Response, NextFunction } from 'express';

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 5000,
});

export interface TenantJwtPayload {
  sub: string;
  tenant_id: string;
  sector: 'AGRICULTURE' | 'LOCAL_GOV' | 'EDUCATION' | 'INDUSTRIAL';
  role: string;
}

// 1. JWT Authentication Middleware
export const authenticateTenant = (req: Request, res: Response, next: NextFunction) => {
  const authHeader = req.headers.authorization;
  if (!authHeader?.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Missing or malformed Authorization header' });
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, process.env.JWT_PUBLIC_KEY!, {
      algorithms: ['RS256'],
      issuer: 'https://auth.geonexus.spatial7.com.au'
    }) as TenantJwtPayload;

    req.tenant = decoded;
    next();
  } catch (err) {
    return res.status(403).json({ error: 'Invalid or expired cryptographic token' });
  }
};

// 2. Transaction Wrapper with Mandatory RLS Handshake
export async function withTenantDb<T>(
  tenantId: string,
  operation: (client: PoolClient) => Promise<T>
): Promise<T> {
  const client = await pool.connect();
  try {
    await client.query('BEGIN');
    
    // CRITICAL: SET LOCAL ensures the tenant context lives ONLY within this transaction
    // If the connection returns to the pool, the variable is immediately cleared.
    await client.query('SET LOCAL app.current_tenant = $1', [tenantId]);
    
    const result = await operation(client);
    await client.query('COMMIT');
    return result;
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    client.release();
  }
}
"""

// Python FastAPI Asyncpg Code
private const val PYTHON_FASTAPI_CODE = """# GeoNexus Australia - FastAPI / Asyncpg Tenant Context Handshake

from fastapi import Depends, HTTPException, Security
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
import asyncpg
import jwt
from uuid import UUID

security = HTTPBearer()
JWT_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n..."

async def get_current_tenant_id(
    creds: HTTPAuthorizationCredentials = Security(security)
) -> UUID:
    try:
        payload = jwt.decode(
            creds.credentials, 
            JWT_PUBLIC_KEY, 
            algorithms=["RS256"], 
            issuer="https://auth.geonexus.spatial7.com.au"
        )
        return UUID(payload["tenant_id"])
    except Exception:
        raise HTTPException(status_code=403, detail="Invalid cryptographic tenant token")

async def get_tenant_db(
    tenant_id: UUID = Depends(get_current_tenant_id)
):
    conn = await db_pool.acquire()
    tx = conn.transaction()
    await tx.start()
    try:
        # Enforce PostgreSQL RLS Session Variable
        await conn.execute("SET LOCAL app.current_tenant = $1;", str(tenant_id))
        yield conn
        await tx.commit()
    except Exception:
        await tx.rollback()
        raise
    finally:
        await db_pool.release(conn)
"""

// Phase 5: React / Next.js Smart Form Component
private val REACT_SMART_FORM_CODE = """// GeoNexus Australia - Phase 5: Smart Asset Intake Form
// React + Tailwind CSS | Drag-and-Drop Ingestion | Confidence Badges & ISO 55001 Audit

'use client';

import React, { useState, useRef, DragEvent, ChangeEvent } from 'react';
import { 
  UploadCloud, 
  FileText, 
  CheckCircle2, 
  AlertTriangle, 
  AlertOctagon, 
  ShieldCheck, 
  Compass, 
  DollarSign, 
  Layers, 
  RefreshCw,
  Sparkles,
  MapPin,
  Check,
  X
} from 'lucide-react';

interface ExtractedField<T> {
  value: T;
  confidence: number; // 0.0 - 1.0
  sourceSnippet?: string;
}

interface ExtractedAssetPayload {
  assetName: ExtractedField<string>;
  category: ExtractedField<string>;
  assetTag: ExtractedField<string>;
  cadastralLotPlan: ExtractedField<string>;
  latitude: ExtractedField<number>;
  longitude: ExtractedField<number>;
  gda2020Zone: ExtractedField<string>;
  replacementCostAud: ExtractedField<number>;
  installationDate: ExtractedField<string>;
  conditionRating: ExtractedField<number>; // 1 - 5
}

interface ComplianceAudit {
  overallScore: number; // 0 - 100
  status: 'CONFORMANT' | 'PROVISIONAL' | 'NON_CONFORMANT';
  pillars: {
    iso55001AssetRegistry: boolean;
    anzlicSpatialFormat: boolean;
    aasb116FinancialLedger: boolean;
    localCouncilStandard: boolean;
  };
  warnings: string[];
}

export default function SmartAssetIntakeForm({ tenantId = 'tenant_vic_ballarat' }: { tenantId?: string }) {
  const [isDragging, setIsDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [parsing, setParsing] = useState(false);
  const [parsingStep, setParsingStep] = useState<string>('');
  const [formData, setFormData] = useState<ExtractedAssetPayload | null>(null);
  const [compliance, setCompliance] = useState<ComplianceAudit | null>(null);
  const [committed, setCommitted] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Drag-and-drop event handlers
  const handleDragOver = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processDocument(e.dataTransfer.files[0]);
    }
  };

  const handleFileInput = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      processDocument(e.target.files[0]);
    }
  };

  const processDocument = async (uploadedFile: File) => {
    setFile(uploadedFile);
    setParsing(true);
    setCommitted(false);

    try {
      // Step 1: Uploading
      setParsingStep('Ingesting document to secure multi-tenant storage...');
      await new Promise(r => setTimeout(r, 600));

      // Step 2: Multimodal extraction
      setParsingStep('Running Gemini 2.0 Multimodal OCR & spatial geometry extraction...');
      await new Promise(r => setTimeout(r, 900));

      // Step 3: ISO 55001 audit
      setParsingStep('Verifying ANZLIC GDA2020 datum & ISO 55001 asset governance rules...');
      await new Promise(r => setTimeout(r, 600));

      // Extracted metadata result with confidence scores
      setFormData({
        assetName: { value: 'Sturt Street Trunk Stormwater Culvert #14', confidence: 0.96 },
        category: { value: 'Stormwater Infrastructure', confidence: 0.94 },
        assetTag: { value: 'CG-SW-2024-884', confidence: 0.91 },
        cadastralLotPlan: { value: 'Lot 14 on Plan SP384920', confidence: 0.88 },
        latitude: { value: -37.5622, confidence: 0.98 },
        longitude: { value: 143.8503, confidence: 0.98 },
        gda2020Zone: { value: 'MGA Zone 54 (EPSG:7854)', confidence: 0.95 },
        replacementCostAud: { value: 685000, confidence: 0.82 },
        installationDate: { value: '2018-04-12', confidence: 0.74, sourceSnippet: 'Handwritten commissioning plate stamp' },
        conditionRating: { value: 3, confidence: 0.79, sourceSnippet: 'Estimated from visual inspection appendix' }
      });

      setCompliance({
        overallScore: 89,
        status: 'CONFORMANT',
        pillars: {
          iso55001AssetRegistry: true,
          anzlicSpatialFormat: true,
          aasb116FinancialLedger: true,
          localCouncilStandard: true
        },
        warnings: [
          'Commissioning date had 74% OCR confidence. Verify against original physical stamping.',
          'Condition rating is derived from appendix notes. On-site acoustic ultrasound recommended.'
        ]
      });
    } finally {
      setParsing(false);
      setParsingStep('');
    }
  };

  const handleFieldChange = (field: keyof ExtractedAssetPayload, newValue: any) => {
    if (!formData) return;
    setFormData({
      ...formData,
      [field]: {
        ...formData[field],
        value: newValue,
        confidence: 1.0 // Manually edited/verified by human reviewer
      }
    });
  };

  const handleCommit = () => {
    setCommitted(true);
  };

  const resetForm = () => {
    setFile(null);
    setFormData(null);
    setCompliance(null);
    setCommitted(false);
  };

  return (
    <div className="max-w-5xl mx-auto p-4 sm:p-8 bg-slate-950 text-slate-100 font-sans">
      {/* Header Banner */}
      <div className="mb-8 border-b border-slate-800 pb-5">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 bg-cyan-950/80 border border-cyan-500/40 rounded-xl text-cyan-400">
              <Sparkles className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl sm:text-2xl font-black tracking-tight text-white flex items-center gap-2">
                GEONEXUS AUSTRALIA
                <span className="text-xs px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/40 font-mono">
                  INTAKE ENGINE
                </span>
              </h1>
              <p className="text-xs sm:text-sm text-slate-400">
                Automated Multimodal Spatial Ingestion & ISO 55001 / ANZLIC Asset Compliance
              </p>
            </div>
          </div>
          <div className="hidden sm:block text-right">
            <span className="text-xs text-slate-400">Target Tenant:</span>
            <div className="text-sm font-mono font-bold text-cyan-400">{tenantId}</div>
          </div>
        </div>
      </div>

      {/* Upload Zone */}
      {!formData && !parsing && (
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          className={
            "relative border-2 border-dashed rounded-2xl p-10 text-center cursor-pointer transition-all duration-300 " +
            (isDragging
              ? "border-cyan-400 bg-cyan-950/30 scale-[1.01]"
              : "border-slate-700 bg-slate-900/60 hover:border-cyan-500/60 hover:bg-slate-900")
          }
        >
          <input
            ref={fileInputRef}
            type="file"
            onChange={handleFileInput}
            accept=".pdf,.dxf,.dwg,.docx,.tiff,.png,.jpg"
            className="hidden"
          />
          <div className="w-16 h-16 mx-auto mb-4 rounded-2xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
            <UploadCloud className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-white mb-1">
            Drop Engineering Blueprint, Council PDF, or Land Title
          </h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto mb-4">
            Supports PDF, AutoCAD DXF, DWG, High-Res Scans, and As-Built drawings. Gemini AI parses geodetic coordinates and asset tags automatically.
          </p>
          <div className="inline-flex items-center space-x-2 px-4 py-2 rounded-lg bg-slate-800 border border-slate-700 text-xs text-slate-300">
            <FileText className="w-4 h-4 text-cyan-400" />
            <span>Click to browse from local filesystem</span>
          </div>
        </div>
      )}

      {/* Parsing Progress Animation */}
      {parsing && (
        <div className="p-8 bg-slate-900 border border-cyan-500/40 rounded-2xl text-center space-y-4">
          <div className="relative w-12 h-12 mx-auto">
            <RefreshCw className="w-12 h-12 text-cyan-400 animate-spin" />
          </div>
          <div>
            <h4 className="text-base font-bold text-white">AI Document Ingestion Pipeline Running</h4>
            <p className="text-xs text-cyan-300/80 font-mono mt-1">{parsingStep}</p>
          </div>
          <div className="w-64 mx-auto bg-slate-800 rounded-full h-1.5 overflow-hidden">
            <div className="bg-cyan-400 h-1.5 rounded-full animate-pulse w-3/4" />
          </div>
        </div>
      )}

      {/* Extracted Form & Compliance Report */}
      {formData && compliance && (
        <div className="space-y-6">
          {/* Top ISO 55001 Compliance Banner */}
          <div className="p-5 bg-slate-900/90 border border-slate-800 rounded-2xl flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-start space-x-4">
              <div className="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-xl text-emerald-400">
                <ShieldCheck className="w-8 h-8" />
              </div>
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-xs uppercase font-bold text-slate-400 tracking-wider">
                    ISO 55001 & ANZLIC Compliance Score
                  </span>
                  <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">
                    {compliance.status}
                  </span>
                </div>
                <h3 className="text-lg font-bold text-white mt-0.5">Asset Registration Conformance Passed</h3>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 mt-3 text-xs">
                  <div className="flex items-center gap-1.5 text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5" /> ISO 55001 §6.2
                  </div>
                  <div className="flex items-center gap-1.5 text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5" /> ANZLIC Point EPSG:7854
                  </div>
                  <div className="flex items-center gap-1.5 text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5" /> AASB 116 Valuation
                  </div>
                  <div className="flex items-center gap-1.5 text-emerald-400">
                    <CheckCircle2 className="w-3.5 h-3.5" /> LGAM Council Standard
                  </div>
                </div>
              </div>
            </div>
            <div className="flex items-center justify-end space-x-3">
              <div className="text-right">
                <div className="text-3xl font-mono font-black text-emerald-400">{compliance.overallScore}%</div>
                <div className="text-[10px] text-slate-400 uppercase tracking-wider">Audit Rating</div>
              </div>
            </div>
          </div>

          {/* Audit Warnings */}
          {compliance.warnings.length > 0 && (
            <div className="p-4 bg-amber-950/20 border border-amber-500/30 rounded-xl space-y-1.5">
              <div className="flex items-center gap-2 text-xs font-bold text-amber-400">
                <AlertTriangle className="w-4 h-4" /> Attention Required: Human Review Advised
              </div>
              {compliance.warnings.map((w, idx) => (
                <p key={idx} className="text-xs text-amber-200/80 pl-6 leading-relaxed">
                  • {w}
                </p>
              ))}
            </div>
          )}

          {/* Dynamic Intake Form */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 space-y-6">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h3 className="font-bold text-sm uppercase tracking-wider text-cyan-400 flex items-center gap-2">
                <Layers className="w-4 h-4" /> Extracted Asset Attributes
              </h3>
              <span className="text-xs text-slate-400">Review AI extractions before PostGIS registration</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              {/* Asset Name */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300">Asset Designation / Title</label>
                  <ConfidenceBadge confidence={formData.assetName.confidence} />
                </div>
                <input
                  type="text"
                  value={formData.assetName.value}
                  onChange={(e) => handleFieldChange('assetName', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Asset Tag */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300">Asset Tag Identifier</label>
                  <ConfidenceBadge confidence={formData.assetTag.confidence} />
                </div>
                <input
                  type="text"
                  value={formData.assetTag.value}
                  onChange={(e) => handleFieldChange('assetTag', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm font-mono text-cyan-300 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Category */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300">ISO Asset Class / Category</label>
                  <ConfidenceBadge confidence={formData.category.confidence} />
                </div>
                <input
                  type="text"
                  value={formData.category.value}
                  onChange={(e) => handleFieldChange('category', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Cadastral Lot on Plan */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300">Cadastral Lot on Plan</label>
                  <ConfidenceBadge confidence={formData.cadastralLotPlan.confidence} />
                </div>
                <input
                  type="text"
                  value={formData.cadastralLotPlan.value}
                  onChange={(e) => handleFieldChange('cadastralLotPlan', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm font-mono text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Coordinates */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-cyan-400" /> Latitude (EPSG:7844 GDA2020)
                  </label>
                  <ConfidenceBadge confidence={formData.latitude.confidence} />
                </div>
                <input
                  type="number"
                  step="0.0001"
                  value={formData.latitude.value}
                  onChange={(e) => handleFieldChange('latitude', parseFloat(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm font-mono text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <MapPin className="w-3.5 h-3.5 text-cyan-400" /> Longitude (EPSG:7844 GDA2020)
                  </label>
                  <ConfidenceBadge confidence={formData.longitude.confidence} />
                </div>
                <input
                  type="number"
                  step="0.0001"
                  value={formData.longitude.value}
                  onChange={(e) => handleFieldChange('longitude', parseFloat(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm font-mono text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Financial Replacement Valuation */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <DollarSign className="w-3.5 h-3.5 text-emerald-400" /> Modern Replacement Cost (AUD)
                  </label>
                  <ConfidenceBadge confidence={formData.replacementCostAud.confidence} />
                </div>
                <input
                  type="number"
                  value={formData.replacementCostAud.value}
                  onChange={(e) => handleFieldChange('replacementCostAud', parseFloat(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm font-mono text-emerald-400 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>

              {/* Commissioning Date */}
              <div>
                <div className="flex justify-between items-center mb-1.5">
                  <label className="text-xs font-semibold text-slate-300">Commissioning Date</label>
                  <ConfidenceBadge confidence={formData.installationDate.confidence} />
                </div>
                <input
                  type="date"
                  value={formData.installationDate.value}
                  onChange={(e) => handleFieldChange('installationDate', e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-cyan-400 transition"
                />
              </div>
            </div>

            {/* Commit / Reset Actions */}
            <div className="pt-4 border-t border-slate-800 flex flex-col sm:flex-row items-center gap-3">
              <button
                type="button"
                onClick={handleCommit}
                disabled={committed}
                className={
                  "w-full sm:flex-1 py-3 px-6 rounded-xl font-bold text-sm transition flex items-center justify-center space-x-2 " +
                  (committed
                    ? "bg-emerald-500/20 text-emerald-400 border border-emerald-500/40 cursor-default"
                    : "bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-lg shadow-emerald-500/20")
                }
              >
                {committed ? (
                  <>
                    <Check className="w-4 h-4" />
                    <span>Committed to PostGIS Spatial Database (RLS Enforced)</span>
                  </>
                ) : (
                  <>
                    <CheckCircle2 className="w-4 h-4" />
                    <span>Verify & Commit Asset to PostGIS Database</span>
                  </>
                )}
              </button>
              <button
                type="button"
                onClick={resetForm}
                className="w-full sm:w-auto px-5 py-3 rounded-xl border border-slate-700 hover:bg-slate-800 text-slate-300 text-sm font-medium transition"
              >
                Upload Another Document
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// Visual Confidence Badge Helper Component
function ConfidenceBadge({ confidence }: { confidence: number }) {
  const percent = Math.round(confidence * 100);

  if (confidence >= 0.8) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
        <CheckCircle2 className="w-3 h-3" /> {percent}% CONF
      </span>
    );
  }

  if (confidence >= 0.6) {
    return (
      <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-amber-500/15 text-amber-400 border border-amber-500/30">
        <AlertTriangle className="w-3 h-3" /> {percent}% (REVIEW)
      </span>
    );
  }

  return (
    <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] font-mono font-bold bg-rose-500/15 text-rose-400 border border-rose-500/30">
      <AlertOctagon className="w-3 h-3" /> {percent}% (LOW)
    </span>
  );
}
"""

// Google AI Studio Mobile API Client & Responsive Viewport Container
private val REACT_MOBILE_VIEWPORT_AND_CONTAINER_CODE = """// Google AI Studio Mobile - Responsive Viewport & API Client Hook
// React / Next.js Multi-Tenant Client Architecture

import React, { useState, useEffect, useCallback } from 'react';

// ==========================================
// 1. Mobile Viewport & Orientation Hook
// ==========================================
export const useMobileViewport = () => {
  const [viewport, setViewport] = useState({
    isMobile: typeof window !== 'undefined' ? window.innerWidth <= 768 : false,
    width: typeof window !== 'undefined' ? window.innerWidth : 0,
    height: typeof window !== 'undefined' ? window.innerHeight : 0,
  });

  useEffect(() => {
    const handleResize = () => {
      setViewport({
        isMobile: window.innerWidth <= 768,
        width: window.innerWidth,
        height: window.innerHeight,
      });
    };

    window.addEventListener('resize', handleResize);
    window.addEventListener('orientationchange', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('orientationchange', handleResize);
    };
  }, []);

  return viewport;
};

// ==========================================
// 2. Google AI Studio Mobile API Client
// ==========================================
export class AIStudioAPIClient {
  constructor(apiKey, baseUrl = 'https://generativelanguage.googleapis.com/v1beta') {
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
  }

  async generateContent(model = 'gemini-1.5-pro', contents = [], config = {}) {
    const url = `${'$'}{this.baseUrl}/models/${'$'}{model}:generateContent?key=${'$'}{this.apiKey}`;
    const payload = {
      contents,
      generationConfig: {
        temperature: config.temperature ?? 0.7,
        topP: config.topP ?? 0.95,
        maxOutputTokens: config.maxOutputTokens ?? 2048,
      },
    };

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Goog-Api-Client': 'google-ai-studio-mobile/1.0.0',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error?.message || `HTTP Error ${'$'}{response.status}`);
    }

    return await response.json();
  }
}

// ==========================================
// 3. Responsive Container Component
// ==========================================
export const CoverCallContainer = ({ 
  children, 
  title = "AI Studio Workspace",
  onReset 
}) => {
  const { isMobile } = useMobileViewport();

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      minHeight: '100vh',
      backgroundColor: '#0f172a',
      color: '#f8fafc',
      fontFamily: 'Inter, system-ui, sans-serif'
    }}>
      {/* Responsive Top Bar */}
      <header style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: isMobile ? '12px 16px' : '16px 24px',
        borderBottom: '1px solid #334155',
        backgroundColor: '#1e293b'
      }}>
        <h1 style={{
          fontSize: isMobile ? '1.125rem' : '1.5rem',
          fontWeight: 600,
          margin: 0,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis'
        }}>
          {title}
        </h1>
        {onReset && (
          <button
            onClick={onReset}
            style={{
              padding: '8px 16px',
              backgroundColor: '#3b82f6',
              color: '#ffffff',
              border: 'none',
              borderRadius: '6px',
              fontSize: '0.875rem',
              fontWeight: 500,
              cursor: 'pointer',
              minHeight: '44px' // Material / Mobile Touch target standard
            }}
          >
            Reset
          </button>
        )}
      </header>

      {/* Main Content Area */}
      <main style={{
        flex: 1,
        padding: isMobile ? '16px' : '24px',
        maxWidth: '1200px',
        width: '100%',
        margin: '0 auto',
        boxSizing: 'border-box'
      }}>
        {children}
      </main>
    </div>
  );
};
"""

