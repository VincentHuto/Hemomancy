package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Face;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Look;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Shard;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.TrenchPoint;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Velorum's visual pass 2 rules: a telegraph's life from reveal to its last frost, seam networks that stay on
 * their footprints and are stable per seed, the sunken trench as the floor plane shows it (the near walls hidden, the
 * far wall where a sunken face would show, nothing outside the mouth), ice that punches, holds and sublimes, chips
 * that land and fade, and the icicles, folds and fills on their clocks.
 */
class VelorumFrostRulesTest {
	@Test
	void aTelegraphBuildsFlashesHoldsAndSublimes() {
		assertFalse(VelorumFrostRules.look(4.0F, 6, 24, 12).visible(), "nothing before the reveal");
		Look early = VelorumFrostRules.look(8.0F, 6, 24, 12), late = VelorumFrostRules.look(22.0F, 6, 24, 12);
		assertTrue(early.visible() && late.visible());
		assertTrue(late.grow() > early.grow() && late.frost() > early.frost() && late.glint() > early.glint(), "the cold builds");
		assertEquals(0.0F, early.ice(), 1.0E-6F, "black ice films only in the wind-up's second half");
		assertTrue(late.ice() > 0.0F);
		Look flash = VelorumFrostRules.look(24.0F, 6, 24, 12);
		assertEquals(1.0F, flash.glint(), 1.0E-6F, "the commit runs white");
		assertTrue(flash.width() > 1.2F, "the seams yawn at the flash");
		Look active = VelorumFrostRules.look(32.0F, 6, 24, 12);
		assertEquals(1.0F, active.ice(), 1.0E-6F);
		assertTrue(active.glint() < flash.glint());
		Look linger = VelorumFrostRules.look(24.0F + 12.0F + 12.0F, 6, 24, 12);
		assertTrue(linger.alpha() < 1.0F && linger.width() < 1.0F, "the seams freeze shut as it fades");
		assertFalse(VelorumFrostRules.look(24.0F + 12.0F + VelorumFrostRules.LINGER, 6, 24, 12).visible(), "gone after the linger");
		// a short recovery (the Repose deed) shortens the linger so the frost is gone when the move ends
		assertFalse(VelorumFrostRules.look(16.0F + 3.0F + 12.0F, 4, 16, 3, 12).visible());
		assertTrue(VelorumFrostRules.look(16.0F + 3.0F + 6.0F, 4, 16, 3, 12).visible());
		// a move shorter than the flash still flashes whole
		assertEquals(1.0F, VelorumFrostRules.look(17.0F, 4, 16, 3).alpha(), 1.0E-6F);
		for (float t = 0.0F; t < 80.0F; t += 0.5F) {
			Look look = VelorumFrostRules.look(t, 6, 24, 12);
			for (float value : new float[]{look.grow(), look.frost(), look.ice(), look.glint(), look.alpha()}) {
				assertTrue(value >= 0.0F && value <= 1.0F, "in range at " + t);
			}
		}
	}

	@Test
	void seamNetworksStayOnTheirFootprintsAndAreStablePerSeed() {
		for (int seed = 0; seed < 40; seed++) {
			for (Seam seam : VelorumFrostRules.radial(2.5D, 10.0D, 16, 3.0D, seed)) {
				for (int i = 0; i < seam.size(); i++) {
					double r = Math.hypot(seam.x()[i], seam.y()[i]);
					assertTrue(r >= 2.3D && r <= 10.2D, "a radial seam stays in its ring: " + r);
				}
				assertTrue(seam.start() >= 0.0D && seam.start() + seam.span() <= 1.0D + 1.0E-9D);
			}
			for (Seam seam : VelorumFrostRules.lips(0.6D, 12.0D, 0.45D, seed)) {
				for (int i = 0; i < seam.size(); i++) {
					assertTrue(seam.x()[i] >= 0.6D - 1.0E-9D && seam.x()[i] <= 12.0D + 1.0E-9D, "a lip runs the lane's length");
					assertTrue(Math.abs(seam.y()[i]) >= 0.4D && Math.abs(seam.y()[i]) <= 0.45D + 0.5D, "a lip hugs its edge");
				}
			}
			for (Seam seam : VelorumFrostRules.cone(0.0D, 3.5D, -35.0D, 35.0D, seed)) {
				for (int i = 0; i < seam.size(); i++) {
					double r = Math.hypot(seam.x()[i], seam.y()[i]);
					double bearing = Math.toDegrees(Math.atan2(seam.y()[i], seam.x()[i]));
					assertTrue(r <= 3.65D, "a cone seam stays inside its radius: " + r);
					assertTrue(Math.abs(bearing) <= 35.0D + 9.0D, "a cone seam stays in its cone: " + bearing);
				}
			}
			List<Seam> once = VelorumFrostRules.radial(1.0D, 8.0D, 12, 0.0D, seed), twice = VelorumFrostRules.radial(1.0D, 8.0D, 12, 0.0D, seed);
			assertEquals(once.size(), twice.size());
			for (int i = 0; i < once.size(); i++) assertArrayEquals(once.get(i).x(), twice.get(i).x(), "stable per seed");
		}
		List<Seam> inward = VelorumFrostRules.inward(1.0D, 10.0D, 8, 0.0D, 3);
		Seam first = inward.get(0);
		assertTrue(Math.hypot(first.x()[0], first.y()[0]) > 9.0D, "the Draw's seams run in from the rim");
		assertEquals(0.0D, VelorumFrostRules.shown(0.2F, 0.4D, 0.6D), 1.0E-9D, "a fork waits for its seam to reach it");
		assertEquals(1.0D, VelorumFrostRules.shown(1.0F, 0.4D, 0.6D), 1.0E-9D);
	}

