package com.example.ui

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GeoNexusRepository
import com.example.data.local.GeoNexusDatabase
import com.example.model.*
import com.example.service.DocumentIngestionService
import com.example.service.GeminiSpatialService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class GeoNexusUiState(
    val activeTenant: Tenant,
    val allTenants: List<Tenant>,
    val assets: List<SpatialAsset> = emptyList(),
    val sensors: List<IoTSensorTelemetry> = emptyList(),
    val risks: List<RiskSimulationScenario> = emptyList(),
    val tickets: List<InspectionTicket> = emptyList(),
    val isLoading: Boolean = false,
    val activeTab: NavigationTab = NavigationTab.NATIONAL_TWIN,
    val perspectiveMode: PerspectiveMode = PerspectiveMode.BACKEND_NATIONAL_SOVEREIGN,
    val selectedStateCode: String = "QLD",
    val selectedCouncilId: String = "bne",
    val selectedSectorTab: String = "Councils",
    val schoolSearchQuery: String = "",
    val globalSearchQuery: String = "",
    val isAccountDialogVisible: Boolean = false,
    val activeRole: com.example.security.UserRole = com.example.security.UserRole.TENANT_ADMIN,
    val allStates: List<AustralianState> = AustralianRegistryData.states,
    val allCouncils: List<AustralianCouncil> = AustralianRegistryData.councils,
    val allSchools: List<AustralianSchool> = AustralianRegistryData.schools,
    val allUniversities: List<AustralianUniversity> = AustralianRegistryData.universities,
    val allAgedCare: List<AustralianAgedCare> = AustralianRegistryData.agedCare,
    val allIndustrial: List<AustralianIndustrial> = AustralianRegistryData.industrial
) {
    // Reactive filtered properties across spatial entities based on global search & state filters
    val filteredCouncils: List<AustralianCouncil>
        get() {
            val query = globalSearchQuery.trim().lowercase()
            return allCouncils.filter { council ->
                (selectedStateCode.isEmpty() || council.stateCode.equals(selectedStateCode, ignoreCase = true)) &&
                (query.isEmpty() || council.name.lowercase().contains(query) || council.region.lowercase().contains(query))
            }
        }

    val filteredSchools: List<AustralianSchool>
        get() {
            val query = (if (schoolSearchQuery.isNotEmpty()) schoolSearchQuery else globalSearchQuery).trim().lowercase()
            return allSchools.filter { school ->
                (selectedStateCode.isEmpty() || school.stateCode.equals(selectedStateCode, ignoreCase = true)) &&
                (query.isEmpty() || school.name.lowercase().contains(query) || school.suburb.lowercase().contains(query))
            }
        }

    val filteredUniversities: List<AustralianUniversity>
        get() {
            val query = globalSearchQuery.trim().lowercase()
            return allUniversities.filter { uni ->
                (selectedStateCode.isEmpty() || uni.stateCode.equals(selectedStateCode, ignoreCase = true)) &&
                (query.isEmpty() || uni.name.lowercase().contains(query) || uni.shortName.lowercase().contains(query) || uni.campus.lowercase().contains(query))
            }
        }

    val filteredAgedCare: List<AustralianAgedCare>
        get() {
            val query = globalSearchQuery.trim().lowercase()
            return allAgedCare.filter { care ->
                (selectedStateCode.isEmpty() || care.stateCode.equals(selectedStateCode, ignoreCase = true)) &&
                (query.isEmpty() || care.name.lowercase().contains(query) || care.provider.lowercase().contains(query) || care.suburb.lowercase().contains(query))
            }
        }

    val filteredIndustrial: List<AustralianIndustrial>
        get() {
            val query = globalSearchQuery.trim().lowercase()
            return allIndustrial.filter { ind ->
                (selectedStateCode.isEmpty() || ind.stateCode.equals(selectedStateCode, ignoreCase = true)) &&
                (query.isEmpty() || ind.name.lowercase().contains(query) || ind.estateType.lowercase().contains(query))
            }
        }
}

enum class NavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    NATIONAL_TWIN("Australia Twin", Icons.Filled.Public),
    DASHBOARD("Sector Twin", Icons.Filled.Dashboard),
    MAP("Spatial Map", Icons.Filled.Map),
    INGESTION("Data & QA/QC", Icons.Filled.CheckCircle),
    AI_COPILOT("Spatial AI", Icons.Filled.AutoAwesome),
    INSPECTIONS("Work Orders", Icons.AutoMirrored.Filled.Assignment),
    ARCHITECTURE("RLS & DB", Icons.Filled.Security)
}

class GeoNexusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GeoNexusRepository
    val geminiService: GeminiSpatialService = GeminiSpatialService()
    val documentIngestionService: DocumentIngestionService = DocumentIngestionService()

    private val _uiState: MutableStateFlow<GeoNexusUiState>
    val uiState: StateFlow<GeoNexusUiState>

    init {
        val db = GeoNexusDatabase.getDatabase(application)
        repository = GeoNexusRepository(db)
        val tenants = repository.getTenants()
        val initialTenant = tenants.first()

        _uiState = MutableStateFlow(
            GeoNexusUiState(
                activeTenant = initialTenant,
                allTenants = tenants,
                sensors = repository.getSensorsForTenant(initialTenant.tenantId),
                risks = repository.getRiskScenarios(initialTenant.tenantId)
            )
        )
        uiState = _uiState.asStateFlow()

        switchTenant(initialTenant)
    }

    fun switchTenant(tenant: Tenant) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activeTenant = tenant) }
            repository.preloadTenantDataIfEmpty(tenant.tenantId)

            // Update sensors & risks
            val sensors = repository.getSensorsForTenant(tenant.tenantId)
            val risks = repository.getRiskScenarios(tenant.tenantId)

            _uiState.update {
                it.copy(
                    activeTenant = tenant,
                    sensors = sensors,
                    risks = risks,
                    isLoading = false
                )
            }

            // Launch flows for assets and tickets for this specific tenant
            repository.getSpatialAssetsFlow(tenant.tenantId).collectLatest { assets ->
                _uiState.update { it.copy(assets = assets) }
            }
        }

        viewModelScope.launch {
            repository.getInspectionTickets(tenant.tenantId).collectLatest { tickets ->
                _uiState.update { it.copy(tickets = tickets) }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setPerspectiveMode(mode: PerspectiveMode) {
        _uiState.update { it.copy(perspectiveMode = mode) }
    }

    fun selectAustralianState(stateCode: String) {
        _uiState.update { current ->
            val matchingCouncils = current.allCouncils.filter { it.stateCode.equals(stateCode, ignoreCase = true) }
            val firstCouncilId = matchingCouncils.firstOrNull()?.id ?: current.selectedCouncilId
            current.copy(
                selectedStateCode = stateCode,
                selectedCouncilId = firstCouncilId
            )
        }
    }

    fun selectAustralianCouncil(councilId: String) {
        _uiState.update { it.copy(selectedCouncilId = councilId) }
    }

    fun setSelectedSectorTab(sector: String) {
        _uiState.update { it.copy(selectedSectorTab = sector) }
    }

    fun setSchoolSearchQuery(query: String) {
        _uiState.update { it.copy(schoolSearchQuery = query) }
    }

    fun setGlobalSearchQuery(query: String) {
        _uiState.update { it.copy(globalSearchQuery = query) }
    }

    fun setAccountDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isAccountDialogVisible = visible) }
    }

    fun setActiveRole(role: com.example.security.UserRole) {
        _uiState.update { it.copy(activeRole = role) }
    }

    fun registerNewTenantAccount(
        name: String,
        sector: SectorType,
        state: String,
        council: String,
        region: String,
        desc: String
    ) {
        val newTenant = Tenant(
            tenantId = "tenant-${UUID.randomUUID().toString().take(8)}",
            name = name,
            sectorType = sector,
            stateCode = state,
            region = region,
            centerLat = -27.4698,
            centerLng = 153.0251,
            defaultZoom = 13.0f,
            description = desc,
            activeAssetsCount = 12,
            activeSensorsCount = 45,
            iotStreamRate = "120 msgs/min",
            complianceStatus = "PostGIS RLS Cryptographically Enforced",
            councilLga = council
        )
        val updatedTenants = _uiState.value.allTenants + newTenant
        _uiState.update { it.copy(allTenants = updatedTenants, isAccountDialogVisible = false) }
        switchTenant(newTenant)
    }

    fun createTicket(ticket: InspectionTicket) {
        viewModelScope.launch {
            repository.createInspectionTicket(ticket)
        }
    }

    fun toggleTicketResolved(ticketId: String, resolved: Boolean) {
        viewModelScope.launch {
            val tenantId = _uiState.value.activeTenant.tenantId
            GeoNexusDatabase.getDatabase(getApplication()).inspectionTicketDao().updateTicketStatus(ticketId, tenantId, resolved)
        }
    }

    fun updateAssetStatus(assetId: String, status: AssetStatus) {
        viewModelScope.launch {
            repository.updateAssetStatus(assetId, _uiState.value.activeTenant.tenantId, status)
        }
    }

    fun commitExtractedAsset(asset: SpatialAsset) {
        viewModelScope.launch {
            repository.addSpatialAsset(asset)
            // Switch to Map tab to visualize the newly committed asset
            selectTab(NavigationTab.MAP)
        }
    }
}
