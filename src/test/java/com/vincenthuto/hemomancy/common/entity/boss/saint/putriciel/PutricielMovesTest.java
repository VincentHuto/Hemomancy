package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowContext;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintFollowUp;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPickRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every Putriciel move: ids, timelines and floors, phases, footprints that hit and miss, strings, cooldowns. */
final class PutricielMovesTest {
	private static final int[] PHASES = {1, 2, 3, 4};
	private static final Vec3 BODY = new Vec3(0.5D, 64.0D, 0.5D);
	/** Aimed south (+z), floor at 64. */
	private static final FootprintFrame FRAME = FootprintFrame.simple(BODY, BODY.add(0, 0, 10), BODY.add(0, 0, 5), 64.0D);

	private static SaintMoveInputs in(int phase) {
		return new SaintMoveInputs(phase, 0, 0, 1);
	}

	private static boolean hits(BossFootprint shape, FootprintFrame frame, double x, double y, double z, double age) {
		return shape.hits(frame, BODY.add(x, y, z), true, age);
	}

	@Test
	void idsAreUniqueAndKeysLowerSnake() {
		Set<Integer> ids = new HashSet<>();
		for (PutricielMove move : PutricielMove.values()) {
			assertTrue(move.id() >= 1 && move.id() <= 255);
			assertTrue(ids.add(move.id()), move + " repeats an id");
			assertTrue(move.key().matches("[a-z_]+"), move.key());
			assertEquals(move, PutricielMove.byId(move.id()));
		}
	}

	@Test
	void everyWindupMeetsItsKindsFloorInEveryPhaseAndDeed() {
		int repose = SaintDeeds.FORCED | 1 << ChapelRules.Kind.REPOSE.ordinal();
		for (PutricielMove move : PutricielMove.values()) {
			for (int phase : PHASES) {
				for (int mask : new int[]{0, repose}) {
					BossMoveTimeline timeline = move.timeline(new SaintMoveInputs(phase, 0, mask, 1));
					assertTrue(timeline.windup() >= SaintMoves.revealFloor(move.kind()), move + " in phase " + phase);
					assertTrue(timeline.active() >= 1 && timeline.recovery() >= 1, move.toString());
				}
			}
		}
		assertEquals(13, PutricielMove.CENSER_SWING.timeline(in(4)).windup(), "Rot III takes 3 off");
		assertEquals(12, PutricielMove.CENSER_BACKSWING.timeline(in(4)).windup(), "never under the melee floor");
		assertEquals(20, PutricielMove.OFFERING_GRASP.timeline(in(4)).windup(), "never under the area floor");
	}

	@Test
	void theReposeDeedCutsRotRecoveriesByAQuarter() {
		int repose = SaintDeeds.FORCED | 1 << ChapelRules.Kind.REPOSE.ordinal();
		assertEquals(14, PutricielMove.CENSER_SWING.timeline(in(1)).recovery());
		assertEquals(11, PutricielMove.CENSER_SWING.timeline(new SaintMoveInputs(1, 0, repose, 1)).recovery());
		assertEquals(PutricielMove.PYRE_HEART.timeline(in(2)).recovery(),
				PutricielMove.PYRE_HEART.timeline(new SaintMoveInputs(2, 0, repose, 1)).recovery(), "fire moves keep theirs");
	}

	@Test
	void areaMovesFollowResolutionB2() {
		for (PutricielMove move : List.of(PutricielMove.OFFERING_GRASP, PutricielMove.PURGING_SWEEP, PutricielMove.CENSER_DROP,
				PutricielMove.SHELF_VOLLEY, PutricielMove.ROOT_SNARE, PutricielMove.BLOOM_OF_ROT, PutricielMove.CENSER_CAST,
				PutricielMove.CAUTERISING_LANE, PutricielMove.PYRE_LINES, PutricielMove.FULL_PURGE)) {
			assertEquals(SaintMove.Kind.AREA, move.kind(), move.toString());
		}
		// a footprint the kit's rule calls AREA is declared AREA
		for (PutricielMove move : PutricielMove.values()) {
			if (SaintMoves.kindFor(move.footprint(in(move.fire() ? 2 : 1))) == SaintMove.Kind.AREA) {
				assertEquals(SaintMove.Kind.AREA, move.kind(), move.toString());
			}
		}
	}

