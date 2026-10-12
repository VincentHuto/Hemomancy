package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.SomaticLoomEffectRules;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Flat-colour thread geometry shared by the Somatic Loom and its frayed threads: a camera-facing knot (hot core,
 * soft halo, turning wraps), frayed fibres trailing against motion, and a sagging tether with a bead of light
 * running along it. Positions are in the pose's own frame; `eye` is the camera in that frame.
 */
public final class ThreadKnotGeometry {
	/** The visual standard's hot core accent. */
	public static final float[] HOT = {1.0f, 0.82f, 0.77f};

	private ThreadKnotGeometry() {
	}

	/** Tendency colour lifted so the darkest schools (Mortem, Ferric) still read as light. */
	public static float[] vivid(ParticleColor color) {
		float r = color.getRed() / 255f, g = color.getGreen() / 255f, b = color.getBlue() / 255f;
		float max = Math.max(r, Math.max(g, b));
		if (max <= 0) return new float[]{0.6f, 0.6f, 0.6f};
		float lift = max < 0.6f ? 0.6f / max : 1f;
		return new float[]{r * lift, g * lift, b * lift};
	}

	public static float[] mix(float[] a, float[] b, float t) {
		return new float[]{Mth.lerp(t, a[0], b[0]), Mth.lerp(t, a[1], b[1]), Mth.lerp(t, a[2], b[2])};
	}

	/** A colour flushed hot, as a thread winding up to strike. */
	public static float[] hot(float[] colour) {
		return mix(colour, HOT, 0.6f);
	}

	/** One smooth thread sagging from `from` to `to`, a bead of light running toward `to`. */
	public static void tether(VertexConsumer vc, Matrix4f mat, Vec3 eye, Vector3f from, Vector3f to,
			float[] colour, float time, float seed, float alpha) {
		float length = from.distance(to);
		float sag = SomaticLoomEffectRules.tetherSag(length);
		float sway = Mth.sin(time * 0.05f + seed * Mth.TWO_PI) * 0.08f;
		Vector3f control = new Vector3f(from).add(to).mul(0.5f).add(sway, -sag, -sway);
		int segments = 20;
		List<Vector3f> points = new ArrayList<>(segments + 1);
		for (int i = 0; i <= segments; i++) {
			float t = i / (float) segments, u = 1 - t;
			points.add(new Vector3f(from).mul(u * u).add(new Vector3f(control).mul(2 * u * t))
					.add(new Vector3f(to).mul(t * t)));
		}
		float bead = SomaticLoomEffectRules.shimmerPosition(time, seed);
		float[] hot = hot(colour);
		for (int i = 0; i < segments; i++) {
			float t = (i + 0.5f) / segments;
			float lift = SomaticLoomEffectRules.shimmer(bead, t);
			float[] c = mix(colour, hot, lift);
			ribbonSegment(vc, mat, eye, points.get(i), points.get(i + 1), 0.016f + 0.01f * lift, 0.016f + 0.01f * lift,
					c, alpha * (0.8f + 0.2f * lift), alpha * (0.8f + 0.2f * lift), 0.05f, alpha * (0.2f + 0.3f * lift));
		}
	}

	/** Three frayed fibres trailing against `motion`; a still knot lets them hang and drift. */
	public static void tails(VertexConsumer vc, Matrix4f mat, Vec3 eye, Vector3f knot, Vec3 motion,
			float[] colour, float time, float seed, float alpha) {
		Vector3f back = motion.lengthSqr() > 4.0E-6D
				? new Vector3f((float) -motion.x, (float) -motion.y, (float) -motion.z).normalize()
				: new Vector3f(Mth.sin(seed * 9f) * 0.3f, -1f, Mth.cos(seed * 9f) * 0.3f).normalize();
		Vector3f toEye = new Vector3f((float) eye.x, (float) eye.y, (float) eye.z).sub(knot).normalize();
		Vector3f side = new Vector3f(back).cross(toEye);
		if (side.lengthSquared() < 1.0E-6f) side.set(1, 0, 0);
		side.normalize();
		for (int f = 0; f < 3; f++) {
			float spread = (f - 1) * 0.45f;
			float length = 0.16f + 0.05f * f;
			Vector3f previous = new Vector3f(knot);
			for (int k = 1; k <= 4; k++) {
				float t = k / 4f;
				float swayed = spread + Mth.sin(time * 0.11f + f * 2.1f + seed * 7f + t * 2.4f) * 0.35f * t;
				Vector3f point = new Vector3f(knot).add(new Vector3f(back).mul(length * t))
						.add(new Vector3f(side).mul(swayed * length * t));
				float w0 = Mth.lerp(t - 0.25f, 0.012f, 0.002f), w1 = Mth.lerp(t, 0.012f, 0.002f);
				ribbonSegment(vc, mat, eye, previous, point, w0, w1, colour, alpha * 0.75f * (1.25f - t),
						alpha * 0.75f * (1 - t), 0f, 0f);
				previous = point;
			}
		}
	}

