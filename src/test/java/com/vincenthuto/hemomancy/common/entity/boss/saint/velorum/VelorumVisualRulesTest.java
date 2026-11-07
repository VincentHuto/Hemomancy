package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.manipulation.congeatio.CruorJavelinRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Velorum's presentation timelines for the Frozen Cruor and liquid dark revision (design R.2, R.3, R.7, R.11, R.15):
 * cue packing, the lantern's light and the pools as the server tests them, the javelins' clocks agreeing with
 * {@link CruorJavelinRules} and the move's own numbers, the pulse's beats, the Shadow Step's upright sink and jumpable
 * ring, the cuts, the Eclipse's light and grid, stains and veins along the floor's seams.
 */
class VelorumVisualRulesTest {
	@Test
	void aCueCarriesItsBeatAndTheEntityItIsAbout() {
		for (int kind : new int[]{VelorumEffects.TOLL, VelorumEffects.BLACK_ICE, VelorumEffects.STAIN, VelorumEffects.SHAPE_REACH}) {
			assertTrue(kind <= VelorumVisualRules.KIND_MASK, "every beat fits the packed bits");
			assertEquals(kind, VelorumVisualRules.kind(kind), "an old unpacked count still reads as its beat");
			assertEquals(-1, VelorumVisualRules.entity(kind), "and is about no entity");
			int packed = VelorumVisualRules.pack(kind, 4071);
			assertEquals(kind, VelorumVisualRules.kind(packed));
			assertEquals(4071, VelorumVisualRules.entity(packed));
			assertEquals(0, VelorumVisualRules.entity(VelorumVisualRules.pack(kind, 0)), "entity 0 is an entity");
		}
	}

	@Test
	void theLanternsLightIsTheServersEdge() {
		assertEquals(6.0F, VelorumVisualRules.lanternRadius(1, 0, -1, -1, 0, -1));
		assertEquals(5.0F, VelorumVisualRules.lanternRadius(2, 0, -1, -1, 0, -1));
		assertEquals(4.0F, VelorumVisualRules.lanternRadius(3, 0, -1, -1, 0, -1));
		assertEquals(3.0F, VelorumVisualRules.lanternRadius(1, 1.0F, -1, -1, 0, -1), 1.0E-4F, "the Long Night's commit");
		assertEquals(1.0F, VelorumVisualRules.lanternRadius(2, 0, 10.0F, -1, 0, -1), 1.0E-4F, "the Gutter within its first ten ticks");
		assertEquals(0.0F, VelorumVisualRules.lanternRadius(3, 0, -1, -1, 1.0F, -1), 1.0E-4F, "the Eclipse");
		assertEquals(8.0F, VelorumVisualRules.lanternRadius(3, 0, -1, -1, 0, 5.0F), 1.0E-4F, "relit");
		for (int phase = 1; phase <= 3; phase++) {
			for (float night = 0; night <= 1.0F; night += 0.25F) {
				assertEquals(VelorumDarkRules.lanternRadius(phase, night, -1, -1, 0, -1), VelorumVisualRules.lanternRadius(phase, night, -1, -1, 0, -1));
			}
		}
		assertEquals(1.0F, VelorumVisualRules.lit(0.0D, 4.0D), 1.0E-4F);
		assertEquals(0.0F, VelorumVisualRules.lit(9.0D, 4.0D), 1.0E-4F);
		assertTrue(VelorumVisualRules.lit(4.0D, 4.0D) > 0.3F && VelorumVisualRules.lit(4.0D, 4.0D) < 0.7F, "torn across its edge, not cut");
		assertEquals(0.0F, VelorumVisualRules.lit(0.0D, 0.0D), "in the Eclipse nothing is lit");
	}

