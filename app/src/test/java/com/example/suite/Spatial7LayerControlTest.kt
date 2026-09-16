package com.example.suite

import com.example.model.Spatial7BaseMap
import com.example.model.Spatial7BaseMapCatalog
import org.junit.Assert.*
import org.junit.Test

class Spatial7LayerControlTest {

    @Test
    fun spatial7BaseMap_hasAllThreeRequiredTypes() {
        val baseMaps = Spatial7BaseMap.entries
        assertEquals(3, baseMaps.size)

        val cadastral = Spatial7BaseMap.CADASTRAL
        assertEquals("Cadastral", cadastral.displayName)
        assertEquals("CAD", cadastral.shortCode)
        assertTrue(cadastral.description.contains("boundaries", ignoreCase = true))

        val zoning = Spatial7BaseMap.ZONING
        assertEquals("Zoning", zoning.displayName)
        assertEquals("ZONE", zoning.shortCode)
        assertTrue(zoning.description.contains("planning", ignoreCase = true))

        val aerial = Spatial7BaseMap.AERIAL
        assertEquals("Aerial Imagery", aerial.displayName)
        assertEquals("ORTHO", aerial.shortCode)
        assertTrue(aerial.description.contains("aerial", ignoreCase = true) || aerial.description.contains("photogrammetry", ignoreCase = true))
    }

    @Test
    fun spatial7BaseMap_fromId_handlesValidAndFallback() {
        assertEquals(Spatial7BaseMap.CADASTRAL, Spatial7BaseMap.fromId("cadastral"))
        assertEquals(Spatial7BaseMap.ZONING, Spatial7BaseMap.fromId("zoning"))
        assertEquals(Spatial7BaseMap.AERIAL, Spatial7BaseMap.fromId("aerial"))
        // Case insensitivity
        assertEquals(Spatial7BaseMap.ZONING, Spatial7BaseMap.fromId("ZONING"))
        // Fallback
        assertEquals(Spatial7BaseMap.CADASTRAL, Spatial7BaseMap.fromId("unknown_basemap"))
    }

    @Test
    fun spatial7BaseMapCatalog_hasParcelsAndZoningDefinitions() {
        val parcels = Spatial7BaseMapCatalog.sampleParcels
        assertTrue(parcels.isNotEmpty())
        assertTrue(parcels.any { it.lotPlan.contains("LOT 101") })
        assertTrue(parcels.any { it.easementPresent })

        val zones = Spatial7BaseMapCatalog.sampleZones
        assertEquals(4, zones.size)
        assertTrue(zones.any { it.code == "LMR" })
        assertTrue(zones.any { it.code == "PC" })
        assertTrue(zones.any { it.code == "OS" })
        assertTrue(zones.any { it.code == "IN1" })
    }

    @Test
    fun baseMapSwitching_stateSimulation() {
        var currentBaseMap = Spatial7BaseMap.default
        assertEquals(Spatial7BaseMap.CADASTRAL, currentBaseMap)

        // Switch to Zoning
        currentBaseMap = Spatial7BaseMap.ZONING
        assertEquals("Zoning", currentBaseMap.displayName)

        // Switch to Aerial Imagery
        currentBaseMap = Spatial7BaseMap.AERIAL
        assertEquals("Aerial Imagery", currentBaseMap.displayName)
    }
}
