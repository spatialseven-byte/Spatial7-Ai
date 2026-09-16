package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.*
import java.util.UUID
import kotlin.math.*
import kotlin.random.Random

// Color Palette strictly matching IPWEA & ISO 55001 Prototype Design
private val BgDark = Color(0xFF070F1E)
private val CardBg = Color(0xFF0D1B2A)
private val AccentCyan = Color(0xFF00F2FE)
private val AccentTeal = Color(0xFF4FACFE)
private val BorderColor = Color(0xFF1E3A5F)
private val BtnNormal = Color(0xFF14243B)
private val CoordBoxBg = Color(0xFF081220)
private val ItemCardBg = Color(0xFF101F33)
private val TextMain = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8A9BB0)

enum class AssetDialogTab(val title: String) {
    CAPTURE("📍 Asset Capture"),
    INVENTORY("📂 ISO Inventory Register")
}

enum class BasemapType(val label: String) {
    GOOGLE_HYBRID("Google Hybrid"),
    GOOGLE_SATELLITE("Google Satellite"),
    GOOGLE_ROADMAP("Google Roadmap")
}

data class IpweaGridOption(
    val code: String,
    val lbl: String,
    val name: String,
    val lifeYears: Int
)

data class IsoAssetRecord(
    val id: String,
    val name: String,
    val domain: String,
    val assetClass: String,
    val geometry: String,
    val condition: String,
    val hierarchy: String,
    val criticality: String,
    val lifecycleStage: String,
    val replacementValue: String,
    val targetLOS: String,
    val coords: String
)

val DefaultIpweaOptions = listOf(
    IpweaGridOption("TR_RD", "Roads", "Roads & Sealed Pavement", 30),
    IpweaGridOption("SW_PIPE", "SW Pipe", "Stormwater Pipe", 80),
    IpweaGridOption("SW_KERB", "Kerb/Chan", "Kerb & Channel", 50),
    IpweaGridOption("TR_PATH", "Footpath", "Footpath & Cycleways", 40),
    IpweaGridOption("PK_OPEN", "Parks", "Parks & Open Spaces", 25),
    IpweaGridOption("BD_FAC", "Buildings", "Buildings & Structures", 50),
    IpweaGridOption("WW_NET", "Wastewater", "Wastewater Network", 70),
    IpweaGridOption("CM_IND", "Comm/Ind", "Commercial / Facility", 45)
)

val DomainOptions = listOf(
    "Local Government / Council",
    "Commercial Infrastructure",
    "Industrial Facilities",
    "Educational Campus"
)

val InitialIsoInventory = listOf(
    IsoAssetRecord(
        id = "AST-LG-001",
        name = "Main Arterial Sealed Pavement Sec 4",
        domain = "Local Government / Council",
        assetClass = "Roads & Sealed Pavement",
        geometry = "Polyline",
        condition = "2 (Minor)",
        hierarchy = "Infrastructure > Transportation > Carriageway",
        criticality = "High (Class A)",
        lifecycleStage = "Operational",
        replacementValue = "$450,000 AUD",
        targetLOS = "ISO 55001 Service Level B",
        coords = "Lat: -18.1960, Lng: 125.5680"
    ),
    IsoAssetRecord(
        id = "AST-SW-042",
        name = "Reinforced Concrete Stormwater Pipe DN600",
        domain = "Local Government / Council",
        assetClass = "Stormwater Pipe",
        geometry = "Polyline",
        condition = "1 (Good)",
        hierarchy = "Stormwater Network > Underground Mains",
        criticality = "Medium",
        lifecycleStage = "Operational",
        replacementValue = "$120,000 AUD",
        targetLOS = "1-in-20 Year Flood Capacity",
        coords = "Lat: -18.1972, Lng: 125.5691"
    ),
    IsoAssetRecord(
        id = "AST-BD-109",
        name = "Administration Building Block A",
        domain = "Educational Campus",
        assetClass = "Buildings & Structures",
        geometry = "Polygon",
        condition = "3 (Fair)",
        hierarchy = "Facilities > Educational Complex > Main Admin",
        criticality = "High",
        lifecycleStage = "Operational",
        replacementValue = "$2,800,000 AUD",
        targetLOS = "ISO 55002 Compliant Facility",
        coords = "Lat: -18.1955, Lng: 125.5670"
    )
)