	@Test
	void poolsGrowLikeTheServersAndTheirEdgesAreTornNotHard() {
		assertEquals(0.0D, VelorumVisualRules.poolRadius(0.0F, 4.5D));
		assertEquals(4.5D, VelorumVisualRules.poolRadius(30.0F, 4.5D), 1.0E-6D, "the Long Night's pools grow over 30 ticks");
		assertEquals(2.5D, VelorumVisualRules.poolRadius(8.0F, 2.5D), 1.0E-6D, "the Step's over 8");
		assertEquals(1.5D, VelorumVisualRules.poolRadius(6.0F, 1.5D), 1.0E-6D, "a cut's over 6");
		assertEquals(VelorumDarkRules.ownRadius(4.5D, 12.0D), VelorumVisualRules.poolRadius(12.0F, 4.5D), 1.0E-9D);
		assertEquals(7.0D, VelorumVisualRules.floodedRadius(4.5D, 40.0F, -1.0F), 1.0E-6D, "the Gutter floods toward 7");
		assertEquals(4.5D, VelorumVisualRules.floodedRadius(4.5D, 40.0F, 40.0F), 1.0E-6D, "easing back over 40");
		double radius = 4.0D;
		assertEquals(1.0F, VelorumVisualRules.poolCover(0.0D, radius));
		assertEquals(0.0F, VelorumVisualRules.poolCover(radius, radius));
		int partial = 0;
		for (double d = radius - 0.9D; d < radius; d += 0.1D) {
			float cover = VelorumVisualRules.poolCover(d, radius);
			if (cover > 0.05F && cover < 0.95F) partial++;
		}
		assertTrue(partial >= 5, "the rim falls off over most of a block, so the shader tears it rather than drawing a ring");
		assertTrue(VelorumVisualRules.rimTatters(4.5D) >= 20, "a rim of broad tatters");
		double[] young = VelorumVisualRules.tatter(0.1D), old = VelorumVisualRules.tatter(0.9D);
		assertTrue(old[0] > young[0] && old[1] > young[1], "a tatter lifts and tears away as it ages");
	}

	@Test
	void javelinClocksAgreeWithTheCruorJavelinAndTheMove() {
		assertEquals(VelorumMove.JAVELIN_SPEED, VelorumVisualRules.SHAFT_SPEED);
		assertEquals(VelorumMove.JAVELIN_LENGTH, VelorumVisualRules.SHAFT_LENGTH);
		assertEquals(VelorumMove.JAVELIN_GAP, VelorumVisualRules.LOOSE_GAP);
		assertEquals(VelorumMove.JAVELIN_FORM, VelorumVisualRules.FORM_START);
		assertEquals(VelorumMove.JAVELIN_FORMED, VelorumVisualRules.FORM_END);
		for (int count : new int[]{3, 5}) {
			for (int i = 0; i < count; i++) {
				assertEquals(0.0F, VelorumVisualRules.formed(VelorumVisualRules.FORM_START - 0.5F, i, count), 1.0E-4F, "nothing before tick 8");
				assertEquals(1.0F, VelorumVisualRules.formed(VelorumVisualRules.FORM_END + 0.5F, i, count), 1.0E-3F, "every shaft formed by 22");
				assertEquals(CruorJavelinRules.formation(0.0D), VelorumVisualRules.formed(-5.0F, i, count), 1.0E-6D);
			}
			assertTrue(VelorumVisualRules.formed(14.0F, 0, count) >= VelorumVisualRules.formed(14.0F, count - 1, count), "one after another");
		}
		assertEquals(-1.0D, VelorumVisualRules.flown(-0.5F, 18.0D));
		assertEquals(18.0D, VelorumVisualRules.flown(30.0F, 18.0D), 1.0E-6D);
		assertEquals(12.0F, VelorumVisualRules.lands(18.0D), 1.0E-4F, "18 blocks at 1.5 a tick");
		assertEquals(0.0F, VelorumVisualRules.shatter(39.0F, false), "a lodged shaft stands 40 ticks");
		assertEquals(1.0F, VelorumVisualRules.shatter(50.0F, false), 1.0E-4F, "then shatters over 10");
		assertEquals(1.0F, VelorumVisualRules.shatter(10.0F, true), 1.0E-4F, "on rime it shatters at once");
	}

