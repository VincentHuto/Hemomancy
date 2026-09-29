package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurveyProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverworldFungalSurveyProgressTest {
    @Test
    void needsRealVisitAndTwoDifferentSpecimensBeforeReport() {
        assertFalse(new OverworldFungalSurveyProgress(false, 2, false).ready());
        assertFalse(new OverworldFungalSurveyProgress(true, 1, false).ready());
        assertTrue(new OverworldFungalSurveyProgress(true, 2, false).ready());
        assertFalse(new OverworldFungalSurveyProgress(true, 2, true).ready());
    }
}
