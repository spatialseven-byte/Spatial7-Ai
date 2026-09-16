package com.example.model

/**
 * Base Map Layer definition for Spatial7 Enterprise Suite
 * Supports Cadastral, Zoning, and Aerial Imagery base layers.
 */
enum class Spatial7BaseMap(
    val id: String,
    val displayName: String,
    val shortCode: String,
    val description: String,
    val dataSource: String,
    val resolution: String,
    val iconEmoji: String
) {
    CADASTRAL(
        id = "cadastral",
        displayName = "Cadastral",
        shortCode = "CAD",
        description = "Legal property boundaries, lot/plan numbers, easements, and road reserves",
        dataSource = "QLD DCDB Cadastral Fabric / GDA2020",
        resolution = "±0.02m Vector Cadastre",
        iconEmoji = "📐"
    ),
    ZONING(
        id = "zoning",
        displayName = "Zoning",
        shortCode = "ZONE",
        description = "Statutory planning schemes, land use zones, building height limits & overlays",
        dataSource = "Lockyer Valley Planning Scheme v4.2",
        resolution = "Statutory Cadastral Zones",
        iconEmoji = "🏷️"
    ),
    AERIAL(
        id = "aerial",
        displayName = "Aerial Imagery",
        shortCode = "ORTHO",
        description = "High-resolution orthorectified multi-spectral aerial photogrammetry",
        dataSource = "Lockyer LGA 5cm Orthophoto Survey",
        resolution = "0.05m GSD Multi-Spectral",
        iconEmoji = "🛰️"
    );

    companion object {
        val default = CADASTRAL

        fun fromId(id: String): Spatial7BaseMap {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CADASTRAL
        }
    }
}

/**
 * Cadastral parcel feature definition for visual rendering
 */
data class CadastralParcel(
    val lotPlan: String,
    val streetAddress: String,
    val areaSqm: Double,
    val frontageM: Double,
    val easementPresent: Boolean
)

/**
 * Planning zoning category definition
 */
data class PlanningZoneInfo(
    val code: String,
    val name: String,
    val category: String,
    val maxBuildingHeightM: Double,
    val colorHex: Long
)

object Spatial7BaseMapCatalog {
    val sampleParcels = listOf(
        CadastralParcel("LOT 101 / RP89420", "14 Main St, Gatton", 845.0, 24.2, false),
        CadastralParcel("LOT 102 / RP89420", "16 Main St, Gatton", 912.0, 26.5, true),
        CadastralParcel("LOT 103 / RP89420", "18 Main St, Gatton", 1040.0, 30.0, false),
        CadastralParcel("LOT 201 / SP14205", "Lockyer Creek Corridor", 14200.0, 180.0, true)
    )

    val sampleZones = listOf(
        PlanningZoneInfo("LMR", "Low-Medium Density Residential", "Residential", 9.5, 0xFF3B82F6),
        PlanningZoneInfo("PC", "Principal Centre / Commercial", "Business", 15.0, 0xFF06B6D4),
        PlanningZoneInfo("OS", "Open Space & Conservation", "Recreation", 4.0, 0xFF22C55E),
        PlanningZoneInfo("IN1", "Low Impact Industry", "Industrial", 12.0, 0xFFA855F7)
    )
}
