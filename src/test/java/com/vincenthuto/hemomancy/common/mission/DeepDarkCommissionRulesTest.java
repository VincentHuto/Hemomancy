package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch;
import com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommissionRules;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeepDarkCommissionRulesTest {
    @Test
    void acceptsAnalysisAndLaterSampleStagesButNotUnrelatedVigilEvidence() {
        AntecedentResearch research = new AntecedentResearch();
        assertFalse(DeepDarkCommissionRules.hasSampleProof(research));
        research.record(VIGIL_RECORD_READ);
        research.record(ARCHIVE_RESPONSE);
        assertFalse(DeepDarkCommissionRules.hasSampleProof(research));

        for (var evidence : new AntecedentResearch.Evidence[]{
                SAMPLE_ANALYZED, SAMPLE_RESPONSE, CONTROLLED_REPLAY, VICAR_RECOGNITION}) {
            AntecedentResearch prior = new AntecedentResearch();
            prior.record(evidence);
            assertTrue(DeepDarkCommissionRules.hasSampleProof(prior), evidence.name());
        }
    }
}
