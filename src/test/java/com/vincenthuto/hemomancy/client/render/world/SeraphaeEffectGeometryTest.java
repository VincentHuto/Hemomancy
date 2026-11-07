package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Look;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Spur;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeTissueGeometry.Ground;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeMove;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Saint Seraphae's visual pass 2 geometry writes whole quads in the encodings her materials decode, stays within a
 * frame's budget and draws nothing when its timing says so: the trench and her networks keep the tissue shader's
 * furrow and vessel tiles (nerves never reach the tissue, they are gold on the Ductilis material), overgrowth keeps the
 * bloom tile, bone is hidden until it grows and gone once withered, her chains are iron link by link, and the light
 * with body (streams, lashes, reaches, sheets, bursts) stays bounded and frays out.
 */
class SeraphaeEffectGeometryTest {
	private static final Vec3 CAMERA = new Vec3(12, 66, -10), RIGHT = new Vec3(1, 0, 0), UP = new Vec3(0, 1, 0);
	private static final Look FULL = new Look(1.0F, 1.3F, 1.0F, 1.0F, 1.3F, 1.0F);

	@Test
	void furrowsVesselsAndOvergrowthKeepTheirShaderTiles() {
		Ground ground = new Ground(10.0D, -4.0D, 0.0D, 1.0D, 64.0D, CAMERA);
		Capture seams = new Capture();
		SeraphaeTissueGeometry.begin();
		SeraphaeTissueGeometry.seams(new PoseStack(), seams, ground, SeraphaeOvergrowthRules.trench(16.0D, 0.8D, 4), FULL, 1.0F, 4);
		SeraphaeTissueGeometry.seams(new PoseStack(), seams, ground, SeraphaeOvergrowthRules.disc(14.0D, 5), FULL, 1.0F, 5);
		seams.check(24000);
		assertTrue(seams.vertices > 0);
		assertEquals(Set.of(SeraphaeTissueGeometry.FURROW, SeraphaeTissueGeometry.VESSEL), seams.shapes,
				"every seam decodes as a furrow or a vessel");

		Capture fibres = new Capture();
		SeraphaeTissueGeometry.seams(new PoseStack(), fibres, ground, onlyFibres(SeraphaeOvergrowthRules.fan(0.6D, 6.0D, -75.0D, 75.0D, 3)),
				FULL, 1.0F, 3);
		assertEquals(0, fibres.vertices, "nerve fibres are gold and never drawn in her tissue");

		Capture bloom = new Capture();
		SeraphaeTissueGeometry.bloom(new PoseStack(), bloom, ground, 0.0D, 14.0D, -180.0D, 180.0D, FULL, 1.0F, 6);
		SeraphaeTissueGeometry.bloomRect(new PoseStack(), bloom, ground, -7.5D, 7.5D, -7.5D, 7.5D, FULL, 1.0F, 7);
		bloom.check(6000);
		assertEquals(Set.of(SeraphaeTissueGeometry.BLOOM), bloom.shapes, "every patch decodes as overgrowth");

		Capture none = new Capture();
		SeraphaeTissueGeometry.seams(new PoseStack(), none, ground, SeraphaeOvergrowthRules.disc(6.0D, 3), Look.NONE, 1.0F, 3);
		SeraphaeTissueGeometry.bloom(new PoseStack(), none, ground, 0.0D, 6.0D, -180.0D, 180.0D, Look.NONE, 1.0F, 3);
		assertEquals(0, none.vertices, "an unrevealed or healed footprint draws nothing");
		Capture early = new Capture();
		SeraphaeTissueGeometry.seams(new PoseStack(), early, ground, SeraphaeOvergrowthRules.trench(10.0D, 0.8D, 3),
				new Look(0.0F, 0.2F, 0.1F, 0.0F, 0.5F, 1.0F), 1.0F, 3);
		assertEquals(0, early.vertices, "no seam shows before the cut runs");
	}