	@Test
	void theTrenchShowsItsFarWallAndBedWhereASunkenFaceWouldShow() {
		double half = 0.45D, depth = 0.42D, height = 6.0D;
		// a camera off to the right of the lane, high above it: the left wall shows, the right wall does not
		List<Face> faces = VelorumFrostRules.trench(0.6D, 12.0D, half, depth, 6.0D, 4.0D, height, true, true);
		long walls = faces.stream().filter(Face::wall).count();
		assertTrue(faces.stream().anyMatch(face -> !face.wall()), "the bed shows");
		assertTrue(walls >= 2, "the far side wall and an end wall show");
		for (Face face : faces) {
			for (TrenchPoint point : face.points()) {
				assertTrue(point.a() >= 0.6D - 1.0E-9D && point.a() <= 12.0D + 1.0E-9D, "nothing outside the mouth");
				assertTrue(Math.abs(point.r()) <= half + 1.0E-9D, "nothing outside the mouth");
				assertTrue(point.v() >= -1.0E-9D && point.v() <= 1.0D + 1.0E-9D);
			}
			if (face.wall()) {
				// the right wall's lip would run the lane's length at +half; turned away, it never shows
				double lo = Double.MAX_VALUE, hi = -Double.MAX_VALUE;
				for (TrenchPoint point : face.points()) {
					if (point.v() > 1.0E-9D || point.r() < half - 1.0E-6D) continue;
					lo = Math.min(lo, point.a());
					hi = Math.max(hi, point.a());
				}
				assertTrue(hi - lo < 1.0D, "the right wall, turned away, never shows");
			}
		}
		// the left wall's band: from the lip at -half in toward the camera by depth/(height+depth) of the gap
		double expected = -half + (4.0D + half) * depth / (height + depth);
		double deepest = faces.stream().filter(Face::wall).flatMap(face -> face.points().stream())
				.filter(point -> point.v() > 0.999D)
				.mapToDouble(TrenchPoint::r).min().orElse(Double.NaN);
		assertEquals(expected, deepest, 1.0E-6D, "the wall's foot shows where the view ray crosses the floor");
		// filled flush, only the bed
		List<Face> flush = VelorumFrostRules.trench(0.6D, 12.0D, half, 0.0D, 6.0D, 4.0D, height, true, true);
		assertEquals(1, flush.size());
		assertFalse(flush.get(0).wall());
		// a camera under the floor sees no trench
		assertTrue(VelorumFrostRules.trench(0.6D, 12.0D, half, depth, 6.0D, 0.0D, -1.0D, true, true).isEmpty());
		// end walls only where asked
		long ends = VelorumFrostRules.trench(2.0D, 4.0D, half, depth, 8.0D, 0.0D, height, false, false).stream().filter(Face::wall)
				.count();
		assertEquals(2, ends, "straight above the lane both side walls show and no end wall");
	}

	@Test
	void clippingKeepsWhatIsInsideAndCarriesV() {
		List<TrenchPoint> inside = List.of(new TrenchPoint(1, -0.2, 0), new TrenchPoint(2, -0.2, 0), new TrenchPoint(2, 0.2, 1),
				new TrenchPoint(1, 0.2, 1));
		assertEquals(inside, VelorumFrostRules.clip(inside, 0, 3, -1, 1));
		assertTrue(VelorumFrostRules.clip(inside, 5, 6, -1, 1).isEmpty());
		List<TrenchPoint> cut = VelorumFrostRules.clip(List.of(new TrenchPoint(0, 0, 0), new TrenchPoint(2, 0, 0), new TrenchPoint(2, 2, 1),
				new TrenchPoint(0, 2, 1)), 0, 2, 0, 1);
		for (TrenchPoint point : cut) {
			assertTrue(point.r() <= 1.0D + 1.0E-9D);
			assertEquals(point.r() / 2.0D, point.v(), 1.0E-9D, "V runs with the cut");
		}
	}

