package com.example.model

/**
 * Enterprise Australian Spatial Entity & Registry Models.
 * Provides unified schema across:
 * - Local Government Areas (Councils / LGAs)
 * - Educational Assets (Schools & University Campuses)
 * - Care Infrastructure (Aged Care & Retirement Villages)
 * - Industrial, Maritime & Commercial Estates
 */

enum class PerspectiveMode(val label: String, val description: String) {
    BACKEND_NATIONAL_SOVEREIGN(
        label = "National Sovereign Backend",
        description = "Full ABS & Geoscience Australia aggregate view with cross-jurisdiction spatial queries"
    ),
    TENANT_RLS_ISOLATED(
        label = "PostGIS RLS Isolated",
        description = "Cryptographically isolated tenant boundary enforced by PostgreSQL Row-Level Security"
    )
}

enum class IpweaAssetClass(
    val code: String,
    val label: String,
    val displayName: String,
    val defaultUsefulLifeYears: Int
) {
    TRANSPORT(
        code = "TR",
        label = "Transport & Roads",
        displayName = "Transport & Roads (IPWEA)",
        defaultUsefulLifeYears = 30
    ),
    STORMWATER_DRAINAGE(
        code = "SW",
        label = "Stormwater & Drainage",
        displayName = "Stormwater & Drainage (IPWEA)",
        defaultUsefulLifeYears = 80
    ),
    PARKS_OPEN_SPACE(
        code = "PK",
        label = "Parks & Open Space",
        displayName = "Parks & Open Space",
        defaultUsefulLifeYears = 20
    ),
    BUILDINGS_FACILITIES(
        code = "BD",
        label = "Buildings & Structures",
        displayName = "Buildings & Structures",
        defaultUsefulLifeYears = 50
    ),
    WATER_WASTEWATER(
        code = "WW",
        label = "Wastewater & Water",
        displayName = "Wastewater & Water",
        defaultUsefulLifeYears = 60
    )
}

data class AustralianState(
    val code: String,
    val name: String,
    val capital: String,
    val councilsCount: Int,
    val schoolsCount: Int,
    val universitiesCount: Int,
    val agedCareCount: Int,
    val industrialEstatesCount: Int,
    val openDataPortalName: String,
    val openDataPortalUrl: String,
    val centerLat: Double,
    val centerLng: Double,
    val defaultZoom: Float
)

data class AustralianCouncil(
    val id: String,
    val name: String,
    val stateCode: String,
    val region: String,
    val population: Int,
    val areaKm2: Double,
    val replacementValueAud: String,
    val openDataSource: String,
    val roadLengthKm: Double,
    val stormwaterPipeKm: Double,
    val openSpaceHectares: Double,
    val municipalAssetsCount: Int,
    val schoolsCount: Int,
    val universitiesCount: Int,
    val agedCareCount: Int,
    val industrialCount: Int,
    val primaryFocus: String,
    val centerLat: Double,
    val centerLng: Double
)

data class AustralianSchool(
    val id: String,
    val acaraId: String,
    val name: String,
    val stateCode: String,
    val suburb: String,
    val councilId: String,
    val sector: String, // Government, Catholic, Independent
    val level: String, // Primary, Secondary, Combined
    val studentEnrollment: Int,
    val campusAreaHectares: Double,
    val lat: Double,
    val lng: Double
)

data class AustralianUniversity(
    val id: String,
    val name: String,
    val shortName: String,
    val stateCode: String,
    val campus: String,
    val councilId: String,
    val studentPopulation: Int,
    val buildingCount: Int,
    val grossFloorAreaM2: String,
    val lat: Double,
    val lng: Double
)

data class AustralianAgedCare(
    val id: String,
    val name: String,
    val provider: String,
    val stateCode: String,
    val suburb: String,
    val councilId: String,
    val bedCount: Int,
    val emergencyPlanCertified: Boolean,
    val lat: Double,
    val lng: Double
)

data class AustralianIndustrial(
    val id: String,
    val name: String,
    val estateType: String,
    val stateCode: String,
    val councilId: String,
    val areaHectares: Double,
    val majorTenants: List<String>,
    val hazardClassification: String,
    val lat: Double,
    val lng: Double
)

object AustralianRegistryData {

