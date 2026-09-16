package com.example.ui.architecture

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.example.sync.*
import com.example.ui.theme.*

@Composable
fun BiDirectionalSyncEngineCard(
    onCopySql: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var engineState by remember { mutableStateOf(Spatial7SyncEngine.createInitialState()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = NexusSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SyncAlt, contentDescription = null, tint = NexusCyan)
                    Column {
                        Text(
                            text = "SPATIAL7 BI-DIRECTIONAL SYNC & AUTO-SELF-HEALING",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Target System: PostGIS / PostgreSQL Engine | Owner: Kris Lalka",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = { onCopySql(Spatial7SyncEngine.PRODUCTION_SYNC_SQL) }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy SQL", tint = NexusCyan)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Specs badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = NexusBackground,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("COORDINATE SYSTEM", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("GDA2020 / Zone 56", fontSize = 11.sp, color = NexusCyan, fontWeight = FontWeight.SemiBold)
                        Text("SRID 7856 (±0.02m)", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                Surface(
                    color = NexusBackground,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusEmerald.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("LOOP PROTECTION", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("pg_trigger_depth()", fontSize = 11.sp, color = NexusEmerald, fontWeight = FontWeight.SemiBold)
                        Text("Halts recursion > 1", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                Surface(
                    color = NexusBackground,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("SELF-HEALING", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        Text("ST_MakeValid()", fontSize = 11.sp, color = NexusPurpleLight, fontWeight = FontWeight.SemiBold)
                        Text("Auto-recovers invalid", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NexusCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LIVE POSTGRESQL TABLES STATE",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Table 1: tbl_asset_data
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(NexusCyan, CircleShape))
                            Text("tbl_asset_data", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Surface(
                            color = NexusEmerald.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = engineState.assetRecord.assetStatus,
                                color = NexusEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = engineState.assetRecord.assetName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                    Text(text = "Class: ${engineState.assetRecord.assetClass} | Condition Score: ${engineState.assetRecord.conditionScore}/5", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Owner: ${engineState.assetRecord.lastModifiedBy}", fontSize = 11.sp, color = NexusCyanLight, fontFamily = FontFamily.Monospace)
                        Text(text = "Updated: ${engineState.assetRecord.updatedAt.takeLast(16)}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Table 2: tbl_spatial_gis
            Surface(
                color = NexusBackground,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(NexusPurple, CircleShape))
                            Text("tbl_spatial_gis", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                        Surface(
                            color = if (engineState.gisRecord.isValidGeometry) NexusEmerald.copy(alpha = 0.2f) else NexusCoral.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (engineState.gisRecord.isValidGeometry) "VALID GEOM (7856)" else "INVALID GEOM",
                                color = if (engineState.gisRecord.isValidGeometry) NexusEmerald else NexusCoral,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = engineState.gisRecord.geomWkt,
                        fontWeight = FontWeight.Normal,
                        color = NexusPurpleLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2
                    )
                    Text(text = "Precision: ±${engineState.gisRecord.spatialPrecisionM}m | FK asset_id: ${engineState.gisRecord.assetId.take(13)}...", color = TextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Owner: ${engineState.gisRecord.lastModifiedBy}", fontSize = 11.sp, color = NexusCyanLight, fontFamily = FontFamily.Monospace)
                        Text(text = "Updated: ${engineState.gisRecord.updatedAt.takeLast(16)}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NexusCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "EXECUTE TRIGGER SIMULATION PROBES",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Test Case A (GIS -> Asset), Case B (Asset -> GIS), ST_MakeValid auto-heal, loop protection, and exception auto-recovery:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Button 1: Case A Valid GIS edit
                Button(
                    onClick = {
                        engineState = Spatial7SyncEngine.simulateGisUpdate(
                            currentState = engineState,
                            newGeomWkt = "LINESTRING(501250.00 6945140.00, 501330.00 6945220.00) [EPSG:7856]",
                            isCorruptOrInvalid = false,
                            author = "Kris Lal"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sync_probe_gis_to_asset")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = NexusCyan, modifier = Modifier.size(16.dp))
                            Text("Case A: Valid GIS Edit -> Sync Asset", color = NexusCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("trg_sync_gis_to_asset", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Button 2: Case A2 Corrupt Bowtie Polygon -> ST_MakeValid Auto-Heal
                Button(
                    onClick = {
                        engineState = Spatial7SyncEngine.simulateGisUpdate(
                            currentState = engineState,
                            newGeomWkt = "POLYGON((501200 6945100, 501250 6945200, 501200 6945200, 501250 6945100, 501200 6945100))",
                            isCorruptOrInvalid = true,
                            author = "Kris Lal"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusAmber.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sync_probe_auto_heal_geom")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Healing, contentDescription = null, tint = NexusAmber, modifier = Modifier.size(16.dp))
                            Text("Case A2: Corrupt Geom -> Auto-Heal (ST_MakeValid)", color = NexusAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Auto-Heal", color = NexusAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Button 3: Case B Asset Condition Update -> Propagates to GIS
                Button(
                    onClick = {
                        val newScore = if (engineState.assetRecord.conditionScore > 2) 2 else 5
                        val newStatus = if (newScore <= 2) "MAINTENANCE_REQUIRED" else "ACTIVE"
                        engineState = Spatial7SyncEngine.simulateAssetUpdate(
                            currentState = engineState,
                            newConditionScore = newScore,
                            newStatus = newStatus,
                            author = "Kris Lal"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusEmerald.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("sync_probe_asset_to_gis")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, tint = NexusEmerald, modifier = Modifier.size(16.dp))
                            Text("Case B: Asset Data Edit -> Sync GIS", color = NexusEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("trg_sync_asset_to_gis", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Row for Loop Guard & Auto-Recovery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            engineState = Spatial7SyncEngine.simulateRecursiveLoopAttempt(engineState)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("sync_probe_loop_guard")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Loop, contentDescription = null, tint = NexusPurpleLight, modifier = Modifier.size(14.dp))
                            Text("Loop Guard", color = NexusPurpleLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            engineState = Spatial7SyncEngine.simulateAnomalyRecovery(engineState)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NexusBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NexusCoral.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("sync_probe_catch_block")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = NexusCoral, modifier = Modifier.size(14.dp))
                            Text("Catch Anomaly", color = NexusCoral, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = NexusCardBorder)
            Spacer(modifier = Modifier.height(14.dp))

            // Audit Event Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENGINE SYNC AUDIT FEED",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusCyan,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { engineState = Spatial7SyncEngine.createInitialState() },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Reset Feed", fontSize = 11.sp, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                engineState.logs.take(4).forEach { log ->
                    val badgeColor = when {
                        log.isLoopPrevented -> NexusPurpleLight
                        log.wasSelfHealed -> NexusAmber
                        log.warningMessage != null -> NexusCoral
                        else -> NexusEmerald
                    }

                    Surface(
                        color = NexusBackground,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.triggerName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "depth=${log.depth}",
                                    fontSize = 10.sp,
                                    color = TextMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = log.summary,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                            if (log.warningMessage != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.warningMessage,
                                    fontSize = 10.sp,
                                    color = NexusCoral,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
