package com.vincenthuto.hemomancy.common.mission.shared;

import java.util.LinkedHashSet;
import java.util.Set;

public final class MnemonicRecipeKnowledgeRules {
    private static final String[] MEMORY_IDS = {
            "sanguine_fists", "laboring_arms", "coursing_legs", "hushed_gait",
            "predatory_eyes", "second_pulse", "enduring_viscera", "carrion_metabolism"
    };
    private static final Set<String> STARTER = Set.of("distillation/tincture_sanguine_fists");
    private static final Set<String> CATALOGUE = buildCatalogue();

    private MnemonicRecipeKnowledgeRules() {
    }

    public static Set<String> starterRecipePaths() {
        return STARTER;
    }

    public static Set<String> catalogueRecipePaths() {
        return CATALOGUE;
    }

    public static boolean knowsCatalogue(boolean bodyAnswersComplete, boolean wovenVesselFinished) {
        return bodyAnswersComplete || wovenVesselFinished;
    }

    private static Set<String> buildCatalogue() {
        Set<String> paths = new LinkedHashSet<>();
        paths.add("cured_clay_jug");
        for (String id : MEMORY_IDS) {
            paths.add("distillation/tincture_" + id);
            paths.add("distillation/tincture_" + id + "_jug");
        }
        return Set.copyOf(paths);
    }
}