	@Test
	void rotNeverBurnsAndFireNeverRots() {
		for (PutricielMove move : PutricielMove.values()) {
			for (int phase : PHASES) {
				if (!move.inPhase(phase)) continue;
				assertEquals(move.fire(), phase == PutricielPhase.INTERVAL.number(), move + " in phase " + phase);
			}
			TelegraphFamily family = move.telegraph(in(1)).fill();
			assertEquals(move.fire() ? TelegraphFamily.FLAME : TelegraphFamily.PUTRICIEL_ROT, family, move.toString());
			assertEquals(move.fire() ? com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes.PUTRICIEL_CLEANSING
					: com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDamageTypes.PUTRICIEL, move.damageType());
		}
		assertFalse(PutricielMove.CENSER_DROP.inPhase(1), "the drop starts in Rot II");
		assertTrue(PutricielMove.CENSER_ORBIT.inPhase(4) && !PutricielMove.CENSER_ORBIT.inPhase(3));
	}

	@Test
	void everyPickedMoveHasACooldownAndNoneRunsOnACadence() {
		for (PutricielMove move : PutricielMove.values()) {
			if (move.picked()) assertTrue(move.cooldown(null) > 0, move + " needs a cooldown");
		}
	}

	@Test
	void theCenserSwingSweepsRightToLeft() {
		BossFootprint swing = PutricielMove.CENSER_SWING.footprint(in(1));
		// right hand of a south-facing aim is -x (Minecraft's right of south is west)
		Vec3 right = BossFootprint.Shapes.at(FRAME, BODY, 60.0D, 4.0D).subtract(BODY);
		Vec3 left = BossFootprint.Shapes.at(FRAME, BODY, -60.0D, 4.0D).subtract(BODY);
		assertTrue(hits(swing, FRAME, right.x, 0, right.z, 0.0D) && !hits(swing, FRAME, left.x, 0, left.z, 0.0D));
		assertTrue(hits(swing, FRAME, left.x, 0, left.z, 6.0D) && !hits(swing, FRAME, right.x, 0, right.z, 6.0D));
		assertFalse(hits(swing, FRAME, 0, 0, 6.5D, 3.0D), "past 6 is clear");
	}

	@Test
	void theSporeCloudIsWalkedOutOfNotJumped() {
		BossFootprint cloud = PutricielMove.SAC_SWELL.footprint(in(1));
		assertTrue(hits(cloud, FRAME, 4.0D, 1.5D, 0, 18.0D), "a jump does not clear the full-height cloud");
		assertFalse(hits(cloud, FRAME, 8.5D + 1.0D, 0, 0, 36.0D), "outside its reach is safe");
		assertEquals(10.0D, PutricielMove.sacRadius(4));
	}

	@Test
	void theFullPurgeIsTheOneJumpRing() {
		BossFootprint purge = PutricielMove.FULL_PURGE.footprint(in(2));
		assertEquals(BossFootprint.Height.JUMPABLE, purge.height());
		assertTrue(hits(purge, FRAME, 4.0D, 0, 0, 8.0D));
		assertFalse(hits(purge, FRAME, 4.0D, 1.0D, 0, 8.0D), "a jump clears it");
		assertTrue(hits(purge, FRAME, 4.0D, 0, 0, 24.0D), "the second ring");
		int jumpRings = 0;
		for (PutricielMove move : PutricielMove.values()) {
			BossFootprint shape = move.footprint(in(move.fire() ? 2 : 1));
			if (shape.height().equals(BossFootprint.Height.JUMPABLE)) jumpRings++;
		}
		assertEquals(1, jumpRings, "one jump ring per Saint (resolution A-3)");
	}

