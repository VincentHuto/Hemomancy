package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Kind;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Look;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Spur;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Seraphae's visual pass 2 rules: a telegraph runs emerge, build, the flash, the strike, the linger and the
 * dissolve; a lane is one furrow filling its whole width (the trench), not parallel lines; every network stays on its
 * footprint and is stable per seed; bone grows past full, settles, throbs and withers away; shards arc and settle; the
 * lash and reach trails, the waves, the whip and the chain links keep their timing and shape.
 */
class SeraphaeOvergrowthRulesTest {
	private static final float EPS = 1.0E-4F;

	@Test
	void aTelegraphEmergesBuildsFlashesStrikesLingersAndDissolves() {
		int windup = 24, active = 10;
		float linger = SeraphaeOvergrowthRules.LINGER;
		assertEquals(Look.NONE, SeraphaeOvergrowthRules.look(-1.0F, 0, windup, active, linger), "nothing before the reveal");
		Look emerge = SeraphaeOvergrowthRules.look(0.0F, 0, windup, active, linger);
		assertEquals(0.0F, emerge.grow(), EPS, "the cut has not run yet at the reveal");
		Look mid = SeraphaeOvergrowthRules.look(12.0F, 0, windup, active, linger);
		Look late = SeraphaeOvergrowthRules.look(23.9F, 0, windup, active, linger);
		assertTrue(mid.grow() > 0.99F, "the cut has run its whole length by 55% of the reveal");
		assertTrue(late.swell() > mid.swell() && late.light() > mid.light() && late.raw() > mid.raw(), "it swells, lights and goes raw");
		assertTrue(late.light() < SeraphaeOvergrowthRules.WHITE_HOT, "the hot core waits for the commit");
		Look flash = SeraphaeOvergrowthRules.look(24.0F, 0, windup, active, linger);
		assertEquals(1.0F, flash.light(), EPS, "the light runs white-hot at the commit");
		assertTrue(flash.swell() > 1.2F, "and the swelling surges");
		Look strike = SeraphaeOvergrowthRules.look(30.0F, 0, windup, active, linger);
		assertEquals(1.0F, strike.swell(), EPS);
		assertTrue(strike.light() > 0.6F && strike.light() < 0.9F, "the light settles through the strike");
		Look early = SeraphaeOvergrowthRules.look(36.0F, 0, windup, active, linger);
		Look scar = SeraphaeOvergrowthRules.look(48.0F, 0, windup, active, linger);
		assertTrue(early.light() > scar.light() && early.swell() > scar.swell(), "the light drains and the swelling subsides");
		assertTrue(scar.swell() >= SeraphaeOvergrowthRules.SCAR - EPS, "into a pale scar");
		assertTrue(scar.alpha() < early.alpha(), "then it fades");
		float last = SeraphaeOvergrowthRules.lastTick(windup, active, linger);
		assertEquals(Look.NONE, SeraphaeOvergrowthRules.look(last + 0.1F, 0, windup, active, linger), "gone after its linger");
		assertTrue(SeraphaeOvergrowthRules.look(last - 0.5F, 0, windup, active, linger).visible());
	}

	@Test
	void theLingerNeverOutlastsTheRecovery() {
		assertEquals(10.0F, SeraphaeOvergrowthRules.linger(10), EPS);
		assertEquals(SeraphaeOvergrowthRules.LINGER, SeraphaeOvergrowthRules.linger(30), EPS);
		assertTrue(SeraphaeOvergrowthRules.linger(0) > 0.0F);
	}

	@Test
	void aLaneIsOneFurrowFillingItsWholeWidth() {
		for (int seed = 0; seed < 40; seed++) {
			double length = 6.0D + seed % 11, half = 0.6D + 0.05D * (seed % 5);
			List<Seam> trench = SeraphaeOvergrowthRules.trench(length, half, seed);
			List<Seam> furrows = trench.stream().filter(s -> s.kind() == Kind.FURROW).toList();
			assertEquals(1, furrows.size(), "one furrow, the trench, never parallel cracks");
			Seam furrow = furrows.get(0);
			assertEquals(half, furrow.width(), 1.0E-9, "the furrow is the lane's full half-width");
			for (int i = 0; i < furrow.size(); i++) {
				assertTrue(Math.abs(furrow.y()[i]) <= half * 0.2D, "it runs down the lane's middle");
				assertTrue(furrow.x()[i] >= 0.0D && furrow.x()[i] <= length + 1.0E-9);
			}
			assertTrue(furrow.length() > length * 0.9D, "it runs the lane's length");
			long left = trench.stream().filter(s -> s.kind() == Kind.VESSEL && s.y()[0] < 0.0D).count();
			long right = trench.stream().filter(s -> s.kind() == Kind.VESSEL && s.y()[0] > 0.0D).count();
			assertTrue(left > 0 && right > 0, "vessels branch off both lips");
			for (Seam seam : trench) {
				for (int i = 0; i < seam.size(); i++) {
					assertTrue(Math.abs(seam.y()[i]) <= half + 4.0D && seam.x()[i] > -1.0D && seam.x()[i] < length + 4.0D, "bounded");
				}
				assertTrue(seam.start() >= 0.0D && seam.start() + seam.span() <= 1.6D);
			}
		}
		assertTrue(SeraphaeOvergrowthRules.trench(0.1D, 0.8D, 3).isEmpty());
	}

