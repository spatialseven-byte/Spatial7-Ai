package com.example.model

object SeedData {

    val TENANTS = listOf(
        Tenant(
            tenantId = "tenant-agri-001-wa",
            name = "Kimberley Pastoral Co.",
            sectorType = SectorType.AGRICULTURE,
            stateCode = "WA",
            region = "Fitzroy Basin, Kimberley Region",
            centerLat = -18.196,
            centerLng = 125.568,
            defaultZoom = 12.5f,
            description = "1.2 Million hectare pastoral station. Virtual fencing corridors, live LoRaWAN cattle collar tracking, soil probe arrays, and Sentinel-2 satellite NDVI.",
            activeAssetsCount = 1420,
            activeSensorsCount = 284,
            iotStreamRate = "1,850 msgs/min"
        ),
        Tenant(
            tenantId = "tenant-council-002-qld",
            name = "City of Gold Coast Council",
            sectorType = SectorType.LOCAL_GOV,
            stateCode = "QLD",
            region = "South East Queensland",
            centerLat = -28.016,
            centerLng = 153.400,
            defaultZoom = 13.0f,
            description = "Municipal infrastructure digital twin. 4,200 smart streetlights, stormwater drainage networks, Nerang River flood simulation, and community maintenance works.",
            activeAssetsCount = 8940,
            activeSensorsCount = 1120,
            iotStreamRate = "3,400 msgs/min"
        ),
        Tenant(
            tenantId = "tenant-edu-003-vic",
            name = "University of Melbourne Twin",
            sectorType = SectorType.EDUCATION,
            stateCode = "VIC",
            region = "Parkville Campus, Melbourne",
            centerLat = -37.798,
            centerLng = 144.960,
            defaultZoom = 15.5f,
            description = "LOD-3 3D Campus Digital Twin. High-precision indoor LiDAR mapping, automated HVAC airflow optimization, solar microgrid telemetry, and lecture hall capacity.",
            activeAssetsCount = 640,
            activeSensorsCount = 890,
            iotStreamRate = "2,100 msgs/min"
        ),
        Tenant(
            tenantId = "tenant-indus-004-wa",
            name = "Pilbara Iron & Port Logistics",
            sectorType = SectorType.INDUSTRIAL,
            stateCode = "WA",
            region = "Port Hedland & Hamersley Range",
            centerLat = -20.312,
            centerLng = 118.575,
            defaultZoom = 13.2f,
            description = "Heavy industrial asset management. 45km overland conveyor systems, bulk iron ore export berths, drone photogrammetry defect detection, and predictive maintenance.",
            activeAssetsCount = 3120,
            activeSensorsCount = 670,
            iotStreamRate = "4,900 msgs/min"
        )
    )

    fun getAssetsForTenant(tenantId: String): List<SpatialAsset> {
        return when (tenantId) {
            "tenant-agri-001-wa" -> agriAssets
            "tenant-council-002-qld" -> councilAssets
            "tenant-edu-003-vic" -> campusAssets
            "tenant-indus-004-wa" -> industrialAssets
            else -> emptyList()
        }
    }

    fun getSensorsForTenant(tenantId: String): List<IoTSensorTelemetry> {
        return when (tenantId) {
            "tenant-agri-001-wa" -> agriSensors
            "tenant-council-002-qld" -> councilSensors
            "tenant-edu-003-vic" -> campusSensors
            "tenant-indus-004-wa" -> industrialSensors
            else -> emptyList()
        }
    }

