package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Field;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Look;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Velorum's visual pass 2 geometry writes whole quads in the material encodings his shaders decode, stays within
 * a frame's budget and draws nothing when its timing says so: seams and rime keep their shape tiles, the trench writes
 * only walls and a bed, ice is hidden until it breaks out and gone once it sublimes, the lantern's light is one quad a
 * ray, and a cut through the air is one quad a step.
 */
class VelorumEffectGeometryTest {
	private static final Vec3 CAMERA = new Vec3(12, 70, -10);
	private static final Look FULL = new Look(1.0F, 1.0F, 1.0F, 1.0F, 1.3F, 1.0F);

	@Test
	void seamsAndRimeKeepTheirShaderTiles() {
		Ground ground = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
		VelorumFrostGeometry.begin();
		Capture seams = new Capture();
		VelorumFrostGeometry.seams(new PoseStack(), seams, ground, VelorumFrostRules.radial(2.5D, 10.0D, 16, 0.0D, 4), FULL, 1.0F, 4);
		VelorumFrostGeometry.seams(new PoseStack(), seams, ground, VelorumFrostRules.ring(14.0D, 5), FULL, 1.0F, 5);
		VelorumFrostGeometry.seams(new PoseStack(), seams, ground, VelorumFrostRules.lips(0.6D, 14.0D, 0.45D, 6), FULL, 1.0F, 6);
		seams.check(24000);
		assertTrue(seams.vertices > 0);
		assertEquals(Set.of(VelorumFrostGeometry.SEAM), seams.shapes, "every seam quad decodes as a seam");

		Capture rime = new Capture();
		VelorumFrostGeometry.annulus(new PoseStack(), rime, ground, VelorumFrostGeometry.RIME, 2.5D, 10.0D, -180.0D, 180.0D, Field.of(1.0F),
				0.5F, 0.5F, Field.of(1.0F), 7);
		VelorumFrostGeometry.rect(new PoseStack(), rime, ground, VelorumFrostGeometry.RIME, -7.5D, 7.5D, -7.5D, 7.5D, Field.of(0.6F), 0.2F,
				0.0F, Field.of(1.0F), 8);
		rime.check(8000);
		assertEquals(Set.of(VelorumFrostGeometry.RIME), rime.shapes, "every patch decodes as rime");

		Capture none = new Capture();
		VelorumFrostGeometry.seams(new PoseStack(), none, ground, VelorumFrostRules.radial(2.5D, 10.0D, 16, 0.0D, 4), Look.NONE, 1.0F, 4);
		VelorumFrostGeometry.annulus(new PoseStack(), none, ground, VelorumFrostGeometry.RIME, 0.0D, 6.0D, -180.0D, 180.0D, Field.of(0.0F),
				0.0F, 0.0F, Field.of(1.0F), 3);
		assertEquals(0, none.vertices, "an unrevealed footprint, and rime with no frost yet, draw nothing");
		Capture early = new Capture();
		VelorumFrostGeometry.seams(new PoseStack(), early, ground, VelorumFrostRules.radial(2.5D, 10.0D, 16, 0.0D, 4),
				new Look(0.0F, 0.2F, 0.0F, 0.1F, 0.5F, 1.0F), 1.0F, 4);
		assertEquals(0, early.vertices, "no seam shows before it starts running");
	}

	@Test
	void theTrenchWritesOnlyWallsAndABed() {
		Ground lane = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
		Capture trench = new Capture();
		VelorumFrostGeometry.trench(new PoseStack(), trench, lane, 0.6D, 12.0D, 0.45D, VelorumFrostRules.TRENCH_DEPTH, true, true, 0.6F, 0.0F,
				0.3F, 1.0F, 3);
		trench.check(400);
		assertTrue(trench.vertices > 0);
		assertTrue(trench.shapes.contains(VelorumFrostGeometry.BED) && trench.shapes.contains(VelorumFrostGeometry.WALL));
		assertTrue(Set.of(VelorumFrostGeometry.BED, VelorumFrostGeometry.WALL).containsAll(trench.shapes));
		assertTrue(trench.maxY < 64.0D - CAMERA.y + 0.05D, "it lies on the floor plane; the floor hides nothing it draws");
		Capture hidden = new Capture();
		VelorumFrostGeometry.trench(new PoseStack(), hidden, lane, 0.6D, 12.0D, 0.45D, VelorumFrostRules.TRENCH_DEPTH, true, true, 0.6F, 0.0F,
				0.3F, 0.0F, 3);
		assertEquals(0, hidden.vertices, "a faded trench draws nothing");
	}