	@Test
	void theIgnitionIsABodyBurst() {
		BossFootprint burst = PutricielMove.IGNITION.footprint(in(2));
		assertTrue(hits(burst, FRAME, 5.5D, 1.5D, 0, 1.0D));
		assertFalse(hits(burst, FRAME, 6.5D, 0, 0, 1.0D));
	}

	@Test
	void theCrossTheLaneTheAnnulusAndTheDiscs() {
		FootprintFrame frame = FRAME.withAim(BODY.add(0, 0, 8));
		BossFootprint snare = PutricielMove.ROOT_SNARE.footprint(in(1));
		assertTrue(hits(snare, frame, 0, 0, 8 + 4.0D, 0) && hits(snare, frame, 4.0D, 0, 8, 0));
		assertFalse(hits(snare, frame, 2.0D, 0, 10.0D, 0), "step off diagonally");
		BossFootprint grasp = PutricielMove.OFFERING_GRASP.footprint(in(1));
		assertTrue(hits(grasp, FRAME, 0, 0, 5.5D, 0) && !hits(grasp, FRAME, 1.0D, 0, 3.0D, 0));
		BossFootprint orbit = PutricielMove.CENSER_ORBIT.footprint(in(4));
		assertFalse(hits(orbit, FRAME, 0, 0, 1.0D, 0), "the eye at their feet");
		assertFalse(hits(orbit, FRAME, 0, 0, 5.0D, 0), "out past 4.5");
		FootprintFrame volley = FRAME.withPoints(List.of(BODY.add(3, 0, 3), BODY.add(-3, 0, 6)));
		BossFootprint discs = PutricielMove.SHELF_VOLLEY.footprint(in(1));
		assertTrue(hits(discs, volley, 3, 0, 3, 0) && !hits(discs, volley, -3, 0, 6, 0), "the second lands later");
		assertTrue(hits(discs, volley, -3, 0, 6, PutricielMove.VOLLEY_GAP));
		assertEquals(9, PutricielMove.volleyDiscs(new SaintMoveInputs(4, 0, 0, 4)), "capped at 9");
	}

	@Test
	void pyreLinesConvergeFromEachAnchorAndTheLaneRunsFromThePyre() {
		BossFootprint lines = PutricielMove.PYRE_LINES.footprint(new SaintMoveInputs(2, 2, 0, 1));
		FootprintFrame frame = FRAME.withAnchors(List.of(BODY.add(10, 0, 0), BODY.add(0, 0, -10)));
		assertTrue(hits(lines, frame, 5, 0, 0, 1) && hits(lines, frame, 0, 0, -5, 1));
		assertFalse(hits(lines, frame, 4, 0, 4, 1), "the wedges between are safe");
		assertTrue(PutricielMove.PYRE_LINES.footprint(new SaintMoveInputs(2, 0, 0, 1)) instanceof BossFootprint.None);
		BossFootprint lane = PutricielMove.CAUTERISING_LANE.footprint(in(2));
		FootprintFrame run = FRAME.withPoints(List.of(BODY.add(12, 0, 0), BODY));
		assertTrue(hits(lane, run, 10, 0, 1.2D, 2.5D), "three blocks wide, the front near the pyre first");
		assertFalse(hits(lane, run, 2, 0, 0, 2.5D), "not yet at the body");
	}

	@Test
	void theBloomSparesItsExcludedCells() {
		BossFootprint bloom = PutricielMove.BLOOM_OF_ROT.footprint(in(3));
		LongOpenHashSet safe = new LongOpenHashSet();
		safe.add(BossFootprint.cellAt(BODY.add(3, 0, 0)));
		FootprintFrame frame = FRAME.withCells(safe);
		assertTrue(hits(bloom, frame, 5, 0, 0, 0));
		assertFalse(hits(bloom, frame, 3, 0, 0, 0), "ash is safe ground");
		assertFalse(hits(bloom, frame, 13, 0, 0, 0));
	}

