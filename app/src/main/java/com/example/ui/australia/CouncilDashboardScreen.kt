package com.example.ui.australia

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssetGeometryType
import com.example.model.AustralianCouncil
import com.example.model.SectorType
import com.example.model.SpatialAsset
import com.example.model.Tenant
import com.example.ui.NavigationTab
import com.example.ui.map.AssetDialogTab
import com.example.ui.map.SpatialAssetCreationDialog
import com.example.ui.theme.*

@Composable
fun CouncilDashboardScreen(
    council: AustralianCouncil,
    onBack: () -> Unit,
    onSwitchTenant: (Tenant) -> Unit,
    onNavigateToSpatialMap: () -> Unit,
    onNavigateToWorkOrders: () -> Unit,
    onCreateAsset: (SpatialAsset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(0) } // 0: Overview & Spatial, 1: Infrastructure Assets, 2: Community Facilities
    var showCreateDialog by remember { mutableStateOf(false) }
    var creationGeometryType by remember { mutableStateOf(AssetGeometryType.POLYGON) }
    var dialogInitialTab by remember { mutableStateOf(AssetDialogTab.CAPTURE) }

    val councilTenant = remember(council) {
        Tenant(
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
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. Navigation Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("back_to_councils_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NexusCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All Councils", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Surface(
                            color = NexusCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, NexusCyan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(NexusGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${council.stateCode} LOCAL GOVERNMENT",
                                    color = NexusCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = council.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${council.region} • Official Municipal Digital Twin Dashboard",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Open Data Verification Badge
                    Surface(
                        color = NexusBackground,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NexusBorder.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "OPEN-SOURCE GOVERNMENT SPATIAL DATA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = council.openDataSource,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Spatial Dimension Key Metrics (Area, Location, Population, Value)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "TOTAL AREA",
                    value = "${council.areaKm2} km²",
                    color = NexusCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "POPULATION",
                    value = "%,d".format(council.population),
                    color = NexusGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricPill(
                    label = "REPLACEMENT VALUE",
                    value = council.replacementValueAud,
                    color = NexusYellow,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "CENTROID COORDINATES",
                    value = "${council.centerLat}, ${council.centerLng}",
                    color = NexusCoral,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Interactive Spatial Boundary Map Preview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "LGA BOUNDARY & SPATIAL COVERAGE",
                                style = MaterialTheme.typography.labelMedium,
                                color = NexusCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Polygon area: ${council.areaKm2} km² • Centroid: [${council.centerLat}, ${council.centerLng}]",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToSpatialMap,
                            colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_council_spatial_canvas")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = NexusBackground, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Map Canvas", color = NexusBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spatial Vector Map Canvas Visualizer
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070E1A))
                            .border(1.dp, NexusBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle coordinate grid lines
                            val gridCountX = 6
                            val gridCountY = 4
                            for (i in 1 until gridCountX) {
                                val x = w * (i.toFloat() / gridCountX)
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = Offset(x, 0f),
                                    end = Offset(x, h),
                                    strokeWidth = 1f
                                )
                            }
                            for (j in 1 until gridCountY) {
                                val y = h * (j.toFloat() / gridCountY)
                                drawLine(
                                    color = Color(0xFF1E293B),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw Council Boundary Polygon (ASGS LGA standard geometric approximation)
                            val path = Path().apply {
                                moveTo(w * 0.18f, h * 0.25f)
                                lineTo(w * 0.42f, h * 0.15f)
                                lineTo(w * 0.78f, h * 0.20f)
                                lineTo(w * 0.88f, h * 0.45f)
                                lineTo(w * 0.82f, h * 0.78f)
                                lineTo(w * 0.58f, h * 0.88f)
                                lineTo(w * 0.25f, h * 0.82f)
                                lineTo(w * 0.12f, h * 0.55f)
                                close()
                            }

                            // Fill semi-transparent polygon
                            drawPath(
                                path = path,
                                color = Color(0x2200E5FF)
                            )
                            // Stroke boundary polygon
                            drawPath(
                                path = path,
                                color = Color(0xFF00E5FF),
                                style = Stroke(width = 2.5f)
                            )

                            // Arterial Road network (LineString vectors)
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = Offset(w * 0.15f, h * 0.52f),
                                end = Offset(w * 0.85f, h * 0.48f),
                                strokeWidth = 2.2f
                            )
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = Offset(w * 0.48f, h * 0.18f),
                                end = Offset(w * 0.52f, h * 0.84f),
                                strokeWidth = 2.2f
                            )

                            // Stormwater Trunk Line (dashed/dotted cyan)
                            drawLine(
                                color = Color(0xFF10B981),
                                start = Offset(w * 0.35f, h * 0.30f),
                                end = Offset(w * 0.65f, h * 0.75f),
                                strokeWidth = 1.8f
                            )

                            // Open Space Park Polygon
                            val parkPath = Path().apply {
                                moveTo(w * 0.32f, h * 0.58f)
                                lineTo(w * 0.44f, h * 0.56f)
                                lineTo(w * 0.42f, h * 0.70f)
                                lineTo(w * 0.30f, h * 0.68f)
                                close()
                            }
                            drawPath(parkPath, color = Color(0x3310B981))
                            drawPath(parkPath, color = Color(0xFF10B981), style = Stroke(width = 1.5f))

                            // Centroid Point (Pulse circle)
                            drawCircle(
                                color = Color(0x44FFB703),
                                radius = 16f,
                                center = Offset(w * 0.5f, h * 0.5f)
                            )
                            drawCircle(
                                color = Color(0xFFFFB703),
                                radius = 6f,
                                center = Offset(w * 0.5f, h * 0.5f)
                            )
                        }

                        // Overlay labels
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                                .background(Color(0xCC0B1528), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Boundary Vertices: 168 pts • ASGS LGA 2024", color = NexusCyan, fontSize = 10.sp)
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color(0xCC0B1528), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("Centroid: [${council.centerLat}, ${council.centerLng}]", color = NexusYellow, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. IPWEA Infrastructure Asset Portfolio
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "IPWEA INFRASTRUCTURE PORTFOLIO",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )

                    // Roads & Transport
                    InfrastructureRow(
                        icon = Icons.Default.DirectionsCar,
                        category = "Transport & Roads (IPWEA)",
                        primaryMetric = "${council.roadLengthKm} km",
                        description = "Sealed arterials, local streets, bridges & kerbing",
                        color = NexusCyan
                    )

                    HorizontalDivider(color = NexusBorder.copy(alpha = 0.5f))

                    // Stormwater & Drainage
                    InfrastructureRow(
                        icon = Icons.Default.WaterDrop,
                        category = "Stormwater Drainage (IPWEA)",
                        primaryMetric = "${council.stormwaterPipeKm} km",
                        description = "Underground pipes, pits, detention basins & flood gates",
                        color = NexusGreen
                    )

                    HorizontalDivider(color = NexusBorder.copy(alpha = 0.5f))

                    // Parks & Open Spaces
                    InfrastructureRow(
                        icon = Icons.Default.Park,
                        category = "Parks & Open Spaces (IPWEA)",
                        primaryMetric = "${council.openSpaceHectares} ha",
                        description = "Active sporting fields, botanical reserves & playgrounds",
                        color = NexusYellow
                    )

                    HorizontalDivider(color = NexusBorder.copy(alpha = 0.5f))

                    // Municipal Assets
                    InfrastructureRow(
                        icon = Icons.Default.Apartment,
                        category = "Civic Buildings & Facilities",
                        primaryMetric = "${council.municipalAssetsCount} units",
                        description = "Libraries, sports pavilions, aquatic centers, depots",
                        color = NexusCoral
                    )
                }
            }
        }

        // 5. Council Focus & Environmental Strategy
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MUNICIPAL STRATEGIC PRIORITIES",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusYellow,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = council.primaryFocus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 6. Registered Community Facilities in this Council
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REGISTERED COMMUNITY ASSETS IN THIS LGA",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FacilityCountBadge("Schools", "${council.schoolsCount}", Icons.Default.School, NexusCyan)
                        FacilityCountBadge("Universities", "${council.universitiesCount}", Icons.Default.AccountBalance, NexusYellow)
                        FacilityCountBadge("Aged Care", "${council.agedCareCount}", Icons.Default.Elderly, NexusCoral)
                        FacilityCountBadge("Industrial", "${council.industrialCount}", Icons.Default.Factory, NexusGreen)
                    }
                }
            }
        }

        // 7. Action Buttons (Switch Tenant, Create Asset, Work Order)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NexusBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "COUNCIL OPERATIONS & ACTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = {
                            onSwitchTenant(councilTenant)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_council_active_tenant_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NexusBackground)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set as Active Session Tenant",
                            color = NexusBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "SPATIAL ASSET CREATION (IPWEA & COUNCIL STANDARDS)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                creationGeometryType = AssetGeometryType.POINT
                                dialogInitialTab = AssetDialogTab.CAPTURE
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Point", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                creationGeometryType = AssetGeometryType.LINESTRING
                                dialogInitialTab = AssetDialogTab.CAPTURE
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Polyline", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                creationGeometryType = AssetGeometryType.POLYGON
                                dialogInitialTab = AssetDialogTab.CAPTURE
                                showCreateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = NexusBackground, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Polygon", fontSize = 11.sp, color = NexusBackground, fontWeight = FontWeight.Bold)
                        }
                    }

                    // ISO 55001 Asset Inventory Register Direct Button
                    OutlinedButton(
                        onClick = {
                            dialogInitialTab = AssetDialogTab.INVENTORY
                            showCreateDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_iso_55001_register_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NexusCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = NexusSurfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open ISO 55001 Inventory Ledger & Register", fontSize = 11.sp, color = NexusCyan, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToSpatialMap,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("create_asset_for_council_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NexusCyan)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Spatial Map", color = NexusCyan, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToWorkOrders,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("council_work_order_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NexusGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Work Orders", color = NexusGreen, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    if (showCreateDialog) {
        SpatialAssetCreationDialog(
            activeTenant = councilTenant,
            initialGeometryType = creationGeometryType,
            initialTab = dialogInitialTab,
            onDismiss = { showCreateDialog = false },
            onSaveAsset = { newAsset ->
                onSwitchTenant(councilTenant)
                onCreateAsset(newAsset)
                showCreateDialog = false
                onNavigateToSpatialMap()
            }
        )
    }
}

@Composable
private fun InfrastructureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    category: String,
    primaryMetric: String,
    description: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(category, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontSize = 11.sp)
        }
        Text(
            text = primaryMetric,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FacilityCountBadge(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 6.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = CircleShape,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(count, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
    }
}
