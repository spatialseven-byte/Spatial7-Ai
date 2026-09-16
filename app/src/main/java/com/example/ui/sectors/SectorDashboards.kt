package com.example.ui.sectors

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.suite.Spatial7DashboardSuite
import com.example.ui.theme.*

@Composable
fun DynamicSectorDashboard(
    tenant: Tenant,
    assets: List<SpatialAsset>,
    sensors: List<IoTSensorTelemetry>,
    risks: List<RiskSimulationScenario>,
    onNavigateToMap: () -> Unit,
    onNavigateToAi: (String) -> Unit,
    onNavigateToArchitecture: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(0) } // 0: Sector Twin & Telemetry, 1: Spatial7 Enterprise Suite

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        // Mode Selector: Sector Twin vs Spatial7 Enterprise Suite
        TabRow(
            selectedTabIndex = viewMode,
            containerColor = NexusSurface,
            contentColor = NexusCyan,
            divider = { HorizontalDivider(color = NexusCardBorder) }
        ) {
            Tab(
                selected = viewMode == 0,
                onClick = { viewMode = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Sector Twin & Telemetry", fontSize = 12.sp, fontWeight = if (viewMode == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = viewMode == 1,
                onClick = { viewMode = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Spatial7 Enterprise Suite", fontSize = 12.sp, fontWeight = if (viewMode == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        if (viewMode == 1) {
            Spatial7DashboardSuite(
                onNavigateToMap = onNavigateToMap,
                onNavigateToAi = onNavigateToAi,
                onNavigateToArchitecture = onNavigateToArchitecture
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Sector Banner Header
        item {
            SectorHeaderBanner(tenant = tenant, onNavigateToMap = onNavigateToMap)
        }

        // Dynamic Sector Content
        when (tenant.sectorType) {
            SectorType.AGRICULTURE -> {
                item { AgricultureTelemetrySection(sensors = sensors, onNavigateToAi = onNavigateToAi) }
                item { VirtualFencingSection(assets = assets, onNavigateToMap = onNavigateToMap) }
            }
            SectorType.LOCAL_GOV -> {
                item { CouncilInfrastructureSection(assets = assets, sensors = sensors) }
                item { RiskSimulationSection(risks = risks, onNavigateToAi = onNavigateToAi) }
            }
            SectorType.EDUCATION -> {
                item { CampusDigitalTwinSection(assets = assets, sensors = sensors) }
                item { HvacEnergySection(sensors = sensors, onNavigateToAi = onNavigateToAi) }
            }
            SectorType.INDUSTRIAL -> {
                item { IndustrialPredictiveSection(sensors = sensors, onNavigateToAi = onNavigateToAi) }
                item { DronePhotogrammetrySection(assets = assets, onNavigateToMap = onNavigateToMap) }
            }
        }

        // Active Assets in Tenant Register
        item {
            Text(
                text = "SPATIAL ASSET TELEMETRY STREAM",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(assets) { asset ->
            AssetTelemetryCard(asset = asset, onExplore = onNavigateToMap)
        }
    }
        }
    }
}

// 1. Sector Banner
@Composable
fun SectorHeaderBanner(tenant: Tenant, onNavigateToMap: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(tenant.sectorType.accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tenant.sectorType.icon,
                            contentDescription = null,
                            tint = tenant.sectorType.accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = tenant.sectorType.title.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = tenant.sectorType.accentColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tenant.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    color = NexusSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${tenant.stateCode} | AUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tenant.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickStatItem(label = "Assets", value = "${tenant.activeAssetsCount}", color = NexusCyan)
                QuickStatItem(label = "IoT Nodes", value = "${tenant.activeSensorsCount}", color = NexusAmber)
                QuickStatItem(label = "Stream Rate", value = tenant.iotStreamRate, color = NexusEmerald)
            }
        }
    }
}

@Composable
private fun QuickStatItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// 2. Agriculture Telemetry
@Composable
fun AgricultureTelemetrySection(sensors: List<IoTSensorTelemetry>, onNavigateToAi: (String) -> Unit) {
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
                    text = "REMOTE SENSING & SOIL MOISTURE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SectorAgriColor,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToAi("Analyze pasture biomass and virtual fence breach patterns in Kimberley Station") }) {
                    Text("AI Analysis", color = NexusCyan, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPillBox(
                    title = "Sentinel-2 NDVI",
                    value = "0.74",
                    sub = "Healthy Dense Canopy",
                    accent = NexusEmerald,
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    title = "Soil Moisture (VWC)",
                    value = "28.4%",
                    sub = "Profile 10-60cm",
                    accent = NexusCyan,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun VirtualFencingSection(assets: List<SpatialAsset>, onNavigateToMap: () -> Unit) {
    val breachAsset = assets.find { it.status == AssetStatus.CRITICAL }
    Card(
        colors = CardDefaults.cardColors(containerColor = if (breachAsset != null) NexusCoral.copy(alpha = 0.1f) else NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, if (breachAsset != null) NexusCoral else NexusAmber)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = if (breachAsset != null) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (breachAsset != null) NexusCoral else NexusEmerald
                    )
                    Text(
                        text = if (breachAsset != null) "VIRTUAL FENCE BREACH ACTIVE" else "VIRTUAL FENCE CONTAINMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (breachAsset != null) NexusCoral else NexusEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onNavigateToMap,
                    colors = ButtonDefaults.buttonColors(containerColor = if (breachAsset != null) NexusCoral else NexusSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (breachAsset != null) "Locate Beast" else "View Paddocks", fontSize = 12.sp)
                }
            }

            if (breachAsset != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${breachAsset.name} has breached Paddock Beta by 140 meters. Collar acoustic tone Level 2 triggered.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        }
    }
}

// 3. Local Gov Telemetry
@Composable
fun CouncilInfrastructureSection(assets: List<SpatialAsset>, sensors: List<IoTSensorTelemetry>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MUNICIPAL INFRASTRUCTURE ASSETS",
                style = MaterialTheme.typography.labelSmall,
                color = SectorCouncilColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPillBox(
                    title = "Smart Streetlights",
                    value = "4,200",
                    sub = "99.8% Uptime | -38% kWh",
                    accent = NexusCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    title = "Stormwater Main",
                    value = "1,800mm",
                    sub = "48% Silt Inflow Alert",
                    accent = NexusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun RiskSimulationSection(risks: List<RiskSimulationScenario>, onNavigateToAi: (String) -> Unit) {
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
                    text = "DISASTER RISK SIMULATION ENGINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCoral,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToAi("Simulate Nerang River flood evacuation routes and pipe failure consequences") }) {
                    Text("Simulate", color = NexusCyan, fontSize = 12.sp)
                }
            }

            risks.forEach { scenario ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NexusSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = scenario.title, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(text = "Area: ${scenario.affectedAreaHectares} ha | ${scenario.triggerCondition}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    Surface(
                        color = NexusCoral.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "INDEX ${scenario.severityIndex}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusCoral,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// 4. Education Telemetry
@Composable
fun CampusDigitalTwinSection(assets: List<SpatialAsset>, sensors: List<IoTSensorTelemetry>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PARKVILLE 3D DIGITAL TWIN (LOD-3)",
                style = MaterialTheme.typography.labelSmall,
                color = SectorEduColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPillBox(
                    title = "Wilson Hall LOD-3",
                    value = "5,200 m²",
                    sub = "Heritage Microclimate",
                    accent = SectorEduColor,
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    title = "Redmond Barry",
                    value = "12 Floors",
                    sub = "Filter Load 92%",
                    accent = NexusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun HvacEnergySection(sensors: List<IoTSensorTelemetry>, onNavigateToAi: (String) -> Unit) {
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
                    text = "HVAC EFFICIENCY & AIR QUALITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToAi("Calculate energy optimization by adjusting South Wing campus chiller setpoints") }) {
                    Text("Optimize", color = NexusCyan, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPillBox(
                    title = "Chiller Plant Power",
                    value = "385 kW",
                    sub = "Central Ring Nominal",
                    accent = NexusCyan,
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    title = "Indoor Air Quality",
                    value = "540 ppm",
                    sub = "CO2 Clean Optimal",
                    accent = NexusEmerald,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// 5. Industrial Telemetry
@Composable
fun IndustrialPredictiveSection(sensors: List<IoTSensorTelemetry>, onNavigateToAi: (String) -> Unit) {
    val vibSensor = sensors.find { it.sensorType == "VIBRATION_PREDICTIVE" }
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NexusCoral)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PREDICTIVE MAINTENANCE ALERT",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCoral,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { onNavigateToAi("Predict conveyor C-104 bearing failure timeline from vibration FFT spectra") }) {
                    Text("Predict Timeline", color = NexusCoral, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPillBox(
                    title = "Conveyor C-104",
                    value = "${vibSensor?.currentValue ?: 4.8} mm/s",
                    sub = "Tri-Axial FFT Harmonic Peak",
                    accent = NexusCoral,
                    modifier = Modifier.weight(1f)
                )
                MetricPillBox(
                    title = "3.2MW Drive Stator",
                    value = "89.4 °C",
                    sub = "Overheating Warning",
                    accent = NexusAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DronePhotogrammetrySection(assets: List<SpatialAsset>, onNavigateToMap: () -> Unit) {
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
                    text = "DRONE PHOTOGRAMMETRY & RUST DEFECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SectorIndusColor,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToMap) {
                    Text("Inspect Berth", color = NexusCyan, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Port Berth #02 Shiploader: 4.2m² Grade C flaking corrosion identified on Gantry 3. Structural re-rating recommended.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// Reusable UI Pieces
@Composable
fun MetricPillBox(title: String, value: String, sub: String, accent: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = NexusSurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(text = sub, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
fun AssetTelemetryCard(asset: SpatialAsset, onExplore: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NexusSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(asset.status.colorHex))
                    )
                    Text(
                        text = asset.assetCategory.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Geometry: ${asset.geometryType.name} | Status: ${asset.status.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            IconButton(onClick = onExplore) {
                Icon(Icons.Default.PinDrop, contentDescription = "View On Map", tint = NexusCyan)
            }
        }
    }
}