	@Test
	void iceBreaksOutStandsAndSublimes() {
		var shards = VelorumFrostRules.cluster(3, 1.2D, 3, 0.42D, 4);
		Capture unborn = new Capture();
		VelorumIceGeometry.cluster(new PoseStack(), unborn, Vec3.ZERO, shards, 0.0F, 20.0F, VelorumIceGeometry.BLACK_ICE,
				VelorumIceGeometry.SHARD, true, 3);
		assertEquals(0, unborn.vertices, "nothing stands before it breaks out");

		Capture standing = new Capture();
		VelorumIceGeometry.cluster(new PoseStack(), standing, Vec3.ZERO, shards, 8.0F, 20.0F, VelorumIceGeometry.BLACK_ICE,
				VelorumIceGeometry.SHARD, true, 3);
		VelorumIceGeometry.chips(new PoseStack(), standing, Vec3.ZERO, 3.0F, 1.2D, 6, VelorumIceGeometry.CRYSTAL, 3);
		standing.check(1600);
		assertTrue(standing.maxY > 1.0D && standing.maxY < 1.5D, "the central blade stands about its scale: " + standing.maxY);
		assertTrue(standing.shapes.contains(VelorumIceGeometry.SHARD) && standing.shapes.contains(VelorumIceGeometry.CHIP));

		Capture sublimed = new Capture();
		VelorumIceGeometry.cluster(new PoseStack(), sublimed, Vec3.ZERO, shards, 20.0F + VelorumFrostRules.CRUMBLE + 3.0F, 20.0F,
				VelorumIceGeometry.BLACK_ICE, VelorumIceGeometry.SHARD, true, 3);
		assertEquals(0, sublimed.vertices, "gone once it sublimes");

		Capture icicle = new Capture();
		VelorumIceGeometry.shard(new PoseStack(), icicle, new Vec3(0, 6, 0), new Vec3(0, -1, 0), 1.6D, 0.16D, 0.9D, 0.0D, 6, 1.0F,
				VelorumIceGeometry.CRYSTAL, VelorumIceGeometry.SHARD, 4);
		assertEquals(6 * 3 * 4, icicle.vertices, "six facets, three stages");
		assertTrue(icicle.minY > 4.3D && icicle.minY < 4.5D, "an icicle hangs point down: " + icicle.minY);
		assertEquals(VelorumIceGeometry.BLACK_ICE & 0xFFFF00, VelorumIceGeometry.withGlint(VelorumIceGeometry.BLACK_ICE, 1.0F) & 0xFFFF00);
		assertEquals(255, VelorumIceGeometry.withGlint(VelorumIceGeometry.BLACK_ICE, 1.0F) & 0xFF);
	}

	@Test
	void theLanternsLightIsOneQuadARayAndATrailIsBounded() {
		Ground ground = new Ground(0.0D, 0.0D, 1.0D, 0.0D, 64.0D, CAMERA);
		double[] reach = new double[17];
		for (int i = 0; i < reach.length; i++) reach[i] = i == 8 ? 4.0D : 20.0D;
		Capture light = new Capture();
		VelorumFrostGeometry.light(new PoseStack(), light, ground, 0.6D, reach, -25.0D, 25.0D, 20.0D, 0.5F, 0.8F, 2);
		assertEquals(16 * 4, light.vertices);
		assertEquals(Set.of(VelorumFrostGeometry.LIGHT), light.shapes);

		List<Vec3> path = new ArrayList<>();
		double[] widths = new double[16];
		for (int i = 0; i < 16; i++) {
			path.add(new Vec3(Math.cos(i * 0.2D) * 2.0D, 1.5D + i * 0.05D, Math.sin(i * 0.2D) * 2.0D));
			widths[i] = 0.02D + 0.01D * i;
		}
		Capture trail = new Capture();
		VelorumFrostGeometry.trail(new PoseStack(), trail, path, widths, 1.0F, 0.7F, 1.0F, 1.0F, 5);
		trail.check(15 * 4);
		assertEquals(Set.of(VelorumFrostGeometry.TRAIL), trail.shapes);
	}

