package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MorphlingHandlingProgressTest {
    @Test
    void onlyUnreportedPolypProofIsReady() {
        assertFalse(new MorphlingHandlingProgress(false, false).ready());
        assertTrue(new MorphlingHandlingProgress(true, false).ready());
        assertFalse(new MorphlingHandlingProgress(true, true).ready());
    }
}