	@Test
	void nerveFibresLieOnTheFloorInGold() {
		Ground ground = new Ground(0.0D, 0.0D, 1.0D, 0.0D, 64.0D, CAMERA);
		List<Seam> spoke = SeraphaeOvergrowthRules.spoke(1.5D, 7.0D, 34.0D, 9);
		Capture gold = new Capture();
		SeraphaeLightGeometry.nerves(new PoseStack(), gold, ground, spoke, 1.0F, 1.0F, 1.0F, 9);
		gold.check(4000);
		assertTrue(gold.vertices > 0);
		assertEquals(Set.of(SeraphaeLightGeometry.BAND, SeraphaeLightGeometry.LINE), gold.shapes);
		assertTrue(gold.maxHeight < -1.9D && gold.maxHeight > -2.0D, "laid flat on the floor, under the camera: " + gold.maxHeight);
		Capture cold = new Capture();
		SeraphaeLightGeometry.nerves(new PoseStack(), cold, ground, spoke, 1.0F, 0.0F, 1.0F, 9);
		assertEquals(0, cold.vertices, "an unfired nerve draws nothing");
	}

	@Test
	void boneGrowsOutOfTheFloorAndWithersAway() {
		Spur spur = SeraphaeOvergrowthRules.cluster(3, 1.2D, 1).get(0);
		Capture unformed = new Capture();
		SeraphaeBoneGeometry.spur(new PoseStack(), unformed, Vec3.ZERO, spur, 1.0D, 1.0D, 0.0F, SeraphaeBoneGeometry.SPUR, 3);
		SeraphaeBoneGeometry.cluster(new PoseStack(), unformed, Vec3.ZERO, SeraphaeOvergrowthRules.cluster(3, 1.2D, 5), 0.0F, 20.0F, 100.0D,
				SeraphaeBoneGeometry.SPUR, 3);
		assertEquals(0, unformed.vertices, "nothing stands before it grows");

		Capture standing = new Capture();
		SeraphaeBoneGeometry.cluster(new PoseStack(), standing, Vec3.ZERO, SeraphaeOvergrowthRules.cluster(3, 1.25D, 6), 10.0F, 20.0F, 100.0D,
				SeraphaeBoneGeometry.SPUR, 3);
		SeraphaeBoneGeometry.shards(new PoseStack(), standing, Vec3.ZERO, 3.0F, 1.4D, 6, 3);
		standing.check(1600);
		assertTrue(standing.vertices > 0);
		assertTrue(standing.maxHeight > 1.0D && standing.maxHeight < 1.7D, "the central spur stands about its scale: " + standing.maxHeight);
		assertEquals(Set.of(SeraphaeBoneGeometry.BONE, SeraphaeBoneGeometry.SHARD), standing.shapes);

		Capture withered = new Capture();
		SeraphaeBoneGeometry.cluster(new PoseStack(), withered, Vec3.ZERO, SeraphaeOvergrowthRules.cluster(3, 1.25D, 6),
				20.0F + SeraphaeOvergrowthRules.WITHER + 5.0F, 20.0F, 100.0D, SeraphaeBoneGeometry.SPUR, 3);
		assertEquals(0, withered.vertices, "withered back into the floor");
		assertEquals(SeraphaeBoneGeometry.SPUR, SeraphaeBoneGeometry.withLight(SeraphaeBoneGeometry.SPUR, 0.55F));
	}

	@Test
	void herChainsAreIronLinkByLink() {
		Vec3 from = new Vec3(-8.0D, 1.5D, -6.0D), to = new Vec3(0.0D, 2.0D, 0.0D);
		Capture chain = new Capture();
		SeraphaeBoneGeometry.chain(new PoseStack(), chain, from, to, 0.4F, new Vec3(1, 0, 0), t -> 0.0D, t -> 0.2D, 7);
		int links = SeraphaeOvergrowthRules.links(from.distanceTo(to));
		assertEquals(links * 16 * 4, chain.vertices, "four bars of four faces a link");
		assertEquals(Set.of(SeraphaeBoneGeometry.IRON), chain.shapes);
		Capture whipped = new Capture();
		SeraphaeBoneGeometry.chain(new PoseStack(), whipped, from, to, 1.0F, new Vec3(0, 0, 1), t -> SeraphaeOvergrowthRules.whip(t, 2.0F, 1.0D),
				t -> 1.0D, 7);
		assertEquals(chain.vertices, whipped.vertices);
		Capture sixteen = new Capture();
		SeraphaeBoneGeometry.chain(new PoseStack(), sixteen, Vec3.ZERO, new Vec3(16.0D, 0.0D, 0.0D), 0.2F, new Vec3(0, 0, 1), t -> 0.0D,
				t -> 0.0D, 3);
		sixteen.check(54 * 64);
	}

