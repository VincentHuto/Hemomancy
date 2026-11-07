package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Shard;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Saint Velorum's ice as real meshes in his ice material ({@link VelorumRenderTypes#ICE}, visual pass 2): faceted shards
 * in three tapering stages that twist and lean (round crystals, or flat blades for black-ice splinters and heaved
 * plates), the low rime collar a cluster breaks out of, tumbling chips, and the clear panes of a preserving shell. The
 * vertex colour is the material's state, never a tint: red the rime crusted on it, green how black it is, blue its
 * glint. The alpha is the formation, so ice sublimes from its point down. Points are camera-relative.
 */
final class VelorumIceGeometry {
	/** Shapes the ice shader decodes: a shard face, a chip, a shell pane and a clear (translucent) shard. */
	static final int SHARD = 0, CHIP = 1, PANE = 2, CLEAR = 3;
	/**
	 * Material states (frost, thickness, glint). Since the Frozen Cruor revision (2026-11-06) every one of them is cruor:
	 * {@code BLACK_ICE} the black-red blades of the Release and the field, {@code CRYSTAL} thinner lit cruor crusted with
	 * frost (splinters, spines, shins, the struck), {@code PLATE} the heaved slabs, {@code FROSTED} frost-white collars and
	 * chips. The names below say what they are now.
	 */
	static final int BLACK_ICE = state(0.08F, 1.0F, 0.0F), CRYSTAL = state(0.5F, 0.1F, 0.0F), PLATE = state(0.7F, 0.55F, 0.0F),
			FROSTED = state(0.95F, 0.25F, 0.0F);
	static final int CRUOR_BLADE = BLACK_ICE, CRUOR_SPLINTER = CRYSTAL, CRUOR_SLAB = PLATE, HOARFROST = FROSTED;
	/** A javelin's shaft (thick, a frost skin) and a pulse's spine (thinner, frost on its point). */
	static final int CRUOR_SHAFT = state(0.3F, 0.8F, 0.0F), CRUOR_SPINE = state(0.4F, 0.6F, 0.0F);
	/** The mound a cluster breaks out of: the floor's own blood set dark, frost only on what lies level (polish round 1). */
	static final int CRUOR_COLLAR = state(0.25F, 0.8F, 0.0F);
	private static final double[] STAGE = {0.0D, 0.5D, 0.82D};
	private static final double[] RADIUS = {1.0D, 0.72D, 0.34D};
	private static final double TWIST = 0.12D;
	/** A shard's foot starts this far under the floor, so it reads as breaking out of it. */
	private static final double BURIED = 0.08D;

	private VelorumIceGeometry() {
	}

	/** A material state: rime, how black, and the glint, each 0 to 1. */
	static int state(float rime, float black, float glint) {
		return VelorumFrostGeometry.state(rime, black, glint);
	}

	/** The same state with its glint replaced. */
	static int withGlint(int state, float glint) {
		return (state & 0xFFFF00) | Math.round(Mth.clamp(glint, 0.0F, 1.0F) * 255.0F);
	}

	/**
	 * One shard from {@code foot} along {@code axis}, {@code length} long: a prism of {@code sides} facets in three
	 * tapering stages and a point, {@code radius} thick, squashed to {@code flat} across its blade (turned {@code yaw}).
	 */
	static void shard(PoseStack p, VertexConsumer v, Vec3 foot, Vec3 axis, double length, double radius, double flat, double yaw,
			int sides, float formation, int state, int shape, int seed) {
		if (length < 0.02D || radius <= 0.0D || formation <= 0.01F) return;
		Vec3 dir = axis.lengthSqr() < 1.0E-8D ? new Vec3(0.0D, 1.0D, 0.0D) : axis.normalize();
		Vec3 reference = Math.abs(dir.y) > 0.9D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(0.0D, 1.0D, 0.0D);
		Vec3 a0 = reference.subtract(dir.scale(reference.dot(dir))).normalize();
		Vec3 b0 = dir.cross(a0).normalize();
		Vec3 across = a0.scale(Math.cos(yaw)).add(b0.scale(Math.sin(yaw)));
		Vec3 side = dir.cross(across).normalize();
		int n = Math.max(3, sides);
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		Vec3[][] rings = new Vec3[STAGE.length][n];
		double[] heights = new double[STAGE.length];
		for (int k = 0; k < STAGE.length; k++) {
			double h = k == 0 ? -BURIED : length * STAGE[k];
			heights[k] = h;
			double r = radius * RADIUS[k];
			for (int j = 0; j < n; j++) {
				double a = j * Math.PI * 2.0D / n + k * TWIST;
				double irregular = 0.82D + 0.36D * VelorumFrostRules.unit(seed, 500 + j);
				rings[k][j] = foot.add(dir.scale(h)).add(across.scale(Math.cos(a) * r * irregular))
						.add(side.scale(Math.sin(a) * r * irregular * flat));
			}
		}
		Vec3 tip = foot.add(dir.scale(length)).add(across.scale(radius * 0.16D * (VelorumFrostRules.unit(seed, 520) - 0.5D)));
		for (int k = 0; k < rings.length; k++) {
			double v0 = Math.max(0.0D, heights[k]) / length, v1 = k + 1 < rings.length ? heights[k + 1] / length : 1.0D;
			for (int j = 0; j < n; j++) {
				Vec3 a = rings[k][j], b = rings[k][(j + 1) % n];
				Vec3 c = k + 1 < rings.length ? rings[k + 1][(j + 1) % n] : tip, d = k + 1 < rings.length ? rings[k + 1][j] : tip;
				face(v, pose, a, b, c, d, state, formation, a.distanceTo(b), v0, v1, shape, tile + j);
			}
		}
	}