    fun getRiskScenariosForTenant(tenantId: String): List<RiskSimulationScenario> {
        return when (tenantId) {
            "tenant-agri-001-wa" -> listOf(
                RiskSimulationScenario(
                    id = "risk-agri-drought",
                    title = "Dry Season Feed Deficit Simulation",
                    hazardType = "DROUGHT",
                    severityIndex = 64,
                    riskLevel = "Moderate-High",
                    affectedAreaHectares = 42000.0,
                    triggerCondition = "Pasture biomass below 750 kg DM/ha over 40 days",
                    mitigationProtocol = "Trigger rotational grazing to Paddock Gamma; activate solar bore 12.",
                    perimeterPolygon = listOf(
                        LatLngCoord(-18.180, 125.550),
                        LatLngCoord(-18.180, 125.590),
                        LatLngCoord(-18.215, 125.590),
                        LatLngCoord(-18.215, 125.550)
                    )
                )
            )
            "tenant-council-002-qld" -> listOf(
                RiskSimulationScenario(
                    id = "risk-qld-flood",
                    title = "Nerang Catchment 1-in-100yr Inundation",
                    hazardType = "FLOOD",
                    severityIndex = 82,
                    riskLevel = "Severe Warning",
                    affectedAreaHectares = 1450.0,
                    triggerCondition = "Rainfall > 185mm / 6hr + high king tide (2.1m)",
                    mitigationProtocol = "Automate stormwater flood gates at Isle of Capri; dispatch road closure crews.",
                    perimeterPolygon = listOf(
                        LatLngCoord(-28.005, 153.385),
                        LatLngCoord(-28.000, 153.415),
                        LatLngCoord(-28.030, 153.420),
                        LatLngCoord(-28.035, 153.390)
                    )
                ),
                RiskSimulationScenario(
                    id = "risk-qld-fire",
                    title = "Hinterland Bushfire Forest Danger (FFDI 48)",
                    hazardType = "BUSHFIRE",
                    severityIndex = 74,
                    riskLevel = "High Threat",
                    affectedAreaHectares = 3800.0,
                    triggerCondition = "Wind gust NW 55 km/h, RH < 15%, Temp 38°C",
                    mitigationProtocol = "Pre-position aerial water bombers; trigger rural perimeter sensors.",
                    perimeterPolygon = listOf(
                        LatLngCoord(-28.040, 153.350),
                        LatLngCoord(-28.035, 153.380),
                        LatLngCoord(-28.065, 153.385),
                        LatLngCoord(-28.070, 153.355)
                    )
                )
            )
            "tenant-edu-003-vic" -> listOf(
                RiskSimulationScenario(
                    id = "risk-edu-heat",
                    title = "Urban Heat Island & HVAC Peak Surge",
                    hazardType = "HEAT_ISLAND",
                    severityIndex = 58,
                    riskLevel = "Operational Stress",
                    affectedAreaHectares = 65.0,
                    triggerCondition = "Ambient 41°C causing campus chiller load to exceed 4.8 MW capacity",
                    mitigationProtocol = "Pre-cool lecture halls A-C; deploy automated external louvers on Wilson Hall.",
                    perimeterPolygon = listOf(
                        LatLngCoord(-37.794, 144.956),
                        LatLngCoord(-37.794, 144.966),
                        LatLngCoord(-37.802, 144.966),
                        LatLngCoord(-37.802, 144.956)
                    )
                )
            )
            "tenant-indus-004-wa" -> listOf(
                RiskSimulationScenario(
                    id = "risk-indus-conveyor",
                    title = "Conveyor C-104 Catastrophic Bearing Seizure",
                    hazardType = "EQUIPMENT_FAILURE",
                    severityIndex = 88,
                    riskLevel = "Critical Predictive Alert",
                    affectedAreaHectares = 12.0,
                    triggerCondition = "FFT high-frequency harmonic peak > 5.2 mm/s at 1800 RPM",
                    mitigationProtocol = "Scheduled safe stop during 14:00 tidal berth window; replace roller assembly.",
                    perimeterPolygon = listOf(
                        LatLngCoord(-20.305, 118.568),
                        LatLngCoord(-20.305, 118.582),
                        LatLngCoord(-20.320, 118.582),
                        LatLngCoord(-20.320, 118.568)
                    )
                )
            )
            else -> emptyList()
        }
    }