	@Test
	void thePulseBeatsThreeTimesWithATellAndTheRingTightens() {
		assertEquals(0, VelorumVisualRules.beat(0.0F));
		assertEquals(1, VelorumVisualRules.beat(45.0F));
		assertEquals(2, VelorumVisualRules.beat(80.0F));
		assertEquals(-1, VelorumVisualRules.beat(92.0F));
		for (int beat : VelorumVisualRules.BEATS) {
			assertEquals(0.0F, VelorumVisualRules.tell(beat - VelorumVisualRules.BEAT_TELL - 1.0F), 1.0E-4F, "the tell shows 12 ticks before");
			assertEquals(1.0F, VelorumVisualRules.tell(beat), 1.0E-4F);
		}
		assertTrue(VelorumVisualRules.markRing(5.0F) > VelorumVisualRules.markRing(35.0F));
		assertTrue(VelorumVisualRules.markRing(35.0F) > VelorumVisualRules.markRing(65.0F));
		assertTrue(VelorumVisualRules.markRing(0.0F) <= 1.5D, "inside the beat's disc");
		assertEquals(0.0F, VelorumVisualRules.spines(0.0F));
		assertEquals(1.0F, VelorumVisualRules.spines(4.0F), 1.0E-4F, "punched out by 2.5 and held");
		assertEquals(0.0F, VelorumVisualRules.spines(VelorumVisualRules.BURST), "crumbled by the burst's end");
		assertEquals(VelorumPulseRules.BEATS.length, VelorumVisualRules.BEATS.length);
		for (int i = 0; i < VelorumVisualRules.BEATS.length; i++) assertEquals(VelorumPulseRules.BEATS[i], VelorumVisualRules.BEATS[i]);
	}

	@Test
	void theStepSinksUprightAndRisesWithAJumpableRing() {
		double last = -1.0D;
		for (float t = 0; t <= 30.0F; t += 1.0F) {
			double sunk = VelorumVisualRules.sunk(t);
			assertTrue(sunk >= last, "he only goes down");
			last = sunk;
		}
		assertEquals(0.0D, VelorumVisualRules.sunk(VelorumVisualRules.SINK_START), 1.0E-9D);
		assertEquals(VelorumVisualRules.SINK_DEPTH, VelorumVisualRules.sunk(VelorumVisualRules.SINK_END), 1.0E-9D);
		assertTrue(VelorumVisualRules.sunk(12.0F) < VelorumVisualRules.SINK_DEPTH * 0.25D, "slowly, then faster");
		assertEquals(-1.0D, VelorumVisualRules.arrivalRing(0.0F, 0.6D, 3.5D));
		assertEquals(0.6D, VelorumVisualRules.arrivalRing(1.0F, 0.6D, 3.5D), 1.0E-9D);
		assertEquals(3.5D, VelorumVisualRules.arrivalRing(7.0F, 0.6D, 3.5D), 1.0E-9D);
		// linear like the hit Ring: halfway through its six ticks it is halfway out
		assertEquals(2.05D, VelorumVisualRules.arrivalRing(4.0F, 0.6D, 3.5D), 1.0E-9D);
		assertEquals(1.0F, VelorumVisualRules.column(VelorumVisualRules.RISE), 1.0E-4F);
	}

	@Test
	void cutsFlyTheirLanesAndSettle() {
		assertEquals(-1.0D, VelorumVisualRules.cutFront(-1.0F, 10.0D, 0.8D));
		assertEquals(10.0D, VelorumVisualRules.cutFront(VelorumVisualRules.CUT_FLIGHT, 10.0D, 0.8D), 1.0E-9D, "10 blocks at 0.8 a tick");
		assertEquals(1.0F, VelorumVisualRules.cutFade(5.0F));
		assertEquals(0.0F, VelorumVisualRules.cutFade(VelorumVisualRules.CUT_FLIGHT + 2.0F), 1.0E-4F);
		for (int i = 0; i < VelorumVisualRules.CUTS.length; i++) assertEquals(VelorumMove.CUT_DELAYS[i], VelorumVisualRules.CUTS[i]);
	}

