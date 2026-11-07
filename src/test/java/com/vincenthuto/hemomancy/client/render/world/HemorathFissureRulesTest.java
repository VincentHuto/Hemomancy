package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Crack;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Look;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Slab;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Spike;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Trench;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Hemorath's visual pass 2 rules: a telegraph's life, the trenches and their slabs (pass 2b), the crack networks,
 * the iron's punch and the debris.
 */
class HemorathFissureRulesTest {
	private static final int REVEAL = 4, WINDUP = 28, ACTIVE = 12;

	@Test
	void aTelegraphBuildsFlashesStandsThenDrainsAndSeals() {
		assertFalse(HemorathFissureRules.look(REVEAL - 1, REVEAL, WINDUP, ACTIVE).visible(), "nothing before the reveal");
		float previousHeat = -1.0F, previousBlood = -1.0F;
		for (float t = REVEAL + 1; t < WINDUP; t += 1.0F) {
			Look look = HemorathFissureRules.look(t, REVEAL, WINDUP, ACTIVE);
			assertTrue(look.visible(), "shows through the wind-up at " + t);
			assertTrue(look.heat() >= previousHeat && look.blood() >= previousBlood, "brightens and wells toward the commit");
			assertTrue(look.heat() < HemorathFissureRules.WHITE_HOT, "white-hot is kept for the commit");
			previousHeat = look.heat();
			previousBlood = look.blood();
		}
		assertEquals(1.0F, HemorathFissureRules.look(REVEAL + (WINDUP - REVEAL) * HemorathFissureRules.CRACK_LEAD, REVEAL, WINDUP,
				ACTIVE).grow(), 1e-4F, "the cracks have run their whole length before the commit");
		Look flash = HemorathFissureRules.look(WINDUP, REVEAL, WINDUP, ACTIVE);
		assertTrue(flash.heat() >= HemorathFissureRules.WHITE_HOT && flash.width() > 1.0F, "the commit flashes and the gap yawns");
		Look standing = HemorathFissureRules.look(WINDUP + ACTIVE - 1, REVEAL, WINDUP, ACTIVE);
		assertEquals(1.0F, standing.alpha(), 1e-6F);
		assertEquals(1.0F, standing.blood(), 1e-6F, "full of blood through the active span");
		Look draining = HemorathFissureRules.look(WINDUP + ACTIVE + HemorathFissureRules.LINGER * 0.5F, REVEAL, WINDUP, ACTIVE);
		assertTrue(draining.visible() && draining.blood() < 1.0F && draining.heat() < standing.heat(), "cools and drains");
		assertFalse(HemorathFissureRules.look(HemorathFissureRules.lastTick(WINDUP, ACTIVE), REVEAL, WINDUP, ACTIVE).visible(),
				"sealed by the last tick");
	}

