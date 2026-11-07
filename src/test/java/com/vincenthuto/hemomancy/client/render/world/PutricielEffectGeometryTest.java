package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Front;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.PutricielGroundGeometry.Paint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielMove;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Putriciel's visual pass 2 geometry writes whole quads in the material encodings it draws with, stays within a
 * frame's budget, and draws nothing when its timing says so: floor patches and trenches keep the ground shader's shape
 * tiles, the colony stops short of the safe cells, growths are hidden until they break out and gone once withered,
 * and spores, ooze and fire stay bounded and expire.
 */
class PutricielEffectGeometryTest {
	private static final Vec3 CAMERA = new Vec3(12, 66, -10), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);
	private static final Ground GROUND = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
	private static final Vec3 ORIGIN = new Vec3(10.0D, 64.0D, -4.0D);

	@Test
	void patchesAndTrenchesKeepTheirShaderTiles() {
		PutricielGroundGeometry.begin();
		Capture mat = new Capture();
		PutricielGroundGeometry.sector(new PoseStack(), mat, GROUND, 0.0D, 12.0D, -180.0D, 180.0D, Front.from(ORIGIN, 6.0D, 1.0D),
				new Paint(PutricielGroundGeometry.MAT, 0.5F, 0.5F, 1.0F), 3);
		PutricielGroundGeometry.rect(new PoseStack(), mat, GROUND, 0.0D, 8.0D, -1.5D, 1.5D, Front.whole(ORIGIN),
				new Paint(PutricielGroundGeometry.MAT, 0.5F, 0.5F, 1.0F), 4);
		mat.check(8000);
		assertTrue(mat.vertices > 0);
		assertEquals(Set.of(PutricielGroundGeometry.MAT), mat.shapes, "every patch decodes as the colonised floor");

		Capture trench = new Capture();
		List<Vec3> lane = List.of(ORIGIN, ORIGIN.add(0.0D, 0.0D, 6.0D), ORIGIN.add(1.0D, 0.0D, 9.0D));
		PutricielGroundGeometry.strip(new PoseStack(), trench, GROUND, lane, 0.8D, 1.0D, 1.0F,
				new Paint(PutricielGroundGeometry.TRENCH, 1.0F, 0.5F, 1.0F), 5);
		PutricielGroundGeometry.ring(new PoseStack(), trench, GROUND, 18.0D, 0.3D, 1.0D, 1.0F,
				new Paint(PutricielGroundGeometry.TRENCH, 1.0F, 0.5F, 1.0F), 6);
		trench.check(4000);
		assertTrue(trench.vertices > 0);
		assertEquals(Set.of(PutricielGroundGeometry.TRENCH), trench.shapes, "every strip decodes as a trench, even round the 18-block ring");

		Capture fire = new Capture();
		PutricielGroundGeometry.sector(new PoseStack(), fire, GROUND, 0.0D, 6.0D, -180.0D, 180.0D, Front.from(ORIGIN, 3.0D, 1.0D),
				new Paint(PutricielGroundGeometry.SCORCH, 0.9F, 0.3F, 1.0F), 7);
		PutricielGroundGeometry.strip(new PoseStack(), fire, GROUND, lane, 1.0D, 0.5D, 1.0F,
				new Paint(PutricielGroundGeometry.FURROW, 0.9F, 0.3F, 1.0F), 8);
		assertEquals(Set.of(PutricielGroundGeometry.SCORCH, PutricielGroundGeometry.FURROW), fire.shapes);

		Capture none = new Capture();
		PutricielGroundGeometry.sector(new PoseStack(), none, GROUND, 0.0D, 6.0D, -180.0D, 180.0D, Front.whole(ORIGIN),
				new Paint(PutricielGroundGeometry.MAT, 1.0F, 1.0F, 0.0F), 3);
		PutricielGroundGeometry.strip(new PoseStack(), none, GROUND, lane, 0.8D, 0.0D, 1.0F,
				new Paint(PutricielGroundGeometry.TRENCH, 1.0F, 1.0F, 1.0F), 3);
		assertEquals(0, none.vertices, "an unrevealed patch or an unopened trench draws nothing");
	}

	@Test
	void theColonySpreadsFromItsStartAndStopsShortOfTheSafeGround() {
		Capture spread = new Capture();
		PutricielGroundGeometry.sector(new PoseStack(), spread, GROUND, 0.0D, 10.0D, -180.0D, 180.0D, Front.from(ORIGIN, 4.0D, 1.0D),
				new Paint(PutricielGroundGeometry.MAT, 0.0F, 0.0F, 1.0F), 3);
		for (int i = 0; i < spread.reds.size(); i++) {
			double distance = Math.hypot(spread.points.get(i).x + CAMERA.x - ORIGIN.x, spread.points.get(i).z + CAMERA.z - ORIGIN.z);
			if (distance < 2.5D) assertEquals(255, spread.reds.get(i), "taken behind the front");
			if (distance > 5.5D) assertEquals(0, spread.reds.get(i), "untouched ahead of it");
		}
		long safe = PutricielGroundGeometry.cell(10, -4);
		Capture cells = new Capture();
		PutricielGroundGeometry.cells(new PoseStack(), cells, GROUND, 3.0D, cell -> cell != safe, Front.whole(ORIGIN),
				new Paint(PutricielGroundGeometry.MAT, 0.5F, 0.5F, 1.0F), 9);
		Capture all = new Capture();
		PutricielGroundGeometry.cells(new PoseStack(), all, GROUND, 3.0D, cell -> true, Front.whole(ORIGIN),
				new Paint(PutricielGroundGeometry.MAT, 0.5F, 0.5F, 1.0F), 9);
		assertEquals(all.vertices - 4, cells.vertices, "the safe cell is left bare");
		assertTrue(cells.reds.stream().anyMatch(red -> red < 60), "the colony thins toward the safe cell's corners");
	}

	@Test
	void growthsBreakOutStandAndWitherAway() {
		var caps = PutricielBloomRules.rosette(3, 0.9D, 6);
		Capture early = new Capture();
		PutricielGrowthGeometry.rosette(new PoseStack(), early, Vec3.ZERO, caps, 0.0F, 30.0F, 100.0D, PutricielGrowthGeometry.FRESH, 3);
		PutricielGrowthGeometry.sac(new PoseStack(), early, Vec3.ZERO, 0.3D, 0.5D, 0.0F, 1.0F, PutricielGrowthGeometry.SAC_SKIN, 3);
		PutricielGrowthGeometry.root(new PoseStack(), early, List.of(Vec3.ZERO, UP), new double[]{0.1D, 0.05D}, 0.0D, 1.0F,
				PutricielGrowthGeometry.BARK, 3);
		assertEquals(0, early.vertices, "nothing stands before it breaks out");

		Capture standing = new Capture();
		PutricielGrowthGeometry.rosette(new PoseStack(), standing, Vec3.ZERO, caps, 12.0F, 30.0F, 100.0D, PutricielGrowthGeometry.FRESH, 3);
		standing.check(1200);
		assertTrue(standing.vertices > 0);
		assertTrue(standing.maxHeight > 0.2D && standing.maxHeight < 1.0D, "a rosette stands about its scale: " + standing.maxHeight);
		assertTrue(standing.minHeight < 0.0D, "its knot is rooted under the floor");
		assertTrue(standing.shapes.containsAll(Set.of(PutricielGrowthGeometry.CAP, PutricielGrowthGeometry.GILL, PutricielGrowthGeometry.ROOT)),
				"zoned tops, gilled undersides and a knot: " + standing.shapes);

		Capture sac = new Capture();
		PutricielGrowthGeometry.sac(new PoseStack(), sac, Vec3.ZERO, 0.3D, 0.5D, 1.0F, 1.0F, PutricielGrowthGeometry.SAC_SKIN, 3);
		sac.check(400);
		assertEquals(Set.of(PutricielGrowthGeometry.SAC), sac.shapes);

		Capture root = new Capture();
		List<Vec3> spine = new ArrayList<>();
		double[] radii = new double[8];
		for (int i = 0; i < 8; i++) {
			spine.add(new Vec3(i * 0.3D, Math.sin(i / 7.0D * Math.PI) * 0.7D, 0.0D));
			radii[i] = 0.1D;
		}
		PutricielGrowthGeometry.root(new PoseStack(), root, spine, radii, 1.0D, 1.0F, PutricielGrowthGeometry.BARK, 3);
		root.check(400);
		assertTrue(root.maxHeight > 0.6D, "the root arches out of the floor");

		Capture gone = new Capture();
		PutricielGrowthGeometry.rosette(new PoseStack(), gone, Vec3.ZERO, caps, 30.0F + PutricielBloomRules.WITHER + 3.0F, 30.0F, 100.0D,
				PutricielGrowthGeometry.FRESH, 3);
		PutricielGrowthGeometry.sac(new PoseStack(), gone, Vec3.ZERO, 0.3D, 0.5D, 1.0F,
				PutricielBloomRules.tear(PutricielBloomRules.BURST + PutricielBloomRules.SHRIVEL + 1.0F), PutricielGrowthGeometry.SAC_SKIN, 3);
		assertEquals(0, gone.vertices, "withered and shrivelled away");
	}

	@Test
	void sporesOozeAndFireStayBoundedAndExpire() {
		Capture spores = new Capture();
		for (float age : new float[]{0.0F, 3.0F, 10.0F}) {
			PutricielSporeGeometry.burst(new PoseStack(), spores, Vec3.ZERO, RIGHT, UP, age, 1.2D, 7, 0.3F, 1.0F, 3);
			PutricielSporeGeometry.splash(new PoseStack(), spores, Vec3.ZERO, Vec3.ZERO, RIGHT, UP, age, 1.4D, 2.2D, 1.0F, 3);
		}
		PutricielSporeGeometry.curtain(new PoseStack(), spores, Vec3.ZERO, 10.0D, 2.6D, Vec3.ZERO, RIGHT, UP, 100.0D, 0.3F, 1.0F, 3);
		List<Vec3> path = new ArrayList<>();
		for (int i = 0; i < 12; i++) path.add(new Vec3(i * 0.3D, 1.5D, Math.sin(i * 0.4D)));
		PutricielSporeGeometry.wake(new PoseStack(), spores, path, Vec3.ZERO, RIGHT, UP, 100.0D, 0.3D, 0.2F, 1.0F, 3);
		PutricielSporeGeometry.reach(new PoseStack(), spores, path, Vec3.ZERO, RIGHT, UP, 100.0D, 0.17D, 1.0F, 3);
		PutricielSporeGeometry.coneCloud(new PoseStack(), spores, Vec3.ZERO, new double[]{0.0D, 1.0D}, 7.0D, 60.0D, RIGHT, UP, 100.0D, 18, 0.3F,
				1.0F, 3);
		spores.check(6000);
		assertTrue(spores.vertices > 0);
		assertTrue(spores.shapes.containsAll(Set.of(PutricielSporeGeometry.PUFF, PutricielSporeGeometry.OOZE, PutricielSporeGeometry.CURTAIN)));
		assertTrue(spores.maxHeight <= 2.6D + 0.8D, "the spore wave's curtain stands at the standing band, its puffs just over");

		Capture spent = new Capture();
		PutricielSporeGeometry.burst(new PoseStack(), spent, Vec3.ZERO, RIGHT, UP, PutricielBloomRules.PUFF_LIFE, 1.2D, 7, 0.3F, 1.0F, 3);
		PutricielSporeGeometry.splash(new PoseStack(), spent, Vec3.ZERO, Vec3.ZERO, RIGHT, UP, 14.5F, 1.4D, 2.2D, 1.0F, 3);
		PutricielSporeGeometry.puff(new PoseStack(), spent, Vec3.ZERO, RIGHT, UP, 1.0D, 1.0F, 0.0F, 0.0F, 1.0F, 3);
		PutricielFlameGeometry.sparks(new PoseStack(), spent, Vec3.ZERO, RIGHT, RIGHT, UP, 14.5F, 2.0D, 10, 3, 1.0F);
		assertEquals(0, spent.vertices, "spent bursts, splashes, sparks and a thinned-out puff draw nothing");

		Capture fire = new Capture();
		PutricielFlameGeometry.ring(new PoseStack(), fire, Vec3.ZERO, 8.0D, 0.4D, 0.8D, 0.18D, RIGHT, UP, 100.0D, 3, 1.0F);
		PutricielFlameGeometry.row(new PoseStack(), fire, Vec3.ZERO, new Vec3(0, 0, 12), 0.38D, 2.1D, f -> 0.55D + 0.45D * f, Vec3.ZERO,
				RIGHT, UP, 100.0D, 3, 1.0F);
		PutricielFlameGeometry.column(new PoseStack(), fire, Vec3.ZERO, 0.6D, 3.4D, RIGHT, UP, 100.0D, 3, 1.0F);
		PutricielFlameGeometry.sparks(new PoseStack(), fire, Vec3.ZERO, Vec3.ZERO, RIGHT, UP, 4.0F, 4.0D, 18, 3, 1.0F);
		fire.check(9000);
		assertTrue(fire.vertices > 0);
	}

	@Test
	void aTongueStandsItsHeightAndLicks() {
		for (double time : new double[]{0.0D, 37.0D, 113.0D}) {
			Capture tongue = new Capture();
			PutricielFlameGeometry.tongue(new PoseStack(), tongue, new Vec3(0.0D, 0.0D, 5.0D), new Vec3(0.1D, 0.0D, 0.0D), 0.8D, 0.1D, time, 3, 1.0F);
			tongue.check(64);
			assertTrue(tongue.maxHeight > 0.7D && tongue.maxHeight < 0.95D, "a tongue stands about its height: " + tongue.maxHeight);
		}
		Capture none = new Capture();
		PutricielFlameGeometry.tongue(new PoseStack(), none, Vec3.ZERO, Vec3.ZERO, 0.02D, 0.1D, 0.0D, 3, 1.0F);
		assertEquals(0, none.vertices, "a guttered tongue draws nothing");
	}

	@Test
	void theStyleClaimsEveryMoveSoTheKitTintsNeverShow() {
		PutricielTelegraphStyle style = new PutricielTelegraphStyle();
		for (PutricielMove move : PutricielMove.values()) {
			TelegraphDraw draw = new TelegraphDraw(null, move, null, null, null, null, 0.0F, 0.0F, null, 0.0F, 0.0F, 0.0F);
			assertTrue(style.paint(draw, null), move + " is the pass's");
		}
	}

	// ── the revision (2026-11-06): the purifier on fire and the rot sludge ──

	private static PutricielSludgeGeometry.State rotPool(float age) {
		return new PutricielSludgeGeometry.State(PutricielBloomRules.sludge(age), -1.0F, null, null, age);
	}

	private static PutricielSludgeGeometry.State burningPool(float b, Vec3 caught) {
		return new PutricielSludgeGeometry.State(PutricielBloomRules.sludgeBurn(b), b, caught, ORIGIN.add(-6.0D, 0.0D, 0.0D), b);
	}

	@Test
	void aSludgePoolStandsOnTheFloorWithBody() {
		double floor = ORIGIN.y - CAMERA.y;
		Vec3 centre = ORIGIN.subtract(CAMERA);
		PutricielSludgeGeometry.Pool pool = PutricielSludgeGeometry.Pool.disc(ORIGIN, 2.0D, 17);
		for (float age : new float[]{PutricielBloomRules.SLUDGE_FORM * 0.7F, 100.0F}) {
			Capture mesh = new Capture();
			PutricielSludgeGeometry.mesh(new PoseStack(), mesh, pool, rotPool(age), CAMERA, 100.0D, 0.0F);
			mesh.check(PutricielSludgeGeometry.RINGS * PutricielSludgeGeometry.SEGMENTS * 4);
			assertEquals(Set.of(PutricielSludgeGeometry.SLUDGE), mesh.shapes, "the growth shader's sludge shape");
			assertTrue(mesh.minHeight >= floor - 1.0E-4D && mesh.maxHeight <= floor + 0.25D, "between the floor and 0.25: " + mesh.maxHeight);
			assertTrue(mesh.maxHeight > floor + 0.15D, "thick at its centre, a mesh with body, not a decal");
			double reach = 0.0D;
			for (Vec3 point : mesh.points) reach = Math.max(reach, Math.hypot(point.x - centre.x, point.z - centre.z));
			assertTrue(reach >= 2.0D * 0.8D && reach <= 2.0D * PutricielBloomRules.LOBE_MAX + 1.0E-4D, "out to its lobed rim: " + reach);
		}
		for (int a = 0; a < 360; a++) {
			double rim = pool.rim(Math.toRadians(a), 1.0D);
			assertTrue(rim >= 0.8D * 2.0D, "never under 0.8 of its radius");
		}
		Capture runnel = new Capture();
		PutricielSludgeGeometry.mesh(new PoseStack(), runnel, PutricielSludgeGeometry.Pool.runnel(ORIGIN, 1.0D, 0.0D, 4.0D, 0.55D, 5), rotPool(100.0F),
				CAMERA, 100.0D, 0.0F);
		assertTrue(runnel.vertices > 0);
		runnel.check(4000);
		assertTrue(runnel.minHeight >= floor - 1.0E-4D && runnel.maxHeight <= floor + 0.25D);
		Capture none = new Capture();
		PutricielSludgeGeometry.mesh(new PoseStack(), none, pool, rotPool(0.0F), CAMERA, 100.0D, 0.0F);
		assertEquals(0, none.vertices, "it spreads out of the splash, from nothing");
		// the floor read rides the pool's surface
		PutricielSludgeField field = new PutricielSludgeField(List.of(new PutricielSludgeField.Live(pool, rotPool(100.0F),
				com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielHazard.ROT_SLUDGE)));
		assertTrue(field.lift(ORIGIN.x, ORIGIN.z) > PutricielBloomRules.SLUDGE_DEPTH, "over the surface at the centre");
		assertEquals(0.0D, field.lift(ORIGIN.x + 4.0D, ORIGIN.z), "and the floor away from it");
	}

	@Test
	void aSludgePoolBurnsFromItsCatchPointByAge() {
		PutricielSludgeGeometry.Pool pool = PutricielSludgeGeometry.Pool.disc(ORIGIN, 2.0D, 17);
		Vec3 caught = ORIGIN.add(2.0D, 0.0D, 0.0D);
		Capture catching = new Capture();
		PutricielSludgeGeometry.mesh(new PoseStack(), catching, pool, burningPool(8.0F, caught), CAMERA, 100.0D, 0.0F);
		assertTrue(catching.reds.stream().anyMatch(red -> red > 20), "charring behind the front at the catch point");
		assertTrue(catching.reds.stream().anyMatch(red -> red == 0), "untouched ahead of it");
		Capture rot = new Capture(), boiled = new Capture();
		PutricielSludgeGeometry.mesh(new PoseStack(), rot, pool, rotPool(100.0F), CAMERA, 100.0D, 0.0F);
		PutricielSludgeGeometry.mesh(new PoseStack(), boiled, pool, burningPool(PutricielBloomRules.SLUDGE_BOIL_END, caught), CAMERA, 100.0D, 0.0F);
		assertTrue(boiled.maxHeight < rot.maxHeight - 0.05D, "the boil thins it");
		assertTrue(boiled.reds.stream().allMatch(red -> red >= Math.round(PutricielBloomRules.BOIL_CHAR * 255.0F) - 2), "a crust over all of it");
		Capture gone = new Capture();
		PutricielSludgeGeometry.mesh(new PoseStack(), gone, pool, burningPool(PutricielBloomRules.SLUDGE_BURN, caught), CAMERA, 100.0D, 0.0F);
		assertEquals(0, gone.vertices, "crumbled onto the ash by 50");
		Capture fire = new Capture(), spores = new Capture();
		for (float b = 0.0F; b < 60.0F; b += 3.0F) {
			PutricielSludgeGeometry.air(new PoseStack(), spores, fire, pool, burningPool(b, caught), CAMERA, RIGHT, UP, 100.0D + b, 36);
		}
		assertTrue(fire.vertices > 0, "it burns with tongues");
		Capture out = new Capture();
		PutricielSludgeGeometry.air(new PoseStack(), out, out, pool, burningPool(PutricielBloomRules.SLUDGE_BURN + 8.0F, caught), CAMERA, RIGHT, UP,
				100.0D, 36);
		assertEquals(0, out.vertices, "the cooling ash carries no fire");
	}

	private static PutricielBodyFireGeometry.Frame body(Vec3 velocity) {
		Vec3 at = new Vec3(0.0D, -1.0D, 6.0D);
		return new PutricielBodyFireGeometry.Frame(at.add(-1.2D, 0.0D, 0.2D), at.add(-1.2D, 3.4D, 0.2D),
				new Vec3[]{at.add(-1.5D, 2.2D, 0.2D), at.add(-1.7D, 2.0D, 0.0D)}, at.add(0.0D, 1.8D, 0.0D), at.add(0.9D, 2.4D, 0.7D),
				at.add(0.0D, 3.0D, 0.15D), new Vec3(0, 0, -1), new Vec3(1, 0, 0), at.y, velocity, CAMERA);
	}

	@Test
	void theBodyFireStaysInItsBudgetAndNeverBurnsInTheRot() {
		PutricielBodyFireGeometry.Frame still = body(Vec3.ZERO);
		var sites = PutricielBodyFireGeometry.fallback(still, Vec3.ZERO, 7);
		assertEquals(70, sites.size(), "about seventy anchors");
		for (var region : com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Region.values()) {
			assertTrue(sites.stream().anyMatch(site -> site.region() == region), "fire on the " + region);
		}
		for (float time = 0.0F; time < 400.0F; time += 53.0F) {
			Capture rot = new Capture();
			PutricielBodyFireGeometry.draw(new PoseStack(), rot, rot, sites, still,
					com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment.of(
							com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage.ROT, time, 0.9F),
					100.0D, time, RIGHT, UP, new PutricielBodyFireGeometry.Budget(), null, false, 1.0F, null);
			assertEquals(0, rot.vertices, "nothing drawn for a body in the rot");
		}
		// heat without flame at the Ignition's tick 5: soot only
		var ignition = com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment.of(
				com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage.IGNITING, 5.0F, 1.0F)
				.moving(PutricielMove.IGNITION, 5.0F, 30, 6);
		Capture seep = new Capture();
		PutricielBodyFireGeometry.draw(new PoseStack(), seep, seep, sites, still, ignition, 100.0D, 5.0F, RIGHT, UP,
				new PutricielBodyFireGeometry.Budget(), null, false, 1.0F, null);
		assertTrue(seep.vertices <= PutricielBodyFireGeometry.SMOKE * 4, "no flame before the first embers: " + seep.vertices);
		// the hottest frame: a swept staff at the commit, every anchor moving fast enough to tear tongues off
		Vec3 fast = new Vec3(0.5D, 0.0D, 0.0D);
		PutricielBodyFireGeometry.Frame swinging = body(fast);
		var moving = PutricielBodyFireGeometry.fallback(swinging, fast, 7);
		var sweep = com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment.of(
				com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage.BURNING, 100.0F, 1.0F)
				.moving(PutricielMove.PURGING_SWEEP, 20.0F, 20, 6);
		Capture flame = new Capture(), ash = new Capture();
		PutricielBodyFireGeometry.draw(new PoseStack(), flame, ash, moving, swinging, sweep, 100.0D, 400.0F, RIGHT, UP,
				new PutricielBodyFireGeometry.Budget(), null, false, 1.0F, null);
		// a tongue is two bent ribbons of six quads; smoke, beds and glows are single cards
		int cards = PutricielBodyFireGeometry.SMOKE + 3 * moving.size() + 12;
		flame.check(PutricielBodyFireGeometry.TONGUES * 48 + cards * 4);
		assertTrue(flame.vertices > 60 * 48, "real flame on the body, not a few lights: " + flame.vertices);
		ash.check(PutricielBodyFireGeometry.SHED * 4);
		// and the steady burn sheds ash
		Capture steady = new Capture(), flakes = new Capture();
		PutricielBodyFireGeometry.draw(new PoseStack(), steady, flakes, sites, still,
				com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment.of(
						com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage.BURNING, 100.0F, 0.8F),
				100.0D, 400.0F, RIGHT, UP, new PutricielBodyFireGeometry.Budget(), null, false, 1.0F, null);
		assertTrue(flakes.vertices > 0 && flakes.shapes.equals(Set.of(PutricielSporeGeometry.ASH)), "grey ash flakes off the back and shoulders");
		// the Embering is out by 45
		Capture out = new Capture();
		PutricielBodyFireGeometry.draw(new PoseStack(), out, out, sites, still,
				com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.Moment.of(
						com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielStage.EMBERING, 45.0F, 0.0F),
				100.0D, 400.0F, RIGHT, UP, new PutricielBodyFireGeometry.Budget(), null, false, 1.0F, null);
		assertEquals(0, out.vertices);
	}

	@Test
	void anAnchoredTongueLeansAndStandsItsLength() {
		Capture up = new Capture();
		PutricielFlameGeometry.anchored(new PoseStack(), up, new Vec3(0, 0, 5), new Vec3(0, 1, 0), 0.8D, 0.08D, 0.7F, RIGHT, UP, 37.0D, 3, 1.0F);
		up.check(128);
		assertTrue(up.maxHeight > 0.7D && up.maxHeight < 0.95D, "an upright tongue stands about its length: " + up.maxHeight);
		Capture leaning = new Capture();
		Vec3 lean = com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielBodyFireRules.clampLean(new Vec3(-1, 0.2, 0).normalize());
		PutricielFlameGeometry.anchored(new PoseStack(), leaning, new Vec3(0, 0, 5), lean, 0.8D, 0.08D, 0.7F, RIGHT, UP, 37.0D, 3, 1.0F);
		assertTrue(leaning.maxHeight < up.maxHeight, "a dragged tongue leans over");
		assertTrue(leaning.points.stream().anyMatch(point -> point.x < -0.5D), "back along the swing");
		Capture torn = new Capture();
		PutricielFlameGeometry.tearOff(new PoseStack(), torn, Vec3.ZERO, new Vec3(0.3, 0, 0), PutricielFlameGeometry.TEAR_LIFE, 0.4D, RIGHT, UP, 0.0D,
				3, 1.0F);
		assertEquals(0, torn.vertices, "a tear-off is gone after its life");
	}

	private static final class Capture implements VertexConsumer {
		int vertices;
		double maxHeight = Double.NEGATIVE_INFINITY, minHeight = Double.POSITIVE_INFINITY;
		final Set<Integer> shapes = new HashSet<>();
		final List<Integer> reds = new ArrayList<>();
		final List<Vec3> points = new ArrayList<>();

		public VertexConsumer addVertex(float x, float y, float z) {
			assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
			maxHeight = Math.max(maxHeight, y);
			minHeight = Math.min(minHeight, y);
			points.add(new Vec3(x, y, z));
			vertices++;
			return this;
		}

		public VertexConsumer setColor(int r, int g, int b, int a) {
			for (int color : new int[]{r, g, b, a}) assertTrue(color >= 0 && color <= 255);
			reds.add(r);
			return this;
		}

		public VertexConsumer setUv(float u, float v) {
			assertTrue(Float.isFinite(u) && Float.isFinite(v) && u >= 0.0F && v >= 0.0F);
			shapes.add((int) Math.floor(u / 2.0F));
			assertTrue((int) Math.floor(v / 2.0F) < MaterialQuads.SEED_TILES + 1, "a seed tile the shaders know");
			return this;
		}

		public VertexConsumer setUv1(int u, int v) {
			return this;
		}

		public VertexConsumer setUv2(int u, int v) {
			return this;
		}

		public VertexConsumer setNormal(float x, float y, float z) {
			return this;
		}

		void check(int max) {
			assertEquals(0, vertices % 4);
			assertTrue(vertices <= max, "Vertices: " + vertices);
		}
	}
}
