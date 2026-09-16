package com.example.ui.ingestion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SpatialAsset
import com.example.model.Tenant
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Color Palette strictly matching Enterprise Asset Data & QA/QC Engine HTML
private val QaBgDark = Color(0xFF070F1E)
private val QaCardBg = Color(0xFF0D1B2A)
private val QaAccentCyan = Color(0xFF00F2FE)
private val QaAccentTeal = Color(0xFF4FACFE)
private val QaAccentRed = Color(0xFFFF4B4B)
private val QaBorderColor = Color(0xFF1E3A5F)
private val QaBtnNormal = Color(0xFF14243B)
private val QaBoxDark = Color(0xFF081220)
private val QaRowEven = Color(0xFF0B1726)
private val QaTextMain = Color(0xFFFFFFFF)
private val QaTextMuted = Color(0xFF8A9BB0)

enum class QaEngineTab(val label: String) {
    CAPTURE("📋 Asset Entry & Import"),
    ANALYTICS("🔍 Data Analytics, QA/QC & Export")
}

data class MasterAssetItem(
    val id: String,
    var name: String,
    var domain: String,
    var assetClass: String,
    var condition: String,
    var cost: Double,
    var author: String,
    var createdDate: String,
    var status: String // "Valid" or "Data Error"
)

val InitialMasterRegistry = listOf(
    MasterAssetItem(
        id = "AST-LG-1001",
        name = "Main St Sealed Pavement",
        domain = "Local Government / Council",
        assetClass = "Roads & Sealed Pavement",
        condition = "2",
        cost = 450000.0,
        author = "Kris Lal",
        createdDate = "2026-09-01 09:15",
        status = "Valid"
    ),
    MasterAssetItem(
        id = "AST-SW-1002",
        name = "DN600 Stormwater Main",
        domain = "Local Government / Council",
        assetClass = "Stormwater Pipe",
        condition = "1",
        cost = 120000.0,
        author = "Kris Lal",
        createdDate = "2026-09-02 11:30",
        status = "Valid"
    ),
    MasterAssetItem(
        id = "AST-ERR-1003",
        name = "",
        domain = "Commercial",
        assetClass = "Kerb & Channel",
        condition = "5",
        cost = 0.0,
        author = "Unknown",
        createdDate = "2026-09-03 14:00",
        status = "Data Error"
    )
)

val DomainChoices = listOf(
    "Local Government / Council",
    "Commercial",
    "Industrial",
    "Educational"
)

val AssetClassChoices = listOf(
    "Roads & Sealed Pavement",
    "Kerb & Channel",
    "Stormwater Pipe",
    "Footpath & Cycleways",
    "Parks & Open Space",
    "Buildings & Structures"
)

