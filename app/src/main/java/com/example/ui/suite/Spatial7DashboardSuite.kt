package com.example.ui.suite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Spatial7BaseMap
import com.example.model.Spatial7MockData
import com.example.model.Spatial7SuiteAsset
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

/**
 * Spatial7 Enterprise Suite Dashboard
 *
 * Implements:
 * 1. Top Navigation & System Indicators (Australia GDA2020, Sectors, Spatial GIS Data, Spatial AI Work, RLS Security, Database View)
 *    - Owner / Operator attribution: Kris Lal
 * 2. Base Map Layer Control:
 *    - Cadastral (Lot boundaries, easements, road reserves)
 *    - Zoning (Planning schemes, land use zones, building heights)
 *    - Aerial Imagery (5cm orthorectified multi-spectral photogrammetry)
 * 3. 3-Tab Architecture:
 *    - Tab 1: Spatial GIS Dashboard (Layer Topology, Precision ±0.02m, LineString km, Point Nodes, Validation Loop)
 *    - Tab 2: Asset Data Dashboard (Gross Replacement Cost, Written Down Value, AASB116 / IPWEA, NAMS 1-5 Condition Summary)
 *    - Tab 3: Combined Spatial & Asset Twin (Spatial GIS Viewport, Asset Data Attributes with Live Condition Score Selector syncing to Spatial GIS, and Synchronized Table Register)
 */