	@Test
	void theRevisionsCruorAndDarkKeepTheirShapesAndLimbsStayInFourSegments() {
		// revision 2026-11-06: liquid dark, the floor's seams, stains and vein ridges decode as their own shapes
		Ground ground = new Ground(10.0D, -4.0D, 1.0D, 0.0D, 64.0D, CAMERA);
		Capture dark = new Capture();
		VelorumFrostGeometry.annulus(new PoseStack(), dark, ground, VelorumFrostGeometry.DARK, 0.0D, 4.5D, -180.0D, 180.0D, Field.of(1.0F), 0.0F,
				0.0F, (along, right) -> (float) Math.max(0.0D, 1.0D - Math.hypot(along, right) / 4.5D), 3);
		dark.check(8000);
		assertEquals(Set.of(VelorumFrostGeometry.DARK), dark.shapes, "a pool of liquid dark is its own shape, torn in the shader");

		Capture seams = new Capture();
		VelorumFrostGeometry.rect(new PoseStack(), seams, ground, VelorumFrostGeometry.SEAMS, -6.0D, 6.0D, -6.0D, 6.0D, Field.of(1.0F), 1.0F, 1.0F,
				Field.of(1.0F), 4);
		seams.check(8000);
		assertEquals(Set.of(VelorumFrostGeometry.SEAMS), seams.shapes);
		Capture dry = new Capture();
		VelorumFrostGeometry.rect(new PoseStack(), dry, ground, VelorumFrostGeometry.SEAMS, -6.0D, 6.0D, -6.0D, 6.0D, Field.of(0.0F), 0.0F, 0.0F,
				Field.of(1.0F), 4);
		assertEquals(0, dry.vertices, "seams no blood has reached, with no glow, draw nothing");

		Capture stain = new Capture();
		VelorumFrostGeometry.flat(new PoseStack(), stain, Vec3.ZERO, 1.2D, 0.4D, VelorumFrostGeometry.STAIN, 0, 1.0F, 5);
		assertEquals(4, stain.vertices);
		assertEquals(Set.of(VelorumFrostGeometry.STAIN), stain.shapes);

		double[] vein = {0.0D, 0.0D, 4.0D, 0.0D, 4.0D, 3.0D, 7.0D, 3.0D};
		assertEquals(10.0D, VelorumFrostGeometry.length(vein), 1.0E-9D);
		assertArrayEquals(new double[]{4.0D, 1.0D}, VelorumFrostGeometry.along(vein, 5.0D), 1.0E-9D);
		Capture ridge = new Capture();
		VelorumFrostGeometry.strip(new PoseStack(), ridge, 64.0D, vein, 6.0D, 0.15D, VelorumFrostGeometry.RIDGE, 1.0F, 0.0F, 0.0F, 0.6D, 1.0F, 1.0F,
				CAMERA, 6);
		assertEquals(2 * 4, ridge.vertices, "a ridge shown to 6 blocks covers two of its three runs");
		assertEquals(Set.of(VelorumFrostGeometry.RIDGE), ridge.shapes);

		// a shape's reaching limb: four straight segments, down off the shape and along the floor
		Capture none = new Capture();
		VelorumTelegraphPass.Scene solid = new VelorumTelegraphPass.Scene(new PoseStack(), VelorumTelegraphPass.Phase.SOLID, none, none, none,
				none, none, none, CAMERA, new Vec3(1.0D, 0.0D, 0.0D), new Vec3(0.0D, 1.0D, 0.0D), 100.0D, 0.0F, null, List.of());
		List<Vec3> limb = VelorumDarkGeometry.limb(solid, new Vec3(0.0D, 65.9D, 0.0D), new Vec3(6.0D, 64.0D, 0.0D), 64.0D, 1.0F, 1.0F, 7);
		assertEquals(5, limb.size(), "never a noodle: five joints, four straight segments");
		assertEquals(6.0D, limb.get(4).x, 1.0E-9D, "a full reach ends at its target");
		assertTrue(limb.get(4).y < 64.3D, "its hand on the floor");
		assertEquals(List.of(), VelorumDarkGeometry.limb(solid, Vec3.ZERO, new Vec3(6.0D, 0.0D, 0.0D), 0.0D, 0.0F, 1.0F, 7), "unthrown, no limb");
		assertEquals(0, none.vertices, "the limb's smoke is the floor phase's");

		// the Veins of Frost path runs out to each figure and back; each vein is read from its figure to the meeting point
		List<Vec3> path = List.of(new Vec3(0, 64, 0), new Vec3(3, 64, 0), new Vec3(3, 64, 4), new Vec3(3, 64, 0), new Vec3(0, 64, 0),
				new Vec3(-2, 64, 0), new Vec3(-2, 64, -5), new Vec3(-2, 64, 0), new Vec3(0, 64, 0));
		List<double[]> veins = VelorumTelegraphPass.veinPaths(path, List.of(new Vec3(3, 64, 4), new Vec3(-2, 64, -5)));
		assertEquals(2, veins.size());
		assertArrayEquals(new double[]{3.0D, 4.0D, 3.0D, 0.0D, 0.0D, 0.0D}, veins.get(0), 1.0E-9D);
		assertArrayEquals(new double[]{-2.0D, -5.0D, -2.0D, 0.0D, 0.0D, 0.0D}, veins.get(1), 1.0E-9D);
	}

	private static final class Capture implements VertexConsumer {
		int vertices;
		double maxY = Double.NEGATIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
		final Set<Integer> shapes = new HashSet<>();

		public VertexConsumer addVertex(float x, float y, float z) {
			assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
			maxY = Math.max(maxY, y);
			minY = Math.min(minY, y);
			vertices++;
			return this;
		}

		public VertexConsumer setColor(int r, int g, int b, int a) {
			for (int color : new int[]{r, g, b, a}) assertTrue(color >= 0 && color <= 255);
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