    val states: List<AustralianState> = listOf(
        AustralianState("NSW", "New South Wales", "Sydney", 128, 3120, 11, 890, 310, "Data.NSW (Spatial Services)", "https://data.nsw.gov.au", -33.8688, 151.2093, 7.5f),
        AustralianState("VIC", "Victoria", "Melbourne", 79, 2280, 9, 740, 260, "Data.Vic (Vicmap Spatial)", "https://data.vic.gov.au", -37.8136, 144.9631, 8.0f),
        AustralianState("QLD", "Queensland", "Brisbane", 77, 1850, 8, 510, 220, "Queensland Spatial Catalogue (QSpatial)", "https://qldspatial.information.qld.gov.au", -27.4698, 153.0251, 7.5f),
        AustralianState("WA", "Western Australia", "Perth", 137, 1150, 5, 290, 190, "Data WA (Landgate SLIP)", "https://data.wa.gov.au", -31.9505, 115.8605, 7.0f),
        AustralianState("SA", "South Australia", "Adelaide", 68, 790, 4, 240, 110, "Data.SA (LocationSA)", "https://data.sa.gov.au", -34.9285, 138.6007, 8.0f),
        AustralianState("TAS", "Tasmania", "Hobart", 29, 260, 1, 95, 45, "LISTdata Tasmania", "https://data.tas.gov.au", -42.8821, 147.3272, 8.0f),
        AustralianState("ACT", "Australian Capital Territory", "Canberra", 1, 140, 2, 42, 28, "ACT Geospatial Portal", "https://data.act.gov.au", -35.2809, 149.1300, 11.0f),
        AustralianState("NT", "Northern Territory", "Darwin", 17, 190, 1, 28, 35, "NT Open Data Portal", "https://data.nt.gov.au", -12.4634, 130.8456, 7.0f)
    )

    val councils: List<AustralianCouncil> = listOf(
        AustralianCouncil(
            id = "bne",
            name = "Brisbane City Council",
            stateCode = "QLD",
            region = "South East Queensland",
            population = 1272461,
            areaKm2 = 1342.7,
            replacementValueAud = "$34.2 Billion",
            openDataSource = "Brisbane Open Data (data.brisbane.qld.gov.au)",
            roadLengthKm = 5820.0,
            stormwaterPipeKm = 4250.0,
            openSpaceHectares = 15200.0,
            municipalAssetsCount = 14280,
            schoolsCount = 285,
            universitiesCount = 4,
            agedCareCount = 112,
            industrialCount = 42,
            primaryFocus = "Brisbane River catchment flood resilience, Olympic 2032 transport corridor, and stormwater asset renewals.",
            centerLat = -27.4698,
            centerLng = 153.0251
        ),
        AustralianCouncil(
            id = "syd",
            name = "City of Sydney",
            stateCode = "NSW",
            region = "Greater Sydney",
            population = 211124,
            areaKm2 = 26.1,
            replacementValueAud = "$16.8 Billion",
            openDataSource = "City of Sydney Data Hub / Spatial Services NSW",
            roadLengthKm = 312.0,
            stormwaterPipeKm = 245.0,
            openSpaceHectares = 420.0,
            municipalAssetsCount = 9820,
            schoolsCount = 42,
            universitiesCount = 3,
            agedCareCount = 28,
            industrialCount = 12,
            primaryFocus = "Net Zero 2035 civic buildings, green infrastructure canopy, pedestrianized light rail spines.",
            centerLat = -33.8688,
            centerLng = 151.2093
        ),
        AustralianCouncil(
            id = "mel",
            name = "City of Melbourne",
            stateCode = "VIC",
            region = "Greater Melbourne",
            population = 149615,
            areaKm2 = 37.7,
            replacementValueAud = "$18.4 Billion",
            openDataSource = "City of Melbourne Open Data (data.melbourne.vic.gov.au)",
            roadLengthKm = 240.0,
            stormwaterPipeKm = 198.0,
            openSpaceHectares = 540.0,
            municipalAssetsCount = 11400,
            schoolsCount = 35,
            universitiesCount = 3,
            agedCareCount = 22,
            industrialCount = 18,
            primaryFocus = "Urban Forest 40% canopy target, sensitive heritage buildings, Yarra River flood attenuation.",
            centerLat = -37.8136,
            centerLng = 144.9631
        ),
        AustralianCouncil(
            id = "gold-coast",
            name = "City of Gold Coast",
            stateCode = "QLD",
            region = "South East Queensland",
            population = 643461,
            areaKm2 = 1334.0,
            replacementValueAud = "$22.6 Billion",
            openDataSource = "City of Gold Coast Open Data / QSpatial",
            roadLengthKm = 3450.0,
            stormwaterPipeKm = 2600.0,
            openSpaceHectares = 13000.0,
            municipalAssetsCount = 11200,
            schoolsCount = 115,
            universitiesCount = 2,
            agedCareCount = 64,
            industrialCount = 24,
            primaryFocus = "Canal seawall integrity, storm surge protection, light rail expansion stage 3.",
            centerLat = -28.0167,
            centerLng = 153.4000
        ),
        AustralianCouncil(
            id = "parramatta",
            name = "City of Parramatta",
            stateCode = "NSW",
            region = "Western Sydney",
            population = 256729,
            areaKm2 = 84.0,
            replacementValueAud = "$12.1 Billion",
            openDataSource = "Parramatta Open Data / Data.NSW",
            roadLengthKm = 720.0,
            stormwaterPipeKm = 580.0,
            openSpaceHectares = 920.0,
            municipalAssetsCount = 7450,
            schoolsCount = 68,
            universitiesCount = 2,
            agedCareCount = 38,
            industrialCount = 26,
            primaryFocus = "Parramatta Light Rail integration, heat island mitigation, river flood levee maintenance.",
            centerLat = -33.8150,
            centerLng = 151.0011
        ),
        AustralianCouncil(
            id = "stirling",
            name = "City of Stirling",
            stateCode = "WA",
            region = "Perth Metropolitan",
            population = 226369,
            areaKm2 = 105.2,
            replacementValueAud = "$8.9 Billion",
            openDataSource = "Data WA / City of Stirling GIS",
            roadLengthKm = 1040.0,
            stormwaterPipeKm = 780.0,
            openSpaceHectares = 1780.0,
            municipalAssetsCount = 6320,
            schoolsCount = 54,
            universitiesCount = 1,
            agedCareCount = 32,
            industrialCount = 14,
            primaryFocus = "Coastal dune protection, groundwater recharge stormwater basins, community sports precincts.",
            centerLat = -31.8790,
            centerLng = 115.8115
        ),
        AustralianCouncil(
            id = "geelong",
            name = "City of Greater Geelong",
            stateCode = "VIC",
            region = "Barwon South West",
            population = 271057,
            areaKm2 = 1252.0,
            replacementValueAud = "$7.8 Billion",
            openDataSource = "Vicmap / Geelong Data Hub",
            roadLengthKm = 2400.0,
            stormwaterPipeKm = 1350.0,
            openSpaceHectares = 3200.0,
            municipalAssetsCount = 5900,
            schoolsCount = 62,
            universitiesCount = 1,
            agedCareCount = 41,
            industrialCount = 20,
            primaryFocus = "Northern & Western Geelong Growth Areas infrastructure, Bellarine Peninsula conservation.",
            centerLat = -38.1499,
            centerLng = 144.3617
        ),
        AustralianCouncil(
            id = "adelaide",
            name = "City of Adelaide",
            stateCode = "SA",
            region = "Adelaide Central",
            population = 25129,
            areaKm2 = 15.6,
            replacementValueAud = "$4.2 Billion",
            openDataSource = "Data.SA / City of Adelaide Smart City Portal",
            roadLengthKm = 180.0,
            stormwaterPipeKm = 140.0,
            openSpaceHectares = 760.0,
            municipalAssetsCount = 4300,
            schoolsCount = 18,
            universitiesCount = 3,
            agedCareCount = 12,
            industrialCount = 6,
            primaryFocus = "Heritage Park Lands maintenance, Torrens Lake water quality, smart city sensor network.",
            centerLat = -34.9285,
            centerLng = 138.6007
        )
    )

