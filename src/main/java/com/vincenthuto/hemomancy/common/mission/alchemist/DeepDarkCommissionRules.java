package com.vincenthuto.hemomancy.common.mission.alchemist;

import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch;

import static com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch.Evidence.*;

public final class DeepDarkCommissionRules {
    private DeepDarkCommissionRules() {
    }

    public static boolean hasSampleProof(AntecedentResearch research) {
        return research.has(SAMPLE_ANALYZED) || research.has(SAMPLE_RESPONSE)
                || research.has(CONTROLLED_REPLAY) || research.has(VICAR_RECOGNITION);
    }
}
