package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.PhlegethonticBasinGuidance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhlegethonticBasinGuidanceTest {
    @Test
    void bearingNamesTheDirectionFromTheNetherStartingPoint() {
        assertEquals("east", PhlegethonticBasinGuidance.direction(100, 0));
        assertEquals("northwest", PhlegethonticBasinGuidance.direction(-100, -100));
        assertEquals("south", PhlegethonticBasinGuidance.direction(0, 100));
        assertEquals("here", PhlegethonticBasinGuidance.direction(0, 0));
    }
}