	@Test
	void lightWithBodyStaysBoundedAndFraysOut() {
		for (Vec3 to : new Vec3[]{new Vec3(20, 1, 0), new Vec3(0, 20, 0), Vec3.ZERO}) {
			Capture mesh = new Capture();
			SeraphaeLightGeometry.stream(new PoseStack(), mesh, new Vec3(0, 2, 0), to, CAMERA, RIGHT, UP, 100.0D, 4, 1.0D, 0.4D, 0.12D, 0.6D, 1.0F);
			mesh.check(1200);
		}
		List<Vec3> trail = new ArrayList<>();
		for (int i = 0; i <= 14; i++) trail.add(new Vec3(Math.cos(i * 0.2D) * 4.0D, 1.5D, Math.sin(i * 0.2D) * 4.0D));
		for (float age : new float[]{-2.0F, 0.0F, 3.0F, 8.0F, 11.0F}) {
			Capture lash = new Capture();
			SeraphaeLightGeometry.lash(new PoseStack(), lash, lash, trail, CAMERA, RIGHT, UP, 1.0D, SeraphaeOvergrowthRules.lashHead(age),
					SeraphaeOvergrowthRules.lashFade(age), SeraphaeOvergrowthRules.lashFray(age), 100.0D, 5);
			lash.check(1400);
			assertTrue(lash.vertices > 0);
			Capture reach = new Capture();
			SeraphaeLightGeometry.reach(new PoseStack(), reach, reach, trail, CAMERA, RIGHT, UP, 1.0F, 100.0D, 6);
			reach.check(1000);
		}
		Capture short_ = new Capture();
		SeraphaeLightGeometry.lash(new PoseStack(), short_, short_, trail.subList(0, 2), CAMERA, RIGHT, UP, 1.0D, 0.5F, 1.0F, 0.0F, 100.0D, 5);
		assertEquals(0, short_.vertices, "a lash needs a path");

		Capture sheet = new Capture();
		SeraphaeLightGeometry.curtain(new PoseStack(), sheet, sheet, Vec3.ZERO, new double[]{1.0D, 0.0D}, 16.0D, -180.0D, 180.0D, 0.0D, 2.2D,
				CAMERA, 100.0D, 3, 1.0F, true, true);
		sheet.check(4000);
		assertTrue(sheet.maxHeight > 1.8D, "it stands as high as asked: " + sheet.maxHeight);

		for (float age : new float[]{0.0F, 2.0F, 6.0F, SeraphaeOvergrowthRules.IMPACT}) {
			Capture burst = new Capture();
			SeraphaeLightGeometry.burst(new PoseStack(), burst, burst, new Vec3(0, 1, 0), new Vec3(1, 0, 0), CAMERA, RIGHT, UP, age, 0.4D, 7, true);
			burst.check(400);
			if (age >= SeraphaeOvergrowthRules.IMPACT) assertEquals(0, burst.vertices, "the burst is gone after the hurt flash");
		}
	}

	@Test
	void eachMoveStrikesInItsOwnStuff() {
		assertEquals(SeraphaeImpactGeometry.Strike.FIRE, SeraphaeImpactGeometry.strike(SeraphaeMove.NERVE_LASH));
		assertEquals(SeraphaeImpactGeometry.Strike.FIRE, SeraphaeImpactGeometry.strike(SeraphaeMove.CONVULSION));
		assertEquals(SeraphaeImpactGeometry.Strike.GROWTH, SeraphaeImpactGeometry.strike(SeraphaeMove.HEALED_GROUND));
		assertEquals(SeraphaeImpactGeometry.Strike.LIGHT, SeraphaeImpactGeometry.strike(SeraphaeMove.UNCLOSING_EYE));
		assertEquals(SeraphaeImpactGeometry.Strike.LIGHT, SeraphaeImpactGeometry.strike(null));
	}

	private static List<Seam> onlyFibres(List<Seam> seams) {
		return seams.stream().filter(s -> s.kind() == SeraphaeOvergrowthRules.Kind.FIBRE).toList();
	}

	private static final class Capture implements VertexConsumer {
		int vertices;
		double maxHeight = Double.NEGATIVE_INFINITY;
		final Set<Integer> shapes = new HashSet<>();

		public VertexConsumer addVertex(float x, float y, float z) {
			assertTrue(Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z));
			maxHeight = Math.max(maxHeight, y);
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
