package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.PutricielBloomRules.Cap;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * What the rot pushes up out of Saint Putriciel's floor, as real meshes in their growth material
 * ({@link PutricielRenderTypes#GROWTH}, visual pass 2): bracket caps with a zoned upper face, gilled underside and a
 * scalloped rim, fanned into rosettes round a knot; spore sacs that swell and tear open from the top; roots that
 * breach the floor and arch; and the clods a breach throws. The vertex colour is the material's state, never a tint:
 * red the char, green the wet, blue the bloom. Alpha is the formation, so a cap unfurls out from its root and crumbles
 * back from its rim. Points are camera-relative.
 */
final class PutricielGrowthGeometry {
	/** Shapes the growth shader decodes. */
	static final int CAP = 0, GILL = 1, SAC = 2, ROOT = 3, CLOD = 4;
	/** Material states (char, wet, bloom): a fresh growth, a wet sac, bark and loam. */
	static final int FRESH = state(0.0F, 0.3F, 0.15F), SAC_SKIN = state(0.0F, 0.7F, 0.2F), BARK = state(0.0F, 0.35F, 0.0F),
			LOAM = state(0.0F, 0.2F, 0.0F);
	private static final int FAN = 6;
	private static final double[] STATIONS = {0.0D, 0.55D, 1.0D};
	/** A growth's root starts this far under the floor, so it reads as coming up out of it. */
	private static final double BURIED = 0.06D;

	private PutricielGrowthGeometry() {
	}

	/**
	 * One bracket cap rooted at {@code foot}: {@code size} scales it (0 to the unfurl's overshoot), {@code droop} tips
	 * it down as it withers (0 to 1), {@code quiver} rocks it about its root (radians), {@code formation} is the growth
	 * front. The upper face is zoned, the underside gilled, the rim scalloped and thick.
	 */
	static void cap(PoseStack p, VertexConsumer v, Vec3 foot, Cap cap, double size, double droop, double quiver, int state,
			float formation, int seed) {
		double radius = cap.radius() * size;
		if (radius < 0.02D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double tilt = cap.tilt() - droop * 0.95D + quiver;
		double thick = cap.thickness() * size;
		Vec3 root = foot.add(cap.dx(), cap.lift() * Math.min(1.0D, size), cap.dz());
		Vec3[][] top = new Vec3[STATIONS.length][FAN + 1], under = new Vec3[STATIONS.length][FAN + 1];
		double lobes = 3.0D + Math.floor(PutricielBloomRules.unit(seed, 1) * 3.0D);
		for (int k = 0; k < STATIONS.length; k++) {
			double f = STATIONS[k];
			for (int j = 0; j <= FAN; j++) {
				double a = cap.yaw() - cap.span() / 2.0D + cap.span() * j / FAN;
				double scallop = 1.0D - 0.12D * Math.max(0.0D, Math.cos((a - cap.yaw()) * lobes * 2.0D + seed));
				double r = radius * f * (k == STATIONS.length - 1 ? scallop : 1.0D);
				double ox = Math.cos(a) * r * Math.cos(tilt), oz = Math.sin(a) * r * Math.cos(tilt);
				double lift = Math.sin(tilt) * r;
				// the top domes up between root and rim; the underside dips a little under the root
				double dome = thick * (1.0D - f * f) * 0.8D + thick * 0.25D;
				top[k][j] = root.add(ox, lift + dome, oz);
				under[k][j] = root.add(ox * 0.97D, lift - thick * 0.2D * (1.0D - f), oz * 0.97D);
			}
		}
		for (int k = 0; k + 1 < STATIONS.length; k++) {
			for (int j = 0; j < FAN; j++) {
				double u0 = radius * cap.span() * j / FAN, u1 = radius * cap.span() * (j + 1) / FAN;
				face(v, pose, top[k][j], top[k][j + 1], top[k + 1][j + 1], top[k + 1][j], state, formation, u0, u1, STATIONS[k],
						STATIONS[k + 1], CAP, tile + j);
				face(v, pose, under[k][j], under[k][j + 1], under[k + 1][j + 1], under[k + 1][j], state, formation, u0, u1,
						STATIONS[k], STATIONS[k + 1], GILL, tile + j + 8);
			}
		}
		// the thick rim, and the two ends of the fan
		int rim = STATIONS.length - 1;
		for (int j = 0; j < FAN; j++) {
			double u0 = radius * cap.span() * j / FAN, u1 = radius * cap.span() * (j + 1) / FAN;
			face(v, pose, under[rim][j], under[rim][j + 1], top[rim][j + 1], top[rim][j], state, formation, u0, u1, 0.9D, 1.0D, GILL,
					tile + j + 16);
		}
		for (int end : new int[]{0, FAN}) {
			for (int k = 0; k + 1 < STATIONS.length; k++) {
				face(v, pose, under[k][end], under[k + 1][end], top[k + 1][end], top[k][end], state, formation, 0.0D, thick,
						STATIONS[k], STATIONS[k + 1], GILL, tile + 24 + end);
			}
		}
	}

	/**
	 * A rosette rooted at {@code foot}: its knot and its caps, each unfurling a beat after the one under it,
	 * {@code age} ticks after the rosette broke out, withering from {@code witherAt}.
	 */
	static void rosette(PoseStack p, VertexConsumer v, Vec3 foot, List<Cap> caps, float age, float witherAt, double time,
			int state, int seed) {
		if (age <= 0.0F || caps.isEmpty()) return;
		double scale = caps.get(0).radius() * 2.0D;
		float knotForm = PutricielBloomRules.formation(age, witherAt);
		knot(p, v, foot, scale * 0.32D, scale * 0.22D * Math.min(1.0D, PutricielBloomRules.size(age, witherAt)), knotForm,
				withWet(state, 0.2F), seed + 3);
		float wither = PutricielBloomRules.withered(age, witherAt);
		for (int i = 0; i < caps.size(); i++) {
			Cap cap = caps.get(i);
			float own = age - cap.delay();
			double size = PutricielBloomRules.size(own, witherAt - cap.delay());
			if (size <= 0.01D) continue;
			cap(p, v, foot, cap, size, wither, PutricielBloomRules.quiver(own, time, seed + i), state,
					PutricielBloomRules.formation(own, witherAt - cap.delay()), seed + i * 13);
		}
	}

	/** A squat, uneven knot the caps fan out of: a seven-sided mound of root. */
	static void knot(PoseStack p, VertexConsumer v, Vec3 foot, double radius, double height, float formation, int state, int seed) {
		if (radius < 0.02D || height < 0.01D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int n = 7, tile = MaterialQuads.seedTile(seed);
		Vec3[] base = new Vec3[n], top = new Vec3[n];
		for (int j = 0; j < n; j++) {
			double a = j * Math.PI * 2.0D / n + seed * 0.31D;
			double r = radius * (0.75D + 0.35D * PutricielBloomRules.unit(seed, 600 + j));
			base[j] = foot.add(Math.cos(a) * r, -BURIED, Math.sin(a) * r);
			top[j] = foot.add(Math.cos(a + 0.25D) * r * 0.6D, height * (0.75D + 0.45D * PutricielBloomRules.unit(seed, 620 + j)),
					Math.sin(a + 0.25D) * r * 0.6D);
		}
		Vec3 crown = foot.add(0.0D, height * 1.1D, 0.0D);
		for (int j = 0; j < n; j++) {
			int k = (j + 1) % n;
			face(v, pose, base[j], base[k], top[k], top[j], state, formation, 0.0D, base[j].distanceTo(base[k]), 0.0D, 0.6D, ROOT,
					tile + j);
			face(v, pose, top[j], top[k], crown, crown, state, formation, 0.0D, top[j].distanceTo(top[k]), 0.6D, 1.0D, ROOT, tile + j + 7);
		}
	}

	/**
	 * A spore sac sitting on {@code foot}: a lumpy bulb {@code radius} across and {@code height} tall at full swell,
	 * swollen by {@code swell}, its skin torn open from the top as {@code formation} falls.
	 */
	static void sac(PoseStack p, VertexConsumer v, Vec3 foot, double radius, double height, float swell, float formation, int state,
			int seed) {
		double r = radius * swell, h = height * swell;
		if (r < 0.02D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int n = 8, rings = 5, tile = MaterialQuads.seedTile(seed);
		Vec3[][] grid = new Vec3[rings + 1][n];
		for (int k = 0; k <= rings; k++) {
			double lat = -Math.PI / 2.0D + Math.PI * k / rings;
			double y = (Math.sin(lat) * 0.5D + 0.5D) * h - BURIED;
			for (int j = 0; j < n; j++) {
				double a = j * Math.PI * 2.0D / n + seed * 0.53D;
				double lump = 1.0D + 0.14D * Math.sin(a * 3.0D + seed + k) * Math.cos(lat);
				double rr = Math.cos(lat) * r * lump * (k == 0 ? 0.8D : 1.0D);
				grid[k][j] = foot.add(Math.cos(a) * rr, y, Math.sin(a) * rr);
			}
		}
		double around = Math.PI * 2.0D * r;
		for (int k = 0; k < rings; k++) {
			for (int j = 0; j < n; j++) {
				int m = (j + 1) % n;
				face(v, pose, grid[k][j], grid[k][m], grid[k + 1][m], grid[k + 1][j], state, formation, around * j / n,
						around * (j + 1) / n, k / (double) rings, (k + 1) / (double) rings, SAC, tile + j);
			}
		}
	}

	/**
	 * A root along {@code spine} (camera-relative), {@code radii} thick at each point, shown to {@code shown} of its
	 * points from the first: a tapered faceted tube of bark.
	 */
	static void root(PoseStack p, VertexConsumer v, List<Vec3> spine, double[] radii, double shown, float formation, int state,
			int seed) {
		int count = spine.size();
		if (count < 2 || shown <= 0.0D || formation <= 0.01F) return;
		int last = Math.min(count - 1, (int) Math.ceil(shown * (count - 1)));
		Matrix4f pose = p.last().pose();
		int n = 5, tile = MaterialQuads.seedTile(seed);
		Vec3[] previous = null;
		double previousV = 0.0D;
		for (int i = 0; i <= last; i++) {
			Vec3 at = spine.get(i);
			Vec3 tangent = spine.get(Math.min(count - 1, i + 1)).subtract(spine.get(Math.max(0, i - 1)));
			if (tangent.lengthSqr() < 1.0E-8D) tangent = new Vec3(0, 1, 0);
			tangent = tangent.normalize();
			Vec3 side = tangent.cross(new Vec3(0, 1, 0));
			if (side.lengthSqr() < 1.0E-6D) side = tangent.cross(new Vec3(1, 0, 0));
			side = side.normalize();
			Vec3 bin = side.cross(tangent).normalize();
			double r = radii[Math.min(i, radii.length - 1)] * (i == last && last < count - 1 ? 0.6D : 1.0D);
			Vec3[] ring = new Vec3[n];
			for (int j = 0; j < n; j++) {
				double a = j * Math.PI * 2.0D / n + seed * 0.7D;
				ring[j] = at.add(side.scale(Math.cos(a) * r)).add(bin.scale(Math.sin(a) * r));
			}
			double vHere = i / (double) (count - 1);
			if (previous != null) {
				for (int j = 0; j < n; j++) {
					int m = (j + 1) % n;
					face(v, pose, previous[j], previous[m], ring[m], ring[j], state, formation, r * j, r * (j + 1), previousV, vHere,
							ROOT, tile + j);
				}
			}
			previous = ring;
			previousV = vHere;
		}
		if (previous != null) {
			Vec3 tip = spine.get(last).add(spine.get(last).subtract(spine.get(Math.max(0, last - 1))).normalize().scale(radii[0] * 0.8D));
			for (int j = 0; j < n; j++) {
				int m = (j + 1) % n;
				face(v, pose, previous[j], previous[m], tip, tip, state, formation, 0.0D, 0.1D, previousV, Math.min(1.0D, previousV + 0.05D),
						ROOT, tile + j + 5);
			}
		}
	}

	/** A tumbling clod at {@code at}, {@code size} across: a small uneven octahedron of loam or fungus flesh. */
	static void clod(PoseStack p, VertexConsumer v, Vec3 at, double size, double tumble, int state, int shape, int seed) {
		if (size < 0.01D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double c = Math.cos(tumble), s = Math.sin(tumble);
		double sx = size * (0.8D + 0.4D * PutricielBloomRules.unit(seed, 1)), sy = size * (0.55D + 0.3D * PutricielBloomRules.unit(seed, 2));
		Vec3[] points = {new Vec3(sx * c, sy * s, 0), new Vec3(-sx * c, -sy * s, 0), new Vec3(-sy * s, sx * c * 0.8D, 0),
				new Vec3(sy * s, -sx * c * 0.8D, 0), new Vec3(0, 0, size * 0.65D), new Vec3(0, 0, -size * 0.65D)};
		int[][] faces = {{0, 2, 4}, {2, 1, 4}, {1, 3, 4}, {3, 0, 4}, {2, 0, 5}, {1, 2, 5}, {3, 1, 5}, {0, 3, 5}};
		for (int[] f : faces) {
			Vec3 a = at.add(points[f[0]]), b = at.add(points[f[1]]), d = at.add(points[f[2]]);
			face(v, pose, a, b, d, d, state, 1.0F, 0.0D, size, 0.0D, 1.0D, shape, tile + f[0]);
		}
	}

	/** Clods thrown by a breach at {@code foot}, {@code age} ticks after it; {@code reach} scales how far they fly. */
	static void clods(PoseStack p, VertexConsumer v, Vec3 foot, float age, double reach, int count, int state, int seed) {
		for (int i = 0; i < count; i++) {
			double[] clod = PutricielBloomRules.clod(seed, i, age, reach);
			if (clod == null) continue;
			double size = (0.06D + 0.07D * PutricielBloomRules.unit(seed, 700 + i)) * clod[4];
			clod(p, v, foot.add(clod[0], clod[1] + size * 0.5D, clod[2]), size, clod[3], state, i % 3 == 0 ? CAP : CLOD, seed + i);
		}
	}

	/** A material state: char, wet and bloom, each 0 to 1. */
	static int state(float charred, float wet, float bloom) {
		return PutricielGroundGeometry.color(charred, wet, bloom);
	}

	/** The same state with its char replaced. */
	static int withChar(int state, float charred) {
		return (state & 0x00FFFF) | Math.round(Mth.clamp(charred, 0.0F, 1.0F) * 255.0F) << 16;
	}

	/** The same state with its wet replaced. */
	static int withWet(int state, float wet) {
		return (state & 0xFF00FF) | Math.round(Mth.clamp(wet, 0.0F, 1.0F) * 255.0F) << 8;
	}

	/** The same state with its bloom replaced. */
	static int withBloom(int state, float bloom) {
		return (state & 0xFFFF00) | Math.round(Mth.clamp(bloom, 0.0F, 1.0F) * 255.0F);
	}

	/**
	 * One face, a to b to c to d: U runs across it in blocks (so the grain sits at sixteen a block) and V from
	 * {@code v0} to {@code v1} along it, which the formation front climbs.
	 */
	private static void face(VertexConsumer v, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int state, float formation, double u0,
			double u1, double v0, double v1, int shape, int tile) {
		int seedTile = MaterialQuads.seedTile(tile);
		float w0 = (float) (seedTile * 2 + Mth.clamp(v0, 0.0D, 0.999D)), w1 = (float) (seedTile * 2 + Mth.clamp(v1, 0.0D, 0.999D));
		float x0 = (float) (shape * 2 + Mth.clamp(u0, 0.0D, 1.9D)), x1 = (float) (shape * 2 + Mth.clamp(u1, 0.0D, 1.9D));
		MaterialQuads.vertex(v, pose, a.x, a.y, a.z, state, formation, x0, w0);
		MaterialQuads.vertex(v, pose, b.x, b.y, b.z, state, formation, x1, w0);
		MaterialQuads.vertex(v, pose, c.x, c.y, c.z, state, formation, x1, w1);
		MaterialQuads.vertex(v, pose, d.x, d.y, d.z, state, formation, x0, w1);
	}
}
