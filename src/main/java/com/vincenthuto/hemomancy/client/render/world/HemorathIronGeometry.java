package com.vincenthuto.hemomancy.client.render.world;

import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.POOL;
import static com.vincenthuto.hemomancy.client.render.world.LuxUmbraGeometry.WISP;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureGeometry.Ground;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Slab;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Spike;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Saint Hemorath's iron as real meshes in his own iron material ({@link HemorathRenderTypes#IRON}, visual pass 2):
 * faceted spikes in three tapering stages that lean, twist and flute, wet with blood from the point down, the low scab
 * they break out of, the chips of floor a punch throws, and the slabs of floor heaved up along a trench's lips. The
 * vertex colour is the material's state, never a tint:
 * red the heat of the rupture at the foot (white-hot at the punch, cooling over {@link #COOL} ticks), green how far
 * wet blood runs down from the point, blue the rust; oxblood iron, never steel, copper or brass. The alpha is the
 * formation, so a rising spike climbs out of the floor's blood and a sinking one dissolves back into it from the point
 * down. Points are camera-relative.
 */
final class HemorathIronGeometry {
	/** Material states (heat, wet, rust): fresh iron, an iron chip soaked in blood, a scab, and floor stone. */
	static final int IRON = state(0.0F, 0.34F, 0.14F), TIP = state(0.0F, 1.0F, 0.1F), SCAB = state(0.0F, 0.18F, 0.85F),
			STONE = state(0.0F, 0.25F, 0.0F);
	/** The state a standing spike rusts toward before it sinks: dried, rust-bloomed. */
	static final int RUSTED = state(0.0F, 0.12F, 1.0F);
	/**
	 * Floor slabs heaved along a trench's lip, in the chamber floor's own dark brick: the top's outer edge and its
	 * broken inner edge (blue marks the inner edge, which the shader lights as a pale cut lip), and the broken faces
	 * wet low down and drier toward their top.
	 */
	static final int SLAB_OUTER = state(0.0F, 0.0F, 0.0F), SLAB_INNER = state(0.0F, 0.0F, 1.0F),
			BROKEN_LOW = state(0.0F, 0.8F, 0.0F), BROKEN_HIGH = state(0.0F, 0.1F, 0.0F);
	/** Shapes the iron shader decodes: iron, a chip of floor stone, a slab's brick top and a slab's broken face. */
	static final int METAL = 0, CHIP = 1, SLAB = 2, BROKEN = 3;
	/** The rupture's heat at a spike's foot cools over this many ticks after the punch. */
	static final float COOL = 7.0F;
	private static final double[] STAGE_HEIGHT = {0.0D, 0.36D, 0.7D};
	private static final double[] STAGE_RADIUS = {1.0D, 0.6D, 0.27D};
	private static final double STAGE_TWIST = 0.18D;
	/** A spike's foot starts this far under the floor, so it reads as breaking out of it. */
	private static final double BURIED = 0.08D;

	private HemorathIronGeometry() {
	}

	/**
	 * One spike with its foot at {@code foot}: {@code grown} scales its height (0 to the rise's overshoot),
	 * {@code formation} is the blood front, {@code shiver} its sway in blocks, {@code state} its material state.
	 */
	static void spike(PoseStack p, VertexConsumer iron, Vec3 foot, Spike s, double grown, float formation, double shiver,
			int state, int seed) {
		double h = s.height() * grown;
		if (h < 0.02D || formation <= 0.01F) return;
		int n = Math.max(3, s.sides());
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double swayX = Math.cos(seed * 1.7D) * shiver, swayZ = Math.sin(seed * 1.7D) * shiver;
		Vec3[][] rings = new Vec3[STAGE_HEIGHT.length][n];
		double[] heights = new double[STAGE_HEIGHT.length];
		for (int k = 0; k < STAGE_HEIGHT.length; k++) {
			double y = k == 0 ? -BURIED : h * STAGE_HEIGHT[k];
			heights[k] = y;
			double r = s.radius() * STAGE_RADIUS[k];
			for (int j = 0; j < n; j++) {
				double a = s.turn() + j * Math.PI * 2.0D / n + k * STAGE_TWIST;
				double flute = j % 2 == 0 ? 1.0D : 0.82D;
				double lean = Math.max(0.0D, y) / Math.max(0.01D, h);
				rings[k][j] = foot.add(Math.cos(a) * r * flute + s.leanX() * y + swayX * lean, y,
						Math.sin(a) * r * flute + s.leanZ() * y + swayZ * lean);
			}
		}
		Vec3 tip = foot.add(s.leanX() * h + swayX, h, s.leanZ() * h + swayZ);
		for (int k = 0; k < rings.length; k++) {
			double v0 = Math.max(0.0D, heights[k]) / h, v1 = k + 1 < rings.length ? heights[k + 1] / h : 1.0D;
			for (int j = 0; j < n; j++) {
				Vec3 a = rings[k][j], b = rings[k][(j + 1) % n];
				Vec3 c = k + 1 < rings.length ? rings[k + 1][(j + 1) % n] : tip, d = k + 1 < rings.length ? rings[k + 1][j] : tip;
				double width = a.distanceTo(b);
				face(iron, pose, a, b, c, d, state, state, formation, 0.0D, width, v0, v1, METAL, tile + j);
			}
		}
	}

	/** A low scab of iron the cluster breaks out of: a squat, uneven seven-sided mound, as hot as its spikes' feet. */
	static void scab(PoseStack p, VertexConsumer iron, Vec3 foot, double radius, double height, float formation, float heat,
			int seed) {
		if (radius < 0.02D || height < 0.01D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int n = 7, tile = MaterialQuads.seedTile(seed + 3);
		int state = withHeat(SCAB, heat);
		Vec3[] base = new Vec3[n], top = new Vec3[n];
		for (int j = 0; j < n; j++) {
			double a = j * Math.PI * 2.0D / n + seed * 0.37D;
			double r = radius * (0.75D + 0.35D * HemorathFissureRules.unit(seed, 2000 + j));
			base[j] = foot.add(Math.cos(a) * r, -0.04D, Math.sin(a) * r);
			top[j] = foot.add(Math.cos(a + 0.2D) * r * 0.62D, height * (0.7D + 0.5D * HemorathFissureRules.unit(seed, 2100 + j)),
					Math.sin(a + 0.2D) * r * 0.62D);
		}
		Vec3 crown = foot.add(0.0D, height * 1.15D, 0.0D);
		for (int j = 0; j < n; j++) {
			int k = (j + 1) % n;
			face(iron, pose, base[j], base[k], top[k], top[j], state, state, formation, 0.0D, base[j].distanceTo(base[k]), 0.0D,
					0.6D, METAL, tile + j);
			face(iron, pose, top[j], top[k], crown, crown, state, state, formation, 0.0D, top[j].distanceTo(top[k]), 0.6D, 1.0D,
					METAL, tile + j + 7);
		}
	}

	/** A tumbling chip: a small uneven octahedron at {@code at}, {@code size} across; stone for {@link #STONE}, else iron. */
	static void chip(PoseStack p, VertexConsumer iron, Vec3 at, double size, double tumble, int state, int seed) {
		if (size < 0.01D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int shape = state == STONE ? CHIP : METAL;
		double c = Math.cos(tumble), s = Math.sin(tumble);
		double sx = size * (0.8D + 0.4D * HemorathFissureRules.unit(seed, 1)), sy = size * (0.5D + 0.3D * HemorathFissureRules.unit(seed, 2));
		Vec3[] points = {new Vec3(sx * c, sy * s, 0), new Vec3(-sx * c, -sy * s, 0), new Vec3(-sy * s, sx * c * 0.8D, 0),
				new Vec3(sy * s, -sx * c * 0.8D, 0), new Vec3(0, 0, size * 0.6D), new Vec3(0, 0, -size * 0.6D)};
		int[][] faces = {{0, 2, 4}, {2, 1, 4}, {1, 3, 4}, {3, 0, 4}, {2, 0, 5}, {1, 2, 5}, {3, 1, 5}, {0, 3, 5}};
		for (int[] f : faces) {
			Vec3 a = at.add(points[f[0]]), b = at.add(points[f[1]]), d = at.add(points[f[2]]);
			face(iron, pose, a, b, d, d, state, state, 1.0F, 0.0D, size, 0.0D, 1.0D, shape, tile + f[0]);
		}
	}

	/**
	 * A slab of floor heaved up along a trench's lip ({@link HemorathFissureRules#slabCorners}) at {@code lift}: its
	 * top, the broken edge facing the trench (stained with what welled up out of it), its outer edge and both ends, in
	 * floor stone at 16 grains a block; with a piece of rubble on the floor beyond it or on the lip just inside the
	 * trench's edge. At rest it lies under the floor and draws nothing.
	 */
	static void slab(PoseStack p, VertexConsumer iron, Ground g, Slab slab, double half, double lift, int seed) {
		if (lift <= 0.02D) return;
		double[][] k = HemorathFissureRules.slabCorners(slab, half, lift);
		Matrix4f pose = p.last().pose();
		Vec3[] w = new Vec3[8];
		for (int i = 0; i < 8; i++) w[i] = local(g, k[i][0], k[i][1], k[i][2]);
		int tile = MaterialQuads.seedTile(seed);
		double length = slab.length(), across = slab.width(), thick = slab.thick();
		// per end, near then far (+4): 0 top outer, 1 top inner, 2 bottom inner, 3 bottom outer
		face(iron, pose, w[0], w[4], w[5], w[1], SLAB_OUTER, SLAB_INNER, 1.0F, 0.0D, length, 0.0D, across, SLAB, tile);
		face(iron, pose, w[2], w[6], w[5], w[1], BROKEN_LOW, BROKEN_HIGH, 1.0F, 0.0D, length, 0.0D, thick, BROKEN, tile + 1);
		face(iron, pose, w[3], w[7], w[4], w[0], BROKEN_HIGH, BROKEN_HIGH, 1.0F, 0.0D, length, 0.0D, thick, BROKEN, tile + 2);
		face(iron, pose, w[3], w[2], w[1], w[0], BROKEN_LOW, BROKEN_HIGH, 1.0F, 0.0D, across, 0.0D, thick, BROKEN, tile + 3);
		face(iron, pose, w[7], w[6], w[5], w[4], BROKEN_LOW, BROKEN_HIGH, 1.0F, 0.0D, across, 0.0D, thick, BROKEN, tile + 4);
		double grow = Math.min(1.0D, lift * 1.4D);
		double size = (0.045D + 0.05D * HemorathFissureRules.unit(seed, 1)) * grow * Math.max(0.7D, Math.min(1.4D, half / 0.5D));
		if (size < 0.015D) return;
		// one piece of rubble a slab, on the floor beyond it or on the lip just inside the trench's edge
		double along = slab.along() + (HemorathFissureRules.unit(seed, 3) - 0.5D) * slab.length() * 1.2D;
		if (HemorathFissureRules.unit(seed, 2) < 0.5D) {
			double beyond = slab.side() * (half + HemorathFissureRules.SLAB_GAP + slab.width() + 0.06D
					+ 0.1D * HemorathFissureRules.unit(seed, 4));
			chip(p, iron, local(g, along, beyond, size * 0.2D), size, HemorathFissureRules.unit(seed, 5) * 6.28D, STONE, seed + 6);
		} else {
			double lip = slab.side() * half * (0.92D + 0.05D * HemorathFissureRules.unit(seed, 7));
			chip(p, iron, local(g, along, lip, size * 0.15D), size * 0.75D, HemorathFissureRules.unit(seed, 8) * 6.28D, STONE, seed + 9);
		}
	}

	/** A ground's local point {along, right} at {@code up} above its floor, camera-relative. */
	private static Vec3 local(Ground g, double along, double right, double up) {
		Vec3 c = g.camera();
		return new Vec3(g.wx(along, right) - c.x, g.y() + up - c.y, g.wz(along, right) - c.z);
	}

	/** Chips thrown by a punch at {@code foot}, {@code age} ticks after it; {@code reach} scales how far they fly. */
	static void chips(PoseStack p, VertexConsumer iron, Vec3 foot, float age, double reach, int count, int seed) {
		for (int i = 0; i < count; i++) {
			double[] chip = HemorathFissureRules.chip(seed, i, age, reach);
			if (chip == null) continue;
			double size = (0.07D + 0.07D * HemorathFissureRules.unit(seed, 2200 + i)) * chip[4];
			chip(p, iron, foot.add(chip[0], chip[1] + size * 0.5D, chip[2]), size, chip[3], i % 3 == 0 ? IRON : STONE, seed + i);
		}
	}

	/** A cluster of spikes with its foot at {@code foot}, broken out {@code age} ticks ago, sinking from {@code sinkAt}. */
	static void cluster(PoseStack p, VertexConsumer iron, Vec3 foot, List<Spike> spikes, float age, float sinkAt, double time,
			int state, int seed) {
		float formation = HemorathFissureRules.formation(age, sinkAt);
		double grown = HemorathFissureRules.height(age, sinkAt);
		if (grown <= 0.01D) return;
		double shiver = HemorathFissureRules.shiver(age, time, seed);
		double girth = spikes.isEmpty() ? 0.1D : spikes.get(0).radius();
		scab(p, iron, foot, girth * 2.0D, girth * 0.9D * Math.min(1.0D, grown), formation, heat(age), seed);
		for (int i = 0; i < spikes.size(); i++) {
			Spike spike = spikes.get(i);
			// the outer spikes break out a beat after the centre one
			float own = age - i * 0.6F;
			double h = i == 0 ? grown : HemorathFissureRules.height(own, sinkAt);
			spike(p, iron, foot.add(spike.dx(), 0.0D, spike.dz()), spike, h, HemorathFissureRules.formation(own, sinkAt),
					shiver * (i == 0 ? 1.0D : 0.6D), withHeat(state, heat(own)), seed + i * 17);
		}
	}

	/**
	 * The light of a punch at {@code foot} in the flow pass: a hot bloom on the floor where the iron tore out, cooling
	 * with the iron's heat. The geometry carries the iron; this is only its glow on the floor.
	 */
	static void rupture(PoseStack p, VertexConsumer blood, Vec3 foot, Vec3 right, Vec3 up, float age, double size, int seed) {
		if (age <= 0.0F || age > 24.0F) return;
		float heat = heat(age);
		LuxUmbraGeometry.ground(p, blood, foot.add(0.0D, 0.035D, 0.0D), size * (0.45D + 0.2D * Math.min(1.0F, age / 3.0F)), POOL,
				seed, HemorathBloodGeometry.CORE, 0.4F * heat);
		LuxUmbraGeometry.ground(p, blood, foot.add(0.0D, 0.03D, 0.0D), size * 1.05D, POOL, seed + 1, HemorathBloodGeometry.TINT,
				0.3F * Math.min(1.0F, heat * 1.6F));
	}

	/** The state a standing spike has rusted to, from fresh iron at 0 to dried rust at 1. */
	static int rusted(float amount) {
		float t = Mth.clamp(amount, 0.0F, 1.0F);
		int r = Math.round(HemoEasing.lerp((IRON >> 16) & 255, (RUSTED >> 16) & 255, t));
		int g = Math.round(HemoEasing.lerp((IRON >> 8) & 255, (RUSTED >> 8) & 255, t));
		int b = Math.round(HemoEasing.lerp(IRON & 255, RUSTED & 255, t));
		return r << 16 | g << 8 | b;
	}

	/** Rust and dried blood thrown up by a punch: a few soft puffs that rise and spread. In the flow pass. */
	static void dust(PoseStack p, VertexConsumer blood, Vec3 at, Vec3 right, Vec3 up, float age, double scale, int seed) {
		if (age < 0.0F || age > 16.0F) return;
		float t = age / 16.0F;
		float alpha = (1.0F - t) * (1.0F - t) * Math.min(1.0F, age / 1.5F);
		for (int i = 0; i < 3; i++) {
			double a = HemorathFissureRules.unit(seed, 2300 + i) * Math.PI * 2.0D;
			double out = scale * (0.15D + 0.5D * t) * (0.6D + 0.6D * HemorathFissureRules.unit(seed, 2310 + i));
			Vec3 puff = at.add(Math.cos(a) * out, 0.1D + scale * 0.6D * t * (0.7D + 0.5D * i / 3.0D), Math.sin(a) * out);
			double size = scale * (0.25D + 0.45D * t);
			LuxUmbraGeometry.card(p, blood, puff, right, up, size, size * 0.8D, WISP, seed + i, 0xB0705C, alpha * 0.55F);
		}
	}

	/** The rupture's heat at a spike's foot {@code age} ticks after it broke out: white-hot at the punch, then cooling. */
	static float heat(float age) {
		if (age <= 0.0F) return 0.0F;
		return 0.12F + 0.88F * (float) Math.exp(-age / COOL);
	}

	/** A material state: the rupture's heat, how far wet blood runs down from the point, and the rust, each 0 to 1. */
	static int state(float heat, float wet, float rust) {
		return HemorathFissureGeometry.color(heat, wet, rust);
	}

	/** The same state with its heat replaced. */
	static int withHeat(int state, float heat) {
		return (state & 0x00FFFF) | Math.round(Mth.clamp(heat, 0.0F, 1.0F) * 255.0F) << 16;
	}

	/**
	 * One face, a to b to c to d: U runs across it in blocks (so the grain sits at 16 a block) and V up the spike from
	 * {@code v0} to {@code v1}, which the formation front climbs. The state runs low to high.
	 */
	private static void face(VertexConsumer iron, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int low, int high,
			float formation, double u0, double u1, double v0, double v1, int shape, int tile) {
		int seedTile = MaterialQuads.seedTile(tile);
		float w0 = (float) (seedTile * 2 + Mth.clamp(v0, 0.0D, 0.999D)), w1 = (float) (seedTile * 2 + Mth.clamp(v1, 0.0D, 0.999D));
		float x0 = (float) (shape * 2 + Mth.clamp(u0, 0.0D, 1.9D)), x1 = (float) (shape * 2 + Mth.clamp(u1, 0.0D, 1.9D));
		MaterialQuads.vertex(iron, pose, a.x, a.y, a.z, low, formation, x0, w0);
		MaterialQuads.vertex(iron, pose, b.x, b.y, b.z, low, formation, x1, w0);
		MaterialQuads.vertex(iron, pose, c.x, c.y, c.z, high, formation, x1, w1);
		MaterialQuads.vertex(iron, pose, d.x, d.y, d.z, high, formation, x0, w1);
	}
}
