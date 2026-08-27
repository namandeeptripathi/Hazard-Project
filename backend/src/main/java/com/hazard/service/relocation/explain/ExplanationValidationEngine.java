package com.hazard.service.relocation.explain;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RelocationPriorityResultDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.DecisionContributorDto;
import com.hazard.dto.relocation.explain.RelocationDecisionExplanationDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stage 7C.8 & 7.13 — Comprehensive Decision & Explanation Validation Engine.
 *
 * Enforces all 9 core consistency and integrity validation rules across the unified decision:
 * 1. Priority Consistency (Level & Score)
 * 2. Recommendation Consistency (Destination ID)
 * 3. Feasibility Consistency (Status vs Boolean flag)
 * 4. Capacity Arithmetic Consistency (Allocated + Unallocated == Required)
 * 5. Destination Identity Consistency (Source != Destination)
 * 6. Explanation Semantic Consistency (Narrative alignment with status)
 * 7. Evidence Mathematical Consistency (Contributor values match source maps)
 * 8. Normalized Score Bounds ([0.0, 1.0])
 * 9. No-Feasible-Destination Integrity (Null destination, zero allocation, failure status)
 */
@Component("relocationExplanationValidationEngine")
public class ExplanationValidationEngine {

    /**
     * Validates the generated aggregated decision explanation against underlying source results.
     *
     * @param explanation     the aggregated decision explanation DTO to validate
     * @param priorityResult  the ground-truth Stage 7A priority result
     * @param recommendation  the ground-truth Stage 7B recommendation result
     * @return list of validation issues found (empty if completely valid)
     */
    public List<String> validateExplanation(RelocationDecisionExplanationDto explanation,
                                           RelocationPriorityResultDto priorityResult,
                                           RelocationRecommendationDto recommendation) {
        List<String> notes = new ArrayList<>();

        if (explanation == null) {
            notes.add("Explanation reference is null.");
            return notes;
        }

        // Rule 1: Priority Consistency Check
        validatePriorityConsistency(explanation, priorityResult, notes);

        // Rule 2: Recommendation & Destination Consistency Check
        validateRecommendationConsistency(explanation, recommendation, notes);

        // Rule 3: Feasibility Consistency Check
        validateFeasibilityConsistency(explanation, recommendation, notes);

        // Rule 4: Capacity Arithmetic Consistency Check
        validateCapacityConsistency(explanation, recommendation, notes);

        // Rule 5: Destination Identity Consistency Check
        validateDestinationIdentityConsistency(explanation, recommendation, notes);

        // Rule 6: Explanation Semantic Consistency Check
        validateExplanationSemanticConsistency(explanation, recommendation, notes);

        // Rule 7: Evidence Mathematical Consistency Check
        validateEvidenceConsistency(explanation, priorityResult, recommendation, notes);

        // Rule 8: Score Bounds Check
        validateScoreBounds(explanation, notes);

        // Rule 9: No-Feasible-Destination Integrity Check
        validateNoFeasibleDestinationIntegrity(explanation, recommendation, notes);

        explanation.setValidationNotes(notes);
        explanation.setValid(notes.isEmpty());

        return notes;
    }

    private void validatePriorityConsistency(RelocationDecisionExplanationDto exp,
                                             RelocationPriorityResultDto priority,
                                             List<String> notes) {
        if (priority != null && exp.getRiskExplanation() != null) {
            if (priority.getPriorityLevel() != null
                    && exp.getRiskExplanation().getPriorityLevel() != priority.getPriorityLevel()) {
                notes.add(String.format("Priority level mismatch: explanation has '%s', source result has '%s'.",
                        exp.getRiskExplanation().getPriorityLevel(), priority.getPriorityLevel()));
            }

            if (priority.getPriorityScore() != null
                    && exp.getRiskExplanation().getPriorityScore() != null
                    && Math.abs(exp.getRiskExplanation().getPriorityScore() - priority.getPriorityScore()) > 0.001) {
                notes.add(String.format("Priority score mismatch: explanation has %.4f, source result has %.4f.",
                        exp.getRiskExplanation().getPriorityScore(), priority.getPriorityScore()));
            }
        }
    }

    private void validateRecommendationConsistency(RelocationDecisionExplanationDto exp,
                                                 RelocationRecommendationDto rec,
                                                 List<String> notes) {
        if (rec != null && exp.getRelocationExplanation() != null) {
            if (rec.getPrimaryDestination() != null) {
                String expDestId = exp.getRelocationExplanation().getDestinationId();
                String recDestId = rec.getPrimaryDestination().getSiteId();
                if (expDestId != null && recDestId != null && !expDestId.equalsIgnoreCase(recDestId)) {
                    notes.add(String.format("Destination ID mismatch: explanation has '%s', recommendation has '%s'.",
                            expDestId, recDestId));
                }
            }
        }
    }

    private void validateFeasibilityConsistency(RelocationDecisionExplanationDto exp,
                                               RelocationRecommendationDto rec,
                                               List<String> notes) {
        if (rec != null && exp.getRelocationExplanation() != null) {
            if (rec.isFeasible() != exp.getRelocationExplanation().isFeasible()) {
                notes.add(String.format("Feasibility flag mismatch: recommendation feasible=%b, explanation feasible=%b.",
                        rec.isFeasible(), exp.getRelocationExplanation().isFeasible()));
            }
            if (rec.getStatus() == RecommendationStatus.NO_FEASIBLE_DESTINATION && exp.getRelocationExplanation().isFeasible()) {
                notes.add("Feasibility contradiction: recommendation is NO_FEASIBLE_DESTINATION but relocation explanation claims feasible.");
            }
        }
    }

