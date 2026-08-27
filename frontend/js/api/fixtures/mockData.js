/**
 * Stage 8B & 8C — Isolated Development Fixtures
 *
 * Provides authentic, DTO-compliant fallback data for Bihar / Sitamarhi operational district
 * when the Spring Boot backend server is starting up or offline in development mode.
 */

export const MOCK_DISTRICT_SUMMARY = {
    districtName: "Sitamarhi",
    state: "Bihar",
    totalSettlementsEvaluated: 18,
    exposedSettlementsCount: 14,
    highRiskSettlementsCount: 8,
    totalVulnerablePopulation: 94293,
    activeDisasterType: "FLOOD",
    disasterSeverity: "HIGH",
    operationalStatus: "ACTIVE_RELOCATION_MONITORING"
};

export const MOCK_SETTLEMENTS = [
    {
        habitationId: "HAB-SIT-001",
        settlementName: "Sonbarsa Flood Inundation Area",
        district: "Sitamarhi",
        block: "Sonbarsa Block",
        latitude: 26.6850,
        longitude: 85.5240,
        population: 5000,
        riskScore: 0.92,
        priorityScore: 0.88,
        priorityLevel: "IMMEDIATE",
        exposureTier: "CRITICAL",
        isRedZone: true,
        floodDepthMeters: 2.8,
        recommendedSiteId: "FAC-EMG-003",
        recommendedSiteName: "Sitamarhi Central Flood Shelter",
        transitDistanceKm: 2.50,
        hazardDetails: {
            primaryHazard: "Monsoon Riverine Flood Inundation",
            severity: "SEVERE",
            waterDepth: "2.8 meters (Danger Mark +1.4m)",
            embankmentAlert: "High Seepage / Breach Hazard",
            terrain: "Flat Alluvial Floodplain (<1° slope, Poor Natural Drainage)",
            rainfallScore: "0.85 (Extreme 72-Hour Precipitation)"
        },
        vulnerabilityDetails: {
            demographicScore: "0.84 (High)",
            demographicDesc: "High dependent demographic (34% elderly & children)",
            housingScore: "0.89 (Very High)",
            housingDesc: "78% kachha unreinforced thatch & mud construction",
            healthcareAccess: ">45 mins travel time (Isolated during flood crest)",
            roadClearance: "Submerged arterial connection (0.8m water over crest)",
            copingCapacity: "LOW (Limited municipal disaster reserves)"
        },
        decisionRationale: {
            who: "Sonbarsa Flood Inundation Area (5,000 vulnerable evacuees) classified as Immediate Priority (Score: 0.88/1.00).",
            where: "Relocate to 'Sitamarhi Central Flood Shelter' [FAC-EMG-003] (Transit: 2.50 km, Destination Score: 0.8950).",
            why: "Origin has IMMEDIATE priority; destination passed all 5 mandatory feasibility gates and ranked #1 with optimal multi-criteria suitability (94/100) and proximity.",
            action: "DEPLOY IMMEDIATELY: Issue mandatory evacuation order for origin habitation and commence convoy transport to 'Sitamarhi Central Flood Shelter'.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.92", impact: "High Impact" },
                { name: "Hazard Intensity & Red-Zone Depth (15%)", value: "0.85", impact: "Red-Zone Inundation" },
                { name: "Population Exposure Magnitude (20%)", value: "0.70", impact: "5,000 Evacuees" },
                { name: "Relocation Urgency Index (10%)", value: "1.00", impact: "Critical Immediate" }
            ]
        }
    },
    {
        habitationId: "HAB-SIT-002",
        settlementName: "Bairgania Embankment Buffer",
        district: "Sitamarhi",
        block: "Bairgania Block",
        latitude: 26.6120,
        longitude: 85.3180,
        population: 3200,
        riskScore: 0.78,
        priorityScore: 0.64,
        priorityLevel: "SHORT_TERM",
        exposureTier: "HIGH",
        isRedZone: true,
        floodDepthMeters: 1.9,
        recommendedSiteId: "FAC-EMG-005",
        recommendedSiteName: "Pupri Cyclone & Flood Relief Centre",
        transitDistanceKm: 6.20,
        hazardDetails: {
            primaryHazard: "Bagmati River Embankment Overflow",
            severity: "HIGH",
            waterDepth: "1.9 meters",
            embankmentAlert: "Active Wave Overtopping Warning",
            terrain: "Embankment toe slope (Marginal Stability)",
            rainfallScore: "0.72 (Heavy Continuous Rainfall)"
        },
        vulnerabilityDetails: {
            demographicScore: "0.71 (High)",
            demographicDesc: "Dense riverine settlement cluster",
            housingScore: "0.75 (High)",
            housingDesc: "62% semi-pucca structures vulnerable to erosion",
            healthcareAccess: "30-40 mins travel time",
            roadClearance: "Elevated ring bund accessible by tractor/truck",
            copingCapacity: "MODERATE"
        },
        decisionRationale: {
            who: "Bairgania Embankment Buffer (3,200 vulnerable evacuees) classified as Short-Term Priority (Score: 0.64/1.00).",
            where: "Relocate to 'Pupri Cyclone & Flood Relief Centre' [FAC-EMG-005] (Transit: 6.20 km, Destination Score: 0.8420).",
            why: "Origin has SHORT_TERM priority; destination passed all feasibility gates with 91% available capacity headroom.",
            action: "STAGE CONVOY: Place NDRF logistics units on 2-hour standby and alert district transport nodal officers.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.78", impact: "High Risk" },
                { name: "Hazard Intensity & Red-Zone Depth (15%)", value: "0.75", impact: "Active Overflow" },
                { name: "Population Exposure Magnitude (20%)", value: "0.60", impact: "3,200 Evacuees" },
                { name: "Relocation Urgency Index (10%)", value: "0.75", impact: "High Urgency" }
            ]
        }
    },
    {
        habitationId: "HAB-SIT-003",
        settlementName: "Riga Lowland Settlement",
        district: "Sitamarhi",
        block: "Riga Block",
        latitude: 26.5810,
        longitude: 85.4200,
        population: 1800,
        riskScore: 0.55,
        priorityScore: 0.32,
        priorityLevel: "MEDIUM_TERM",
        exposureTier: "MODERATE",
        isRedZone: false,
        floodDepthMeters: 0.8,
        recommendedSiteId: "FAC-EMG-008",
        recommendedSiteName: "Dumra High School Community Center",
        transitDistanceKm: 4.10,
        hazardDetails: {
            primaryHazard: "Surface Waterlogging & Crop Inundation",
            severity: "MODERATE",
            waterDepth: "0.8 meters",
            embankmentAlert: "No Immediate Breach Risk",
            terrain: "Agricultural Plain",
            rainfallScore: "0.50 (Moderate Monsoon Runoff)"
        },
        vulnerabilityDetails: {
            demographicScore: "0.52 (Moderate)",
            demographicDesc: "Scattered farming hamlets",
            housingScore: "0.48 (Moderate)",
            housingDesc: "Mixed pucca and brick structures",
            healthcareAccess: "20 mins travel time",
            roadClearance: "Paved district road dry and passable",
            copingCapacity: "MODERATE"
        },
        decisionRationale: {
            who: "Riga Lowland Settlement (1,800 evacuees) classified as Medium-Term Priority (Score: 0.32/1.00).",
            where: "Relocate to 'Dumra High School Community Center' [FAC-EMG-008] (Transit: 4.10 km, Destination Score: 0.7890).",
            why: "Origin has MEDIUM_TERM priority; shelter capacity is sufficient and access routes remain clear.",
            action: "MONITOR WATERLOGGING: Maintain regular drone surveillance and prepare secondary shelters.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.55", impact: "Moderate" },
                { name: "Hazard Intensity (15%)", value: "0.45", impact: "Waterlogging" },
                { name: "Population Exposure (20%)", value: "0.40", impact: "1,800 Evacuees" },
                { name: "Relocation Urgency (10%)", value: "0.40", impact: "Moderate" }
            ]
        }
    },
    {
        habitationId: "HAB-SIT-004",
        settlementName: "Sursand Border Habitation",
        district: "Sitamarhi",
        block: "Sursand Block",
        latitude: 26.6450,
        longitude: 85.7020,
        population: 2400,
        riskScore: 0.84,
        priorityScore: 0.79,
        priorityLevel: "IMMEDIATE",
        exposureTier: "CRITICAL",
        isRedZone: true,
        floodDepthMeters: 2.3,
        recommendedSiteId: "FAC-EMG-003",
        recommendedSiteName: "Sitamarhi Central Flood Shelter",
        transitDistanceKm: 7.80,
        hazardDetails: {
            primaryHazard: "Flash Flood / Transboundary Runoff",
            severity: "SEVERE",
            waterDepth: "2.3 meters",
            embankmentAlert: "Flash Inundation Surge",
            terrain: "Low-lying catchment depression",
            rainfallScore: "0.88 (Extreme Torrential Downpour)"
        },
        vulnerabilityDetails: {
            demographicScore: "0.79 (High)",
            demographicDesc: "Border agricultural community with limited local facilities",
            housingScore: "0.82 (High)",
            housingDesc: "70% kachha structures",
            healthcareAccess: ">40 mins travel time",
            roadClearance: "Border road partially inundated at culverts",
            copingCapacity: "LOW"
        },
        decisionRationale: {
            who: "Sursand Border Habitation (2,400 vulnerable evacuees) classified as Immediate Priority (Score: 0.79/1.00).",
            where: "Relocate to 'Sitamarhi Central Flood Shelter' [FAC-EMG-003] (Transit: 7.80 km, Destination Score: 0.8650).",
            why: "Rapidly rising transboundary flash flood poses imminent danger to border habitation.",
            action: "DEPLOY IMMEDIATELY: Issue priority evacuation convoy order via elevated state highway corridor.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.84", impact: "Severe" },
                { name: "Hazard Intensity (15%)", value: "0.82", impact: "Flash Flood" },
                { name: "Population Exposure (20%)", value: "0.65", impact: "2,400 Evacuees" },
                { name: "Relocation Urgency (10%)", value: "0.95", impact: "Urgent" }
            ]
        }
    },
    {
        habitationId: "HAB-SIT-005",
        settlementName: "Runni Saidpur Agricultural Cluster",
        district: "Sitamarhi",
        block: "Runni Saidpur Block",
        latitude: 26.3980,
        longitude: 85.5120,
        population: 4100,
        riskScore: 0.42,
        priorityScore: 0.28,
        priorityLevel: "MEDIUM_TERM",
        exposureTier: "MODERATE",
        isRedZone: false,
        floodDepthMeters: 0.6,
        recommendedSiteId: "FAC-EDU-012",
        recommendedSiteName: "Saidpur Inter College Disaster Camp",
        transitDistanceKm: 3.40,
        hazardDetails: {
            primaryHazard: "River Overflow Margin",
            severity: "MODERATE",
            waterDepth: "0.6 meters",
            embankmentAlert: "Stable Buffer Zone",
            terrain: "Elevated Terrace",
            rainfallScore: "0.45"
        },
        vulnerabilityDetails: {
            demographicScore: "0.48 (Moderate)",
            demographicDesc: "Large rural population with local road connectivity",
            housingScore: "0.42 (Moderate)",
            housingDesc: "55% reinforced masonry houses",
            healthcareAccess: "15 mins travel time (Near Community Health Center)",
            roadClearance: "National Highway 77 accessible",
            copingCapacity: "MODERATE_HIGH"
        },
        decisionRationale: {
            who: "Runni Saidpur Agricultural Cluster (4,100 evacuees) classified as Medium-Term Priority (Score: 0.28/1.00).",
            where: "Relocate to 'Saidpur Inter College Disaster Camp' [FAC-EDU-012] (Transit: 3.40 km, Destination Score: 0.8120).",
            why: "Area remains stable with moderate waterlogging; safe shelter is situated within 3.4 km.",
            action: "MAINTAIN WATCH: Monitor river discharge from upstream barrages.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.42", impact: "Moderate" },
                { name: "Hazard Intensity (15%)", value: "0.35", impact: "Low Overflow" },
                { name: "Population Exposure (20%)", value: "0.55", impact: "4,100 Population" },
                { name: "Relocation Urgency (10%)", value: "0.30", impact: "Low Urgency" }
            ]
        }
    },
    {
        habitationId: "HAB-SIT-006",
        settlementName: "Majorganj Riverine Settlement",
        district: "Sitamarhi",
        block: "Majorganj Block",
        latitude: 26.7110,
        longitude: 85.4410,
        population: 2900,
        riskScore: 0.89,
        priorityScore: 0.82,
        priorityLevel: "IMMEDIATE",
        exposureTier: "CRITICAL",
        isRedZone: true,
        floodDepthMeters: 2.5,
        recommendedSiteId: "FAC-EMG-005",
        recommendedSiteName: "Pupri Cyclone & Flood Relief Centre",
        transitDistanceKm: 8.10,
        hazardDetails: {
            primaryHazard: "Monsoon Riverine Flood Inundation",
            severity: "SEVERE",
            waterDepth: "2.5 meters (Danger Mark +1.2m)",
            embankmentAlert: "Severe Erosion Threat on Left Bank",
            terrain: "Depression Basin (<0.5° slope)",
            rainfallScore: "0.82"
        },
        vulnerabilityDetails: {
            demographicScore: "0.81 (High)",
            demographicDesc: "High vulnerable density with 40% agrarian workers",
            housingScore: "0.85 (Very High)",
            housingDesc: "74% unreinforced mud & bamboo huts",
            healthcareAccess: ">45 mins travel time",
            roadClearance: "Secondary link road submerged",
            copingCapacity: "LOW"
        },
        decisionRationale: {
            who: "Majorganj Riverine Settlement (2,900 vulnerable evacuees) classified as Immediate Priority (Score: 0.82/1.00).",
            where: "Relocate to 'Pupri Cyclone & Flood Relief Centre' [FAC-EMG-005] (Transit: 8.10 km, Destination Score: 0.8750).",
            why: "Origin has IMMEDIATE priority; high flood depth and riverbank erosion mandate immediate evacuation.",
            action: "DEPLOY IMMEDIATELY: Dispatch amphibious NDRF rescue boats and transport buses to primary evacuation point.",
            contributors: [
                { name: "Multi-Hazard Risk Severity (30%)", value: "0.89", impact: "Severe" },
                { name: "Hazard Intensity (15%)", value: "0.80", impact: "Red-Zone Inundation" },
                { name: "Population Exposure (20%)", value: "0.68", impact: "2,900 Evacuees" },
                { name: "Relocation Urgency (10%)", value: "0.90", impact: "Critical" }
            ]
        }
    }
];

