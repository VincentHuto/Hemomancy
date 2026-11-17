package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The lash: the orb's charge, the throw and its arc, the live rope, the fade and its revival, and damage by speed. */
class LashRulesTest {
	private static final Vec3 HAND = new Vec3(0, 1.2, 0);
	private static final double LENGTH = 7.5D;
	private static final LashTuning T = LashTuning.DEFAULT;
	/** Twenty ticks to a full charge, which multiplies the first crack by two. */
	private static final LashProfile PROFILE = new LashProfile(4.5F, 20.0D, 12, 0, 10.0D, 20, 2.0F);

	/** One tick with the view at {@code look} and the whip aim at {@code aim} (yaw, level pitch). */
	private static LashRules.Step step(LashRules.State state, double look, double aim, long tick) {
		return LashRules.step(state, HAND, (float) look, 0.0F, (float) aim, 0.0F, LENGTH, tick, LashRules.Clipper.NONE,
				1.0D, PROFILE);
	}

	/** Holds still for {@code charged} ticks, flicks the look to the right, and runs the throw out. Returns the next tick. */
	private static long thrown(LashRules.State state, int charged) {
		long tick = 0;
		for (; tick <= charged; tick++) step(state, 0, 0, tick);
		step(state, T.flickTurn() + 2, 0, tick++);
		for (int i = 0; i < T.throwTicks(); i++) step(state, T.flickTurn() + 2, 0, tick++);
		return tick;
	}

	@Test
	void aClickOnABlockOrOnSomethingNotAliveIsVanillas() {
		assertTrue(LashRules.clickStaysVanilla(true, false, false, true, false), "a block mines");
		assertTrue(LashRules.clickStaysVanilla(false, true, false, true, false), "a boat or an item frame is hit as usual");
		assertFalse(LashRules.clickStaysVanilla(false, false, false, true, false), "open air taps");
		assertFalse(LashRules.clickStaysVanilla(false, true, true, true, false), "a creature is tapped");
	}

	@Test
	void anOffHandLashLeavesACreatureToTheMainHandsWeapon() {
		assertTrue(LashRules.clickStaysVanilla(false, true, true, false, false), "the sword in the main hand strikes");
		assertFalse(LashRules.clickStaysVanilla(false, true, true, false, true), "an empty main hand lets the lash tap");
		assertFalse(LashRules.clickStaysVanilla(false, false, false, false, false), "open air still taps from the off hand");
	}

