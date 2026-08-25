package com.hazard.dto.risk;

/**
 * Encapsulates an individual driver/contributor to final disaster risk.
 */
public class RiskContributorDto {

    private String name;
    private String pillar;
    private Double normalizedScore;
    private Double effectiveWeight;
    private Double contribution;
    private String description;

    public RiskContributorDto() {}

    public RiskContributorDto(String name, String pillar, Double normalizedScore, Double effectiveWeight, Double contribution, String description) {
        this.name = name;
        this.pillar = pillar;
        this.normalizedScore = normalizedScore;
        this.effectiveWeight = effectiveWeight;
        this.contribution = contribution;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPillar() {
        return pillar;
    }

    public void setPillar(String pillar) {
        this.pillar = pillar;
    }

    public Double getNormalizedScore() {
        return normalizedScore;
    }

    public void setNormalizedScore(Double normalizedScore) {
        this.normalizedScore = normalizedScore;
    }

    public Double getEffectiveWeight() {
        return effectiveWeight;
    }

    public void setEffectiveWeight(Double effectiveWeight) {
        this.effectiveWeight = effectiveWeight;
    }

    public Double getContribution() {
        return contribution;
    }

    public void setContribution(Double contribution) {
        this.contribution = contribution;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