fun SpatialAsset.toIsoAssetRecord(tenant: Tenant): IsoAssetRecord {
    val domain = when (sectorType) {
        SectorType.LOCAL_GOV -> "Local Government / Council"
        SectorType.EDUCATION -> "Educational Campus"
        SectorType.INDUSTRIAL -> "Industrial Facilities"
        SectorType.AGRICULTURE -> "Commercial Infrastructure"
    }
    val conditionDesc = when (status) {
        AssetStatus.OPTIMAL -> "1 (Good)"
        AssetStatus.WARNING -> "3 (Fair)"
        AssetStatus.CRITICAL -> "5 (Critical)"
        AssetStatus.MAINTENANCE -> "2 (In Service)"
    }
    val geomDesc = when (geometryType) {
        AssetGeometryType.POINT -> "Point"
        AssetGeometryType.LINESTRING -> "Polyline"
        AssetGeometryType.POLYGON -> "Polygon"
        AssetGeometryType.BUILDING_3D -> "3D Building"
    }
    val repValue = metrics["Valuation"] ?: metrics["Replacement Cost"] ?: metrics["calculated_replacement_value"] ?: "$320,000 AUD"
    val los = metrics["iso_standard"] ?: "ISO 55001 Level B"
    val hierarchy = "$domain > ${tenant.name} > $assetCategory"
    val coordsStr = "Lat: ${String.format("%.4f", primaryLocation.lat)}, Lng: ${String.format("%.4f", primaryLocation.lng)}"

    return IsoAssetRecord(
        id = id,
        name = name,
        domain = domain,
        assetClass = assetCategory,
        geometry = geomDesc,
        condition = conditionDesc,
        hierarchy = hierarchy,
        criticality = if (status == AssetStatus.CRITICAL) "Critical (Class A)" else "Medium (ISO 55001 Standard)",
        lifecycleStage = "Operational",
        replacementValue = repValue,
        targetLOS = los,
        coords = coordsStr
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpatialAssetCreationDialog(
    activeTenant: Tenant,
    initialGeometryType: AssetGeometryType = AssetGeometryType.POINT,
    initialTab: AssetDialogTab = AssetDialogTab.CAPTURE,
    existingAssets: List<SpatialAsset> = emptyList(),
    onDismiss: () -> Unit,
    onSaveAsset: (SpatialAsset) -> Unit
) {
    // Current Active Tab (Asset Capture vs ISO Inventory Register)
    var activeTab by remember { mutableStateOf(initialTab) }

    // Tab 1: Form States
    var geometryType by remember { mutableStateOf(initialGeometryType) }
    var selectedDomain by remember { mutableStateOf(DomainOptions[0]) }
    var assetName by remember { mutableStateOf("Main Street Bus Stop") }
    var selectedIpweaOption by remember { mutableStateOf(DefaultIpweaOptions[0]) }
    var conditionScore by remember { mutableIntStateOf(1) } // 1: Good, 2: Minor, 3: Fair, 4: Poor, 5: Critical
    var selectedBasemap by remember { mutableStateOf(BasemapType.GOOGLE_HYBRID) }

    // Coordinates state
    val defaultLat = if (activeTenant.centerLat != 0.0) activeTenant.centerLat else -18.19600
    val defaultLng = if (activeTenant.centerLng != 0.0) activeTenant.centerLng else 125.56800

    var latInputText by remember { mutableStateOf(String.format("%.5f", defaultLat)) }
    var lngInputText by remember { mutableStateOf(String.format("%.5f", defaultLng)) }

    var loggedCoordinates by remember {
        val initialList = when (initialGeometryType) {
            AssetGeometryType.POINT -> listOf(LatLngCoord(defaultLat, defaultLng))
            AssetGeometryType.LINESTRING -> listOf(
                LatLngCoord(defaultLat - 0.0015, defaultLng - 0.0015),
                LatLngCoord(defaultLat + 0.0015, defaultLng + 0.0015)
            )
            AssetGeometryType.POLYGON -> listOf(
                LatLngCoord(defaultLat - 0.002, defaultLng - 0.002),
                LatLngCoord(defaultLat + 0.002, defaultLng - 0.002),
                LatLngCoord(defaultLat + 0.002, defaultLng + 0.002),
                LatLngCoord(defaultLat - 0.002, defaultLng + 0.002)
            )
            else -> listOf(LatLngCoord(defaultLat, defaultLng))
        }
        mutableStateOf(initialList)
    }

    // Calculated Area & Length
    val calculatedAreaHectares = remember(loggedCoordinates, geometryType) {
        if (geometryType == AssetGeometryType.POLYGON && loggedCoordinates.size >= 3) {
            calculatePolygonAreaHectares(loggedCoordinates)
        } else 0.0
    }

    val calculatedLengthMeters = remember(loggedCoordinates, geometryType) {
        if ((geometryType == AssetGeometryType.LINESTRING || geometryType == AssetGeometryType.POLYGON) && loggedCoordinates.size >= 2) {
            calculatePolylineLengthMeters(loggedCoordinates)
        } else 0.0
    }

    // Tab 2: Inventory state & filters
    val baseInventoryRecords = remember(existingAssets, activeTenant.tenantId) {
        val converted = existingAssets.map { it.toIsoAssetRecord(activeTenant) }
        converted + InitialIsoInventory
    }

    var inventoryList by remember(baseInventoryRecords) {
        mutableStateOf(baseInventoryRecords)
    }

    var domainFilter by remember { mutableStateOf("ALL") }
    var classFilter by remember { mutableStateOf("ALL") }
    var successBannerMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .fillMaxHeight(0.95f)
                    .testTag("spatial_asset_register_dialog"),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // -------------------------------------------------------------
                    // TOP BAR: CLOSE BUTTON & APP-LEVEL NOTIFICATION
                    // -------------------------------------------------------------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SPATIAL ASSET PLATFORM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentTeal,
                            letterSpacing = 1.sp
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // -------------------------------------------------------------
                    // DUAL TAB NAVIGATION (Asset Capture & ISO Inventory Register)
                    // -------------------------------------------------------------
                    Surface(
                        color = CoordBoxBg,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AssetDialogTab.values().forEach { tab ->
                                val isTabActive = activeTab == tab
                                Surface(
                                    onClick = {
                                        activeTab = tab
                                        successBannerMessage = null
                                    },
                                    color = if (isTabActive) AccentCyan else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = tab.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTabActive) Color.Black else TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Success Banner if recently registered
                    successBannerMessage?.let { banner ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF0F382A),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Success",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = banner,
                                    fontSize = 11.sp,
                                    color = Color(0xFFD1FAE5),
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // -------------------------------------------------------------
                    // CONTENT BASED ON ACTIVE TAB
                    // -------------------------------------------------------------
                    when (activeTab) {
                        AssetDialogTab.CAPTURE -> {
                            CaptureTabContent(
                                geometryType = geometryType,
                                onGeometryTypeChange = {
                                    geometryType = it
                                    if (it == AssetGeometryType.POINT && loggedCoordinates.size > 1) {
                                        loggedCoordinates = listOf(loggedCoordinates.first())
                                    } else if (it == AssetGeometryType.LINESTRING && loggedCoordinates.size < 2) {
                                        loggedCoordinates = listOf(
                                            LatLngCoord(defaultLat - 0.0015, defaultLng - 0.0015),
                                            LatLngCoord(defaultLat + 0.0015, defaultLng + 0.0015)
                                        )
                                    } else if (it == AssetGeometryType.POLYGON && loggedCoordinates.size < 3) {
                                        loggedCoordinates = listOf(
                                            LatLngCoord(defaultLat - 0.002, defaultLng - 0.002),
                                            LatLngCoord(defaultLat + 0.002, defaultLng - 0.002),
                                            LatLngCoord(defaultLat + 0.002, defaultLng + 0.002),
                                            LatLngCoord(defaultLat - 0.002, defaultLng + 0.002)
                                        )
                                    }
                                },
                                selectedDomain = selectedDomain,
                                onDomainChange = { selectedDomain = it },
                                assetName = assetName,
                                onAssetNameChange = { assetName = it },
                                selectedIpweaOption = selectedIpweaOption,
                                onIpweaOptionChange = { selectedIpweaOption = it },
                                conditionScore = conditionScore,
                                onConditionScoreChange = { conditionScore = it },
                                selectedBasemap = selectedBasemap,
                                onBasemapChange = { selectedBasemap = it },
                                defaultLat = defaultLat,
                                defaultLng = defaultLng,
                                latInputText = latInputText,
                                onLatInputChange = { latInputText = it },
                                lngInputText = lngInputText,
                                onLngInputChange = { lngInputText = it },
                                loggedCoordinates = loggedCoordinates,
                                onLoggedCoordinatesChange = { loggedCoordinates = it },
                                calculatedAreaHectares = calculatedAreaHectares,
                                calculatedLengthMeters = calculatedLengthMeters,
                                onCancel = onDismiss,
                                onRegisterAsset = {
                                    val finalName = if (assetName.isNotBlank()) assetName.trim() else "Asset-${selectedIpweaOption.code}"
                                    val primaryLoc = loggedCoordinates.firstOrNull() ?: LatLngCoord(defaultLat, defaultLng)
                                    val newId = "AST-EXP-${Random.nextInt(1000, 9999)}"

                                    val status = when (conditionScore) {
                                        1, 2 -> AssetStatus.OPTIMAL
                                        3 -> AssetStatus.WARNING
                                        else -> AssetStatus.CRITICAL
                                    }

                                    val newSpatialAsset = SpatialAsset(
                                        id = newId,
                                        tenantId = activeTenant.tenantId,
                                        name = finalName,
                                        sectorType = activeTenant.sectorType,
                                        assetCategory = selectedIpweaOption.name,
                                        geometryType = geometryType,
                                        primaryLocation = primaryLoc,
                                        polygonBounds = if (geometryType == AssetGeometryType.POINT) emptyList() else loggedCoordinates,
                                        status = status,
                                        metrics = mapOf(
                                            "domain" to selectedDomain,
                                            "ipwea_code" to selectedIpweaOption.code,
                                            "ipwea_class" to selectedIpweaOption.name,
                                            "condition_grade" to "$conditionScore / 5",
                                            "useful_life" to "${selectedIpweaOption.lifeYears} yrs",
                                            "calculated_area_ha" to String.format("%.3f", calculatedAreaHectares),
                                            "calculated_length_m" to String.format("%.1f", calculatedLengthMeters),
                                            "datum" to "GDA2020 / EPSG:7844",
                                            "iso_standard" to "ISO 55001"
                                        ),
                                        lastInspected = "Today (ISO 55001 Registered)",
                                        areaHectares = calculatedAreaHectares
                                    )

                                    // Create corresponding ISO Record for ledger
                                    val newIsoRecord = IsoAssetRecord(
                                        id = newId,
                                        name = finalName,
                                        domain = selectedDomain,
                                        assetClass = selectedIpweaOption.name,
                                        geometry = geometryType.name.lowercase().replaceFirstChar { it.uppercase() },
                                        condition = "$conditionScore (${when(conditionScore){1->"Good";2->"Minor";3->"Fair";4->"Poor";else->"Critical"}})",
                                        hierarchy = "$selectedDomain > Infrastructure > ${selectedIpweaOption.name}",
                                        criticality = if (conditionScore >= 4) "Critical (Class A)" else "Medium/High (ISO 55001 Evaluated)",
                                        lifecycleStage = "Operational",
                                        replacementValue = "Calculated via Useful Life (${selectedIpweaOption.lifeYears} Yrs)",
                                        targetLOS = "ISO 55001 Aligned Baseline",
                                        coords = if (loggedCoordinates.isNotEmpty()) {
                                            "Lat: ${String.format("%.4f", loggedCoordinates.first().lat)}, Lng: ${String.format("%.4f", loggedCoordinates.first().lng)}"
                                        } else {
                                            "Manual Entry"
                                        }
                                    )

                                    inventoryList = listOf(newIsoRecord) + inventoryList
                                    onSaveAsset(newSpatialAsset)
                                    successBannerMessage = "Asset Registered Successfully under ISO 55001 Protocol!\nAsset ID: $newId"
                                    activeTab = AssetDialogTab.INVENTORY
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AssetDialogTab.INVENTORY -> {
                            InventoryTabContent(
                                inventoryList = inventoryList,
                                domainFilter = domainFilter,
                                onDomainFilterChange = { domainFilter = it },
                                classFilter = classFilter,
                                onClassFilterChange = { classFilter = it },
                                onSwitchToCapture = {
                                    activeTab = AssetDialogTab.CAPTURE
                                    successBannerMessage = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: CAPTURE SPATIAL ASSET COMPONENT
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaptureTabContent(
    geometryType: AssetGeometryType,
    onGeometryTypeChange: (AssetGeometryType) -> Unit,
    selectedDomain: String,
    onDomainChange: (String) -> Unit,
    assetName: String,
    onAssetNameChange: (String) -> Unit,
    selectedIpweaOption: IpweaGridOption,
    onIpweaOptionChange: (IpweaGridOption) -> Unit,
    conditionScore: Int,
    onConditionScoreChange: (Int) -> Unit,
    selectedBasemap: BasemapType,
    onBasemapChange: (BasemapType) -> Unit,
    defaultLat: Double,
    defaultLng: Double,
    latInputText: String,
    onLatInputChange: (String) -> Unit,
    lngInputText: String,
    onLngInputChange: (String) -> Unit,
    loggedCoordinates: List<LatLngCoord>,
    onLoggedCoordinatesChange: (List<LatLngCoord>) -> Unit,
    calculatedAreaHectares: Double,
    calculatedLengthMeters: Double,
    onCancel: () -> Unit,
    onRegisterAsset: () -> Unit,
    modifier: Modifier = Modifier
) {
    var domainExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Info
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Create Spatial Asset",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "IPWEA IIMM & Council Asset Standards • GDA2020 / EPSG:7844",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // -------------------------------------------------------------
            // 1. SELECT ASSET GEOMETRY TYPE
            // -------------------------------------------------------------
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. SELECT ASSET GEOMETRY TYPE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GeometryOptionBtn(
                            title = "Point",
                            subtitle = "Node / Pit / Tree",
                            isActive = geometryType == AssetGeometryType.POINT,
                            onClick = { onGeometryTypeChange(AssetGeometryType.POINT) },
                            modifier = Modifier.weight(1f)
                        )
                        GeometryOptionBtn(
                            title = "Polyline",
                            subtitle = "Road / Pipe / Path",
                            isActive = geometryType == AssetGeometryType.LINESTRING,
                            onClick = { onGeometryTypeChange(AssetGeometryType.LINESTRING) },
                            modifier = Modifier.weight(1f)
                        )
                        GeometryOptionBtn(
                            title = "Polygon",
                            subtitle = "Parcel / Park / Basin",
                            isActive = geometryType == AssetGeometryType.POLYGON,
                            onClick = { onGeometryTypeChange(AssetGeometryType.POLYGON) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // -------------------------------------------------------------
            // 2. ASSET DOMAIN & COMPREHENSIVE ASSET CLASS
            // -------------------------------------------------------------
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. ASSET DOMAIN & COMPREHENSIVE ASSET CLASS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Row: Domain Selector & Asset Name Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Domain dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                onClick = { domainExpanded = true },
                                color = BtnNormal,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedDomain,
                                        fontSize = 11.sp,
                                        color = TextMain,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand Domain",
                                        tint = AccentTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = domainExpanded,
                                onDismissRequest = { domainExpanded = false },
                                modifier = Modifier.background(CardBg)
                            ) {
                                DomainOptions.forEach { dom ->
                                    DropdownMenuItem(
                                        text = { Text(dom, color = TextMain, fontSize = 12.sp) },
                                        onClick = {
                                            onDomainChange(dom)
                                            domainExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Asset Name Text Field
                        OutlinedTextField(
                            value = assetName,
                            onValueChange = onAssetNameChange,
                            placeholder = { Text("Asset Name / Tag", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("spatial_asset_name_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BtnNormal,
                                unfocusedContainerColor = BtnNormal,
                                focusedBorderColor = AccentCyan,
                                unfocusedBorderColor = BorderColor,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 8-Button IPWEA Grid (2 rows of 4)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val row1 = DefaultIpweaOptions.take(4)
                        val row2 = DefaultIpweaOptions.drop(4)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row1.forEach { item ->
                                val isSelected = selectedIpweaOption == item
                                Surface(
                                    onClick = { onIpweaOptionChange(item) },
                                    color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else BtnNormal,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentCyan else BorderColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.lbl,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentCyan else TextMain
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row2.forEach { item ->
                                val isSelected = selectedIpweaOption == item
                                Surface(
                                    onClick = { onIpweaOptionChange(item) },
                                    color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else BtnNormal,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) AccentCyan else BorderColor
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.lbl,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) AccentCyan else TextMain
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Selected IPWEA info label
                    Text(
                        text = "Selected: ${selectedIpweaOption.name} (Useful Life: ${selectedIpweaOption.lifeYears} yrs)",
                        fontSize = 11.sp,
                        color = AccentTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // -------------------------------------------------------------
            // 3. CONDITION ASSESSMENT (IPWEA 1-5 SCALE)
            // -------------------------------------------------------------
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "3. CONDITION ASSESSMENT (IPWEA 1-5 SCALE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val conditions = listOf(
                        Pair(1, "Good"),
                        Pair(2, "Minor"),
                        Pair(3, "Fair"),
                        Pair(4, "Poor"),
                        Pair(5, "Critical")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        conditions.forEach { (score, label) ->
                            val isSelected = conditionScore == score
                            Surface(
                                onClick = { onConditionScoreChange(score) },
                                color = if (isSelected) AccentCyan.copy(alpha = 0.15f) else BtnNormal,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentCyan else BorderColor
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "$score",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AccentCyan else TextMain
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        color = if (isSelected) AccentTeal else TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // 4. MAP INTERFACE & COORDINATE LOGGER
            // -------------------------------------------------------------
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. BASEMAP & COORDINATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Basemap Switcher Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BasemapType.values().forEach { bmap ->
                            val isSelected = selectedBasemap == bmap
                            Surface(
                                onClick = { onBasemapChange(bmap) },
                                color = if (isSelected) AccentTeal.copy(alpha = 0.2f) else CoordBoxBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) AccentTeal else BorderColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = bmap.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AccentTeal else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Interactive Map Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(geometryType, loggedCoordinates) {
                                    detectTapGestures { offset ->
                                        val relX = (offset.x / size.width) - 0.5f
                                        val relY = (offset.y / size.height) - 0.5f
                                        val newLat = defaultLat - (relY * 0.006)
                                        val newLng = defaultLng + (relX * 0.006)

                                        onLatInputChange(String.format("%.5f", newLat))
                                        onLngInputChange(String.format("%.5f", newLng))

                                        val newCoord = LatLngCoord(newLat, newLng)
                                        if (geometryType == AssetGeometryType.POINT) {
                                            onLoggedCoordinatesChange(listOf(newCoord))
                                        } else {
                                            onLoggedCoordinatesChange(loggedCoordinates + newCoord)
                                        }
                                    }
                                }
                        ) {
                            drawInteractiveBasemap(
                                basemapType = selectedBasemap,
                                centerLat = defaultLat,
                                centerLng = defaultLng,
                                coords = loggedCoordinates,
                                geometryType = geometryType
                            )
                        }

                        // Floating map indicator
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(CardBg.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                                .border(1.dp, BorderColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(AccentCyan, CircleShape)
                            )
                            Text(
                                text = "Tap map to plot point",
                                fontSize = 9.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Coordinate Box
                    Surface(
                        color = CoordBoxBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Lat/Lng input inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Latitude", fontSize = 10.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = latInputText,
                                        onValueChange = onLatInputChange,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = BtnNormal,
                                            unfocusedContainerColor = BtnNormal,
                                            focusedBorderColor = AccentCyan,
                                            unfocusedBorderColor = BorderColor,
                                            focusedTextColor = TextMain,
                                            unfocusedTextColor = TextMain
                                        )
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Longitude", fontSize = 10.sp, color = TextMuted)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    OutlinedTextField(
                                        value = lngInputText,
                                        onValueChange = onLngInputChange,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = BtnNormal,
                                            unfocusedContainerColor = BtnNormal,
                                            focusedBorderColor = AccentCyan,
                                            unfocusedBorderColor = BorderColor,
                                            focusedTextColor = TextMain,
                                            unfocusedTextColor = TextMain
                                        )
                                    )
                                }
                            }

                            // Log Coordinate Point Button
                            Button(
                                onClick = {
                                    val parsedLat = latInputText.toDoubleOrNull() ?: defaultLat
                                    val parsedLng = lngInputText.toDoubleOrNull() ?: defaultLng
                                    val point = LatLngCoord(parsedLat, parsedLng)
                                    if (geometryType == AssetGeometryType.POINT) {
                                        onLoggedCoordinatesChange(listOf(point))
                                    } else {
                                        onLoggedCoordinatesChange(loggedCoordinates + point)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Log Coordinate Point",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }

                            // Coordinate list
                            if (loggedCoordinates.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 75.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    loggedCoordinates.forEachIndexed { idx, coord ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${idx + 1}. Lat: ${String.format("%.6f", coord.lat)}, Lng: ${String.format("%.6f", coord.lng)}",
                                                fontSize = 11.sp,
                                                color = AccentCyan,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            IconButton(
                                                onClick = {
                                                    onLoggedCoordinatesChange(loggedCoordinates.filterIndexed { i, _ -> i != idx })
                                                },
                                                modifier = Modifier.size(18.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Metrics summary
                            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (geometryType) {
                                    AssetGeometryType.POLYGON -> {
                                        Text(
                                            text = "Area: ${String.format("%.3f", calculatedAreaHectares)} ha (${(calculatedAreaHectares * 10000).toInt()} m²)",
                                            fontSize = 11.sp,
                                            color = Color(0xFF10B981),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    AssetGeometryType.LINESTRING -> {
                                        Text(
                                            text = "Length: ${String.format("%.1f", calculatedLengthMeters)} m (${String.format("%.2f", calculatedLengthMeters / 1000)} km)",
                                            fontSize = 11.sp,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "GDA2020 / EPSG:7844 (Centroid Plotted)",
                                            fontSize = 11.sp,
                                            color = AccentTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (loggedCoordinates.isNotEmpty()) {
                                    Text(
                                        text = "Clear All",
                                        fontSize = 11.sp,
                                        color = Color(0xFFEF4444),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.clickable {
                                            onLoggedCoordinatesChange(emptyList())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------------------------------------
        // ACTION BUTTONS (Cancel & Register Asset)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TextMain
                )
            ) {
                Text("Cancel", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }

            Button(
                onClick = onRegisterAsset,
                modifier = Modifier
                    .weight(2f)
                    .height(44.dp)
                    .testTag("commit_spatial_asset_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentCyan,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✓ Register Asset",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// =========================================================================
// TAB 2: ISO 55001 ASSETS INVENTORY REGISTER COMPONENT
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryTabContent(
    inventoryList: List<IsoAssetRecord>,
    domainFilter: String,
    onDomainFilterChange: (String) -> Unit,
    classFilter: String,
    onClassFilterChange: (String) -> Unit,
    onSwitchToCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    var domainFilterExpanded by remember { mutableStateOf(false) }
    var classFilterExpanded by remember { mutableStateOf(false) }

    val filterDomainOptions = listOf(
        "ALL" to "All Domains",
        "Local Government / Council" to "Local Government / Council",
        "Commercial Infrastructure" to "Commercial Infrastructure",
        "Industrial Facilities" to "Industrial Facilities",
        "Educational Campus" to "Educational Campus"
    )

    val filterClassOptions = listOf(
        "ALL" to "All Asset Classes",
        "Roads & Sealed Pavement" to "Sealed Pavements / Roads",
        "Stormwater Pipe" to "Stormwater Pipelines",
        "Kerb & Channel" to "Kerb & Channels",
        "Footpath & Cycleways" to "Footpaths & Cycleways",
        "Parks & Open Spaces" to "Parks & Open Space",
        "Buildings & Structures" to "Buildings & Structures",
        "Wastewater Network" to "Wastewater Network",
        "Commercial / Facility" to "Commercial / Facility"
    )

    val filteredList = remember(inventoryList, domainFilter, classFilter) {
        inventoryList.filter { item ->
            val matchDomain = (domainFilter == "ALL" || item.domain.contains(domainFilter, ignoreCase = true))
            val matchClass = (classFilter == "ALL" || item.assetClass.contains(classFilter, ignoreCase = true))
            matchDomain && matchClass
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ISO 55001 Asset Inventory",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Standardized Asset Management Framework & Class Ledger",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Surface(
                    onClick = onSwitchToCapture,
                    color = BtnNormal,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = AccentCyan, modifier = Modifier.size(14.dp))
                        Text("Capture", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------------------------------------
        // FILTER BAR (Domain & Class Dropdowns)
        // -------------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Domain Filter
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { domainFilterExpanded = true },
                    color = BtnNormal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = filterDomainOptions.firstOrNull { it.first == domainFilter }?.second ?: domainFilter,
                            fontSize = 11.sp,
                            color = TextMain,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                    }
                }

                DropdownMenu(
                    expanded = domainFilterExpanded,
                    onDismissRequest = { domainFilterExpanded = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    filterDomainOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = TextMain, fontSize = 12.sp) },
                            onClick = {
                                onDomainFilterChange(value)
                                domainFilterExpanded = false
                            }
                        )
                    }
                }
            }

            // Class Filter
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    onClick = { classFilterExpanded = true },
                    color = BtnNormal,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = filterClassOptions.firstOrNull { it.first == classFilter }?.second ?: classFilter,
                            fontSize = 11.sp,
                            color = TextMain,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                    }
                }

                DropdownMenu(
                    expanded = classFilterExpanded,
                    onDismissRequest = { classFilterExpanded = false },
                    modifier = Modifier.background(CardBg)
                ) {
                    filterClassOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = TextMain, fontSize = 12.sp) },
                            onClick = {
                                onClassFilterChange(value)
                                classFilterExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // -------------------------------------------------------------
        // ASSET LIST CONTAINER
        // -------------------------------------------------------------
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No ISO 55001 assets found for the selected filter.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { asset ->
                    IsoAssetCard(asset = asset)
                }
            }
        }
    }
}

@Composable
private fun IsoAssetCard(asset: IsoAssetRecord) {
    Surface(
        color = ItemCardBg,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Cyan Accent Bar on the left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(AccentCyan)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header: Title & ID badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = asset.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = AccentCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = asset.id,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Domain & Asset Class
                Text(
                    text = "${asset.domain} • ${asset.assetClass}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan
                )

                // Geometry & Condition summary
                Text(
                    text = "Geometry: ${asset.geometry} | Condition: ${asset.condition}",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                // ISO 55001 / 55002 Standards Attribute Breakdown Box
                Surface(
                    color = CoordBoxBg,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IsoAttributeItem(label = "ISO Hierarchy", value = asset.hierarchy, modifier = Modifier.weight(1f))
                            IsoAttributeItem(label = "Criticality", value = asset.criticality, modifier = Modifier.weight(1f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IsoAttributeItem(label = "Lifecycle Stage", value = asset.lifecycleStage, modifier = Modifier.weight(1f))
                            IsoAttributeItem(label = "Valuation", value = asset.replacementValue, modifier = Modifier.weight(1f))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IsoAttributeItem(label = "Target LOS", value = asset.targetLOS, modifier = Modifier.weight(1f))
                            IsoAttributeItem(label = "GDA2020 Location", value = asset.coords, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IsoAttributeItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$label:",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 10.sp,
            color = TextMain,
            maxLines = 2
        )
    }
}

@Composable
private fun GeometryOptionBtn(
    title: String,
    subtitle: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = if (isActive) AccentCyan.copy(alpha = 0.15f) else BtnNormal,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isActive) AccentCyan else BorderColor
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) AccentCyan else TextMain
            )
            Text(
                text = subtitle,
                fontSize = 8.sp,
                color = if (isActive) AccentTeal else TextMuted
            )
        }
    }
}

// -------------------------------------------------------------
// DRAW INTERACTIVE BASEMAP & GEOMETRIES ON CANVAS
// -------------------------------------------------------------
private fun DrawScope.drawInteractiveBasemap(
    basemapType: BasemapType,
    centerLat: Double,
    centerLng: Double,
    coords: List<LatLngCoord>,
    geometryType: AssetGeometryType
) {
    val w = size.width
    val h = size.height

    when (basemapType) {
        BasemapType.GOOGLE_SATELLITE -> {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF0F2618), Color(0xFF05100B), Color(0xFF020906)),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.8f
                )
            )
            drawCircle(color = Color(0xFF133622).copy(alpha = 0.6f), radius = w * 0.35f, center = Offset(w * 0.4f, h * 0.4f))
            drawCircle(color = Color(0xFF18422A).copy(alpha = 0.4f), radius = w * 0.25f, center = Offset(w * 0.7f, h * 0.6f))
        }
        BasemapType.GOOGLE_ROADMAP -> {
            drawRect(color = Color(0xFF0B1728))
            val gridStep = 40f
            var x = 0f
            while (x < w) {
                drawLine(color = Color(0xFF182A44), start = Offset(x, 0f), end = Offset(x, h), strokeWidth = 1f)
                x += gridStep
            }
            var y = 0f
            while (y < h) {
                drawLine(color = Color(0xFF182A44), start = Offset(0f, y), end = Offset(w, y), strokeWidth = 1f)
                y += gridStep
            }
            drawLine(color = Color(0xFF234B78), start = Offset(0f, h * 0.45f), end = Offset(w, h * 0.45f), strokeWidth = 3f)
            drawLine(color = Color(0xFF234B78), start = Offset(w * 0.5f, 0f), end = Offset(w * 0.5f, h), strokeWidth = 3f)
        }
        BasemapType.GOOGLE_HYBRID -> {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F2033), Color(0xFF07111E))
                )
            )
            drawLine(color = Color(0xFF2C5688), start = Offset(0f, h * 0.5f), end = Offset(w, h * 0.5f), strokeWidth = 2f)
            drawLine(color = Color(0xFF2C5688), start = Offset(w * 0.4f, 0f), end = Offset(w * 0.4f, h), strokeWidth = 2f)
        }
    }

    if (coords.isEmpty()) return

    fun toCanvasOffset(c: LatLngCoord): Offset {
        val relLng = (c.lng - centerLng) / 0.006
        val relLat = (c.lat - centerLat) / 0.006
        val px = (w / 2f) + (relLng * w).toFloat()
        val py = (h / 2f) - (relLat * h).toFloat()
        return Offset(px.coerceIn(10f, w - 10f), py.coerceIn(10f, h - 10f))
    }

    val offsets = coords.map { toCanvasOffset(it) }

    when (geometryType) {
        AssetGeometryType.POINT -> {
            offsets.forEach { pt ->
                drawCircle(color = AccentCyan.copy(alpha = 0.25f), radius = 18f, center = pt)
                drawCircle(color = AccentCyan.copy(alpha = 0.6f), radius = 10f, center = pt, style = Stroke(width = 2f))
                drawCircle(color = AccentCyan, radius = 5f, center = pt)
            }
        }
        AssetGeometryType.LINESTRING -> {
            if (offsets.size >= 2) {
                val polyPath = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    for (i in 1 until offsets.size) {
                        lineTo(offsets[i].x, offsets[i].y)
                    }
                }
                drawPath(polyPath, color = AccentCyan.copy(alpha = 0.3f), style = Stroke(width = 6f))
                drawPath(polyPath, color = AccentCyan, style = Stroke(width = 2.5f))
            }
            offsets.forEach { pt ->
                drawCircle(color = CardBg, radius = 6f, center = pt)
                drawCircle(color = AccentTeal, radius = 4f, center = pt)
            }
        }
        AssetGeometryType.POLYGON -> {
            if (offsets.size >= 3) {
                val polyPath = Path().apply {
                    moveTo(offsets.first().x, offsets.first().y)
                    for (i in 1 until offsets.size) {
                        lineTo(offsets[i].x, offsets[i].y)
                    }
                    close()
                }
                drawPath(polyPath, color = AccentCyan.copy(alpha = 0.2f), style = Fill)
                drawPath(polyPath, color = AccentCyan, style = Stroke(width = 2f))
            } else if (offsets.size == 2) {
                drawLine(color = AccentCyan, start = offsets[0], end = offsets[1], strokeWidth = 2f)
            }
            offsets.forEach { pt ->
                drawCircle(color = CardBg, radius = 5f, center = pt)
                drawCircle(color = AccentCyan, radius = 3.5f, center = pt)
            }
        }
        else -> {
            offsets.forEach { pt ->
                drawCircle(color = AccentCyan, radius = 5f, center = pt)
            }
        }
    }
}

// Geometric calculations
private fun calculatePolygonAreaHectares(coords: List<LatLngCoord>): Double {
    if (coords.size < 3) return 0.0
    var area = 0.0
    val avgLat = coords.map { it.lat }.average()
    val radAvgLat = Math.toRadians(avgLat)
    val metersPerLat = 111132.92 - 559.82 * cos(2 * radAvgLat)
    val metersPerLng = 111412.84 * cos(radAvgLat)

    val points = coords.map {
        Pair(it.lng * metersPerLng, it.lat * metersPerLat)
    }

    val n = points.size
    for (i in 0 until n) {
        val j = (i + 1) % n
        area += points[i].first * points[j].second
        area -= points[j].first * points[i].second
    }
    val areaM2 = abs(area) / 2.0
    return areaM2 / 10000.0
}

private fun calculatePolylineLengthMeters(coords: List<LatLngCoord>): Double {
    if (coords.size < 2) return 0.0
    var totalMeters = 0.0
    val earthRadiusM = 6371000.0
    for (i in 0 until coords.size - 1) {
        val lat1 = Math.toRadians(coords[i].lat)
        val lat2 = Math.toRadians(coords[i + 1].lat)
        val dLat = Math.toRadians(coords[i + 1].lat - coords[i].lat)
        val dLng = Math.toRadians(coords[i + 1].lng - coords[i].lng)

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        totalMeters += earthRadiusM * c
    }
    return totalMeters
}
