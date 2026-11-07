package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Flame;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Region;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The purifier on fire (design revision 2026-11-06, R1): nothing in the rot for any input; the Ignition builds from
 * smoke to flame up the staff and across the back, draws in at the locked beat and flares at the commit; the steady
 * burn follows the Kindling; each move's peak, the Offering's draw-down and its acceptance pulse; the Embering's
 * die-back by 45; the Repose gutter; the ending's fire beats; the lean and the stretch stay bounded; the growths
 * blacken in kindle order and regrow by the Embering's tick 60.
 */
class PutricielBodyFireRulesTest {
	private static final float EPS = 1.0E-3F;

	private static Moment burning(float kindling) {
		return Moment.of(PutricielStage.BURNING, 100.0F, kindling);
	}

	private static Moment igniting(float t) {
		return Moment.of(PutricielStage.IGNITING, t, 1.0F).moving(PutricielMove.IGNITION, t, 30, 6);
	}

	private static float tongue(Region region, double place, Moment m) {
		return PutricielBodyFireRules.anchor(region, place, false, m).tongue();
	}

	@Test
	void theRotNeverBurnsForAnyInput() {
		for (float time = 0.0F; time < 900.0F; time += 37.0F) {
			for (float kindling = 0.0F; kindling <= 1.0F; kindling += 0.25F) {
				for (boolean repose : new boolean[]{false, true}) {
					for (PutricielMove move : PutricielMove.values()) {
						for (float since : new float[]{-1.0F, 1.0F, 3.0F}) {
							Moment m = new Moment(PutricielStage.ROT, time, kindling, repose, move.id(), time % 60.0F, 30, 6,
									PutricielOpenings.OFFERING_ID, 4.0F, since, since, -1.0F);
							assertFalse(PutricielBodyFireRules.drawsBody(m));
							assertFalse(PutricielBodyFireRules.drawsLayer(m));
							assertEquals(0.0D, PutricielBodyFireRules.column(m));
							assertEquals(0.0F, PutricielBodyFireRules.heat(m));
							for (Region region : Region.values()) {
								for (double place = 0.0D; place <= 1.0D; place += 0.25D) {
									assertSame(Flame.NONE, PutricielBodyFireRules.anchor(region, place, place > 0.5D, m), region + " burns in the rot");
								}
								assertEquals(0.0F, PutricielBodyFireRules.region(region, m));
							}
						}
					}
				}
			}
		}
	}

