package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpatialAssetDao {
    @Query("SELECT * FROM spatial_assets WHERE tenantId = :tenantId ORDER BY name ASC")
    fun getAssetsByTenant(tenantId: String): Flow<List<SpatialAssetEntity>>

    @Query("SELECT * FROM spatial_assets WHERE id = :assetId AND tenantId = :tenantId LIMIT 1")
    suspend fun getAssetById(assetId: String, tenantId: String): SpatialAssetEntity?

    @Query("SELECT COUNT(*) FROM spatial_assets WHERE tenantId = :tenantId")
    suspend fun countByTenant(tenantId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<SpatialAssetEntity>)

    @Query("UPDATE spatial_assets SET status = :newStatus WHERE id = :assetId AND tenantId = :tenantId")
    suspend fun updateAssetStatus(assetId: String, tenantId: String, newStatus: String)
}

@Dao
interface InspectionTicketDao {
    @Query("SELECT * FROM inspection_tickets WHERE tenantId = :tenantId ORDER BY timestamp DESC")
    fun getTicketsByTenant(tenantId: String): Flow<List<InspectionTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: InspectionTicketEntity)

    @Query("UPDATE inspection_tickets SET resolved = :resolved WHERE id = :ticketId AND tenantId = :tenantId")
    suspend fun updateTicketStatus(ticketId: String, tenantId: String, resolved: Boolean)
}