	@Test
	void nothingIsOutWhileTheOrbGathers() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 10; tick++) {
			LashRules.Step held = step(state, 0, 0, tick);
			assertEquals(LashRules.Phase.CHARGING, held.phase());
			assertFalse(state.seeded(), "a strand is out while charging");
			assertTrue(LashRules.sweeps(state).isEmpty(), "a charging lash sweeps");
		}
	}

	@Test
	void theChargeClimbsToFullOverTheProfileTicksAndHolds() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 10; tick++) step(state, 0, 0, tick);
		assertEquals(0.5D, state.charge(), 1.0E-9D);
		for (int tick = 10; tick < 60; tick++) step(state, 0, 0, tick);
		assertEquals(1.0D, state.charge(), 1.0E-9D);
	}

	@Test
	void aSlowTurnNeverThrows() {
		LashRules.State state = new LashRules.State();
		double yaw = 0;
		for (int tick = 0; tick < 40; tick++) {
			assertEquals(LashRules.Phase.CHARGING, step(state, yaw += 5, yaw, tick).phase(), "a slow turn threw the lash");
		}
	}

	@Test
	void aFlickThrowsFromTheSideOppositeTheFlick() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 0, 0, tick);
		LashRules.Step flick = step(state, 8, 0, 5);
		assertTrue(flick.threw(), "a flick did not throw");
		assertEquals(LashRules.Phase.THROWING, flick.phase());
		assertTrue(state.seeded(), "the throw laid no strand out");
		assertEquals(-T.arcDegrees() / 2, state.aimYaw(), 1.0E-4D, "the arc does not start half its width back from the old aim");
		Vec3 start = HAND.add(LashRules.direction((float) (-T.arcDegrees() / 2), 0.0F).scale(LENGTH));
		assertTrue(state.tip().distanceTo(start) < 1.0E-6D, "the strand is not laid along the arc's start");
	}

	@Test
	void theThrowCrossesItsArcAndCracksAtTheEndWithTheCharge() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 10; tick++) step(state, 0, 0, tick);
		step(state, 8, 0, 10);
		for (int i = 1; i < T.throwTicks(); i++) {
			LashRules.Step mid = step(state, 8, 0, 10 + i);
			assertEquals(LashRules.Phase.THROWING, mid.phase());
			assertFalse(mid.crack(), "the throw cracked before its arc ran out");
		}
		LashRules.Step end = step(state, 8, 0, 10 + T.throwTicks());
		assertTrue(end.crack(), "the throw did not crack at the end of its arc");
		assertEquals(0.5D, end.crackCharge(), 1.0E-9D, "the crack did not carry the charge");
		assertEquals(LashRules.Phase.LIVE, end.phase());
		assertEquals(T.arcDegrees() / 2, state.aimYaw(), 1.0E-4D, "the whip aim is not left at the arc's end");
		assertEquals(0.0D, state.charge(), 1.0E-9D, "the charge was not spent");
	}

	@Test
	void aThrowThatMeetsSomethingCracksThereAndGoesLive() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 20; tick++) step(state, 0, 0, tick);
		step(state, 8, 0, 20);
		step(state, 8, 0, 21);
		assertEquals(1.0D, LashRules.struck(state, 21), 1.0E-9D);
		assertEquals(LashRules.Phase.LIVE, state.phase());
		assertEquals(-1.0D, LashRules.struck(state, 22), 1.0E-9D, "a live lash was struck as a throw");
	}

	@Test
	void onlyTheThrowCarriesCharge() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 20);
		double aim = state.aimYaw();
		boolean cracked = false;
		for (int i = 0; i < 60; i++) {
			// Six ticks one way, six the other: full strokes reversed at speed.
			aim += (i / 6) % 2 == 0 ? 8 : -8;
			LashRules.Step live = step(state, 8, aim, tick++);
			if (live.crack()) {
				cracked = true;
				assertEquals(0.0D, live.crackCharge(), 1.0E-9D, "a stroke's crack carried charge");
			}
		}
		assertTrue(cracked, "hard reversals never cracked");
	}

	@Test
	void aFlickOfTenDegreesATickReachesFullSpeed() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		for (int i = 0; i < 20; i++) step(state, 8, aim, tick++);
		double fastest = 0;
		for (int i = 0; i < 3; i++) fastest = Math.max(fastest, step(state, 8, aim += 10, tick++).tipSpeed());
		for (int i = 0; i < 4; i++) fastest = Math.max(fastest, step(state, 8, aim, tick++).tipSpeed());
		assertTrue(fastest >= T.fullSpeed(), "a thirty degree wrist flick only reached " + fastest);
	}

	@Test
	void theTipOvershootsTheAimAndSettles() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double from = state.aimYaw();
		for (int i = 0; i < 30; i++) step(state, 8, from, tick++);
		double aim = from + 40;
		Vec3 line = HAND.add(LashRules.direction((float) aim, 0.0F).scale(LENGTH));
		Vec3 across = LashRules.direction((float) (aim + 90), 0.0F);
		double furthest = Double.NEGATIVE_INFINITY;
		// Keep the lash live with a tiny wobble so it does not fade while it rings.
		for (int i = 0; i < 40; i++) {
			step(state, 8, aim + (i % 2 == 0 ? 2 : 0), tick++);
			furthest = Math.max(furthest, state.tip().subtract(line).dot(across));
		}
		assertTrue(furthest > 0.05D, "the tip never passed the aim line: a rope with no momentum");
		assertTrue(state.tip().distanceTo(line) < 1.0D, "the tip never settled near the aim: " + state.tip());
	}

	@Test
	void linksNeverStretchPastTheClamp() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double rest = LENGTH / LashRules.NODES;
		for (int i = 0; i < 30; i++) {
			step(state, 8, i % 2 == 0 ? 0 : 170, tick++);
			Vec3 previous = HAND;
			for (int n = 0; n < LashRules.NODES; n++) {
				assertTrue(state.node(n).distanceTo(previous) <= rest * LashRules.STRETCH + 1.0E-9D,
						"link " + n + " stretched to " + state.node(n).distanceTo(previous));
				previous = state.node(n);
			}
		}
	}

	@Test
	void theRopeStaysBoundedUnderAThousandRandomAims() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		Random random = new Random(12L);
		for (int i = 0; i < 1000; i++) {
			LashRules.step(state, HAND, 8.0F, 0.0F, random.nextFloat() * 360.0F - 180.0F, random.nextFloat() * 180.0F - 90.0F,
					LENGTH, tick++, LashRules.Clipper.NONE, 1.0D, PROFILE);
			if (!state.seeded()) continue;
			for (int n = 0; n < LashRules.NODES; n++) {
				Vec3 node = state.node(n);
				assertTrue(Double.isFinite(node.x) && Double.isFinite(node.y) && Double.isFinite(node.z), "node " + n + " blew up");
				assertTrue(node.distanceTo(HAND) <= LENGTH * LashRules.STRETCH * 1.05D, "node " + n + " left the lash's reach");
			}
		}
	}

	@Test
	void twoStatesFedTheSameInputsMatchExactly() {
		LashRules.State a = new LashRules.State(), b = new LashRules.State();
		Random random = new Random(7L);
		float look = 0, aim = 0;
		for (int tick = 0; tick < 300; tick++) {
			look += random.nextFloat() * 16.0F - 8.0F;
			aim += random.nextFloat() * 30.0F - 15.0F;
			LashRules.Step first = LashRules.step(a, HAND, look, 0.0F, aim, 0.0F, LENGTH, tick, LashRules.Clipper.NONE, 1.0D, PROFILE);
			LashRules.Step second = LashRules.step(b, HAND, look, 0.0F, aim, 0.0F, LENGTH, tick, LashRules.Clipper.NONE, 1.0D, PROFILE);
			assertEquals(first, second, "the two copies parted on tick " + tick);
			if (a.seeded()) assertEquals(a.tip(), b.tip());
		}
	}

	@Test
	void aBlockStopsTheNodesAndIsReported() {
		LashRules.State state = new LashRules.State();
		LashRules.Clipper wall = (from, to) -> to.z > 3.0D ? new Vec3(to.x, to.y, 3.0D) : to;
		long tick = 0;
		for (; tick < 5; tick++) LashRules.step(state, HAND, 60.0F, 0.0F, 0.0F, 0.0F, LENGTH, tick, wall, 1.0D, PROFILE);
		// Flick toward yaw 0: the arc sweeps into the wall at z = 3.
		boolean blocked = false;
		for (int i = 0; i <= T.throwTicks(); i++) {
			blocked |= LashRules.step(state, HAND, 50.0F, 0.0F, 0.0F, 0.0F, LENGTH, tick++, wall, 1.0D, PROFILE).tipBlocked();
		}
		assertTrue(blocked, "the wall never stopped the tip");
		for (int n = 0; n < LashRules.NODES; n++) assertTrue(state.node(n).z <= 3.0D + 1.0E-9D, "node " + n + " passed the wall");
	}

	@Test
	void aStillWhipFadesAndReturnsToTheOrb() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		LashRules.Step still = null;
		for (int i = 0; i < T.restTicksToFade(); i++) still = step(state, 8, aim, tick++);
		assertEquals(LashRules.Phase.FADING, still.phase(), "a lash at rest did not start to fade");
		double before = state.extent();
		step(state, 8, aim, tick++);
		assertTrue(state.extent() < before, "a fading lash did not shorten");
		for (int i = 0; i < T.fadeTicks() + 2; i++) still = step(state, 8, aim, tick++);
		assertEquals(LashRules.Phase.CHARGING, still.phase(), "the fade never finished");
		assertFalse(state.seeded(), "the strand outlived its fade");
		assertEquals(0.0D, state.charge(), 0.11D, "the orb did not start again from empty");
	}

	@Test
	void aFlickDuringTheFadeRevivesItAndRegrows() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		for (int i = 0; i < T.restTicksToFade() + T.fadeTicks() / 2; i++) step(state, 8, aim, tick++);
		assertEquals(LashRules.Phase.FADING, state.phase());
		double faded = state.extent();
		assertTrue(faded < 0.7D && faded > 0.2D, "half a fade left " + faded);
		LashRules.Step revived = step(state, 8, aim += 10, tick++);
		assertEquals(LashRules.Phase.LIVE, revived.phase(), "a flick did not revive the fading lash");
		assertFalse(revived.threw(), "a revival threw a fresh lash");
		for (int i = 0; i < T.regrowTicks(); i++) step(state, 8, aim += 10, tick++);
		assertEquals(1.0D, state.extent(), 1.0E-9D, "a revived lash did not regrow");
	}

	@Test
	void theAttackButtonLetsTheChargeGoDownTheLook() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 30, 30, tick);
		assertFalse(LashRules.maySnap(state, 5), "an orb a quarter gathered was let go");
		for (int tick = 5; tick < 10; tick++) step(state, 30, 30, tick);
		assertEquals(T.releaseMinCharge(), state.charge(), 1.0E-9D);
		assertTrue(LashRules.maySnap(state, 10));
		assertEquals(0.5D, LashRules.release(state, 30.0F, 0.0F, 10), 1.0E-9D);
		assertEquals(LashRules.Phase.LIVE, state.phase());
		step(state, 30, 30, 11);
		Vec3 line = HAND.add(LashRules.direction(30.0F, 0.0F).scale(LENGTH));
		assertTrue(state.tip().distanceTo(line) < 1.0E-6D, "the released lash is not laid along the look");
		assertFalse(LashRules.maySnap(state, 10 + LashRules.SNAP_COOLDOWN - 1));
		assertTrue(LashRules.maySnap(state, 10 + LashRules.SNAP_COOLDOWN));
	}

	@Test
	void theAttackButtonBringsALiveWhipBackToTheLook() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		for (int i = 0; i < T.restTicksToFade() + 5; i++) step(state, 8, aim, tick++);
		assertEquals(0.0D, LashRules.release(state, 8.0F, 0.0F, tick), 1.0E-9D, "a live lash's snap carried charge");
		assertEquals(LashRules.Phase.LIVE, state.phase());
		assertEquals(8.0F, state.aimYaw(), 1.0E-6F);
		assertEquals(1.0D, state.extent(), 1.0E-9D);
	}

	@Test
	void aThrowCannotBeSnapped() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 0, 0, tick);
		step(state, 8, 0, 5);
		assertFalse(LashRules.maySnap(state, 100), "a lash mid-throw snapped");
	}

	@Test
	void damageRunsFromTheFloorToTheCapBySpeed() {
		assertEquals(0.0F, LashRules.sweepDamage(T.slackSpeed() * 0.5D, false), "a slack lash hurts");
		assertEquals(LashRules.FLOOR_DAMAGE, LashRules.sweepDamage(T.slackSpeed(), false), 1.0E-6F);
		assertEquals(LashRules.CAP_DAMAGE, LashRules.sweepDamage(T.fullSpeed(), false), 1.0E-6F);
		assertEquals(LashRules.CAP_DAMAGE, LashRules.sweepDamage(T.fullSpeed() * 5.0D, false), 1.0E-6F,
				"more speed than the cap buys nothing");
		assertEquals(LashRules.FLOOR_DAMAGE, LashRules.sweepDamage(T.fullSpeed(), true), 1.0E-6F,
				"a tired stroke falls to the floor");
	}

	@Test
	void crackDamageAtFullChargeIsNineTimesTheMultiplier() {
		assertEquals(9.0F, LashRules.crackDamage(0.0D, PROFILE), 1.0E-6F);
		assertEquals(13.5F, LashRules.crackDamage(0.5D, PROFILE), 1.0E-6F);
		assertEquals(18.0F, LashRules.crackDamage(1.0D, PROFILE), 1.0E-6F);
		assertEquals(18.0F, LashRules.crackDamage(3.0D, PROFILE), 1.0E-6F, "charge past full bought more");
	}

	@Test
	void aConstantSpinTiresAndNeverCracks() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		for (int i = 0; i < 20; i++) step(state, 8, aim, tick++);
		tick += LashRules.CRACK_COOLDOWN;
		for (int i = 0; i < 80; i++) {
			LashRules.Step spin = step(state, 8, aim += 20, tick++);
			if (state.strokeTurn() > LashRules.FRESH_TURN) {
				assertTrue(spin.tired(), "a spin past a quarter turn is still fresh");
				assertTrue(LashRules.sweepDamage(spin.tipSpeed(), spin.tired()) <= LashRules.FLOOR_DAMAGE);
			}
			assertFalse(spin.crack(), "a spin in one direction cracked");
		}
	}

	@Test
	void strokeCracksComeOncePerCooldown() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		long last = Long.MIN_VALUE / 2;
		int cracks = 0;
		for (int i = 0; i < 200; i++) {
			aim += (i / 6) % 2 == 0 ? 8 : -8;
			if (step(state, 8, aim, tick).crack()) {
				assertTrue(tick - last >= LashRules.CRACK_COOLDOWN, "two cracks " + (tick - last) + " ticks apart");
				last = tick;
				cracks++;
			}
			tick++;
		}
		assertTrue(cracks >= 3, "two hundred ticks of hard strokes cracked " + cracks + " times");
	}

	@Test
	void aLazyWaveDoesNotCrack() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		double aim = state.aimYaw();
		for (int i = 0; i < 20; i++) step(state, 8, aim, tick++);
		tick += LashRules.CRACK_COOLDOWN;
		for (int i = 0; i < 60; i++) {
			aim += (i / 4) % 2 == 0 ? 2 : -2;
			assertFalse(step(state, 8, aim, tick++).crack(), "a lazy wave cracked");
		}
	}

	@Test
	void sweepsCoverTheOuterHalfOfTheChain() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		step(state, 8, state.aimYaw() + 20, tick);
		List<Vec3[]> sweeps = LashRules.sweeps(state);
		int outer = LashRules.NODES - LashRules.NODES / 2;
		assertEquals(outer * 2 - 1, sweeps.size(), "each outer node and each gap between them sweeps once");
		for (Vec3[] sweep : sweeps) assertTrue(sweep[0].distanceTo(HAND) > LENGTH * 0.4D, "a sweep came from the inner half");
	}

	@Test
	void theLashOutreachesTheVerbByTwoBlocks() {
		assertEquals(7.5D, LashRules.length(5.5D), 1.0E-9D);
		assertEquals(7.5D, StrandForm.LASH.length(5.5D), 1.0E-9D);
	}

	/** Taut Line: a tauter lash's tip is drawn to the aim harder, and never by more than the whole way. */
	@Test
	void aTautLashTipFollowsCloser() {
		assertTrue(LashRules.pull(LashRules.NODES - 1, StrandSkills.taut(2)) > LashRules.pull(LashRules.NODES - 1, 1.0D));
		for (int i = 0; i < LashRules.NODES; i++) {
			assertTrue(LashRules.pull(i, StrandSkills.taut(2)) <= 1.0D, "node " + i + " overshoots its place");
			assertTrue(LashRules.pull(i, 100.0D) <= 1.0D);
		}
	}
	/** The server's copy reads no flicks: it throws only when the caster's client tells it to. */
	@Test
	void aCopyThatIsToldItsFlicksNeverThrowsOnItsOwn() {
		LashRules.State state = new LashRules.State(false);
		double yaw = 0;
		for (int tick = 0; tick < 20; tick++) {
			assertEquals(LashRules.Phase.CHARGING, step(state, yaw += 25, yaw, tick).phase(), "the told copy threw on a look");
		}
		assertTrue(state.charge() > 0.9D, "a turning look stopped the charge");
	}

	@Test
	void aToldFlickThrowsRoundTheGivenLook() {
		LashRules.State state = new LashRules.State(false);
		for (int tick = 0; tick < 10; tick++) step(state, 0, 0, tick);
		assertFalse(LashRules.flick(state, HAND, 10.0F, 0.0F, 0.0D, 0.0D, LENGTH, LashRules.Clipper.NONE), "a turn with no direction threw");
		assertFalse(LashRules.flick(state, HAND, 10.0F, 0.0F, Double.NaN, 1.0D, LENGTH, LashRules.Clipper.NONE), "a turn that is not a number threw");
		assertTrue(LashRules.flick(state, HAND, 10.0F, 0.0F, -3.0D, 0.0D, LENGTH, LashRules.Clipper.NONE));
		assertEquals(LashRules.Phase.THROWING, state.phase());
		assertEquals(10.0F + T.arcDegrees() / 2, state.aimYaw(), 1.0E-4D, "a flick to the left does not start from the right");
		assertFalse(LashRules.flick(state, HAND, 10.0F, 0.0F, 1.0D, 0.0D, LENGTH, LashRules.Clipper.NONE), "a lash mid-throw was thrown again");
		LashRules.Step end = null;
		for (int i = 1; i <= T.throwTicks(); i++) end = step(state, 0, 0, 10 + i);
		assertTrue(end.crack());
		assertEquals(0.5D, end.crackCharge(), 1.0E-9D);
		assertEquals(10.0F - T.arcDegrees() / 2, state.aimYaw(), 1.0E-4D);
	}

	@Test
	void theLocalCopyRemembersItsThrowForTheServer() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 20, 20, tick);
		step(state, 28, 20, 5);
		assertEquals(20.0F, state.throwCentreYaw(), 1.0E-6F);
		assertEquals(1.0D, state.throwX(), 1.0E-9D);
		assertEquals(0.0D, state.throwY(), 1.0E-9D);
	}

	@Test
	void aCopyTakesTheServersPhase() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 0, 0, tick);
		LashRules.adopt(state, LashRules.Phase.CHARGING, 0.5D, PROFILE);
		assertEquals(0.5D, state.charge(), 1.0E-9D, "a charging copy did not take the charge it was told");
		LashRules.adopt(state, LashRules.Phase.THROWING, 0.0D, PROFILE);
		assertEquals(LashRules.Phase.CHARGING, state.phase(), "a throw was started with no arc");
		LashRules.adopt(state, LashRules.Phase.LIVE, 0.0D, PROFILE);
		assertEquals(LashRules.Phase.LIVE, state.phase());
		step(state, 0, 0, 6);
		assertTrue(state.seeded(), "a lash the server has out was not laid out");
		LashRules.adopt(state, LashRules.Phase.CHARGING, 0.0D, PROFILE);
		assertEquals(LashRules.Phase.CHARGING, state.phase());
		assertFalse(state.seeded(), "a lash the server took back stayed out");
	}
	/** A flick while the last lash is still dissolving throws a fresh one, with nothing gathered behind it. */
	@Test
	void aToldFlickWhileFadingThrowsAfreshWithNoCharge() {
		LashRules.State state = new LashRules.State(false);
		for (int tick = 0; tick < 10; tick++) step(state, 0, 0, tick);
		assertTrue(LashRules.flick(state, HAND, 0.0F, 0.0F, 1.0D, 0.0D, LENGTH, LashRules.Clipper.NONE));
		long tick = 10;
		for (int i = 0; i <= T.throwTicks(); i++) step(state, 0, state.aimYaw(), tick++);
		for (int i = 0; i < T.restTicksToFade() + 4; i++) step(state, 0, state.aimYaw(), tick++);
		assertEquals(LashRules.Phase.FADING, state.phase());
		assertTrue(LashRules.flick(state, HAND, 0.0F, 0.0F, -1.0D, 0.0D, LENGTH, LashRules.Clipper.NONE),
				"a flick during the fade was refused");
		assertEquals(LashRules.Phase.THROWING, state.phase());
		LashRules.Step end = null;
		for (int i = 0; i < T.throwTicks(); i++) end = step(state, 0, 0, tick++);
		assertTrue(end.crack());
		assertEquals(0.0D, end.crackCharge(), 1.0E-9D, "a lash thrown out of a fade carried charge");
	}
	@Test
	void aThrowSweepsItsWholeLengthAndALiveLashItsOuterHalf() {
		LashRules.State state = new LashRules.State();
		for (int tick = 0; tick < 5; tick++) step(state, 0, 0, tick);
		step(state, 8, 0, 5);
		step(state, 8, 0, 6);
		List<Vec3[]> thrown = LashRules.throwSweeps(state);
		assertEquals(LashRules.NODES * 2 - 1, thrown.size());
		assertTrue(thrown.stream().anyMatch(sweep -> sweep[0].distanceTo(HAND) < 1.0D), "the throw does not sweep at the hand");
		assertTrue(LashRules.sweeps(state).size() < thrown.size());
	}

	/** A tick with no word of the aim is not a hand at rest: the stroke is neither rested nor advanced. */
	@Test
	void aTickWithNoAimNeitherRestsNorAdvancesTheStroke() {
		LashRules.State state = new LashRules.State();
		long tick = thrown(state, 5);
		float aim = state.aimYaw();
		for (int i = 0; i < 10; i++) {
			LashRules.step(state, HAND, 8.0F, 0.0F, aim += 12.0F, 0.0F, LENGTH, tick++, LashRules.Clipper.NONE, 1.0D, PROFILE, true);
			double turned = state.strokeTurn();
			LashRules.Step gap = LashRules.step(state, HAND, 8.0F, 0.0F, 999.0F, 0.0F, LENGTH, tick++, LashRules.Clipper.NONE, 1.0D,
					PROFILE, false);
			assertEquals(turned, state.strokeTurn(), 1.0E-9D, "a tick with no aim moved the stroke");
			assertEquals(LashRules.Phase.LIVE, gap.phase());
			assertFalse(gap.resting());
			assertFalse(gap.crack(), "a tick with no aim cracked");
			assertEquals(aim, state.aimYaw(), 1.0E-6F, "a tick with no aim took the stale aim");
		}
		assertTrue(state.strokeTurn() > LashRules.FRESH_TURN, "the stroke never tired");
	}
}