export const MOCK_SAFE_SITES = [
    {
        siteId: "FAC-EMG-003",
        name: "Sitamarhi Central Flood Shelter",
        district: "Sitamarhi",
        category: "EMERGENCY_SHELTER",
        latitude: 26.5950,
        longitude: 85.5030,
        totalCapacity: 5000,
        allocatedCapacity: 850,
        availableCapacity: 4150,
        hazardSafetyStatus: "SAFE",
        terrainStatus: "FAVORABLE",
        suitabilityClass: "HIGHLY_SUITABLE",
        suitabilityScore: 0.94,
        rank: 1,
        roadAccessStatus: "NEAR",
        healthcareAccessStatus: "NEAR",
        powerBackup: true,
        sanitationFacilities: "32 Units (Operational)",
        medicalUnit: "Primary Emergency First Aid Station",
        assignedHabitations: ["Sonbarsa Flood Inundation Area", "Sursand Border Habitation"]
    },
    {
        siteId: "FAC-EMG-005",
        name: "Pupri Cyclone & Flood Relief Centre",
        district: "Sitamarhi",
        category: "EMERGENCY_SHELTER",
        latitude: 26.5420,
        longitude: 85.6780,
        totalCapacity: 6000,
        allocatedCapacity: 540,
        availableCapacity: 5460,
        hazardSafetyStatus: "SAFE",
        terrainStatus: "FAVORABLE",
        suitabilityClass: "HIGHLY_SUITABLE",
        suitabilityScore: 0.91,
        rank: 2,
        roadAccessStatus: "NEAR",
        healthcareAccessStatus: "NEAR",
        powerBackup: true,
        sanitationFacilities: "40 Units (Operational)",
        medicalUnit: "Mobile Disaster Medical Unit",
        assignedHabitations: ["Bairgania Embankment Buffer", "Majorganj Riverine Settlement"]
    },
    {
        siteId: "FAC-EMG-008",
        name: "Dumra High School Community Center",
        district: "Sitamarhi",
        category: "EDUCATION",
        latitude: 26.5620,
        longitude: 85.5180,
        totalCapacity: 4000,
        allocatedCapacity: 1400,
        availableCapacity: 2600,
        hazardSafetyStatus: "SAFE",
        terrainStatus: "FAVORABLE",
        suitabilityClass: "SUITABLE",
        suitabilityScore: 0.85,
        rank: 3,
        roadAccessStatus: "NEAR",
        healthcareAccessStatus: "MODERATE",
        powerBackup: false,
        sanitationFacilities: "20 Units (Functional)",
        medicalUnit: "Auxiliary First Aid Centre",
        assignedHabitations: ["Riga Lowland Settlement", "Sonbarsa Flood Inundation Area (Overflow)"]
    },
    {
        siteId: "FAC-EDU-012",
        name: "Saidpur Inter College Disaster Camp",
        district: "Sitamarhi",
        category: "EDUCATION",
        latitude: 26.4150,
        longitude: 85.5350,
        totalCapacity: 3500,
        allocatedCapacity: 0,
        availableCapacity: 3500,
        hazardSafetyStatus: "SAFE",
        terrainStatus: "FAVORABLE",
        suitabilityClass: "SUITABLE",
        suitabilityScore: 0.82,
        rank: 4,
        roadAccessStatus: "NEAR",
        healthcareAccessStatus: "MODERATE",
        powerBackup: false,
        sanitationFacilities: "18 Units (Functional)",
        medicalUnit: "Auxiliary First Aid Centre",
        assignedHabitations: ["Runni Saidpur Agricultural Cluster"]
    }
];