	@Test
	void aTrenchSplitsFloodsFromTheFarEndFlaresThenDrainsAndCloses() {
		assertFalse(trench(REVEAL - 1, 7.0D, -1.0D).visible(), "nothing before the reveal");
		float early = REVEAL + (WINDUP - REVEAL) * 0.1F;
		assertTrue(trench(early, 1.5D, -1.0D).visible() && !trench(early, 12.0D, -1.0D).visible(),
				"the floor splits out from his feet");
		float mid = REVEAL + (WINDUP - REVEAL) * 0.55F;
		Trench near = trench(mid, 1.5D, -1.0D), far = trench(mid, 12.5D, -1.0D);
		assertTrue(far.blood() > near.blood() + 0.2F, "the blood floods in from the far end: " + near + " " + far);
		assertTrue(near.depth() > 0.0F && near.heat() < HemorathFissureRules.WHITE_HOT, "white-hot is kept for the commit");
		assertTrue(trench(WINDUP - 1, 7.0D, -1.0D).depth() > trench(mid, 7.0D, -1.0D).depth(), "the floor drops as it builds");
		assertEquals(HemorathFissureRules.FLOOD_LEVEL, trench(WINDUP - 0.01F, 1.5D, -1.0D).blood(), 1e-3F,
				"flooded back to his feet by the commit");
		Trench flash = trench(WINDUP, 7.0D, -1.0D);
		assertTrue(flash.heat() >= HemorathFissureRules.WHITE_HOT && flash.heave() > flash.depth(),
				"the seam flares and the slabs jolt at the commit");
		assertTrue(trench(WINDUP + 2, 0.0D, -1.0D).depth() < trench(WINDUP + 2, 7.0D, -1.0D).depth() * 0.3F,
				"the ends shoal up");
		float t = WINDUP + 8;
		float behind = trench(t, 8.8D, 9.0D).heat();
		assertTrue(behind > trench(t, 2.0D, 9.0D).heat() && behind > trench(t, 12.0D, 9.0D).heat(),
				"the seam flares just behind the iron's front");
		for (float time = REVEAL; time < WINDUP + ACTIVE + HemorathFissureRules.TRENCH_CLOSE; time += 0.5F) {
			for (double along = 0.0D; along <= 14.0D; along += 0.5D) {
				Trench state = trench(time, along, -1.0D);
				assertTrue(state.blood() <= HemorathFissureRules.SURGE_LEVEL + 1e-6F && state.depth() <= 1.0F + 1e-6F,
						"the blood never brims");
			}
		}
		float draining = WINDUP + ACTIVE + HemorathFissureRules.TRENCH_CLOSE * 0.3F;
		assertTrue(trench(draining, 1.5D, -1.0D).blood() < trench(draining, 12.5D, -1.0D).blood(),
				"it drains back out the far end, the near end first");
		assertTrue(trench(WINDUP + ACTIVE + HemorathFissureRules.TRENCH_CLOSE * 0.95F, 7.0D, -1.0D).depth() < 0.05F,
				"the channel closes");
		assertFalse(trench(HemorathFissureRules.trenchLastTick(WINDUP, ACTIVE, HemorathFissureRules.TRENCH_CLOSE), 7.0D, -1.0D)
				.visible(), "closed by the last tick");
		assertFalse(trench(WINDUP, 14.5D, -1.0D).visible(), "nothing past the strip's end");
	}

	@Test
	void slabsFrameTheTrenchNeverCoverItAndHideAtRest() {
		for (int seed = 0; seed < 40; seed++) {
			for (double half : new double[]{0.5D, 0.55D, 0.6D}) {
				List<Slab> slabs = HemorathFissureRules.slabs(0.5D, 14.0D, half, seed);
				assertTrue(slabs.size() >= 12 && slabs.size() <= 64, "both lips broken into a few dozen slabs: " + slabs.size());
				for (Slab slab : slabs) {
					assertTrue(slab.along() - slab.length() * 0.5D >= 0.5D - 1e-9 && slab.along() + slab.length() * 0.5D <= 14.0D + 1e-9);
					assertEquals(1, Math.abs(slab.side()));
					assertEquals(0.0D, HemorathFissureRules.slabLift(slab, (float) slab.lag()), 1e-6, "waits for its lag");
					for (double lift : new double[]{0.0D, 0.4D, 1.0D, 1.3D}) {
						for (double[] corner : HemorathFissureRules.slabCorners(slab, half, lift)) {
							assertTrue(slab.side() * corner[1] >= half + HemorathFissureRules.SLAB_GAP - 1e-9,
									"a slab frames the footprint and never covers it");
							assertTrue(Math.abs(corner[1]) <= half * (1.0D + HemorathFissureRules.SLAB_REACH) + 1e-9, "within its reach");
							if (lift == 0.0D) assertTrue(corner[2] <= 0.0D, "hidden under the floor at rest");
							assertTrue(corner[2] < 0.6D, "heaved, not thrown");
						}
					}
				}
			}
		}
		assertTrue(HemorathFissureRules.slabs(0.0D, 0.3D, 0.5D, 1).isEmpty(), "no room, no slabs");
	}