	@Test
	void everyNetworkStaysOnItsFootprint() {
		for (int seed = 0; seed < 40; seed++) {
			double radius = 2.0D + seed % 6;
			for (Seam seam : SeraphaeOvergrowthRules.fan(0.6D, radius, -75.0D, 75.0D, seed)) {
				for (int i = 0; i < seam.size(); i++) {
					double r = Math.hypot(seam.x()[i], seam.y()[i]);
					assertTrue(r <= radius + 0.25D, "fan fibre inside the cone: " + r);
					assertTrue(Math.abs(Math.toDegrees(Math.atan2(seam.y()[i], seam.x()[i]))) <= 75.0D + 20.0D);
				}
			}
			for (Seam seam : SeraphaeOvergrowthRules.disc(radius, seed)) {
				for (int i = 0; i < seam.size(); i++) assertTrue(Math.hypot(seam.x()[i], seam.y()[i]) <= radius * 1.06D, "disc");
			}
			for (Seam seam : SeraphaeOvergrowthRules.star(radius, seed)) {
				for (int i = 0; i < seam.size(); i++) assertTrue(Math.hypot(seam.x()[i], seam.y()[i]) <= radius * 1.1D, "star");
			}
			for (Seam seam : SeraphaeOvergrowthRules.fingers(radius, 5, seed)) {
				assertEquals(Kind.FURROW, seam.kind());
				for (int i = 0; i < seam.size(); i++) assertTrue(Math.hypot(seam.x()[i], seam.y()[i]) <= radius + 1.0E-9, "finger");
				assertTrue(Math.hypot(seam.x()[seam.size() - 1], seam.y()[seam.size() - 1]) < radius * 0.2D, "it curls in to the centre");
			}
			for (Seam seam : SeraphaeOvergrowthRules.spoke(1.5D, 7.0D, 34.0D, seed)) {
				for (int i = 0; i < seam.size(); i++) {
					double r = Math.hypot(seam.x()[i], seam.y()[i]);
					assertTrue(r >= 1.4D && r <= 7.1D, "spoke radius " + r);
					assertTrue(Math.abs(Math.toDegrees(Math.atan2(seam.y()[i], seam.x()[i]))) <= 17.0D + 2.0D, "inside its arc");
				}
			}
			assertTrue(SeraphaeOvergrowthRules.spoke(1.5D, 7.0D, 34.0D, seed).stream().filter(s -> s.kind() == Kind.FIBRE).count() >= 4,
					"a spoke is a bundle of nerve fibres");
		}
	}

	@Test
	void networksAreStablePerSeed() {
		List<Seam> a = SeraphaeOvergrowthRules.disc(4.0D, 11), b = SeraphaeOvergrowthRules.disc(4.0D, 11);
		assertEquals(a.size(), b.size());
		for (int i = 0; i < a.size(); i++) {
			assertArrayEquals(a.get(i).x(), b.get(i).x());
			assertArrayEquals(a.get(i).y(), b.get(i).y());
		}
		assertFalse(Arrays.equals(a.get(0).x(), SeraphaeOvergrowthRules.disc(4.0D, 12).get(0).x()), "another seed grows otherwise");
		assertTrue(a.size() < 120, "a disc stays a few dozen seams: " + a.size());
	}