	@Test
	void stringsAreAtMostTwoDeepAndFollowTheirConditions() {
		SaintFollowContext near = new SaintFollowContext(null, 3, true, BossBands.Band.HUG, BossBands.Sector.FRONT);
		SaintFollowUp link = SaintPickRules.followUpLink(PutricielMove.CENSER_SWING, near, 0, false, () -> 0.0D);
		assertEquals(PutricielMove.CENSER_BACKSWING, link.next());
		assertEquals(PutricielMove.CENSER_DROP, SaintPickRules.followUp(PutricielMove.CENSER_BACKSWING, near, 1, false, () -> 0.0D));
		assertNull(SaintPickRules.followUp(PutricielMove.CENSER_BACKSWING, near, 2, false, () -> 0.0D), "two deep at most");
		SaintFollowContext rotOne = new SaintFollowContext(null, 1, true, BossBands.Band.HUG, BossBands.Sector.FRONT);
		assertNull(SaintPickRules.followUp(PutricielMove.CENSER_BACKSWING, rotOne, 1, false, () -> 0.0D), "no drop in Rot I");
		SaintFollowContext missed = new SaintFollowContext(null, 1, false, BossBands.Band.NEAR, BossBands.Sector.FRONT);
		assertNull(SaintPickRules.followUp(PutricielMove.OFFERING_GRASP, missed, 0, false, () -> 0.0D), "the breath needs a hit");
		SaintFollowContext mid = new SaintFollowContext(null, 2, false, BossBands.Band.MID, BossBands.Sector.FRONT);
		assertEquals(PutricielMove.CENSER_CAST, SaintPickRules.followUp(PutricielMove.PURGING_SWEEP, mid, 0, false, () -> 0.0D));
		assertNull(SaintPickRules.followUp(PutricielMove.PURGING_SWEEP, mid, 0, true, () -> 0.0D), "an interval cancels it");
	}

	@Test
	void openingsSitOnTheRightRecoveries() {
		assertEquals(34, PutricielOpenings.SPENT_SACS.ticks());
		assertEquals(30, PutricielOpenings.FOULED_CHAIN.ticks());
		assertEquals(1.5F, PutricielOpenings.SPENT_BLOOM.damageTaken());
		assertEquals(60, PutricielOpenings.EMBERING.ticks());
		assertEquals(PutricielOpenings.OFFERING_ID, PutricielOpenings.offering(60).id());
		assertFalse(PutricielOpenings.offering(50).counts().contains(
				com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening.Delivery.CHANNEL), "projection is read by the channel");
	}

	// ── the rot sludge (revision 2026-11-06, R2) ─────────────────────

	@Test
	void onlyTheFiveRotMovesLaySludgeAndNeverInAFireStage() {
		Set<PutricielMove> layers = new HashSet<>();
		for (PutricielMove move : PutricielMove.values()) {
			if (PutricielSludgeRules.lays(move, PutricielStage.ROT)) layers.add(move);
			for (PutricielStage stage : PutricielStage.values()) {
				if (stage.interval()) assertFalse(PutricielSludgeRules.lays(move, stage), move + " lays in " + stage);
			}
		}
		assertEquals(Set.of(PutricielMove.SHELF_VOLLEY, PutricielMove.CENSER_DROP, PutricielMove.OFFERING_GRASP,
				PutricielMove.ROOT_SNARE, PutricielMove.BLOOM_OF_ROT), layers);
		for (PutricielMove move : layers) {
			assertFalse(move.fire(), move + " is a rot move");
			for (int phase : PHASES) {
				if (move.inPhase(phase)) assertTrue(PutricielPhase.isRot(phase), move + " runs only in the rot");
			}
		}
	}
}
