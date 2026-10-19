package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaEncounterRules.Move;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentCombatRules.Attack;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Mode;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.PriestessCombatRules.Wing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnettaCombatRulesTest {
    @Test
    void breakNeedsBothTwoMinutesAndFiftyDamage() {
        assertFalse(AnnettaEncounterRules.breakReady(2399, 400.0F));
        assertFalse(AnnettaEncounterRules.breakReady(9000, 49.9F));
        assertTrue(AnnettaEncounterRules.breakReady(2400, 50.0F));
    }

    @Test
    void phaseOneDamageNeverKillsHer() {
        assertEquals(9.0F, AnnettaEncounterRules.guardedDamage(10.0F, 30.0F), 1.0E-6F);
        assertEquals(4.0F, AnnettaEncounterRules.guardedDamage(10.0F, 4.0F), 1.0E-6F);
        assertEquals(0.0F, AnnettaEncounterRules.guardedDamage(1.0F, 12.0F), 1.0E-6F);
    }

    @Test
    void breakSceneRunsInOrder() {
        int[] beats = {AnnettaEncounterRules.SHIELD_DROP, AnnettaEncounterRules.LAST_WORDS, AnnettaEncounterRules.GRIP,
                AnnettaEncounterRules.STAB,
                AnnettaEncounterRules.BURST, AnnettaEncounterRules.DAGGER_DROP, AnnettaEncounterRules.FALL_START,
                AnnettaEncounterRules.FALL_END, AnnettaEncounterRules.RISE};
        for (int i = 1; i < beats.length; i++) assertTrue(beats[i] > beats[i - 1], "beat " + i);
        assertFalse(AnnettaEncounterRules.headless(AnnettaEncounterRules.BURST - 1));
        assertTrue(AnnettaEncounterRules.headless(AnnettaEncounterRules.BURST));
        // A second or two headless before she falls, and about ten seconds of creeping blood.
        int standing = AnnettaEncounterRules.FALL_START - AnnettaEncounterRules.BURST;
        assertTrue(standing >= 20 && standing <= 45, "headless stand " + standing);
        int creep = AnnettaEncounterRules.RISE - AnnettaEncounterRules.FALL_END;
        assertTrue(creep >= 180 && creep <= 240, "creep " + creep);
    }

    @Test
    void tickSwellsAgainstTheBladeUntilItPops() {
        // Whatever Phase 1 left it at (full health 0.15, near death 1.0), it only grows, and stays smaller than her head.
        for (float phase : new float[]{0.15F, 0.6F, 1.0F}) {
            assertEquals(phase, AnnettaEncounterRules.breakSwell(AnnettaEncounterRules.STAB, phase), 1.0E-6F);
            float last = phase;
            for (int t = AnnettaEncounterRules.STAB; t <= AnnettaEncounterRules.BURST; t++) {
                float swell = AnnettaEncounterRules.breakSwell(t, phase);
                assertTrue(swell >= last, "the tick never shrinks while the blade is in it");
                last = swell;
            }
            assertTrue(last > phase + 0.2F && last <= 1.25F, "visibly swollen, never a block: " + last);
        }
        assertTrue(AnnettaEncounterRules.BURST - AnnettaEncounterRules.STAB >= 10, "long enough to read the swell");
        assertTrue(AnnettaEncounterRules.SHIELD_BASH_HIT < AnnettaEncounterRules.SHIELD_BASH_TICKS);
    }

    @Test
    void bloodCreepsMonotonically() {
        float last = -1.0F;
        for (int t = 0; t <= AnnettaEncounterRules.RISE + 10; t++) {
            float creep = AnnettaEncounterRules.creep(t);
            assertTrue(creep >= last && creep >= 0.0F && creep <= 1.0F);
            last = creep;
        }
        assertEquals(0.0F, AnnettaEncounterRules.creep(AnnettaEncounterRules.FALL_END - 1), 1.0E-6F);
        assertEquals(1.0F, AnnettaEncounterRules.creep(AnnettaEncounterRules.RISE), 1.0E-6F);
        assertTrue(AnnettaEncounterRules.reanimationPoolRadius(AnnettaEncounterRules.RISE) > 2.4F);
    }

    @Test
    void purgeDrainsHairThenTearsTheInfectionOut() {
        assertEquals(0.0F, AnnettaEncounterRules.hairDrain(AnnettaEncounterRules.DRINK_END), 1.0E-6F);
        assertEquals(1.0F, AnnettaEncounterRules.hairDrain(AnnettaEncounterRules.TEAR), 1.0E-6F);
        assertTrue(AnnettaEncounterRules.TEAR < AnnettaEncounterRules.PURGE_END);
        assertTrue(AnnettaEncounterRules.TEAR == LatentCombatRules.EMERGE_HIDDEN,
                "the infection breaks the surface the moment it tears out of her");
    }

    @Test
    void phaseOneMovesFitTheDistance() {
        for (int roll = 0; roll < 100; roll++) {
            Move close = AnnettaEncounterRules.choose(2.0D, false, roll);
            assertNotEquals(Move.STILL_BOLT, close, "she does not throw from arm's length");
            Move far = AnnettaEncounterRules.choose(14.0D, false, roll);
            assertTrue(far == Move.STILL_BOLT || far == Move.STILL_DASH, "far: " + far);
            Move middle = AnnettaEncounterRules.choose(8.0D, false, roll);
            assertNotEquals(Move.DAGGER_COMBO, middle);
            assertNotEquals(Move.SILVER_AURA, middle, "the aura only reaches six blocks");
        }
        boolean guards = false;
        for (int roll = 0; roll < 100; roll++) guards |= AnnettaEncounterRules.choose(2.0D, true, roll) == Move.SHIELD_GUARD;
        assertTrue(guards, "she raises the shield when she is being hit");
    }

    @Test
    void desperationShortensHerCooldowns() {
        for (Move move : Move.values()) {
            assertTrue(AnnettaEncounterRules.cooldownAfter(move, 0.1F) < AnnettaEncounterRules.cooldownAfter(move, 0.9F));
            assertTrue(AnnettaEncounterRules.duration(move) > 0);
        }
    }

    @Test
    void priestessEmergesThenTakesOff() {
        assertEquals(Mode.TAKEOFF, PriestessCombatRules.next(Mode.EMERGING, 0, false));
        assertEquals(Mode.HOVER, PriestessCombatRules.next(Mode.TAKEOFF, 50, false));
        assertEquals(Mode.WIND_BLAST, PriestessCombatRules.next(Mode.SHIELD, 50, false));
        assertEquals(Mode.TAKEOFF, PriestessCombatRules.next(Mode.LANDED, 50, false));
        boolean swoops = false, lands = false, plummets = false;
        for (int roll = 0; roll < 100; roll++) {
            Mode after = PriestessCombatRules.next(Mode.HOVER, roll, false);
            swoops |= after == Mode.SWOOP;
            lands |= after == Mode.LANDED;
            plummets |= after == Mode.PLUMMET;
            assertTrue(after == Mode.SWOOP || after == Mode.LANDED || after == Mode.PLUMMET);
        }
        assertTrue(swoops && lands && plummets);
        assertEquals(Mode.LANDED, PriestessCombatRules.next(Mode.PLUMMET, 0, false), "a plummet ends on foot");
        assertTrue(PriestessCombatRules.airborne(Mode.PLUMMET));
        assertEquals(Mode.SWOOP, PriestessCombatRules.next(Mode.HOVER, 99, true), "a fleeing target gets swooped");
    }

    @Test
    void priestessEmergeGrowsWingsAndHeadBeforeTheLanceaRises() {
        assertEquals(0.0F, PriestessCombatRules.wingGrowth(0), 1.0E-6F);
        assertEquals(1.0F, PriestessCombatRules.wingGrowth(PriestessCombatRules.EMERGE_LENGTH), 1.0E-6F);
        assertEquals(1.0F, PriestessCombatRules.headGrowth(PriestessCombatRules.EMERGE_LENGTH), 1.0E-6F);
        assertTrue(PriestessCombatRules.headGrowth(40) > PriestessCombatRules.wingGrowth(40), "the head knot sprouts first");
        assertTrue(PriestessCombatRules.LANCEA_AT > PriestessCombatRules.KNEEL_END
                && PriestessCombatRules.LANCEA_AT < PriestessCombatRules.EMERGE_LENGTH);
    }

    @Test
    void priestessSpearTimelinesRunInOrder() {
        assertTrue(PriestessCombatRules.LUNGE_WIND_END < PriestessCombatRules.LUNGE_HIT
                && PriestessCombatRules.LUNGE_HIT < PriestessCombatRules.REAP_WIND_START
                && PriestessCombatRules.REAP_START < PriestessCombatRules.REAP_HIT
                && PriestessCombatRules.REAP_HIT < PriestessCombatRules.REAP_END
                && PriestessCombatRules.REAP_END < PriestessCombatRules.LEAP_CROUCH
                && PriestessCombatRules.LEAP_JUMP < PriestessCombatRules.PLUNGE_DRIVE
                && PriestessCombatRules.PLUNGE_DRIVE < PriestessCombatRules.PLUNGE_HIT
                && PriestessCombatRules.PLUNGE_PULL < PriestessCombatRules.COMBO_LENGTH);
        assertTrue(PriestessCombatRules.PLUMMET_RISE_END < PriestessCombatRules.PLUMMET_HANG_END
                && PriestessCombatRules.PLUMMET_HANG_END < PriestessCombatRules.PLUMMET_LATEST_IMPACT);
        assertTrue(PriestessCombatRules.lineDelay(1) > PriestessCombatRules.lineDelay(0), "the thorns run away from her");
    }

    @Test
    void javelinLeavesHerHandAndGrowsBack() {
        assertEquals(1.0F, PriestessCombatRules.heldSpear(0), 1.0E-6F);
        assertEquals(0.0F, PriestessCombatRules.heldSpear(PriestessCombatRules.JAVELIN_RELEASE), 1.0E-6F);
        assertEquals(1.0F, PriestessCombatRules.heldSpear(PriestessCombatRules.JAVELIN_LENGTH), 1.0E-6F);
        assertEquals(1, PriestessCombatRules.javelinCount(0.9F));
        assertEquals(3, PriestessCombatRules.javelinCount(0.3F), "a wounded Priestess throws a fan");
    }

    @Test
    void hoverStaysBelowTheCeiling() {
        double[] open = PriestessCombatRules.hoverPoint(0, 64, 0, 0.0D, 6.0D, 4.5D, 30.0D);
        assertEquals(68.5D, open[1], 1.0E-6D);
        assertEquals(6.0D, Math.hypot(open[0], open[2]), 1.0E-6D);
        double[] low = PriestessCombatRules.hoverPoint(0, 64, 0, 1.0D, 6.0D, 4.5D, 3.0D);
        assertTrue(low[1] <= 64 + 3.0D - 2.4D + 1.0E-6D, "her body and wings fit under the roof");
    }

    @Test
    void priestessWingsFollowHerMode() {
        assertEquals(Wing.SHIELD, PriestessCombatRules.wing(Mode.SHIELD, false));
        assertEquals(Wing.SPREAD, PriestessCombatRules.wing(Mode.HOVER, true));
        assertEquals(Wing.SPREAD, PriestessCombatRules.wing(Mode.WIND_BLAST, false));
        assertEquals(Wing.FOLDED, PriestessCombatRules.wing(Mode.LANDED, true));
        assertTrue(PriestessCombatRules.airborne(Mode.HOVER) && !PriestessCombatRules.airborne(Mode.LANDED));
    }

    @Test
    void latentAttacksFitTheDistance() {
        for (int roll = 0; roll < 100; roll++) {
            Attack close = LatentCombatRules.choose(2.5D, roll, false, false);
            assertTrue(close == Attack.CLAW_RAKE || close == Attack.SPEAR_THRUST, "close: " + close);
            Attack reach = LatentCombatRules.choose(6.0D, roll, false, false);
            assertTrue(reach == Attack.WHIP_LASH || reach == Attack.SPEAR_THRUST, "reach: " + reach);
            assertEquals(Attack.ROOT_ERUPTION, LatentCombatRules.choose(12.0D, roll, false, true));
        }
        assertEquals(Attack.INFECTION_BLOOM, LatentCombatRules.choose(3.0D, 10, true, false));
        assertTrue(LatentCombatRules.hitTick(Attack.WHIP_LASH) < LatentCombatRules.duration(Attack.WHIP_LASH));
    }

    @Test
    void latentTurnsOnAnnettaNowAndThen() {
        assertFalse(LatentCombatRules.turnOnAnnetta(0, 100));
        assertTrue(LatentCombatRules.turnOnAnnetta(0, 400));
        assertFalse(LatentCombatRules.turnOnAnnetta(90, 400));
        assertEquals(0.0F, LatentCombatRules.rise(LatentCombatRules.EMERGE_HIDDEN), 1.0E-6F);
        assertEquals(1.0F, LatentCombatRules.rise(LatentCombatRules.EMERGE_END), 1.0E-6F);
    }
}
