package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.function.LongPredicate;

/**
 * The chamber floor answering Saint Putriciel (visual pass 2), in their ground material
 * ({@link PutricielRenderTypes#GROUND}): patches of floor the rot colonises (MAT) or the fire scorches (SCORCH), each
 * vertex carrying how far the colony or the char has reached it so the shader's blotches run out from where they
 * started, and trenches the rot tears open along a lane and fills with ooze (TRENCH), or the fire burns into a bed of
 * coals (FURROW). Points are world positions; the camera is subtracted here.
 */
final class PutricielGroundGeometry {
	/** Shapes the ground shader decodes. */
	static final int MAT = 0, TRENCH = 1, SCORCH = 2, FURROW = 3;
	/**
	 * The dark wet ring a rot sludge pool soaks into the floor round it (revision 2026-11-06), MAT's stain: red the
	 * place out from the pool's rim (0 under the pool, 1 at the stain's edge), green how wet, blue the mould fringe.
	 */
	static final int STAIN = 4;
	/** Blocks per unit of the shader's coordinates. */
	static final double SPAN = 32.0D;
	/** A trench's strip is this many times the trench's half-width: the rest is its lip and the stain round it. */
	static final double TRENCH_HALO = 1.6D;
	/** Height over the floor, and a hair more for each drawn layer so two never fight. */
	private static final double LIFT = 0.013D, LAYER = 0.0011D;
	private static final double ARC_STEP = 9.0D, RING_STEP = 1.0D, RECT_STEP = 1.0D, STRIP_STEP = 0.5D;
	/** A trench's root and running head taper over this many blocks. */
	private static final double TAPER = 0.7D;
	/** How much of its coverage a rectangle keeps at its outer corners, so its colony or char thins out to them. */
	private static final float FEATHER = 0.3F;

	/** Where a footprint's local points land: origin, aim and floor, and the camera they draw relative to. */
	record Ground(double ox, double oz, double fx, double fz, double y, Vec3 camera) {
		static Ground of(Vec3 origin, double[] forward, double floorY, Vec3 camera) {
			return new Ground(origin.x, origin.z, forward[0], forward[1], floorY, camera);
		}

		/** The same aim at another origin. */
		Ground at(Vec3 origin) {
			return new Ground(origin.x, origin.z, fx, fz, y, camera);
		}

		/** The same origin turned by {@code degrees} toward the right hand. */
		Ground turned(double degrees) {
			double rad = Math.toRadians(degrees), c = Math.cos(rad), s = Math.sin(rad);
			return new Ground(ox, oz, fx * c - fz * s, fz * c + fx * s, y, camera);
		}

		double wx(double along, double right) {
			return ox + fx * along - fz * right;
		}

		double wz(double along, double right) {
			return oz + fz * along + fx * right;
		}

		Vec3 world(double along, double right) {
			return new Vec3(wx(along, right), y, wz(along, right));
		}
	}

	/**
	 * Where a colony or a char spreads from and how far it has run: each vertex reads its coverage from its distance
	 * to ({@code x}, {@code z}). Inward fronts run in from {@code outer} toward the centre instead (the rot creeping
	 * back over a scorch).
	 */
	record Front(double x, double z, double reach, double feather, boolean inward, double outer) {
		static Front from(Vec3 at, double reach, double feather) {
			return new Front(at.x, at.z, reach, feather, false, 0.0D);
		}

		static Front inward(Vec3 at, double outer, double reach, double feather) {
			return new Front(at.x, at.z, reach, feather, true, outer);
		}

		/** A front that has taken everything. */
		static Front whole(Vec3 at) {
			return new Front(at.x, at.z, 1.0E6D, 1.0D, false, 0.0D);
		}

		float at(double wx, double wz) {
			double d = Math.hypot(wx - x, wz - z);
			return PutricielBloomRules.front(inward ? outer - d : d, reach, feather);
		}
	}

	/** The state a patch or a strip carries besides its coverage: rot (wet, spore) or fire (heat, ash). */
	record Paint(int shape, float second, float third, float alpha) {
		boolean visible() {
			return alpha > 0.003F;
		}
	}

	private static int layer;
	/** The frame's live sludge pools: every telegraph vertex rides their surface, so no ground read hides under one. */
	private static PutricielSludgeField field = PutricielSludgeField.EMPTY;

	private PutricielGroundGeometry() {
	}

	/** Starts a frame's layering. */
	static void begin() {
		begin(PutricielSludgeField.EMPTY);
	}

	/** Starts a frame's layering over {@code pools}, whose surfaces the frame's floor patches and trenches ride. */
	static void begin(PutricielSludgeField pools) {
		layer = 0;
		field = pools == null ? PutricielSludgeField.EMPTY : pools;
	}

