package com.example.compliance

import com.example.model.ComplianceRiskItem
import com.example.model.ExtractedAssetMetadata
import com.example.model.Iso55001ComplianceAudit
import com.example.model.SectorType
import java.util.UUID

object Iso55001Engine {

    fun auditAsset(metadata: ExtractedAssetMetadata): Iso55001ComplianceAudit {
        val missingFields = mutableListOf<String>()
        val risks = mutableListOf<ComplianceRiskItem>()
        var scoreDeductions = 0

        // 1. ISO 55001 §6.2.1 Mandatory Asset Lifecycle Identifiers
        if (metadata.serialOrAssetTag.isBlank()) {
            missingFields.add("Asset Serial Tag / Barcode")
            risks.add(
                ComplianceRiskItem(
                    field = "serialOrAssetTag",
                    standardCode = "ISO 55001 §6.2.1",
                    riskLevel = "CRITICAL",
                    description = "Missing unique asset identifier required for financial depreciation and lifecycle tracking.",
                    remediationAction = "Assign council / station asset tag barcode before GIS sync."
                )
            )
            scoreDeductions += 18
        }

        if (metadata.installationDate.isBlank()) {
            missingFields.add("Installation / Commissioning Date")
            risks.add(
                ComplianceRiskItem(
                    field = "installationDate",
                    standardCode = "ISO 55001 §6.2.2 & IPWEA NAMS+",
                    riskLevel = "MAJOR",
                    description = "Without installation date, remaining useful life (RUL) and straight-line depreciation cannot be calculated.",
                    remediationAction = "Confirm commissioning date from contractor handover certificate."
                )
            )
            scoreDeductions += 14
        }

        if (metadata.replacementCostAud <= 0.0) {
            missingFields.add("Replacement Value (AUD)")
            risks.add(
                ComplianceRiskItem(
                    field = "replacementCostAud",
                    standardCode = "AASB 116 / ISO 55001 §7.5",
                    riskLevel = "MAJOR",
                    description = "Asset lacks modern replacement cost valuation required for annual financial audits.",
                    remediationAction = "Estimate unit replacement cost using current council/Rawlinsons rate tables."
                )
            )
            scoreDeductions += 12
        }

        // 2. ANZLIC / ICSM Spatial Accuracy Standards
        val hasValidCoords = metadata.locationLat != 0.0 && metadata.locationLng != 0.0
        val anzlicPassed = hasValidCoords && metadata.gda2020Zone.isNotBlank()

        if (!hasValidCoords) {
            missingFields.add("Spatial Point Coordinate (Lat/Lng)")
            risks.add(
                ComplianceRiskItem(
                    field = "location",
                    standardCode = "ANZLIC AS/NZS ISO 19115",
                    riskLevel = "CRITICAL",
                    description = "No spatial geometry captured. Asset cannot be located in GIS Digital Twin.",
                    remediationAction = "Derive location from CAD as-built drawing or survey RTK GNSS."
                )
            )
            scoreDeductions += 25
        } else if (metadata.gda2020Zone.isBlank()) {
            risks.add(
                ComplianceRiskItem(
                    field = "gda2020Zone",
                    standardCode = "ICSM GDA2020 Standard",
                    riskLevel = "MINOR",
                    description = "Australian GDA2020 projection zone not explicitly defined; falling back to WGS84 ellipsoid.",
                    remediationAction = "Specify MGA Zone (e.g., MGA Zone 50 or Zone 56)."
                )
            )
            scoreDeductions += 5
        }

        // 3. Local Council / Sector Specific Asset Framework Validation
        var councilValid = true
        when (metadata.sectorType) {
            SectorType.LOCAL_GOV -> {
                if (metadata.cadastralLotPlan.isBlank()) {
                    risks.add(
                        ComplianceRiskItem(
                            field = "cadastralLotPlan",
                            standardCode = "Qld/NSW Local Govt Asset Framework",
                            riskLevel = "MAJOR",
                            description = "Council asset not cross-referenced to Cadastral Lot on Plan for ratepayer liability.",
                            remediationAction = "Perform spatial overlay with state digital cadastre."
                        )
                    )
                    scoreDeductions += 10
                    councilValid = false
                }
                if (!metadata.fieldMetrics.containsKey("Pipe Diameter") && metadata.assetCategory.contains("Stormwater", ignoreCase = true)) {
                    missingFields.add("Nominal Pipe Diameter (mm)")
                    scoreDeductions += 8
                    councilValid = false
                }
            }
            SectorType.AGRICULTURE -> {
                if (!metadata.fieldMetrics.containsKey("Flow Rate") && metadata.assetCategory.contains("Bore", ignoreCase = true)) {
                    missingFields.add("Bore Flow Rate (L/min)")
                    scoreDeductions += 8
                }
            }
            SectorType.EDUCATION -> {
                if (!metadata.fieldMetrics.containsKey("Gross Floor Area") && metadata.assetCategory.contains("Building", ignoreCase = true)) {
                    missingFields.add("Gross Floor Area (m²)")
                    scoreDeductions += 8
                }
            }
            SectorType.INDUSTRIAL -> {
                if (metadata.material.isBlank()) {
                    missingFields.add("Structural Material / Steel Grade")
                    scoreDeductions += 10
                }
            }
        }

        // 4. Calculate Final Compliance Score & Status
        val finalScore = (100 - scoreDeductions).coerceIn(0, 100)
        val status = when {
            finalScore >= 85 -> "CONFORMANT"
            finalScore >= 60 -> "CONDITIONAL_CONFORMANCE"
            else -> "NON_CONFORMANT"
        }

        // Asset Health Index (AHI): Combines condition rating (1-5) and risk score
        val healthIndex = ((5 - metadata.conditionRating) * 20 - (metadata.riskScore * 0.2f)).toInt().coerceIn(10, 100)

        return Iso55001ComplianceAudit(
            auditId = "audit-${UUID.randomUUID().toString().take(8)}",
            overallScore = finalScore,
            status = status,
            anzlicSpatialStandardMet = anzlicPassed,
            councilAssetClassValid = councilValid,
            financialValuationAuditMet = metadata.replacementCostAud > 0.0,
            lifecycleHealthIndex = healthIndex,
            missingMandatoryFields = missingFields,
            complianceRisks = risks,
            standardsReferenced = listOf(
                "ISO 55001:2024 (Asset Management Systems)",
                "ANZLIC / ICSM GDA2020 Spatial Metadata Standards",
                "IPWEA NAMS+ Infrastructure Financial Guidelines",
                "Australian Accounting Standard AASB 116 (Property, Plant & Equipment)"
            ),
            auditTimestamp = "2026-09-14 06:12 AEST"
        )
    }
}
