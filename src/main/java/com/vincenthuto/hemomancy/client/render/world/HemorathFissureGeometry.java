package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Crack;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Look;
import com.vincenthuto.hemomancy.client.render.world.HemorathFissureRules.Trench;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.DoubleFunction;

/**
 * The floor cracking under Saint Hemorath, in his fissure material ({@link HemorathRenderTypes#FISSURE}): each crack a
 * strip whose shader carves the gap, its lit seam, the blood welling in it, its broken lip and the scorched, rusting
 * stone round it; crazed patches of floor for the area a footprint covers; and sunken trenches, whose mouth lies on
 * the floor over a lane while the shader looks down into the channel under it. Points are world positions; the
 * camera is subtracted here. The look's heat, blood and rust ride in the vertex colour.
 */
final class HemorathFissureGeometry {
	/** Shapes the fissure shader decodes. */
	static final int CRACK = 0, CRAZE = 1, TRENCH = 2;
	/** A trench's mouth is cut into quads this many blocks long, each end carrying the trench's state there. */
	private static final double TRENCH_STEP = 0.5D;
	/** Blocks per unit of the shader's coordinates. */
	static final double SPAN = 32.0D;
	/** Height of the cracks above the floor, and a hair more for each drawn layer so two never fight. */
	private static final double LIFT = 0.016D, LAYER = 0.0011D;
	private static final double ARC_STEP = 8.0D, PATCH_STEP = 2.0D;
	/** A crack's lead-in and growing tip taper over this many blocks. */
	private static final double TAPER = 0.45D;

	/** Where a footprint's local crack points land: origin, aim and floor, and the camera they draw relative to. */
	record Ground(double ox, double oz, double fx, double fz, double y, Vec3 camera) {
		static Ground of(Vec3 origin, double[] forward, double floorY, Vec3 camera) {
			return new Ground(origin.x, origin.z, forward[0], forward[1], floorY, camera);
		}

		/** The same aim at another origin. */
		Ground at(Vec3 origin) {
			return new Ground(origin.x, origin.z, fx, fz, y, camera);
		}

		double wx(double along, double right) {
			return ox + fx * along - fz * right;
		}

		double wz(double along, double right) {
			return oz + fz * along + fx * right;
		}
	}

	private static int layer;

	private HemorathFissureGeometry() {
	}

	/** Starts a frame's layering. */
	static void begin() {
		layer = 0;
	}

	/** Every crack of a network, each as far as the look's run has reached it. */
	static void cracks(PoseStack p, VertexConsumer v, Ground g, List<Crack> cracks, Look look, float strength, int seed) {
		if (!look.visible() || strength <= 0.0F) return;
		double y = g.y() + LIFT + (layer++ % 12) * LAYER;
		for (int i = 0; i < cracks.size(); i++) {
			Crack crack = cracks.get(i);
			double shown = HemorathFissureRules.shown(look.grow(), crack.start(), crack.span());
			if (shown > 0.0D) crack(p, v, g, y, crack, shown, look, strength, seed + i * 7);
		}
	}