    val universities: List<AustralianUniversity> = listOf(
        AustralianUniversity("uni-uq", "The University of Queensland", "UQ", "QLD", "St Lucia Campus", "bne", 55000, 185, "740,000 m²", -27.4975, 153.0137),
        AustralianUniversity("uni-syd", "The University of Sydney", "USYD", "NSW", "Camperdown / Darlington", "syd", 73000, 220, "920,000 m²", -33.8886, 151.1873),
        AustralianUniversity("uni-melb", "The University of Melbourne", "UniMelb", "VIC", "Parkville Campus", "mel", 65000, 195, "810,000 m²", -37.7963, 144.9614),
        AustralianUniversity("uni-unsw", "UNSW Sydney", "UNSW", "NSW", "Kensington Campus", "syd", 64000, 170, "680,000 m²", -33.9173, 151.2313),
        AustralianUniversity("uni-monash", "Monash University", "Monash", "VIC", "Clayton Campus", "mel", 86000, 210, "880,000 m²", -37.9105, 145.1362),
        AustralianUniversity("uni-curtin", "Curtin University", "Curtin", "WA", "Bentley Campus", "stirling", 58000, 140, "520,000 m²", -32.0044, 115.8943),
        AustralianUniversity("uni-uwa", "The University of Western Australia", "UWA", "WA", "Crawley Campus", "stirling", 26000, 95, "390,000 m²", -31.9803, 115.8172),
        AustralianUniversity("uni-adelaide", "The University of Adelaide", "Adelaide", "SA", "North Terrace Campus", "adelaide", 28000, 85, "340,000 m²", -34.9205, 138.6053),
        AustralianUniversity("uni-anu", "Australian National University", "ANU", "ACT", "Acton Campus", "act", 21000, 130, "480,000 m²", -35.2777, 149.1185),
        AustralianUniversity("uni-qut", "Queensland University of Technology", "QUT", "QLD", "Gardens Point Campus", "bne", 52000, 68, "310,000 m²", -27.4772, 153.0284)
    )

