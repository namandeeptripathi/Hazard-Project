package com.hazard.domain.infrastructure;

/**
 * Normalized Infrastructure Categories for Stage 4.3 Infrastructure Exposure.
 */
public enum InfrastructureCategory {
    HEALTHCARE("Healthcare", "#E91E63", InfrastructureCriticality.HIGH, "Hospitals, primary health centers, clinics, and medical dispensaries"),
    EDUCATION("Education", "#9C27B0", InfrastructureCriticality.MODERATE, "Schools, colleges, training centers, and educational institutes"),
    TRANSPORT("Transport", "#2196F3", InfrastructureCriticality.HIGH, "Bridges, roads, railway stations, river corridors, and transport hubs"),
    EMERGENCY_SERVICES("Emergency Services", "#F44336", InfrastructureCriticality.VERY_HIGH, "Fire stations, police posts, disaster response centers, and relief shelters"),
    GOVERNMENT("Government", "#673AB7", InfrastructureCriticality.MODERATE, "Collectorates, block development offices, panchayat bhawans, and administrative offices"),
    POWER("Power & Energy", "#FF9800", InfrastructureCriticality.HIGH, "Electrical substations, power transformers, and energy supply nodes"),
    WATER("Water & Drainage", "#00BCD4", InfrastructureCriticality.HIGH, "Dams, canals, reservoirs, weirs, lock gates, retention basins, and drainage networks"),
    COMMUNICATION("Communication", "#3F51B5", InfrastructureCriticality.MODERATE, "Telecommunication towers, post offices, and digital relay stations"),
    OTHER_CRITICAL("Other Critical", "#607D8B", InfrastructureCriticality.MODERATE, "Fuel storage depots, essential warehouses, and supply logistics");

    private final String displayName;
    private final String colorHex;
    private final InfrastructureCriticality defaultCriticality;
    private final String description;

    InfrastructureCategory(String displayName, String colorHex, InfrastructureCriticality defaultCriticality, String description) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.defaultCriticality = defaultCriticality;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public InfrastructureCriticality getDefaultCriticality() {
        return defaultCriticality;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Resolves an InfrastructureCategory from a subtype string (e.g. "dam", "hospital", "canal").
     */
    public static InfrastructureCategory fromSubtype(String subtype) {
        if (subtype == null) return OTHER_CRITICAL;
        String s = subtype.trim().toLowerCase();

        if (s.contains("hospital") || s.contains("clinic") || s.contains("health") || s.contains("medical") || s.contains("pharmacy")) {
            return HEALTHCARE;
        }
        if (s.contains("school") || s.contains("college") || s.contains("university") || s.contains("education") || s.contains("institute")) {
            return EDUCATION;
        }
        if (s.contains("bridge") || s.contains("road") || s.contains("railway") || s.contains("station") || s.contains("highway") || s.contains("river")) {
            return TRANSPORT;
        }
        if (s.contains("fire") || s.contains("police") || s.contains("emergency") || s.contains("shelter") || s.contains("disaster")) {
            return EMERGENCY_SERVICES;
        }
        if (s.contains("government") || s.contains("collectorate") || s.contains("office") || s.contains("panchayat") || s.contains("block")) {
            return GOVERNMENT;
        }
        if (s.contains("power") || s.contains("substation") || s.contains("electric") || s.contains("grid") || s.contains("transformer")) {
            return POWER;
        }
        if (s.contains("dam") || s.contains("canal") || s.contains("reservoir") || s.contains("weir") || s.contains("drain") ||
            s.contains("water") || s.contains("lock_gate") || s.contains("basin") || s.contains("ditch")) {
            return WATER;
        }
        if (s.contains("telecom") || s.contains("tower") || s.contains("communication") || s.contains("post")) {
            return COMMUNICATION;
        }
        return OTHER_CRITICAL;
    }
}
