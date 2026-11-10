package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingSyringeDrawTimelineTest {
    @Test
    void theVialFillsWhenTheBloodArrivesAndTheDrumWaitsForItToFinish() {
        assertEquals(6, LivingSyringeDrawTimeline.shownLoad(7, 0));
        assertEquals(1, LivingSyringeDrawTimeline.head(LivingSyringeDrawTimeline.FILL));
        assertEquals(7, LivingSyringeDrawTimeline.shownLoad(7, LivingSyringeDrawTimeline.FILL));
        assertEquals(6, LivingSyringeDrawTimeline.drumLoad(7, LivingSyringeDrawTimeline.BLOOD_END - .5F));
        assertEquals(7, LivingSyringeDrawTimeline.drumLoad(7, LivingSyringeDrawTimeline.INDEX));
    }

    @Test
    void theBloodDrainsFromTheMobEnd() {
        assertEquals(0, LivingSyringeDrawTimeline.tail(LivingSyringeDrawTimeline.TAIL_START));
        assertEquals(1, LivingSyringeDrawTimeline.tail(LivingSyringeDrawTimeline.BLOOD_END));
    }

    @Test
    void theArmHoldsStillWhileTheDrumTurnsThenLetsGo() {
        assertEquals(1, LivingSyringeDrawTimeline.weight(LivingSyringeDrawTimeline.INDEX));
        assertEquals(1, LivingSyringeDrawTimeline.weight(LivingSyringeDrawTimeline.RECOVER));
        assertEquals(1, LivingSyringeDrawTimeline.lift(LivingSyringeDrawTimeline.INDEX));
        assertEquals(0, LivingSyringeDrawTimeline.pull(LivingSyringeDrawTimeline.INDEX));
        assertEquals(0, LivingSyringeDrawTimeline.weight(LivingSyringeDrawTimeline.DURATION));
        assertTrue(LivingSyringeDrawTimeline.INDEX + 8 <= LivingSyringeDrawTimeline.RECOVER,
                "The drum's eight-tick turn has to finish before the arm drops");
    }
}
