package com.hazard.dto.relocation.explain;

/**
 * Stage 7C.5 — Decision Contributor DTO.
 *
 * Represents an auditable, structured decision factor/contributor for either
 * Stage 7A (Priority scoring) or Stage 7B (Destination suitability scoring).
 */
public class DecisionContributorDto {

    private String contributorKey;
    private String displayName;
    private String category;           // "PRIORITY" or "DESTINATION"
    private Object rawValue;           // e.g. 0.85, 2500, "HIGH", "4.2 km"
    private Double normalizedScore;    // Normalized score in [0.0, 1.0]
    private Double weight;             // Configured weight (e.g. 0.30)
    private Double weightedImpact;      // normalizedScore * weight
    private Double impactPercentage;   // (weightedImpact / totalScore) * 100
    private String impactDirection;    // "HIGH_IMPACT", "MODERATE_IMPACT", "LOW_IMPACT", "NEUTRAL"
    private String interpretation;     // Concise human-readable interpretation

    public DecisionContributorDto() {
    }

    public DecisionContributorDto(String contributorKey, String displayName, String category,
                                  Object rawValue, Double normalizedScore, Double weight,
                                  Double weightedImpact, String interpretation) {
        this.contributorKey = contributorKey;
        this.displayName = displayName;
        this.category = category;
        this.rawValue = rawValue;
        this.normalizedScore = normalizedScore;
        this.weight = weight;
        this.weightedImpact = weightedImpact;
        this.interpretation = interpretation;
        if (weightedImpact != null) {
            if (weightedImpact >= 0.20) {
                this.impactDirection = "HIGH_IMPACT";
            } else if (weightedImpact >= 0.08) {
                this.impactDirection = "MODERATE_IMPACT";
            } else {
                this.impactDirection = "LOW_IMPACT";
            }
        }
    }

    // --- Getters and Setters ---

    public String getContributorKey() {
        return contributorKey;
    }

    public void setContributorKey(String contributorKey) {
        this.contributorKey = contributorKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Object getRawValue() {
        return rawValue;
    }

    public void setRawValue(Object rawValue) {
        this.rawValue = rawValue;
    }

    public Double getNormalizedScore() {
        return normalizedScore;
    }

    public void setNormalizedScore(Double normalizedScore) {
        this.normalizedScore = normalizedScore;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getWeightedImpact() {
        return weightedImpact;
    }

    public void setWeightedImpact(Double weightedImpact) {
        this.weightedImpact = weightedImpact;
    }

    public Double getImpactPercentage() {
        return impactPercentage;
    }

    public void setImpactPercentage(Double impactPercentage) {
        this.impactPercentage = impactPercentage;
    }

    public String getImpactDirection() {
        return impactDirection;
    }

    public void setImpactDirection(String impactDirection) {
        this.impactDirection = impactDirection;
    }

    public String getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(String interpretation) {
        this.interpretation = interpretation;
    }
}