	@Test
	void theIgnitionBuildsFromSmokeToTheFlare() {
		Moment cold = Moment.of(PutricielStage.IGNITING, 3.0F, 1.0F);
		for (Region region : Region.values()) assertEquals(0.0F, tongue(region, 0.0D, cold), "nothing lit before the Ignition runs");
		// heat without flame: soot seeps from the seams, nothing is lit
		for (Region region : Region.values()) assertEquals(0.0F, tongue(region, 0.0D, igniting(8.0F)), region + " before tick 10");
		assertTrue(PutricielBodyFireRules.anchor(Region.CHEST, 0.0D, false, igniting(8.0F)).smoke() > 0.1F, "the seams seep soot");
		// the first embers glow in the chest tears and at the grip wrap
		assertTrue(PutricielBodyFireRules.anchor(Region.CHEST, 0.0D, false, igniting(13.0F)).bed() > 0.1F);
		assertTrue(PutricielBodyFireRules.anchor(Region.STAFF, 0.0D, false, igniting(13.0F)).bed() > 0.1F);
		// the climb: the staff from its grip at 16 to its head at 26, the back from its root at 20 to its rim by 28
		assertEquals(0.0F, tongue(Region.STAFF, 0.0D, igniting(15.9F)));
		assertTrue(tongue(Region.STAFF, 0.0D, igniting(18.0F)) > 0.2F);
		assertEquals(0.0F, tongue(Region.STAFF, 1.0D, igniting(25.5F)), "the head catches last");
		assertTrue(tongue(Region.STAFF, 1.0D, igniting(27.0F)) > 0.05F);
		assertEquals(0.0F, tongue(Region.BACK, 0.0D, igniting(19.9F)));
		assertTrue(tongue(Region.BACK, 0.0D, igniting(23.0F)) > 0.3F);
		assertEquals(0.0F, tongue(Region.BACK, 1.0D, igniting(27.9F)));
		for (Region region : Region.values()) {
			float k = region.kindleTick(0.5D);
			Flame before = PutricielBodyFireRules.anchor(region, 0.5D, false, igniting(k - 2.0F));
			assertEquals(0.0F, before.tongue(), region + " smokes before it catches");
			assertTrue(before.smoke() > 0.2F, region + " smokes before it catches");
			assertTrue(tongue(region, 0.5D, igniting(Math.min(27.9F, k + 5.0F))) <= PutricielBodyFireRules.KINDLED + EPS);
		}
		// the locked beat draws every flame in, the cores white; the commit flares to 1.45 and settles to 1 by 42
		assertTrue(tongue(Region.STAFF, 0.0D, igniting(29.0F)) <= PutricielBodyFireRules.KINDLED * PutricielBodyFireRules.LOCKED + EPS);
		assertEquals(1.0F, PutricielBodyFireRules.anchor(Region.STAFF, 0.0D, false, igniting(28.5F)).heat(), EPS);
		for (Region region : Region.values()) {
			assertEquals(PutricielBodyFireRules.FLARE, tongue(region, 1.0D, igniting(30.0F)), EPS, region + " flares at the commit");
			assertEquals(PutricielBodyFireRules.DIP, tongue(region, 0.5D, igniting(36.0F)), EPS, "the breath at 36");
			assertEquals(1.0F, tongue(region, 0.5D, igniting(42.0F)), EPS, "settled by 42");
		}
		assertEquals(PutricielBodyFireRules.COLUMN_FLARE, PutricielBodyFireRules.column(igniting(30.0F)), 1.0E-3D, "the back column leaps to 2.2");
	}

	@Test
	void theSteadyBurnFollowsTheKindling() {
		assertEquals(1.0F, tongue(Region.BACK, 0.5D, burning(1.0F)), EPS);
		assertEquals(0.864F, tongue(Region.BACK, 0.5D, burning(0.66F)), EPS, "at 66% it burns at 0.86");
		assertEquals(0.732F, tongue(Region.BACK, 0.5D, burning(0.33F)), EPS, "at 33% at 0.73");
		float previous = Float.MAX_VALUE;
		for (float k = 1.0F; k >= 0.1F; k -= 0.05F) {
			float t = tongue(Region.STAFF, 0.5D, burning(k));
			assertTrue(t <= previous + EPS, "it burns down with the meter");
			previous = t;
		}
		// the last six percent: under Repose it gutters out, otherwise it falters to half
		Moment repose = new Moment(PutricielStage.BURNING, 100.0F, 0.0F, true, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, -1.0F);
		assertEquals(0.0F, tongue(Region.BACK, 0.5D, repose), EPS, "the Repose gutter reaches nothing at Kindling 0");
		assertEquals(PutricielBodyFireRules.KINDLING_FLOOR * PutricielBodyFireRules.FALTER, tongue(Region.BACK, 0.5D, burning(0.0F)), EPS);
		Moment halfway = new Moment(PutricielStage.BURNING, 100.0F, 0.03F, true, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, -1.0F);
		assertTrue(tongue(Region.BACK, 0.5D, halfway) < tongue(Region.BACK, 0.5D, burning(0.03F)), "Repose gutters below the falter");
	}

