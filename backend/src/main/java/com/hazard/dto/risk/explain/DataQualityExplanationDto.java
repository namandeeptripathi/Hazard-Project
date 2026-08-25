package com.hazard.dto.risk.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * Explains input data completeness and component availability status.
 */
public class DataQualityExplanationDto {

    private String status;                      // e.g. "DATA_COMPLETE", "PARTIAL_DATA"
    private int configuredComponents;           // e.g. 4
    private int availableComponents;            // e.g. 4
    private double completenessPercentage;      // e.g. 100.0
    private List<String> availableComponentNames = new ArrayList<>();
    private List<String> missingComponentNames = new ArrayList<>();
    private String explanationText;
    private boolean isRedistributionApplied;

    public DataQualityExplanationDto() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getConfiguredComponents() { return configuredComponents; }
    public void setConfiguredComponents(int configuredComponents) { this.configuredComponents = configuredComponents; }

    public int getAvailableComponents() { return availableComponents; }
    public void setAvailableComponents(int availableComponents) { this.availableComponents = availableComponents; }

    public double getCompletenessPercentage() { return completenessPercentage; }
    public void setCompletenessPercentage(double completenessPercentage) { this.completenessPercentage = completenessPercentage; }

    public List<String> getAvailableComponentNames() { return availableComponentNames; }
    public void setAvailableComponentNames(List<String> availableComponentNames) { this.availableComponentNames = availableComponentNames != null ? availableComponentNames : new ArrayList<>(); }

    public List<String> getMissingComponentNames() { return missingComponentNames; }
    public void setMissingComponentNames(List<String> missingComponentNames) { this.missingComponentNames = missingComponentNames != null ? missingComponentNames : new ArrayList<>(); }

    public String getExplanationText() { return explanationText; }
    public void setExplanationText(String explanationText) { this.explanationText = explanationText; }

    public boolean isRedistributionApplied() { return isRedistributionApplied; }
    public void setRedistributionApplied(boolean redistributionApplied) { isRedistributionApplied = redistributionApplied; }
}
