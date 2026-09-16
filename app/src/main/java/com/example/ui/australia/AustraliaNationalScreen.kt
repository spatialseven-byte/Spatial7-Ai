package com.example.ui.australia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.GeoNexusUiState
import com.example.ui.NavigationTab
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AustraliaNationalScreen(
    uiState: GeoNexusUiState,
    onSelectState: (String) -> Unit,
    onSelectCouncil: (String) -> Unit,
    onSelectSectorTab: (String) -> Unit,
    onSearchSchools: (String) -> Unit,
    onSwitchTenant: (Tenant) -> Unit,
    onSelectTab: (NavigationTab) -> Unit,
    onTogglePerspective: (PerspectiveMode) -> Unit,
    onOpenAccountDialog: (Boolean) -> Unit,
    onRegisterAccount: (String, SectorType, String, String, String, String) -> Unit,
    onCreateAsset: (SpatialAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var viewingCouncilDetailId by remember { mutableStateOf<String?>(null) }

    // Check if viewing a specific council detail
    val activeCouncil = uiState.allCouncils.firstOrNull { it.id == viewingCouncilDetailId }
    if (activeCouncil != null) {
        CouncilDashboardScreen(
            council = activeCouncil,
            onBack = { viewingCouncilDetailId = null },
            onSwitchTenant = onSwitchTenant,
            onNavigateToSpatialMap = { onSelectTab(NavigationTab.MAP) },
            onNavigateToWorkOrders = { onSelectTab(NavigationTab.INSPECTIONS) },
            onCreateAsset = onCreateAsset,
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. Perspective Banner & Sovereign Mode Switch
        item {
            PerspectiveModeCard(
                perspectiveMode = uiState.perspectiveMode,
                onTogglePerspective = onTogglePerspective
            )
        }

        // 2. Persistent Top-Level Search Bar Component
        item {
            SpatialSearchBar(
                query = uiState.globalSearchQuery,
                onQueryChange = { newQuery ->
                    onSearchSchools(newQuery)
                },
                onClear = {
                    onSearchSchools("")
                    focusManager.clearFocus()
                }
            )
        }

        // 3. State & Territory Selection Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUSTRALIAN JURISDICTIONS (ABS AOI)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    val activeState = uiState.allStates.firstOrNull { it.code == uiState.selectedStateCode }
                    if (activeState != null) {
                        Text(
                            text = "${activeState.councilsCount} LGAs • ${activeState.schoolsCount} Schools",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedStateCode.isEmpty(),
                            onClick = { onSelectState("") },
                            label = { Text("ALL AUST", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexusCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NexusCyan,
                                containerColor = NexusSurface,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(1.dp, if (uiState.selectedStateCode.isEmpty()) NexusCyan else NexusCardBorder)
                        )
                    }
                    items(uiState.allStates) { state ->
                        val isSelected = uiState.selectedStateCode.equals(state.code, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectState(state.code) },
                            label = {
                                Text(
                                    text = state.code,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NexusCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NexusCyan,
                                containerColor = NexusSurface,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(1.dp, if (isSelected) NexusCyan else NexusCardBorder)
                        )
                    }
                }
            }
        }

        // 4. Open-Source Data Source Hub Card for Selected State
        item {
            val currentState = uiState.allStates.firstOrNull { it.code.equals(uiState.selectedStateCode, ignoreCase = true) }
                ?: uiState.allStates.first()
            StateSpatialPortalCard(state = currentState)
        }

        // 5. Sector Categories Tab Bar (Councils, Schools, Universities, Aged Care, Industrial)
        item {
            val sectorTabs = listOf("Councils", "Schools", "Universities", "Aged Care", "Industrial")
            ScrollableTabRow(
                selectedTabIndex = sectorTabs.indexOf(uiState.selectedSectorTab).coerceAtLeast(0),
                containerColor = NexusSurface,
                contentColor = NexusCyan,
                edgePadding = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, NexusCardBorder, RoundedCornerShape(12.dp))
            ) {
                sectorTabs.forEach { tab ->
                    val isSelected = uiState.selectedSectorTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectSectorTab(tab) },
                        text = {
                            Text(
                                text = tab,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NexusCyan else TextSecondary
                            )
                        }
                    )
                }
            }
        }

        // 6. Filtered Entity List
        when (uiState.selectedSectorTab) {
            "Councils" -> {
                item {
                    ListHeader(
                        title = "LOCAL GOVERNMENT COUNCILS (LGAs)",
                        count = uiState.filteredCouncils.size,
                        hint = "Click to inspect municipal digital twin, flood assets & roads"
                    )
                }
                items(uiState.filteredCouncils) { council ->
                    CouncilCard(
                        council = council,
                        onOpenDashboard = { viewingCouncilDetailId = council.id },
                        onLoadIntoMap = {
                            val tenant = Tenant(
                                tenantId = "tenant-${council.id}",
                                name = council.name,
                                sectorType = SectorType.LOCAL_GOV,
                                stateCode = council.stateCode,
                                councilLga = council.name,
                                region = council.region,
                                centerLat = council.centerLat,
                                centerLng = council.centerLng,
                                defaultZoom = 13.5f,
                                description = council.primaryFocus,
                                activeAssetsCount = council.municipalAssetsCount,
                                activeSensorsCount = 380,
                                iotStreamRate = "2,400 msgs/min"
                            )
                            onSwitchTenant(tenant)
                            onSelectTab(NavigationTab.MAP)
                        }
                    )
                }
            }
            "Schools" -> {
                item {
                    ListHeader(
                        title = "ACARA REGISTERED EDUCATIONAL ASSETS",
                        count = uiState.filteredSchools.size,
                        hint = "Campus boundary polygons, student enrollments & bushfire ratings"
                    )
                }
                items(uiState.filteredSchools) { school ->
                    SchoolCard(school = school)
                }
            }
            "Universities" -> {
                item {
                    ListHeader(
                        title = "TERTIARY CAMPUSES & DIGITAL TWINS",
                        count = uiState.filteredUniversities.size,
                        hint = "BIM/CAD models, HVAC telemetry & 3D building floorplans"
                    )
                }
                items(uiState.filteredUniversities) { uni ->
                    UniversityCard(university = uni)
                }
            }
            "Aged Care" -> {
                item {
                    ListHeader(
                        title = "AGED CARE & RETIREMENT LIVING FACILITIES",
                        count = uiState.filteredAgedCare.size,
                        hint = "Evacuation zoning, emergency generator telemetry & vulnerable resident registries"
                    )
                }
                items(uiState.filteredAgedCare) { care ->
                    AgedCareCard(agedCare = care)
                }
            }
            "Industrial" -> {
                item {
                    ListHeader(
                        title = "MAJOR HAZARD & COMMERCIAL INDUSTRIAL ESTATES",
                        count = uiState.filteredIndustrial.size,
                        hint = "Dangerous goods setbacks, container logistics & port infrastructure"
                    )
                }
                items(uiState.filteredIndustrial) { ind ->
                    IndustrialCard(industrial = ind)
                }
            }
        }

        // 7. Onboarding & Registration CTA
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NexusCyan.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NexusCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = NexusCyan)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Register Your Council, Campus or Facility",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Spin up an isolated PostGIS RLS tenant with GDA2020 projection & custom ISO 55001 workflows.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { onOpenAccountDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add Entity", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Account Creation Dialog
    if (uiState.isAccountDialogVisible) {
        RegisterTenantDialog(
            onDismiss = { onOpenAccountDialog(false) },
            onRegister = onRegisterAccount
        )
    }
}

