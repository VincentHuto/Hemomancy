package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroductionProgress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoyagerIntroductionProgressTest {
    @Test
    void needsObservationBeforeReport() {
        assertFalse(new VoyagerIntroductionProgress(false, false).ready());
        assertTrue(new VoyagerIntroductionProgress(true, false).ready());
        assertFalse(new VoyagerIntroductionProgress(true, true).ready());
    }
}
