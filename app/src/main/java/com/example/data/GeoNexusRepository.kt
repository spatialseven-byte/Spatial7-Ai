package com.example.data

import com.example.data.local.GeoNexusDatabase
import com.example.data.local.InspectionTicketEntity
import com.example.data.local.SpatialAssetEntity
import com.example.model.AssetGeometryType
import com.example.model.AssetStatus
import com.example.model.InspectionTicket
import com.example.model.IoTSensorTelemetry
import com.example.model.LatLngCoord
import com.example.model.RiskSimulationScenario
import com.example.model.SectorType
import com.example.model.SeedData
import com.example.model.SpatialAsset
import com.example.model.Tenant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class GeoNexusRepository(private val database: GeoNexusDatabase) {

    fun getTenants(): List<Tenant> = SeedData.TENANTS

    suspend fun preloadTenantDataIfEmpty(tenantId: String) = withContext(Dispatchers.IO) {
        val count = database.spatialAssetDao().countByTenant(tenantId)
        if (count == 0) {
            val domainAssets = SeedData.getAssetsForTenant(tenantId)
            val entities = domainAssets.map { it.toEntity() }
            database.spatialAssetDao().insertAssets(entities)

            // Seed an inspection ticket
            val sampleTicket = InspectionTicketEntity(
                id = "ticket-${tenantId}-init",
                tenantId = tenantId,
                assetId = domainAssets.firstOrNull()?.id ?: "gen-01",
                assetName = domainAssets.firstOrNull()?.name ?: "Infrastructure Asset",
                inspector = "Autonomous Drone Sentinel #07",
                findings = "Routine thermal LiDAR & multispectral scan complete. Geometric tolerances within 99.4% bounds.",
                priority = "P3 - Normal",
                aiConfidence = 0.96f,
                timestamp = "2026-09-14 05:20 AEST",
                resolved = false
            )
            database.inspectionTicketDao().insertTicket(sampleTicket)
        }
    }

    fun getSpatialAssetsFlow(tenantId: String): Flow<List<SpatialAsset>> {
        return database.spatialAssetDao().getAssetsByTenant(tenantId).map { entities ->
            if (entities.isEmpty()) {
                // Fallback to in-memory seed if Room is synchronizing
                SeedData.getAssetsForTenant(tenantId)
            } else {
                entities.map { it.toDomain() }
            }
        }
    }

    fun getSensorsForTenant(tenantId: String): List<IoTSensorTelemetry> {
        return SeedData.getSensorsForTenant(tenantId)
    }

    fun getRiskScenarios(tenantId: String): List<RiskSimulationScenario> {
        return SeedData.getRiskScenariosForTenant(tenantId)
    }

    fun getInspectionTickets(tenantId: String): Flow<List<InspectionTicket>> {
        return database.inspectionTicketDao().getTicketsByTenant(tenantId).map { list ->
            list.map { entity ->
                InspectionTicket(
                    id = entity.id,
                    tenantId = entity.tenantId,
                    assetId = entity.assetId,
                    assetName = entity.assetName,
                    inspector = entity.inspector,
                    findings = entity.findings,
                    priority = entity.priority,
                    aiConfidence = entity.aiConfidence,
                    timestamp = entity.timestamp,
                    resolved = entity.resolved
                )
            }
        }
    }

    suspend fun createInspectionTicket(ticket: InspectionTicket) = withContext(Dispatchers.IO) {
        val entity = InspectionTicketEntity(
            id = ticket.id,
            tenantId = ticket.tenantId,
            assetId = ticket.assetId,
            assetName = ticket.assetName,
            inspector = ticket.inspector,
            findings = ticket.findings,
            priority = ticket.priority,
            aiConfidence = ticket.aiConfidence,
            timestamp = ticket.timestamp,
            resolved = ticket.resolved
        )
        database.inspectionTicketDao().insertTicket(entity)
    }

    suspend fun updateAssetStatus(assetId: String, tenantId: String, status: AssetStatus) = withContext(Dispatchers.IO) {
        database.spatialAssetDao().updateAssetStatus(assetId, tenantId, status.name)
    }

    suspend fun addSpatialAsset(asset: SpatialAsset) = withContext(Dispatchers.IO) {
        database.spatialAssetDao().insertAssets(listOf(asset.toEntity()))
    }
}

// Helper converters between Entity & Domain
private fun SpatialAsset.toEntity(): SpatialAssetEntity {
    val coordsStr = polygonBounds.joinToString(";") { "${it.lat},${it.lng}" }
    val metricsStr = metrics.entries.joinToString(";") { "${it.key}:${it.value}" }
    return SpatialAssetEntity(
        id = id,
        tenantId = tenantId,
        name = name,
        sectorType = sectorType.name,
        assetCategory = assetCategory,
        geometryType = geometryType.name,
        lat = primaryLocation.lat,
        lng = primaryLocation.lng,
        polygonCoordsJson = coordsStr,
        status = status.name,
        metricsJson = metricsStr,
        lastInspected = lastInspected,
        heightMeters = heightMeters,
        areaHectares = areaHectares
    )
}

private fun SpatialAssetEntity.toDomain(): SpatialAsset {
    val parsedBounds = if (polygonCoordsJson.isBlank()) {
        emptyList()
    } else {
        polygonCoordsJson.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) LatLngCoord(lat, lng) else null
            } else null
        }
    }

    val parsedMetrics = if (metricsJson.isBlank()) {
        emptyMap()
    } else {
        metricsJson.split(";").mapNotNull { kv ->
            val parts = kv.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    val parsedStatus = try {
        AssetStatus.valueOf(status)
    } catch (_: Exception) {
        AssetStatus.OPTIMAL
    }

    val parsedSector = try {
        SectorType.valueOf(sectorType)
    } catch (_: Exception) {
        SectorType.AGRICULTURE
    }

    val parsedGeometry = try {
        AssetGeometryType.valueOf(geometryType)
    } catch (_: Exception) {
        AssetGeometryType.POINT
    }

    return SpatialAsset(
        id = id,
        tenantId = tenantId,
        name = name,
        sectorType = parsedSector,
        assetCategory = assetCategory,
        geometryType = parsedGeometry,
        primaryLocation = LatLngCoord(lat, lng),
        polygonBounds = parsedBounds,
        status = parsedStatus,
        metrics = parsedMetrics,
        lastInspected = lastInspected,
        heightMeters = heightMeters,
        areaHectares = areaHectares
    )
}
