package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Face;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Look;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.Seam;
import com.vincenthuto.hemomancy.client.render.world.VelorumFrostRules.TrenchPoint;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Saint Velorum's floor in his frost material ({@link VelorumRenderTypes#FROST}, visual pass 2): frozen seams laid as
 * strips, rime spreading over ground patches with its front carried per vertex, black ice, the sunken trench as the
 * floor plane shows it (its far walls and bed drawn where a camera would see them under the lip), gloom, pools of
 * shadow, folds of darkness, the lantern's light, the shades and the trails cut through the air. Points are world
 * positions; the camera is subtracted here. The vertex colour is state: frost, black ice and glint. Since the Frozen
 * Cruor revision (2026-11-06) the shader's "black ice" is frozen cruor, and it adds the liquid dark, smoke tatters, the
 * floor's block seams with blood in them, stains, gloam cuts and vein ridges ({@link #DARK} to {@link #RIDGE}).
 */
final class VelorumFrostGeometry {
	/** Shapes the frost shader decodes. */
	static final int SEAM = 0, RIME = 1, BLACK = 2, WALL = 3, BED = 4, FOLD = 5, POOL = 6, SHADE = 7, LIGHT = 8, GLOOM = 9,
			TRAIL = 10;
	/**
	 * The Frozen Cruor and liquid dark revision's shapes (2026-11-06): liquid dark spread on the floor (its alpha is its
	 * coverage, torn at every edge), a torn tatter of smoke, the floor's own block seams with blood welling and setting in
	 * them (and the Eclipse's red grid), a stain of spilled blood, a gloam cut and a frozen vein's ridge. {@code BLACK} is
	 * now a sheet of frozen cruor.
	 */
	static final int DARK = 11, SMOKE = 12, SEAMS = 13, STAIN = 14, CUT = 15, RIDGE = 16;
	/** The cruor sheet's old name. */
	static final int CRUOR = BLACK;
	/** Blocks per unit of the shader's ground coordinates. */
	static final double SPAN = 32.0D;
	/** Height over the floor, and a hair more for each seam layer so two never fight. */
	private static final double LIFT = 0.018D, LAYER = 0.0012D;
	/** Patch subdivision: a block across, nine degrees round. */
	private static final double STEP = 1.0D, ARC_STEP = 9.0D;
	/** A seam's root and running tip taper over this many blocks. */
	private static final double TAPER = 0.4D;

	/** Where a footprint's local points land: origin, aim (a horizontal unit vector) and floor, and the camera. */
	record Ground(double ox, double oz, double fx, double fz, double y, Vec3 camera) {
		static Ground of(Vec3 origin, double[] forward, double floorY, Vec3 camera) {
			return new Ground(origin.x, origin.z, forward[0], forward[1], floorY, camera);
		}

		/** The same aim at another origin. */
		Ground at(Vec3 origin) {
			return new Ground(origin.x, origin.z, fx, fz, y, camera);
		}

		/** A lane of the same floor: moved {@code offset} blocks to the right and turned {@code degrees} to the right. */
		Ground lane(double offset, double degrees) {
			double[] f = BossFootprint.Shapes.rotate(new double[]{fx, fz}, degrees);
			return new Ground(wx(0.0D, offset), wz(0.0D, offset), f[0], f[1], y, camera);
		}

		double wx(double along, double right) {
			return ox + fx * along - fz * right;
		}

		double wz(double along, double right) {
			return oz + fz * along + fx * right;
		}

		/** {along, right} of a world point. */
		double[] local(double x, double z) {
			double dx = x - ox, dz = z - oz;
			return new double[]{dx * fx + dz * fz, -dx * fz + dz * fx};
		}

		/** The horizontal world direction of a local {along, right}. */
		Vec3 direction(double along, double right) {
			return new Vec3(fx * along - fz * right, 0.0D, fz * along + fx * right);
		}
	}

