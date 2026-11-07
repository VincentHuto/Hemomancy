package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Look;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Slab;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Spike;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Trench;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Hemorath's visual pass 2 geometry writes whole quads in the material encodings it draws with, stays within a
 * frame's budget, and draws nothing when its timing says so: cracks keep the fissure shader's shape tiles even along
 * the eighteen-block ring, iron is hidden until it forms, and blood streams and drains stay bounded.
 */
class HemorathEffectGeometryTest {
	private static final Vec3 CAMERA = new Vec3(12, 66, -10), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);
	private static final Look FULL = new Look(1.0F, 1.0F, 1.0F, 1.0F, 1.35F, 1.0F);

	@Test
	void cracksAndCrazeKeepTheirShaderTiles() {
		Ground ground = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
		Capture cracks = new Capture();
		HemorathFissureGeometry.begin();
		HemorathFissureGeometry.cracks(new PoseStack(), cracks, ground, HemorathFissureRules.ring(18.0D, 4), FULL, 1.0F, 4);
		HemorathFissureGeometry.cracks(new PoseStack(), cracks, ground, HemorathFissureRules.lane(14.0D, 1.0D, 4), FULL, 1.0F, 5);
		cracks.check(24000);
		assertTrue(cracks.vertices > 0);
		assertEquals(Set.of(HemorathFissureGeometry.CRACK), cracks.shapes, "every crack quad decodes as a crack");

		Capture craze = new Capture();
		HemorathFissureGeometry.craze(new PoseStack(), craze, ground, 0.0D, 14.0D, -180.0D, 180.0D, FULL, 1.0F, 6);
		HemorathFissureGeometry.crazeRect(new PoseStack(), craze, ground, -7.5D, 7.5D, -7.5D, 7.5D, FULL, 1.0F, 7);
		craze.check(6000);
		assertEquals(Set.of(HemorathFissureGeometry.CRAZE), craze.shapes, "every patch decodes as crazed floor");

		Capture none = new Capture();
		HemorathFissureGeometry.cracks(new PoseStack(), none, ground, HemorathFissureRules.disc(6.0D, 3), Look.NONE, 1.0F, 3);
		HemorathFissureGeometry.craze(new PoseStack(), none, ground, 0.0D, 6.0D, -180.0D, 180.0D, Look.NONE, 1.0F, 3);
		assertEquals(0, none.vertices, "an unrevealed or sealed footprint draws nothing");
		Capture early = new Capture();
		HemorathFissureGeometry.cracks(new PoseStack(), early, ground, HemorathFissureRules.disc(6.0D, 3),
				new Look(0.0F, 0.2F, 0.0F, 0.0F, 0.5F, 1.0F), 1.0F, 3);
		assertEquals(0, early.vertices, "no crack shows before it starts running");
	}

	@Test
	void aTrenchMouthLiesExactlyOnItsStripAndItsSlabsHideAtRest() {
		// aim along +z from (10, -4): a point's world x is 10 - right and z is -4 + along; the camera is at (12, 66, -10)
		Ground ground = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
		double half = 0.55D;
		Capture mouth = new Capture();
		HemorathFissureGeometry.trench(new PoseStack(), mouth, ground, 0.0D, 14.0D, half,
				along -> new Trench(1.0F, 0.6F, 0.9F, 1.0F, 1.0F), 5);
		mouth.check(200);
		assertTrue(mouth.vertices > 0);
		assertEquals(Set.of(HemorathFissureGeometry.TRENCH), mouth.shapes, "every mouth quad decodes as a trench");
		assertEquals(-2.0D - half, mouth.minX, 1e-4, "the mouth's edges are the strip's edges");
		assertEquals(-2.0D + half, mouth.maxX, 1e-4);
		assertEquals(6.0D, mouth.minZ, 1e-4, "from his feet");
		assertEquals(20.0D, mouth.maxZ, 1e-4, "to the strip's end");
		assertTrue(mouth.maxHeight > -2.0D && mouth.maxHeight < -1.95D, "laid on the floor");

		Capture none = new Capture();
		HemorathFissureGeometry.trench(new PoseStack(), none, ground, 0.0D, 14.0D, half, along -> Trench.NONE, 5);
		assertEquals(0, none.vertices, "an unrevealed or closed trench draws nothing");

		List<Slab> slabs = HemorathFissureRules.slabs(0.0D, 14.0D, half, 5);
		Capture rest = new Capture(), heaved = new Capture();
		for (int i = 0; i < slabs.size(); i++) {
			HemorathIronGeometry.slab(new PoseStack(), rest, ground, slabs.get(i), half, 0.0D, i);
			HemorathIronGeometry.slab(new PoseStack(), heaved, ground, slabs.get(i), half, 1.0D, i);
		}
		assertEquals(0, rest.vertices, "slabs at rest lie under the floor");
		heaved.check(4000);
		assertTrue(heaved.vertices > 0);
		assertTrue(heaved.maxHeight < -2.0D + 0.6D, "heaved, not thrown: " + heaved.maxHeight);
		assertEquals(Set.of(HemorathIronGeometry.CHIP, HemorathIronGeometry.SLAB, HemorathIronGeometry.BROKEN), heaved.shapes,
				"slabs are the floor's brick with broken faces, and their rubble is floor stone");
	}

	@Test
	void ironFormsOutOfTheFloorAndStaysBounded() {
		Spike spike = HemorathFissureRules.cluster(3, 1.2D, 1).get(0);
		Capture unformed = new Capture();
		HemorathIronGeometry.spike(new PoseStack(), unformed, Vec3.ZERO, spike, 1.0D, 0.0F, 0.0D, HemorathIronGeometry.IRON, 3);
		HemorathIronGeometry.cluster(new PoseStack(), unformed, Vec3.ZERO, HemorathFissureRules.cluster(3, 1.2D, 5), 0.0F, 20.0F, 100.0D,
				HemorathIronGeometry.IRON, 3);
		assertEquals(0, unformed.vertices, "nothing stands before it breaks out");

		Capture standing = new Capture();
		HemorathIronGeometry.cluster(new PoseStack(), standing, Vec3.ZERO, HemorathFissureRules.cluster(3, 1.2D, 5), 6.0F, 20.0F, 100.0D,
				HemorathIronGeometry.IRON, 3);
		HemorathIronGeometry.chips(new PoseStack(), standing, Vec3.ZERO, 3.0F, 1.4D, 6, 3);
		standing.check(1200);
		assertTrue(standing.vertices > 0);
		assertTrue(standing.maxHeight > 1.0D && standing.maxHeight < 1.6D, "the central spike stands about its scale: " + standing.maxHeight);

		Capture sunk = new Capture();
		HemorathIronGeometry.cluster(new PoseStack(), sunk, Vec3.ZERO, HemorathFissureRules.cluster(3, 1.2D, 5),
				20.0F + HemorathFissureRules.SINK + 3.0F, 20.0F, 100.0D, HemorathIronGeometry.IRON, 3);
		assertEquals(0, sunk.vertices, "sunk back into the floor");
		assertEquals(HemorathIronGeometry.IRON, HemorathIronGeometry.rusted(0.0F));
		assertEquals(HemorathIronGeometry.RUSTED, HemorathIronGeometry.rusted(1.0F));
	}

	@Test
	void bloodStreamsDrainsAndWallsStayBounded() {
		for (Vec3 to : new Vec3[]{new Vec3(20, 1, 0), new Vec3(0, 20, 0), Vec3.ZERO}) {
			Capture mesh = new Capture();
			HemorathBloodGeometry.stream(new PoseStack(), mesh, Vec3.ZERO, to, CAMERA, RIGHT, UP, 100, 3, 1, 1.2, 0.14, 0.32, 1);
			HemorathBloodGeometry.stream(new PoseStack(), mesh, Vec3.ZERO, to, CAMERA, RIGHT, UP, 100, 3, 0.4, 1.2, 0.14, 0.32, 1);
			HemorathBloodGeometry.drain(new PoseStack(), mesh, Vec3.ZERO, to, CAMERA, RIGHT, UP, 100, 3, 1);
			HemorathBloodGeometry.crown(new PoseStack(), mesh, Vec3.ZERO, CAMERA, RIGHT, UP, 100, 3, 0.8, 0.5, 1);
			HemorathBloodGeometry.spatter(new PoseStack(), mesh, Vec3.ZERO, to, CAMERA, RIGHT, UP, 4, 3, 3, 1);
			mesh.check(2400);
		}
		Capture wall = new Capture();
		HemorathBloodGeometry.wall(new PoseStack(), wall, 10.0D, 0.0D, 0.9D, CAMERA, RIGHT, UP, 100, 3, 1);
		wall.check(2400);
		assertTrue(wall.maxHeight <= 0.9D + 0.45D, "the rupture's sheet stands at its jump height, its spray just over");
		Capture spent = new Capture();
		HemorathBloodGeometry.spatter(new PoseStack(), spent, Vec3.ZERO, RIGHT, CAMERA, RIGHT, UP, 13, 3, 3, 1);
		HemorathBloodGeometry.stream(new PoseStack(), spent, Vec3.ZERO, RIGHT, CAMERA, RIGHT, UP, 100, 3, 0, 1.2, 0.14, 0.32, 1);
		assertEquals(0, spent.vertices, "a spent spatter and an unreached stream draw nothing");
	}

	private static final class Capture implements VertexConsumer {
		int vertices;
		double maxHeight = Double.NEGATIVE_INFINITY;
		double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
		double minZ = Double.POSITIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
		final Set<Integer> shapes = new HashSet<>();

		public VertexConsumer addVertex(float x, float y, float z) {
			assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
			maxHeight = Math.max(maxHeight, y);
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x);
			minZ = Math.min(minZ, z);
			maxZ = Math.max(maxZ, z);
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