@Composable
fun Spatial7DashboardSuite(
    onNavigateToMap: () -> Unit = {},
    onNavigateToAi: (String) -> Unit = {},
    onNavigateToArchitecture: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("combined") } // "spatial" | "asset" | "combined"
    var assetList by remember { mutableStateOf(Spatial7MockData.initialAssets) }
    var selectedAsset by remember { mutableStateOf<Spatial7SuiteAsset?>(assetList.firstOrNull()) }
    var conditionDropdownExpanded by remember { mutableStateOf(false) }

    // Base Map Layer Control States (Cadastral, Zoning, Aerial)
    var selectedBaseMap by remember { mutableStateOf(Spatial7BaseMap.CADASTRAL) }
    var showBaseMapLabels by remember { mutableStateOf(true) }
    var showBaseMapEasements by remember { mutableStateOf(true) }

    val currencyFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    // Synchronized Update Handler
    fun handleConditionChange(id: String, newCondition: Int) {
        assetList = assetList.map { item ->
            if (item.id == id) {
                item.copy(condition = newCondition, updatedBy = "Kris Lal")
            } else item
        }
        if (selectedAsset?.id == id) {
            selectedAsset = selectedAsset?.copy(condition = newCondition, updatedBy = "Kris Lal")
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ====================================================================
        // 1. TOP NAVIGATION & SYSTEM INDICATORS
        // ====================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spatial7 Enterprise Suite",
                            color = Color(0xFF38BDF8),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Owner / Operator: Kris Lal",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Surface(
                        color = Color(0xFF22C55E).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF22C55E)))
                            Text("ENGINE READY", color = Color(0xFF22C55E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Indicators / Main Icons Badges (Horizontal scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuiteBadge(text = "🇦🇺 Australia (GDA2020)")
                    SuiteBadge(text = "🏢 Sectors")
                    SuiteBadge(text = "🗺️ Spatial GIS Data", onClick = onNavigateToMap)
                    SuiteBadge(text = "🤖 Spatial AI Work", onClick = { onNavigateToAi("Analyze pavement segment degradation") })
                    SuiteBadge(text = "🔒 RLS Security: ACTIVE", onClick = onNavigateToArchitecture)
                    SuiteBadge(text = "🗄️ Database View (DV)", onClick = onNavigateToArchitecture)
                }
            }
        }

        // ====================================================================
        // 2. TAB CONTROLS
        // ====================================================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    label = "🗺️ Spatial GIS Dashboard",
                    isActive = activeTab == "spatial",
                    onClick = { activeTab = "spatial" },
                    testTag = "tab_spatial_gis"
                )
                TabButton(
                    label = "📊 Asset Data Dashboard",
                    isActive = activeTab == "asset",
                    onClick = { activeTab = "asset" },
                    testTag = "tab_asset_data"
                )
                TabButton(
                    label = "🔄 Combined Spatial & Asset Twin",
                    isActive = activeTab == "combined",
                    onClick = { activeTab = "combined" },
                    testTag = "tab_combined_twin"
                )
            }
        }

        // ====================================================================
        // 3. TAB 1: SPATIAL GIS DASHBOARD
        // ====================================================================
        if (activeTab == "spatial") {
            item {
                SuiteCard(title = "GIS Layer Topology & Precision") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Projection: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("EPSG:7856 (GDA2020 / MGA Zone 56)", color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Positional Accuracy: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("± 0.02m (GNSS Vector Verified)", color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Topology Status: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("✓ 0 Overlaps / Self-Intersections", color = Color(0xFF4ADE80), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                SuiteCard(title = "Spatial Feature Distribution") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total LineString Features: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("1,420 km", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total Point Nodes (Pits/Assets): ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("8,940", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Spatial AI Validation Loop: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("Active (PostGIS Triggers Enforced)", color = Color(0xFF4ADE80), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                SuiteCard(title = "Spatial Layer Explorer & Base Maps") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Spatial7BaseMapLayerControl(
                            selectedBaseMap = selectedBaseMap,
                            onSelectBaseMap = { selectedBaseMap = it },
                            showLabels = showBaseMapLabels,
                            onToggleLabels = { showBaseMapLabels = it },
                            showEasements = showBaseMapEasements,
                            onToggleEasements = { showBaseMapEasements = it }
                        )

                        Spatial7InteractiveMapCanvas(
                            baseMap = selectedBaseMap,
                            selectedAsset = selectedAsset,
                            showLabels = showBaseMapLabels,
                            showEasements = showBaseMapEasements
                        )
                    }
                }
            }
        }

        // ====================================================================
        // 4. TAB 2: ASSET DATA DASHBOARD
        // ====================================================================
        if (activeTab == "asset") {
            item {
                SuiteCard(title = "Asset Register Financial Metrics") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Gross Replacement Cost (GRC): ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("$24,500,000 AUD", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Written Down Value (WDV): ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("$18,200,000 AUD", color = Color(0xFF4ADE80), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Depreciation Standard: ", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            Text("AASB116 / IPWEA Compliant", color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                SuiteCard(title = "Asset Condition Summary (NAMS 1–5)") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ConditionBarRow(
                            label = "Condition 1-2 (Good/Very Good):",
                            percentage = "72%",
                            barRatio = 0.72f,
                            color = Color(0xFF22C55E)
                        )
                        ConditionBarRow(
                            label = "Condition 3 (Fair):",
                            percentage = "18%",
                            barRatio = 0.18f,
                            color = Color(0xFFFBBF24)
                        )
                        ConditionBarRow(
                            label = "Condition 4-5 (Poor/Critical):",
                            percentage = "10%",
                            barRatio = 0.10f,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // ====================================================================
        // 5. TAB 3: COMBINED SPATIAL & ASSET DATA DASHBOARD
        // ====================================================================
        if (activeTab == "combined") {
            // Spatial Map Viewport Card with Layer Control
            item {
                SuiteCard(title = "Spatial GIS Viewport & Base Map Control") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Base Map Layer Control (Cadastral, Zoning, Aerial)
                        Spatial7BaseMapLayerControl(
                            selectedBaseMap = selectedBaseMap,
                            onSelectBaseMap = { selectedBaseMap = it },
                            showLabels = showBaseMapLabels,
                            onToggleLabels = { showBaseMapLabels = it },
                            showEasements = showBaseMapEasements,
                            onToggleEasements = { showBaseMapEasements = it }
                        )

                        // Interactive Dynamic Map Viewport
                        Spatial7InteractiveMapCanvas(
                            baseMap = selectedBaseMap,
                            selectedAsset = selectedAsset,
                            showLabels = showBaseMapLabels,
                            showEasements = showBaseMapEasements
                        )

                        // Active Feature Status Bar
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Selected Spatial Feature:", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text(
                                        text = selectedAsset?.let { "${it.name} (${it.lat}, ${it.lng})" } ?: "Click an asset below",
                                        color = Color(0xFFF8FAFC),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF475569))
                                ) {
                                    Text(
                                        text = "${selectedBaseMap.shortCode} • GDA2020",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Asset Data Attributes Card
            item {
                SuiteCard(title = "Asset Data Attributes") {
                    if (selectedAsset != null) {
                        val asset = selectedAsset!!
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Asset ID: ${asset.id}", color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text("Updated: ${asset.updatedBy}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            Text("Name: ${asset.name}", color = Color(0xFFF8FAFC), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Class: ${asset.assetClass} (${asset.subClass})", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("Replacement Value: $${currencyFormat.format(asset.value)} AUD", color = Color(0xFF4ADE80), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF334155))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive Condition Score Dropdown / Sync Trigger
                            Text(
                                text = "Update Condition Score (Syncs to Spatial GIS):",
                                color = Color(0xFFF8FAFC),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Box {
                                Surface(
                                    onClick = { conditionDropdownExpanded = true },
                                    color = Color(0xFF334155),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFF475569)),
                                    modifier = Modifier.fillMaxWidth().testTag("condition_score_selector")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val conditionText = when (asset.condition) {
                                            1 -> "1 - Very Good (Optimal)"
                                            2 -> "2 - Good"
                                            3 -> "3 - Fair"
                                            4 -> "4 - Poor"
                                            5 -> "5 - Very Poor (Critical)"
                                            else -> "${asset.condition}"
                                        }
                                        Text(conditionText, color = Color(0xFFF8FAFC), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF94A3B8))
                                    }
                                }

                                DropdownMenu(
                                    expanded = conditionDropdownExpanded,
                                    onDismissRequest = { conditionDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B))
                                ) {
                                    listOf(
                                        1 to "1 - Very Good",
                                        2 to "2 - Good",
                                        3 to "3 - Fair",
                                        4 to "4 - Poor",
                                        5 to "5 - Very Poor"
                                    ).forEach { (score, desc) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(desc, color = if (score > 3) Color(0xFFEF4444) else Color(0xFFF8FAFC))
                                            },
                                            onClick = {
                                                handleConditionChange(asset.id, score)
                                                conditionDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Select an asset to view attributes.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    }
                }
            }

            // Synchronized Spatial & Asset Data Register Table
            item {
                SuiteCard(title = "Synchronized Spatial & Asset Data Register") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(BorderStroke(0.5.dp, Color(0xFF475569)), RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F172A))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Asset ID", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Name", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                            Text("Score", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("Value", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                            Text("Action", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        }

                        // Table Rows
                        assetList.forEach { asset ->
                            val isSelected = selectedAsset?.id == asset.id
                            Surface(
                                onClick = { selectedAsset = asset },
                                color = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF2563EB) else Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth().testTag("asset_row_${asset.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(asset.id, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1.2f))
                                    Column(modifier = Modifier.weight(2f)) {
                                        Text(asset.name, color = Color(0xFFF8FAFC), fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                        Text(asset.assetClass, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = if (asset.condition > 3) Color(0xFFEF4444) else Color(0xFF22C55E),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "${asset.condition}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        "$${currencyFormat.format(asset.value)}",
                                        color = Color(0xFFF8FAFC),
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Button(
                                        onClick = { selectedAsset = asset },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(28.dp).weight(1f)
                                    ) {
                                        Text("Twin", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// UI HELPER COMPONENTS
// ============================================================================

@Composable
private fun SuiteCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color(0xFF38BDF8),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SuiteBadge(
    text: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        color = Color(0xFF334155),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF475569))
    ) {
        Text(
            text = text,
            color = Color(0xFFF1F5F9),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun TabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF2563EB) else Color(0xFF334155)
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = Modifier.testTag(testTag)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ConditionBarRow(
    label: String,
    percentage: String,
    barRatio: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color(0xFF94A3B8), fontSize = 12.sp)
            Text(percentage, color = Color(0xFFF8FAFC), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF334155))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(barRatio)
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}