@Composable
fun SpatialSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NexusSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (query.isNotEmpty()) NexusCyan else NexusCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (query.isNotEmpty()) NexusCyan else TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search Councils, Schools, Unis, Aged Care, Ports...",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("spatial_search_input")
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PerspectiveModeCard(
    perspectiveMode: PerspectiveMode,
    onTogglePerspective: (PerspectiveMode) -> Unit
) {
    val isNational = perspectiveMode == PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isNational) NexusSurfaceVariant else NexusSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isNational) NexusCyan.copy(alpha = 0.5f) else SectorCouncilColor.copy(alpha = 0.5f)
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
                color = if (isNational) NexusCyan.copy(alpha = 0.15f) else SectorCouncilColor.copy(alpha = 0.15f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isNational) Icons.Default.Public else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isNational) NexusCyan else SectorCouncilColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isNational) "NATIONAL SOVEREIGN PERSPECTIVE" else "TENANT RLS ISOLATED PERSPECTIVE",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isNational) NexusCyan else SectorCouncilColor,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = if (isNational) NexusCyan.copy(alpha = 0.2f) else NexusEmerald.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isNational) "AGGREGATE" else "ISOLATED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isNational) NexusCyan else NexusEmerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            fontSize = 9.sp
                        )
                    }
                }
                Text(
                    text = if (isNational)
                        "Cross-jurisdiction ABS LGA boundaries, open-data feeds & national infrastructure."
                    else
                        "Row-Level Security is actively restricting spatial queries strictly to your tenant.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = isNational,
                onCheckedChange = { checked ->
                    onTogglePerspective(
                        if (checked) PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN else PerspectiveMode.TENANT_RLS_ISOLATED
                    )
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = NexusCyan,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = NexusSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun StateSpatialPortalCard(state: AustralianState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "LIVE OPEN DATA INTEGRATION: ${state.name.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = NexusEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "GDA2020 / EPSG:7844",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "${state.openDataPortalName} • Automated spatial harvesting from ABS ASGS 2021 boundaries, state cadastral parcels, and G-NAF geocoding.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatPill(label = "LGAs", value = "${state.councilsCount}")
                StatPill(label = "Schools", value = "${state.schoolsCount}")
                StatPill(label = "Unis", value = "${state.universitiesCount}")
                StatPill(label = "Aged Care", value = "${state.agedCareCount}")
                StatPill(label = "Industrial", value = "${state.industrialEstatesCount}")
            }
        }
    }
}

