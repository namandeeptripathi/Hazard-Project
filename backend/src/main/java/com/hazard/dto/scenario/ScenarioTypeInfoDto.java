package com.hazard.dto.scenario;

import com.hazard.domain.scenario.ScenarioType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9A — DTO describing supported Scenario Types and metadata for clients.
 */
@Schema(description = "Information on supported scenario simulation types")
public class ScenarioTypeInfoDto {

    @Schema(description = "Scenario type identifier code", example = "RAINFALL_CHANGE")
    private String type;

    @Schema(description = "Human-readable display name", example = "Rainfall Change Scenario")
    private String displayName;

    @Schema(description = "Detailed description of the scenario type", example = "Simulates precipitation anomalies impacting hydrological discharge.")
    private String description;

    @Schema(description = "True if this type represents the unperturbed baseline reference", example = "false")
    private boolean baseline;

    public ScenarioTypeInfoDto() {
    }

    public ScenarioTypeInfoDto(String type, String displayName, String description, boolean baseline) {
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.baseline = baseline;
    }

    public static ScenarioTypeInfoDto fromScenarioType(ScenarioType scenarioType) {
        if (scenarioType == null) return null;
        return new ScenarioTypeInfoDto(
                scenarioType.name(),
                scenarioType.getDisplayName(),
                scenarioType.getDescription(),
                scenarioType.isBaseline()
        );
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isBaseline() {
        return baseline;
    }

    public void setBaseline(boolean baseline) {
        this.baseline = baseline;
    }
}
