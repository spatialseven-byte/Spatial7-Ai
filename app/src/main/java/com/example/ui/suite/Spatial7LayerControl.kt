package com.example.ui.suite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CadastralParcel
import com.example.model.PlanningZoneInfo
import com.example.model.Spatial7BaseMap
import com.example.model.Spatial7BaseMapCatalog
import com.example.model.Spatial7SuiteAsset
import com.example.ui.theme.*

/**
 * Spatial7 Base Map Layer Control Component
 *
 * Allows toggling between Cadastral, Zoning, and Aerial Imagery base maps,
 * with secondary layer feature toggles (labels, easements, grid) and
 * technical metadata display.
 */
@Composable
fun Spatial7BaseMapLayerControl(
    selectedBaseMap: Spatial7BaseMap,
    onSelectBaseMap: (Spatial7BaseMap) -> Unit,
    showLabels: Boolean,
    onToggleLabels: (Boolean) -> Unit,
    showEasements: Boolean,
    onToggleEasements: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E293B))
            .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = "Base Map Layers",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Base Map Layer Control",
                    color = Color(0xFFF8FAFC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.5.dp, Color(0xFF475569))
            ) {
                Text(
                    text = "EPSG:7856 (GDA2020)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Primary 3-Way Base Map Selector Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BaseMapSegmentButton(
                baseMap = Spatial7BaseMap.CADASTRAL,
                isSelected = selectedBaseMap == Spatial7BaseMap.CADASTRAL,
                onClick = { onSelectBaseMap(Spatial7BaseMap.CADASTRAL) },
                icon = Icons.Default.GridOn,
                testTag = "basemap_toggle_cadastral",
                modifier = Modifier.weight(1f)
            )

            BaseMapSegmentButton(
                baseMap = Spatial7BaseMap.ZONING,
                isSelected = selectedBaseMap == Spatial7BaseMap.ZONING,
                onClick = { onSelectBaseMap(Spatial7BaseMap.ZONING) },
                icon = Icons.Default.Category,
                testTag = "basemap_toggle_zoning",
                modifier = Modifier.weight(1f)
            )

            BaseMapSegmentButton(
                baseMap = Spatial7BaseMap.AERIAL,
                isSelected = selectedBaseMap == Spatial7BaseMap.AERIAL,
                onClick = { onSelectBaseMap(Spatial7BaseMap.AERIAL) },
                icon = Icons.Default.SatelliteAlt,
                testTag = "basemap_toggle_aerial",
                modifier = Modifier.weight(1f)
            )
        }

        // Secondary Options & Layer Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedBaseMap.description,
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = "Source: ${selectedBaseMap.dataSource} • ${selectedBaseMap.resolution}",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = showLabels,
                    onClick = { onToggleLabels(!showLabels) },
                    label = { Text("Labels", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF334155),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.height(26.dp)
                )

                FilterChip(
                    selected = showEasements,
                    onClick = { onToggleEasements(!showEasements) },
                    label = { Text("Easements", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF334155),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.height(26.dp)
                )
            }
        }
    }
}

@Composable
private fun BaseMapSegmentButton(
    baseMap: Spatial7BaseMap,
    isSelected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF2563EB) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "segmentBg"
    )

    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = baseMap.displayName,
                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = baseMap.displayName,
                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Interactive Graphic Viewport rendering the selected Base Map
 * (Cadastral boundaries, Statutory Zoning, or Orthorectified Aerial imagery)
 */
