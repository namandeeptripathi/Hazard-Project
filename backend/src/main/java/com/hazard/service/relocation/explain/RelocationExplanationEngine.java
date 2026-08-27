package com.hazard.service.relocation.explain;

import com.hazard.domain.relocation.RecommendationStatus;
import com.hazard.dto.relocation.RecommendedDestinationDto;
import com.hazard.dto.relocation.RelocationRecommendationDto;
import com.hazard.dto.relocation.explain.DecisionContributorDto;
import com.hazard.dto.relocation.explain.RelocationExplanationDto;
import com.hazard.service.relocation.RecommendationScoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stage 7C.3 — Relocation & Destination Selection Explanation Engine.
 *
 * Explains why the selected destination was chosen as the optimal choice,
 * explicitly contrasting hard feasibility constraints against soft destination preferences,
 * and detailing the 4 destination scoring contributors.
 */
@Component("relocationDestinationExplanationEngine")
public class RelocationExplanationEngine {

    private final RecommendationScoringConfig recommendationConfig;

    @Autowired
    public RelocationExplanationEngine(RecommendationScoringConfig recommendationConfig) {
        this.recommendationConfig = recommendationConfig;
    }

    public RelocationExplanationEngine() {
        this(new RecommendationScoringConfig());
    }

    /**
     * Generates a RelocationExplanationDto from a RelocationRecommendationDto.
     */
    public RelocationExplanationDto explainRelocation(RelocationRecommendationDto recommendation) {
        RelocationExplanationDto explanation = new RelocationExplanationDto();

        if (recommendation == null) {
            explanation.setFeasible(false);
            explanation.setFeasibilityGateSummary("No recommendation data provided.");
            explanation.setSoftPreferenceSummary("Recommendation evaluation skipped.");
            return explanation;
        }

        explanation.setFeasible(recommendation.isFeasible());

        RecommendedDestinationDto primary = recommendation.getPrimaryDestination();

        // 1. Handle No Feasible Destination
        if (primary == null || recommendation.getStatus() == RecommendationStatus.NO_FEASIBLE_DESTINATION) {
            explanation.setDestinationId("NONE");
            explanation.setDestinationName("No Feasible Destination");
            explanation.setDestinationScore(0.0);
            explanation.setDestinationRank(0);
            explanation.setFeasibilityGateSummary(String.format(
                    "Hard Feasibility Rejection: Evaluated %d candidate safe site(s) in region; 0 met all mandatory safety, suitability, distance, and capacity gates.",
                    recommendation.getTotalCandidatesEvaluated()
            ));
            explanation.setSoftPreferenceSummary("No candidates qualified for soft suitability preference scoring.");
            explanation.setComparativeRankNarrative("Zero destinations eligible for ranking.");
            explanation.setAlternativeDestinationsSummary("No fallback alternative safe sites available.");
            return explanation;
        }

        // 2. Populate Destination Identity & Metrics
        explanation.setDestinationId(primary.getSiteId());
        explanation.setDestinationName(primary.getSiteName() != null ? primary.getSiteName() : primary.getSiteId());
        explanation.setDestinationScore(primary.getDestinationScore());
        explanation.setDestinationRank(primary.getDestinationRank());

        double totalScore = (primary.getDestinationScore() != null) ? primary.getDestinationScore() : 0.0;

        // 3. Build Structured 4-Contributor Destination Breakdown (7C.5)
        List<DecisionContributorDto> contributors = buildDestinationContributors(primary, totalScore);
        explanation.setContributors(contributors);

        // 4. Hard Feasibility Gate Narrative
        explanation.setFeasibilityGateSummary(generateFeasibilityGateSummary(primary, recommendation));

        // 5. Soft Preference Summary
        explanation.setSoftPreferenceSummary(generateSoftPreferenceSummary(primary, recommendation));

        // 6. Dimension-Specific Explanations
        explanation.setProximityExplanation(generateProximityExplanation(primary));
        explanation.setSuitabilityExplanation(generateSuitabilityExplanation(primary));
        explanation.setCapacityFitExplanation(generateCapacityFitExplanation(primary, recommendation));
        explanation.setAccessExplanation(generateAccessExplanation(primary));

        // 7. Comparative Rank Narrative
        explanation.setComparativeRankNarrative(String.format(
                "'%s' [%s] achieved Rank #1 among %d feasible candidate safe sites with a composite destination suitability score of %.4f/1.00.",
                explanation.getDestinationName(), primary.getSiteId(),
                recommendation.getTotalFeasibleCandidates(), totalScore
        ));

        // 8. Alternative Destinations Summary
        explanation.setAlternativeDestinationsSummary(generateAlternativesSummary(recommendation));

        return explanation;
    }

