package com.hazard.service.safesite;

import com.hazard.domain.boundaries.DistrictBoundary;
import com.hazard.domain.risk.RiskTier;
import com.hazard.domain.risk.ZoneLevel;
import com.hazard.domain.safesite.HazardSafetyStatus;
import com.hazard.dto.risk.DistrictRiskScoreDto;
import com.hazard.dto.safesite.CandidateSafeSiteDto;
import com.hazard.repository.boundaries.DistrictBoundaryRepository;
import com.hazard.service.risk.RiskCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Stage 5.3 — Hazard Safety Evaluator.
 *
 * Spatially evaluates candidate safe sites against PostGIS district risk boundaries
 * and Stage 4 / Stage 5.1 disaster risk syntheses.
 *
 * Rules:
 * - SAFE: Location falls within an area with LOW or MODERATE disaster risk.
 * - AT_RISK: Location falls within an area with HIGH, VERY_HIGH, or CRITICAL disaster risk.
 * - UNKNOWN: Missing/invalid coordinates, unmapped boundary, or missing hazard risk data.
 *   (Never silently converted to SAFE).
 */
@Component
public class HazardSafetyEvaluator {

    private static final Logger log = LoggerFactory.getLogger(HazardSafetyEvaluator.class);

    private final DistrictBoundaryRepository districtBoundaryRepository;
    private final RiskCalculationService riskCalculationService;

    private final java.util.Map<String, DistrictRiskScoreDto> districtScoreCache = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile long lastCacheClear = System.currentTimeMillis();
    private static final long CACHE_TTL_MS = 10_000L;

    public HazardSafetyEvaluator(DistrictBoundaryRepository districtBoundaryRepository,
                                 RiskCalculationService riskCalculationService) {
        this.districtBoundaryRepository = districtBoundaryRepository;
        this.riskCalculationService = riskCalculationService;
    }