	private static Trench trench(float time, double along, double front) {
		return HemorathFissureRules.trench(time, REVEAL, WINDUP, ACTIVE, HemorathFissureRules.TRENCH_CLOSE, along, 0.0D, 14.0D,
				front);
	}

	@Test
	void eachNetworkStaysOnItsFootprint() {
		for (int seed = 0; seed < 40; seed++) {
			for (Crack crack : HemorathFissureRules.lane(14.0D, 0.5D, seed)) {
				for (int i = 0; i < crack.size(); i++) {
					assertTrue(crack.x()[i] >= -0.1D && crack.x()[i] <= 14.1D, "along the lane, its end cap wandering a little");
					assertTrue(Math.abs(crack.y()[i]) <= 0.5D + 0.31D, "inside the lane's width and its jag");
				}
			}
			for (Crack crack : HemorathFissureRules.cone(0.0D, 4.5D, -50.0D, 50.0D, seed)) {
				for (int i = 0; i < crack.size(); i++) {
					double r = Math.hypot(crack.x()[i], crack.y()[i]);
					assertTrue(r <= 4.5D + 0.45D, "inside the claw's reach: " + r);
					if (r > 1.0D) assertTrue(Math.abs(Math.toDegrees(Math.atan2(crack.y()[i], crack.x()[i]))) <= 50.0D + 14.0D,
							"inside the cone's sides");
				}
			}
			for (Crack crack : HemorathFissureRules.ring(14.0D, seed)) {
				for (int i = 0; i < crack.size(); i++) {
					double r = Math.hypot(crack.x()[i], crack.y()[i]);
					assertTrue(r >= 12.4D && r <= 15.2D, "on the siphon's ring: " + r);
				}
			}
			for (Crack crack : HemorathFissureRules.star(1.5D, seed)) {
				for (int i = 0; i < crack.size(); i++) assertTrue(Math.hypot(crack.x()[i], crack.y()[i]) <= 1.5D * 1.2D + 0.3D);
			}
			for (Crack crack : HemorathFissureRules.rim(7.5D, 7.5D, seed)) {
				for (int i = 0; i < crack.size(); i++) {
					assertTrue(Math.abs(crack.x()[i]) <= 7.5D && Math.abs(crack.y()[i]) <= 7.5D, "inside the boss floor");
				}
			}
		}
	}

	@Test
	void networksAreStableForASeedAndBounded() {
		List<Crack> a = HemorathFissureRules.disc(6.0D, 17), b = HemorathFissureRules.disc(6.0D, 17);
		assertEquals(a.size(), b.size());
		for (int i = 0; i < a.size(); i++) {
			assertArrayEquals(a.get(i).x(), b.get(i).x());
			assertArrayEquals(a.get(i).y(), b.get(i).y());
		}
		for (List<Crack> network : List.of(a, HemorathFissureRules.lane(14.0D, 1.0D, 3), HemorathFissureRules.ring(18.0D, 3),
				HemorathFissureRules.cone(0.0D, 9.0D, -25.0D, 25.0D, 3), HemorathFissureRules.gouges(4.5D, -50.0D, 50.0D, 3))) {
			assertFalse(network.isEmpty());
			assertTrue(network.size() <= 64, "a network stays a few dozen cracks");
			for (Crack crack : network) {
				assertTrue(crack.size() >= 2 && crack.width() > 0.0D && crack.length() > 0.0D);
				assertTrue(crack.start() >= 0.0D && crack.start() <= 1.0D && crack.span() > 0.0D);
			}
		}
		assertEquals(0.0D, HemorathFissureRules.shown(0.2D, 0.5D, 0.3D));
		assertEquals(1.0D, HemorathFissureRules.shown(0.9D, 0.5D, 0.3D));
	}