	@Test
	void icePunchesHoldsAndSublimesFromThePoint() {
		assertEquals(0.0D, VelorumFrostRules.height(0.0F, 20.0F));
		double peak = 0.0D;
		for (float t = 0.0F; t < 10.0F; t += 0.25F) peak = Math.max(peak, VelorumFrostRules.height(t, 20.0F));
		assertTrue(peak > 1.05D && peak <= VelorumFrostRules.OVERSHOOT + 1.0E-9D, "it punches past its height");
		assertEquals(1.0D, VelorumFrostRules.height(12.0F, 20.0F), 1.0E-9D, "and settles");
		assertEquals(1.0F, VelorumFrostRules.formation(12.0F, 20.0F));
		assertTrue(VelorumFrostRules.formation(25.0F, 20.0F) < 1.0F && VelorumFrostRules.formation(25.0F, 20.0F) > 0.0F, "it sublimes");
		assertEquals(0.0F, VelorumFrostRules.formation(20.0F + VelorumFrostRules.CRUMBLE, 20.0F));
		assertEquals(0.0D, VelorumFrostRules.height(20.0F + VelorumFrostRules.CRUMBLE, 20.0F), "gone once sublimed");
		assertTrue(VelorumFrostRules.birthGlint(0.5F) > VelorumFrostRules.birthGlint(4.0F), "born glinting");
	}

	@Test
	void clustersHaveMassAndTheCentreStandsTallest() {
		for (int seed = 0; seed < 30; seed++) {
			List<Shard> cluster = VelorumFrostRules.cluster(seed, 1.2D, 4, 0.45D, 4);
			assertEquals(4, cluster.size());
			for (int i = 1; i < cluster.size(); i++) assertTrue(cluster.get(i).height() < cluster.get(0).height());
			assertEquals(0.17D * Math.sqrt(1.2D), cluster.get(0).radius(), 1.0E-9D, "girth goes with the square root of the scale");
			List<Shard> leaned = VelorumFrostRules.leaned(cluster, 1.0D, 0.0D, 0.3D);
			assertEquals(cluster.get(0).leanX() + 0.3D, leaned.get(0).leanX(), 1.0E-9D);
		}
		assertNotEquals(VelorumFrostRules.cluster(1, 1.0D, 3, 0.5D, 4), VelorumFrostRules.cluster(2, 1.0D, 3, 0.5D, 4));
	}

	@Test
	void chipsArcLandSkitterAndFade() {
		boolean landed = false;
		for (int i = 0; i < 8; i++) {
			assertNull(VelorumFrostRules.chip(5, i, 0.0F, 1.2D), "nothing before the burst");
			assertNull(VelorumFrostRules.chip(5, i, VelorumFrostRules.CHIP_LIFE + 2.0F, 1.2D), "gone after its life");
			double[] chip = VelorumFrostRules.chip(5, i, VelorumFrostRules.CHIP_FLIGHT + 3.5F, 1.2D);
			if (chip != null) {
				landed = true;
				assertEquals(0.0D, chip[1], 1.0E-9D, "on the floor after its flight");
				assertTrue(Math.hypot(chip[0], chip[2]) <= 1.2D * 1.2D);
			}
		}
		assertTrue(landed);
	}

	@Test
	void theClocksOfIciclesFoldsWavesAndFills() {
		assertEquals(VelorumFrostRules.ICICLE_HEIGHT, VelorumFrostRules.icicleDrop(10.0F, 26), 1.0E-9D, "it hangs");
		assertEquals(0.0D, VelorumFrostRules.icicleDrop(26.0F, 26), 1.0E-9D, "it lands at the commit");
		double previous = Double.MAX_VALUE;
		for (float t = 22.0F; t <= 26.0F; t += 0.25F) {
			double y = VelorumFrostRules.icicleDrop(t, 26);
			assertTrue(y <= previous, "falling");
			previous = y;
		}
		assertTrue(VelorumFrostRules.foldRise(30.0F, 32) >= 0.99F && VelorumFrostRules.foldRise(4.0F, 32) < 0.2F);
		assertEquals(0.0F, VelorumFrostRules.foldSink(-1.0F));
		assertEquals(1.0F, VelorumFrostRules.foldSink(VelorumFrostRules.FOLD_SINK), 1.0E-6F);
		assertTrue(VelorumFrostRules.darkWave(VelorumFrostRules.DARK_WAVE + 1.0F, 16.0D) < 0.0D);
		assertTrue(VelorumFrostRules.darkWave(9.0F, 16.0D) > VelorumFrostRules.darkWave(2.0F, 16.0D));
		assertTrue(VelorumFrostRules.shockRadius(VelorumFrostRules.SHOCK, 4.0D) < 0.0D);
		assertEquals(0.0F, VelorumFrostRules.fill(0.0D), 1.0E-6F);
		assertEquals(1.0F, VelorumFrostRules.fill(VelorumFrostRules.WELL), 1.0E-6F);
		assertEquals(1.0F, VelorumFrostRules.slashFade(2.0F, 6.0F));
		assertEquals(0.0F, VelorumFrostRules.slashFade(6.0F + VelorumFrostRules.SLASH_HOLD, 6.0F));
		assertEquals(1.0F, VelorumFrostRules.shellGrow(VelorumFrostRules.SHELL_GROW), 1.0E-6F);
		for (int salt = 0; salt < 500; salt++) {
			double unit = VelorumFrostRules.unit(17, salt);
			assertTrue(unit >= 0.0D && unit < 1.0D);
		}
	}
}