	/** A camera-facing knot of thread: hot core, breathing halo, three turning wraps. */
	public static void knot(VertexConsumer vc, Matrix4f mat, Vector3f right, Vector3f up, Vector3f at,
			float[] colour, float time, float seed, float scale, float alpha) {
		float breath = 1f + 0.15f * Mth.sin(time * 0.12f + seed * Mth.TWO_PI);
		float[] core = mix(colour, HOT, 0.45f);
		disc(vc, mat, right, up, at, 0f, 0.06f * scale, core, 0.95f * alpha, 0.95f * alpha);
		disc(vc, mat, right, up, at, 0.06f * scale, 0.2f * breath * scale, colour, 0.4f * alpha, 0f);
		for (int w = 0; w < 3; w++) {
			float start = time * (0.04f + 0.015f * w) * (w % 2 == 0 ? 1 : -1) + w * Mth.TWO_PI / 3 + seed * Mth.TWO_PI;
			arc(vc, mat, right, up, at, (0.088f + 0.012f * w) * scale, start, start + 1.9f, 0.014f * scale, colour,
					0.85f * alpha);
		}
	}

	/** Camera-facing octagonal disc or ring between two radii, its alpha fading from inner to outer edge. */
	public static void disc(VertexConsumer vc, Matrix4f mat, Vector3f right, Vector3f up, Vector3f at,
			float inner, float outer, float[] c, float innerAlpha, float outerAlpha) {
		for (int i = 0; i < 8; i++) {
			float a0 = i * Mth.TWO_PI / 8, a1 = (i + 1) * Mth.TWO_PI / 8;
			Vector3f i0 = onPlane(right, up, at, a0, inner), i1 = onPlane(right, up, at, a1, inner);
			Vector3f o0 = onPlane(right, up, at, a0, outer), o1 = onPlane(right, up, at, a1, outer);
			quad(vc, mat, i0, o0, o1, i1, c, innerAlpha, outerAlpha, outerAlpha, innerAlpha);
		}
	}

	private static void arc(VertexConsumer vc, Matrix4f mat, Vector3f right, Vector3f up, Vector3f at,
			float radius, float a0, float a1, float width, float[] c, float alpha) {
		int steps = 6;
		for (int i = 0; i < steps; i++) {
			float s0 = Mth.lerp(i / (float) steps, a0, a1), s1 = Mth.lerp((i + 1) / (float) steps, a0, a1);
			float fade0 = Mth.sin(Mth.PI * i / steps), fade1 = Mth.sin(Mth.PI * (i + 1) / steps);
			quad(vc, mat, onPlane(right, up, at, s0, radius - width / 2), onPlane(right, up, at, s0, radius + width / 2),
					onPlane(right, up, at, s1, radius + width / 2), onPlane(right, up, at, s1, radius - width / 2),
					c, alpha * fade0, alpha * fade0, alpha * fade1, alpha * fade1);
		}
	}

	private static Vector3f onPlane(Vector3f right, Vector3f up, Vector3f at, float angle, float radius) {
		return new Vector3f(right).mul(Mth.cos(angle) * radius).add(new Vector3f(up).mul(Mth.sin(angle) * radius)).add(at);
	}

	/**
	 * One camera-facing segment of thread from a to b: a core of widths w0 to w1 with alphas a0 to a1, and
	 * optionally a soft glow either side, `glowWidth` wide at `glowAlpha`.
	 */
	public static void ribbonSegment(VertexConsumer vc, Matrix4f mat, Vec3 eye, Vector3f a, Vector3f b,
			float w0, float w1, float[] c, float a0, float a1, float glowWidth, float glowAlpha) {
		Vector3f along = new Vector3f(b).sub(a);
		if (along.lengthSquared() < 1.0E-8f) return;
		Vector3f toEye = new Vector3f((float) eye.x, (float) eye.y, (float) eye.z).sub(a);
		Vector3f side = along.cross(toEye, new Vector3f());
		if (side.lengthSquared() < 1.0E-8f) return;
		side.normalize();
		Vector3f s0 = new Vector3f(side).mul(w0 / 2), s1 = new Vector3f(side).mul(w1 / 2);
		quad(vc, mat, new Vector3f(a).sub(s0), new Vector3f(a).add(s0), new Vector3f(b).add(s1), new Vector3f(b).sub(s1),
				c, a0, a0, a1, a1);
		if (glowWidth <= 0 || glowAlpha <= 0) return;
		for (int edge = -1; edge <= 1; edge += 2) {
			Vector3f inA = new Vector3f(s0).mul(edge), inB = new Vector3f(s1).mul(edge);
			Vector3f outA = new Vector3f(side).mul(edge * (w0 / 2 + glowWidth)), outB = new Vector3f(side).mul(edge * (w1 / 2 + glowWidth));
			quad(vc, mat, new Vector3f(a).add(inA), new Vector3f(a).add(outA), new Vector3f(b).add(outB),
					new Vector3f(b).add(inB), c, glowAlpha, 0f, 0f, glowAlpha);
		}
	}

	public static void quad(VertexConsumer vc, Matrix4f mat, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3,
			float[] c, float a0, float a1, float a2, float a3) {
		emitQuad(vc, mat, p0.x, p0.y, p0.z, c[0], c[1], c[2], a0, p1.x, p1.y, p1.z, c[0], c[1], c[2], a1,
				p2.x, p2.y, p2.z, c[0], c[1], c[2], a2, p3.x, p3.y, p3.z, c[0], c[1], c[2], a3);
	}

	public static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x1, float y1, float z1, float r1, float g1, float b1, float a1,
			float x2, float y2, float z2, float r2, float g2, float b2, float a2,
			float x3, float y3, float z3, float r3, float g3, float b3, float a3,
			float x4, float y4, float z4, float r4, float g4, float b4, float a4) {
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r2, g2, b2, a2);
		vc.addVertex(mat, x3, y3, z3).setColor(r3, g3, b3, a3);
		vc.addVertex(mat, x4, y4, z4).setColor(r4, g4, b4, a4);
	}
}
