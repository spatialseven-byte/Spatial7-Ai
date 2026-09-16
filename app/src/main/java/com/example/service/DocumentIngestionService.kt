package com.example.service

import com.example.BuildConfig
import com.example.compliance.Iso55001Engine
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class DocumentSampleTemplate(
    val title: String,
    val fileName: String,
    val fileType: String,
    val fileSizeKb: Int,
    val sectorType: SectorType,
    val summary: String,
    val rawTextPayload: String
)

class DocumentIngestionService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val sampleDocuments: List<DocumentSampleTemplate> = listOf(
        DocumentSampleTemplate(
            title = "Nerang River Stormwater Trunk Culvert #04 As-Constructed Drawing",
            fileName = "GCCC_ENG_DWG_2024_0891_RC_CULVERT.pdf",
            fileType = "PDF / Engineering CAD",
            fileSizeKb = 4280,
            sectorType = SectorType.LOCAL_GOV,
            summary = "City of Gold Coast certified engineering blueprint detailing 1800mm reinforced concrete box culvert installation at Bundall / Isle of Capri.",
            rawTextPayload = """
                CITY OF GOLD COAST - INFRASTRUCTURE AS-CONSTRUCTED RECORD
                Asset: Nerang River Stormwater Trunk Culvert #04
                Asset Tag: GCCC-SW-TRUNK-0891
                Category: Stormwater Main
                Contractor: Gold Coast Civil Engineering Pty Ltd
                Material: Spun Reinforced Concrete Class 4 (AS/NZS 4058)
                Nominal Barrel Diameter: 1800 mm
                Length: 420 meters
                Commissioning Date: 2018-04-12
                Design Lifecycle: 80 Years
                Modern Replacement Value: AUD $2,840,000
                Cadastral Lot on Plan: Lot 14 RP89210
                Spatial Reference: MGA Zone 56 (GDA2020)
                Coordinates: Lat -28.0120, Lng 153.4050
                Latest CCTV Inspection Rating: Grade 3 (Fair - Silt Accumulation 48%)
                Structural Risk Score: 68/100
            """.trimIndent()
        ),
        DocumentSampleTemplate(
            title = "Kimberley Station Solar Water Bore #12 Handover Certificate",
            fileName = "KPC_PASTORAL_BORE_12_CERT_2023.pdf",
            fileType = "PDF / Certificate",
            fileSizeKb = 1840,
            sectorType = SectorType.AGRICULTURE,
            summary = "Fitzroy Basin Pastoral Bore commissioning cert with solar pump inverter, flow meters, and LoRa telemetry tag.",
            rawTextPayload = """
                KIMBERLEY PASTORAL COMPANY - ASSET COMMISSIONING SHEET
                Asset: Solar Water Bore & Trough Array #12
                Asset Tag: KPC-BORE-SOLAR-012
                Category: Pastoral Water Infrastructure
                Manufacturer: Mono Pumps Australia & Lorentz Solar
                Commissioning Date: 2023-08-20
                Material: 316 Stainless Steel Casing with HDPE Riser
                Design Flow Rate: 42 L/min
                Static Water Level: 28.5 meters
                Inverter Power: 1.8 kW Lorentz PV
                Expected Lifecycle: 25 Years
                Replacement Valuation: AUD $185,000
                Station Sector: Paddock Alpha (Red Ridge)
                Spatial Coordinate: Lat -18.1890, Lng 125.5580
                MGA Zone: Zone 51 (GDA2020)
                Condition Rating: Grade 1 (Very Good)
                Risk Score: 18/100
            """.trimIndent()
        ),
        DocumentSampleTemplate(
            title = "Wilson Hall LOD-3 BIM Asset Schedule & Energy Cert",
            fileName = "UNIMELB_PARKVILLE_WILSON_HALL_LOD3.pdf",
            fileType = "PDF / BIM Schedule",
            fileSizeKb = 6120,
            sectorType = SectorType.EDUCATION,
            summary = "Heritage Register LOD-3 architectural schedule with central chiller connection and solar glazing specs.",
            rawTextPayload = """
                UNIVERSITY OF MELBOURNE - ESTATE PLANNING & DIGITAL TWINS
                Building: Wilson Hall LOD-3 Digital Twin
                Asset Identifier: UNIMELB-BLDG-151
                Asset Category: Academic Heritage Infrastructure
                Original Construction: 1956 | Seismic Upgrade: 2021
                Gross Floor Area: 5,200 m²
                Primary Material: Reinforced Concrete & Bavarian Quartzite
                Chilled Water Sub-Plant: 142 kW nominal demand
                Solar Glazing Array: 48.5 kWp
                Building Replacement Valuation: AUD $48,500,000
                Campus Coordinate: Lat -37.7978, Lng 144.9605
                Projection: MGA Zone 55 (GDA2020)
                Condition Rating: Grade 2 (Good)
                Occupancy Risk Score: 24/100
            """.trimIndent()
        ),
        DocumentSampleTemplate(
            title = "Berth 2 Bulk Shiploader Gantry 3 Ultrasonic NDT Report",
            fileName = "PILBARA_PORT_BERTH2_NDT_ULTRASONIC.pdf",
            fileType = "PDF / NDT Report",
            fileSizeKb = 3450,
            sectorType = SectorType.INDUSTRIAL,
            summary = "Pilbara Port maritime asset inspection report detailing flaking rust scale and steel thickness loss.",
            rawTextPayload = """
                PILBARA IRON & PORTS - NON-DESTRUCTIVE TESTING AUDIT
                Asset Target: Port Berth #02 Bulk Carrier Shiploader Gantry 3
                Asset Tag: PILB-PORT-SL-002-G3
                Category: Maritime Export Handling
                Manufacturer: Krupp Industrial Mining Cranes
                Commissioning Date: 2011-11-05
                Structural Steel Grade: AS/NZS 3678 Grade 350L15
                Design Service Life: 35 Years
                Asset Replacement Value: AUD $38,000,000
                Geographic Coordinate: Lat -20.3080, Lng 118.5720
                MGA Zone: Zone 50 (GDA2020)
                Defect Findings: Flaking Grade C rust scale across 4.2m² of structural web girder; 18% thickness reduction at coastal gusset.
                Condition Rating: Grade 4 (Poor - Urgent Maintenance Required)
                Integrity Risk Score: 89/100
            """.trimIndent()
        )
    )

    suspend fun ingestDocument(
        sample: DocumentSampleTemplate,
        tenant: Tenant
    ): IngestedDocument = withContext(Dispatchers.IO) {
        // Simulate real document OCR and vector parsing delay
        delay(1200)

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }
        val extractedMetadata = if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                callGeminiMultimodalExtraction(sample, tenant, apiKey) ?: parseTemplateOffline(sample, tenant)
            } catch (_: Exception) {
                parseTemplateOffline(sample, tenant)
            }
        } else {
            parseTemplateOffline(sample, tenant)
        }

        val complianceAudit = Iso55001Engine.auditAsset(extractedMetadata)

        return@withContext IngestedDocument(
            id = "doc-${UUID.randomUUID().toString().take(8)}",
            tenantId = tenant.tenantId,
            fileName = sample.fileName,
            fileType = sample.fileType,
            fileSizeKb = sample.fileSizeKb,
            uploadDate = "2026-09-14 06:15 AEST",
            documentTitle = sample.title,
            documentType = "ENGINEERING_AS_CONSTRUCTED",
            extractedMetadata = extractedMetadata,
            complianceAudit = complianceAudit,
            status = DocumentStatus.EXTRACTED_PENDING_REVIEW
        )
    }

    private fun callGeminiMultimodalExtraction(
        sample: DocumentSampleTemplate,
        tenant: Tenant,
        apiKey: String
    ): ExtractedAssetMetadata? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$apiKey"

        val prompt = """
            Extract engineering asset metadata from this Australian document according to ISO 55001 standards.
            Document text:
            ${sample.rawTextPayload}

            Return strict JSON format:
            {
              "asset_name": "...",
              "asset_category": "...",
              "serial_number": "...",
              "manufacturer": "...",
              "installation_date": "YYYY-MM-DD",
              "material": "...",
              "lifecycle_years": 50,
              "replacement_cost_aud": 2500000,
              "lat": -28.012,
              "lng": 153.405,
              "gda2020_zone": "MGA Zone 56",
              "cadastral_lot": "Lot 14 RP89210",
              "condition_rating": 3,
              "risk_score": 68
            }
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val text = responseJson.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text") ?: return null

        val cleanJson = if (text.contains("{") && text.contains("}")) {
            text.substring(text.indexOf("{"), text.lastIndexOf("}") + 1)
        } else text

        val p = JSONObject(cleanJson)
        return ExtractedAssetMetadata(
            assetName = p.optString("asset_name", sample.title),
            assetCategory = p.optString("asset_category", "Infrastructure"),
            sectorType = sample.sectorType,
            serialOrAssetTag = p.optString("serial_number", "TAG-AUTO-01"),
            manufacturerOrContractor = p.optString("manufacturer", "Certified Contractor"),
            installationDate = p.optString("installation_date", "2022-01-01"),
            material = p.optString("material", "Reinforced Concrete"),
            expectedLifecycleYears = p.optInt("lifecycle_years", 50),
            replacementCostAud = p.optDouble("replacement_cost_aud", 1500000.0),
            locationLat = p.optDouble("lat", tenant.centerLat),
            locationLng = p.optDouble("lng", tenant.centerLng),
            gda2020Zone = p.optString("gda2020_zone", "MGA Zone 56"),
            cadastralLotPlan = p.optString("cadastral_lot", "Lot 1 SP102"),
            conditionRating = p.optInt("condition_rating", 3),
            riskScore = p.optInt("risk_score", 45),
            fieldMetrics = mapOf("Extracted Source" to "Gemini 2.0 Multimodal API"),
            confidenceScores = mapOf(
                "assetName" to 0.98f,
                "serialOrAssetTag" to 0.94f,
                "locationLat" to 0.96f,
                "locationLng" to 0.96f,
                "material" to 0.92f,
                "replacementCostAud" to 0.88f,
                "installationDate" to 0.74f, // Triggers yellow human review badge!
                "cadastralLotPlan" to 0.71f // Triggers yellow human review badge!
            )
        )
    }

    private fun parseTemplateOffline(sample: DocumentSampleTemplate, tenant: Tenant): ExtractedAssetMetadata {
        return when (sample.sectorType) {
            SectorType.LOCAL_GOV -> ExtractedAssetMetadata(
                assetName = "Nerang River Stormwater Trunk Culvert #04",
                assetCategory = "Stormwater Main",
                sectorType = SectorType.LOCAL_GOV,
                serialOrAssetTag = "GCCC-SW-TRUNK-0891",
                manufacturerOrContractor = "Gold Coast Civil Engineering Pty Ltd",
                installationDate = "2018-04-12",
                material = "Spun Reinforced Concrete Class 4 (AS/NZS 4058)",
                expectedLifecycleYears = 80,
                replacementCostAud = 2840000.0,
                locationLat = -28.0120,
                locationLng = 153.4050,
                gda2020Zone = "MGA Zone 56",
                cadastralLotPlan = "Lot 14 RP89210",
                conditionRating = 3,
                riskScore = 68,
                fieldMetrics = mapOf(
                    "Pipe Diameter" to "1800 mm",
                    "Silt Accumulation" to "48%",
                    "Length" to "420 m"
                ),
                confidenceScores = mapOf(
                    "assetName" to 0.98f,
                    "serialOrAssetTag" to 0.95f,
                    "material" to 0.96f,
                    "locationLat" to 0.94f,
                    "locationLng" to 0.94f,
                    "gda2020Zone" to 0.92f,
                    "replacementCostAud" to 0.89f,
                    "installationDate" to 0.76f, // Below 80%: Yellow review badge
                    "cadastralLotPlan" to 0.72f  // Below 80%: Yellow review badge
                )
            )
            SectorType.AGRICULTURE -> ExtractedAssetMetadata(
                assetName = "Solar Water Bore & Trough Array #12",
                assetCategory = "Pastoral Bore",
                sectorType = SectorType.AGRICULTURE,
                serialOrAssetTag = "KPC-BORE-SOLAR-012",
                manufacturerOrContractor = "Mono Pumps & Lorentz Solar",
                installationDate = "2023-08-20",
                material = "316 Stainless Steel & HDPE Riser",
                expectedLifecycleYears = 25,
                replacementCostAud = 185000.0,
                locationLat = -18.1890,
                locationLng = 125.5580,
                gda2020Zone = "MGA Zone 51",
                cadastralLotPlan = "Lot 4 Pastoral Lease 3114",
                conditionRating = 1,
                riskScore = 18,
                fieldMetrics = mapOf(
                    "Flow Rate" to "42 L/min",
                    "Solar Array" to "1.8 kW Lorentz PV",
                    "Water Table Depth" to "28.5 m"
                ),
                confidenceScores = mapOf(
                    "assetName" to 0.99f,
                    "serialOrAssetTag" to 0.97f,
                    "material" to 0.94f,
                    "locationLat" to 0.95f,
                    "locationLng" to 0.95f,
                    "replacementCostAud" to 0.91f,
                    "installationDate" to 0.88f,
                    "cadastralLotPlan" to 0.68f // Below 80%: Yellow review badge
                )
            )
            SectorType.EDUCATION -> ExtractedAssetMetadata(
                assetName = "Wilson Hall - 3D Digital Twin LOD-3",
                assetCategory = "Building LOD-3",
                sectorType = SectorType.EDUCATION,
                serialOrAssetTag = "UNIMELB-BLDG-151",
                manufacturerOrContractor = "Estate Planning & Heritage Works",
                installationDate = "1956-06-01",
                material = "Reinforced Concrete & Bavarian Quartzite",
                expectedLifecycleYears = 120,
                replacementCostAud = 48500000.0,
                locationLat = -37.7978,
                locationLng = 144.9605,
                gda2020Zone = "MGA Zone 55",
                cadastralLotPlan = "Crown Allotment 2 Sec 44",
                conditionRating = 2,
                riskScore = 24,
                fieldMetrics = mapOf(
                    "Gross Floor Area" to "5,200 m²",
                    "Chilled Water Demand" to "142 kW",
                    "Solar PV Glazing" to "48.5 kWp"
                ),
                confidenceScores = mapOf(
                    "assetName" to 0.98f,
                    "serialOrAssetTag" to 0.96f,
                    "locationLat" to 0.97f,
                    "locationLng" to 0.97f,
                    "material" to 0.92f,
                    "replacementCostAud" to 0.87f,
                    "installationDate" to 0.72f, // Below 80%: Yellow review badge
                    "cadastralLotPlan" to 0.75f  // Below 80%: Yellow review badge
                )
            )
            SectorType.INDUSTRIAL -> ExtractedAssetMetadata(
                assetName = "Berth #02 Bulk Shiploader Gantry 3",
                assetCategory = "Bulk Material Handling",
                sectorType = SectorType.INDUSTRIAL,
                serialOrAssetTag = "PILB-PORT-SL-002-G3",
                manufacturerOrContractor = "Krupp Industrial Mining Cranes",
                installationDate = "2011-11-05",
                material = "Structural Steel AS/NZS 3678 Grade 350L15",
                expectedLifecycleYears = 35,
                replacementCostAud = 38000000.0,
                locationLat = -20.3080,
                locationLng = 118.5720,
                gda2020Zone = "MGA Zone 50",
                cadastralLotPlan = "Port Hedland Port Authority Special Lease 104",
                conditionRating = 4,
                riskScore = 89,
                fieldMetrics = mapOf(
                    "Structural Rust Grade" to "Grade C (Flaking)",
                    "Ultrasonic Thickness Loss" to "18% at Gusset",
                    "Throughput Speed" to "6.5 m/s"
                ),
                confidenceScores = mapOf(
                    "assetName" to 0.97f,
                    "serialOrAssetTag" to 0.96f,
                    "material" to 0.95f,
                    "locationLat" to 0.93f,
                    "locationLng" to 0.93f,
                    "replacementCostAud" to 0.88f,
                    "installationDate" to 0.78f, // Below 80%: Yellow review badge
                    "cadastralLotPlan" to 0.70f  // Below 80%: Yellow review badge
                )
            )
        }
    }
}
