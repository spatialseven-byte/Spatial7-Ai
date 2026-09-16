package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.PerspectiveMode
import com.example.model.SectorType
import com.example.model.Tenant
import com.example.ui.ai.SpatialAiScreen
import com.example.ui.architecture.ArchitectureScreen
import com.example.ui.australia.AustraliaNationalScreen
import com.example.ui.inspections.InspectionsScreen
import com.example.ui.map.SpatialMapScreen
import com.example.ui.sectors.DynamicSectorDashboard
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoNexusMainApp(
    viewModel: GeoNexusViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTenantSheet by remember { mutableStateOf(false) }
    var aiInitialPrompt by remember { mutableStateOf("") }

    Scaffold(
        containerColor = NexusBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.clickable { viewModel.selectTab(NavigationTab.NATIONAL_TWIN) }
                    ) {
                        Surface(
                            color = NexusCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = NexusCyan,
                                modifier = Modifier.padding(6.dp).size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "SPATIAL7",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    color = NexusCyan.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "AUSTRALIA",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexusCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN)
                                    "National Sovereign Backend"
                                else
                                    "RLS: ${uiState.activeTenant.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN) NexusCyan else SectorCouncilColor,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Perspective Mode Button
                    IconButton(
                        onClick = {
                            val nextMode = if (uiState.perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN)
                                PerspectiveMode.TENANT_RLS_ISOLATED
                            else
                                PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN
                            viewModel.setPerspectiveMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = if (uiState.perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN)
                                Icons.Default.AdminPanelSettings
                            else
                                Icons.Default.Lock,
                            contentDescription = "Toggle Perspective",
                            tint = if (uiState.perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN) NexusCyan else NexusYellow,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Tenant Switcher Button
                    Surface(
                        onClick = { showTenantSheet = true },
                        color = NexusSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, uiState.activeTenant.sectorType.accentColor.copy(alpha = 0.6f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(uiState.activeTenant.sectorType.accentColor)
                            )
                            Text(
                                text = uiState.activeTenant.stateCode,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Tenant",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NexusSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NexusSurface,
                contentColor = NexusCyan
            ) {
                NavigationTab.values().forEach { tab ->
                    val selected = uiState.activeTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 9.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NexusCyan,
                            selectedTextColor = NexusCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = NexusCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Crossfade(
            targetState = uiState.activeTab,
            label = "ScreenTransition",
            modifier = Modifier.padding(paddingValues)
        ) { tab ->
            when (tab) {
                NavigationTab.NATIONAL_TWIN -> {
                    AustraliaNationalScreen(
                        uiState = uiState,
                        onSelectState = { stateCode -> viewModel.selectAustralianState(stateCode) },
                        onSelectCouncil = { councilId -> viewModel.selectAustralianCouncil(councilId) },
                        onSelectSectorTab = { sector -> viewModel.setSelectedSectorTab(sector) },
                        onSearchSchools = { query -> viewModel.setSchoolSearchQuery(query) },
                        onSwitchTenant = { tenant -> viewModel.switchTenant(tenant) },
                        onSelectTab = { destination -> viewModel.selectTab(destination) },
                        onTogglePerspective = { mode -> viewModel.setPerspectiveMode(mode) },
                        onOpenAccountDialog = { open -> viewModel.setAccountDialogVisible(open) },
                        onRegisterAccount = { name, sector, state, council, region, desc ->
                            viewModel.registerNewTenantAccount(name, sector, state, council, region, desc)
                        },
                        onCreateAsset = { asset -> viewModel.commitExtractedAsset(asset) }
                    )
                }
                NavigationTab.DASHBOARD -> {
                    DynamicSectorDashboard(
                        tenant = uiState.activeTenant,
                        assets = uiState.assets,
                        sensors = uiState.sensors,
                        risks = uiState.risks,
                        onNavigateToMap = { viewModel.selectTab(NavigationTab.MAP) },
                        onNavigateToAi = { prompt ->
                            aiInitialPrompt = prompt
                            viewModel.selectTab(NavigationTab.AI_COPILOT)
                        },
                        onNavigateToArchitecture = { viewModel.selectTab(NavigationTab.ARCHITECTURE) }
                    )
                }
                NavigationTab.MAP -> {
                    SpatialMapScreen(
                        activeTenant = uiState.activeTenant,
                        assets = uiState.assets,
                        sensors = uiState.sensors,
                        risks = uiState.risks,
                        onAssetSelected = { /* Inspected in map overlay */ },
                        onCreateAsset = { asset -> viewModel.commitExtractedAsset(asset) },
                        activeRole = uiState.activeRole
                    )
                }
                NavigationTab.AI_COPILOT -> {
                    SpatialAiScreen(
                        activeTenant = uiState.activeTenant,
                        geminiService = viewModel.geminiService,
                        initialPrompt = aiInitialPrompt,
                        onCreateTicket = { ticket -> viewModel.createTicket(ticket) },
                        activeRole = uiState.activeRole
                    )
                }
                NavigationTab.INGESTION -> {
                    com.example.ui.ingestion.DocumentIngestionScreen(
                        activeTenant = uiState.activeTenant,
                        ingestionService = viewModel.documentIngestionService,
                        onCommitAssetToGis = { asset -> viewModel.commitExtractedAsset(asset) }
                    )
                }
                NavigationTab.INSPECTIONS -> {
                    InspectionsScreen(
                        activeTenant = uiState.activeTenant,
                        tickets = uiState.tickets,
                        onCreateTicket = { ticket -> viewModel.createTicket(ticket) },
                        onToggleResolved = { id, resolved -> viewModel.toggleTicketResolved(id, resolved) }
                    )
                }
                NavigationTab.ARCHITECTURE -> {
                    ArchitectureScreen(
                        activeTenant = uiState.activeTenant,
                        allTenants = uiState.allTenants,
                        activeRole = uiState.activeRole,
                        onRoleChange = { role -> viewModel.setActiveRole(role) }
                    )
                }
            }
        }
    }

    if (showTenantSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTenantSheet = false },
            containerColor = NexusSurface,
            scrimColor = Color.Black.copy(alpha = 0.65f),
            dragHandle = { BottomSheetDefaults.DragHandle(color = NexusCyan.copy(alpha = 0.6f)) },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SWITCH ENTERPRISE TENANT (RLS)",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        color = NexusEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "RLS ISOLATED",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Each sector operates within an isolated PostgreSQL Row-Level Security boundary. Switching tenants rebinds SET LOCAL app.current_tenant context.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                HorizontalDivider(color = NexusCardBorder)

                uiState.allTenants.forEach { tenant ->
                    val isSelected = tenant.tenantId == uiState.activeTenant.tenantId
                    Surface(
                        onClick = {
                            showTenantSheet = false
                            viewModel.switchTenant(tenant)
                        },
                        color = if (isSelected) tenant.sectorType.accentColor.copy(alpha = 0.15f) else NexusBackground,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) tenant.sectorType.accentColor else NexusCardBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = tenant.sectorType.accentColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = tenant.sectorType.icon,
                                        contentDescription = null,
                                        tint = tenant.sectorType.accentColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = tenant.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isSelected) tenant.sectorType.accentColor else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = NexusSurfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tenant.stateCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Text(
                                    text = "${tenant.sectorType.title} • ${tenant.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = tenant.sectorType.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        showTenantSheet = false
                        viewModel.setAccountDialogVisible(true)
                        viewModel.selectTab(NavigationTab.NATIONAL_TWIN)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusCyan, contentColor = Color.Black),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register New Entity Account (Schools, Councils, Ports)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
