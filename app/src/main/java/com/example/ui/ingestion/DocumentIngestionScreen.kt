package com.example.ui.ingestion

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.compliance.Iso55001Engine
import com.example.model.*
import com.example.service.DocumentIngestionService
import com.example.service.DocumentSampleTemplate
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocumentIngestionScreen(
    activeTenant: Tenant,
    ingestionService: DocumentIngestionService,
    onCommitAssetToGis: (SpatialAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var currentDocument by remember { mutableStateOf<IngestedDocument?>(null) }
    var showAuditDetails by remember { mutableStateOf(false) }

    // Dynamic Form Editing State
    var formAssetName by remember { mutableStateOf("") }
    var formAssetCategory by remember { mutableStateOf("") }
    var formAssetTag by remember { mutableStateOf("") }
    var formMaterial by remember { mutableStateOf("") }
    var formInstallationDate by remember { mutableStateOf("") }
    var formReplacementCost by remember { mutableStateOf("") }
    var formLat by remember { mutableStateOf("") }
    var formLng by remember { mutableStateOf("") }
    var formGdaZone by remember { mutableStateOf("") }
    var formCadastralLot by remember { mutableStateOf("") }
    var formConditionRating by remember { mutableIntStateOf(3) }
    var formMetricKey1 by remember { mutableStateOf("") }
    var formMetricVal1 by remember { mutableStateOf("") }

    // Live update form fields when document is ingested
    LaunchedEffect(currentDocument) {
        val meta = currentDocument?.extractedMetadata
        if (meta != null) {
            formAssetName = meta.assetName
            formAssetCategory = meta.assetCategory
            formAssetTag = meta.serialOrAssetTag
            formMaterial = meta.material
            formInstallationDate = meta.installationDate
            formReplacementCost = String.format("%.0f", meta.replacementCostAud)
            formLat = String.format("%.4f", meta.locationLat)
            formLng = String.format("%.4f", meta.locationLng)
            formGdaZone = meta.gda2020Zone
            formCadastralLot = meta.cadastralLotPlan
            formConditionRating = meta.conditionRating

            val firstMetric = meta.fieldMetrics.entries.firstOrNull()
            formMetricKey1 = firstMetric?.key ?: "Nominal Parameter"
            formMetricVal1 = firstMetric?.value ?: ""
        }
    }

    var activeEngineMode by remember { mutableIntStateOf(0) } // 0: Enterprise QA/QC Engine, 1: AI Blueprint Parser

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NexusBackground)
    ) {
        // Mode Switcher Header
        Surface(
            color = NexusSurface,
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = { activeEngineMode = 0 },
                    color = if (activeEngineMode == 0) NexusCyan else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (activeEngineMode == 0) Color.Black else NexusCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Enterprise QA/QC",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeEngineMode == 0) Color.Black else TextMuted
                            )
                        }
                    }
                }

                Surface(
                    onClick = { activeEngineMode = 1 },
                    color = if (activeEngineMode == 1) NexusCyan else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = if (activeEngineMode == 1) Color.Black else NexusCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "AI Blueprint Parser",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeEngineMode == 1) Color.Black else TextMuted
                            )
                        }
                    }
                }
            }
        }

        if (activeEngineMode == 0) {
            EnterpriseAssetDataQaQcScreen(
                activeTenant = activeTenant,
                onCommitAssetToGis = onCommitAssetToGis,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(NexusBackground)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Top Header Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NexusSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, tint = NexusCyan)
                        Text(
                            text = "AI DOCUMENT INGESTION & ISO 55001 AUDITING",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexusCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Engineering Blueprint & As-Constructed Parser",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gemini Multimodal extraction with human-in-the-loop confidence verification and automated ISO 55001 compliance scoring.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Document Selection / Upload Simulator Section
        item {
            Text(
                text = "SELECT ASSET DOCUMENT FOR EXTRACTION",
                style = MaterialTheme.typography.labelSmall,
                color = NexusCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        items(ingestionService.sampleDocuments.size) { index ->
            val sample = ingestionService.sampleDocuments[index]
            val isCurrent = currentDocument?.fileName == sample.fileName
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        1.dp,
                        if (isCurrent) NexusCyan else NexusCardBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        isProcessing = true
                        coroutineScope.launch {
                            val ingested = ingestionService.ingestDocument(sample, activeTenant)
                            currentDocument = ingested
                            isProcessing = false
                            Toast.makeText(context, "Parsed ${sample.fileName} with Gemini Vision", Toast.LENGTH_SHORT).show()
                        }
                    },
                color = if (isCurrent) NexusCyan.copy(alpha = 0.08f) else NexusSurface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = sample.sectorType.accentColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = sample.sectorType.accentColor,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(text = sample.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(text = "${sample.fileName} • ${sample.fileSizeKb} KB • ${sample.fileType}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }

                    Icon(
                        imageVector = if (isCurrent) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                        contentDescription = "Ingest",
                        tint = if (isCurrent) NexusCyan else TextSecondary
                    )
                }
            }
        }

        // Processing Progress Bar
        if (isProcessing) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = NexusCyan, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        Column {
                            Text("Running Multimodal Gemini Vision Parser...", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Extracting spatial bounds, asset metadata, and evaluating ISO 55001 rules...", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                    }
                }
            }
        }

        // Dynamic Form & Verification Loop
        if (currentDocument != null) {
            val doc = currentDocument!!
            val audit = doc.complianceAudit

            // ISO 55001 Compliance Score Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, if (audit.overallScore >= 80) NexusEmerald else NexusAmber)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    imageVector = if (audit.overallScore >= 80) Icons.Default.Verified else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (audit.overallScore >= 80) NexusEmerald else NexusAmber
                                )
                                Column {
                                    Text(text = "ISO 55001 ASSET COMPLIANCE SCORE", style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text(text = "${audit.status.replace("_", " ")}", style = MaterialTheme.typography.titleMedium, color = if (audit.overallScore >= 80) NexusEmerald else NexusAmber, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                color = if (audit.overallScore >= 80) NexusEmerald.copy(alpha = 0.2f) else NexusAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${audit.overallScore}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = if (audit.overallScore >= 80) NexusEmerald else NexusAmber,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compliance Pillars Checklist
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            CompliancePillarBadge(label = "ANZLIC Spatial", passed = audit.anzlicSpatialStandardMet)
                            CompliancePillarBadge(label = "Council Specs", passed = audit.councilAssetClassValid)
                            CompliancePillarBadge(label = "AASB 116 Value", passed = audit.financialValuationAuditMet)
                            CompliancePillarBadge(label = "Health Idx (${audit.lifecycleHealthIndex})", passed = audit.lifecycleHealthIndex >= 50)
                        }

                        if (audit.missingMandatoryFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = NexusCoral.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = NexusCoral, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Missing mandatory: ${audit.missingMandatoryFields.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexusCoral,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Form Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DYNAMIC ASSET INTAKE FORM (VERIFICATION LOOP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexusCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ConfidenceLegend(color = NexusEmerald, label = ">80% Conf")
                        ConfidenceLegend(color = NexusAmber, label = "<80% Review")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = NexusSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexusCardBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Asset Name (High Conf)
                        DynamicFieldInput(
                            label = "Asset Name",
                            value = formAssetName,
                            onValueChange = { formAssetName = it },
                            confidence = doc.extractedMetadata.confidenceScores["assetName"] ?: 0.95f
                        )

                        // Asset Category & Tag
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DynamicFieldInput(
                                label = "Category",
                                value = formAssetCategory,
                                onValueChange = { formAssetCategory = it },
                                confidence = 0.95f,
                                modifier = Modifier.weight(1f)
                            )
                            DynamicFieldInput(
                                label = "Asset Tag / Serial",
                                value = formAssetTag,
                                onValueChange = { formAssetTag = it },
                                confidence = doc.extractedMetadata.confidenceScores["serialOrAssetTag"] ?: 0.95f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Material & Commissioning Date (Date is <80% confidence trigger)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DynamicFieldInput(
                                label = "Structural Material",
                                value = formMaterial,
                                onValueChange = { formMaterial = it },
                                confidence = doc.extractedMetadata.confidenceScores["material"] ?: 0.90f,
                                modifier = Modifier.weight(1.2f)
                            )
                            DynamicFieldInput(
                                label = "Installation Date",
                                value = formInstallationDate,
                                onValueChange = { formInstallationDate = it },
                                confidence = doc.extractedMetadata.confidenceScores["installationDate"] ?: 0.74f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Spatial Coordinates (GDA2020 / LatLng)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DynamicFieldInput(
                                label = "Latitude",
                                value = formLat,
                                onValueChange = { formLat = it },
                                confidence = doc.extractedMetadata.confidenceScores["locationLat"] ?: 0.95f,
                                modifier = Modifier.weight(1f)
                            )
                            DynamicFieldInput(
                                label = "Longitude",
                                value = formLng,
                                onValueChange = { formLng = it },
                                confidence = doc.extractedMetadata.confidenceScores["locationLng"] ?: 0.95f,
                                modifier = Modifier.weight(1f)
                            )
                            DynamicFieldInput(
                                label = "GDA2020 Zone",
                                value = formGdaZone,
                                onValueChange = { formGdaZone = it },
                                confidence = doc.extractedMetadata.confidenceScores["gda2020Zone"] ?: 0.92f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Cadastral Lot on Plan (<80% Review badge)
                        DynamicFieldInput(
                            label = "Cadastral Lot on Plan",
                            value = formCadastralLot,
                            onValueChange = { formCadastralLot = it },
                            confidence = doc.extractedMetadata.confidenceScores["cadastralLotPlan"] ?: 0.71f
                        )

                        // Replacement Valuation AUD
                        DynamicFieldInput(
                            label = "Replacement Valuation (AUD)",
                            value = formReplacementCost,
                            onValueChange = { formReplacementCost = it },
                            confidence = doc.extractedMetadata.confidenceScores["replacementCostAud"] ?: 0.88f
                        )

                        // Dynamic Category Specific Metric
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DynamicFieldInput(
                                label = "Category Metric",
                                value = formMetricKey1,
                                onValueChange = { formMetricKey1 = it },
                                confidence = 0.90f,
                                modifier = Modifier.weight(1f)
                            )
                            DynamicFieldInput(
                                label = "Value",
                                value = formMetricVal1,
                                onValueChange = { formMetricVal1 = it },
                                confidence = 0.90f,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Action: Commit to PostGIS Database
                        Button(
                            onClick = {
                                val lat = formLat.toDoubleOrNull() ?: activeTenant.centerLat
                                val lng = formLng.toDoubleOrNull() ?: activeTenant.centerLng

                                val spatialAsset = SpatialAsset(
                                    id = "asset-${doc.id}",
                                    tenantId = activeTenant.tenantId,
                                    name = formAssetName,
                                    sectorType = doc.extractedMetadata.sectorType,
                                    assetCategory = formAssetCategory,
                                    geometryType = if (formAssetCategory.contains("Pipe") || formAssetCategory.contains("Conveyor")) AssetGeometryType.LINESTRING
                                                   else if (formAssetCategory.contains("Building")) AssetGeometryType.BUILDING_3D
                                                   else AssetGeometryType.POINT,
                                    primaryLocation = LatLngCoord(lat, lng),
                                    polygonBounds = listOf(
                                        LatLngCoord(lat - 0.003, lng - 0.003),
                                        LatLngCoord(lat - 0.003, lng + 0.003),
                                        LatLngCoord(lat + 0.003, lng + 0.003),
                                        LatLngCoord(lat + 0.003, lng - 0.003)
                                    ),
                                    status = if (formConditionRating <= 2) AssetStatus.OPTIMAL else if (formConditionRating == 3) AssetStatus.WARNING else AssetStatus.CRITICAL,
                                    metrics = mapOf(
                                        "Asset Tag" to formAssetTag,
                                        "Material" to formMaterial,
                                        "Installed" to formInstallationDate,
                                        "Valuation" to "AUD $$formReplacementCost",
                                        "ISO 55001 Score" to "${audit.overallScore}% (${audit.status})",
                                        formMetricKey1 to formMetricVal1
                                    ),
                                    lastInspected = "Today (Verified via Gemini AI)",
                                    heightMeters = if (formAssetCategory.contains("Building")) 28f else 0f
                                )

                                onCommitAssetToGis(spatialAsset)
                                Toast.makeText(context, "Asset successfully committed to PostGIS & Room GIS Database with RLS!", Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexusEmerald),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = NexusBackground)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Commit to PostGIS & Room GIS Database", color = NexusBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
}
}

@Composable
private fun DynamicFieldInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val isHighConfidence = confidence >= 0.80f
    val badgeColor = if (isHighConfidence) NexusEmerald else NexusAmber
    val confPercent = (confidence * 100).toInt()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "$confPercent% CONF",
                    style = MaterialTheme.typography.labelSmall,
                    color = badgeColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = badgeColor,
                unfocusedBorderColor = NexusCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun CompliancePillarBadge(label: String, passed: Boolean) {
    Surface(
        color = if (passed) NexusEmerald.copy(alpha = 0.15f) else NexusCoral.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (passed) NexusEmerald else NexusCoral,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (passed) NexusEmerald else NexusCoral,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ConfidenceLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 9.sp)
    }
}
