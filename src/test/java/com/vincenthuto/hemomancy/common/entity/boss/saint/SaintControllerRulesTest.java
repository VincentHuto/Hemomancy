package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The controller's pure rules: the commit locks the aim, LEAD aims ahead, interrupts wait for the recovery boundary,
 * the wind-up and reveal floors hold whatever a phase or deed cuts, and a recovery opening stretches the recovery.
 */
final class SaintControllerRulesTest {
	private static final Vec3 LOCKED = new Vec3(1, 64, 1);
	private static final Vec3 TARGET = new Vec3(5, 64, 9);
	private static final Vec3 MOTION = new Vec3(0.2, 0, -0.1);

	@Test
	void theCommitLocksTheAim() {
		assertEquals(TARGET, SaintControllerRules.aim(SaintMove.Aim.TRACK, false, LOCKED, TARGET, MOTION, 0),
				"a tracking wind-up follows the target");
		assertEquals(LOCKED, SaintControllerRules.aim(SaintMove.Aim.TRACK, true, LOCKED, TARGET, MOTION, 0),
				"after the commit it keeps the locked point");
		assertEquals(LOCKED, SaintControllerRules.aim(SaintMove.Aim.LOCK_AT_START, false, LOCKED, TARGET, MOTION, 0),
				"locked at the start, it never follows");
	}

	@Test
	void leadAimsAheadOfTheTargetsMotion() {
		Vec3 led = SaintControllerRules.aim(SaintMove.Aim.LEAD, false, LOCKED, TARGET, MOTION, 10);
		assertEquals(7.0D, led.x, 1.0E-9);
		assertEquals(8.0D, led.z, 1.0E-9);
		assertEquals(64.0D, led.y, 1.0E-9, "the lead is planar");
		assertEquals(LOCKED, SaintControllerRules.aim(SaintMove.Aim.LEAD, true, LOCKED, TARGET, MOTION, 10));
	}

	@Test
	void interruptsWaitForTheRecoveryBoundary() {
		assertTrue(SaintControllerRules.interruptible(false, BossMoveTimeline.Stage.COMPLETE), "no move");
		assertFalse(SaintControllerRules.interruptible(true, BossMoveTimeline.Stage.WINDUP));
		assertFalse(SaintControllerRules.interruptible(true, BossMoveTimeline.Stage.ACTIVE),
				"a stagger or a phase change never cuts an active move");
		assertTrue(SaintControllerRules.interruptible(true, BossMoveTimeline.Stage.RECOVERY));
		SaintPhaseTable phases = SaintPhaseTable.byHealth(
				new SaintPhaseTable.Phase(1, "", net.minecraft.world.BossEvent.BossBarColor.RED, 1.0F, null),
				new SaintPhaseTable.Phase(2, "", net.minecraft.world.BossEvent.BossBarColor.PURPLE, 0.5F, null));
		phases.advance(2, SaintCombatController.Cut.IMMEDIATE);
		assertTrue(phases.update(phases.target(1.0F, 0.0F, 1), false), "Cut.IMMEDIATE does not wait for the boundary");
	}

	@Test
	void windupAndRevealFloorsHold() {
		BossMoveTimeline quick = new BossMoveTimeline(8, 4, 6);
		assertEquals(12, SaintControllerRules.effective(SaintMove.Kind.MELEE, quick).windup());
		assertEquals(20, SaintControllerRules.effective(SaintMove.Kind.AREA, quick).windup());
		assertEquals(30, SaintControllerRules.effective(SaintMove.Kind.AREA, new BossMoveTimeline(30, 4, 6)).windup());
		assertEquals(20, SaintMoves.cut(SaintMove.Kind.AREA, 24, 0.5F), "a phase cut never dips under 20");
		assertEquals(18, SaintMoves.cut(SaintMove.Kind.MELEE, 24, 0.25F));
		assertEquals(12, SaintMoves.cut(SaintMove.Kind.MELEE, 14, 0.5F));
		BossMoveTimeline area = new BossMoveTimeline(32, 6, 10);
		TelegraphSpec hidden = new TelegraphSpec(null, null, 28, 0, true);
		assertEquals(12, SaintControllerRules.revealTick(hidden, area, SaintMove.Kind.AREA),
				"a deed's late reveal still shows 20 ticks before the commit");
		assertEquals(0, SaintControllerRules.revealTick(TelegraphSpec.DEFAULT, area, SaintMove.Kind.AREA));
		assertEquals(4, SaintControllerRules.revealTick(new TelegraphSpec(null, null, 4, 0, true), area,
				SaintMove.Kind.AREA));
		assertEquals(20, SaintControllerRules.revealTick(hidden, area, SaintMove.Kind.MELEE));
	}

	@Test
	void aRecoveryOpeningStretchesTheRecovery() {
		BossMoveTimeline timeline = new BossMoveTimeline(20, 6, 10);
		assertEquals(30, SaintControllerRules.withOpening(timeline, 30).recovery());
		assertEquals(10, SaintControllerRules.withOpening(timeline, 8).recovery(), "a short opening keeps the recovery");
		assertEquals(20, SaintControllerRules.withOpening(timeline, 30).windup());
	}

	@Test
	void theAreaRuleReadsTheFootprint() {
		BossFootprint.Height h = BossFootprint.Height.STANDING;
		assertEquals(SaintMove.Kind.MELEE, SaintMoves.kindFor(new BossFootprint.Cone(0, 6.5, 120, 0, h)));
		assertEquals(SaintMove.Kind.AREA, SaintMoves.kindFor(new BossFootprint.Lane(14, 0.5, 1, 2, 0, 1, 0, h)),
				"reaching past 7 blocks");
		assertEquals(SaintMove.Kind.AREA, SaintMoves.kindFor(new BossFootprint.Disc(BossFootprint.Anchor.TARGET, 2, h)),
				"not attached to the body");
		assertEquals(SaintMove.Kind.MELEE, SaintMoves.kindFor(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 3.5, h)));
		assertEquals(SaintMove.Kind.AREA, SaintMoves.kindFor(new BossFootprint.Union(List.of(
				new BossFootprint.Union.Part(new BossFootprint.Disc(BossFootprint.Anchor.ORIGIN, 3, h), 0),
				new BossFootprint.Union.Part(new BossFootprint.Region(BossFootprint.Area.BOSS_FLOOR, 0, h), 6)))));
		assertEquals(SaintMove.Kind.MELEE, SaintMoves.kindFor(BossFootprint.None.INSTANCE));
		assertTrue(SaintControllerRules.aimMoved(Vec3.ZERO, new Vec3(0.05, 0, 0)));
		assertFalse(SaintControllerRules.aimMoved(Vec3.ZERO, new Vec3(0.04, 0, 0)));
	}
}