	/** A shard of a cluster standing at {@code foot}, grown to {@code grown} of its height. */
	static void stand(PoseStack p, VertexConsumer v, Vec3 foot, Shard s, double grown, float formation, int state, int shape,
			int seed) {
		double h = s.height() * grown;
		if (h < 0.02D) return;
		Vec3 axis = new Vec3(s.leanX(), 1.0D, s.leanZ());
		shard(p, v, foot.add(s.dx(), 0.0D, s.dz()), axis, h * axis.length(), s.radius(), s.flat(), s.yaw(), s.sides(), formation,
				state, shape, seed);
	}

	/**
	 * A cluster broken out {@code age} ticks ago and crumbling from {@code crumbleAt}: its shards punch up a beat apart,
	 * the centre first, each with the cold light of its birth; with {@code collar} a squat rime mound rings their feet.
	 */
	static void cluster(PoseStack p, VertexConsumer v, Vec3 foot, List<Shard> shards, float age, float crumbleAt, int state,
			int shape, boolean collar, int seed) {
		double grown = VelorumFrostRules.height(age, crumbleAt);
		if (grown <= 0.01D) return;
		if (collar && !shards.isEmpty()) {
			double girth = shards.get(0).radius();
			collar(p, v, foot, girth * 2.1D, girth * 0.7D * Math.min(1.0D, grown), VelorumFrostRules.formation(age, crumbleAt),
					withGlint(CRUOR_COLLAR, VelorumFrostRules.birthGlint(age)), shape, seed + 9);
		}
		for (int i = 0; i < shards.size(); i++) {
			float own = age - i * 0.5F;
			double h = i == 0 ? grown : VelorumFrostRules.height(own, crumbleAt);
			stand(p, v, foot, shards.get(i), h, VelorumFrostRules.formation(own, crumbleAt),
					withGlint(state, VelorumFrostRules.birthGlint(own)), shape, seed + i * 17);
		}
	}

	/** A squat, uneven six-sided mound of rime round a cluster's feet. */
	static void collar(PoseStack p, VertexConsumer v, Vec3 foot, double radius, double height, float formation, int state, int shape,
			int seed) {
		if (radius < 0.02D || height < 0.01D || formation <= 0.01F) return;
		Matrix4f pose = p.last().pose();
		int n = 6, tile = MaterialQuads.seedTile(seed + 3);
		Vec3[] base = new Vec3[n], top = new Vec3[n];
		for (int j = 0; j < n; j++) {
			double a = j * Math.PI * 2.0D / n + seed * 0.37D;
			double r = radius * (0.75D + 0.35D * VelorumFrostRules.unit(seed, 2000 + j));
			base[j] = foot.add(Math.cos(a) * r, -0.04D, Math.sin(a) * r);
			top[j] = foot.add(Math.cos(a + 0.25D) * r * 0.6D, height * (0.7D + 0.5D * VelorumFrostRules.unit(seed, 2100 + j)),
					Math.sin(a + 0.25D) * r * 0.6D);
		}
		Vec3 crown = foot.add(0.0D, height * 1.15D, 0.0D);
		for (int j = 0; j < n; j++) {
			int k = (j + 1) % n;
			face(v, pose, base[j], base[k], top[k], top[j], state, formation, base[j].distanceTo(base[k]), 0.0D, 0.6D, shape, tile + j);
			face(v, pose, top[j], top[k], crown, crown, state, formation, top[j].distanceTo(top[k]), 0.6D, 1.0D, shape, tile + j + 7);
		}
	}

