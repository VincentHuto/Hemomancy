package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.shared.MnemonicRecipeKnowledgeRules;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicRecipeKnowledgeRulesTest {
    @Test
    void briefingTeachesOnlyTheStarterFormula() {
        assertEquals(Set.of("distillation/tincture_sanguine_fists"),
                MnemonicRecipeKnowledgeRules.starterRecipePaths());
    }

    @Test
    void catalogueContainsAllFlasksAllJugsAndTheCuredJug() {
        Set<String> paths = MnemonicRecipeKnowledgeRules.catalogueRecipePaths();

        assertEquals(17, paths.size());
        assertTrue(paths.contains("cured_clay_jug"));
        assertTrue(paths.contains("distillation/tincture_sanguine_fists"));
        assertTrue(paths.contains("distillation/tincture_carrion_metabolism_jug"));
    }

    @Test
    void eitherCompletionRouteUnlocksTheCatalogueWithoutGrantingReserve() {
        assertFalse(MnemonicRecipeKnowledgeRules.knowsCatalogue(false, false));
        assertTrue(MnemonicRecipeKnowledgeRules.knowsCatalogue(true, false));
        assertTrue(MnemonicRecipeKnowledgeRules.knowsCatalogue(false, true));
    }

    @Test
    void everyTaughtFormulaExistsAsARecipeResource() {
        Path root = Path.of("src/main/resources/data/hemomancy/recipe");
        for (String path : MnemonicRecipeKnowledgeRules.catalogueRecipePaths()) {
            assertTrue(Files.isRegularFile(root.resolve(path + ".json")), path);
        }
    }
}