	@Test
	void boneGrowsPastFullSettlesAndWithersAway() {
		assertEquals(0.0F, SeraphaeOvergrowthRules.rise(0.0F), EPS);
		assertEquals(SeraphaeOvergrowthRules.OVERGROW, SeraphaeOvergrowthRules.rise(SeraphaeOvergrowthRules.GROW), EPS);
		assertEquals(1.0F, SeraphaeOvergrowthRules.rise(SeraphaeOvergrowthRules.GROW + SeraphaeOvergrowthRules.SETTLE), EPS);
		assertTrue(SeraphaeOvergrowthRules.rise(1.0F) > 0.5F, "growth is fast");
		float witherAt = 20.0F;
		assertEquals(1.0F, SeraphaeOvergrowthRules.formation(SeraphaeOvergrowthRules.GROW, witherAt), EPS, "fully bone once grown");
		float soft = SeraphaeOvergrowthRules.formation(0.5F, witherAt);
		assertTrue(soft > 0.3F && soft < 1.0F, "the tip is still soft as it grows");
		assertEquals(0.0F, SeraphaeOvergrowthRules.formation(witherAt + SeraphaeOvergrowthRules.WITHER, witherAt), EPS, "withered away");
		assertTrue(SeraphaeOvergrowthRules.height(witherAt + 6.0F, witherAt) < 1.0F, "it shrinks as it withers");
		assertEquals(1.0D, SeraphaeOvergrowthRules.throb(1.0F, 3.0D, 4), 1.0E-9, "no throb while it grows");
		for (double t = 0.0D; t < 40.0D; t += 0.7D) {
			double throb = SeraphaeOvergrowthRules.throb(10.0F, t, 4);
			assertTrue(throb >= 1.0D && throb <= 1.0D + SeraphaeOvergrowthRules.THROB + 1.0E-9);
		}
		assertTrue(SeraphaeOvergrowthRules.glow(1.0F) > SeraphaeOvergrowthRules.glow(30.0F), "the light in it dims after birth");
	}

	@Test
	void clustersVaryWithTheCentreTallest() {
		for (int seed = 0; seed < 20; seed++) {
			List<Spur> spurs = SeraphaeOvergrowthRules.cluster(seed, 1.3D, 6);
			assertEquals(6, spurs.size());
			for (int i = 1; i < spurs.size(); i++) {
				assertTrue(spurs.get(i).height() < spurs.get(0).height(), "the centre spur stands tallest");
				assertTrue(spurs.get(i).curl() > spurs.get(0).curl(), "the outer spurs curl further");
			}
		}
		assertNotEquals(SeraphaeOvergrowthRules.cluster(1, 1.3D, 4).get(1).dx(), SeraphaeOvergrowthRules.cluster(2, 1.3D, 4).get(1).dx());
		Spur bent = SeraphaeOvergrowthRules.cluster(1, 1.0D, 1).get(0).toward(1.0D, 0.0D, 0.5D, 0.2D);
		assertTrue(bent.leanX() > 0.42D && bent.curl() > 0.3D);
	}

	@Test
	void shardsArcAndSettle() {
		assertNull(SeraphaeOvergrowthRules.shard(3, 0, -1.0F, 1.0D));
		assertNull(SeraphaeOvergrowthRules.shard(3, 0, SeraphaeOvergrowthRules.SHARD_LIFE, 1.0D));
		double highest = 0.0D;
		for (float age = 0.0F; age < SeraphaeOvergrowthRules.SHARD_LIFE; age += 0.5F) {
			double[] shard = SeraphaeOvergrowthRules.shard(3, 2, age, 1.5D);
			assertNotNull(shard);
			assertTrue(shard[1] >= 0.0D, "never under the floor");
			highest = Math.max(highest, shard[1]);
			assertTrue(shard[4] >= 0.0D && shard[4] <= 1.0D);
		}
		assertTrue(highest > 0.1D, "they fly");
		assertEquals(1.0D, SeraphaeOvergrowthRules.shard(3, 2, 0.5F, 1.5D)[4], 1.0E-9);
	}