	@Test
	void ironPunchesPastFullHeightShiversThenSinksIntoBlood() {
		assertEquals(0.0F, HemorathFissureRules.rise(0.0F));
		float peak = 0.0F;
		for (float t = 0.0F; t <= HemorathFissureRules.PUNCH; t += 0.25F) peak = Math.max(peak, HemorathFissureRules.rise(t));
		assertTrue(peak > 1.1F, "the punch overshoots");
		assertEquals(1.0F, HemorathFissureRules.rise(HemorathFissureRules.PUNCH + HemorathFissureRules.SETTLE), 1e-5F);
		float sinkAt = 30.0F;
		assertEquals(1.0F, HemorathFissureRules.height(sinkAt, sinkAt), 1e-5F, "stands until it sinks");
		assertEquals(0.0F, HemorathFissureRules.height(sinkAt + HemorathFissureRules.SINK, sinkAt), 1e-5F, "gone by the end");
		assertTrue(HemorathFissureRules.formation(1.0F, sinkAt) < 1.0F, "the wet front is still climbing in the punch");
		assertEquals(1.0F, HemorathFissureRules.formation(10.0F, sinkAt), 1e-5F);
		assertTrue(HemorathFissureRules.formation(sinkAt + HemorathFissureRules.SINK, sinkAt) <= 0.5F + 1e-5F,
				"it dissolves back into blood from the point");
		assertEquals(0.0D, HemorathFissureRules.shiver(1.0F, 100.0D, 3));
		assertTrue(Math.abs(HemorathFissureRules.shiver(40.0F, 100.0D, 3)) < 0.002D, "the shiver dies away");
	}

	@Test
	void clustersVaryAndKeepTheirTallSpikeInTheMiddle() {
		List<Spike> one = HemorathFissureRules.cluster(5, 1.2D, 5), two = HemorathFissureRules.cluster(6, 1.2D, 5);
		assertEquals(5, one.size());
		assertEquals(0.0D, one.get(0).dx());
		for (Spike spike : one) {
			assertTrue(spike.height() <= one.get(0).height() + 1e-9, "the centre is the tallest");
			assertTrue(spike.radius() > 0.0D && spike.radius() < spike.height(), "sharp, not stubby");
			assertTrue(spike.sides() >= 5);
		}
		assertNotEquals(one.get(1).height(), two.get(1).height(), "no two clusters stand alike");
	}

	@Test
	void chipsArcUpAndSettle() {
		for (int i = 0; i < 12; i++) {
			double highest = 0.0D;
			for (float age = 0.0F; age < HemorathFissureRules.CHIP_LIFE; age += 0.5F) {
				double[] chip = HemorathFissureRules.chip(9, i, age, 1.5D);
				assertNotNull(chip);
				assertTrue(chip[1] >= 0.0D, "never under the floor");
				highest = Math.max(highest, chip[1]);
			}
			assertTrue(highest > 0.1D, "thrown up");
			assertNull(HemorathFissureRules.chip(9, i, HemorathFissureRules.CHIP_LIFE, 1.5D));
		}
	}

	@Test
	void bolusesSwellOncePerSpacing() {
		int peaks = 0;
		double previous = HemorathFissureRules.bolus(0.0D, 0.0D, 0.3D, 1.6D), current = HemorathFissureRules.bolus(0.05D, 0.0D, 0.3D, 1.6D);
		for (double s = 0.1D; s <= 8.0D; s += 0.05D) {
			double next = HemorathFissureRules.bolus(s, 0.0D, 0.3D, 1.6D);
			if (current > previous && current >= next) peaks++;
			previous = current;
			current = next;
		}
		assertEquals(5, peaks, "one swell every 1.6 blocks along 8");
		for (int i = 0; i < 200; i++) {
			double u = HemorathFissureRules.unit(i * 7919, i);
			assertTrue(u >= 0.0D && u < 1.0D);
		}
	}
}
