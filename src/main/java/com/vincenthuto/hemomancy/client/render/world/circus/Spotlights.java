package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.List;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules.Look;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Spotlight cones from high over the ring down onto it: one slow sweep while the show gathers, three holding the ring
 * act and following the swinging artists once the visitor is attuned. Soft beams with a warm pool on the floor.
 */
public final class Spotlights {
	private static final double HEIGHT = 12.0D;
	private static final float TOP = 0.4F;
	private static final float BOTTOM = 2.2F;
	private static final int SEGMENTS = 16;
	private static final int BEAM = 0xFFE6C8;
	private static final float SWEEP = (float) Math.toRadians(25.0D);
	private static final float SWEEP_PERIOD = 300.0F;

	private Spotlights() {
	}

	/** Where each light falls this frame: the ring act and the swingers first, else the ring. */
	public static List<Vec3> targets(ShowMarks marks, Look look, List<PhantomFigures.Figure> cast, float time) {
		if (look.spotlights() <= 0 || !marks.hasRing()) return List.of();
		Vec3 centre = marks.ring().centre();
		if (!look.chase()) {
			double angle = Math.sin(time / SWEEP_PERIOD * Math.PI * 2.0D) * SWEEP;
			double reach = Math.tan(angle) * HEIGHT;
			return List.of(centre.add(reach, 0.0D, reach * 0.4D));
		}
		List<Vec3> targets = new java.util.ArrayList<>();
		for (PhantomFigures.Figure figure : cast) {
			if (figure.kind() == PhantomFigures.Kind.SEATED || figure.hidesFromGaze()) continue;
			if (figure.kind() == PhantomFigures.Kind.STANDING && figure.at().distanceTo(centre) > marks.ring().radius())
				continue;
			Vec3 c = figure.centre();
			targets.add(new Vec3(c.x, centre.y, c.z));
			if (targets.size() == look.spotlights()) break;
		}
		while (targets.size() < look.spotlights()) targets.add(centre);
		return targets;
	}

	public static void render(VertexConsumer glow, Matrix4f matrix, Vec3 camera, ShowMarks marks, List<Vec3> targets,
			float strength) {
		if (targets.isEmpty() || strength <= 0.0F) return;
		Vec3 source = marks.ring().centre().add(0.0D, HEIGHT, 0.0D);
		for (Vec3 target : targets) {
			cone(glow, matrix, camera, source, target, BOTTOM, (int) (34 * strength), 0);
			cone(glow, matrix, camera, source, target, BOTTOM * 0.55F, (int) (30 * strength), 6);
			pool(glow, matrix, camera, target.add(0.0D, 0.03D, 0.0D), BOTTOM * 1.05F, (int) (70 * strength));
		}
	}

	/** A beam: the top ring at the source, the bottom ring round the target, alpha fading toward the floor. */
	private static void cone(VertexConsumer glow, Matrix4f matrix, Vec3 camera, Vec3 top, Vec3 bottom, float radius,
			int alphaTop, int alphaBottom) {
		float tx = (float) (top.x - camera.x), ty = (float) (top.y - camera.y), tz = (float) (top.z - camera.z);
		float bx = (float) (bottom.x - camera.x), by = (float) (bottom.y - camera.y), bz = (float) (bottom.z - camera.z);
		for (int i = 0; i < SEGMENTS; i++) {
			double a0 = i * Math.PI * 2.0D / SEGMENTS, a1 = (i + 1) * Math.PI * 2.0D / SEGMENTS;
			float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0), c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
			vertex(glow, matrix, tx + c0 * TOP, ty, tz + s0 * TOP, alphaTop);
			vertex(glow, matrix, tx + c1 * TOP, ty, tz + s1 * TOP, alphaTop);
			vertex(glow, matrix, bx + c1 * radius, by, bz + s1 * radius, alphaBottom);
			vertex(glow, matrix, bx + c0 * radius, by, bz + s0 * radius, alphaBottom);
		}
	}

	/** The lit floor: a soft disc, brightest in the middle. */
	private static void pool(VertexConsumer glow, Matrix4f matrix, Vec3 camera, Vec3 at, float radius, int alpha) {
		float x = (float) (at.x - camera.x), y = (float) (at.y - camera.y), z = (float) (at.z - camera.z);
		for (int i = 0; i < SEGMENTS; i++) {
			double a0 = i * Math.PI * 2.0D / SEGMENTS, a1 = (i + 1) * Math.PI * 2.0D / SEGMENTS;
			vertex(glow, matrix, x, y, z, alpha);
			vertex(glow, matrix, x, y, z, alpha);
			vertex(glow, matrix, x + (float) Math.cos(a1) * radius, y, z + (float) Math.sin(a1) * radius, 0);
			vertex(glow, matrix, x + (float) Math.cos(a0) * radius, y, z + (float) Math.sin(a0) * radius, 0);
		}
	}

	private static void vertex(VertexConsumer glow, Matrix4f matrix, float x, float y, float z, int alpha) {
		glow.addVertex(matrix, x, y, z).setColor(BEAM >> 16 & 0xFF, BEAM >> 8 & 0xFF, BEAM & 0xFF, Math.clamp(alpha, 0, 255));
	}
}