	@Test
	void lashAndReachTrailsKeepTheirTiming() {
		assertEquals(-1.0F, SeraphaeOvergrowthRules.lashHead(-SeraphaeOvergrowthRules.LASH_LEAD - 0.5F), EPS);
		assertEquals(0.0F, SeraphaeOvergrowthRules.lashHead(-SeraphaeOvergrowthRules.LASH_LEAD), EPS, "it starts before the commit");
		assertEquals(1.0F, SeraphaeOvergrowthRules.lashHead(SeraphaeOvergrowthRules.LASH_SWEEP), EPS);
		assertEquals(1.0F, SeraphaeOvergrowthRules.lashFade(2.0F), EPS, "full through the sweep");
		assertTrue(SeraphaeOvergrowthRules.lashFray(10.0F) > 0.5F, "then it frays");
		assertEquals(-1.0F, SeraphaeOvergrowthRules.lashHead(SeraphaeOvergrowthRules.LASH_SWEEP + SeraphaeOvergrowthRules.LASH_LINGER), EPS);
		assertEquals(0.0F, SeraphaeOvergrowthRules.reachFade(-3.0F, 10), EPS);
		assertEquals(1.0F, SeraphaeOvergrowthRules.reachFade(4.0F, 10), EPS, "full through the glide");
		assertEquals(0.0F, SeraphaeOvergrowthRules.reachFade(10.0F + SeraphaeOvergrowthRules.REACH_LINGER, 10), EPS);
		assertTrue(SeraphaeOvergrowthRules.beamFlare(0.0F) > SeraphaeOvergrowthRules.beamFlare(6.0F));
		assertEquals(0.0F, SeraphaeOvergrowthRules.bite(0.0F), EPS);
		assertEquals(1.0F, SeraphaeOvergrowthRules.bite(2.0F), EPS, "bone out of the struck body");
		assertEquals(0.0F, SeraphaeOvergrowthRules.bite(8.5F), EPS, "and drawn back in");
	}

	@Test
	void wavesRollOutAndStop() {
		assertTrue(SeraphaeOvergrowthRules.wave(-0.5F, 4.0D) < 0.0D);
		assertTrue(SeraphaeOvergrowthRules.wave(SeraphaeOvergrowthRules.WAVE + 0.5F, 4.0D) < 0.0D);
		assertEquals(4.0D, SeraphaeOvergrowthRules.wave(SeraphaeOvergrowthRules.WAVE * 0.8F, 4.0D), 1.0E-6);
		assertTrue(SeraphaeOvergrowthRules.wave(1.0F, 4.0D) < SeraphaeOvergrowthRules.wave(3.0F, 4.0D));
	}

	@Test
	void chainsLinkSagAndWhipPinnedAtBothEnds() {
		assertEquals(1, SeraphaeOvergrowthRules.links(SeraphaeOvergrowthRules.LINK_LENGTH));
		assertEquals(9, SeraphaeOvergrowthRules.links(3.0D));
		assertEquals(0.0D, SeraphaeOvergrowthRules.sag(0.5D, 1.0F), 1.0E-9, "a taut chain hangs straight");
		assertEquals(SeraphaeOvergrowthRules.SAG, SeraphaeOvergrowthRules.sag(0.5D, 0.0F), 1.0E-9);
		assertEquals(0.0D, SeraphaeOvergrowthRules.sag(0.0D, 0.0F), 1.0E-9);
		for (float age = 0.0F; age < 30.0F; age += 1.0F) {
			assertEquals(0.0D, SeraphaeOvergrowthRules.whip(0.0D, age, 1.0D), 1.0E-9, "her hands hold it");
			assertEquals(0.0D, SeraphaeOvergrowthRules.whip(1.0D, age, 1.0D), 1.0E-9, "the anchor holds it");
			for (double t = 0.0D; t <= 1.0D; t += 0.1D) assertTrue(Math.abs(SeraphaeOvergrowthRules.whip(t, age, 1.0D)) <= 1.0D + 1.0E-9);
		}
		assertEquals(0.0D, SeraphaeOvergrowthRules.whip(0.5D, -1.0F, 1.0D), 1.0E-9, "nothing before the commit");
		assertTrue(Math.abs(SeraphaeOvergrowthRules.whip(0.3D, 2.0F, 1.0D)) > 0.05D, "the wave runs out from her hands");
		assertTrue(Math.abs(SeraphaeOvergrowthRules.whip(0.5D, 60.0F, 1.0D)) < 0.02D, "and dies away");
	}

	@Test
	void knotsTravelAndSeedsAreUnit() {
		assertTrue(SeraphaeOvergrowthRules.knot(0.7D, 0.0D, 0.5D, 1.4D) > 0.99D, "a knot's heart half a spacing along");
		assertTrue(SeraphaeOvergrowthRules.knot(0.0D, 0.0D, 0.5D, 1.4D) < 0.2D);
		assertEquals(SeraphaeOvergrowthRules.knot(0.7D, 0.0D, 0.5D, 1.4D), SeraphaeOvergrowthRules.knot(1.2D, 1.0D, 0.5D, 1.4D), 1.0E-9,
				"it moves at its speed");
		for (int i = 0; i < 500; i++) {
			double u = SeraphaeOvergrowthRules.unit(i * 7, i);
			assertTrue(u >= 0.0D && u < 1.0D);
		}
	}
}
