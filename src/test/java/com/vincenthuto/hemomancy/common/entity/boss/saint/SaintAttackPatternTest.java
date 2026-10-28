package com.vincenthuto.hemomancy.common.entity.boss.saint;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SaintAttackPatternTest {
    @Test void aSweepNeverHitsBeyondTheWarnedFanAtEitherEnd() {
        var sweep=SaintAttackPattern.CENSER_SWEEP;
        double x=Math.sin(Math.toRadians(70))*6,z=Math.cos(Math.toRadians(70))*6;
        assertFalse(sweep.hits(-x,z,0,8,sweep.windup()));
        assertFalse(sweep.hits(x,z,0,8,sweep.windup()+sweep.active()-1));
        assertFalse(SaintAttackPattern.MARROW_RUPTURE.hits(0,10.2,0,8,55));
        assertFalse(SaintAttackPattern.FROZEN_ADVANCE.hits(0,10.2,0,8,69));
    }
    @Test void motionIsBoundedAndReturnsToRestAfterEveryAttack() {
        for(var move:SaintAttackPattern.values()) {
            for(int remaining=-4;remaining<move.duration()+4;remaining++) {
                var motion=move.motion(remaining);
                assertTrue(Float.isFinite(motion.gather()) && motion.gather()>=0 && motion.gather()<=1);
                assertTrue(Float.isFinite(motion.strike()) && motion.strike()>=0 && motion.strike()<=1);
            }
            assertEquals(0,move.motion(0).gather());
            assertEquals(0,move.motion(0).strike());
        }
    }
    @Test void allMovesWarnBeforeAnyHitAndRetireAfterRecovery() {
        for(var move:SaintAttackPattern.values()) {
            for(int age=0;age<move.windup();age++)assertFalse(move.damageTick(age),move.name());
            assertTrue(move.damageTick(move.windup()),move.name());
            assertFalse(move.damageTick(move.windup()+move.active()),move.name());
            assertFalse(move.damageTick(move.duration()),move.name());
            assertNull(SaintAttackPattern.fromCue(0));
            assertSame(move,SaintAttackPattern.fromCue(move.cue()));
        }
    }
    @Test void parallelRakesAndFrozenAdvanceLeaveActualSafeGaps() {
        for(var move:new SaintAttackPattern[]{SaintAttackPattern.IRON_RAKE,SaintAttackPattern.FROZEN_ADVANCE}) {
            assertTrue(move.hits(0,5,0,8,move.windup()+move.active()/2));
            assertFalse(move.hits(1,5,0,8,move.windup()+move.active()/2));
            assertFalse(move.hits(0,-2,0,8,move.windup()));
        }
    }
    @Test void lanceDirectionIsLockedAndCrossHasDiagonalEscapes() {
        var lance=SaintAttackPattern.NERVE_LANCE;
        assertTrue(lance.hits(0,6,0,8,lance.windup()));
        assertFalse(lance.hits(3,6,0,8,lance.windup()));
        var cross=SaintAttackPattern.SHADOW_CROSS;
        assertTrue(cross.hits(0,8,0,8,cross.windup()));
        assertFalse(cross.hits(2,10,0,8,cross.windup()));
    }
    @Test void marrowRingTravelsAndGardenStaysAtItsCommittedTarget() {
        var ring=SaintAttackPattern.MARROW_RUPTURE;
        assertFalse(ring.hits(0,8,0,8,ring.windup()));
        assertTrue(ring.hits(0,5,0,8,ring.windup()+ring.active()/2));
        var garden=SaintAttackPattern.SPORE_GARDEN;
        assertTrue(garden.hits(0,8,0,8,garden.windup()));
        assertFalse(garden.hits(0,3,0,8,garden.windup()));
    }
}
