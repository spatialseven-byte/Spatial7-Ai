package com.example.sync

import org.junit.Assert.*
import org.junit.Test

class Spatial7SyncEngineTest {

    @Test
    fun initialState_hasKrisLalAsDefaultOwner() {
        val state = Spatial7SyncEngine.createInitialState()
        assertEquals(Spatial7SyncEngine.OWNER_KRIS_LAL, state.assetRecord.lastModifiedBy)
        assertEquals(Spatial7SyncEngine.OWNER_KRIS_LAL, state.gisRecord.lastModifiedBy)
        assertEquals(Spatial7SyncEngine.SRID_GDA2020_ZONE56, 7856)
        assertTrue(state.gisRecord.isValidGeometry)
        assertEquals(0.02, state.gisRecord.spatialPrecisionM, 0.001)
    }

    @Test
    fun caseA_gisUpdateSyncsToAssetData() {
        val initial = Spatial7SyncEngine.createInitialState()
        val newGeom = "LINESTRING(501250.00 6945140.00, 501330.00 6945220.00) [EPSG:7856]"

        val updated = Spatial7SyncEngine.simulateGisUpdate(
            currentState = initial,
            newGeomWkt = newGeom,
            isCorruptOrInvalid = false,
            author = "Kris Lal"
        )

        assertEquals(newGeom, updated.gisRecord.geomWkt)
        assertEquals("Kris Lal", updated.assetRecord.lastModifiedBy)
        assertEquals(updated.gisRecord.updatedAt, updated.assetRecord.updatedAt)
        assertEquals("trg_sync_gis_to_asset", updated.logs.first().triggerName)
        assertFalse(updated.logs.first().wasSelfHealed)
    }

    @Test
    fun caseA2_invalidGeometryIsAutoHealedViaST_MakeValid() {
        val initial = Spatial7SyncEngine.createInitialState()
        val corruptBowtie = "POLYGON((0 0, 0 2, 2 0, 2 2, 0 0))"

        val updated = Spatial7SyncEngine.simulateGisUpdate(
            currentState = initial,
            newGeomWkt = corruptBowtie,
            isCorruptOrInvalid = true,
            author = "Kris Lal"
        )

        // Verifies auto-self healing
        assertTrue(updated.gisRecord.isValidGeometry)
        assertTrue(updated.gisRecord.geomWkt.contains("ST_MakeValid"))
        assertTrue(updated.logs.first().wasSelfHealed)
        assertEquals(updated.gisRecord.updatedAt, updated.assetRecord.updatedAt)
    }

    @Test
    fun caseB_assetUpdateSyncsToSpatialGis() {
        val initial = Spatial7SyncEngine.createInitialState()

        val updated = Spatial7SyncEngine.simulateAssetUpdate(
            currentState = initial,
            newConditionScore = 2,
            newStatus = "MAINTENANCE_REQUIRED",
            author = "Kris Lal"
        )

        assertEquals(2, updated.assetRecord.conditionScore)
        assertEquals("MAINTENANCE_REQUIRED", updated.assetRecord.assetStatus)
        assertEquals("Kris Lal", updated.gisRecord.lastModifiedBy)
        assertEquals(updated.assetRecord.updatedAt, updated.gisRecord.updatedAt)
        assertEquals("trg_sync_asset_to_gis", updated.logs.first().triggerName)
    }

    @Test
    fun recursionLoopIsPreventedByTriggerDepth() {
        val initial = Spatial7SyncEngine.createInitialState()
        val updated = Spatial7SyncEngine.simulateRecursiveLoopAttempt(initial)

        val loopLog = updated.logs.first()
        assertEquals(2, loopLog.depth)
        assertTrue(loopLog.isLoopPrevented)
        assertTrue(loopLog.summary.contains("RECURSION LOOP INTERCEPTED"))
    }

    @Test
    fun exceptionCatchBlockPreventsPipelineCrash() {
        val initial = Spatial7SyncEngine.createInitialState()
        val updated = Spatial7SyncEngine.simulateAnomalyRecovery(initial)

        val errorLog = updated.logs.first()
        assertNotNull(errorLog.warningMessage)
        assertTrue(errorLog.warningMessage!!.contains("Spatial7 Auto-Correction Engine repaired sync anomaly"))
        assertTrue(errorLog.summary.contains("CRASH PREVENTED"))
    }

    @Test
    fun productionSyncSql_containsAllMandatedComponents() {
        val sql = Spatial7SyncEngine.PRODUCTION_SYNC_SQL
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS tbl_asset_data"))
        assertTrue(sql.contains("CREATE TABLE IF NOT EXISTS tbl_spatial_gis"))
        assertTrue(sql.contains("GEOMETRY(Geometry, 7856)"))
        assertTrue(sql.contains("pg_trigger_depth()"))
        assertTrue(sql.contains("Kris Lal"))
        assertTrue(sql.contains("ST_MakeValid(NEW.geom)"))
        assertTrue(sql.contains("trg_sync_gis_to_asset"))
        assertTrue(sql.contains("trg_sync_asset_to_gis"))
        assertTrue(sql.contains("Spatial7 Auto-Correction Engine repaired sync anomaly"))
    }
}