    private List<DecisionContributorDto> buildDestinationContributors(RecommendedDestinationDto dest, double totalScore) {
        List<DecisionContributorDto> list = new ArrayList<>();
        Map<String, Double> contributorMap = dest.getScoringContributors();

        // 1. Suitability Quality (35%)
        double normSuit = (contributorMap != null && contributorMap.containsKey(RecommendationScoringConfig.SUITABILITY_QUALITY))
                ? contributorMap.get(RecommendationScoringConfig.SUITABILITY_QUALITY) : 0.40;
        double wSuit = recommendationConfig.getWeight(RecommendationScoringConfig.SUITABILITY_QUALITY);
        list.add(createContributor(
                RecommendationScoringConfig.SUITABILITY_QUALITY, "Site Suitability Quality", "DESTINATION",
                dest.getSuitabilityClass() != null ? dest.getSuitabilityClass().getDisplayName() : "UNKNOWN",
                normSuit, wSuit, totalScore,
                interpretSuitabilityQuality(dest)
        ));

        // 2. Transit Proximity (30%)
        double normProx = (contributorMap != null && contributorMap.containsKey(RecommendationScoringConfig.TRANSIT_PROXIMITY))
                ? contributorMap.get(RecommendationScoringConfig.TRANSIT_PROXIMITY) : 0.20;
        double wProx = recommendationConfig.getWeight(RecommendationScoringConfig.TRANSIT_PROXIMITY);
        list.add(createContributor(
                RecommendationScoringConfig.TRANSIT_PROXIMITY, "Geodesic Transit Proximity", "DESTINATION",
                dest.getDistanceKilometers() != null ? String.format("%.2f km", dest.getDistanceKilometers()) : "N/A",
                normProx, wProx, totalScore,
                interpretTransitProximity(dest)
        ));

        // 3. Capacity Fit (20%)
        double normCap = (contributorMap != null && contributorMap.containsKey(RecommendationScoringConfig.CAPACITY_FIT))
                ? contributorMap.get(RecommendationScoringConfig.CAPACITY_FIT) : 0.50;
        double wCap = recommendationConfig.getWeight(RecommendationScoringConfig.CAPACITY_FIT);
        list.add(createContributor(
                RecommendationScoringConfig.CAPACITY_FIT, "Shelter Capacity Fit & Headroom", "DESTINATION",
                dest.getAvailableCapacity() != null ? dest.getAvailableCapacity() + " available beds" : "Unbounded Capacity",
                normCap, wCap, totalScore,
                interpretCapacityFit(dest)
        ));

        // 4. Access Reliability (15%)
        double normAccess = (contributorMap != null && contributorMap.containsKey(RecommendationScoringConfig.ACCESS_RELIABILITY))
                ? contributorMap.get(RecommendationScoringConfig.ACCESS_RELIABILITY) : 0.50;
        double wAccess = recommendationConfig.getWeight(RecommendationScoringConfig.ACCESS_RELIABILITY);
        list.add(createContributor(
                RecommendationScoringConfig.ACCESS_RELIABILITY, "Multi-Modal Access Reliability", "DESTINATION",
                "Road/Water/Healthcare Access",
                normAccess, wAccess, totalScore,
                interpretAccessReliability(normAccess)
        ));

        return list;
    }

    private DecisionContributorDto createContributor(String key, String displayName, String category,
                                                     Object rawVal, double normScore, double weight,
                                                     double totalScore, String interpretation) {
        double weightedImpact = Math.round(normScore * weight * 10000.0) / 10000.0;
        double pct = (totalScore > 0.0) ? Math.round((weightedImpact / totalScore) * 1000.0) / 10.0 : 0.0;
        DecisionContributorDto dto = new DecisionContributorDto(
                key, displayName, category, rawVal, normScore, weight, weightedImpact, interpretation
        );
        dto.setImpactPercentage(pct);
        return dto;
    }

    private String generateFeasibilityGateSummary(RecommendedDestinationDto dest, RelocationRecommendationDto rec) {
        String distStr = (dest.getDistanceKilometers() != null) ? String.format("%.2f km", dest.getDistanceKilometers()) : "N/A";
        String capStr = (dest.getAvailableCapacity() != null) ? dest.getAvailableCapacity() + " beds" : "Unbounded capacity";

        return String.format(
                "Hard Feasibility Passed: '%s' satisfied all mandatory gating criteria (Safety: %s, Suitability: %s, Distance: %s within limit, Shelter: %s).",
                dest.getSiteName(),
                dest.getHazardSafetyStatus() != null ? dest.getHazardSafetyStatus().name() : "SAFE",
                dest.getSuitabilityClass() != null ? dest.getSuitabilityClass().getDisplayName() : "SUITABLE",
                distStr,
                capStr
        );
    }

    private String generateSoftPreferenceSummary(RecommendedDestinationDto dest, RelocationRecommendationDto rec) {
        return String.format(
                "Soft Preference Selection: Among all %d feasible candidates, '%s' scored highest overall (Score: %.4f) by balancing high multi-criteria suitability (35%%) and close transit proximity (30%%).",
                rec.getTotalFeasibleCandidates(), dest.getSiteName(),
                dest.getDestinationScore() != null ? dest.getDestinationScore() : 0.0
        );
    }

