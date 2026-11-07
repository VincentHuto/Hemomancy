package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Kind;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Look;
import com.vincenthuto.hemomancy.client.render.world.SeraphaeOvergrowthRules.Seam;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Saint Seraphae's floor healing over, in her tissue material ({@link SeraphaeRenderTypes#TISSUE}, visual pass 2):
 * furrows, the floor opened along a footprint and swelling shut, filled the whole width of a lane (the trench, never a
 * set of parallel lines); vessels of light lying on the floor; and patches of overgrowth for the area a footprint
 * covers. Nerve fibres are gold and are drawn on the Ductilis material by {@link SeraphaeLightGeometry}, not here.
 * Points are world positions; the camera is subtracted here. The look's swell, light and rawness ride in the colour.
 */
final class SeraphaeTissueGeometry {
	/** Shapes the tissue shader decodes. */
	static final int FURROW = 0, BLOOM = 1, VESSEL = 2;
	/** Blocks per unit of the shader's coordinates. */
	static final double SPAN = 32.0D;
	/** Height above the floor, and a hair more for each drawn layer so two never fight. */
	private static final double LIFT = 0.018D, LAYER = 0.0011D;
	private static final double ARC_STEP = 8.0D, PATCH_STEP = 2.0D;
	/** A seam's lead-in and running tip taper over this many blocks; a furrow's ends round off over its own width. */
	private static final double TAPER = 0.4D;

	/** Where a footprint's local points land: origin, aim and floor, and the camera they draw relative to. */
	record Ground(double ox, double oz, double fx, double fz, double y, Vec3 camera) {
		static Ground of(Vec3 origin, double[] forward, double floorY, Vec3 camera) {
			return new Ground(origin.x, origin.z, forward[0], forward[1], floorY, camera);
		}

		/** The same aim at another origin. */
		Ground at(Vec3 origin) {
			return new Ground(origin.x, origin.z, fx, fz, y, camera);
		}

		/** The same origin turned {@code degrees} toward the right hand. */
		Ground turned(double degrees) {
			double r = Math.toRadians(degrees), c = Math.cos(r), s = Math.sin(r);
			return new Ground(ox, oz, fx * c - fz * s, fz * c + fx * s, y, camera);
		}

		double wx(double along, double right) {
			return ox + fx * along - fz * right;
		}

		double wz(double along, double right) {
			return oz + fz * along + fx * right;
		}

		/** A local point at height {@code above} the floor, camera-relative. */
		Vec3 local(double along, double right, double above) {
			return new Vec3(wx(along, right) - camera.x, y + above - camera.y, wz(along, right) - camera.z);
		}
	}

	private static int layer;

	private SeraphaeTissueGeometry() {
	}

	/** Starts a frame's layering. */
	static void begin() {
		layer = 0;
	}

	/** Every furrow and vessel of a network, each as far as the look's run has reached it. Nerves are skipped. */
	static void seams(PoseStack p, VertexConsumer v, Ground g, List<Seam> seams, Look look, float strength, int seed) {
		if (!look.visible() || strength <= 0.0F) return;
		double y = g.y() + LIFT + (layer++ % 12) * LAYER;
		for (int i = 0; i < seams.size(); i++) {
			Seam seam = seams.get(i);
			if (seam.kind() == Kind.FIBRE) continue;
			double shown = SeraphaeOvergrowthRules.shown(look.grow(), seam.start(), seam.span());
			if (shown > 0.0D) seam(p, v, g, seam.kind() == Kind.VESSEL ? y + LAYER * 0.5D : y, seam, shown, look, strength, seed + i * 7);
		}
	}

	/** One seam drawn to {@code shown} of its length, tapering at its root and its running tip. */
	static void seam(PoseStack p, VertexConsumer v, Ground g, double y, Seam seam, double shown, Look look, float strength, int seed) {
		int n = seam.size();
		if (n < 2) return;
		double total = seam.length();
		double visible = total * Mth.clamp(shown, 0.0D, 1.0D);
		if (visible < 0.05D) return;
		boolean furrow = seam.kind() == Kind.FURROW;
		int shape = furrow ? FURROW : VESSEL;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int color = color(look.swell(), look.light(), look.raw());
		float alpha = look.alpha() * strength;
		// a furrow's strip carries its lips and pallor; a vessel's is the vessel itself, swelling with the tissue
		double half = furrow ? seam.width() * SeraphaeOvergrowthRules.HALO * look.width()
				: seam.width() * (0.7D + 0.3D * Math.min(1.2F, look.swell()));
		double taper = furrow ? Math.max(TAPER, seam.width() * 1.2D) : TAPER;
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
				// a furrow rounds off at both ends like a lens; a vessel tapers to a point as it runs out
				double in = furrow ? Math.sqrt(Math.min(1.0D, 0.15D + s / taper)) : Math.min(1.0D, 0.6D + s / taper);
				double out = furrow ? Math.sqrt(Math.min(1.0D, (visible - s) / taper + 0.08D)) : Math.min(1.0D, (visible - s) / taper + 0.1D);
				double w = half * Math.min(in, out);
				double na = -tz / length * w, nr = tx / length * w;
				double[] here = {g.wx(x - na, z - nr), g.wz(x - na, z - nr), g.wx(x + na, z + nr), g.wz(x + na, z + nr)};
				if (previous != null) {
					double base = Math.floor(previousS / SPAN) * SPAN;
					float u0 = (float) (shape * 2 + (previousS - base) / SPAN), u1 = (float) (shape * 2 + (s - base) / SPAN);
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

	/** An overgrown annular sector of floor round the ground's origin: radii r0 to r1, bearings from and to about the aim. */
	static void bloom(PoseStack p, VertexConsumer v, Ground g, double r0, double r1, double fromDeg, double toDeg, Look look,
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

	/** An overgrown rectangle of floor in the footprint's frame: along from a0 to a1, right from b0 to b1. */
	static void bloomRect(PoseStack p, VertexConsumer v, Ground g, double a0, double a1, double b0, double b1, Look look,
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

	/** One quad of overgrowth; its shader coordinates are its ground position about the ground's origin. */
	private static void patch(PoseStack p, VertexConsumer v, Ground g, double y, double[] along, double[] right, Look look,
			float strength, int seed) {
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		int color = color(look.swell(), look.light(), look.raw());
		float alpha = look.alpha() * strength;
		Vec3 c = g.camera();
		for (int i = 0; i < 4; i++) {
			double wx = g.wx(along[i], right[i]), wz = g.wz(along[i], right[i]);
			float u = (float) (BLOOM * 2 + 1.0D + Mth.clamp((wx - g.ox()) / SPAN, -0.99D, 0.99D));
			float w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - g.oz()) / SPAN, -0.99D, 0.99D));
			MaterialQuads.vertex(v, pose, wx - c.x, y - c.y, wz - c.z, color, alpha, u, w);
		}
	}

	/** The tissue state as the shader reads it from the vertex colour: swelling red, light green, rawness blue. */
	static int color(float swell, float light, float raw) {
		return channel(swell) << 16 | channel(light) << 8 | channel(raw);
	}

	private static int channel(float value) {
		return Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}
}