	@Test
	void eachMovePeaksItsRegions() {
		Moment sweep = burning(1.0F).moving(PutricielMove.PURGING_SWEEP, 20.0F, 20, 6);
		assertEquals(1.4F, tongue(Region.STAFF, 0.5D, sweep), EPS);
		assertEquals(1.4F, tongue(Region.CENSER, 0.5D, sweep), EPS);
		assertEquals(1.1F, tongue(Region.BACK, 0.5D, sweep), EPS);
		Moment cast = burning(1.0F).moving(PutricielMove.CENSER_CAST, 15.0F, 22, 4);
		assertEquals(1.5F, tongue(Region.CENSER, 0.0D, cast), EPS, "the thrown censer");
		assertEquals(1.2F, tongue(Region.STAFF, 0.5D, cast), EPS);
		Moment lane = burning(1.0F).moving(PutricielMove.CAUTERISING_LANE, 30.0F, 26, 24);
		assertEquals(1.5F, PutricielBodyFireRules.anchor(Region.SHOULDER, 0.5D, true, lane).tongue(), EPS, "the palm arm");
		assertEquals(1.0F, PutricielBodyFireRules.anchor(Region.SHOULDER, 0.5D, false, lane).tongue(), EPS, "the other arm");
		Moment lines = burning(1.0F).moving(PutricielMove.PYRE_LINES, 20.0F, 30, 20);
		assertEquals(1.3F, tongue(Region.CROWN, 0.5D, lines), EPS);
		// the Pyre Heart sucks the fire into the core, then flares to 1.6 with the column at 2.6
		assertEquals(0.8F, tongue(Region.BACK, 0.5D, burning(1.0F).moving(PutricielMove.PYRE_HEART, 12.0F, 28, 8)), 0.02F);
		assertEquals(0.6F, tongue(Region.BACK, 0.5D, burning(1.0F).moving(PutricielMove.PYRE_HEART, 24.0F, 28, 8)), EPS);
		assertEquals(1.6F, tongue(Region.BACK, 0.5D, burning(1.0F).moving(PutricielMove.PYRE_HEART, 29.0F, 28, 8)), EPS);
		assertEquals(1.0F, tongue(Region.BACK, 0.5D, burning(1.0F).moving(PutricielMove.PYRE_HEART, 44.0F, 28, 8)), EPS);
		assertEquals(PutricielBodyFireRules.COLUMN_HEART, PutricielBodyFireRules.column(burning(1.0F).moving(PutricielMove.PYRE_HEART, 29.0F, 28, 8)),
				1.0E-3D);
		// the Full Purge pulses each ring
		assertEquals(1.35F, tongue(Region.HEM, 0.5D, burning(1.0F).moving(PutricielMove.FULL_PURGE, 41.0F, 40, 48)), EPS);
		assertEquals(1.35F, tongue(Region.HEM, 0.5D, burning(1.0F).moving(PutricielMove.FULL_PURGE, 57.0F, 40, 48)), EPS);
		assertEquals(1.0F, tongue(Region.HEM, 0.5D, burning(1.0F).moving(PutricielMove.FULL_PURGE, 52.0F, 40, 48)), EPS);
		// a hit flares the body for four ticks
		Moment hit = new Moment(PutricielStage.BURNING, 100.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, 1.0F, -1.0F);
		assertEquals(PutricielBodyFireRules.HIT_PEAK, tongue(Region.BACK, 0.5D, hit), EPS);
	}

	@Test
	void theOfferingDrawsTheFireDownToThePalm() {
		Moment kneel = burning(1.0F).moving(PutricielMove.OFFERING, 10.0F, 12, 1);
		assertEquals(PutricielBodyFireRules.OFFERING_DRAW, tongue(Region.BACK, 0.5D, kneel), EPS);
		assertEquals(1.0F, PutricielBodyFireRules.anchor(Region.SHOULDER, 0.5D, true, kneel).tongue(), EPS, "the palm burns at full");
		// accepted: a 1.5 pulse from the palm up the arm and across the back, the body held down, back by recoil + 8
		Moment accepted = new Moment(PutricielStage.BURNING, 100.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 1.5F, -1.0F, -1.0F);
		assertEquals(1.5F, PutricielBodyFireRules.anchor(Region.SHOULDER, 0.5D, true, accepted).tongue(), EPS);
		Moment across = new Moment(PutricielStage.BURNING, 100.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 5.0F, -1.0F, -1.0F);
		assertTrue(tongue(Region.BACK, 0.5D, across) > PutricielBodyFireRules.OFFERING_DRAW * 1.3F, "the pulse crosses the back");
		Moment returned = new Moment(PutricielStage.BURNING, 100.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F,
				PutricielAbsolutionRules.RECOIL_TICKS + 8.5F, -1.0F, -1.0F);
		assertEquals(1.0F, tongue(Region.BACK, 0.5D, returned), EPS);
	}