@Composable
fun Spatial7InteractiveMapCanvas(
    baseMap: Spatial7BaseMap,
    selectedAsset: Spatial7SuiteAsset?,
    showLabels: Boolean,
    showEasements: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .border(BorderStroke(1.dp, Color(0xFF334155)), RoundedCornerShape(8.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (baseMap) {
                Spatial7BaseMap.CADASTRAL -> drawCadastralBaseMap(
                    width = width,
                    height = height,
                    showLabels = showLabels,
                    showEasements = showEasements
                )
                Spatial7BaseMap.ZONING -> drawZoningBaseMap(
                    width = width,
                    height = height,
                    showLabels = showLabels
                )
                Spatial7BaseMap.AERIAL -> drawAerialBaseMap(
                    width = width,
                    height = height
                )
            }

            // Draw Selected Asset Vector Overlay
            drawAssetOverlay(
                asset = selectedAsset,
                width = width,
                height = height
            )
        }

        // Overlay Badge top-left: Active Layer Type
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.85f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(baseMap.iconEmoji, fontSize = 12.sp)
                Text(
                    text = "${baseMap.displayName.uppercase()} BASEMAP",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Overlay Bottom Legend: Context-sensitive legend
        Surface(
            color = Color(0xFF0F172A).copy(alpha = 0.88f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, Color(0xFF334155)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            when (baseMap) {
                Spatial7BaseMap.CADASTRAL -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF38BDF8), label = "Parcel Boundary")
                        if (showEasements) {
                            LegendItem(color = Color(0xFFF59E0B), label = "Easement (Dashed)")
                        }
                        LegendItem(color = Color(0xFF22C55E), label = "Selected Asset Node")
                    }
                }
                Spatial7BaseMap.ZONING -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF3B82F6), label = "LMR Residential")
                        LegendItem(color = Color(0xFF06B6D4), label = "PC Commercial")
                        LegendItem(color = Color(0xFF22C55E), label = "OS Open Space")
                        LegendItem(color = Color(0xFFA855F7), label = "IN1 Industrial")
                    }
                }
                Spatial7BaseMap.AERIAL -> {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = Color(0xFF475569), label = "Orthophoto 0.05m GSD")
                        LegendItem(color = Color(0xFF0284C7), label = "Canopy & Footprints")
                        LegendItem(color = Color(0xFF38BDF8), label = "Active Asset Vector")
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, color = Color(0xFFCBD5E1), fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

// ============================================================================
// CANVAS DRAWING EXTENSIONS
// ============================================================================

/**
 * Draw Cadastral Base Map:
 * Sharp boundary lines, parcel polygons, road reserve corridors, and easements
 */
private fun DrawScope.drawCadastralBaseMap(
    width: Float,
    height: Float,
    showLabels: Boolean,
    showEasements: Boolean
) {
    // 1. Background dark canvas with subtle cadastral grid
    drawRect(color = Color(0xFF0B132B), size = Size(width, height))

    val gridSpacing = 40f
    var x = 0f
    while (x < width) {
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = 0.5f
        )
        x += gridSpacing
    }
    var y = 0f
    while (y < height) {
        drawLine(
            color = Color(0xFF1E293B),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 0.5f
        )
        y += gridSpacing
    }

    // 2. Road Reserve Corridor (Main Street)
    val roadTop = height * 0.42f
    val roadBottom = height * 0.58f
    drawRect(
        color = Color(0xFF162032),
        topLeft = Offset(0f, roadTop),
        size = Size(width, roadBottom - roadTop)
    )
    // Road centerline
    drawLine(
        color = Color(0xFF334155),
        start = Offset(0f, (roadTop + roadBottom) / 2),
        end = Offset(width, (roadTop + roadBottom) / 2),
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
    )

    // 3. Cadastral Parcels - Top Row (Lots 101, 102, 103, 104)
    val parcelWidth = width * 0.23f
    val parcelHeightTop = roadTop - 15f
    val parcelStroke = Stroke(width = 1.5f)

    for (i in 0..3) {
        val px = 10f + i * (parcelWidth + 8f)
        val py = 12f
        val pW = parcelWidth
        val pH = parcelHeightTop - 12f

        // Parcel Fill
        drawRoundRect(
            color = Color(0xFF1E293B).copy(alpha = 0.7f),
            topLeft = Offset(px, py),
            size = Size(pW, pH),
            cornerRadius = CornerRadius(2f, 2f)
        )
        // Parcel Boundary
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(px, py),
            size = Size(pW, pH),
            cornerRadius = CornerRadius(2f, 2f),
            style = parcelStroke
        )

        // Easement in Lot 102
        if (showEasements && i == 1) {
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(px + pW - 12f, py),
                end = Offset(px + pW - 12f, py + pH),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        }
    }

    // 4. Cadastral Parcels - Bottom Row (Lots 201, 202, 203)
    val parcelHeightBottom = height - roadBottom - 24f
    for (i in 0..2) {
        val px = 15f + i * (width * 0.31f + 10f)
        val py = roadBottom + 12f
        val pW = width * 0.30f
        val pH = parcelHeightBottom

        drawRoundRect(
            color = Color(0xFF1E293B).copy(alpha = 0.7f),
            topLeft = Offset(px, py),
            size = Size(pW, pH),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = Color(0xFF38BDF8),
            topLeft = Offset(px, py),
            size = Size(pW, pH),
            cornerRadius = CornerRadius(2f, 2f),
            style = parcelStroke
        )

        // Easement in Lot 201
        if (showEasements && i == 0) {
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(px, py + 16f),
                end = Offset(px + pW, py + 16f),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )
        }
    }
}