export const MOCK_RELOCATION_CASES = [
    {
        caseId: "REL-CASE-001",
        habitationId: "HAB-SIT-001",
        habitationName: "Sonbarsa Flood Inundation Area",
        district: "Sitamarhi",
        population: 5000,
        priorityLevel: "IMMEDIATE",
        priorityScore: 0.88,
        riskScore: 0.92,
        originCoordinates: [26.6850, 85.5240],
        primaryDestination: {
            siteId: "FAC-EMG-003",
            siteName: "Sitamarhi Central Flood Shelter",
            suitabilityClass: "HIGHLY_SUITABLE",
            suitabilityScore: 0.94,
            availableCapacity: 4150,
            allocatedPopulation: 4150,
            transitDistanceKm: 2.50,
            coordinates: [26.5950, 85.5030]
        },
        hasCapacityGap: true,
        capacityShortfall: 850,
        overflowDestination: {
            siteId: "FAC-EMG-008",
            siteName: "Dumra High School Community Center",
            suitabilityClass: "SUITABLE",
            suitabilityScore: 0.85,
            availableCapacity: 2600,
            allocatedPopulation: 850,
            transitDistanceKm: 4.80,
            coordinates: [26.5620, 85.5180]
        },
        totalAllocated: 5000,
        feasibilityStatus: "FEASIBLE_WITH_OVERFLOW",
        feasibilityGates: [
            { gate: "Gate 1: Hazard Safety", status: "PASS", detail: "Destination is situated outside active flood inundation boundary" },
            { gate: "Gate 2: Transit Distance", status: "PASS", detail: "2.50 km is well within the 25.0 km emergency evacuation threshold" },
            { gate: "Gate 3: Road Access", status: "PASS", detail: "Elevated all-weather NH link passable for high-clearance transport" },
            { gate: "Gate 4: Healthcare Proximity", status: "PASS", detail: "Dedicated medical triage unit available on shelter premises" },
            { gate: "Gate 5: Single-Site Capacity", status: "PARTIAL", detail: "4,150 beds absorbed at primary site; remaining 850 evacuees routed to Dumra High School" }
        ],
        decisionRationale: {
            who: "Sonbarsa Flood Inundation Area (5,000 vulnerable evacuees) classified as Immediate Priority (Score: 0.88/1.00).",
            where: "Relocate 4,150 evacuees to 'Sitamarhi Central Flood Shelter' and 850 evacuees to 'Dumra High School'.",
            why: "Origin has IMMEDIATE priority; primary shelter provides optimal proximity (2.5 km) with 4,150 headroom, overflow cleanly absorbed by Dumra (4.8 km).",
            action: "DEPLOY MULTI-CONVOY: Issue mandatory evacuation order. Dispatch Convoy Alpha (4,150) to Central Shelter and Convoy Beta (850) to Dumra Center."
        }
    },
    {
        caseId: "REL-CASE-002",
        habitationId: "HAB-SIT-002",
        habitationName: "Bairgania Embankment Buffer",
        district: "Sitamarhi",
        population: 3200,
        priorityLevel: "SHORT_TERM",
        priorityScore: 0.64,
        riskScore: 0.78,
        originCoordinates: [26.6120, 85.3180],
        primaryDestination: {
            siteId: "FAC-EMG-005",
            siteName: "Pupri Cyclone & Flood Relief Centre",
            suitabilityClass: "HIGHLY_SUITABLE",
            suitabilityScore: 0.91,
            availableCapacity: 5460,
            allocatedPopulation: 3200,
            transitDistanceKm: 6.20,
            coordinates: [26.5420, 85.6780]
        },
        hasCapacityGap: false,
        capacityShortfall: 0,
        overflowDestination: null,
        totalAllocated: 3200,
        feasibilityStatus: "FEASIBLE",
        feasibilityGates: [
            { gate: "Gate 1: Hazard Safety", status: "PASS", detail: "Destination is elevated above highest recorded flood level" },
            { gate: "Gate 2: Transit Distance", status: "PASS", detail: "6.20 km transit corridor is fully feasible" },
            { gate: "Gate 3: Road Access", status: "PASS", detail: "Elevated ring bund road passable for convoy trucks" },
            { gate: "Gate 4: Healthcare Proximity", status: "PASS", detail: "Mobile disaster medical unit operational" },
            { gate: "Gate 5: Single-Site Capacity", status: "PASS", detail: "3,200 evacuees accommodated with +2,260 surplus headroom remaining" }
        ],
        decisionRationale: {
            who: "Bairgania Embankment Buffer (3,200 evacuees) classified as Short-Term Priority (Score: 0.64/1.00).",
            where: "Relocate 3,200 evacuees to 'Pupri Cyclone & Flood Relief Centre' [FAC-EMG-005] (Distance: 6.20 km).",
            why: "Origin has SHORT_TERM priority; destination passed all feasibility gates with 5,460 available headroom.",
            action: "STAGE CONVOY: Place transport units on 2-hour alert and notify relief centre nodal officer."
        }
    },
    {
        caseId: "REL-CASE-003",
        habitationId: "HAB-SIT-004",
        habitationName: "Sursand Border Habitation",
        district: "Sitamarhi",
        population: 2400,
        priorityLevel: "IMMEDIATE",
        priorityScore: 0.79,
        riskScore: 0.84,
        originCoordinates: [26.6450, 85.7020],
        primaryDestination: {
            siteId: "FAC-EMG-003",
            siteName: "Sitamarhi Central Flood Shelter",
            suitabilityClass: "HIGHLY_SUITABLE",
            suitabilityScore: 0.94,
            availableCapacity: 4150,
            allocatedPopulation: 2400,
            transitDistanceKm: 7.80,
            coordinates: [26.5950, 85.5030]
        },
        hasCapacityGap: false,
        capacityShortfall: 0,
        overflowDestination: null,
        totalAllocated: 2400,
        feasibilityStatus: "FEASIBLE",
        feasibilityGates: [
            { gate: "Gate 1: Hazard Safety", status: "PASS", detail: "Outside transboundary flood surge path" },
            { gate: "Gate 2: Transit Distance", status: "PASS", detail: "7.80 km transit along state highway" },
            { gate: "Gate 3: Road Access", status: "PASS", detail: "Elevated highway clear for heavy transit" },
            { gate: "Gate 4: Healthcare Proximity", status: "PASS", detail: "Emergency medical team active" },
            { gate: "Gate 5: Single-Site Capacity", status: "PASS", detail: "2,400 evacuees accommodated within available capacity" }
        ],
        decisionRationale: {
            who: "Sursand Border Habitation (2,400 vulnerable evacuees) classified as Immediate Priority (Score: 0.79/1.00).",
            where: "Relocate to 'Sitamarhi Central Flood Shelter' [FAC-EMG-003] (Distance: 7.80 km).",
            why: "Transboundary flash flood surge necessitates rapid evacuation via state highway corridor.",
            action: "DEPLOY IMMEDIATELY: Dispatch 12 state transport buses with police escort along SH-52."
        }
    },
    {
        caseId: "REL-CASE-004",
        habitationId: "HAB-SIT-006",
        habitationName: "Majorganj Riverine Settlement",
        district: "Sitamarhi",
        population: 2900,
        priorityLevel: "IMMEDIATE",
        priorityScore: 0.82,
        riskScore: 0.89,
        originCoordinates: [26.7110, 85.4410],
        primaryDestination: {
            siteId: "FAC-EMG-005",
            siteName: "Pupri Cyclone & Flood Relief Centre",
            suitabilityClass: "HIGHLY_SUITABLE",
            suitabilityScore: 0.91,
            availableCapacity: 5460,
            allocatedPopulation: 2900,
            transitDistanceKm: 8.10,
            coordinates: [26.5420, 85.6780]
        },
        hasCapacityGap: false,
        capacityShortfall: 0,
        overflowDestination: null,
        totalAllocated: 2900,
        feasibilityStatus: "FEASIBLE",
        feasibilityGates: [
            { gate: "Gate 1: Hazard Safety", status: "PASS", detail: "Destination site safe from riverbank erosion" },
            { gate: "Gate 2: Transit Distance", status: "PASS", detail: "8.10 km transit distance is feasible" },
            { gate: "Gate 3: Road Access", status: "PASS", detail: "Amphibious boat link to elevated road staged" },
            { gate: "Gate 4: Healthcare Proximity", status: "PASS", detail: "Mobile triage on site" },
            { gate: "Gate 5: Single-Site Capacity", status: "PASS", detail: "2,900 evacuees fully absorbed with surplus headroom" }
        ],
        decisionRationale: {
            who: "Majorganj Riverine Settlement (2,900 vulnerable evacuees) classified as Immediate Priority (Score: 0.82/1.00).",
            where: "Relocate to 'Pupri Cyclone & Flood Relief Centre' [FAC-EMG-005] (Distance: 8.10 km).",
            why: "Severe riverbank erosion and 2.5m flood depth mandate emergency evacuation.",
            action: "DEPLOY IMMEDIATELY: Dispatch NDRF rescue boats to transport evacuees to staging point."
        }
    }
];