	@Test
	void theEmberingDiesBackInReverseKindleOrder() {
		assertTrue(tongue(Region.BACK, 0.5D, Moment.of(PutricielStage.EMBERING, 0.0F, 0.0F)) > 0.1F);
		for (Region region : Region.values()) {
			assertEquals(0.0F, tongue(region, 0.5D, Moment.of(PutricielStage.EMBERING, PutricielBodyFireRules.GUTTERED, 0.0F)), EPS,
					region + " has guttered by 10");
			assertTrue(PutricielBodyFireRules.anchor(region, 0.5D, false, Moment.of(PutricielStage.EMBERING, 9.0F, 0.0F)).bed() > 0.5F);
		}
		// the hem, the crown and the arms go first, the back from its outer shelves in, the staff head last at 30
		assertEquals(0.0F, PutricielBodyFireRules.anchor(Region.HEM, 1.0D, false, Moment.of(PutricielStage.EMBERING, 14.0F, 0.0F)).bed(), EPS);
		assertTrue(PutricielBodyFireRules.anchor(Region.BACK, 0.0D, false, Moment.of(PutricielStage.EMBERING, 14.0F, 0.0F)).bed() > 0.5F);
		assertTrue(Region.BACK.outTick(1.0D) < Region.BACK.outTick(0.0D), "the back's outer shelves go before its root");
		assertTrue(PutricielBodyFireRules.anchor(Region.STAFF, 1.0D, false, Moment.of(PutricielStage.EMBERING, 28.0F, 0.0F)).bed() > 0.5F);
		assertEquals(0.0F, PutricielBodyFireRules.anchor(Region.STAFF, 1.0D, false, Moment.of(PutricielStage.EMBERING, 31.1F, 0.0F)).bed(), EPS);
		for (Region region : Region.values()) {
			for (double place = 0.0D; place <= 1.0D; place += 0.25D) {
				assertTrue(region.outTick(place) <= PutricielBodyFireRules.EMBERS_OUT, region + " out by 30");
			}
		}
		assertTrue(PutricielBodyFireRules.drawsLayer(Moment.of(PutricielStage.EMBERING, 29.0F, 0.0F)));
		assertFalse(PutricielBodyFireRules.drawsLayer(Moment.of(PutricielStage.EMBERING, 30.0F, 0.0F)), "the burning layer is out at 30");
		assertTrue(PutricielBodyFireRules.drawsBody(Moment.of(PutricielStage.EMBERING, 44.0F, 0.0F)));
		assertFalse(PutricielBodyFireRules.drawsBody(Moment.of(PutricielStage.EMBERING, 45.0F, 0.0F)), "nothing from 45");
		assertSame(Flame.NONE, PutricielBodyFireRules.anchor(Region.BACK, 0.5D, false, Moment.of(PutricielStage.EMBERING, 50.0F, 0.0F)));
	}

	@Test
	void theEndingBurnsThroughItsFireBeatsOnly() {
		Moment before = new Moment(PutricielStage.BURNING, 0.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, 10.0F);
		assertFalse(PutricielBodyFireRules.drawsBody(before));
		Moment burn = new Moment(PutricielStage.BURNING, 0.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, 40.0F);
		assertTrue(tongue(Region.BACK, 0.5D, burn) > 0.9F, "the last cleansing climbs the body");
		Moment after = new Moment(PutricielStage.BURNING, 0.0F, 1.0F, false, 0, -1.0F, 0, 0, 0, -1.0F, 1000.0F, -1.0F, 70.0F);
		assertFalse(PutricielBodyFireRules.drawsBody(after));
	}