/**
 * Draw Statutory Zoning Base Map:
 * Distinct thematic color-coded zones with planning scheme codes
 */
private fun DrawScope.drawZoningBaseMap(
    width: Float,
    height: Float,
    showLabels: Boolean
) {
    // 1. Base dark background
    drawRect(color = Color(0xFF0F172A), size = Size(width, height))

    // Zone 1: LMR - Low Medium Density Residential (Top Left)
    val lmrColor = Color(0xFF3B82F6)
    drawRect(
        color = lmrColor.copy(alpha = 0.28f),
        topLeft = Offset(10f, 10f),
        size = Size(width * 0.48f, height * 0.45f)
    )
    drawRect(
        color = lmrColor,
        topLeft = Offset(10f, 10f),
        size = Size(width * 0.48f, height * 0.45f),
        style = Stroke(width = 1.5f)
    )

    // Zone 2: PC - Principal Centre / Commercial (Top Right)
    val pcColor = Color(0xFF06B6D4)
    drawRect(
        color = pcColor.copy(alpha = 0.28f),
        topLeft = Offset(width * 0.52f, 10f),
        size = Size(width * 0.44f, height * 0.45f)
    )
    drawRect(
        color = pcColor,
        topLeft = Offset(width * 0.52f, 10f),
        size = Size(width * 0.44f, height * 0.45f),
        style = Stroke(width = 1.5f)
    )

    // Zone 3: OS - Open Space & Environmental (Bottom Left)
    val osColor = Color(0xFF22C55E)
    drawRect(
        color = osColor.copy(alpha = 0.28f),
        topLeft = Offset(10f, height * 0.52f),
        size = Size(width * 0.48f, height * 0.40f)
    )
    drawRect(
        color = osColor,
        topLeft = Offset(10f, height * 0.52f),
        size = Size(width * 0.48f, height * 0.40f),
        style = Stroke(width = 1.5f)
    )

    // Zone 4: IN1 - Industrial (Bottom Right)
    val in1Color = Color(0xFFA855F7)
    drawRect(
        color = in1Color.copy(alpha = 0.28f),
        topLeft = Offset(width * 0.52f, height * 0.52f),
        size = Size(width * 0.44f, height * 0.40f)
    )
    drawRect(
        color = in1Color,
        topLeft = Offset(width * 0.52f, height * 0.52f),
        size = Size(width * 0.44f, height * 0.40f),
        style = Stroke(width = 1.5f)
    )

    // Separating road corridor buffer
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(0f, height * 0.47f),
        size = Size(width, height * 0.04f)
    )
    drawRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(width * 0.49f, 0f),
        size = Size(width * 0.025f, height)
    )
}

/**
 * Draw Orthorectified Aerial Imagery Base Map:
 * Multi-spectral textured simulation with buildings, canopies, and road corridors
 */
