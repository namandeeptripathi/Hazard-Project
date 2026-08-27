package com.hazard.service.relocation;

import com.hazard.domain.relocation.PriorityLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Stage 7A.2 — Priority Classification Engine.
 *
 * Classifies a composite priority score into a deterministic {@link PriorityLevel}
 * using centralized thresholds from {@link PriorityScoringConfig}.
 *
 * <p>Classification boundaries (using >= for threshold inclusion):
 * <ul>
 *   <li>score >= 0.70 → IMMEDIATE</li>
 *   <li>score >= 0.40 → SHORT_TERM</li>
 *   <li>score >= 0.15 → MEDIUM_TERM</li>
 *   <li>score <  0.15 → MONITORING</li>
 * </ul>
 */
@Component
public class PriorityClassificationEngine {

    private static final Logger log = LoggerFactory.getLogger(PriorityClassificationEngine.class);

    private final PriorityScoringConfig config;

    public PriorityClassificationEngine(PriorityScoringConfig config) {
        this.config = config;
    }

    /**
     * Default constructor for isolated unit testing with default thresholds.
     */
    public PriorityClassificationEngine() {
        this(new PriorityScoringConfig());
    }

    /**
     * Classifies a composite priority score into a {@link PriorityLevel}.
     *
     * @param priorityScore the composite score in [0.0, 1.0]
     * @return the corresponding PriorityLevel
     */
    public PriorityLevel classify(double priorityScore) {
        if (Double.isNaN(priorityScore)) {
            log.warn("NaN priority score encountered; defaulting to MONITORING");
            return PriorityLevel.MONITORING;
        }

        if (priorityScore < 0.0) {
            log.warn("Negative priority score ({}) encountered; defaulting to MONITORING", priorityScore);
            return PriorityLevel.MONITORING;
        }

        if (priorityScore >= config.getImmediateThreshold()) {
            return PriorityLevel.IMMEDIATE;
        }
        if (priorityScore >= config.getShortTermThreshold()) {
            return PriorityLevel.SHORT_TERM;
        }
        if (priorityScore >= config.getMediumTermThreshold()) {
            return PriorityLevel.MEDIUM_TERM;
        }
        return PriorityLevel.MONITORING;
    }
}