    private void validateCapacityConsistency(RelocationDecisionExplanationDto exp,
                                             RelocationRecommendationDto rec,
                                             List<String> notes) {
        if (exp.getCapacityExplanation() != null) {
            long req = (exp.getCapacityExplanation().getRequiredPopulation() != null)
                    ? exp.getCapacityExplanation().getRequiredPopulation() : 0L;
            long alloc = (exp.getCapacityExplanation().getAllocatedPopulation() != null)
                    ? exp.getCapacityExplanation().getAllocatedPopulation() : 0L;
            long unalloc = (exp.getCapacityExplanation().getUnallocatedPopulation() != null)
                    ? exp.getCapacityExplanation().getUnallocatedPopulation() : 0L;

            if (req > 0 && (alloc + unalloc != req)) {
                notes.add(String.format("Capacity arithmetic inconsistency: allocated (%d) + unallocated (%d) != required (%d).",
                        alloc, unalloc, req));
            }
        }
    }

    private void validateDestinationIdentityConsistency(RelocationDecisionExplanationDto exp,
                                                       RelocationRecommendationDto rec,
                                                       List<String> notes) {
        if (rec != null && rec.getPrimaryDestination() != null) {
            String habId = rec.getHabitationId();
            String destId = rec.getPrimaryDestination().getSiteId();
            if (habId != null && destId != null && habId.equalsIgnoreCase(destId)) {
                notes.add(String.format("Invalid destination identity: origin habitation ID '%s' is identical to destination site ID '%s'.",
                        habId, destId));
            }
        }
    }

    private void validateExplanationSemanticConsistency(RelocationDecisionExplanationDto exp,
                                                        RelocationRecommendationDto rec,
                                                        List<String> notes) {
        if (rec != null && exp.getDecisionRationale() != null) {
            if (rec.getStatus() == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
                if (exp.getDecisionRationale().getWhyStatement() != null
                        && exp.getDecisionRationale().getWhyStatement().contains("100% capacity accommodation")) {
                    notes.add("Semantic inconsistency: rationale claims 100% accommodation for NO_FEASIBLE_DESTINATION status.");
                }
            }
        }
    }

    private void validateEvidenceConsistency(RelocationDecisionExplanationDto exp,
                                             RelocationPriorityResultDto priority,
                                             RelocationRecommendationDto rec,
                                             List<String> notes) {
        if (priority != null && priority.getScoringContributors() != null && exp.getPriorityEvidence() != null) {
            Map<String, Double> map = priority.getScoringContributors();
            for (DecisionContributorDto c : exp.getPriorityEvidence()) {
                if (map.containsKey(c.getContributorKey())) {
                    double expected = map.get(c.getContributorKey());
                    if (c.getNormalizedScore() != null && Math.abs(c.getNormalizedScore() - expected) > 0.001) {
                        notes.add(String.format("Priority evidence contributor '%s' value mismatch: expected %.4f, found %.4f.",
                                c.getContributorKey(), expected, c.getNormalizedScore()));
                    }
                }
            }
        }

        if (rec != null && rec.getPrimaryDestination() != null
                && rec.getPrimaryDestination().getScoringContributors() != null
                && exp.getDestinationEvidence() != null) {
            Map<String, Double> map = rec.getPrimaryDestination().getScoringContributors();
            for (DecisionContributorDto c : exp.getDestinationEvidence()) {
                if (map.containsKey(c.getContributorKey())) {
                    double expected = map.get(c.getContributorKey());
                    if (c.getNormalizedScore() != null && Math.abs(c.getNormalizedScore() - expected) > 0.001) {
                        notes.add(String.format("Destination evidence contributor '%s' value mismatch: expected %.4f, found %.4f.",
                                c.getContributorKey(), expected, c.getNormalizedScore()));
                    }
                }
            }
        }
    }

    private void validateScoreBounds(RelocationDecisionExplanationDto exp, List<String> notes) {
        if (exp.getPriorityEvidence() != null) {
            for (DecisionContributorDto c : exp.getPriorityEvidence()) {
                if (c.getNormalizedScore() != null && (c.getNormalizedScore() < 0.0 || c.getNormalizedScore() > 1.0)) {
                    notes.add(String.format("Priority contributor '%s' normalized score %.4f is out of [0.0, 1.0] bounds.",
                            c.getContributorKey(), c.getNormalizedScore()));
                }
            }
        }

        if (exp.getDestinationEvidence() != null) {
            for (DecisionContributorDto c : exp.getDestinationEvidence()) {
                if (c.getNormalizedScore() != null && (c.getNormalizedScore() < 0.0 || c.getNormalizedScore() > 1.0)) {
                    notes.add(String.format("Destination contributor '%s' normalized score %.4f is out of [0.0, 1.0] bounds.",
                            c.getContributorKey(), c.getNormalizedScore()));
                }
            }
        }
    }

    private void validateNoFeasibleDestinationIntegrity(RelocationDecisionExplanationDto exp,
                                                        RelocationRecommendationDto rec,
                                                        List<String> notes) {
        if (rec != null && rec.getStatus() == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            if (rec.getPrimaryDestination() != null) {
                notes.add("Integrity violation: NO_FEASIBLE_DESTINATION status must have null primaryDestination.");
            }
            if (rec.getAllocatedPopulation() != null && rec.getAllocatedPopulation() > 0) {
                notes.add(String.format("Integrity violation: NO_FEASIBLE_DESTINATION status cannot allocate %d population.",
                        rec.getAllocatedPopulation()));
            }
        }
    }
}
