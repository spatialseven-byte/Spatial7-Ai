package com.example.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InspectionTicket
import com.example.model.Tenant
import com.example.security.UserRole
import com.example.service.DroneDefectDetection
import com.example.service.GeminiSpatialService
import com.example.service.SpatialAiAnalysisResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpatialAiScreen(
    activeTenant: Tenant,
    geminiService: GeminiSpatialService,
    initialPrompt: String = "",
    onCreateTicket: (InspectionTicket) -> Unit,
    activeRole: UserRole = UserRole.TENANT_ADMIN,
    modifier: Modifier = Modifier
) {
    var queryText by remember(initialPrompt) {
        mutableStateOf(
            if (initialPrompt.isNotBlank()) initialPrompt
            else "Find all assets with warning or critical status within 25km of center"
        )
    }
    var isAnalyzingQuery by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<SpatialAiAnalysisResult?>(null) }

    var selectedDroneTarget by remember { mutableStateOf("Autonomous Drone Flight - Corridor Alpha") }
    var isAnalyzingDrone by remember { mutableStateOf(false) }
    var droneResult by remember { mutableStateOf<DroneDefectDetection?>(null) }
    var ticketCreatedNotice by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val presetQueries = listOf(
        "Find cattle collar virtual fence breaches in the last 2 hours",
        "List stormwater pipes submerged by 1-in-100yr flood simulation",
        "Calculate HVAC energy savings if building setpoint increases by 1.5°C",
        "Predict conveyor bearing failure from 4.8 mm/s vibration spectra"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        color = NexusPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NexusPurple,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "GEOSPATIAL AI COPILOT",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusPurple,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gemini Spatial Feature & SQL Engine",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Natural language to PostGIS SQL with strict RLS multi-tenant boundary",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = NexusBackground,
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NexusPurple.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = NexusPurple, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "TENANT: ${activeTenant.name} | ROLE: ${activeRole.roleKey}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexusPurpleLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Natural Language Spatial Query
        item {
            Text(
                text = "NATURAL LANGUAGE SPATIAL QUERY",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = queryText,
                        onValueChange = { queryText = it },
                        label = { Text("Ask any spatial query in plain English") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexusCyan,
                            unfocusedBorderColor = NexusCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Quick Spatial Templates:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetQueries.forEach { preset ->
                            SuggestionChip(
                                onClick = { queryText = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = NexusSurfaceVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isAnalyzingQuery = true
                            coroutineScope.launch {
                                analysisResult = geminiService.querySpatialDatabase(queryText, activeTenant)
                                isAnalyzingQuery = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NexusCyan),
                        enabled = !isAnalyzingQuery,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAnalyzingQuery) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NexusBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compiling PostGIS Spatial Query...", color = NexusBackground)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NexusBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute Spatial Query", color = NexusBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Analysis Result Display
        if (analysisResult != null) {
            item {
                val res = analysisResult!!
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
                            Text(
                                text = "GEOSPATIAL REASONING RESULT",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = if (res.isLiveGeminiCall) NexusEmerald.copy(alpha = 0.2f) else NexusAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (res.isLiveGeminiCall) "LIVE GEMINI API" else "SPATIAL REASONING ENGINE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (res.isLiveGeminiCall) NexusEmerald else NexusAmber,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = res.naturalLanguageExplanation, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Generated PostGIS SQL (RLS Enforced):", style = MaterialTheme.typography.labelSmall, color = NexusCyanLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = NexusBackground,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.generatedPostGisSql,
                                style = MaterialTheme.typography.bodySmall,
                                color = NexusCyan,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Function: ${res.spatialFunctionUsed}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "Severity: ${res.severityLevel}", style = MaterialTheme.typography.labelSmall, color = NexusCoral, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Recommended Field Action: ${res.recommendedFieldAction}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Section 2: Drone Photogrammetry & Defect Extraction
        item {
            Text(
                text = "DRONE PHOTOGRAMMETRY & DEFECT EXTRACTION",
                style = MaterialTheme.typography.labelSmall,
                color = NexusAmber,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Autonomous Drone Mission Imagery",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Run computer vision and multimodal spatial analysis to extract cracks, rust, washouts, and thermal anomalies.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            isAnalyzingDrone = true
                            coroutineScope.launch {
                                droneResult = geminiService.analyzeDronePhotogrammetry(
                                    selectedDroneTarget,
                                    activeTenant.sectorType,
                                    "Flight #412 - GDA2020 High Resolution Orthomosaic"
                                )
                                isAnalyzingDrone = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NexusAmber),
                        enabled = !isAnalyzingDrone,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAnalyzingDrone) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NexusBackground, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing Aerial Orthophoto...", color = NexusBackground)
                        } else {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = NexusBackground)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Extract Drone Features & Defects", color = NexusBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Drone Detection Result
        if (droneResult != null) {
            item {
                val drone = droneResult!!
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
                            Text(
                                text = "ANOMALY EXTRACTION REPORT",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexusAmber,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = NexusCoral.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "SEVERITY ${drone.severityScore}/100",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexusCoral,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = drone.assetTarget, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(text = "Defect Type: ${drone.anomalyType}", style = MaterialTheme.typography.bodySmall, color = NexusAmber)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Extracted Defects:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        drone.detectedDefects.forEach { defect ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = NexusCoral, modifier = Modifier.size(8.dp))
                                Text(text = defect, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = drone.boundingBoxSummary, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontFamily = FontFamily.Monospace)

                        Spacer(modifier = Modifier.height(12.dp))
                        if (activeRole.canWriteAssets) {
                            Button(
                                onClick = {
                                    val ticket = InspectionTicket(
                                        id = "ticket-${System.currentTimeMillis()}",
                                        tenantId = activeTenant.tenantId,
                                        assetId = "drone-anomaly-asset",
                                        assetName = drone.assetTarget,
                                        inspector = "Gemini Drone Vision AI",
                                        findings = "${drone.anomalyType}: ${drone.detectedDefects.joinToString("; ")}",
                                        priority = drone.maintenanceWorkPriority,
                                        aiConfidence = 0.94f,
                                        timestamp = "2026-09-14 05:35 AEST",
                                        resolved = false
                                    )
                                    onCreateTicket(ticket)
                                    ticketCreatedNotice = "Inspection Ticket Saved to Local Room Database (${drone.maintenanceWorkPriority})!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NexusSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = NexusCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Dispatch Official Maintenance Work Order", color = NexusCyan, fontSize = 12.sp)
                            }
                        } else {
                            Surface(
                                color = NexusCoral.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCoral.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NexusCoral)
                                    Column {
                                        Text(
                                            text = "WORK ORDER DISPATCH RESTRICTED",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NexusCoral,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Active role (${activeRole.roleKey}) possesses READ-ONLY privileges. Ticket creation is disabled.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (ticketCreatedNotice != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = ticketCreatedNotice!!, color = NexusEmerald, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