	/** One crack drawn to {@code shown} of its length, tapering at its root and at its running tip. */
	static void crack(PoseStack p, VertexConsumer v, Ground g, double y, Crack crack, double shown, Look look, float strength,
			int seed) {
		int n = crack.size();
		if (n < 2) return;
		double total = crack.length();
		double visible = total * Mth.clamp(shown, 0.0D, 1.0D);
		if (visible < 0.05D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int color = color(look.heat(), look.blood(), look.rust());
		// a hierarchy: the faults and edges carry the shape, the branches between them stay quieter
		float alpha = look.alpha() * strength * (float) Mth.clamp(crack.width() / HemorathFissureRules.EDGE, 0.5D, 1.0D);
		double half = crack.width() * HemorathFissureRules.HALO * look.width();
		double[] xs = crack.x(), zs = crack.y();
		Vec3 c = g.camera();
		double s = 0.0D, previousS = 0.0D;
		double[] previous = null;
		for (int i = 0; i < n; i++) {
			double x = xs[i], z = zs[i];
			boolean last = false;
			if (i > 0) {
				double step = Math.hypot(x - xs[i - 1], z - zs[i - 1]);
				if (s + step >= visible) {
					double t = step < 1e-6 ? 0.0D : (visible - s) / step;
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
			if (length > 1e-6) {
				double taper = Math.min(Math.min(1.0D, 0.35D + s / TAPER), Math.min(1.0D, (visible - s) / TAPER + 0.08D));
				double na = -tz / length * half * taper, nr = tx / length * half * taper;
				double[] here = {g.wx(x - na, z - nr), g.wz(x - na, z - nr), g.wx(x + na, z + nr), g.wz(x + na, z + nr)};
				if (previous != null) {
					double base = Math.floor(previousS / SPAN) * SPAN;
					float u0 = (float) (CRACK * 2 + (previousS - base) / SPAN), u1 = (float) (CRACK * 2 + (s - base) / SPAN);
					MaterialQuads.vertex(v, pose, previous[0] - c.x, y - c.y, previous[1] - c.z, color, alpha, u0, tile * 2);
					MaterialQuads.vertex(v, pose, here[0] - c.x, y - c.y, here[1] - c.z, color, alpha, u1, tile * 2);
					MaterialQuads.vertex(v, pose, here[2] - c.x, y - c.y, here[3] - c.z, color, alpha, u1, tile * 2 + 1);
					MaterialQuads.vertex(v, pose, previous[2] - c.x, y - c.y, previous[3] - c.z, color, alpha, u0, tile * 2 + 1);
				}
				previous = here;
				previousS = s;
			}
			if (last) break;
		}
	}

	/** A crazed annular sector of floor round {@code centre}: radii r0 to r1, bearings from and to about the aim. */
	static void craze(PoseStack p, VertexConsumer v, Ground g, double r0, double r1, double fromDeg, double toDeg, Look look,
			float strength, int seed) {
		if (!look.visible() || strength <= 0.0F || r1 <= r0 || toDeg <= fromDeg) return;
		double y = g.y() + LIFT * 0.5D;
		int angles = Math.max(4, (int) Math.ceil((toDeg - fromDeg) / ARC_STEP));
		int rings = Math.max(1, (int) Math.ceil((r1 - r0) / PATCH_STEP));
		for (int i = 0; i < angles; i++) {
			double a0 = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / angles);
			double a1 = Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / angles);
			for (int k = 0; k < rings; k++) {
				double q0 = r0 + (r1 - r0) * k / rings, q1 = r0 + (r1 - r0) * (k + 1) / rings;
				patch(p, v, g, y, new double[]{Math.cos(a0) * q0, Math.cos(a0) * q1, Math.cos(a1) * q1, Math.cos(a1) * q0},
						new double[]{Math.sin(a0) * q0, Math.sin(a0) * q1, Math.sin(a1) * q1, Math.sin(a1) * q0}, look, strength, seed);
			}
		}
	}

	/** A crazed rectangle of floor in the footprint's frame: along from a0 to a1, right from b0 to b1. */
	static void crazeRect(PoseStack p, VertexConsumer v, Ground g, double a0, double a1, double b0, double b1, Look look,
			float strength, int seed) {
		if (!look.visible() || strength <= 0.0F || a1 <= a0 || b1 <= b0) return;
		double y = g.y() + LIFT * 0.5D;
		int along = Math.max(1, (int) Math.ceil((a1 - a0) / PATCH_STEP));
		int across = Math.max(1, (int) Math.ceil((b1 - b0) / PATCH_STEP));
		for (int i = 0; i < along; i++) {
			double x0 = a0 + (a1 - a0) * i / along, x1 = a0 + (a1 - a0) * (i + 1) / along;
			for (int k = 0; k < across; k++) {
				double z0 = b0 + (b1 - b0) * k / across, z1 = b0 + (b1 - b0) * (k + 1) / across;
				patch(p, v, g, y, new double[]{x0, x1, x1, x0}, new double[]{z0, z0, z1, z1}, look, strength, seed);
			}
		}
	}

	/**
	 * A trench sunk along the ground's line from {@code from} to {@code to} blocks out, {@code half} to either side: its
	 * mouth laid on the floor exactly over that strip (the footprint's own edges), the shader looking down into the
	 * channel under it. Each quad end carries the trench's state there ({@code state} of its distance along): red the
	 * seam's heat, green the blood's height, blue the drop, alpha the strength. U is the distance along in blocks / 32,
	 * V runs 0 to 1 from the left lip to the right.
	 */
	static void trench(PoseStack p, VertexConsumer v, Ground g, double from, double to, double half,
			DoubleFunction<Trench> state, int seed) {
		double start = Math.max(0.0D, from);
		if (to <= start || half <= 0.0D) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double y = g.y() + LIFT * 0.75D;
		Vec3 c = g.camera();
		int steps = Math.max(1, (int) Math.ceil((to - start) / TRENCH_STEP));
		double a0 = start;
		Trench s0 = state.apply(a0);
		for (int i = 1; i <= steps; i++) {
			double a1 = i == steps ? to : start + (to - start) * i / steps;
			Trench s1 = state.apply(a1);
			if (s0.visible() || s1.visible()) {
				int c0 = color(s0.heat(), s0.blood(), s0.depth()), c1 = color(s1.heat(), s1.blood(), s1.depth());
				float u0 = (float) (TRENCH * 2 + a0 / SPAN), u1 = (float) (TRENCH * 2 + a1 / SPAN);
				MaterialQuads.vertex(v, pose, g.wx(a0, -half) - c.x, y - c.y, g.wz(a0, -half) - c.z, c0, s0.alpha(), u0, tile * 2);
				MaterialQuads.vertex(v, pose, g.wx(a1, -half) - c.x, y - c.y, g.wz(a1, -half) - c.z, c1, s1.alpha(), u1, tile * 2);
				MaterialQuads.vertex(v, pose, g.wx(a1, half) - c.x, y - c.y, g.wz(a1, half) - c.z, c1, s1.alpha(), u1, tile * 2 + 1);
				MaterialQuads.vertex(v, pose, g.wx(a0, half) - c.x, y - c.y, g.wz(a0, half) - c.z, c0, s0.alpha(), u0, tile * 2 + 1);
			}
			a0 = a1;
			s0 = s1;
		}
	}

	/** One quad of crazed floor; its shader coordinates are its ground position about the ground's origin. */
	private static void patch(PoseStack p, VertexConsumer v, Ground g, double y, double[] along, double[] right, Look look,
			float strength, int seed) {
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int color = color(look.heat(), look.blood(), look.rust());
		float alpha = look.alpha() * strength;
		Vec3 c = g.camera();
		for (int i = 0; i < 4; i++) {
			double wx = g.wx(along[i], right[i]), wz = g.wz(along[i], right[i]);
			float u = (float) (CRAZE * 2 + 1.0D + Mth.clamp((wx - g.ox()) / SPAN, -0.99D, 0.99D));
			float w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - g.oz()) / SPAN, -0.99D, 0.99D));
			MaterialQuads.vertex(v, pose, wx - c.x, y - c.y, wz - c.z, color, alpha, u, w);
		}
	}

	/** The crack state as the shader reads it from the vertex colour: heat red, blood green, rust blue. */
	static int color(float heat, float blood, float rust) {
		return channel(heat) << 16 | channel(blood) << 8 | channel(rust);
	}

	private static int channel(float value) {
		return Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}
}