    // --- 1. Agriculture Sector Assets ---
    private val agriAssets = listOf(
        SpatialAsset(
            id = "agri-fence-01",
            tenantId = "tenant-agri-001-wa",
            name = "Paddock Alpha (Red Ridge) Virtual Geofence",
            sectorType = SectorType.AGRICULTURE,
            assetCategory = "Virtual Fence",
            geometryType = AssetGeometryType.POLYGON,
            primaryLocation = LatLngCoord(-18.190, 125.560),
            polygonBounds = listOf(
                LatLngCoord(-18.180, 125.545),
                LatLngCoord(-18.180, 125.575),
                LatLngCoord(-18.200, 125.578),
                LatLngCoord(-18.205, 125.542)
            ),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Enclosed Area" to "4,850 ha",
                "Herd Capacity" to "650 head",
                "Pasture Biomass" to "1,120 kg/ha",
                "Containment Rate" to "99.2%"
            ),
            lastInspected = "2026-09-12",
            areaHectares = 4850.0
        ),
        SpatialAsset(
            id = "agri-fence-02",
            tenantId = "tenant-agri-001-wa",
            name = "Paddock Beta (Boab Creek) Virtual Geofence",
            sectorType = SectorType.AGRICULTURE,
            assetCategory = "Virtual Fence",
            geometryType = AssetGeometryType.POLYGON,
            primaryLocation = LatLngCoord(-18.208, 125.580),
            polygonBounds = listOf(
                LatLngCoord(-18.198, 125.568),
                LatLngCoord(-18.202, 125.595),
                LatLngCoord(-18.225, 125.590),
                LatLngCoord(-18.220, 125.565)
            ),
            status = AssetStatus.WARNING,
            metrics = mapOf(
                "Enclosed Area" to "3,900 ha",
                "Herd Capacity" to "500 head",
                "Water Bore Pressure" to "2.1 bar (Low)",
                "Containment Rate" to "96.4%"
            ),
            lastInspected = "2026-09-10",
            areaHectares = 3900.0
        ),
        SpatialAsset(
            id = "agri-collar-402",
            tenantId = "tenant-agri-001-wa",
            name = "Bull #402 (Brahman Stud) GPS Collar",
            sectorType = SectorType.AGRICULTURE,
            assetCategory = "Livestock Collar",
            geometryType = AssetGeometryType.POINT,
            primaryLocation = LatLngCoord(-18.203, 125.598),
            status = AssetStatus.CRITICAL,
            metrics = mapOf(
                "Alert" to "VIRTUAL FENCE BREACH",
                "Distance Outside" to "140 meters",
                "Battery Level" to "88%",
                "Pulse Audio Trigger" to "Active (Stage 2)"
            ),
            lastInspected = "Just Now"
        ),
        SpatialAsset(
            id = "agri-bore-12",
            tenantId = "tenant-agri-001-wa",
            name = "Solar Water Bore & Trough #12",
            sectorType = SectorType.AGRICULTURE,
            assetCategory = "Water Infrastructure",
            geometryType = AssetGeometryType.POINT,
            primaryLocation = LatLngCoord(-18.189, 125.558),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Flow Rate" to "42 L/min",
                "Tank Level" to "94%",
                "Water Temp" to "24.1 °C",
                "Solar Inverter" to "1.8 kW"
            ),
            lastInspected = "2026-09-13"
        )
    )

    private val agriSensors = listOf(
        IoTSensorTelemetry(
            sensorId = "agri-soil-01",
            tenantId = "tenant-agri-001-wa",
            label = "Soil Moisture Probe Array (Profile 10/30/60cm)",
            sensorType = "SOIL_MOISTURE",
            lat = -18.188,
            lng = 125.562,
            currentValue = 28.4,
            unit = "% VWC",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "2 mins ago",
            sparklineValues = listOf(24.0, 25.1, 26.3, 27.0, 27.9, 28.4)
        ),
        IoTSensorTelemetry(
            sensorId = "agri-ndvi-01",
            tenantId = "tenant-agri-001-wa",
            label = "Sentinel-2 Crop Vegetation Index (NDVI)",
            sensorType = "REMOTE_SENSING",
            lat = -18.192,
            lng = 125.565,
            currentValue = 0.74,
            unit = "NDVI",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "Today 04:00 (Cloud Free)",
            sparklineValues = listOf(0.62, 0.65, 0.69, 0.71, 0.73, 0.74)
        ),
        IoTSensorTelemetry(
            sensorId = "agri-collar-rate",
            tenantId = "tenant-agri-001-wa",
            label = "Active LoRaWAN Cattle Telemetry Nodes",
            sensorType = "CATTLE_GPS",
            lat = -18.195,
            lng = 125.570,
            currentValue = 384.0,
            unit = "online collars",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "Real-time stream",
            sparklineValues = listOf(380.0, 381.0, 384.0, 383.0, 384.0, 384.0)
        )
    )

    // --- 2. Local Government Sector Assets ---
    private val councilAssets = listOf(
        SpatialAsset(
            id = "council-drain-01",
            tenantId = "tenant-council-002-qld",
            name = "Nerang River Stormwater Trunk Culvert #04",
            sectorType = SectorType.LOCAL_GOV,
            assetCategory = "Stormwater Main",
            geometryType = AssetGeometryType.LINESTRING,
            primaryLocation = LatLngCoord(-28.012, 153.405),
            polygonBounds = listOf(
                LatLngCoord(-28.008, 153.398),
                LatLngCoord(-28.012, 153.405),
                LatLngCoord(-28.018, 153.414)
            ),
            status = AssetStatus.WARNING,
            metrics = mapOf(
                "Pipe Diameter" to "1,800 mm",
                "Silt Accumulation" to "48% (Clean Req)",
                "Discharge Velocity" to "2.4 m/s",
                "Catchment Zone" to "Bundall / Isle of Capri"
            ),
            lastInspected = "2026-09-08"
        ),
        SpatialAsset(
            id = "council-lights-broadbeach",
            tenantId = "tenant-council-002-qld",
            name = "Broadbeach Smart LED Lighting Mesh Grid",
            sectorType = SectorType.LOCAL_GOV,
            assetCategory = "Smart Lighting",
            geometryType = AssetGeometryType.POLYGON,
            primaryLocation = LatLngCoord(-28.030, 153.428),
            polygonBounds = listOf(
                LatLngCoord(-28.022, 153.422),
                LatLngCoord(-28.022, 153.435),
                LatLngCoord(-28.038, 153.436),
                LatLngCoord(-28.038, 153.423)
            ),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Total Poles" to "412 Units",
                "Power Saved" to "38.2% vs High Pressure Sodium",
                "Motion Adaptive Dimming" to "Active (Midnight-05:00)",
                "Luminaire Health" to "99.8%"
            ),
            lastInspected = "2026-09-11",
            areaHectares = 220.0
        ),
        SpatialAsset(
            id = "council-ticket-pothole",
            tenantId = "tenant-council-002-qld",
            name = "Citizen Report #4109: Gold Coast Hwy Subsidence",
            sectorType = SectorType.LOCAL_GOV,
            assetCategory = "Citizen Work Order",
            geometryType = AssetGeometryType.POINT,
            primaryLocation = LatLngCoord(-28.025, 153.430),
            status = AssetStatus.MAINTENANCE,
            metrics = mapOf(
                "Reported Hazard" to "Deep Asphalt Pothole (Lane 2)",
                "Severity Score" to "High (Catering Bus Route)",
                "Assigned Crew" to "Road Maintenance Unit 3",
                "ETA to Repair" to "Within 4 Hours"
            ),
            lastInspected = "Today 08:30"
        )
    )

    private val councilSensors = listOf(
        IoTSensorTelemetry(
            sensorId = "council-flood-gauge",
            tenantId = "tenant-council-002-qld",
            label = "Nerang River Hydro Telemetry Gauge",
            sensorType = "FLOOD_GAUGE",
            lat = -28.014,
            lng = 153.408,
            currentValue = 1.48,
            unit = "meters AHD",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "1 min ago",
            sparklineValues = listOf(1.10, 1.22, 1.35, 1.40, 1.44, 1.48)
        ),
        IoTSensorTelemetry(
            sensorId = "council-bushfire-ffdi",
            tenantId = "tenant-council-002-qld",
            label = "Hinterland McArthur Fire Danger Index (FFDI)",
            sensorType = "FIRE_RISK",
            lat = -28.050,
            lng = 153.360,
            currentValue = 44.0,
            unit = "FFDI (Very High)",
            status = AssetStatus.WARNING,
            lastUpdated = "10 mins ago",
            sparklineValues = listOf(28.0, 32.0, 36.0, 40.0, 42.0, 44.0)
        )
    )

    // --- 3. Education Sector Assets ---
    private val campusAssets = listOf(
        SpatialAsset(
            id = "edu-bldg-wilson",
            tenantId = "tenant-edu-003-vic",
            name = "Wilson Hall - 3D Digital Twin LOD-3",
            sectorType = SectorType.EDUCATION,
            assetCategory = "Academic Heritage",
            geometryType = AssetGeometryType.BUILDING_3D,
            primaryLocation = LatLngCoord(-37.7978, 144.9605),
            polygonBounds = listOf(
                LatLngCoord(-37.7975, 144.9598),
                LatLngCoord(-37.7975, 144.9612),
                LatLngCoord(-37.7983, 144.9612),
                LatLngCoord(-37.7983, 144.9598)
            ),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Gross Floor Area" to "5,200 m²",
                "Chilled Water Demand" to "142 kW",
                "Indoor Air CO2" to "520 ppm (Clean)",
                "Solar Generation" to "48.5 kWh"
            ),
            lastInspected = "2026-09-14",
            heightMeters = 24f,
            areaHectares = 0.52
        ),
        SpatialAsset(
            id = "edu-bldg-redmond",
            tenantId = "tenant-edu-003-vic",
            name = "Redmond Barry Science Tower - LOD-3",
            sectorType = SectorType.EDUCATION,
            assetCategory = "Research Laboratories",
            geometryType = AssetGeometryType.BUILDING_3D,
            primaryLocation = LatLngCoord(-37.7988, 144.9592),
            polygonBounds = listOf(
                LatLngCoord(-37.7984, 144.9587),
                LatLngCoord(-37.7984, 144.9598),
                LatLngCoord(-37.7993, 144.9598),
                LatLngCoord(-37.7993, 144.9587)
            ),
            status = AssetStatus.WARNING,
            metrics = mapOf(
                "Floors" to "12 Levels",
                "HVAC Fan Filter Status" to "92% Load (Replacement Due)",
                "Lab Fume Hood Airflow" to "0.52 m/s (Nominal)",
                "Occupancy Rate" to "78%"
            ),
            lastInspected = "2026-09-09",
            heightMeters = 48f,
            areaHectares = 0.74
        ),
        SpatialAsset(
            id = "edu-quad-lawn",
            tenantId = "tenant-edu-003-vic",
            name = "Old Arts Quadrangle & Microclimate Sensor",
            sectorType = SectorType.EDUCATION,
            assetCategory = "Outdoor Commons",
            geometryType = AssetGeometryType.POLYGON,
            primaryLocation = LatLngCoord(-37.7982, 144.9620),
            polygonBounds = listOf(
                LatLngCoord(-37.7978, 144.9615),
                LatLngCoord(-37.7978, 144.9628),
                LatLngCoord(-37.7987, 144.9628),
                LatLngCoord(-37.7987, 144.9615)
            ),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Canopy Coverage" to "64%",
                "Urban Heat Reduction" to "-2.8 °C vs Surrounding Street",
                "Soil Irrigation Sensor" to "Automatic Drip"
            ),
            lastInspected = "2026-09-13",
            areaHectares = 1.1
        )
    )

    private val campusSensors = listOf(
        IoTSensorTelemetry(
            sensorId = "campus-hvac-power",
            tenantId = "tenant-edu-003-vic",
            label = "Campus Main Chiller Central Plant Energy",
            sensorType = "HVAC_ENERGY",
            lat = -37.7985,
            lng = 144.9602,
            currentValue = 385.0,
            unit = "kW",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "10 secs ago",
            sparklineValues = listOf(320.0, 340.0, 360.0, 375.0, 390.0, 385.0)
        ),
        IoTSensorTelemetry(
            sensorId = "campus-indoor-co2",
            tenantId = "tenant-edu-003-vic",
            label = "Lecture Theatre 1 Air Quality (CO2)",
            sensorType = "AIR_QUALITY",
            lat = -37.7977,
            lng = 144.9608,
            currentValue = 540.0,
            unit = "ppm",
            status = AssetStatus.OPTIMAL,
            lastUpdated = "Just now",
            sparklineValues = listOf(480.0, 500.0, 515.0, 530.0, 545.0, 540.0)
        )
    )

    // --- 4. Industrial Sector Assets ---
    private val industrialAssets = listOf(
        SpatialAsset(
            id = "indus-conveyor-c104",
            tenantId = "tenant-indus-004-wa",
            name = "Overland Iron Ore Conveyor C-104 (Drive Head)",
            sectorType = SectorType.INDUSTRIAL,
            assetCategory = "Bulk Material Handling",
            geometryType = AssetGeometryType.LINESTRING,
            primaryLocation = LatLngCoord(-20.312, 118.575),
            polygonBounds = listOf(
                LatLngCoord(-20.302, 118.565),
                LatLngCoord(-20.312, 118.575),
                LatLngCoord(-20.324, 118.588)
            ),
            status = AssetStatus.CRITICAL,
            metrics = mapOf(
                "Throughput Speed" to "6.5 m/s",
                "Tonnage" to "11,200 t/h",
                "Pulley Vibration" to "4.8 mm/s (ALERT)",
                "Motor Winding Temp" to "89.4 °C"
            ),
            lastInspected = "2026-09-14 06:15"
        ),
        SpatialAsset(
            id = "indus-berth-02",
            tenantId = "tenant-indus-004-wa",
            name = "Port Berth #02 Bulk Carrier Shiploader",
            sectorType = SectorType.INDUSTRIAL,
            assetCategory = "Maritime Export",
            geometryType = AssetGeometryType.POLYGON,
            primaryLocation = LatLngCoord(-20.308, 118.572),
            polygonBounds = listOf(
                LatLngCoord(-20.304, 118.568),
                LatLngCoord(-20.304, 118.578),
                LatLngCoord(-20.314, 118.578),
                LatLngCoord(-20.314, 118.568)
            ),
            status = AssetStatus.WARNING,
            metrics = mapOf(
                "Vessel Moored" to "M/V Pilbara Maru (260,000 DWT)",
                "Loading Arm Extension" to "42 meters",
                "Corrosion Surface Defect" to "Structural Rust Grade C (Gantry 3)",
                "Berth Utilization" to "94.2%"
            ),
            lastInspected = "2026-09-13",
            areaHectares = 18.0
        ),
        SpatialAsset(
            id = "indus-stacker-04",
            tenantId = "tenant-indus-004-wa",
            name = "Iron Ore Stockpile Stacker/Reclaimer 04",
            sectorType = SectorType.INDUSTRIAL,
            assetCategory = "Stockpile Yard",
            geometryType = AssetGeometryType.POINT,
            primaryLocation = LatLngCoord(-20.318, 118.582),
            status = AssetStatus.OPTIMAL,
            metrics = mapOf(
                "Slew Angle" to "142°",
                "Bucket Wheel RPM" to "6.2",
                "Ore Moisture" to "6.8%",
                "Hydraulic Pressure" to "210 bar"
            ),
            lastInspected = "2026-09-11"
        )
    )

    private val industrialSensors = listOf(
        IoTSensorTelemetry(
            sensorId = "indus-vib-c104",
            tenantId = "tenant-indus-004-wa",
            label = "Conveyor Drive 104 Tri-Axial Vibration (FFT)",
            sensorType = "VIBRATION_PREDICTIVE",
            lat = -20.312,
            lng = 118.575,
            currentValue = 4.8,
            unit = "mm/s RMS",
            status = AssetStatus.CRITICAL,
            lastUpdated = "Live Timescale stream",
            sparklineValues = listOf(2.1, 2.4, 2.8, 3.5, 4.2, 4.8)
        ),
        IoTSensorTelemetry(
            sensorId = "indus-temp-motor",
            tenantId = "tenant-indus-004-wa",
            label = "3.2MW Drive Motor Stator Temp",
            sensorType = "TEMPERATURE",
            lat = -20.313,
            lng = 118.576,
            currentValue = 89.4,
            unit = "°C",
            status = AssetStatus.WARNING,
            lastUpdated = "15 secs ago",
            sparklineValues = listOf(76.0, 79.5, 82.1, 85.0, 88.2, 89.4)
        )
    )
}