	@Test
	void theLeanAndTheStretchStayBounded() {
		double max = Math.toRadians(PutricielBodyFireRules.MAX_LEAN_DEGREES) + 1.0E-6D;
		for (double speed = 0.0D; speed <= 4.0D; speed += 0.25D) {
			for (int a = 0; a < 8; a++) {
				double angle = a * Math.PI / 4.0D;
				Vec3 velocity = new Vec3(Math.cos(angle) * speed, (a % 3 - 1) * speed * 0.5D, Math.sin(angle) * speed);
				Vec3 tip = PutricielBodyFireRules.tip(new Vec3(Math.cos(angle + 1.0D), 0.2D, Math.sin(angle + 1.0D)).normalize(), velocity);
				assertEquals(1.0D, tip.length(), 1.0E-6D);
				assertTrue(Math.acos(tip.y) <= max, "within 70 degrees of up at " + speed);
			}
			double stretch = PutricielBodyFireRules.stretch(speed);
			assertTrue(stretch >= 1.0D && stretch <= 1.0D + PutricielBodyFireRules.STRETCH + 1.0E-9D);
		}
		assertEquals(1.0D, PutricielBodyFireRules.stretch(0.0D));
		assertEquals(1.8D, PutricielBodyFireRules.stretch(0.35D), 1.0E-9D);
		assertFalse(PutricielBodyFireRules.sheds(0.2D));
		assertTrue(PutricielBodyFireRules.sheds(0.3D));
		Vec3 swung = PutricielBodyFireRules.tip(new Vec3(1, 0, 0), new Vec3(0.3D, 0.0D, 0.0D));
		assertTrue(swung.x < 0.0D, "a swung staff drags its flames back along the swing");
		for (int seed = 0; seed < 50; seed++) {
			for (double time = 0.0D; time < 400.0D; time += 7.3D) {
				float flicker = PutricielBodyFireRules.flicker(time, seed);
				assertTrue(flicker >= 1.0F - PutricielBodyFireRules.FLICKER - EPS && flicker <= 1.0F + PutricielBodyFireRules.FLICKER + EPS);
			}
		}
	}

	@Test
	void theGrowthsBlackenInKindleOrderAndRegrowByTick60() {
		assertEquals(1.0F, PutricielBodyFireRules.growth(0, 0, false, Moment.of(PutricielStage.ROT, 10.0F, 0.5F)));
		assertEquals(0.0F, PutricielBodyFireRules.charAt(0, 0, Moment.of(PutricielStage.ROT, 10.0F, 0.5F)));
		assertEquals(PutricielVisualRules.CHAR_FULL, PutricielBodyFireRules.charAt(0, 0, igniting(16.0F)), EPS, "the first black by 16");
		assertEquals(0.0F, PutricielBodyFireRules.charAt(10, 0, igniting(16.0F)), EPS, "a later one not yet");
		for (int order = 0; order <= 30; order++) {
			assertEquals(PutricielVisualRules.CHAR_FULL, PutricielBodyFireRules.charAt(order, 0, igniting(30.0F)), EPS, "black by 30: " + order);
			assertEquals(PutricielVisualRules.GROWTH_BURNT, PutricielBodyFireRules.growth(order, 0, false, igniting(30.0F)), EPS);
		}
		assertEquals(PutricielBodyFireRules.EATEN, PutricielBodyFireRules.growth(0, 0, true, burning(0.0F)), EPS, "the outer shelves eaten");
		assertEquals(PutricielVisualRules.GROWTH_BURNT, PutricielBodyFireRules.growth(0, 0, true, burning(1.0F)), EPS);
		for (int rank = 0; rank <= 40; rank++) {
			for (boolean eaten : new boolean[]{false, true}) {
				Moment end = Moment.of(PutricielStage.EMBERING, 60.0F, 0.0F);
				assertEquals(1.0F, PutricielBodyFireRules.growth(0, rank, eaten, end), EPS, "regrown by 60: " + rank);
				assertEquals(0.0F, PutricielBodyFireRules.charAt(0, rank, end), EPS);
			}
		}
		assertTrue(PutricielBodyFireRules.growth(0, 0, false, Moment.of(PutricielStage.EMBERING, 6.0F, 0.0F))
				> PutricielBodyFireRules.growth(0, 8, false, Moment.of(PutricielStage.EMBERING, 6.0F, 0.0F)), "the root regrows first");
	}
}
