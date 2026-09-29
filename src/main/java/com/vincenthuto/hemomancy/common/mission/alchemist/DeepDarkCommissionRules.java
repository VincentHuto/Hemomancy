package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch;

import static com.vincenthuto.hemomancy.common.antecedent.AntecedentResearch.Evidence.*;

public final class DeepDarkCommissionRules {
    private DeepDarkCommissionRules() {
    }

    public static boolean hasSampleProof(AntecedentResearch research) {
        return research.has(SAMPLE_ANALYZED) || research.has(SAMPLE_RESPONSE)
                || research.has(CONTROLLED_REPLAY) || research.has(VICAR_RECOGNITION);
    }
}
