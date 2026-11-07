package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.saint.TelegraphFamily;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Where a Saint telegraph draws: the family-to-material map over the school materials ({@link LuxUmbraBatch} for
 * ANIMUS, CRUOR, LUX, DUCTILIS, PUTRICIEL_ROT, FLAME, RIME and UMBRA; {@link FerricDuctilisRenderTypes#IRON} for IRON in
 * the solid pass) plus one flat translucent tint for ground fills. Points are world positions; the canvas makes them
 * camera-relative. Flat pieces lie on the ground plane at the {@code y} given.
 * <p>
 * The subtlety budget lives here: {@link #MAX_FILL_ALPHA} caps every tint and {@link #MAX_EDGE_WIDTH} every edge, so a
 * style that goes through the canvas cannot paint a thick strand or an opaque floor.
 */
public final class TelegraphCanvas {
	/** No ground fill is more opaque than this. */
	public static final float MAX_FILL_ALPHA = 0.22F;
	/** No edge is wider than this, in blocks. */
	public static final double MAX_EDGE_WIDTH = 0.012D;
	/** Arc segments per this many degrees. */
	private static final double ARC_STEP = 6.0D;
	/** The ribbon shape most school shaders read as a soft band; the rime family reads its frosted surface. */
	private static final int RIBBON = LuxUmbraGeometry.RIBBON;
	private static final int FROST_SURFACE = 5;
	private static final int EMBER = 2;

	private final PoseStack poses;
	private final Vec3 camera;
	@Nullable
	private final LuxUmbraBatch batch;
	@Nullable
	private final VertexConsumer tint;
	@Nullable
	private final VertexConsumer iron;
	private final double time;
	private int layer;

	TelegraphCanvas(PoseStack poses, Vec3 camera, @Nullable LuxUmbraBatch batch, @Nullable VertexConsumer tint,
			@Nullable VertexConsumer iron, double time) {
		this.poses = poses;
		this.camera = camera;
		this.batch = batch;
		this.tint = tint;
		this.iron = iron;
		this.time = time;
	}

	/** The solid pass: only {@link #iron()} draws; tints and material edges are skipped. */
	public boolean solids() {
		return iron != null;
	}

	public Vec3 camera() {
		return camera;
	}

	/** Game time plus the partial tick, for idle motion. */
	public double time() {
		return time;
	}

	public PoseStack poses() {
		return poses;
	}

	/** The Ferric plate material, in the solid pass only. */
	@Nullable
	public VertexConsumer iron() {
		return iron;
	}

	/** The translucent material a family draws its edges and shapes in; null in the solid pass. */
	@Nullable
	public VertexConsumer material(TelegraphFamily family) {
		return batch == null ? null : batch.vertices(batchMaterial(familyOrDefault(family)));
	}

	/** Each drawn footprint gets its own hair of height, so two overlapping fills never fight. */
	public double ground(double floorY) {
		layer = (layer + 1) % 16;
		return floorY + 0.025D + layer * 0.0015D;
	}

	// ── tints ────────────────────────────────────────────────────────

	public void tintQuad(Vec3 a, Vec3 b, Vec3 c, Vec3 d, int rgb, float alpha) {
		if (tint == null || alpha <= 0.0F) return;
		float clamped = Math.min(alpha, MAX_FILL_ALPHA);
		Matrix4f pose = poses.last().pose();
		tintVertex(pose, a, rgb, clamped);
		tintVertex(pose, b, rgb, clamped);
		tintVertex(pose, c, rgb, clamped);
		tintVertex(pose, d, rgb, clamped);
	}

	/** A flat strip from {@code from} to {@code to} on the plane at {@code y}. */
	public void tintStrip(Vec3 from, Vec3 to, double y, double halfWidth, int rgb, float alpha) {
		double[] side = side(from, to);
		if (side == null) return;
		Vec3 a = flat(from, y), b = flat(to, y);
		Vec3 s = new Vec3(side[0] * halfWidth, 0.0D, side[1] * halfWidth);
		tintQuad(a.subtract(s), b.subtract(s), b.add(s), a.add(s), rgb, alpha);
	}

	/** A flat annular sector round {@code centre}: radii r0..r1, bearings fromDeg..toDeg about {@code forward}. */
	public void tintSector(Vec3 centre, double y, double[] forward, double r0, double r1, double fromDeg, double toDeg,
			int rgb, float alpha) {
		if (r1 <= r0 || toDeg <= fromDeg) return;
		int steps = Math.max(6, (int) Math.ceil((toDeg - fromDeg) / ARC_STEP));
		for (int i = 0; i < steps; i++) {
			double a0 = fromDeg + (toDeg - fromDeg) * i / steps, a1 = fromDeg + (toDeg - fromDeg) * (i + 1) / steps;
			tintQuad(polar(centre, y, forward, a0, r0), polar(centre, y, forward, a0, r1),
					polar(centre, y, forward, a1, r1), polar(centre, y, forward, a1, r0), rgb, alpha);
		}
	}

	public void tintDisc(Vec3 centre, double y, double radius, int rgb, float alpha) {
		tintSector(centre, y, new double[]{0.0D, 1.0D}, 0.0D, radius, -180.0D, 180.0D, rgb, alpha);
	}

	// ── edges ────────────────────────────────────────────────────────

	/** A flat material ribbon through {@code points} on the ground, one shader run along its whole length. */
	public void edgePath(TelegraphFamily family, List<Vec3> points, double halfWidth, float alpha, int seed) {
		VertexConsumer v = material(family);
		if (v == null || points.size() < 2 || alpha <= 0.0F) return;
		TelegraphFamily f = familyOrDefault(family);
		double width = Math.min(halfWidth, MAX_EDGE_WIDTH);
		double total = 0.0D;
		double[] lengths = new double[points.size()];
		for (int i = 1; i < points.size(); i++) {
			total += points.get(i).distanceTo(points.get(i - 1));
			lengths[i] = total;
		}
		if (total < 1.0E-6D) return;
		Matrix4f pose = poses.last().pose();
		int tile = MaterialQuads.seedTile(seed);
		for (int i = 1; i < points.size(); i++) {
			Vec3 a = points.get(i - 1), b = points.get(i);
			double[] side = side(a, b);
			if (side == null) continue;
			Vec3 s = new Vec3(side[0] * width, 0.0D, side[1] * width);
			Vec3 a0 = a.subtract(s).subtract(camera), a1 = a.add(s).subtract(camera);
			Vec3 b0 = b.subtract(s).subtract(camera), b1 = b.add(s).subtract(camera);
			MaterialQuads.quad(v, pose, a0.x, a0.y, a0.z, b0.x, b0.y, b0.z, b1.x, b1.y, b1.z, a1.x, a1.y, a1.z,
					edgeShape(f), tile, edgeColor(f), alpha, lengths[i - 1] / total, lengths[i] / total, false);
		}
	}

	public void edge(TelegraphFamily family, Vec3 from, Vec3 to, double halfWidth, float alpha, int seed) {
		edgePath(family, List.of(from, to), halfWidth, alpha, seed);
	}

	/** An arc on the ground round {@code centre}. */
	public void edgeArc(TelegraphFamily family, Vec3 centre, double y, double[] forward, double radius, double fromDeg,
			double toDeg, double halfWidth, float alpha, int seed) {
		if (radius <= 1.0E-3D || toDeg <= fromDeg) return;
		int steps = Math.max(6, (int) Math.ceil((toDeg - fromDeg) / ARC_STEP));
		List<Vec3> points = new ArrayList<>(steps + 1);
		for (int i = 0; i <= steps; i++) points.add(polar(centre, y, forward, fromDeg + (toDeg - fromDeg) * i / steps, radius));
		edgePath(family, points, halfWidth, alpha, seed);
	}

	// ── geometry ─────────────────────────────────────────────────────

	/** The ground point at {@code distance} from {@code centre}, {@code degrees} to the right of {@code forward}. */
	public static Vec3 polar(Vec3 centre, double y, double[] forward, double degrees, double distance) {
		double[] f = BossFootprint.Shapes.rotate(forward, degrees);
		return new Vec3(centre.x + f[0] * distance, y, centre.z + f[1] * distance);
	}

	public static Vec3 flat(Vec3 point, double y) {
		return new Vec3(point.x, y, point.z);
	}

	/** The horizontal unit vector to the right of a -> b, or null when they meet. */
	@Nullable
	static double[] side(Vec3 a, Vec3 b) {
		double dx = b.x - a.x, dz = b.z - a.z;
		double length = Math.sqrt(dx * dx + dz * dz);
		if (length < 1.0E-6D) return null;
		return new double[]{-dz / length, dx / length};
	}

	// ── families ─────────────────────────────────────────────────────

	/** The tint a family's ground fill takes. */
	public static int rgb(TelegraphFamily family) {
		return switch (familyOrDefault(family)) {
			case ANIMUS -> 0x8E1424;
			case CRUOR -> 0x5E0A16;
			case IRON -> 0x5C4B4A;
			case LUX -> 0xF4EEDE;
			case DUCTILIS -> 0xE2BE5A;
			case PUTRICIEL_ROT -> 0x8A8638;
			case FLAME -> 0xC8282E;
			case RIME -> 0xBEE2F2;
			case UMBRA -> 0x16121E;
		};
	}

	static LuxUmbraBatch.Material batchMaterial(TelegraphFamily family) {
		return switch (family) {
			case ANIMUS, IRON -> LuxUmbraBatch.Material.ANIMUS;
			case CRUOR -> LuxUmbraBatch.Material.CRUOR;
			case LUX -> LuxUmbraBatch.Material.LUX;
			case DUCTILIS -> LuxUmbraBatch.Material.DUCTILIS;
			case PUTRICIEL_ROT -> LuxUmbraBatch.Material.PUTRICIEL_ROT;
			case FLAME -> LuxUmbraBatch.Material.FLAME;
			case RIME -> LuxUmbraBatch.Material.RIME;
			case UMBRA -> LuxUmbraBatch.Material.UMBRA;
		};
	}

	private static int edgeShape(TelegraphFamily family) {
		return switch (family) {
			case RIME, CRUOR -> FROST_SURFACE;
			case FLAME -> EMBER;
			default -> RIBBON;
		};
	}

	private static int edgeColor(TelegraphFamily family) {
		return switch (family) {
			case DUCTILIS -> 0xF2CE6A;
			case LUX -> LuxUmbraGeometry.WHITE;
			default -> 0xFFFFFF;
		};
	}

	private static TelegraphFamily familyOrDefault(@Nullable TelegraphFamily family) {
		return family == null ? TelegraphFamily.ANIMUS : family;
	}

	private void tintVertex(Matrix4f pose, Vec3 at, int rgb, float alpha) {
		tint.addVertex(pose, (float) (at.x - camera.x), (float) (at.y - camera.y), (float) (at.z - camera.z))
				.setColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255));
	}
}
