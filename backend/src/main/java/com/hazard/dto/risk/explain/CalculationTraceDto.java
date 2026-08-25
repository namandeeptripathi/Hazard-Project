package com.hazard.dto.risk.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete mathematical calculation trace and formula representation.
 */
public class CalculationTraceDto {

    private String formulaString;
    private String parameterizedFormulaString;
    private List<CalculationComponentTraceDto> components = new ArrayList<>();
    private Double sumOfContributions;
    private Double finalNormalizedScore;
    private Double finalDisplayScore100;
    private boolean isReconciled;
    private String calculationMethodology = "Multi-Criteria Weighted Linear Combination with active missing-data weight redistribution";

    public CalculationTraceDto() {}

    public String getFormulaString() { return formulaString; }
    public void setFormulaString(String formulaString) { this.formulaString = formulaString; }

    public String getParameterizedFormulaString() { return parameterizedFormulaString; }
    public void setParameterizedFormulaString(String parameterizedFormulaString) { this.parameterizedFormulaString = parameterizedFormulaString; }

    public List<CalculationComponentTraceDto> getComponents() { return components; }
    public void setComponents(List<CalculationComponentTraceDto> components) { this.components = components != null ? components : new ArrayList<>(); }

    public Double getSumOfContributions() { return sumOfContributions; }
    public void setSumOfContributions(Double sumOfContributions) { this.sumOfContributions = sumOfContributions; }

    public Double getFinalNormalizedScore() { return finalNormalizedScore; }
    public void setFinalNormalizedScore(Double finalNormalizedScore) { this.finalNormalizedScore = finalNormalizedScore; }

    public Double getFinalDisplayScore100() { return finalDisplayScore100; }
    public void setFinalDisplayScore100(Double finalDisplayScore100) { this.finalDisplayScore100 = finalDisplayScore100; }

    @com.fasterxml.jackson.annotation.JsonProperty("isReconciled")
    public boolean isReconciled() { return isReconciled; }
    
    @com.fasterxml.jackson.annotation.JsonProperty("isReconciled")
    public void setReconciled(boolean reconciled) { isReconciled = reconciled; }

    @com.fasterxml.jackson.annotation.JsonProperty("reconciled")
    public boolean getReconciled() { return isReconciled; }

    public String getCalculationMethodology() { return calculationMethodology; }
    public void setCalculationMethodology(String calculationMethodology) { this.calculationMethodology = calculationMethodology; }
}