    /**
     * Evaluates spatial hazard safety exposure for an individual candidate safe site.
     * Mutates the site DTO to populate hazardSafetyStatus, hazardSafetyReason, riskZone, and riskScore.
     */
    public void evaluateHazardSafety(CandidateSafeSiteDto site) {
        if (site == null) {
            return;
        }

        Double lat = site.getLatitude();
        Double lon = site.getLongitude();

        // 1. Validate geographic coordinates
        if (lat == null || lon == null || lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0) {
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setHazardSafetyReason("Missing or invalid geographic coordinates; spatial hazard exposure cannot be evaluated.");
            site.setRiskZone("UNKNOWN");
            site.setRiskScore(null);
            return;
        }

        // 2. Perform Point-in-Polygon spatial lookup using PostGIS geometry
        String targetDistrict = null;
        try {
            Optional<DistrictBoundary> boundaryOpt = districtBoundaryRepository.findDistrictContainingPoint(lon, lat);
            if (boundaryOpt.isPresent()) {
                targetDistrict = boundaryOpt.get().getName2();
            }
        } catch (Exception e) {
            log.warn("Spatial point-in-polygon lookup failed for site {}: {}", site.getSiteId(), e.getMessage());
        }

        // 3. Fallback to assigned district name if PostGIS lookup yielded no spatial boundary match
        if (targetDistrict == null || targetDistrict.trim().isEmpty()) {
            if (site.getDistrict() != null && !site.getDistrict().trim().isEmpty()) {
                Optional<DistrictBoundary> fallbackOpt = districtBoundaryRepository.findByName2IgnoreCase(site.getDistrict().trim());
                if (fallbackOpt.isPresent()) {
                    targetDistrict = fallbackOpt.get().getName2();
                } else {
                    targetDistrict = site.getDistrict().trim();
                }
            }
        }

        if (targetDistrict == null || targetDistrict.trim().isEmpty()) {
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setHazardSafetyReason("Candidate coordinates (" + lon + ", " + lat + ") fall outside mapped administrative district boundaries; spatial hazard exposure undetermined.");
            site.setRiskZone("UNKNOWN");
            site.setRiskScore(null);
            return;
        }

        // 4. Retrieve spatial disaster risk profile for the containing geographic area
        final String finalDistrictName = targetDistrict;
        final String lookupKey = targetDistrict.trim().toUpperCase();
        long now = System.currentTimeMillis();
        if (now - lastCacheClear > CACHE_TTL_MS) {
            districtScoreCache.clear();
            lastCacheClear = now;
        }

        DistrictRiskScoreDto riskScoreDto = districtScoreCache.computeIfAbsent(lookupKey, k -> {
            try {
                return riskCalculationService.getDistrictRiskScore(finalDistrictName, null);
            } catch (Exception e) {
                log.warn("Failed to retrieve disaster risk profile for district {}: {}", finalDistrictName, e.getMessage());
                return null;
            }
        });

        if (riskScoreDto == null || riskScoreDto.getRiskScore() == null) {
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setHazardSafetyReason("Spatial disaster risk assessment data is unavailable for area: " + targetDistrict + ".");
            site.setRiskZone("UNKNOWN");
            site.setRiskScore(null);
            return;
        }

        // 5. Evaluate Risk Tier and Zone Classification
        RiskTier riskTier = riskScoreDto.getRiskTier();
        ZoneLevel zoneLevel = ZoneLevel.fromRiskTier(riskTier);
        Double score100 = riskScoreDto.getRiskScore100();
        if (score100 == null && riskScoreDto.getRiskScore() != null) {
            score100 = Math.round(riskScoreDto.getRiskScore() * 1000.0) / 10.0;
        }

        site.setRiskZone(zoneLevel != null ? zoneLevel.name() : (riskTier != null ? riskTier.name() : "UNKNOWN"));
        site.setRiskScore(score100);

        if (zoneLevel == null || zoneLevel == ZoneLevel.UNKNOWN) {
            site.setHazardSafetyStatus(HazardSafetyStatus.UNKNOWN);
            site.setHazardSafetyReason("Spatial risk evaluation returned unclassified hazard risk level for area: " + targetDistrict + ".");
            site.setRiskScore(null);
            return;
        }

        // Classification Rules:
        // - CRITICAL (Red Zone: VERY_HIGH or CRITICAL tier) -> AT_RISK
        // - HIGH (High Risk tier) -> AT_RISK
        // - MODERATE -> SAFE
        // - LOW -> SAFE
        if (zoneLevel == ZoneLevel.CRITICAL) {
            site.setHazardSafetyStatus(HazardSafetyStatus.AT_RISK);
            site.setHazardSafetyReason("Candidate location falls within a Critical Red Zone (" + targetDistrict +
                    ", Risk Score: " + score100 + "/100, Tier: " + (riskTier != null ? riskTier.name() : "CRITICAL") +
                    ") with severe disaster risk.");
        } else if (zoneLevel == ZoneLevel.HIGH) {
            site.setHazardSafetyStatus(HazardSafetyStatus.AT_RISK);
            site.setHazardSafetyReason("Candidate location falls within a High Risk Zone (" + targetDistrict +
                    ", Risk Score: " + score100 + "/100, Tier: " + (riskTier != null ? riskTier.name() : "HIGH") +
                    ") with elevated disaster exposure.");
        } else if (zoneLevel == ZoneLevel.MODERATE) {
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setHazardSafetyReason("Candidate location is within a Moderate Risk area (" + targetDistrict +
                    ", Risk Score: " + score100 + "/100, Tier: " + (riskTier != null ? riskTier.name() : "MODERATE") +
                    "), outside high-risk/red zones.");
        } else if (zoneLevel == ZoneLevel.LOW) {
            site.setHazardSafetyStatus(HazardSafetyStatus.SAFE);
            site.setHazardSafetyReason("Candidate location is within a Low Risk area (" + targetDistrict +
                    ", Risk Score: " + score100 + "/100, Tier: " + (riskTier != null ? riskTier.name() : "LOW") +
                    "), outside high-risk/red zones.");
        }
    }
}
