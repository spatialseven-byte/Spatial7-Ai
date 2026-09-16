package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.SectorAgriColor
import com.example.ui.theme.SectorCouncilColor
import com.example.ui.theme.SectorEduColor
import com.example.ui.theme.SectorIndusColor

enum class SectorType(
    val title: String,
    val shortName: String,
    val description: String,
    val accentColor: Color
) {
    AGRICULTURE(
        title = "Agriculture & Pastoral",
        shortName = "Agri-Pastoral",
        description = "Virtual fencing, cattle collar GPS, soil moisture IoT, NDVI crop health",
        accentColor = SectorAgriColor
    ),
    LOCAL_GOV(
        title = "Local Government & Environment",
        shortName = "Local Council",
        description = "Municipal infrastructure, flood/bushfire risk simulation, citizen works",
        accentColor = SectorCouncilColor
    ),
    EDUCATION(
        title = "Educational (Campus Digital Twin)",
        shortName = "Campus Twin",
        description = "Hyper-local 3D digital twins, indoor HVAC efficiency, campus asset telemetry",
        accentColor = SectorEduColor
    ),
    INDUSTRIAL(
        title = "Industrial & Commercial",
        shortName = "Industrial Port",
        description = "Predictive maintenance, drone photogrammetry rust detection, supply chain",
        accentColor = SectorIndusColor
    );

    val icon: ImageVector
        get() = when (this) {
            AGRICULTURE -> Icons.Default.Agriculture
            LOCAL_GOV -> Icons.Default.LocationCity
            EDUCATION -> Icons.Default.Apartment
            INDUSTRIAL -> Icons.Default.Factory
        }
}

data class Tenant(
    val tenantId: String,
    val name: String,
    val sectorType: SectorType,
    val stateCode: String,
    val region: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultZoom: Float,
    val description: String,
    val activeAssetsCount: Int,
    val activeSensorsCount: Int,
    val iotStreamRate: String,
    val complianceStatus: String = "PostGIS RLS Cryptographically Enforced",
    val councilLga: String = ""
)

enum class AssetGeometryType {
    POINT,
    POLYGON,
    LINESTRING,
    BUILDING_3D
}

enum class AssetStatus(val label: String, val colorHex: Long) {
    OPTIMAL("Optimal", 0xFF10B981),
    WARNING("Warning", 0xFFFFB703),
    CRITICAL("Breach / Alert", 0xFFEF4444),
    MAINTENANCE("In Service", 0xFF00E5FF)
}

data class LatLngCoord(
    val lat: Double,
    val lng: Double,
    val elevationMeters: Double = 0.0
)

data class SpatialAsset(
    val id: String,
    val tenantId: String,
    val name: String,
    val sectorType: SectorType,
    val assetCategory: String,
    val geometryType: AssetGeometryType,
    val primaryLocation: LatLngCoord,
    val polygonBounds: List<LatLngCoord> = emptyList(),
    val status: AssetStatus,
    val metrics: Map<String, String>,
    val lastInspected: String,
    val heightMeters: Float = 0f,
    val areaHectares: Double = 0.0
)

data class IoTSensorTelemetry(
    val sensorId: String,
    val tenantId: String,
    val label: String,
    val sensorType: String,
    val lat: Double,
    val lng: Double,
    val currentValue: Double,
    val unit: String,
    val status: AssetStatus,
    val lastUpdated: String,
    val sparklineValues: List<Double>
)

data class RiskSimulationScenario(
    val id: String,
    val title: String,
    val hazardType: String,
    val severityIndex: Int, // 0 to 100
    val riskLevel: String,
    val affectedAreaHectares: Double,
    val triggerCondition: String,
    val mitigationProtocol: String,
    val perimeterPolygon: List<LatLngCoord>
)

data class InspectionTicket(
    val id: String,
    val tenantId: String,
    val assetId: String,
    val assetName: String,
    val inspector: String,
    val findings: String,
    val priority: String,
    val aiConfidence: Float,
    val timestamp: String,
    val resolved: Boolean = false
)
