package com.hazard.dto.relocation.explain;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 7C.1 — Decision Rationale DTO.
 *
 * High-level executive synthesis answering WHO, WHERE, and WHY, alongside
 * operational next steps, key decision strengths, and potential risks/deficits.
 */
public class DecisionRationaleDto {

    private String whoStatement;               // WHO should move (origin habitation, urgency, priority tier)
    private String whereStatement;             // WHERE they should go (recommended destination & proximity)
    private String whyStatement;               // WHY this decision was made (core mathematical & logistical justification)
    private String actionabilityGuidance;      // Immediate recommended operational action for disaster managers

    private List<String> keyStrengths = new ArrayList<>();        // Bullet points highlighting advantages of this decision
    private List<String> keyRisksOrDeficits = new ArrayList<>();  // Bullet points highlighting any warnings or unallocated deficits

    public DecisionRationaleDto() {
        this.keyStrengths = new ArrayList<>();
        this.keyRisksOrDeficits = new ArrayList<>();
    }

    // --- Getters & Setters ---

    public String getWhoStatement() {
        return whoStatement;
    }

    public void setWhoStatement(String whoStatement) {
        this.whoStatement = whoStatement;
    }

    public String getWhereStatement() {
        return whereStatement;
    }

    public void setWhereStatement(String whereStatement) {
        this.whereStatement = whereStatement;
    }

    public String getWhyStatement() {
        return whyStatement;
    }

    public void setWhyStatement(String whyStatement) {
        this.whyStatement = whyStatement;
    }

    public String getActionabilityGuidance() {
        return actionabilityGuidance;
    }

    public void setActionabilityGuidance(String actionabilityGuidance) {
        this.actionabilityGuidance = actionabilityGuidance;
    }

    public List<String> getKeyStrengths() {
        return keyStrengths;
    }

    public void setKeyStrengths(List<String> keyStrengths) {
        this.keyStrengths = keyStrengths != null ? keyStrengths : new ArrayList<>();
    }

    public void addKeyStrength(String strength) {
        if (this.keyStrengths == null) {
            this.keyStrengths = new ArrayList<>();
        }
        this.keyStrengths.add(strength);
    }

    public List<String> getKeyRisksOrDeficits() {
        return keyRisksOrDeficits;
    }

    public void setKeyRisksOrDeficits(List<String> keyRisksOrDeficits) {
        this.keyRisksOrDeficits = keyRisksOrDeficits != null ? keyRisksOrDeficits : new ArrayList<>();
    }

    public void addKeyRiskOrDeficit(String riskOrDeficit) {
        if (this.keyRisksOrDeficits == null) {
            this.keyRisksOrDeficits = new ArrayList<>();
        }
        this.keyRisksOrDeficits.add(riskOrDeficit);
    }
}
