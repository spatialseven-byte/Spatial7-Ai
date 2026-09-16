package com.example.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.security.UserRole
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpatialMapScreen(
    activeTenant: Tenant,
    assets: List<SpatialAsset>,
    sensors: List<IoTSensorTelemetry>,
    risks: List<RiskSimulationScenario>,
    onAssetSelected: (SpatialAsset) -> Unit,
    onCreateAsset: (SpatialAsset) -> Unit = {},
    activeRole: UserRole = UserRole.TENANT_ADMIN,
    modifier: Modifier = Modifier
) {
    var zoomScale by remember(activeTenant.tenantId) { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember(activeTenant.tenantId) { mutableFloatStateOf(0f) }
    var panOffsetY by remember(activeTenant.tenantId) { mutableFloatStateOf(0f) }

    // Layer toggles
    var showPolygons by remember { mutableStateOf(true) }
    var showSensors by remember { mutableStateOf(true) }
    var show3DBuildings by remember { mutableStateOf(true) }
    var showRiskSim by remember { mutableStateOf(true) }
    var showNdviOverlay by remember { mutableStateOf(false) }

    var selectedAsset by remember { mutableStateOf<SpatialAsset?>(null) }
    var selectedSensor by remember { mutableStateOf<IoTSensorTelemetry?>(null) }

    // Spatial Asset Creation Dialog state
    var showCreateDialog by remember { mutableStateOf(false) }
    var creationGeometryType by remember { mutableStateOf(AssetGeometryType.POLYGON) }
    var dialogInitialTab by remember { mutableStateOf(AssetDialogTab.CAPTURE) }

    // Pulsing radar animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        // Map Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeTenant.tenantId) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 5.0f)
                        panOffsetX += pan.x
                        panOffsetY += pan.y
                    }
                }
                .pointerInput(assets, sensors) {
                    detectTapGestures { tapOffset ->
                        // Find closest asset or sensor
                        val centerX = size.width / 2f + panOffsetX
                        val centerY = size.height / 2f + panOffsetY
                        val scale = (size.width / 0.08f) * zoomScale

                        var hitAsset: SpatialAsset? = null
                        for (asset in assets) {
                            val ax = centerX + (asset.primaryLocation.lng - activeTenant.centerLng).toFloat() * scale
                            val ay = centerY - (asset.primaryLocation.lat - activeTenant.centerLat).toFloat() * scale
                            val dist = kotlin.math.hypot(tapOffset.x - ax, tapOffset.y - ay)
                            if (dist < 36f) {
                                hitAsset = asset
                                break
                            }
                        }

                        if (hitAsset != null) {
                            selectedAsset = hitAsset
                            selectedSensor = null
                            onAssetSelected(hitAsset)
                        } else {
                            // Check sensors
                            var hitSensor: IoTSensorTelemetry? = null
                            for (sensor in sensors) {
                                val sx = centerX + (sensor.lng - activeTenant.centerLng).toFloat() * scale
                                val sy = centerY - (sensor.lat - activeTenant.centerLat).toFloat() * scale
                                val dist = kotlin.math.hypot(tapOffset.x - sx, tapOffset.y - sy)
                                if (dist < 36f) {
                                    hitSensor = sensor
                                    break
                                }
                            }
                            if (hitSensor != null) {
                                selectedSensor = hitSensor
                                selectedAsset = null
                            } else {
                                selectedAsset = null
                                selectedSensor = null
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f + panOffsetX
            val centerY = canvasHeight / 2f + panOffsetY
            val coordScale = (canvasWidth / 0.08f) * zoomScale

            // 1. Draw Geospatial Grid & Australian Coordinate Baseline
            drawSpatialGrid(centerX, centerY, canvasWidth, canvasHeight, zoomScale)

            // 2. Draw NDVI Vegetation / False Color Satellite layer if enabled
            if (showNdviOverlay) {
                drawNdviOverlay(centerX, centerY, coordScale)
            }

            // 3. Draw Risk Simulation Polygons (Bushfire / Flood / Failure)
            if (showRiskSim) {
                for (risk in risks) {
                    drawRiskZone(risk, centerX, centerY, coordScale, activeTenant)
                }
            }

            // 4. Draw Polygons and Linestrings
            if (showPolygons) {
                for (asset in assets) {
                    when (asset.geometryType) {
                        AssetGeometryType.POLYGON -> {
                            drawAssetPolygon(asset, centerX, centerY, coordScale, activeTenant, selectedAsset == asset)
                        }
                        AssetGeometryType.LINESTRING -> {
                            drawAssetLinestring(asset, centerX, centerY, coordScale, activeTenant, selectedAsset == asset)
                        }
                        AssetGeometryType.BUILDING_3D -> {
                            if (show3DBuildings) {
                                draw3DBuilding(asset, centerX, centerY, coordScale, activeTenant, selectedAsset == asset)
                            }
                        }
                        AssetGeometryType.POINT -> {
                            // Points drawn in next step
                        }
                    }
                }
            }

            // 5. Draw Sensor Nodes & Asset Points with Pulsing Telemetry
            if (showSensors) {
                for (sensor in sensors) {
                    val sx = centerX + (sensor.lng - activeTenant.centerLng).toFloat() * coordScale
                    val sy = centerY - (sensor.lat - activeTenant.centerLat).toFloat() * coordScale
                    drawSensorNode(sensor, sx, sy, pulseRadius, selectedSensor == sensor)
                }
            }

            // 6. Draw Point Assets
            for (asset in assets.filter { it.geometryType == AssetGeometryType.POINT }) {
                val px = centerX + (asset.primaryLocation.lng - activeTenant.centerLng).toFloat() * coordScale
                val py = centerY - (asset.primaryLocation.lat - activeTenant.centerLat).toFloat() * coordScale
                drawPointAsset(asset, px, py, selectedAsset == asset)
            }
        }

        // Top Spatial HUD (Coordinates, Projection, GDA2020)
        SpatialHudHeader(
            activeTenant = activeTenant,
            zoomScale = zoomScale,
            panOffsetX = panOffsetX,
            panOffsetY = panOffsetY,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 12.dp, end = 12.dp)
        )

        // Spatial Asset Creation Bar (Point, Polyline, Polygon)
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 78.dp, start = 12.dp),
            colors = CardDefaults.cardColors(containerColor = NexusSurface.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, NexusCyan.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (activeRole.canWriteAssets) {
                    Text("CREATE:", color = NexusCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Button(
                        onClick = {
                            creationGeometryType = AssetGeometryType.POINT
                            dialogInitialTab = AssetDialogTab.CAPTURE
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("create_point_asset_btn")
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Point", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            creationGeometryType = AssetGeometryType.LINESTRING
                            dialogInitialTab = AssetDialogTab.CAPTURE
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("create_polyline_asset_btn")
                    ) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Polyline", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            creationGeometryType = AssetGeometryType.POLYGON
                            dialogInitialTab = AssetDialogTab.CAPTURE
                            showCreateDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("create_polygon_asset_btn")
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = NexusBackground, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Polygon", color = NexusBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        color = NexusCoral.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, NexusCoral.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NexusCoral, modifier = Modifier.size(12.dp))
                            Text("AUDITOR (READ-ONLY)", color = NexusCoral, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = {
                        dialogInitialTab = AssetDialogTab.INVENTORY
                        showCreateDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("open_iso_register_btn")
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ISO Ledger", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Floating Map Controls (Zoom In/Out, Reset, Layer Toggles)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(5f) },
                containerColor = NexusSurfaceVariant,
                contentColor = NexusCyan
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In")
            }
            SmallFloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f) },
                containerColor = NexusSurfaceVariant,
                contentColor = NexusCyan
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
            }
            SmallFloatingActionButton(
                onClick = {
                    zoomScale = 1.0f
                    panOffsetX = 0f
                    panOffsetY = 0f
                },
                containerColor = NexusSurfaceVariant,
                contentColor = NexusAmber
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter")
            }
        }

        // Layer Filter Chips along the bottom-top
        FlowRow(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = if (selectedAsset != null || selectedSensor != null) 170.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = showPolygons,
                onClick = { showPolygons = !showPolygons },
                label = { Text("Polygons", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexusCyan.copy(alpha = 0.25f),
                    selectedLabelColor = NexusCyan
                )
            )
            FilterChip(
                selected = showSensors,
                onClick = { showSensors = !showSensors },
                label = { Text("IoT Nodes", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexusAmber.copy(alpha = 0.25f),
                    selectedLabelColor = NexusAmber
                )
            )
            FilterChip(
                selected = show3DBuildings,
                onClick = { show3DBuildings = !show3DBuildings },
                label = { Text("3D Twins", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexusPurple.copy(alpha = 0.25f),
                    selectedLabelColor = NexusPurple
                )
            )
            FilterChip(
                selected = showRiskSim,
                onClick = { showRiskSim = !showRiskSim },
                label = { Text("Risk Sim", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexusCoral.copy(alpha = 0.25f),
                    selectedLabelColor = NexusCoral
                )
            )
            FilterChip(
                selected = showNdviOverlay,
                onClick = { showNdviOverlay = !showNdviOverlay },
                label = { Text("NDVI Satellite", fontSize = 11.sp) },
                leadingIcon = { Icon(Icons.Default.Satellite, contentDescription = null, modifier = Modifier.size(14.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NexusEmerald.copy(alpha = 0.25f),
                    selectedLabelColor = NexusEmerald
                )
            )
        }

        // Bottom Inspector Card (Tapped Asset or Sensor)
        AnimatedVisibility(
            visible = selectedAsset != null || selectedSensor != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (selectedAsset != null) {
                AssetInspectorCard(
                    asset = selectedAsset!!,
                    onDismiss = { selectedAsset = null }
                )
            } else if (selectedSensor != null) {
                SensorInspectorCard(
                    sensor = selectedSensor!!,
                    onDismiss = { selectedSensor = null }
                )
            }
        }

        // Spatial Asset Creation Dialog (Point, Polyline, Polygon using surveyor logs)
        if (showCreateDialog) {
            SpatialAssetCreationDialog(
                activeTenant = activeTenant,
                initialGeometryType = creationGeometryType,
                initialTab = dialogInitialTab,
                existingAssets = assets,
                onDismiss = { showCreateDialog = false },
                onSaveAsset = { newAsset ->
                    onCreateAsset(newAsset)
                    selectedAsset = newAsset
                    showCreateDialog = false
                }
            )
        }
    }
}

// Canvas Drawing Functions
private fun DrawScope.drawSpatialGrid(centerX: Float, centerY: Float, width: Float, height: Float, zoom: Float) {
    val step = (60f * zoom).coerceIn(30f, 120f)
    val gridColor = Color(0xFF162540)

    var x = (centerX % step)
    while (x < width) {
        drawLine(color = gridColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 1f)
        x += step
    }

    var y = (centerY % step)
    while (y < height) {
        drawLine(color = gridColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 1f)
        y += step
    }
}

private fun DrawScope.drawNdviOverlay(centerX: Float, centerY: Float, scale: Float) {
    val ndviBrush = androidx.compose.ui.graphics.Brush.radialGradient(
        colors = listOf(
            NexusEmerald.copy(alpha = 0.35f),
            NexusAmber.copy(alpha = 0.15f),
            Color.Transparent
        ),
        center = Offset(centerX, centerY),
        radius = 280f * (scale / 4000f).coerceIn(0.5f, 2.0f)
    )
    drawCircle(brush = ndviBrush, center = Offset(centerX, centerY), radius = 280f * (scale / 4000f).coerceIn(0.5f, 2.0f))
}

private fun DrawScope.drawRiskZone(
    risk: RiskSimulationScenario,
    centerX: Float,
    centerY: Float,
    scale: Float,
    tenant: Tenant
) {
    if (risk.perimeterPolygon.isEmpty()) return
    val path = Path()
    val isFire = risk.hazardType == "BUSHFIRE"
    val riskColor = if (isFire) NexusCoral else Color(0xFF0284C7)

    risk.perimeterPolygon.forEachIndexed { index, coord ->
        val x = centerX + (coord.lng - tenant.centerLng).toFloat() * scale
        val y = centerY - (coord.lat - tenant.centerLat).toFloat() * scale
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path = path, color = riskColor.copy(alpha = 0.20f), style = Fill)
    drawPath(path = path, color = riskColor, style = Stroke(width = 2.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f))))
}

private fun DrawScope.drawAssetPolygon(
    asset: SpatialAsset,
    centerX: Float,
    centerY: Float,
    scale: Float,
    tenant: Tenant,
    isSelected: Boolean
) {
    if (asset.polygonBounds.isEmpty()) return
    val path = Path()
    val baseColor = if (asset.status == AssetStatus.CRITICAL) NexusCoral else NexusCyan

    asset.polygonBounds.forEachIndexed { index, coord ->
        val x = centerX + (coord.lng - tenant.centerLng).toFloat() * scale
        val y = centerY - (coord.lat - tenant.centerLat).toFloat() * scale
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        color = if (isSelected) baseColor.copy(alpha = 0.35f) else baseColor.copy(alpha = 0.12f),
        style = Fill
    )
    drawPath(
        path = path,
        color = if (isSelected) Color.White else baseColor,
        style = Stroke(width = if (isSelected) 3.5f else 2.0f)
    )

    // Draw vertex pins
    asset.polygonBounds.forEach { coord ->
        val vx = centerX + (coord.lng - tenant.centerLng).toFloat() * scale
        val vy = centerY - (coord.lat - tenant.centerLat).toFloat() * scale
        drawCircle(color = baseColor, radius = 4f, center = Offset(vx, vy))
    }
}

private fun DrawScope.drawAssetLinestring(
    asset: SpatialAsset,
    centerX: Float,
    centerY: Float,
    scale: Float,
    tenant: Tenant,
    isSelected: Boolean
) {
    if (asset.polygonBounds.size < 2) return
    val path = Path()
    val lineColor = if (asset.status == AssetStatus.CRITICAL) NexusCoral else NexusAmber

    asset.polygonBounds.forEachIndexed { index, coord ->
        val x = centerX + (coord.lng - tenant.centerLng).toFloat() * scale
        val y = centerY - (coord.lat - tenant.centerLat).toFloat() * scale
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = lineColor,
        style = Stroke(
            width = if (isSelected) 5f else 3.5f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(16f, 6f))
        )
    )
}

private fun DrawScope.draw3DBuilding(
    asset: SpatialAsset,
    centerX: Float,
    centerY: Float,
    scale: Float,
    tenant: Tenant,
    isSelected: Boolean
) {
    if (asset.polygonBounds.size < 4) return
    val heightPx = (asset.heightMeters * 1.8f).coerceIn(16f, 80f)
    val isoAngle = 0.52f // ~30 degrees isometric lift
    val isoX = heightPx * cos(isoAngle)
    val isoY = -heightPx * sin(isoAngle)

    val groundPath = Path()
    val roofPath = Path()
    val groundPoints = mutableListOf<Offset>()
    val roofPoints = mutableListOf<Offset>()

    asset.polygonBounds.forEachIndexed { index, coord ->
        val gx = centerX + (coord.lng - tenant.centerLng).toFloat() * scale
        val gy = centerY - (coord.lat - tenant.centerLat).toFloat() * scale
        val rx = gx + isoX * 0.4f
        val ry = gy + isoY * 0.8f

        groundPoints.add(Offset(gx, gy))
        roofPoints.add(Offset(rx, ry))

        if (index == 0) {
            groundPath.moveTo(gx, gy)
            roofPath.moveTo(rx, ry)
        } else {
            groundPath.lineTo(gx, gy)
            roofPath.lineTo(rx, ry)
        }
    }
    groundPath.close()
    roofPath.close()

    // Draw extrusion walls
    for (i in 0 until groundPoints.size) {
        val next = (i + 1) % groundPoints.size
        val wallPath = Path().apply {
            moveTo(groundPoints[i].x, groundPoints[i].y)
            lineTo(groundPoints[next].x, groundPoints[next].y)
            lineTo(roofPoints[next].x, roofPoints[next].y)
            lineTo(roofPoints[i].x, roofPoints[i].y)
            close()
        }
        drawPath(wallPath, color = NexusSurfaceVariant.copy(alpha = 0.85f), style = Fill)
        drawPath(wallPath, color = NexusCyan.copy(alpha = 0.4f), style = Stroke(width = 1.2f))
    }

    // Draw roof plate
    val roofColor = if (isSelected) NexusCyan.copy(alpha = 0.6f) else NexusPurple.copy(alpha = 0.4f)
    drawPath(roofPath, color = roofColor, style = Fill)
    drawPath(roofPath, color = if (isSelected) Color.White else NexusCyan, style = Stroke(width = 2.0f))
}

private fun DrawScope.drawSensorNode(
    sensor: IoTSensorTelemetry,
    x: Float,
    y: Float,
    pulseR: Float,
    isSelected: Boolean
) {
    val nodeColor = when (sensor.status) {
        AssetStatus.OPTIMAL -> NexusEmerald
        AssetStatus.WARNING -> NexusAmber
        AssetStatus.CRITICAL -> NexusCoral
        AssetStatus.MAINTENANCE -> NexusCyan
    }

    // Pulsing radar ripple
    drawCircle(
        color = nodeColor.copy(alpha = (1f - (pulseR / 28f)).coerceIn(0f, 0.7f)),
        radius = pulseR,
        center = Offset(x, y),
        style = Stroke(width = 1.5f)
    )

    // Center marker
    drawCircle(color = NexusSurface, radius = 9f, center = Offset(x, y))
    drawCircle(color = if (isSelected) Color.White else nodeColor, radius = 6f, center = Offset(x, y))
}

private fun DrawScope.drawPointAsset(
    asset: SpatialAsset,
    x: Float,
    y: Float,
    isSelected: Boolean
) {
    val pointColor = when (asset.status) {
        AssetStatus.OPTIMAL -> NexusEmerald
        AssetStatus.WARNING -> NexusAmber
        AssetStatus.CRITICAL -> NexusCoral
        AssetStatus.MAINTENANCE -> NexusCyan
    }

    drawCircle(color = pointColor.copy(alpha = 0.35f), radius = 14f, center = Offset(x, y))
    drawCircle(color = NexusSurface, radius = 8f, center = Offset(x, y))
    drawCircle(color = if (isSelected) Color.White else pointColor, radius = 5f, center = Offset(x, y))
}

// Map HUD Overlays
@Composable
private fun SpatialHudHeader(
    activeTenant: Tenant,
    zoomScale: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = NexusSurface.copy(alpha = 0.92f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(activeTenant.sectorType.accentColor)
                    )
                    Text(
                        text = activeTenant.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "GDA2020 / MGA Zone | Lat: ${String.format("%.4f", activeTenant.centerLat)}  Lng: ${String.format("%.4f", activeTenant.centerLng)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Surface(
                color = NexusSurfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "ZOOM ${String.format("%.1fx", zoomScale)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AssetInspectorCard(
    asset: SpatialAsset,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NexusCyan)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = asset.assetCategory.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metrics table
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                asset.metrics.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = key, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (value.contains("ALERT") || value.contains("BREACH")) NexusCoral else TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${asset.status.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(asset.status.colorHex),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Last Scan: ${asset.lastInspected}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun SensorInspectorCard(
    sensor: IoTSensorTelemetry,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, NexusAmber)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TIMESCALEDB IOT NODE",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sensor.label,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Current Telemetry", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${sensor.currentValue} ${sensor.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    color = NexusSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
                ) {
                    Text(
                        text = sensor.lastUpdated,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