export const MOCK_CAPACITY_SUMMARY = {
    district: "Sitamarhi",
    totalValidatedSites: 4,
    totalCapacity: 18500,
    allocatedCapacity: 2790,
    availableCapacity: 15710,
    utilizationPercentage: 15.08,
    settlementsNeedingRelocation: 4,
    evacueesRequiringRelocation: 13500,
    evacueesAccommodated: 12650,
    capacityGap: 850
};

export const MOCK_HAZARD_POLYGONS = {
    type: "FeatureCollection",
    features: [
        {
            type: "Feature",
            id: "HAZ-RED-001",
            properties: {
                name: "Sonbarsa-Majorganj Red Zone Flood Basin",
                severity: "SEVERE",
                riskTier: "CRITICAL",
                waterDepth: "2.8m",
                fillColor: "#ef4444"
            },
            geometry: {
                type: "Polygon",
                coordinates: [[
                    [85.40, 26.65],
                    [85.60, 26.65],
                    [85.62, 26.75],
                    [85.42, 26.75],
                    [85.40, 26.65]
                ]]
            }
        },
        {
            type: "Feature",
            id: "HAZ-RED-002",
            properties: {
                name: "Bairgania Bagmati Overflow Zone",
                severity: "HIGH",
                riskTier: "CRITICAL",
                waterDepth: "1.9m",
                fillColor: "#ef4444"
            },
            geometry: {
                type: "Polygon",
                coordinates: [[
                    [85.28, 26.58],
                    [85.36, 26.58],
                    [85.38, 26.65],
                    [85.30, 26.65],
                    [85.28, 26.58]
                ]]
            }
        }
    ]
};