	/** A tumbling chip: a small uneven octahedron at {@code at}, {@code size} across. */
	static void chip(PoseStack p, VertexConsumer v, Vec3 at, double size, double tumble, int state, int seed) {
		if (size < 0.01D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double c = Math.cos(tumble), s = Math.sin(tumble);
		double sx = size * (0.8D + 0.4D * VelorumFrostRules.unit(seed, 1)), sy = size * (0.45D + 0.3D * VelorumFrostRules.unit(seed, 2));
		Vec3[] points = {new Vec3(sx * c, sy * s, 0.0D), new Vec3(-sx * c, -sy * s, 0.0D), new Vec3(-sy * s, sx * c * 0.8D, 0.0D),
				new Vec3(sy * s, -sx * c * 0.8D, 0.0D), new Vec3(0.0D, 0.0D, size * 0.6D), new Vec3(0.0D, 0.0D, -size * 0.6D)};
		int[][] faces = {{0, 2, 4}, {2, 1, 4}, {1, 3, 4}, {3, 0, 4}, {2, 0, 5}, {1, 2, 5}, {3, 1, 5}, {0, 3, 5}};
		for (int[] f : faces) {
			Vec3 a = at.add(points[f[0]]), b = at.add(points[f[1]]), d = at.add(points[f[2]]);
			face(v, pose, a, b, d, d, state, 1.0F, size, 0.0D, 1.0D, CHIP, tile + f[0]);
		}
	}

	/** Chips thrown by a burst at {@code foot}, {@code age} ticks after it; {@code reach} scales how far they fly. */
	static void chips(PoseStack p, VertexConsumer v, Vec3 foot, float age, double reach, int count, int state, int seed) {
		for (int i = 0; i < count; i++) {
			double[] chip = VelorumFrostRules.chip(seed, i, age, reach);
			if (chip == null) continue;
			double size = (0.06D + 0.07D * VelorumFrostRules.unit(seed, 2200 + i)) * chip[4];
			chip(p, v, foot.add(chip[0], chip[1] + size * 0.5D, chip[2]), size, chip[3], i % 3 == 0 ? FROSTED : state, seed + i);
		}
	}

	/** A pane of a shell over four camera-relative corners, bottom edge first; it grows up from the bottom by {@code formation}. */
	static void pane(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int state, float formation, int seed) {
		face(v, p.last().pose(), a, b, c, d, state, formation, a.distanceTo(b), 0.0D, 1.0D, PANE, MaterialQuads.seedTile(seed));
	}

	/**
	 * One face, a to b to c to d: U runs across it in blocks (so the grain sits at sixteen a block) and V up it from
	 * {@code v0} to {@code v1}, which the formation front climbs.
	 */
	private static void face(VertexConsumer v, Matrix4f pose, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int state, float formation,
			double width, double v0, double v1, int shape, int tile) {
		int seedTile = MaterialQuads.seedTile(tile);
		state = crusted(state, a, b, d);
		float w0 = (float) (seedTile * 2 + Mth.clamp(v0, 0.0D, 0.999D)), w1 = (float) (seedTile * 2 + Mth.clamp(v1, 0.0D, 0.999D));
		float x0 = (float) (shape * 2), x1 = (float) (shape * 2 + Mth.clamp(width, 0.0D, 1.9D));
		MaterialQuads.vertex(v, pose, a.x, a.y, a.z, state, formation, x0, w0);
		MaterialQuads.vertex(v, pose, b.x, b.y, b.z, state, formation, x1, w0);
		MaterialQuads.vertex(v, pose, c.x, c.y, c.z, state, formation, x1, w1);
		MaterialQuads.vertex(v, pose, d.x, d.y, d.z, state, formation, x0, w1);
	}

	/**
	 * Frost settles on what faces up: a face's frost (the state's red) is raised with how level it lies, so the tops of
	 * slabs, collars and leaning points crust pale while the sides stay glassy cruor. Points are world-aligned.
	 */
	static int crusted(int state, Vec3 a, Vec3 b, Vec3 d) {
		Vec3 n = b.subtract(a).cross(d.subtract(a));
		double length = n.length();
		if (length < 1.0E-9D) return state;
		double up = Math.abs(n.y / length);
		if (up < 0.45D) return state;
		int red = state >> 16 & 255;
		int raised = Math.min(255, red + (int) Math.round((up - 0.45D) / 0.55D * 130.0D));
		return (state & 0x00FFFF) | raised << 16;
	}
}