	private static double height(Ground g) {
		return g.y() + LIFT + (layer++ % 14) * LAYER;
	}

	/**
	 * An annular sector of floor round the ground's origin: radii {@code r0} to {@code r1}, bearings {@code fromDeg} to
	 * {@code toDeg} about the aim (positive to the right).
	 */
	static void sector(PoseStack p, VertexConsumer v, Ground g, double r0, double r1, double fromDeg, double toDeg, Front front,
			Paint paint, int seed) {
		if (!paint.visible() || r1 <= r0 || toDeg <= fromDeg) return;
		double y = height(g);
		int angles = Math.max(3, (int) Math.ceil((toDeg - fromDeg) / ARC_STEP));
		int rings = Math.max(1, (int) Math.ceil((r1 - r0) / RING_STEP));
		for (int i = 0; i < angles; i++) {
			double a0 = Math.toRadians(fromDeg + (toDeg - fromDeg) * i / angles);
			double a1 = Math.toRadians(fromDeg + (toDeg - fromDeg) * (i + 1) / angles);
			for (int k = 0; k < rings; k++) {
				double q0 = r0 + (r1 - r0) * k / rings, q1 = r0 + (r1 - r0) * (k + 1) / rings;
				patch(p, v, g, y, new double[]{Math.cos(a0) * q0, Math.cos(a0) * q1, Math.cos(a1) * q1, Math.cos(a1) * q0},
						new double[]{Math.sin(a0) * q0, Math.sin(a0) * q1, Math.sin(a1) * q1, Math.sin(a1) * q0}, front, paint, seed);
			}
		}
	}

	/** A rectangle of floor in the ground's frame: along {@code a0} to {@code a1}, right {@code b0} to {@code b1}. */
	static void rect(PoseStack p, VertexConsumer v, Ground g, double a0, double a1, double b0, double b1, Front front, Paint paint,
			int seed) {
		if (!paint.visible() || a1 <= a0 || b1 <= b0) return;
		double y = height(g);
		int along = Math.max(1, (int) Math.ceil((a1 - a0) / RECT_STEP));
		int across = Math.max(1, (int) Math.ceil((b1 - b0) / RECT_STEP));
		for (int i = 0; i < along; i++) {
			double x0 = a0 + (a1 - a0) * i / along, x1 = a0 + (a1 - a0) * (i + 1) / along;
			for (int k = 0; k < across; k++) {
				double z0 = b0 + (b1 - b0) * k / across, z1 = b0 + (b1 - b0) * (k + 1) / across;
				// the colony thins out ragged to the rectangle's sides and far end, so it never stops on a ruled line
				// (a scorch keeps its edge: its char front draws a burning line, which along a side would be a hairline)
				float feather = paint.shape() == MAT ? FEATHER : 1.0F;
				float e0 = k == 0 ? feather : 1.0F, e1 = k == across - 1 ? feather : 1.0F, f1 = i == along - 1 ? feather : 1.0F;
				patch(p, v, g, y, new double[]{x0, x1, x1, x0}, new double[]{z0, z0, z1, z1}, front, paint, seed,
						new float[]{e0, e0 * f1, e1 * f1, e1});
			}
		}
	}