	/** A value laid over a patch by its local point, read at every corner and interpolated by the card. */
	interface Field {
		float at(double along, double right);

		static Field of(float value) {
			return (along, right) -> value;
		}
	}

	private static int layer;

	private VelorumFrostGeometry() {
	}

	/** Starts a frame's layering. */
	static void begin() {
		layer = 0;
	}

	/** The material state the shaders read from the vertex colour: frost red, black ice green, glint blue. */
	static int state(float frost, float ice, float glint) {
		return channel(frost) << 16 | channel(ice) << 8 | channel(glint);
	}

	private static int channel(float value) {
		return Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}

	// ── seams ────────────────────────────────────────────────────────

	/** Every seam of a network, each as far as the look's grow has reached it; forks quieter than the seams they leave. */
	static void seams(PoseStack p, VertexConsumer v, Ground g, List<Seam> seams, Look look, float strength, int seed) {
		if (!look.visible() || strength <= 0.0F) return;
		double y = g.y() + LIFT + (layer++ % 10) * LAYER;
		int state = state(look.frost(), look.ice(), look.glint());
		for (int i = 0; i < seams.size(); i++) {
			Seam seam = seams.get(i);
			double shown = VelorumFrostRules.shown(look.grow(), seam.start(), seam.span());
			if (shown <= 0.0D) continue;
			float weight = (float) Mth.clamp(seam.width() / VelorumFrostRules.EDGE, 0.6D, 1.0D);
			seam(p, v, g, y, seam, shown, state, look.alpha() * strength * weight, look.width(), seed + i * 7);
		}
	}

