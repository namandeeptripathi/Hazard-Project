package com.hazard.service.relocation.explain;

import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Stage 7C.7 — Relocation Explainability Generation Engine.
 *
 * Coordinates sub-engines to synthesize a complete, validated RelocationDecisionExplanationDto:
 * 1. RiskExplanationEngine (7C.2)
 * 2. RelocationExplanationEngine (7C.3)
 * 3. CapacityExplanationEngine (7C.4)
 * 4. DecisionRationaleEngine (7C.1)
 * 5. ExplanationValidationEngine (7C.8)
 */
@Component
public class RelocationExplainabilityEngine {

    private static final Logger log = LoggerFactory.getLogger(RelocationExplainabilityEngine.class);

    private final RiskExplanationEngine riskExplanationEngine;
    private final RelocationExplanationEngine relocationExplanationEngine;
    private final CapacityExplanationEngine capacityExplanationEngine;
    private final DecisionRationaleEngine decisionRationaleEngine;
    private final ExplanationValidationEngine validationEngine;

    @Autowired
    public RelocationExplainabilityEngine(RiskExplanationEngine riskExplanationEngine,
                                         RelocationExplanationEngine relocationExplanationEngine,
                                         CapacityExplanationEngine capacityExplanationEngine,
                                         DecisionRationaleEngine decisionRationaleEngine,
                                         ExplanationValidationEngine validationEngine) {
        this.riskExplanationEngine = riskExplanationEngine;
        this.relocationExplanationEngine = relocationExplanationEngine;
        this.capacityExplanationEngine = capacityExplanationEngine;
        this.decisionRationaleEngine = decisionRationaleEngine;
        this.validationEngine = validationEngine;
    }

    public RelocationExplainabilityEngine() {
        this.riskExplanationEngine = new RiskExplanationEngine();
        this.relocationExplanationEngine = new RelocationExplanationEngine();
        this.capacityExplanationEngine = new CapacityExplanationEngine();
        this.decisionRationaleEngine = new DecisionRationaleEngine();
        this.validationEngine = new ExplanationValidationEngine();
    }

    /**
     * Generates a complete decision explanation for a single prioritized and recommended case.
     *
     * @param priorityResult the Stage 7A priority result
     * @param recommendation the Stage 7B recommendation result
     * @return fully populated and validated RelocationDecisionExplanationDto
     */
    public RelocationDecisionExplanationDto explainDecision(RelocationPriorityResultDto priorityResult,
                                                           RelocationRecommendationDto recommendation) {
        RelocationDecisionExplanationDto explanation = new RelocationDecisionExplanationDto();
        explanation.setTimestamp(LocalDateTime.now());

        String habId = (recommendation != null && recommendation.getHabitationId() != null)
                ? recommendation.getHabitationId()
                : (priorityResult != null ? priorityResult.getHabitationId() : "HAB");

        explanation.setExplanationId("EXP-" + habId + "-" + System.currentTimeMillis());
        explanation.setHabitationId(habId);

        String habName = (recommendation != null && recommendation.getHabitationName() != null)
                ? recommendation.getHabitationName()
                : (priorityResult != null ? priorityResult.getHabitationName() : habId);
        explanation.setHabitationName(habName);

        String district = (recommendation != null && recommendation.getDistrict() != null)
                ? recommendation.getDistrict()
                : (priorityResult != null ? priorityResult.getDistrict() : "Sitamarhi");
        explanation.setDistrict(district);

        String state = (recommendation != null && recommendation.getState() != null)
                ? recommendation.getState()
                : (priorityResult != null ? priorityResult.getState() : "Bihar");
        explanation.setState(state);

        explanation.setPriorityResult(priorityResult);
        explanation.setRecommendationResult(recommendation);

        // 1. Generate Domain Sub-Explanations
        explanation.setRiskExplanation(riskExplanationEngine.explainRisk(priorityResult));
        explanation.setRelocationExplanation(relocationExplanationEngine.explainRelocation(recommendation));
        explanation.setCapacityExplanation(capacityExplanationEngine.explainCapacity(recommendation));

        // 2. Populate Evidence Lists
        if (explanation.getRiskExplanation() != null) {
            explanation.setPriorityEvidence(explanation.getRiskExplanation().getContributors());
        }
        if (explanation.getRelocationExplanation() != null) {
            explanation.setDestinationEvidence(explanation.getRelocationExplanation().getContributors());
        }

        // 3. Synthesize Executive Decision Rationale
        explanation.setDecisionRationale(decisionRationaleEngine.synthesizeRationale(priorityResult, recommendation));

        // 4. Validate Explanation Consistency (7C.8)
        validationEngine.validateExplanation(explanation, priorityResult, recommendation);

        log.info("Generated explanation for {}: Valid = {}", habName, explanation.isValid());
        return explanation;
    }
}
