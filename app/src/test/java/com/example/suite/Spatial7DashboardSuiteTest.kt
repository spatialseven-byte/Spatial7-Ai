package com.example.suite

import com.example.model.Spatial7MockData
import com.example.model.Spatial7SuiteAsset
import org.junit.Assert.*
import org.junit.Test

class Spatial7DashboardSuiteTest {

    @Test
    fun initialMockAssets_matchSpecification() {
        val assets = Spatial7MockData.initialAssets
        assertEquals(3, assets.size)

        val ast101 = assets.first { it.id == "AST-101" }
        assertEquals("Main St Pavement Segment 1", ast101.name)
        assertEquals("Transport", ast101.assetClass)
        assertEquals("Pavement", ast101.subClass)
        assertEquals(2, ast101.condition)
        assertEquals(145000L, ast101.value)
        assertEquals(-27.56, ast101.lat, 0.001)
        assertEquals(152.41, ast101.lng, 0.001)
        assertEquals("Active", ast101.status)
        assertEquals("Kris Lal", ast101.updatedBy)

        val ast102 = assets.first { it.id == "AST-102" }
        assertEquals("Lockyer Creek DN600 Pipe", ast102.name)
        assertEquals("Stormwater", ast102.assetClass)
        assertEquals("Gravity Pipe", ast102.subClass)
        assertEquals(4, ast102.condition)
        assertEquals(82000L, ast102.value)
        assertEquals("Needs Inspection", ast102.status)
        assertEquals("Kris Lal", ast102.updatedBy)

        val ast103 = assets.first { it.id == "AST-103" }
        assertEquals("Gatton Kerb & Gutter East", ast103.name)
        assertEquals(1, ast103.condition)
        assertEquals(34000L, ast103.value)
        assertEquals("Kris Lal", ast103.updatedBy)
    }

    @Test
    fun conditionChange_updatesAssetRecordAndAttribution() {
        var assetList = Spatial7MockData.initialAssets
        var selectedAsset: Spatial7SuiteAsset? = assetList.first()

        // Simulate handleConditionChange
        fun handleConditionChange(id: String, newCondition: Int) {
            assetList = assetList.map { item ->
                if (item.id == id) {
                    item.copy(condition = newCondition, updatedBy = "Kris Lal")
                } else item
            }
            if (selectedAsset?.id == id) {
                selectedAsset = selectedAsset?.copy(condition = newCondition, updatedBy = "Kris Lal")
            }
        }

        // Change AST-101 condition to 4 (Poor)
        handleConditionChange("AST-101", 4)

        val updatedAst101 = assetList.first { it.id == "AST-101" }
        assertEquals(4, updatedAst101.condition)
        assertEquals("Kris Lal", updatedAst101.updatedBy)
        assertEquals(4, selectedAsset?.condition)
        assertEquals("Kris Lal", selectedAsset?.updatedBy)
    }

    @Test
    fun totalGrossReplacementCost_sumsCorrectly() {
        val totalGrc = Spatial7MockData.initialAssets.sumOf { it.value }
        assertEquals(261000L, totalGrc)
    }
}
