package com.example.model

data class IngestedDocument(
    val id: String,
    val tenantId: String,
    val fileName: String,
    val fileType: String, // 'PDF', 'CAD_DXF', 'DOCX', 'INVOICE'
    val fileSizeKb: Int,
    val uploadDate: String,
    val documentTitle: String,
    val documentType: String, // 'AS_CONSTRUCTED_DRAWING', 'CONDITION_INSPECTION', 'LAND_TITLE', 'ASSET_CERTIFICATE'
    val extractedMetadata: ExtractedAssetMetadata,
    val complianceAudit: Iso55001ComplianceAudit,
    val status: DocumentStatus = DocumentStatus.EXTRACTED_PENDING_REVIEW
)

enum class DocumentStatus(val label: String, val colorHex: Long) {
    UPLOADING("Uploading & Vectorizing", 0xFF64748B),
    GEMINI_EXTRACTING("Gemini Multimodal Parsing", 0xFF00E5FF),
    EXTRACTED_PENDING_REVIEW("Review Pending (Human Sign-off)", 0xFFFFB300),
    COMMITTED_TO_GIS("Committed to PostGIS DB", 0xFF00E676),
    REJECTED("Rejected Non-Conformance", 0xFFFF5252)
}

data class ExtractedAssetMetadata(
    val assetName: String,
    val assetCategory: String, // 'Stormwater Pipe', 'Pastoral Bore', 'Building LOD-3', 'Bulk Conveyor', etc.
    val sectorType: SectorType,
    val serialOrAssetTag: String,
    val manufacturerOrContractor: String,
    val installationDate: String,
    val material: String,
    val expectedLifecycleYears: Int,
    val replacementCostAud: Double,
    val locationLat: Double,
    val locationLng: Double,
    val gda2020Zone: String,
    val cadastralLotPlan: String,
    val conditionRating: Int, // 1 (Very Good) to 5 (Very Poor - Critical)
    val riskScore: Int, // 0 to 100
    val fieldMetrics: Map<String, String>,
    val confidenceScores: Map<String, Float> // field name -> confidence (0.0 to 1.0)
)

data class ComplianceRiskItem(
    val field: String,
    val standardCode: String, // e.g. "ISO 55001 §6.2", "ANZLIC-v2", "IPWEA-NAMS"
    val riskLevel: String, // "CRITICAL", "MAJOR", "MINOR"
    val description: String,
    val remediationAction: String
)

data class Iso55001ComplianceAudit(
    val auditId: String,
    val overallScore: Int, // 0 to 100%
    val status: String, // "CONFORMANT", "CONDITIONAL_CONFORMANCE", "NON_CONFORMANT"
    val anzlicSpatialStandardMet: Boolean,
    val councilAssetClassValid: Boolean,
    val financialValuationAuditMet: Boolean,
    val lifecycleHealthIndex: Int, // 0 to 100
    val missingMandatoryFields: List<String>,
    val complianceRisks: List<ComplianceRiskItem>,
    val standardsReferenced: List<String>,
    val auditTimestamp: String
)