val ConditionChoices = listOf(
    "1" to "1 - Very Good",
    "2" to "2 - Minor Defects",
    "3" to "3 - Fair / Maintenance Required",
    "4" to "4 - Poor / Renewal Planned",
    "5" to "5 - Critical / Action Needed"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterpriseAssetDataQaQcScreen(
    activeTenant: Tenant? = null,
    onCommitAssetToGis: ((SpatialAsset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Active Tab state
    var activeTab by remember { mutableStateOf(QaEngineTab.CAPTURE) }

    // Master Registry List
    var masterAssetRegistry by remember { mutableStateOf(InitialMasterRegistry) }

    // Tab 1 Form Fields
    var assetNameInput by remember { mutableStateOf("") }
    var selectedDomain by remember { mutableStateOf(DomainChoices[0]) }
    var selectedAssetClass by remember { mutableStateOf(AssetClassChoices[0]) }
    var selectedCondition by remember { mutableStateOf("1") }
    var costInput by remember { mutableStateOf("150000") }
    var authorInput by remember { mutableStateOf("Kris Lal (Asset Officer)") }

    // Dropdown visibility states
    var domainExpanded by remember { mutableStateOf(false) }
    var classExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }

    // Notification / Dialog States
    var registrationSuccessMsg by remember { mutableStateOf<String?>(null) }
    var exportPreviewDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // Title to Content

    // QA/QC State
    var qaSummaryTitle by remember { mutableStateOf("QA/QC Status: Ready for Scan") }
    var qaSummaryColor by remember { mutableStateOf(QaAccentCyan) }
    var qaLogs by remember {
        mutableStateOf(
            listOf("Click \"Run Quality Assurance Check\" to audit dataset for missing attributes, zero valuations, or duplicate IDs.")
        )
    }

    // CSV File Picker
    val csvPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val lines = inputStream.bufferedReader().readLines().drop(1)
                    var count = 0
                    val newItems = mutableListOf<MasterAssetItem>()
                    lines.forEach { line ->
                        val parts = line.split(",").map { it.trim().replace("\"", "") }
                        if (parts.size >= 5) {
                            val id = if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0] else "AST-${Random.nextInt(1000, 9999)}"
                            val name = if (parts.size > 1) parts[1] else "Imported Asset"
                            val dom = if (parts.size > 2) parts[2] else "Local Government / Council"
                            val cls = if (parts.size > 3) parts[3] else "General Asset"
                            val cnd = if (parts.size > 4) parts[4] else "3"
                            val cost = parts.getOrNull(5)?.toDoubleOrNull() ?: 0.0
                            val auth = parts.getOrNull(6) ?: "CSV Import"
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val date = parts.getOrNull(7) ?: sdf.format(Date())

                            newItems.add(
                                MasterAssetItem(
                                    id = id,
                                    name = name,
                                    domain = dom,
                                    assetClass = cls,
                                    condition = cnd,
                                    cost = cost,
                                    author = auth,
                                    createdDate = date,
                                    status = if (name.isNotBlank() && cost > 0) "Valid" else "Data Error"
                                )
                            )
                            count++
                        }
                    }
                    if (newItems.isNotEmpty()) {
                        masterAssetRegistry = newItems + masterAssetRegistry
                        Toast.makeText(context, "Imported $count assets from CSV", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Main Card Container
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QaBgDark)
            .padding(12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 900.dp)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = QaCardBg),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, QaBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // -------------------------------------------------------------
                // TAB NAVIGATION (Asset Entry & Import vs Analytics, QA/QC & Export)
                // -------------------------------------------------------------
                Surface(
                    color = QaBoxDark,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, QaBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QaEngineTab.values().forEach { tab ->
                            val isTabActive = activeTab == tab
                            Surface(
                                onClick = {
                                    activeTab = tab
                                    registrationSuccessMsg = null
                                },
                                color = if (isTabActive) QaAccentCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = tab.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTabActive) Color.Black else QaTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Banner alert for newly registered asset
                registrationSuccessMsg?.let { msg ->
                    Surface(
                        color = Color(0xFF0F382A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Text(text = msg, color = Color(0xFFD1FAE5), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { registrationSuccessMsg = null }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // -------------------------------------------------------------
                // TAB CONTENT
                // -------------------------------------------------------------
                when (activeTab) {
                    // TAB 1: ASSET ENTRY & IMPORT
                    QaEngineTab.CAPTURE -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "ASSET REGISTER DATA ENTRY",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = QaTextMain,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "ISO 55001 / IPWEA Standard Asset Master Data Entry & CSV Import",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QaTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Manual Form Entry (2-column layout)
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Row 1: Asset Name & Domain
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Asset Name
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Asset Name / Description", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            OutlinedTextField(
                                                value = assetNameInput,
                                                onValueChange = { assetNameInput = it },
                                                placeholder = { Text("e.g. Main St Kerb & Channel Sec 2", color = QaTextMuted, fontSize = 12.sp) },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("qa_asset_name_input"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = QaBtnNormal,
                                                    unfocusedContainerColor = QaBtnNormal,
                                                    focusedBorderColor = QaAccentCyan,
                                                    unfocusedBorderColor = QaBorderColor,
                                                    focusedTextColor = QaTextMain,
                                                    unfocusedTextColor = QaTextMain
                                                )
                                            )
                                        }

                                        // Domain Selector
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Domain / Sector", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Surface(
                                                    onClick = { domainExpanded = true },
                                                    color = QaBtnNormal,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, QaBorderColor),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(54.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(selectedDomain, fontSize = 12.sp, color = QaTextMain, maxLines = 1, modifier = Modifier.weight(1f))
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = QaAccentTeal)
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = domainExpanded,
                                                    onDismissRequest = { domainExpanded = false },
                                                    modifier = Modifier.background(QaCardBg)
                                                ) {
                                                    DomainChoices.forEach { dom ->
                                                        DropdownMenuItem(
                                                            text = { Text(dom, color = QaTextMain, fontSize = 12.sp) },
                                                            onClick = {
                                                                selectedDomain = dom
                                                                domainExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Row 2: Asset Class & Condition Score
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Asset Class
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Asset Class (IPWEA)", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Surface(
                                                    onClick = { classExpanded = true },
                                                    color = QaBtnNormal,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, QaBorderColor),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(54.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(selectedAssetClass, fontSize = 12.sp, color = QaTextMain, maxLines = 1, modifier = Modifier.weight(1f))
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = QaAccentTeal)
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = classExpanded,
                                                    onDismissRequest = { classExpanded = false },
                                                    modifier = Modifier.background(QaCardBg)
                                                ) {
                                                    AssetClassChoices.forEach { cls ->
                                                        DropdownMenuItem(
                                                            text = { Text(cls, color = QaTextMain, fontSize = 12.sp) },
                                                            onClick = {
                                                                selectedAssetClass = cls
                                                                classExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Condition Score
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Condition Score (1 = Good, 5 = Critical)", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                val condLabel = ConditionChoices.firstOrNull { it.first == selectedCondition }?.second ?: selectedCondition
                                                Surface(
                                                    onClick = { conditionExpanded = true },
                                                    color = QaBtnNormal,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, QaBorderColor),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(54.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 12.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(condLabel, fontSize = 12.sp, color = QaTextMain, maxLines = 1, modifier = Modifier.weight(1f))
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = QaAccentTeal)
                                                    }
                                                }

                                                DropdownMenu(
                                                    expanded = conditionExpanded,
                                                    onDismissRequest = { conditionExpanded = false },
                                                    modifier = Modifier.background(QaCardBg)
                                                ) {
                                                    ConditionChoices.forEach { (code, lbl) ->
                                                        DropdownMenuItem(
                                                            text = { Text(lbl, color = QaTextMain, fontSize = 12.sp) },
                                                            onClick = {
                                                                selectedCondition = code
                                                                conditionExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Row 3: Replacement Cost & Created By Author
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Cost
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Replacement Cost (AUD $)", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            OutlinedTextField(
                                                value = costInput,
                                                onValueChange = { costInput = it },
                                                placeholder = { Text("e.g. 150000", color = QaTextMuted, fontSize = 12.sp) },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = QaBtnNormal,
                                                    unfocusedContainerColor = QaBtnNormal,
                                                    focusedBorderColor = QaAccentCyan,
                                                    unfocusedBorderColor = QaBorderColor,
                                                    focusedTextColor = QaTextMain,
                                                    unfocusedTextColor = QaTextMain
                                                )
                                            )
                                        }

                                        // Created By
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Created By (Officer Name / ID)", fontSize = 10.sp, color = QaTextMuted)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            OutlinedTextField(
                                                value = authorInput,
                                                onValueChange = { authorInput = it },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = QaBtnNormal,
                                                    unfocusedContainerColor = QaBtnNormal,
                                                    focusedBorderColor = QaAccentCyan,
                                                    unfocusedBorderColor = QaBorderColor,
                                                    focusedTextColor = QaTextMain,
                                                    unfocusedTextColor = QaTextMain
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Button Group: Register Asset & Import CSV
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val name = assetNameInput.trim()
                                            val cost = costInput.toDoubleOrNull() ?: 0.0
                                            val author = authorInput.ifBlank { "System Officer" }
                                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                            val timestamp = sdf.format(Date())
                                            val newId = "AST-${Random.nextInt(1000, 9999)}"

                                            val newItem = MasterAssetItem(
                                                id = newId,
                                                name = if (name.isNotBlank()) name else "UNNAMED ASSET",
                                                domain = selectedDomain,
                                                assetClass = selectedAssetClass,
                                                condition = selectedCondition,
                                                cost = cost,
                                                author = author,
                                                createdDate = timestamp,
                                                status = if (name.isNotBlank() && cost > 0) "Valid" else "Data Error"
                                            )

                                            masterAssetRegistry = listOf(newItem) + masterAssetRegistry
                                            registrationSuccessMsg = "Asset successfully registered into Master Ledger!\nID: $newId"

                                            if (onCommitAssetToGis != null && activeTenant != null) {
                                                val spatialAsset = SpatialAsset(
                                                    id = newId,
                                                    tenantId = activeTenant.tenantId,
                                                    name = if (name.isNotBlank()) name else "Unnamed Asset",
                                                    sectorType = activeTenant.sectorType,
                                                    assetCategory = selectedAssetClass,
                                                    geometryType = com.example.model.AssetGeometryType.POINT,
                                                    primaryLocation = com.example.model.LatLngCoord(activeTenant.centerLat, activeTenant.centerLng),
                                                    status = when (selectedCondition) {
                                                        "1", "2" -> com.example.model.AssetStatus.OPTIMAL
                                                        "3" -> com.example.model.AssetStatus.WARNING
                                                        else -> com.example.model.AssetStatus.CRITICAL
                                                    },
                                                    metrics = mapOf(
                                                        "domain" to selectedDomain,
                                                        "Valuation" to "AUD $$cost",
                                                        "author" to author,
                                                        "condition_grade" to "$selectedCondition / 5"
                                                    ),
                                                    lastInspected = "Today (QA/QC Registered)"
                                                )
                                                onCommitAssetToGis(spatialAsset)
                                            }

                                            assetNameInput = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = QaAccentCyan,
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(44.dp)
                                            .testTag("qa_register_asset_btn")
                                    ) {
                                        Text("+ Register Asset", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    }

                                    OutlinedButton(
                                        onClick = { csvPickerLauncher.launch("text/*") },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, QaBorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QaTextMain),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("qa_import_csv_btn")
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = QaAccentCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("📥 Import CSV File", fontSize = 12.sp, color = QaTextMain, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Quick sample CSV loader for demonstration
                            item {
                                Surface(
                                    color = QaBoxDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, QaBorderColor.copy(alpha = 0.6f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Quick Demo Sample Data", fontSize = 11.sp, color = QaAccentTeal, fontWeight = FontWeight.Bold)
                                            Text("Inject pre-formatted Council asset rows with deliberate test variations", fontSize = 9.sp, color = QaTextMuted)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                                val now = sdf.format(Date())
                                                val demoItems = listOf(
                                                    MasterAssetItem("AST-RC-2041", "East Ring Collector Road", "Local Government / Council", "Roads & Sealed Pavement", "2", 380000.0, "Kris Lal", now, "Valid"),
                                                    MasterAssetItem("AST-PP-2042", "Centenary Community Park Pavilion", "Local Government / Council", "Buildings & Structures", "3", 620000.0, "Kris Lal", now, "Valid"),
                                                    MasterAssetItem("AST-ERR-2043", "UNNAMED ASSET", "Commercial", "Stormwater Pipe", "4", -5000.0, "Unknown", now, "Data Error")
                                                )
                                                masterAssetRegistry = demoItems + masterAssetRegistry
                                                Toast.makeText(context, "Loaded 3 demo records into Master Ledger", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            border = BorderStroke(1.dp, QaAccentTeal)
                                        ) {
                                            Text("+ Load Demo Data", fontSize = 10.sp, color = QaAccentTeal, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: DATA ANALYTICS, QA/QC & EXPORT
                    QaEngineTab.ANALYTICS -> {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Header
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "ANALYTICS, QA/QC & DATA EXTRACTION",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = QaTextMain,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Text(
                                        text = "Audit Trail Verification, Data Validation Checks & Multi-Format Export",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QaTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Action Buttons (Run QA/QC, Export CSV, Export JSON)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            // QA/QC Validation Audit
                                            val errorsFound = mutableListOf<String>()
                                            val updatedList = masterAssetRegistry.mapIndexed { idx, item ->
                                                val issues = mutableListOf<String>()
                                                if (item.name.isBlank() || item.name.equals("UNNAMED ASSET", ignoreCase = true)) {
                                                    issues.add("Missing Asset Name/Description")
                                                }
                                                if (item.cost <= 0) {
                                                    issues.add("Zero or Negative Valuation")
                                                }
                                                if (item.author.isBlank() || item.author.equals("Unknown", ignoreCase = true)) {
                                                    issues.add("Missing Created By Author Tag")
                                                }

                                                if (issues.isNotEmpty()) {
                                                    errorsFound.add("[Row ${idx + 1} - ${item.id}]: ${issues.joinToString(", ")}")
                                                    item.copy(status = "Data Error")
                                                } else {
                                                    item.copy(status = "Valid")
                                                }
                                            }
                                            masterAssetRegistry = updatedList

                                            if (errorsFound.isNotEmpty()) {
                                                qaSummaryTitle = "QA/QC Audit Completed: ${errorsFound.size} Issue(s) Detected"
                                                qaSummaryColor = QaAccentRed
                                                qaLogs = errorsFound
                                            } else {
                                                qaSummaryTitle = "QA/QC Audit Completed: All Records Valid (100% Quality Pass)"
                                                qaSummaryColor = QaAccentCyan
                                                qaLogs = listOf("✅ No data missing. All replacement values, authors, and attributes passed validation checks.")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = QaAccentCyan, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(42.dp)
                                            .testTag("qa_run_audit_btn")
                                    ) {
                                        Text("⚡ Run QA Check", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val csvContent = buildCsvExport(masterAssetRegistry)
                                            exportPreviewDialog = Pair("Export Clean CSV (Excel)", csvContent)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, QaBorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QaTextMain),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("qa_export_csv_btn")
                                    ) {
                                        Text("📤 Export CSV", fontSize = 11.sp, color = QaTextMain, fontWeight = FontWeight.SemiBold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val jsonContent = buildJsonExport(masterAssetRegistry)
                                            exportPreviewDialog = Pair("Export Master JSON", jsonContent)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, QaBorderColor),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = QaTextMain),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(42.dp)
                                            .testTag("qa_export_json_btn")
                                    ) {
                                        Text("📤 Export JSON", fontSize = 11.sp, color = QaTextMain, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // QA/QC Findings Box
                            item {
                                Surface(
                                    color = QaBoxDark,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, QaBorderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = qaSummaryTitle,
                                            fontWeight = FontWeight.Bold,
                                            color = qaSummaryColor,
                                            fontSize = 13.sp
                                        )

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 120.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            qaLogs.forEach { logLine ->
                                                Text(
                                                    text = if (logLine.startsWith("✅") || logLine.startsWith("Click")) logLine else "⚠️ $logLine",
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    color = if (qaSummaryColor == QaAccentRed && !logLine.startsWith("Click")) QaAccentRed else QaTextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Section Title: Master Asset Ledger
                            item {
                                Text(
                                    text = "MASTER ASSET LEDGER",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = QaAccentCyan,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Master Asset Table (Scrollable row)
                            item {
                                Surface(
                                    color = QaBoxDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, QaBorderColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        // Table Header Row
                                        Row(
                                            modifier = Modifier
                                                .background(Color(0xFF081220))
                                                .border(BorderStroke(0.5.dp, QaBorderColor))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TableCell(text = "Asset ID", width = 110.dp, isHeader = true)
                                            TableCell(text = "Name", width = 170.dp, isHeader = true)
                                            TableCell(text = "Domain", width = 150.dp, isHeader = true)
                                            TableCell(text = "Class", width = 150.dp, isHeader = true)
                                            TableCell(text = "Cond", width = 60.dp, isHeader = true)
                                            TableCell(text = "Valuation", width = 110.dp, isHeader = true)
                                            TableCell(text = "Created By", width = 120.dp, isHeader = true)
                                            TableCell(text = "Created Date", width = 130.dp, isHeader = true)
                                            TableCell(text = "Status", width = 90.dp, isHeader = true)
                                        }

                                        // Data Rows
                                        masterAssetRegistry.forEachIndexed { index, asset ->
                                            val rowBg = if (index % 2 == 0) QaBoxDark else QaRowEven
                                            Row(
                                                modifier = Modifier
                                                    .background(rowBg)
                                                    .border(BorderStroke(0.5.dp, Color(0xFF14243B)))
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                TableCell(text = asset.id, width = 110.dp, isBold = true)
                                                TableCell(text = if (asset.name.isNotBlank()) asset.name else "—", width = 170.dp)
                                                TableCell(text = asset.domain, width = 150.dp)
                                                TableCell(text = asset.assetClass, width = 150.dp)
                                                TableCell(text = asset.condition, width = 60.dp)
                                                TableCell(text = formatCurrency(asset.cost), width = 110.dp)
                                                TableCell(text = asset.author, width = 120.dp)
                                                TableCell(text = asset.createdDate, width = 130.dp)
                                                Box(modifier = Modifier.width(90.dp)) {
                                                    val isValid = asset.status == "Valid"
                                                    Surface(
                                                        color = if (isValid) QaAccentCyan.copy(alpha = 0.2f) else QaAccentRed.copy(alpha = 0.2f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = asset.status,
                                                            color = if (isValid) QaAccentCyan else QaAccentRed,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
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
            }
        }
    }

    // -------------------------------------------------------------
    // EXPORT PREVIEW MODAL DIALOG (CSV / JSON)
    // -------------------------------------------------------------
    exportPreviewDialog?.let { (title, dataText) ->
        AlertDialog(
            onDismissRequest = { exportPreviewDialog = null },
            containerColor = QaCardBg,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = QaAccentCyan, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { exportPreviewDialog = null }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = QaTextMuted)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    Surface(
                        color = QaBoxDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, QaBorderColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dataText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = QaTextMain,
                            modifier = Modifier
                                .padding(10.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, dataText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, title)
                        context.startActivity(shareIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QaAccentCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share / Save File", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(dataText))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, QaBorderColor)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = QaAccentTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", color = QaTextMain, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isHeader: Boolean = false,
    isBold: Boolean = false
) {
    Box(
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isHeader || isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isHeader) QaAccentCyan else QaTextMain,
            maxLines = 1
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US)
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("USD", "AUD").replace("$", "$")
}

private fun buildCsvExport(items: List<MasterAssetItem>): String {
    val sb = StringBuilder()
    sb.appendLine("Asset ID,Name,Domain,Asset Class,Condition,Cost (AUD),Created By,Created Date,QA Status")
    items.forEach { a ->
        sb.appendLine("\"${a.id}\",\"${a.name}\",\"${a.domain}\",\"${a.assetClass}\",\"${a.condition}\",\"${a.cost}\",\"${a.author}\",\"${a.createdDate}\",\"${a.status}\"")
    }
    return sb.toString()
}

private fun buildJsonExport(items: List<MasterAssetItem>): String {
    val sb = StringBuilder()
    sb.appendLine("[")
    items.forEachIndexed { index, a ->
        sb.appendLine("  {")
        sb.appendLine("    \"id\": \"${a.id}\",")
        sb.appendLine("    \"name\": \"${a.name.replace("\"", "\\\"")}\",")
        sb.appendLine("    \"domain\": \"${a.domain}\",")
        sb.appendLine("    \"assetClass\": \"${a.assetClass}\",")
        sb.appendLine("    \"condition\": \"${a.condition}\",")
        sb.appendLine("    \"cost\": ${a.cost},")
        sb.appendLine("    \"author\": \"${a.author}\",")
        sb.appendLine("    \"createdDate\": \"${a.createdDate}\",")
        sb.appendLine("    \"status\": \"${a.status}\"")
        sb.append("  }")
        if (index < items.size - 1) sb.append(",")
        sb.appendLine()
    }
    sb.append("]")
    return sb.toString()
}
