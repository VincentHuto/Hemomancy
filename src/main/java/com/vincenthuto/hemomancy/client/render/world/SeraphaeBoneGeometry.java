package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Spur;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Saint Seraphae's bone and iron as real meshes in her bone material ({@link SeraphaeRenderTypes#BONE}, visual pass 2):
 * spurs of bone that grow out of the floor in four tapering, twisting stages and curl as they rise, with white light
 * running in their grooves and a raw wet root where they tore out; low blisters of new tissue they break out of; shards
 * a growth or a burst throws; and her hematic iron chains link by link, flat and edge in turn, the light she pours out
 * caught in them. The vertex colour is the material's state (light, rawness, wetness), never a tint, and the alpha is
 * the formation, so a growing spur climbs out of the floor and a withering one dissolves back from the tip. Points are
 * camera-relative.
 */
final class SeraphaeBoneGeometry {
	/** Shapes the bone shader decodes. */
	static final int BONE = 0, IRON = 1, SHARD = 2;
	/** Material states: fresh bone (light in its grooves, a raw wet root), a blister, a shard, and dark iron. */
	static final int SPUR = state(0.55F, 0.85F, 0.7F), BLISTER = state(0.35F, 0.45F, 0.9F), CHIP = state(0.7F, 0.0F, 0.0F),
			DARK = state(0.0F, 0.0F, 0.0F);
	private static final double[] STAGE_HEIGHT = {0.0D, 0.3D, 0.6D, 0.84D};
	private static final double[] STAGE_RADIUS = {1.0D, 0.68D, 0.42D, 0.2D};
	private static final double STAGE_TWIST = 0.22D;
	/** A spur's foot starts this far under the floor, so it reads as breaking out of it. */
	private static final double BURIED = 0.06D;
	/** A link's outer width across the chain and its wire's thickness (one texel). */
	static final double LINK_WIDTH = 0.25D, WIRE = 0.0625D;
	private static final Vec3 UP = new Vec3(0, 1, 0);

	private SeraphaeBoneGeometry() {
	}

	/**
	 * One spur with its foot at {@code foot}: {@code grown} scales its height (0 to the growth's overshoot),
	 * {@code girth} its thickness (the throb), {@code formation} the living front, {@code state} its material state.
	 */
	static void spur(PoseStack p, VertexConsumer bone, Vec3 foot, Spur s, double grown, double girth, float formation, int state,
			int seed) {
		double h = s.height() * grown;
		if (h < 0.02D || formation <= 0.01F) return;
		int n = Math.max(4, s.sides());
		Matrix4f pose = p.last().pose();
		double lean = Math.hypot(s.leanX(), s.leanZ());
		double cx = lean < 1e-6 ? 0.0D : s.leanX() / lean, cz = lean < 1e-6 ? 0.0D : s.leanZ() / lean;
		Vec3[][] rings = new Vec3[STAGE_HEIGHT.length][n];
		double[] heights = new double[STAGE_HEIGHT.length];
		for (int k = 0; k < STAGE_HEIGHT.length; k++) {
			double y = k == 0 ? -BURIED : h * STAGE_HEIGHT[k];
			heights[k] = y;
			double r = s.radius() * girth * STAGE_RADIUS[k];
			double up = Math.max(0.0D, y) / h;
			// the lean carries it out; the curl bends its upper half further, like something still growing
			double ox = s.leanX() * y + cx * s.curl() * h * up * up, oz = s.leanZ() * y + cz * s.curl() * h * up * up;
			for (int j = 0; j < n; j++) {
				double a = s.turn() + j * Math.PI * 2.0D / n + k * STAGE_TWIST;
				double flute = j % 2 == 0 ? 1.0D : 0.8D;
				rings[k][j] = foot.add(Math.cos(a) * r * flute + ox, y, Math.sin(a) * r * flute + oz);
			}
		}
		Vec3 tip = foot.add(s.leanX() * h + cx * s.curl() * h, h, s.leanZ() * h + cz * s.curl() * h);
		for (int k = 0; k < rings.length; k++) {
			double v0 = Math.max(0.0D, heights[k]) / h, v1 = k + 1 < rings.length ? heights[k + 1] / h : 1.0D;
			for (int j = 0; j < n; j++) {
				Vec3 a = rings[k][j], b = rings[k][(j + 1) % n];
				Vec3 c = k + 1 < rings.length ? rings[k + 1][(j + 1) % n] : tip, d = k + 1 < rings.length ? rings[k + 1][j] : tip;
				face(bone, pose, a, b, c, d, state, formation, 0.0D, a.distanceTo(b), v0, v1, BONE, seed + j);
			}
		}
	}

