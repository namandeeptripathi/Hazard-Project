package com.hazard.dto.risk.contributor;

import com.hazard.domain.risk.contributor.ContributorImportance;

import java.util.ArrayList;
import java.util.List;

/**
 * Node for hierarchical risk contributor tree representation.
 */
public class ContributorTreeNodeDto {

    private String id;
    private String name;
    private int level;
    private Double score;
    private Double effectiveWeight;
    private Double contribution;
    private Double contributionPercent;
    private ContributorImportance importance;
    private String colorHex;
    private List<ContributorTreeNodeDto> children = new ArrayList<>();

    public ContributorTreeNodeDto() {}

    public ContributorTreeNodeDto(String id, String name, int level, Double score,
                                  Double effectiveWeight, Double contribution, Double contributionPercent,
                                  ContributorImportance importance, String colorHex) {
        this.id = id;
        this.name = name;
        this.level = level;
        this.score = score;
        this.effectiveWeight = effectiveWeight;
        this.contribution = contribution;
        this.contributionPercent = contributionPercent;
        this.importance = importance;
        this.colorHex = colorHex;
    }

    public void addChild(ContributorTreeNodeDto child) {
        if (child != null) {
            this.children.add(child);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Double getEffectiveWeight() { return effectiveWeight; }
    public void setEffectiveWeight(Double effectiveWeight) { this.effectiveWeight = effectiveWeight; }

    public Double getContribution() { return contribution; }
    public void setContribution(Double contribution) { this.contribution = contribution; }

    public Double getContributionPercent() { return contributionPercent; }
    public void setContributionPercent(Double contributionPercent) { this.contributionPercent = contributionPercent; }

    public ContributorImportance getImportance() { return importance; }
    public void setImportance(ContributorImportance importance) { this.importance = importance; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public List<ContributorTreeNodeDto> getChildren() { return children; }
    public void setChildren(List<ContributorTreeNodeDto> children) { this.children = children != null ? children : new ArrayList<>(); }
}
