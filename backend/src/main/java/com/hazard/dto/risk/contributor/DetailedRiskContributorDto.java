package com.hazard.dto.risk.contributor;

import com.hazard.domain.risk.contributor.ContributorDataAvailability;
import com.hazard.domain.risk.contributor.ContributorDirection;
import com.hazard.domain.risk.contributor.ContributorImportance;

/**
 * Detailed risk contributor record with absolute contribution, percentage share of total risk,
 * hierarchical level, ranking, and explanation metadata.
 */
public class DetailedRiskContributorDto {

    private String id;                       // e.g. "HAZARD_SEVERITY", "POPULATION_EXPOSURE"
    private String name;                     // Human-readable title
    private String parentPillar;             // "HAZARD", "EXPOSURE", "VULNERABILITY", "HISTORICAL"
    private int level;                       // 1=Top Pillar, 2=Sub-Component, 3=Indicator/Evidence

    private Double rawValue;
    private Double normalizedScore;          // Value in [0.0000, 1.0000]
    private Double configuredWeight;         // Configured nominal weight
    private Double effectiveWeight;          // Effective weight after active redistribution
    private Double contribution;             // Absolute contribution = effectiveWeight * normalizedScore
    private Double contributionPercent;      // Relative share = (contribution / totalRisk) * 100%

    private int rank;
    private ContributorImportance importance;
    private ContributorDirection direction;
    private ContributorDataAvailability dataAvailability;

    private String provenance;               // Source stage / dataset (e.g. "Stage 3 Multi-Hazard Engine")
    private String description;

    public DetailedRiskContributorDto() {
        this.direction = ContributorDirection.INCREASES_RISK;
        this.dataAvailability = ContributorDataAvailability.AVAILABLE;
    }

    public DetailedRiskContributorDto(String id, String name, String parentPillar, int level,
                                      Double rawValue, Double normalizedScore, Double configuredWeight,
                                      Double effectiveWeight, Double contribution, Double contributionPercent,
                                      int rank, ContributorImportance importance, String provenance, String description) {
        this.id = id;
        this.name = name;
        this.parentPillar = parentPillar;
        this.level = level;
        this.rawValue = rawValue;
        this.normalizedScore = normalizedScore;
        this.configuredWeight = configuredWeight;
        this.effectiveWeight = effectiveWeight;
        this.contribution = contribution;
        this.contributionPercent = contributionPercent;
        this.rank = rank;
        this.importance = importance;
        this.direction = ContributorDirection.INCREASES_RISK;
        this.dataAvailability = ContributorDataAvailability.AVAILABLE;
        this.provenance = provenance;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getParentPillar() { return parentPillar; }
    public void setParentPillar(String parentPillar) { this.parentPillar = parentPillar; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public Double getRawValue() { return rawValue; }
    public void setRawValue(Double rawValue) { this.rawValue = rawValue; }

    public Double getNormalizedScore() { return normalizedScore; }
    public void setNormalizedScore(Double normalizedScore) { this.normalizedScore = normalizedScore; }

    public Double getConfiguredWeight() { return configuredWeight; }
    public void setConfiguredWeight(Double configuredWeight) { this.configuredWeight = configuredWeight; }

    public Double getEffectiveWeight() { return effectiveWeight; }
    public void setEffectiveWeight(Double effectiveWeight) { this.effectiveWeight = effectiveWeight; }

    public Double getContribution() { return contribution; }
    public void setContribution(Double contribution) { this.contribution = contribution; }

    public Double getContributionPercent() { return contributionPercent; }
    public void setContributionPercent(Double contributionPercent) { this.contributionPercent = contributionPercent; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public ContributorImportance getImportance() { return importance; }
    public void setImportance(ContributorImportance importance) { this.importance = importance; }

    public ContributorDirection getDirection() { return direction; }
    public void setDirection(ContributorDirection direction) { this.direction = direction; }

    public ContributorDataAvailability getDataAvailability() { return dataAvailability; }
    public void setDataAvailability(ContributorDataAvailability dataAvailability) { this.dataAvailability = dataAvailability; }

    public String getProvenance() { return provenance; }
    public void setProvenance(String provenance) { this.provenance = provenance; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
