package com.vincenthuto.hemomancy.client.render.outpouring;

import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Detail;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Mode;
import com.vincenthuto.hemomancy.client.render.outpouring.OutpouringRules.Stage;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType.*;
import static org.junit.jupiter.api.Assertions.*;

class OutpouringRulesTest {
    @Test void profilesGrowMonotonically() {
        var ranks = EnumManipulationRank.values();
        for (int i = 1; i < ranks.length; i++) {
            var a = OutpouringRules.profile(ranks[i - 1]);
            var b = OutpouringRules.profile(ranks[i]);
            assertTrue(b.volume() >= a.volume() && b.reach() >= a.reach() && b.wildness() >= a.wildness()
                    && b.markLife() >= a.markLife() && b.maxMarks() >= a.maxMarks() && b.veinReach() >= a.veinReach()
                    && b.aftermath() >= a.aftermath(), ranks[i] + " must not shrink");
        }
        assertEquals(5.0, OutpouringRules.profile(PERFECTUS).reach());
    }

    @Test void woundRosterCoversOnBodySpells() {
        for (String id : new String[]{"hematic_riposte", "crimson_coronation", "summon_avatar_arms", "conjure_blade"})
            assertTrue(OutpouringRules.woundOnly(id), id);
        assertFalse(OutpouringRules.woundOnly("blood_shot"));
        assertEquals(Mode.WOUND, OutpouringRules.mode("hematic_riposte", QUICK));
        assertEquals(Mode.FULL, OutpouringRules.mode("blood_shot", null));
    }

    @Test void passivesToggle() {
        assertEquals(Mode.TOGGLE, OutpouringRules.mode("blackhearted", PASSIVE));
    }

    @Test void chargedBuildsWithCharge() {
        var pulse = OutpouringRules.pulse(CHARGED, CastPhase.HOLD, .5f, 0, 0, SUMMA);
        assertEquals(Stage.BUILD, pulse.stage());
        assertEquals(.45f, pulse.strength(), 1e-4);
    }

    @Test void quickPeaksOnArrival() {
        var pulse = OutpouringRules.pulse(QUICK, CastPhase.RELEASE, 0, 2, 0, SUMMA);
        assertEquals(Stage.PEAK, pulse.stage());
        assertEquals(1f, pulse.strength(), 1e-4);
    }

    @Test void aftermathLastsRankTicks() {
        assertEquals(Stage.DONE, OutpouringRules.pulse(QUICK, CastPhase.RELEASE, 0, 8 + 40, 0, SUMMA).stage());
        assertEquals(Stage.AFTERMATH, OutpouringRules.pulse(QUICK, CastPhase.RELEASE, 0, 8 + 39, 0, SUMMA).stage());
    }

    @Test void channelRampsThenHolds() {
        assertEquals(.8f, OutpouringRules.pulse(CONTINUOUS, CastPhase.HOLD, 0, 100, 0, PERFECTUS).strength(), 1e-4);
        assertTrue(OutpouringRules.pulse(CONTINUOUS, CastPhase.OPENING, 0, 2, 0, PERFECTUS).strength() < .8f);
    }

    @Test void cancelWithdraws() {
        var pulse = OutpouringRules.pulse(CHARGED, CastPhase.CANCEL, 0, 3, .6f, SUMMA);
        assertEquals(Stage.WITHDRAW, pulse.stage());
        assertEquals(.3f, pulse.strength(), 1e-4);
        assertEquals(Stage.DONE, OutpouringRules.pulse(CHARGED, CastPhase.CANCEL, 0, 6, .6f, SUMMA).stage());
    }

    @Test void passiveIsReduced() {
        assertEquals(.55f, OutpouringRules.pulse(PASSIVE, CastPhase.RELEASE, 0, 4, 0, PERFECTUS).strength(), 1e-4);
        assertEquals(Stage.DONE, OutpouringRules.pulse(PASSIVE, CastPhase.RELEASE, 0, 8 + 14, 0, PERFECTUS).stage());
    }

    @Test void detailThresholds() {
        assertEquals(Detail.FULL, OutpouringRules.detail(0, 10 * 10));
        assertEquals(Detail.REDUCED, OutpouringRules.detail(0, 30 * 30));
        assertEquals(Detail.VEINS, OutpouringRules.detail(0, 40 * 40));
        assertEquals(Detail.NONE, OutpouringRules.detail(0, 50 * 50));
        assertEquals(Detail.VEINS, OutpouringRules.detail(2, 1));
        assertEquals(Detail.REDUCED, OutpouringRules.detail(1, 1));
    }

    @Test void unknownIdFallsBackToArms() {
        assertNull(OutpouringRules.previewSection("preview"));
        assertNull(OutpouringRules.previewSection(null));
        assertEquals(EnumVeinSections.HEART, OutpouringRules.previewSection(OutpouringRules.previewId(EnumVeinSections.HEART)));
    }

    @Test void bodyContactPeaksWithRank() {
        var peak = OutpouringRules.pulse(QUICK, CastPhase.RELEASE, 0, 4, 0, PERFECTUS);
        assertEquals(1.0, OutpouringRules.bodyContact(peak, PERFECTUS), 1e-6);
        assertEquals(.2, OutpouringRules.bodyContact(peak, HUMILIS), 1e-6);
        var build = OutpouringRules.pulse(CHARGED, CastPhase.HOLD, .5f, 0, 0, PERFECTUS);
        assertEquals(0, OutpouringRules.bodyContact(build, PERFECTUS));
    }
}