	/** A low blister of new tissue a cluster breaks out of: a soft seven-sided dome, wet and raw at its foot. */
	static void blister(PoseStack p, VertexConsumer bone, Vec3 foot, double radius, double height, float formation, int state,
			int seed) {
		if (radius < 0.02D || height < 0.01D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int n = 7;
		double[] rise = {-0.04D, 0.55D, 0.9D};
		double[] spread = {1.0D, 0.82D, 0.45D};
		Vec3[][] rings = new Vec3[rise.length][n];
		for (int k = 0; k < rise.length; k++) {
			for (int j = 0; j < n; j++) {
				double a = j * Math.PI * 2.0D / n + seed * 0.37D + k * 0.15D;
				double r = radius * spread[k] * (0.8D + 0.3D * SeraphaeOvergrowthRules.unit(seed, 2000 + j));
				rings[k][j] = foot.add(Math.cos(a) * r, k == 0 ? rise[k] : height * rise[k], Math.sin(a) * r);
			}
		}
		Vec3 crown = foot.add(0.0D, height, 0.0D);
		for (int k = 0; k < rise.length; k++) {
			double v0 = Math.max(0.0D, k == 0 ? 0.0D : rise[k]), v1 = k + 1 < rise.length ? rise[k + 1] : 1.0D;
			for (int j = 0; j < n; j++) {
				int m = (j + 1) % n;
				Vec3 a = rings[k][j], b = rings[k][m];
				Vec3 c = k + 1 < rise.length ? rings[k + 1][m] : crown, d = k + 1 < rise.length ? rings[k + 1][j] : crown;
				face(bone, pose, a, b, c, d, state, formation, 0.0D, a.distanceTo(b), v0, v1, BONE, seed + j + 7 * k);
			}
		}
	}

	/**
	 * A cluster of spurs with its foot at {@code foot}, broken out {@code age} ticks ago and withering from
	 * {@code witherAt}: the blister first, the central spur, then the others a beat apart, all throbbing on her heartbeat.
	 */
	static void cluster(PoseStack p, VertexConsumer bone, Vec3 foot, List<Spur> spurs, float age, float witherAt, double time,
			int state, int seed) {
		double grown = SeraphaeOvergrowthRules.height(age, witherAt);
		if (grown <= 0.01D) return;
		double throb = SeraphaeOvergrowthRules.throb(age, time, seed);
		double girth = spurs.isEmpty() ? 0.1D : spurs.get(0).radius();
		float formation = SeraphaeOvergrowthRules.formation(age, witherAt);
		blister(p, bone, foot, girth * 2.3D * throb, girth * 1.1D * Math.min(1.0D, grown), formation, withLight(BLISTER,
				SeraphaeOvergrowthRules.glow(age) * 0.6F), seed + 3);
		for (int i = 0; i < spurs.size(); i++) {
			Spur spur = spurs.get(i);
			float own = age - i * 0.7F;
			double h = i == 0 ? grown : SeraphaeOvergrowthRules.height(own, witherAt);
			spur(p, bone, foot.add(spur.dx(), 0.0D, spur.dz()), spur, h, throb, SeraphaeOvergrowthRules.formation(own, witherAt),
					withLight(state, SeraphaeOvergrowthRules.glow(own)), seed + i * 17);
		}
	}

	/** A tumbling shard: a small uneven octahedron at {@code at}, {@code size} across. */
	static void shard(PoseStack p, VertexConsumer bone, Vec3 at, double size, double tumble, int state, int seed) {
		if (size < 0.01D) return;
		Matrix4f pose = p.last().pose();
		double c = Math.cos(tumble), s = Math.sin(tumble);
		double sx = size * (0.9D + 0.5D * SeraphaeOvergrowthRules.unit(seed, 1)), sy = size * (0.4D + 0.3D * SeraphaeOvergrowthRules.unit(seed, 2));
		Vec3[] points = {new Vec3(sx * c, sy * s, 0), new Vec3(-sx * c, -sy * s, 0), new Vec3(-sy * s, sx * c * 0.7D, 0),
				new Vec3(sy * s, -sx * c * 0.7D, 0), new Vec3(0, 0, size * 0.5D), new Vec3(0, 0, -size * 0.5D)};
		int[][] faces = {{0, 2, 4}, {2, 1, 4}, {1, 3, 4}, {3, 0, 4}, {2, 0, 5}, {1, 2, 5}, {3, 1, 5}, {0, 3, 5}};
		for (int[] f : faces) {
			Vec3 a = at.add(points[f[0]]), b = at.add(points[f[1]]), d = at.add(points[f[2]]);
			face(bone, pose, a, b, d, d, state, 1.0F, 0.0D, size, 0.0D, 1.0D, SHARD, seed + f[0]);
		}
	}

	/** Shards thrown by a growth or a burst at {@code at}, {@code age} ticks after it; {@code reach} scales how far. */
	static void shards(PoseStack p, VertexConsumer bone, Vec3 at, float age, double reach, int count, int seed) {
		for (int i = 0; i < count; i++) {
			double[] shard = SeraphaeOvergrowthRules.shard(seed, i, age, reach);
			if (shard == null) continue;
			double size = (0.06D + 0.06D * SeraphaeOvergrowthRules.unit(seed, 2200 + i)) * shard[4];
			shard(p, bone, at.add(shard[0], shard[1] + size * 0.5D, shard[2]), size, shard[3], CHIP, seed + i);
		}
	}

	/**
	 * Her chain from {@code from} to {@code to}, link by link, flat and edge in turn: sagging by {@code taut} as the
	 * radiance braid sags, thrown sideways along {@code side} by {@code throwAt} (blocks at each point along it, 0 to
	 * 1), and lit from inside by {@code lightAt} (her light caught in the iron at each point along it).
	 */
	static void chain(PoseStack p, VertexConsumer iron, Vec3 from, Vec3 to, float taut, Vec3 side, DoubleUnaryOperator throwAt,
			DoubleUnaryOperator lightAt, int seed) {
		double length = from.distanceTo(to);
		if (length < 0.2D) return;
		int count = SeraphaeOvergrowthRules.links(length);
		double step = 1.0D / count;
		for (int i = 0; i < count; i++) {
			double t = (i + 0.5D) * step;
			Vec3 at = point(from, to, t, taut, side, throwAt);
			Vec3 axis = point(from, to, Math.min(1.0D, t + step * 0.5D), taut, side, throwAt)
					.subtract(point(from, to, Math.max(0.0D, t - step * 0.5D), taut, side, throwAt));
			if (axis.lengthSqr() < 1e-8) continue;
			axis = axis.normalize();
			Vec3 across = axis.cross(UP);
			if (across.lengthSqr() < 1e-6) across = axis.cross(new Vec3(1, 0, 0));
			across = across.normalize();
			if (i % 2 == 1) across = axis.cross(across).normalize();
			link(p, iron, at, axis, across, state(Mth.clamp((float) lightAt.applyAsDouble(t), 0.0F, 1.0F), 0.0F, 0.0F), seed + i);
		}
	}

	private static Vec3 point(Vec3 from, Vec3 to, double t, float taut, Vec3 side, DoubleUnaryOperator throwAt) {
		return from.lerp(to, t).add(0.0D, -SeraphaeOvergrowthRules.sag(t, taut), 0.0D).add(side.scale(throwAt.applyAsDouble(t)));
	}

	/** One iron link at {@code at}: two long bars and two end bars, a wire thick, in the plane of axis and across. */
	static void link(PoseStack p, VertexConsumer iron, Vec3 at, Vec3 axis, Vec3 across, int state, int seed) {
		Matrix4f pose = p.last().pose();
		Vec3 normal = axis.cross(across).normalize();
		double length = SeraphaeOvergrowthRules.LINK_LENGTH, half = WIRE * 0.5D;
		double sideOffset = (LINK_WIDTH - WIRE) * 0.5D, endOffset = (length - WIRE) * 0.5D;
		for (int s = -1; s <= 1; s += 2) {
			bar(iron, pose, at.add(across.scale(s * sideOffset)), axis.scale(length * 0.5D), across.scale(half), normal.scale(half),
					state, seed + s);
			bar(iron, pose, at.add(axis.scale(s * endOffset)), across.scale(sideOffset - half), axis.scale(half), normal.scale(half),
					state, seed + 3 + s);
		}
	}

	/** A bar centred at {@code c} running along {@code e1}, its faces {@code e2} and {@code e3} out from its middle. */
	private static void bar(VertexConsumer iron, Matrix4f pose, Vec3 c, Vec3 e1, Vec3 e2, Vec3 e3, int state, int seed) {
		Vec3 a0 = c.subtract(e1), a1 = c.add(e1);
		Vec3[] corners = {e2.add(e3), e2.subtract(e3), e2.scale(-1).subtract(e3), e3.subtract(e2)};
		double span = e1.length() * 2.0D;
		for (int j = 0; j < 4; j++) {
			Vec3 k0 = corners[j], k1 = corners[(j + 1) % 4];
			face(iron, pose, a0.add(k0), a1.add(k0), a1.add(k1), a0.add(k1), state, 1.0F, 0.0D, span, 0.0D, 1.0D, IRON, seed + j);
		}
	}

	/** A material state: white light (in the grooves, caught in the iron), rawness and wetness, each 0 to 1. */
	static int state(float light, float raw, float wet) {
		return channel(light) << 16 | channel(raw) << 8 | channel(wet);
	}

	/** The same state with its light replaced. */
	static int withLight(int state, float light) {
		return (state & 0x00FFFF) | channel(light) << 16;
	}

	private static int channel(float value) {
		return Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}

	/**
	 * One face, a to b to c to d: U runs across it in blocks (so the grain sits at 16 a block) and V up the spur from
	 * {@code v0} to {@code v1}, which the formation front climbs.
	 */
	private static void face(VertexConsumer v, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int state, float formation,
			double u0, double u1, double v0, double v1, int shape, int tile) {
		int seedTile = MaterialQuads.seedTile(tile);
		float w0 = (float) (seedTile * 2 + Mth.clamp(v0, 0.0D, 0.999D)), w1 = (float) (seedTile * 2 + Mth.clamp(v1, 0.0D, 0.999D));
		float x0 = (float) (shape * 2 + Mth.clamp(u0, 0.0D, 1.9D)), x1 = (float) (shape * 2 + Mth.clamp(u1, 0.0D, 1.9D));
		MaterialQuads.vertex(v, pose, a.x, a.y, a.z, state, formation, x0, w0);
		MaterialQuads.vertex(v, pose, b.x, b.y, b.z, state, formation, x1, w0);
		MaterialQuads.vertex(v, pose, c.x, c.y, c.z, state, formation, x1, w1);
		MaterialQuads.vertex(v, pose, d.x, d.y, d.z, state, formation, x0, w1);
	}
}