	/**
	 * Every whole block cell within {@code radius} of the ground's origin that {@code open} admits, one patch each. A
	 * corner shared with a refused cell (the ash, a pyre's circle) takes almost nothing, so the colony stops short of
	 * the safe ground with its fringe toward it.
	 */
	static void cells(PoseStack p, VertexConsumer v, Ground g, double radius, LongPredicate open, Front front, Paint paint, int seed) {
		if (!paint.visible() || radius <= 0.0D) return;
		double y = height(g);
		int x0 = Mth.floor(g.ox() - radius), x1 = Mth.floor(g.ox() + radius);
		int z0 = Mth.floor(g.oz() - radius), z1 = Mth.floor(g.oz() + radius);
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		Vec3 c = g.camera();
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				if (Math.hypot(x + 0.5D - g.ox(), z + 0.5D - g.oz()) > radius) continue;
				if (!open.test(cell(x, z))) continue;
				int[][] corners = {{x, z}, {x + 1, z}, {x + 1, z + 1}, {x, z + 1}};
				for (int[] corner : corners) {
					boolean edge = !open.test(cell(corner[0], corner[1])) || !open.test(cell(corner[0] - 1, corner[1]))
							|| !open.test(cell(corner[0], corner[1] - 1)) || !open.test(cell(corner[0] - 1, corner[1] - 1));
					float cover = front.at(corner[0], corner[1]) * (edge ? 0.12F : 1.0F);
					vertex(v, pose, g, c, corner[0], y, corner[1], paint, cover, tile);
				}
			}
		}
	}

	/** The column key {@code BossFootprint} uses for a cell. */
	static long cell(int x, int z) {
		return net.minecraft.core.BlockPos.asLong(x, 0, z);
	}

	/**
	 * A trench (or a burning furrow, by the paint's shape) along world points over the ground's floor, {@code half}
	 * its half-width, running out to {@code shown} of its length from the first point, opened {@code open} (the
	 * shader reads the opening from red). It tapers at its root and at its running head.
	 */
	static void strip(PoseStack p, VertexConsumer v, Ground g, List<Vec3> points, double half, double shown, float open, Paint paint,
			int seed) {
		if (!paint.visible() || points.size() < 2 || half <= 0.0D || shown <= 0.0D) return;
		double total = 0.0D;
		for (int i = 1; i < points.size(); i++) total += planar(points.get(i - 1), points.get(i));
		double visible = total * Mth.clamp(shown, 0.0D, 1.0D);
		if (visible < 0.05D) return;
		double y = height(g);
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double w = half * TRENCH_HALO;
		int previousColor = 0;
		Vec3 c = g.camera();
		double s = 0.0D, previousS = 0.0D;
		double[] previous = null;
		boolean done = false;
		for (int i = 0; i + 1 < points.size() && !done; i++) {
			Vec3 a = points.get(i), b = points.get(i + 1);
			double length = planar(a, b);
			if (length < 1.0E-6D) continue;
			double tx = (b.x - a.x) / length, tz = (b.z - a.z) / length;
			int steps = Math.max(1, (int) Math.ceil(length / STRIP_STEP));
			for (int k = previous == null ? 0 : 1; k <= steps; k++) {
				double at = length * k / steps;
				double here = s + at;
				if (here >= visible) {
					at = visible - s;
					here = visible;
					done = true;
				}
				double px = a.x + tx * at, pz = a.z + tz * at;
				// the tear pinches shut at both ends (the shader closes the channel with its red) while the strip
				// itself narrows only a little, so the walls' bands never crowd into chevrons at a squeezed end
				double taper = Math.min(Math.min(1.0D, 0.3D + here / TAPER), Math.min(1.0D, (visible - here) / TAPER + 0.1D));
				int color = color(open * (float) Mth.clamp((taper - 0.1D) / 0.9D, 0.0D, 1.0D), paint.second(), paint.third());
				double narrow = 0.7D + 0.3D * taper;
				double nx = -tz * w * narrow, nz = tx * w * narrow;
				double[] edge = {px - nx, pz - nz, px + nx, pz + nz};
				if (previous != null) {
					double base = Math.floor(previousS / SPAN) * SPAN;
					float u0 = (float) (paint.shape() * 2 + (previousS - base) / SPAN), u1 = (float) (paint.shape() * 2 + (here - base) / SPAN);
					MaterialQuads.vertex(v, pose, previous[0] - c.x, y + field.lift(previous[0], previous[1]) - c.y, previous[1] - c.z, previousColor,
							paint.alpha(), u0, tile * 2);
					MaterialQuads.vertex(v, pose, edge[0] - c.x, y + field.lift(edge[0], edge[1]) - c.y, edge[1] - c.z, color, paint.alpha(), u1,
							tile * 2);
					MaterialQuads.vertex(v, pose, edge[2] - c.x, y + field.lift(edge[2], edge[3]) - c.y, edge[3] - c.z, color, paint.alpha(), u1,
							tile * 2 + 1);
					MaterialQuads.vertex(v, pose, previous[2] - c.x, y + field.lift(previous[2], previous[3]) - c.y, previous[3] - c.z, previousColor,
							paint.alpha(), u0, tile * 2 + 1);
				}
				previous = edge;
				previousColor = color;
				previousS = here;
				if (done) break;
			}
			s += length;
		}
	}

	/** A closed trench ring of {@code radius} round the ground's origin. */
	static void ring(PoseStack p, VertexConsumer v, Ground g, double radius, double half, double shown, float open, Paint paint,
			int seed) {
		if (radius <= 0.2D) return;
		int segments = (int) Math.max(16, Math.min(96, radius * 7.0D));
		List<Vec3> points = new java.util.ArrayList<>(segments + 1);
		double turn = PutricielBloomRules.unit(seed, 7) * Math.PI * 2.0D;
		for (int i = 0; i <= segments; i++) {
			double a = turn + i * Math.PI * 2.0D / segments;
			double r = radius + (PutricielBloomRules.unit(seed, 20 + i % segments) - 0.5D) * 0.12D;
			points.add(new Vec3(g.ox() + Math.cos(a) * r, g.y(), g.oz() + Math.sin(a) * r));
		}
		strip(p, v, g, points, half, shown, open, paint, seed);
	}

	/** One quad of floor in the ground's frame; its shader coordinates are its world position about the ground's origin. */
	private static void patch(PoseStack p, VertexConsumer v, Ground g, double y, double[] along, double[] right, Front front,
			Paint paint, int seed) {
		patch(p, v, g, y, along, right, front, paint, seed, new float[]{1.0F, 1.0F, 1.0F, 1.0F});
	}

	private static void patch(PoseStack p, VertexConsumer v, Ground g, double y, double[] along, double[] right, Front front,
			Paint paint, int seed, float[] edge) {
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		Vec3 c = g.camera();
		for (int i = 0; i < 4; i++) {
			double wx = g.wx(along[i], right[i]), wz = g.wz(along[i], right[i]);
			vertex(v, pose, g, c, wx, y, wz, paint, front.at(wx, wz) * edge[i], tile);
		}
	}

	private static void vertex(VertexConsumer v, Matrix4f pose, Ground g, Vec3 c, double wx, double y, double wz, Paint paint,
			float cover, int tile) {
		float u = (float) (paint.shape() * 2 + 1.0D + Mth.clamp((wx - g.ox()) / SPAN, -0.99D, 0.99D));
		float w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - g.oz()) / SPAN, -0.99D, 0.99D));
		MaterialQuads.vertex(v, pose, wx - c.x, y + field.lift(wx, wz) - c.y, wz - c.z, color(cover, paint.second(), paint.third()), paint.alpha(),
				u, w);
	}

	/**
	 * The wet stain round a sludge pool (shape {@link #STAIN}): rings from under the pool out to {@code beyond} blocks
	 * past its lobed rim, which {@code rim} gives at each bearing (radians) about {@code centre}. Not lifted onto the
	 * pool: it lies on the floor under it.
	 */
	static void stain(PoseStack p, VertexConsumer v, Vec3 centre, java.util.function.DoubleUnaryOperator rim, double beyond, float wet,
			float fringe, float alpha, Vec3 camera, int seed) {
		if (alpha <= 0.003F || wet <= 0.003F) return;
		Matrix4f pose = p.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		double y = centre.y + LIFT + (layer++ % 14) * LAYER;
		int segments = 28;
		// bands under the pool out to its rim (red 0), then the stain's own band out past it (red 0 to 1)
		double[] places = {0.0D, 0.55D, 0.85D, 1.0D};
		for (int j = 0; j < segments; j++) {
			double a0 = j * Math.PI * 2.0D / segments, a1 = (j + 1) * Math.PI * 2.0D / segments;
			double r0 = rim.applyAsDouble(a0), r1 = rim.applyAsDouble(a1);
			for (int k = 0; k < places.length; k++) {
				boolean last = k == places.length - 1;
				double inner = places[k], outer = last ? 0.0D : places[k + 1];
				double oa = last ? r0 + beyond : outer * r0, ob = last ? r1 + beyond : outer * r1;
				float redOut = last ? 1.0F : 0.0F;
				stainVertex(v, pose, centre, camera, y, a0, inner * r0, 0.0F, wet, fringe, alpha, tile);
				stainVertex(v, pose, centre, camera, y, a0, oa, redOut, wet, fringe, alpha, tile);
				stainVertex(v, pose, centre, camera, y, a1, ob, redOut, wet, fringe, alpha, tile);
				stainVertex(v, pose, centre, camera, y, a1, inner * r1, 0.0F, wet, fringe, alpha, tile);
			}
		}
	}

	private static void stainVertex(VertexConsumer v, Matrix4f pose, Vec3 centre, Vec3 c, double y, double angle, double radius, float red,
			float wet, float fringe, float alpha, int tile) {
		double wx = centre.x + Math.cos(angle) * radius, wz = centre.z + Math.sin(angle) * radius;
		float u = (float) (STAIN * 2 + 1.0D + Mth.clamp((wx - centre.x) / SPAN, -0.99D, 0.99D));
		float w = (float) (tile * 2 + 1.0D + Mth.clamp((wz - centre.z) / SPAN, -0.99D, 0.99D));
		MaterialQuads.vertex(v, pose, wx - c.x, y - c.y, wz - c.z, color(red, wet, fringe), alpha, u, w);
	}

	private static double planar(Vec3 a, Vec3 b) {
		return Math.hypot(b.x - a.x, b.z - a.z);
	}

	/** The ground state as the shader reads it from the vertex colour: red, green, blue each 0 to 1. */
	static int color(float red, float green, float blue) {
		return channel(red) << 16 | channel(green) << 8 | channel(blue);
	}

	private static int channel(float value) {
		return Math.round(Mth.clamp(value, 0.0F, 1.0F) * 255.0F);
	}
}