@Composable
fun CouncilCard(
    council: AustralianCouncil,
    onOpenDashboard: () -> Unit,
    onLoadIntoMap: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDashboard() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = council.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = SectorCouncilColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = council.stateCode,
                            style = MaterialTheme.typography.labelSmall,
                            color = SectorCouncilColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                IconButton(
                    onClick = onLoadIntoMap,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Load in Map",
                        tint = NexusCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = council.primaryFocus,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )

            HorizontalDivider(color = NexusCardBorder.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("REPLACEMENT VALUE", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text(council.replacementValueAud, style = MaterialTheme.typography.labelMedium, color = NexusCyan, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("POPULATION", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("%,d".format(council.population), style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Column {
                        Text("ROAD NETWORK", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
                        Text("${council.roadLengthKm.toInt()} km", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.clickable { onOpenDashboard() }
                ) {
                    Text(
                        text = "Inspect",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = NexusCyan,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SchoolCard(school: AustralianSchool) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = school.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SectorEduColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${school.stateCode} • ACARA ${school.acaraId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SectorEduColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "${school.suburb} • ${school.sector} ${school.level} Campus",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Enrollment: %,d students".format(school.studentEnrollment), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                Text("Campus: ${school.campusAreaHectares} ha", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun UniversityCard(university: AustralianUniversity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${university.name} (${university.shortName})",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SectorEduColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = university.stateCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = SectorEduColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${university.campus} • ${university.buildingCount} Buildings • Floor Area ${university.grossFloorAreaM2}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Student Population: %,d".format(university.studentPopulation), style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                Text("HVAC & Sub-metering Connected", style = MaterialTheme.typography.labelSmall, color = NexusEmerald)
            }
        }
    }
}

@Composable
fun AgedCareCard(agedCare: AustralianAgedCare) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = agedCare.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = NexusEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (agedCare.emergencyPlanCertified) "EMERGENCY READY" else "AUDIT PENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 9.sp
                    )
                }
            }

            Text(
                text = "Provider: ${agedCare.provider} • ${agedCare.suburb}, ${agedCare.stateCode}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${agedCare.bedCount} Approved Places", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                Text("Bushfire BAL-29 Evacuation Route Verified", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun IndustrialCard(industrial: AustralianIndustrial) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NexusCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = industrial.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = SectorIndusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = industrial.stateCode,
                        style = MaterialTheme.typography.labelSmall,
                        color = SectorIndusColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${industrial.estateType} • ${industrial.areaHectares} ha • Hazard: ${industrial.hazardClassification}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Text(
                text = "Key Tenants: ${industrial.majorTenants.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ListHeader(title: String, count: Int, hint: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Surface(
                color = NexusSurfaceVariant,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "$count MATCHING",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp
                )
            }
        }
        Text(text = hint, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
fun StatPill(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
        Text(text = value, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterTenantDialog(
    onDismiss: () -> Unit,
    onRegister: (String, SectorType, String, String, String, String) -> Unit
) {
    var entityName by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf(SectorType.LOCAL_GOV) }
    var selectedState by remember { mutableStateOf("QLD") }
    var councilLga by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Onboard Entity / Campus to Spatial7",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configures a dedicated PostgreSQL Row-Level Security tenant with automated GDA2020 spatial indexing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = entityName,
                    onValueChange = { entityName = it },
                    label = { Text("Entity Name (e.g. Moreton Bay City Council)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NexusCyan,
                        unfocusedBorderColor = NexusCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = selectedState,
                        onValueChange = { selectedState = it.uppercase().take(3) },
                        label = { Text("State (e.g. QLD)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NexusCyan,
                            unfocusedBorderColor = NexusCardBorder
                        )
                    )
                    OutlinedTextField(
                        value = councilLga,
                        onValueChange = { councilLga = it },
                        label = { Text("LGA / Suburb") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NexusCyan,
                            unfocusedBorderColor = NexusCardBorder
                        )
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Primary Focus / Objective") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NexusCyan,
                        unfocusedBorderColor = NexusCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (entityName.isNotBlank()) {
                        onRegister(
                            entityName,
                            selectedSector,
                            selectedState,
                            councilLga,
                            region.ifBlank { "Greater Metropolitan" },
                            description.ifBlank { "Asset & Spatial GIS Management" }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexusCyan, contentColor = Color.Black),
                enabled = entityName.isNotBlank()
            ) {
                Text("Provision Tenant RLS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = NexusSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
