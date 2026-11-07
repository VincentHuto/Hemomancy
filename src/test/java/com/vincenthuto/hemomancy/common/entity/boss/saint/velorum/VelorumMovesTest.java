package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMove;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoveInputs;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintMoves;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumMovesTest {
	/** He stands at the origin facing +z (aim 10 blocks ahead); the floor is y 0. */
	private static final FootprintFrame FRAME = FootprintFrame.simple(Vec3.ZERO, new Vec3(0, 0, 10), new Vec3(0, 0, 10), 0.0D);
	private static final int REPOSE = SaintDeeds.FORCED | 1 << ChapelRules.Kind.REPOSE.ordinal();

	@Test
	void twentyOneMovesWithUniqueIdsAndLowerKeys() {
		Set<Integer> ids = new HashSet<>();
		for (VelorumMove move : VelorumMove.values()) {
			assertTrue(move.id() >= 1 && move.id() <= 255);
			assertTrue(ids.add(move.id()), move + " id");
			assertEquals(move.name().toLowerCase(java.util.Locale.ROOT), move.key());
			assertEquals(move, VelorumMove.byId(move.id()));
		}
		assertEquals(21, ids.size());
		// the Release keeps the old splinter rows' id, so a synced move id means the same move
		assertEquals(4, VelorumMove.CRUOR_RELEASE.id());
		assertEquals("cruor_release", VelorumMove.CRUOR_RELEASE.key());
		assertEquals(List.of(16, 17, 18, 19, 20, 21), List.of(VelorumMove.ARRESTED_PULSE.id(), VelorumMove.CRUOR_JAVELINS.id(),
				VelorumMove.VEINS_OF_FROST.id(), VelorumMove.UMBRAL_STEP.id(), VelorumMove.GLOAM_CUTS.id(),
				VelorumMove.ECLIPSE.id()));
	}

	@Test
	void everyTimelineMeetsTheFloorsAfterThePhaseCutAndTheReposeDeed() {
		for (VelorumMove move : VelorumMove.values()) {
			for (int phase = 1; phase <= 3; phase++) {
				for (int mask : new int[]{0, REPOSE}) {
					BossMoveTimeline timeline = move.timeline(new SaintMoveInputs(phase, 0, mask, 1));
					assertTrue(timeline.windup() >= SaintMoves.revealFloor(move.kind()), move + " phase " + phase);
					assertTrue(timeline.recovery() <= move.authored().recovery(), move + " recovery");
				}
			}
			BossMoveTimeline repose = move.timeline(new SaintMoveInputs(1, 0, REPOSE, 1));
			if (move.authored().recovery() >= VelorumDeedRules.MIN_RECOVERY) {
				assertTrue(repose.recovery() >= VelorumDeedRules.MIN_RECOVERY, move + " never under 10");
			}
		}
		assertEquals(20, VelorumMove.HOLLOW_TOLL.timeline(new SaintMoveInputs(3, 0, 0, 1)).windup());
		assertEquals(26, VelorumMove.ENDLESS_HOUR.timeline(new SaintMoveInputs(3, 0, 0, 1)).windup());
		assertEquals(15, VelorumMove.CRUOR_RELEASE.timeline(new SaintMoveInputs(1, 0, REPOSE, 1)).recovery());
	}

	@Test
	void everyMoveThatOpensInItsRecoveryHasARecoveryTick() {
		// the kit opens a recovery opening on the recovery's first tick: the Long Night and the Eclipse open the Gutter
		for (VelorumMove move : List.of(VelorumMove.LONG_NIGHT, VelorumMove.ECLIPSE)) {
			for (int phase = 1; phase <= 3; phase++) {
				for (int mask : new int[]{0, REPOSE}) {
					BossMoveTimeline timeline = move.timeline(new SaintMoveInputs(phase, 0, mask, 1));
					assertEquals(BossMoveTimeline.Stage.RECOVERY, timeline.stageAt(timeline.windup() + timeline.active()),
							move + " phase " + phase);
				}
			}
		}
	}

	@Test
	void everyAreaFootprintIsDeclaredArea() {
		for (VelorumMove move : VelorumMove.values()) {
			BossFootprint shape = move.footprint(SaintMoveInputs.NONE);
			if (SaintMoves.kindFor(shape) == SaintMove.Kind.AREA) assertEquals(SaintMove.Kind.AREA, move.kind(), move.toString());
		}
		assertEquals(SaintMove.Kind.AREA, VelorumMove.SET_IN_PLACE.kind());
		assertEquals(20, VelorumMove.SET_IN_PLACE.timeline(SaintMoveInputs.NONE).windup());
	}

	@Test
	void theTollConeAndTheHangingLanternBehindHim() {
		BossFootprint toll = VelorumMove.STAFF_TOLL.footprint(SaintMoveInputs.NONE);
		assertTrue(toll.hits(FRAME, new Vec3(0, 0, 2.5), true, 0));
		assertFalse(toll.hits(FRAME, new Vec3(0, 0, -2), true, 0));
		assertFalse(toll.hits(FRAME, new Vec3(3, 0, 0.5), true, 0));
		BossFootprint rear = VelorumMove.HANGING_LANTERN.footprint(SaintMoveInputs.NONE);
		assertTrue(rear.hits(FRAME, new Vec3(0, 0, -2.5), true, 0));
		assertFalse(rear.hits(FRAME, new Vec3(0, 0, 2.5), true, 0));
	}

	@Test
	void theSweepTravelsAcrossItsArc() {
		BossFootprint.Sweep sweep = (BossFootprint.Sweep) VelorumMove.LANTERN_SWEEP.footprint(SaintMoveInputs.NONE);
		assertTrue(sweep.angle(0) < sweep.angle(6));
		assertFalse(sweep.hits(FRAME, new Vec3(0, 0, 0.5), true, 3));
	}

	@Test
	void theReleaseIsAFiveRowFanWithGapsThatClose() {
		BossFootprint.Lane fan = (BossFootprint.Lane) VelorumMove.CRUOR_RELEASE.footprint(SaintMoveInputs.NONE);
		assertEquals(5, fan.lanes());
		assertEquals(15.0D, fan.fanDegrees());
		assertTrue(fan.hits(FRAME, new Vec3(0, 0, 5), true, 6));
		assertFalse(fan.hits(FRAME, new Vec3(0, 0, 8), true, 6), "the front runs a block a tick");
		// between two rows 10 blocks out the gap is open
		double half = Math.toRadians(7.5D);
		assertFalse(fan.hits(FRAME, new Vec3(Math.sin(half) * 10, 0, Math.cos(half) * 10), true, 12));
		assertEquals(14.0D, ((BossFootprint.Lane) VelorumMove.CRUOR_RELEASE.footprint(new SaintMoveInputs(3, 0, 0, 1))).length());
	}

	@Test
	void theStillFloorTakesOnlyTheGroundedOutsideTheEye() {
		BossFootprint ring = VelorumMove.STILL_FLOOR.footprint(SaintMoveInputs.NONE);
		assertTrue(ring.hits(FRAME, new Vec3(0, 0, 5), true, 0));
		assertFalse(ring.hits(FRAME, new Vec3(0, 0, 5), false, 0), "a jump clears it");
		assertFalse(ring.hits(FRAME, new Vec3(0, 0, 2), true, 0), "the eye stays clear");
		assertFalse(ring.hits(FRAME, new Vec3(0, 0, 10.5), true, 0));
	}

	@Test
	void theHollowTollIsAFullHeightRingThatPassesThrough() {
		BossFootprint.Ring ring = (BossFootprint.Ring) VelorumMove.HOLLOW_TOLL.footprint(SaintMoveInputs.NONE);
		assertEquals(BossFootprint.Height.STANDING, ring.height());
		assertEquals(9.0D, ring.radius(18, 0), 1.0E-6D);
		assertTrue(ring.radius(9, 0) > 4.0D && ring.radius(9, 0) < 6.0D);
	}

	@Test
	void theProcessionsShadesWalkTwoBlocksApart() {
		BossFootprint.Path path = (BossFootprint.Path) VelorumMove.PROCESSION.footprint(SaintMoveInputs.NONE);
		assertEquals(3, path.walkers());
		assertEquals(2.0D, path.walker(10, 0) - path.walker(10, 1), 1.0E-6D);
		FootprintFrame line = FRAME.withPoints(List.of(new Vec3(-9, 0, 10), new Vec3(9, 0, 10)));
		assertTrue(path.hits(line, new Vec3(-4, 0, 10), true, 10));
		assertFalse(path.hits(line, new Vec3(4, 0, 10), true, 10));
	}

	@Test
	void phasesAndPicking() {
		for (VelorumMove move : List.of(VelorumMove.RECALL, VelorumMove.CALL_TO_PRAYER, VelorumMove.PROCESSION,
				VelorumMove.SPLINTER_CROWN)) {
			assertFalse(move.inPhase(1), move.toString());
			assertTrue(move.inPhase(2) && move.inPhase(3), move.toString());
		}
		assertTrue(VelorumMove.HOLLOW_TOLL.inPhase(3) && !VelorumMove.HOLLOW_TOLL.inPhase(2));
		assertTrue(VelorumMove.ENDLESS_HOUR.inPhase(3) && !VelorumMove.ENDLESS_HOUR.picked());
		assertTrue(VelorumMove.STAFF_TOLL.inPhase(1) && VelorumMove.STAFF_TOLL.inPhase(3));
		assertEquals(List.of(VelorumMove.BID_TO_KNEEL, VelorumMove.RECALL, VelorumMove.SILENCE_DRAW), VelorumMove.INVOCATIONS);
	}

	@Test
	void stringsAreTheDesignsFour() {
		assertEquals(VelorumMove.LANTERN_SWEEP, VelorumMove.STAFF_TOLL.followUps(1).get(0).next());
		assertEquals(VelorumMove.HOLLOW_TOLL, VelorumMove.STAFF_TOLL.followUps(3).get(0).next());
		assertTrue(VelorumMove.LANTERN_SWEEP.followUps(1).isEmpty());
		assertEquals(VelorumMove.CRUOR_RELEASE, VelorumMove.LANTERN_SWEEP.followUps(2).get(0).next());
		assertEquals(VelorumMove.CRUOR_RELEASE, VelorumMove.BID_TO_KNEEL.followUps(1).get(0).next());
		assertEquals(VelorumMove.SPLINTER_CROWN, VelorumMove.STILL_FLOOR.followUps(2).get(0).next());
		assertEquals(VelorumMove.HOLLOW_TOLL, VelorumMove.HOLLOW_TOLL.followUps(3).get(0).next());
	}

	// ── revision 2026-11-06: Frozen Cruor and liquid dark ────────────

	private static final List<VelorumMove> NEW_MOVES = List.of(VelorumMove.ARRESTED_PULSE, VelorumMove.CRUOR_JAVELINS,
			VelorumMove.VEINS_OF_FROST, VelorumMove.UMBRAL_STEP, VelorumMove.GLOAM_CUTS, VelorumMove.ECLIPSE);

	@Test
	void theSixNewMovesAreAreaAndMeetTheFloorsAfterThePhaseCut() {
		for (VelorumMove move : NEW_MOVES) assertEquals(SaintMove.Kind.AREA, move.kind(), move.toString());
		SaintMoveInputs three = new SaintMoveInputs(3, 0, 0, 1);
		assertEquals(20, VelorumMove.ARRESTED_PULSE.timeline(three).windup(), "22 cut to 18.7, clamped to 20");
		assertEquals(20, VelorumMove.CRUOR_JAVELINS.timeline(three).windup(), "24 cut to 20.4");
		assertEquals(26, VelorumMove.VEINS_OF_FROST.timeline(three).windup(), "30 cut to 25.5");
		assertEquals(20, VelorumMove.GLOAM_CUTS.timeline(three).windup(), "22 cut to 18.7, clamped to 20");
		assertEquals(34, VelorumMove.ECLIPSE.timeline(three).windup(), "40 cut to 34");
		assertFalse(VelorumMove.UMBRAL_STEP.inPhase(3), "the Shadow Step is not picked in phase 3");
		assertEquals(28, VelorumMove.CRUOR_JAVELINS.timeline(three).active(), "five shafts fly longer");
		assertEquals(20, VelorumMove.CRUOR_JAVELINS.timeline(SaintMoveInputs.NONE).active());
		assertEquals(40, VelorumMove.ECLIPSE.authored().windup());
		assertEquals(160, VelorumMove.ECLIPSE.authored().active());
		assertEquals(30, VelorumMove.ECLIPSE.authored().recovery());
	}

	@Test
	void everyNewFootprintButTheEclipsesShowsBeforeItsCommit() {
		for (VelorumMove move : NEW_MOVES) {
			BossFootprint shape = move.footprint(SaintMoveInputs.NONE);
			if (move == VelorumMove.ECLIPSE) {
				assertTrue(shape instanceof BossFootprint.None, "the Eclipse's reach lives in its owned hazards");
				continue;
			}
			assertFalse(shape instanceof BossFootprint.None, move.toString());
			// the reveal floor: an AREA footprint shows at least 20 ticks before its commit
			assertTrue(move.timeline(SaintMoveInputs.NONE).windup() >= SaintMoves.AREA_FLOOR, move.toString());
		}
		assertTrue(VelorumMove.ECLIPSE.inPhase(3) && !VelorumMove.ECLIPSE.inPhase(2) && !VelorumMove.ECLIPSE.picked());
		assertTrue(VelorumMove.ARRESTED_PULSE.inPhase(1) && VelorumMove.ARRESTED_PULSE.inPhase(3));
		assertTrue(VelorumMove.UMBRAL_STEP.inPhase(2) && !VelorumMove.UMBRAL_STEP.inPhase(1));
		for (VelorumMove move : List.of(VelorumMove.CRUOR_JAVELINS, VelorumMove.VEINS_OF_FROST, VelorumMove.GLOAM_CUTS)) {
			assertFalse(move.inPhase(1), move.toString());
			assertTrue(move.inPhase(2) && move.inPhase(3), move.toString());
		}
	}

	@Test
	void javelinsAreThreeShaftsFiveInPhaseThreeLoosedFourApart() {
		assertEquals(3, VelorumMove.javelins(SaintMoveInputs.NONE));
		assertEquals(5, VelorumMove.javelins(new SaintMoveInputs(3, 0, 0, 2)));
		BossFootprint.Union three = (BossFootprint.Union) VelorumMove.CRUOR_JAVELINS.footprint(SaintMoveInputs.NONE);
		BossFootprint.Union five = (BossFootprint.Union) VelorumMove.CRUOR_JAVELINS.footprint(new SaintMoveInputs(3, 0, 0, 1));
		assertEquals(3, three.parts().size());
		assertEquals(5, five.parts().size());
		for (int i = 0; i < 5; i++) {
			assertEquals(i * VelorumMove.JAVELIN_GAP, five.parts().get(i).delay());
			BossFootprint.Tether shaft = (BossFootprint.Tether) five.parts().get(i).shape();
			assertEquals(BossFootprint.End.ANCHOR, shaft.to());
			assertEquals(i, shaft.anchor(), "shaft i runs to anchor i, its cut end");
		}
		// the last of five lands inside the phase-3 active span: loosed at 16, 18 blocks at 1.5 a tick
		assertTrue(4 * VelorumMove.JAVELIN_GAP + VelorumMove.JAVELIN_REACH / VelorumMove.JAVELIN_SPEED
				<= VelorumMove.CRUOR_JAVELINS.timeline(new SaintMoveInputs(3, 0, 0, 1)).active());
		assertTrue(2 * VelorumMove.JAVELIN_GAP + VelorumMove.JAVELIN_REACH / VelorumMove.JAVELIN_SPEED
				<= VelorumMove.CRUOR_JAVELINS.timeline(SaintMoveInputs.NONE).active());
	}

	@Test
	void aShaftStrikesOnlyAtItsHeadAndNeverPastWhereItWasCut() {
		Vec3 from = Vec3.ZERO, cut = new Vec3(0, 0, 6);
		double speed = VelorumMove.JAVELIN_SPEED, half = VelorumMove.JAVELIN_HALF_WIDTH, length = VelorumMove.JAVELIN_LENGTH;
		// two ticks after the loose the point is 3 out: a body there is struck, one it already passed is not
		assertTrue(VelorumMove.head(from, cut, new Vec3(0, 0, 3.0), 2, speed, half, length));
		assertFalse(VelorumMove.head(from, cut, new Vec3(0, 0, 0.0), 2, speed, half, length));
		assertFalse(VelorumMove.head(from, cut, new Vec3(0.6, 0, 3.0), 2, speed, half, length), "0.45 each side");
		assertFalse(VelorumMove.head(from, cut, new Vec3(0, 0, 3.0), -1, speed, half, length), "not loosed yet");
		assertFalse(VelorumMove.head(from, cut, new Vec3(0, 0, -1.0), 0, speed, half, length), "never behind him");
		// a body behind the figure that cut the shaft at 6 is never reached, at any tick
		for (int since = 0; since <= 20; since++) {
			assertFalse(VelorumMove.head(from, cut, new Vec3(0, 0, 8.0), since, speed, half, length), "tick " + since);
		}
		// standing still meets the shaft: it arrives at the spot stood on
		boolean met = false;
		for (int since = 0; since <= 12; since++) met |= VelorumMove.head(from, cut, new Vec3(0, 0, 5.5), since, speed, half, length);
		assertTrue(met);
	}

	@Test
	void veinsRunAlongSeamsWithAtMostTwoTurnsAndMeetInADisc() {
		java.util.Random random = new java.util.Random(5L);
		for (int i = 0; i < 200; i++) {
			double fx = random.nextDouble() * 30.0D - 15.0D, fz = random.nextDouble() * 30.0D - 15.0D;
			double tx = random.nextDouble() * 10.0D - 5.0D, tz = random.nextDouble() * 10.0D - 5.0D;
			double[] path = VelorumVisualRules.veinPath(fx, fz, tx, tz, i);
			assertTrue(VelorumVisualRules.turns(path) <= 2, "vein " + i);
			assertEquals(tx, path[path.length - 2], 1.0E-9D);
			assertEquals(tz, path[path.length - 1], 1.0E-9D);
		}
		BossFootprint.Union veins = (BossFootprint.Union) VelorumMove.VEINS_OF_FROST.footprint(SaintMoveInputs.NONE);
		// the meeting point first, then a vein out to its figure along an x seam and a z seam
		FootprintFrame frame = FRAME.withPoints(List.of(new Vec3(0, 0, 10), new Vec3(4, 0, 10), new Vec3(4, 0, 16)));
		assertTrue(veins.hits(frame, new Vec3(1.2, 0, 10), true, 0), "the meeting disc of 2.0");
		assertTrue(veins.hits(frame, new Vec3(4.3, 0, 14), true, 0), "on a vein");
		assertFalse(veins.hits(frame, new Vec3(5.2, 0, 14), true, 0), "off it");
		assertFalse(veins.hits(frame, new Vec3(4.3, 1.8, 14), false, 0), "the veins' spines stand 1.6");
		BossFootprint.Discs disc = (BossFootprint.Discs) veins.parts().get(1).shape();
		assertTrue(disc.landed(6, 0) && !disc.landed(6, 1), "only the meeting point lands");
		assertFalse(veins.hits(frame, new Vec3(4.0, 0, 17.8), true, 0), "a corner is not a disc");
	}

	@Test
	void theLowCutsClearAJumperAndTheHighCutClearsAStoopingBody() {
		// a jump lifts the feet over 0.9: the low cuts pass under
		assertTrue(VelorumMove.cutStrikes(false, 0.0D, 1.8D));
		assertFalse(VelorumMove.cutStrikes(false, 1.0D, 1.8D));
		// the high cut: a sneaking body (1.5) stoops under it, a standing one (1.8) is struck, and so is a jumper
		assertFalse(VelorumMove.cutStrikes(true, 0.0D, 1.5D));
		assertTrue(VelorumMove.cutStrikes(true, 0.0D, 1.8D));
		assertTrue(VelorumMove.cutStrikes(true, 1.0D, 1.8D));
		BossFootprint.Union cuts = (BossFootprint.Union) VelorumMove.GLOAM_CUTS.footprint(SaintMoveInputs.NONE);
		assertEquals(List.of(0, 6, 12), cuts.parts().stream().map(BossFootprint.Union.Part::delay).toList());
		BossFootprint.Spokes first = (BossFootprint.Spokes) cuts.parts().get(0).shape();
		BossFootprint.Spokes second = (BossFootprint.Spokes) cuts.parts().get(1).shape();
		BossFootprint.Spokes high = (BossFootprint.Spokes) cuts.parts().get(2).shape();
		assertEquals(-15.0D, first.angle(0), 1.0E-9D);
		assertEquals(15.0D, second.angle(0), 1.0E-9D);
		assertEquals(BossFootprint.Height.JUMPABLE, first.height());
		assertEquals(0.0D, high.angle(0), 1.0E-9D);
		assertEquals(3.2D, 2 * high.halfWidth(), 1.0E-9D, "a 3.2-wide front to sidestep");
		// the last cut flies until +24.5, inside the 26-tick active span
		assertTrue(12 + VelorumMove.CUT_LENGTH / VelorumMove.CUT_SPEED <= VelorumMove.GLOAM_CUTS.authored().active());
	}

	@Test
	void theShadowStepsArrivalRingIsJumpableAndHeIsUntouchableUnder() {
		BossFootprint.Union arrival = (BossFootprint.Union) VelorumMove.UMBRAL_STEP.footprint(SaintMoveInputs.NONE);
		BossFootprint.Ring ring = (BossFootprint.Ring) arrival.parts().get(0).shape();
		assertEquals(BossFootprint.Height.JUMPABLE, ring.height());
		assertEquals(1, arrival.parts().get(0).delay(), "the ring at +1");
		assertEquals(3.5D, ring.radius(6, 0), 1.0E-9D);
		// three ticks into the ring's run its band stands near 2.05
		assertTrue(arrival.hits(FRAME, new Vec3(0, 0, 2.05), true, 4));
		assertFalse(arrival.hits(FRAME, new Vec3(0, 1.2, 2.05), false, 4), "jumped");
		int commit = VelorumMove.UMBRAL_STEP.timeline(new SaintMoveInputs(2, 0, 0, 1)).commitTick();
		assertFalse(VelorumLocomotionRules.under(13, commit));
		assertTrue(VelorumLocomotionRules.under(14, commit) && VelorumLocomotionRules.under(commit + 4, commit));
		assertFalse(VelorumLocomotionRules.under(commit + 5, commit));
	}

	@Test
	void theThreeNewStringsMixTheTendencies() {
		// E: the Shadow Step into the Release from where he rose, else Gloam Cuts
		assertEquals(VelorumMove.CRUOR_RELEASE, VelorumMove.UMBRAL_STEP.followUps(2).get(0).next());
		assertEquals(VelorumMove.GLOAM_CUTS, VelorumMove.UMBRAL_STEP.followUps(2).get(1).next());
		// F: Gloam Cuts into Cruor Javelins
		assertEquals(VelorumMove.CRUOR_JAVELINS, VelorumMove.GLOAM_CUTS.followUps(2).get(0).next());
		// G: Arrested Pulse into Gloam Cuts, phase 3 only
		assertTrue(VelorumMove.ARRESTED_PULSE.followUps(2).isEmpty());
		assertEquals(VelorumMove.GLOAM_CUTS, VelorumMove.ARRESTED_PULSE.followUps(3).get(0).next());
		assertEquals(8, VelorumMove.ARRESTED_PULSE.followUps(3).get(0).linkGap());
		assertTrue(VelorumMove.CRUOR_JAVELINS.followUps(3).isEmpty() && VelorumMove.VEINS_OF_FROST.followUps(3).isEmpty());
	}
}