private fun DrawScope.drawAerialBaseMap(
    width: Float,
    height: Float
) {
    // 1. Natural satellite terrain background with subtle tonal gradient
    val terrainBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF14241B), // dark foliage green
            Color(0xFF1C2D22),
            Color(0xFF1A262C)  // dark riverine slate
        )
    )
    drawRect(brush = terrainBrush, size = Size(width, height))

    // 2. Asphalt road corridor with curves
    val roadPath = Path().apply {
        moveTo(0f, height * 0.48f)
        cubicTo(
            width * 0.35f, height * 0.45f,
            width * 0.65f, height * 0.55f,
            width, height * 0.50f
        )
    }
    drawPath(
        path = roadPath,
        color = Color(0xFF334155),
        style = Stroke(width = 24f)
    )
    // Road markings
    drawPath(
        path = roadPath,
        color = Color(0xFF94A3B8),
        style = Stroke(
            width = 1.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
    )

    // 3. Building Rooftop Footprints (Simulated Aerial Imagery)
    val roofColor = Color(0xFF475569)
    val roofHighlight = Color(0xFF64748B)

    // Building 1 (Top Left)
    drawRect(color = roofColor, topLeft = Offset(width * 0.12f, height * 0.16f), size = Size(width * 0.18f, height * 0.20f))
    drawRect(color = roofHighlight, topLeft = Offset(width * 0.12f, height * 0.16f), size = Size(width * 0.18f, height * 0.20f), style = Stroke(width = 1f))

    // Building 2 (Top Right)
    drawRect(color = roofColor, topLeft = Offset(width * 0.65f, height * 0.14f), size = Size(width * 0.22f, height * 0.24f))
    drawRect(color = roofHighlight, topLeft = Offset(width * 0.65f, height * 0.14f), size = Size(width * 0.22f, height * 0.24f), style = Stroke(width = 1f))

    // Building 3 (Bottom Right)
    drawRect(color = roofColor, topLeft = Offset(width * 0.60f, height * 0.68f), size = Size(width * 0.25f, height * 0.22f))
    drawRect(color = roofHighlight, topLeft = Offset(width * 0.60f, height * 0.68f), size = Size(width * 0.25f, height * 0.22f), style = Stroke(width = 1f))

    // 4. Tree Canopy Clusters (Green circles)
    val canopyColor = Color(0xFF166534).copy(alpha = 0.8f)
    drawCircle(color = canopyColor, radius = 16f, center = Offset(width * 0.38f, height * 0.22f))
    drawCircle(color = canopyColor, radius = 22f, center = Offset(width * 0.42f, height * 0.28f))
    drawCircle(color = canopyColor, radius = 18f, center = Offset(width * 0.18f, height * 0.78f))
    drawCircle(color = canopyColor, radius = 24f, center = Offset(width * 0.24f, height * 0.82f))

    // 5. Watercourse / Creek (Lockyer Creek representation)
    val creekPath = Path().apply {
        moveTo(width * 0.30f, height)
        cubicTo(
            width * 0.36f, height * 0.82f,
            width * 0.40f, height * 0.70f,
            width * 0.48f, height * 0.62f
        )
    }
    drawPath(
        path = creekPath,
        color = Color(0xFF0369A1).copy(alpha = 0.75f),
        style = Stroke(width = 12f)
    )
}

/**
 * Draw Vector Asset Overlay over the active base map
 */
private fun DrawScope.drawAssetOverlay(
    asset: Spatial7SuiteAsset?,
    width: Float,
    height: Float
) {
    if (asset == null) return

    val (assetX, assetY) = when (asset.id) {
        "AST-101" -> Pair(width * 0.50f, height * 0.50f) // Main St Pavement
        "AST-102" -> Pair(width * 0.42f, height * 0.72f) // Lockyer Creek Pipe
        "AST-103" -> Pair(width * 0.25f, height * 0.46f) // Gatton Kerb & Gutter
        else -> Pair(width * 0.5f, height * 0.5f)
    }

    // Outer glow circle
    drawCircle(
        color = Color(0xFF38BDF8).copy(alpha = 0.3f),
        radius = 24f,
        center = Offset(assetX, assetY)
    )

    // Inner pin ring
    drawCircle(
        color = Color(0xFF38BDF8),
        radius = 12f,
        center = Offset(assetX, assetY),
        style = Stroke(width = 2.5f)
    )

    // Core point
    drawCircle(
        color = if (asset.condition > 3) Color(0xFFEF4444) else Color(0xFF22C55E),
        radius = 6f,
        center = Offset(assetX, assetY)
    )

    // Crosshair lines
    drawLine(
        color = Color(0xFF38BDF8).copy(alpha = 0.6f),
        start = Offset(assetX - 20f, assetY),
        end = Offset(assetX + 20f, assetY),
        strokeWidth = 1f
    )
    drawLine(
        color = Color(0xFF38BDF8).copy(alpha = 0.6f),
        start = Offset(assetX, assetY - 20f),
        end = Offset(assetX, assetY + 20f),
        strokeWidth = 1f
    )
}