	/** One seam drawn to {@code shown} of its length, tapering at its root and its running tip. */
	private static void seam(PoseStack p, VertexConsumer v, Ground g, double y, Seam seam, double shown, int state, float alpha,
			float width, int seed) {
		int n = seam.size();
		if (n < 2 || alpha <= 0.004F) return;
		double visible = seam.length() * Mth.clamp(shown, 0.0D, 1.0D);
		if (visible < 0.05D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double half = seam.width() * VelorumFrostRules.FEATHER * width;
		double[] xs = seam.x(), zs = seam.y();
		Vec3 c = g.camera();
		double s = 0.0D, previousS = 0.0D;
		double[] previous = null;
		for (int i = 0; i < n; i++) {
			double x = xs[i], z = zs[i];
			boolean last = false;
			if (i > 0) {
				double step = Math.hypot(x - xs[i - 1], z - zs[i - 1]);
				if (s + step >= visible) {
					double t = step < 1.0E-6D ? 0.0D : (visible - s) / step;
					x = xs[i - 1] + (x - xs[i - 1]) * t;
					z = zs[i - 1] + (z - zs[i - 1]) * t;
					step = visible - s;
					last = true;
				}
				s += step;
			}
			double tx = xs[Math.min(n - 1, i + 1)] - xs[Math.max(0, i - 1)];
			double tz = zs[Math.min(n - 1, i + 1)] - zs[Math.max(0, i - 1)];
			double length = Math.hypot(tx, tz);
			if (length > 1.0E-6D) {
				double taper = Math.min(Math.min(1.0D, 0.35D + s / TAPER), Math.min(1.0D, (visible - s) / TAPER + 0.1D));
				double na = -tz / length * half * taper, nr = tx / length * half * taper;
				double[] here = {g.wx(x - na, z - nr), g.wz(x - na, z - nr), g.wx(x + na, z + nr), g.wz(x + na, z + nr)};
				if (previous != null) {
					double base = Math.floor(previousS / SPAN) * SPAN;
					float u0 = (float) (SEAM * 2 + (previousS - base) / SPAN), u1 = (float) (SEAM * 2 + (s - base) / SPAN);
					MaterialQuads.vertex(v, pose, previous[0] - c.x, y - c.y, previous[1] - c.z, state, alpha, u0, tile * 2);
					MaterialQuads.vertex(v, pose, here[0] - c.x, y - c.y, here[1] - c.z, state, alpha, u1, tile * 2);
					MaterialQuads.vertex(v, pose, here[2] - c.x, y - c.y, here[3] - c.z, state, alpha, u1, tile * 2 + 1);
					MaterialQuads.vertex(v, pose, previous[2] - c.x, y - c.y, previous[3] - c.z, state, alpha, u0, tile * 2 + 1);
				}
				previous = here;
				previousS = s;
			}
			if (last) break;
		}
	}

	// ── ground patches ───────────────────────────────────────────────

	/**
	 * An annular sector of floor round the ground's origin in a ground shape (rime, black ice, bed or gloom): radii r0 to
	 * r1, bearings from and to about the aim. Frost and strength are fields, so a front or a gradient runs across it.
	 */
	static void annulus(PoseStack p, VertexConsumer v, Ground g, int shape, double r0, double r1, double fromDeg, double toDeg,
			Field frost, float ice, float glint, Field alpha, int seed) {
		if (r1 <= r0 || toDeg <= fromDeg) return;
		double y = g.y() + LIFT * 0.5D;
		int angles = Math.max(4, (int) Math.ceil((toDeg - fromDeg) / ARC_STEP));
		int rings = Math.max(1, (int) Math.ceil((r1 - r0) / STEP));
		for (int i = 0; i < angles; i++) {
			double a0 = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / angles);
			double a1 = Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / angles);
			for (int k = 0; k < rings; k++) {
				double q0 = r0 + (r1 - r0) * k / rings, q1 = r0 + (r1 - r0) * (k + 1) / rings;
				patch(p, v, g, shape, y, new double[]{Math.cos(a0) * q0, Math.cos(a0) * q1, Math.cos(a1) * q1, Math.cos(a1) * q0},
						new double[]{Math.sin(a0) * q0, Math.sin(a0) * q1, Math.sin(a1) * q1, Math.sin(a1) * q0}, frost, ice, glint,
						alpha, seed);
			}
		}
	}

	/** A rectangle of floor in a ground shape, in the ground's frame: along a0 to a1, right b0 to b1. */
	static void rect(PoseStack p, VertexConsumer v, Ground g, int shape, double a0, double a1, double b0, double b1, Field frost,
			float ice, float glint, Field alpha, int seed) {
		if (a1 <= a0 || b1 <= b0) return;
		double y = g.y() + LIFT * 0.5D;
		int along = Math.max(1, (int) Math.ceil((a1 - a0) / STEP));
		int across = Math.max(1, (int) Math.ceil((b1 - b0) / STEP));
		for (int i = 0; i < along; i++) {
			double x0 = a0 + (a1 - a0) * i / along, x1 = a0 + (a1 - a0) * (i + 1) / along;
			for (int k = 0; k < across; k++) {
				double z0 = b0 + (b1 - b0) * k / across, z1 = b0 + (b1 - b0) * (k + 1) / across;
				patch(p, v, g, shape, y, new double[]{x0, x1, x1, x0}, new double[]{z0, z0, z1, z1}, frost, ice, glint, alpha, seed);
			}
		}
	}

	/** One ground quad; its shader coordinates are its ground position about the ground's origin, so patches join. */
	private static void patch(PoseStack p, VertexConsumer v, Ground g, int shape, double y, double[] along, double[] right,
			Field frost, float ice, float glint, Field alpha, int seed) {
		float[] frosts = new float[4], alphas = new float[4];
		float most = 0.0F, mostFrost = 0.0F;
		for (int i = 0; i < 4; i++) {
			frosts[i] = frost.at(along[i], right[i]);
			alphas[i] = alpha.at(along[i], right[i]);
			most = Math.max(most, alphas[i]);
			mostFrost = Math.max(mostFrost, frosts[i]);
		}
		if (most <= 0.004F) return;
		// rime with no frost and no ice draws nothing, so skip it
		if (shape == RIME && mostFrost <= 0.004F && ice <= 0.004F && glint <= 0.004F) return;
		// seams that no blood has reached and no glow lights draw nothing either
		if (shape == SEAMS && mostFrost <= 0.004F && glint <= 0.004F) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		Vec3 c = g.camera();
		// the floor's block seams lie on whole blocks, so their patches count from a whole-block corner
		double bx = shape == SEAMS ? Math.floor(g.ox()) : g.ox(), bz = shape == SEAMS ? Math.floor(g.oz()) : g.oz();
		for (int i = 0; i < 4; i++) {
			double wx = g.wx(along[i], right[i]), wz = g.wz(along[i], right[i]);
			float u = (float) (shape * 2 + 1.0D + Mth.clamp((wx - bx) / SPAN, -0.99D, 0.99D));
			float w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - bz) / SPAN, -0.99D, 0.99D));
			MaterialQuads.vertex(v, pose, wx - c.x, y - c.y, wz - c.z, state(frosts[i], ice, glint), alphas[i], u, w);
		}
	}

	// ── the trench ───────────────────────────────────────────────────

	/**
	 * A trench sunk {@code depth} into the floor along a lane's ground from {@code a0} to {@code a1}, {@code half} wide
	 * either side: its bed (empty shadow, or black ice as the ice rises) and the walls the camera sees, all on the floor
	 * plane where they would show under the lip ({@link VelorumFrostRules#trench}). A camera under the floor sees none.
	 */
	static void trench(PoseStack p, VertexConsumer v, Ground lane, double a0, double a1, double half, double depth, boolean nearEnd,
			boolean farEnd, float frost, float ice, float glint, float alpha, int seed) {
		if (alpha <= 0.004F || a1 <= a0) return;
		Vec3 c = lane.camera();
		double[] eye = lane.local(c.x, c.z);
		double y = lane.y() + LIFT * 0.6D;
		List<Face> faces = VelorumFrostRules.trench(a0, a1, half, depth, eye[0], eye[1], c.y - lane.y(), nearEnd, farEnd);
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int color = state(frost, ice, glint);
		for (Face face : faces) {
			List<TrenchPoint> points = face.points();
			int n = points.size();
			// fan the polygon into quads; a repeated last corner closes a triangle
			for (int i = 1; i + 1 < n; i += 2) {
				TrenchPoint[] quad = {points.get(0), points.get(i), points.get(i + 1), points.get(Math.min(i + 2, n - 1))};
				for (TrenchPoint corner : quad) {
					double wx = lane.wx(corner.a(), corner.r()), wz = lane.wz(corner.a(), corner.r());
					float u, w;
					if (face.wall()) {
						u = (float) (WALL * 2 + Mth.clamp((corner.a() + corner.r() + 8.0D) / SPAN, 0.0D, 0.999D));
						w = (float) (tile * 2 + Mth.clamp(corner.v(), 0.0D, 0.999D));
					} else {
						u = (float) (BED * 2 + 1.0D + Mth.clamp((wx - lane.ox()) / SPAN, -0.99D, 0.99D));
						w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - lane.oz()) / SPAN, -0.99D, 0.99D));
					}
					MaterialQuads.vertex(v, pose, wx - c.x, y - c.y, wz - c.z, color, alpha, u, w);
				}
			}
		}
	}

	// ── cards ────────────────────────────────────────────────────────

	/** A card over four camera-relative corners: a bottom left, b bottom right, c top right, d top left. */
	static void card(PoseStack p, VertexConsumer v, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int shape, int state, float alpha, int seed) {
		if (alpha <= 0.004F) return;
		MaterialQuads.quad(v, p.last().pose(), a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z, d.x, d.y, d.z, shape,
				MaterialQuads.seedTile(seed), state, alpha, 0.0D, 1.0D, false);
	}

	/** A square card lying on the floor at a camera-relative centre: a pool of shadow. */
	static void pool(PoseStack p, VertexConsumer v, Vec3 centre, double radius, int state, float alpha, int seed) {
		Vec3 lifted = centre.add(0.0D, LIFT * 0.8D, 0.0D);
		card(p, v, lifted.add(-radius, 0.0D, -radius), lifted.add(radius, 0.0D, -radius), lifted.add(radius, 0.0D, radius),
				lifted.add(-radius, 0.0D, radius), POOL, state, alpha, seed);
	}

	/**
	 * A figure card standing on the floor at a camera-relative foot, turned about the vertical to face the camera
	 * (a shade of the Procession).
	 */
	static void upright(PoseStack p, VertexConsumer v, Vec3 foot, double width, double height, int shape, int state, float alpha,
			int seed) {
		double dx = -foot.x, dz = -foot.z;
		double length = Math.hypot(dx, dz);
		Vec3 side = length < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : new Vec3(dz / length, 0.0D, -dx / length);
		Vec3 a = foot.subtract(side.scale(width)), b = foot.add(side.scale(width));
		card(p, v, a, b, b.add(0.0D, height, 0.0D), a.add(0.0D, height, 0.0D), shape, state, alpha, seed);
	}

	/**
	 * The lantern's light laid on the floor: a fan from {@code inner} out to each ray's own reach (where a pillar or a
	 * figure stopped it), the rays spread evenly from {@code fromDeg} to {@code toDeg} about the ground's aim.
	 */
	static void light(PoseStack p, VertexConsumer v, Ground g, double inner, double[] reach, double fromDeg, double toDeg,
			double maxReach, float glint, float alpha, int seed) {
		int rays = reach.length;
		if (rays < 2 || alpha <= 0.004F) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int state = state(0.4F, 0.0F, glint);
		double y = g.y() + LIFT * 0.7D;
		Vec3 c = g.camera();
		for (int i = 0; i + 1 < rays; i++) {
			double b0 = fromDeg + (toDeg - fromDeg) * i / (rays - 1), b1 = fromDeg + (toDeg - fromDeg) * (i + 1) / (rays - 1);
			double r0 = Math.max(inner + 0.05D, reach[i]), r1 = Math.max(inner + 0.05D, reach[i + 1]);
			double[][] corners = {{inner, b0, i}, {r0, b0, i}, {r1, b1, i + 1}, {inner, b1, i + 1}};
			for (double[] corner : corners) {
				double a = Math.toRadians(corner[1]);
				double along = Math.cos(a) * corner[0], right = Math.sin(a) * corner[0];
				float u = (float) (LIGHT * 2 + Mth.clamp(corner[0] / Math.max(0.1D, maxReach), 0.0D, 0.999D));
				float w = (float) (tile * 2 + Mth.clamp(corner[2] / (rays - 1), 0.0D, 0.999D));
				MaterialQuads.vertex(v, pose, g.wx(along, right) - c.x, y - c.y, g.wz(along, right) - c.z, state, alpha, u, w);
			}
		}
	}

	/** A trail cut through the air along camera-relative points, its half-width at each point; head last. */
	static void trail(PoseStack p, VertexConsumer v, List<Vec3> points, double[] widths, float frost, float dark, float glint,
			float alpha, int seed) {
		LuxUmbraGeometry.ribbonPath(p, v, points, Vec3.ZERO, widths, TRAIL, seed, state(frost, dark, glint), alpha);
	}

	// ── the revision's pieces (2026-11-06) ──────────────────────────

	/** A flat card on the floor at a camera-relative centre, turned {@code yaw} radians: a stain of spilled blood. */
	static void flat(PoseStack p, VertexConsumer v, Vec3 centre, double radius, double yaw, int shape, int state, float alpha, int seed) {
		Vec3 lifted = centre.add(0.0D, LIFT * 0.9D + (layer++ % 10) * LAYER, 0.0D);
		double c = Math.cos(yaw) * radius, s = Math.sin(yaw) * radius;
		card(p, v, lifted.add(-c + s, 0.0D, -s - c), lifted.add(c + s, 0.0D, s - c), lifted.add(c - s, 0.0D, s + c),
				lifted.add(-c - s, 0.0D, -s + c), shape, state, alpha, seed);
	}

	/**
	 * A strip laid flat on the floor at height {@code y} along world points {x, z, x, z, ...}: a frozen vein's ridge (U
	 * along it in blocks / 32, V across it). Shown to {@code shown} blocks of its length; the last {@code head} blocks of
	 * what is shown carry {@code headGlint} in the glint, the vein's running bead.
	 */
	static void strip(PoseStack p, VertexConsumer v, double y, double[] xz, double shown, double half, int shape, float frost,
			float ice, float glint, double head, float headGlint, float alpha, Vec3 camera, int seed) {
		int n = xz.length / 2;
		if (n < 2 || alpha <= 0.004F || shown <= 0.02D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double s = 0.0D;
		double lift = y + LIFT * 1.1D;
		for (int i = 0; i + 1 < n; i++) {
			double x0 = xz[i * 2], z0 = xz[i * 2 + 1], x1 = xz[i * 2 + 2], z1 = xz[i * 2 + 3];
			double length = Math.hypot(x1 - x0, z1 - z0);
			if (length < 1.0E-4D) continue;
			if (s >= shown) break;
			double end = Math.min(length, shown - s);
			double tx = (x1 - x0) / length, tz = (z1 - z0) / length;
			// a bevel of half a width past each corner, so the turns of a vein along the seams read as one ridge
			double back = i == 0 ? 0.0D : half;
			double ax = x0 - tx * back, az = z0 - tz * back, bx = x0 + tx * end, bz = z0 + tz * end;
			double nx = -tz * half, nz = tx * half;
			float u0 = (float) (shape * 2 + Mth.clamp((s - back) / SPAN, 0.0D, 0.999D)), u1 = (float) (shape * 2 + Mth.clamp((s + end) / SPAN, 0.0D, 0.999D));
			int back0 = state(frost, ice, s - back >= shown - head ? headGlint : glint);
			int front = state(frost, ice, s + end >= shown - head ? headGlint : glint);
			MaterialQuads.vertex(v, pose, ax - nx - camera.x, lift - camera.y, az - nz - camera.z, back0, alpha, u0, tile * 2);
			MaterialQuads.vertex(v, pose, bx - nx - camera.x, lift - camera.y, bz - nz - camera.z, front, alpha, u1, tile * 2);
			MaterialQuads.vertex(v, pose, bx + nx - camera.x, lift - camera.y, bz + nz - camera.z, front, alpha, u1, tile * 2 + 1);
			MaterialQuads.vertex(v, pose, ax + nx - camera.x, lift - camera.y, az + nz - camera.z, back0, alpha, u0, tile * 2 + 1);
			s += length;
		}
	}

	/** The length of a strip's world points {x, z, ...}. */
	static double length(double[] xz) {
		double total = 0.0D;
		for (int i = 0; i + 3 < xz.length; i += 2) total += Math.hypot(xz[i + 2] - xz[i], xz[i + 3] - xz[i + 1]);
		return total;
	}

	/** The world point {x, z} {@code distance} blocks along a strip's points. */
	static double[] along(double[] xz, double distance) {
		double s = 0.0D;
		for (int i = 0; i + 3 < xz.length; i += 2) {
			double length = Math.hypot(xz[i + 2] - xz[i], xz[i + 3] - xz[i + 1]);
			if (s + length >= distance && length > 1.0E-6D) {
				double t = (distance - s) / length;
				return new double[]{xz[i] + (xz[i + 2] - xz[i]) * t, xz[i + 1] + (xz[i + 3] - xz[i + 1]) * t};
			}
			s += length;
		}
		return xz.length >= 2 ? new double[]{xz[xz.length - 2], xz[xz.length - 1]} : new double[]{0.0D, 0.0D};
	}
}