    val schools: List<AustralianSchool> = listOf(
        AustralianSchool("sch-1", "40129", "Brisbane State High School", "QLD", "South Brisbane", "bne", "Government", "Secondary", 3450, 4.8, -27.4839, 153.0183),
        AustralianSchool("sch-2", "40156", "Sydney Boys High School", "NSW", "Surry Hills", "syd", "Government", "Secondary", 1210, 3.2, -33.8938, 151.2185),
        AustralianSchool("sch-3", "40892", "Melbourne High School", "VIC", "South Yarra", "mel", "Government", "Secondary", 1420, 3.8, -37.8398, 144.9922),
        AustralianSchool("sch-4", "41021", "The Southport School", "QLD", "Southport", "gold-coast", "Independent", "Combined", 1680, 18.5, -27.9785, 153.4147),
        AustralianSchool("sch-5", "41255", "Parramatta High School", "NSW", "Parramatta", "parramatta", "Government", "Secondary", 980, 2.9, -33.8214, 150.9982),
        AustralianSchool("sch-6", "41430", "Churchlands Senior High School", "WA", "Churchlands", "stirling", "Government", "Secondary", 2850, 8.4, -31.9167, 115.7890),
        AustralianSchool("sch-7", "41890", "Geelong Grammar School", "VIC", "Corio", "geelong", "Independent", "Combined", 1540, 230.0, -38.0583, 144.3833),
        AustralianSchool("sch-8", "42010", "Adelaide High School", "SA", "Adelaide", "adelaide", "Government", "Secondary", 1650, 3.5, -34.9312, 138.5867)
    )

    val agedCare: List<AustralianAgedCare> = listOf(
        AustralianAgedCare("ac-1", "TriCare Jindalee Aged Care Residence", "TriCare Australia", "QLD", "Jindalee", "bne", 145, true, -27.5312, 152.9421),
        AustralianAgedCare("ac-2", "Opal HealthCare Paddington", "Opal HealthCare", "NSW", "Paddington", "syd", 96, true, -33.8845, 151.2267),
        AustralianAgedCare("ac-3", "Regis Aged Care Carlton", "Regis Health", "VIC", "Carlton", "mel", 112, true, -37.7998, 144.9689),
        AustralianAgedCare("ac-4", "Uniting AgeWell Hawthorn Community", "Uniting AgeWell", "VIC", "Hawthorn", "mel", 120, true, -37.8223, 145.0345),
        AustralianAgedCare("ac-5", "Aegis Amberley Aged Care", "Aegis Aged Care Group", "WA", "Spearwood", "stirling", 134, true, -32.0988, 115.7765)
    )

    val industrial: List<AustralianIndustrial> = listOf(
        AustralianIndustrial("ind-1", "Port of Brisbane Maritime & Logistics Precinct", "Intermodal Container Terminal", "QLD", "bne", 480.0, listOf("DP World", "Patrick Terminals", "Qube Logistics"), "Class 2 / 3 Hazardous Cargo Safe Zone", -27.3750, 153.1667),
        AustralianIndustrial("ind-2", "Camellia - Rosehill Heavy Industrial Estate", "Chemical & Energy Storage", "NSW", "parramatta", 320.0, listOf("Viva Energy", "Boral Concrete", "Veolia Environmental"), "Major Hazard Facility (MHF) Tier 1", -33.8290, 151.0250),
        AustralianIndustrial("ind-3", "Fisherman Islands Trade Coast", "Advanced Manufacturing & Cold Chain", "QLD", "bne", 210.0, listOf("GrainCorp", "Inghams", "Linfox"), "General Industrial Standard", -27.3820, 153.1780),
        AustralianIndustrial("ind-4", "Kwinana Strategic Industrial Area", "Heavy Industry & Clean Energy", "WA", "stirling", 650.0, listOf("BHP Nickel West", "Tianqi Lithium", "BP Australia"), "Critical Infrastructure Protective Security Act", -32.2280, 115.7680),
        AustralianIndustrial("ind-5", "Fishermans Bend Innovation Precinct", "Aerospace, Defence & Engineering", "VIC", "mel", 480.0, listOf("Boeing Aerostructures", "DSTG Defence", "Siemens"), "High Security Advanced R&D", -37.8310, 144.9140)
    )
}