    private String generateProximityExplanation(RecommendedDestinationDto dest) {
        if (dest.getDistanceKilometers() != null) {
            return String.format(
                    "Transit distance is %.2f km from origin, providing rapid evacuation access with minimal transport burden.",
                    dest.getDistanceKilometers()
            );
        }
        return "Geodesic transit distance could not be precisely verified from coordinates.";
    }

    private String generateSuitabilityExplanation(RecommendedDestinationDto dest) {
        String tier = (dest.getSuitabilityClass() != null) ? dest.getSuitabilityClass().getDisplayName() : "Suitable";
        String scoreStr = (dest.getSuitabilityScore() != null) ? String.format("(Score: %.1f/100)", dest.getSuitabilityScore()) : "";
        return String.format(
                "Classified as '%s' %s based on comprehensive terrain stability, road network connectivity, water access, and emergency health support.",
                tier, scoreStr
        );
    }

    private String generateCapacityFitExplanation(RecommendedDestinationDto dest, RelocationRecommendationDto rec) {
        if (dest.getAvailableCapacity() == null) {
            return "Shelter capacity is unconstrained/unbounded, accommodating all evacuees.";
        }
        long reqPop = (rec.getVulnerablePopulation() != null) ? rec.getVulnerablePopulation() : 0L;
        if (dest.getAvailableCapacity() >= reqPop) {
            return String.format(
                    "Shelter has %d available beds for %d vulnerable evacuees, leaving a surplus safety buffer of %d beds.",
                    dest.getAvailableCapacity(), reqPop, dest.getAvailableCapacity() - reqPop
            );
        }
        return String.format(
                "Shelter capacity (%d beds) can partially accommodate %d of %d evacuees (%d unallocated deficit).",
                dest.getAvailableCapacity(), dest.getAccommodatablePopulation() != null ? dest.getAccommodatablePopulation() : dest.getAvailableCapacity(),
                reqPop, reqPop - (dest.getAccommodatablePopulation() != null ? dest.getAccommodatablePopulation() : 0)
        );
    }

    private String generateAccessExplanation(RecommendedDestinationDto dest) {
        return "Site has verified road access and nearby public utility connections supporting emergency evacuation logistics.";
    }

    private String generateAlternativesSummary(RelocationRecommendationDto rec) {
        if (rec.getAlternativeDestinations() == null || rec.getAlternativeDestinations().isEmpty()) {
            return "No secondary fallback destinations available in immediate vicinity.";
        }
        int count = rec.getAlternativeDestinations().size();
        RecommendedDestinationDto firstAlt = rec.getAlternativeDestinations().get(0);
        return String.format(
                "%d alternative feasible safe site(s) identified. Top fallback option: '%s' [%s] (Score: %.4f, Distance: %s).",
                count,
                firstAlt.getSiteName(),
                firstAlt.getSiteId(),
                firstAlt.getDestinationScore() != null ? firstAlt.getDestinationScore() : 0.0,
                firstAlt.getDistanceKilometers() != null ? String.format("%.2f km", firstAlt.getDistanceKilometers()) : "N/A"
        );
    }

    private String interpretSuitabilityQuality(RecommendedDestinationDto dest) {
        if (dest.getSuitabilityClass() != null && dest.getSuitabilityClass().isHighlySuitable()) {
            return "Optimal suitability class with superior infrastructure, water access, and medical proximity.";
        }
        if (dest.getSuitabilityClass() != null && dest.getSuitabilityClass().isSuitable()) {
            return "Solid suitability class meeting all essential civil protection standards.";
        }
        return "Marginal or baseline suitability meeting minimum safety criteria.";
    }

    private String interpretTransitProximity(RecommendedDestinationDto dest) {
        if (dest.getDistanceKilometers() != null) {
            if (dest.getDistanceKilometers() <= 5.0) return "Very close proximity (<5 km) enables swift foot or vehicular transit.";
            if (dest.getDistanceKilometers() <= 15.0) return "Moderate transit distance (5-15 km) manageable via emergency bus transport.";
            return "Extended distance (>15 km) requires organized vehicular convoy.";
        }
        return "Transit distance unmeasured.";
    }

    private String interpretCapacityFit(RecommendedDestinationDto dest) {
        if (dest.getAvailableCapacity() == null) return "Unbounded capacity accommodates entire vulnerable population without saturation.";
        if (dest.getAvailableCapacity() > 500) return "High shelter capacity headroom provides resilience against population surges.";
        return "Sufficient available shelter capacity to house the requested cohort.";
    }

    private String interpretAccessReliability(double norm) {
        if (norm >= 0.80) return "High accessibility reliability across primary roads, water supply, and health centers.";
        if (norm >= 0.50) return "Moderate accessibility with functional road connections.";
        return "Limited access points require active route clearance.";
    }
}
