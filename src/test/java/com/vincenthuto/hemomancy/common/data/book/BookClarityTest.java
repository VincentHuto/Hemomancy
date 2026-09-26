package com.vincenthuto.hemomancy.common.data.book;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookClarityTest {
    @Test
    void clarityRequiresAscensionAndUsesTheExistingFiveStages() {
        assertFalse(HemomancyBookPresentation.canReveal(false, 100, 1));
        for (int level = 1; level <= 5; level++) {
            float threshold = (level - 1) * 25;
            assertTrue(HemomancyBookPresentation.canReveal(true, threshold, level));
            if (level > 1) assertFalse(HemomancyBookPresentation.canReveal(true, threshold - 0.1f, level));
        }
        assertFalse(HemomancyBookPresentation.canReveal(true, 100, 6));
        assertFalse(HemomancyBookPresentation.canReveal(true, Float.NaN, 1));
    }
}
