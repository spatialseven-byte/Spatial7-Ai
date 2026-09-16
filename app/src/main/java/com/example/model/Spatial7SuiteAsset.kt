package com.example.model

/**
 * Mock Dataset linking Spatial GIS and Asset Data
 * Target System: Spatial7 Enterprise Suite
 * System Owner / Operator: Kris Lal
 */
data class Spatial7SuiteAsset(
    val id: String,
    val name: String,
    val assetClass: String,
    val subClass: String,
    val condition: Int, // 1 to 5 (NAMS / IPWEA standard)
    val value: Long,    // AUD replacement value
    val lat: Double,
    val lng: Double,
    val status: String,
    val updatedBy: String = "Kris Lal",
    val srid: Int = 7856 // GDA2020 / MGA Zone 56
)

object Spatial7MockData {
    val initialAssets = listOf(
        Spatial7SuiteAsset(
            id = "AST-101",
            name = "Main St Pavement Segment 1",
            assetClass = "Transport",
            subClass = "Pavement",
            condition = 2,
            value = 145000L,
            lat = -27.56,
            lng = 152.41,
            status = "Active",
            updatedBy = "Kris Lal"
        ),
        Spatial7SuiteAsset(
            id = "AST-102",
            name = "Lockyer Creek DN600 Pipe",
            assetClass = "Stormwater",
            subClass = "Gravity Pipe",
            condition = 4,
            value = 82000L,
            lat = -27.57,
            lng = 152.43,
            status = "Needs Inspection",
            updatedBy = "Kris Lal"
        ),
        Spatial7SuiteAsset(
            id = "AST-103",
            name = "Gatton Kerb & Gutter East",
            assetClass = "Transport",
            subClass = "Kerb & Gutter",
            condition = 1,
            value = 34000L,
            lat = -27.55,
            lng = 152.40,
            status = "Active",
            updatedBy = "Kris Lal"
        )
    )
}