	@Test
	void theEclipsePutsTheLightOutAndTheRedGridShowsOnlyInIt() {
		assertEquals(0, VelorumVisualRules.fingers(5.0F));
		assertEquals(VelorumVisualRules.FINGERS, VelorumVisualRules.fingers(39.0F));
		assertEquals(0.0F, VelorumVisualRules.lightOut(0.0F));
		assertEquals(1.0F, VelorumVisualRules.lightOut(VelorumVisualRules.ECLIPSE_WINDUP), 1.0E-4F);
		assertEquals(0.0F, VelorumVisualRules.grid(0.0F), "no grid before the light goes");
		assertEquals(1.0F, VelorumVisualRules.grid(VelorumVisualRules.ECLIPSE_WINDUP), 1.0E-4F);
		assertEquals(0.15F, VelorumVisualRules.cruorGlow(0.0F), 1.0E-4F, "faint always");
		assertEquals(0.8F, VelorumVisualRules.cruorGlow(1.0F), 1.0E-4F, "strong only in the Eclipse");
		assertEquals(VelorumEclipseRules.SHAPE_RISE, VelorumVisualRules.SHAPE_RISE);
		assertEquals(VelorumEclipseRules.REACH_TELL, VelorumVisualRules.REACH_TELL);
		assertEquals(VelorumEclipseRules.GRIP_TICKS, VelorumVisualRules.GRIP);
		assertEquals(1.0F, VelorumVisualRules.shapeRise(VelorumVisualRules.SHAPE_RISE), 1.0E-4F);
		assertEquals(1.0F, VelorumVisualRules.tearBack(20.0F), 1.0E-4F);
	}

	@Test
	void stainsDeepenAndVeinsRunAlongTheSeamsWithAtMostTwoTurns() {
		assertTrue(VelorumVisualRules.stainRadius(1.0F) > VelorumVisualRules.stainRadius(0.5F));
		assertTrue(VelorumVisualRules.stainRadius(0.5F) > VelorumVisualRules.stainRadius(0.0F));
		assertEquals(1.5D, VelorumVisualRules.stainRadius(1.0F), 1.0E-9D, "a soaked stain is the Splinter Crown's disc");
		for (int seed = 0; seed < 40; seed++) {
			double fx = -7.3D + seed * 0.37D, fz = 4.6D - seed * 0.21D;
			double[] path = VelorumVisualRules.veinPath(fx, fz, 3.4D, -2.2D, seed);
			assertEquals(0, path.length % 2);
			assertEquals(fx, path[0], 1.0E-9D, "it leaves the figure");
			assertEquals(-2.2D, path[path.length - 1], 1.0E-9D, "and arrives at the meeting point");
			assertTrue(VelorumVisualRules.turns(path) <= 2, "straight runs with at most two right-angle turns");
			for (int i = 2; i + 3 < path.length - 2; i += 2) {
				boolean seamX = Math.abs(path[i] - Math.rint(path[i])) < 1.0E-9D;
				boolean seamZ = Math.abs(path[i + 1] - Math.rint(path[i + 1])) < 1.0E-9D;
				assertTrue(seamX && seamZ, "its corners lie on the floor's block seams");
			}
		}
		assertEquals(0.0F, VelorumVisualRules.veinRun(VelorumVisualRules.VEIN_FLARE));
		assertEquals(1.0F, VelorumVisualRules.veinRun(VelorumVisualRules.VEIN_RUN_END), 1.0E-4F, "all fronts arrive together");
	}
}
