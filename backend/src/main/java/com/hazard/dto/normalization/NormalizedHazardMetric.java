package com.hazard.dto.normalization;

import com.hazard.domain.hazard.NormalizationDirection;
import com.hazard.domain.hazard.NormalizationMethod;

/**
 * Encapsulates an individual normalized hazard indicator with its original value,
 * normalized score on [0.00, 1.00], reference range, and provenance metadata.
 */
public class NormalizedHazardMetric {

    private String metricName;
    private String metricLabel;
    private String units;
    private Double rawValue;
    private Double normalizedValue;
    private Double referenceMin;
    private Double referenceMax;
    private NormalizationMethod method = NormalizationMethod.MIN_MAX;
    private NormalizationDirection direction = NormalizationDirection.INCREASING;
    private boolean clamped;
    private String referenceRationale;

    public NormalizedHazardMetric() {
    }

    public NormalizedHazardMetric(String metricName, String metricLabel, String units,
                                  Double rawValue, Double normalizedValue,
                                  Double referenceMin, Double referenceMax,
                                  NormalizationMethod method, NormalizationDirection direction,
                                  boolean clamped, String referenceRationale) {
        this.metricName = metricName;
        this.metricLabel = metricLabel;
        this.units = units;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.referenceMin = referenceMin;
        this.referenceMax = referenceMax;
        this.method = method;
        this.direction = direction;
        this.clamped = clamped;
        this.referenceRationale = referenceRationale;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricLabel() {
        return metricLabel;
    }

    public void setMetricLabel(String metricLabel) {
        this.metricLabel = metricLabel;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Double getRawValue() {
        return rawValue;
    }

    public void setRawValue(Double rawValue) {
        this.rawValue = rawValue;
    }

    public Double getNormalizedValue() {
        return normalizedValue;
    }

    public void setNormalizedValue(Double normalizedValue) {
        this.normalizedValue = normalizedValue;
    }

    public Double getReferenceMin() {
        return referenceMin;
    }

    public void setReferenceMin(Double referenceMin) {
        this.referenceMin = referenceMin;
    }

    public Double getReferenceMax() {
        return referenceMax;
    }

    public void setReferenceMax(Double referenceMax) {
        this.referenceMax = referenceMax;
    }

    public NormalizationMethod getMethod() {
        return method;
    }

    public void setMethod(NormalizationMethod method) {
        this.method = method;
    }

    public NormalizationDirection getDirection() {
        return direction;
    }

    public void setDirection(NormalizationDirection direction) {
        this.direction = direction;
    }

    public boolean isClamped() {
        return clamped;
    }

    public void setClamped(boolean clamped) {
        this.clamped = clamped;
    }

    public String getReferenceRationale() {
        return referenceRationale;
    }

    public void setReferenceRationale(String referenceRationale) {
        this.referenceRationale = referenceRationale;
    }
}
