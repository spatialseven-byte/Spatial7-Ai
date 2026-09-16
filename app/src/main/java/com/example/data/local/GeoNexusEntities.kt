package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "spatial_assets",
    indices = [Index(value = ["tenantId"]), Index(value = ["sectorType"])]
)
data class SpatialAssetEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val sectorType: String,
    val assetCategory: String,
    val geometryType: String,
    val lat: Double,
    val lng: Double,
    val polygonCoordsJson: String,
    val status: String,
    val metricsJson: String,
    val lastInspected: String,
    val heightMeters: Float,
    val areaHectares: Double
)

@Entity(
    tableName = "inspection_tickets",
    indices = [Index(value = ["tenantId"]), Index(value = ["assetId"])]
)
data class InspectionTicketEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val assetId: String,
    val assetName: String,
    val inspector: String,
    val findings: String,
    val priority: String,
    val aiConfidence: Float,
    val timestamp: String,
    val resolved: Boolean
)
