package com.hazard.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Stage 9B — Request DTO for executing a scenario simulation.
 */
@Schema(description = "Scenario simulation execution request payload")
public class ScenarioExecutionRequestDto {

    @Schema(description = "Target administrative district to simulate (e.g. Sitamarhi, Patna, Supaul)", example = "Sitamarhi")
    private String districtName;

    public ScenarioExecutionRequestDto() {
        this.districtName = "Sitamarhi";
    }

    public ScenarioExecutionRequestDto(String districtName) {
        this.districtName = districtName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }
}
