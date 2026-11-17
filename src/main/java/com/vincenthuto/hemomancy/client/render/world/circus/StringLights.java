package com.vincenthuto.hemomancy.client.render.world.circus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules.Bulb;
import com.vincenthuto.hemomancy.client.render.world.circus.PhantomShowRules.Look;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * The big top's string lights: bulbs on sagging wire from pole collar to pole collar, and chained along each gallery
 * rail. A bulb is dead, flickering or lit by its own seed and the stage, so the same bulb stays dead for everyone who
 * looks; once Attuned the lit bulbs run a chase on each act's beat.
 */
public final class StringLights {
	/** Act beats, in ticks: the chase runs once a beat. */
	public static final float BEAT = 80.0F;
	private static final double SPACING = 0.6D;
	private static final double POLE_SAG = 0.07D;
	private static final double RAIL_SAG = 0.05D;
	private static final double MAX_SPAN = 40.0D;
	private static final double RAIL_LIFT = 0.15D;
	private static final float WIRE = 1.0F / 32.0F;
	private static final float CORE = 0.07F;
	private static final float HALO = 0.32F;
	private static final int DISC = 10;

	private StringLights() {
	}

	/** One hung wire, its bulbs and the seed its bulbs take theirs from. */
	public record Run(List<Vec3> points, long seed) {
	}

	/** Each pole collar to its two nearest others (each pair once), and each gallery rail's tops in a chain. */
	public static List<Run> runs(ShowMarks marks) {
		List<Run> runs = new ArrayList<>();
		List<Vec3> poles = marks.poleTops();
		Set<Long> paired = new HashSet<>();
		for (int i = 0; i < poles.size(); i++) {
			Vec3 from = poles.get(i);
			List<Integer> others = new ArrayList<>();
			for (int j = 0; j < poles.size(); j++) if (j != i) others.add(j);
			others.sort((a, b) -> Double.compare(poles.get(a).distanceToSqr(from), poles.get(b).distanceToSqr(from)));
			for (int k = 0; k < Math.min(2, others.size()); k++) {
				int j = others.get(k);
				long key = Math.min(i, j) * 4096L + Math.max(i, j);
				Vec3 to = poles.get(j);
				if (!paired.add(key) || from.distanceTo(to) > MAX_SPAN) continue;
				runs.add(new Run(Catenary.points(from, to, POLE_SAG, SPACING), key * 31L + 7L));
			}
		}
		for (List<Vec3> rail : marks.railLines()) {
			for (int i = 0; i + 1 < rail.size(); i++) {
				Vec3 from = rail.get(i).add(0.0D, RAIL_LIFT, 0.0D);
				Vec3 to = rail.get(i + 1).add(0.0D, RAIL_LIFT, 0.0D);
				if (from.distanceTo(to) > MAX_SPAN) continue;
				long seed = (long) Math.floor(from.x) * 73856093L ^ (long) Math.floor(from.z) * 19349663L
						^ (long) Math.floor(from.y) * 83492791L;
				runs.add(new Run(Catenary.points(from, to, RAIL_SAG, SPACING), seed));
			}
		}
		return runs;
	}

	/** Wire and bulbs, camera-relative. {@code left} and {@code up} are the camera's axes, so bulbs face the eye. */
	public static void render(VertexConsumer glow, Matrix4f matrix, Vec3 camera, Vector3f left, Vector3f up,
			List<Run> runs, Look look, float time) {
		if (look.bulbLit() <= 0.0F && look.flicker() <= 0.0F) return;
		float chaseAt = (time % BEAT) / BEAT;
		for (Run run : runs) {
			List<Vec3> points = run.points();
			for (int i = 0; i + 1 < points.size(); i++)
				wire(glow, matrix, camera, up, points.get(i), points.get(i + 1));
			for (int i = 1; i + 1 < points.size(); i++) {
				long seed = run.seed() * 1_000_003L + i;
				Bulb bulb = PhantomShowRules.bulb(look, seed);
				float bright = switch (bulb) {
					case DEAD -> 0.0F;
					case FLICKER -> flicker(time, seed);
					case LIT -> 0.82F + 0.18F * (float) Math.sin(time * 0.05F + PhantomShowRules.unit(seed) * 6.28F);
				};
				if (look.chase() && bulb == Bulb.LIT) {
					float along = i / (float) points.size();
					float gap = Math.abs(along - chaseAt);
					bright = Math.min(1.0F, bright + 0.6F * Math.max(0.0F, 1.0F - Math.min(gap, 1.0F - gap) * 8.0F));
				}
				bulb(glow, matrix, camera, left, up, points.get(i), bright);
			}
		}
	}

	/** A broken bulb's stutter: mostly on, dropping out in short bursts. */
	private static float flicker(float time, long seed) {
		float phase = time * 0.9F + PhantomShowRules.unit(seed) * 40.0F;
		float wave = (float) (Math.sin(phase) * Math.sin(phase * 2.7F + 1.3F));
		return wave > 0.35F ? 0.08F : 0.7F;
	}

	private static void wire(VertexConsumer glow, Matrix4f matrix, Vec3 camera, Vector3f up, Vec3 a, Vec3 b) {
		float ax = (float) (a.x - camera.x), ay = (float) (a.y - camera.y), az = (float) (a.z - camera.z);
		float bx = (float) (b.x - camera.x), by = (float) (b.y - camera.y), bz = (float) (b.z - camera.z);
		float ux = up.x() * WIRE, uy = up.y() * WIRE, uz = up.z() * WIRE;
		quad(glow, matrix, ax - ux, ay - uy, az - uz, bx - ux, by - uy, bz - uz, bx + ux, by + uy, bz + uz,
				ax + ux, ay + uy, az + uz, 0x2A0A10, 210);
	}

	private static void bulb(VertexConsumer glow, Matrix4f matrix, Vec3 camera, Vector3f left, Vector3f up, Vec3 at,
			float bright) {
		float x = (float) (at.x - camera.x), y = (float) (at.y - camera.y - 0.06D), z = (float) (at.z - camera.z);
		if (bright <= 0.02F) {
			glowDisc(glow, matrix, x, y, z, left, up, CORE * 0.8F, 0x3A2A22, 200);
			return;
		}
		glowDisc(glow, matrix, x, y, z, left, up, HALO, 0xFF9A5A, (int) (70 * bright));
		glowDisc(glow, matrix, x, y, z, left, up, HALO * 0.4F, 0xFFC890, (int) (150 * bright));
		glowDisc(glow, matrix, x, y, z, left, up, CORE, 0xFFF0D8, (int) (255 * bright));
	}

	/** A round glow facing the eye: full alpha at the middle falling to nothing at the rim. */
	private static void glowDisc(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z, Vector3f left,
			Vector3f up, float size, int rgb, int alpha) {
		int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF, a = Math.clamp(alpha, 0, 255);
		for (int i = 0; i < DISC; i++) {
			double a0 = i * Math.PI * 2.0D / DISC, a1 = (i + 1) * Math.PI * 2.0D / DISC;
			float c0 = (float) Math.cos(a0) * size, s0 = (float) Math.sin(a0) * size;
			float c1 = (float) Math.cos(a1) * size, s1 = (float) Math.sin(a1) * size;
			consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
			consumer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
			consumer.addVertex(matrix, x + left.x() * c1 + up.x() * s1, y + left.y() * c1 + up.y() * s1,
					z + left.z() * c1 + up.z() * s1).setColor(r, g, b, 0);
			consumer.addVertex(matrix, x + left.x() * c0 + up.x() * s0, y + left.y() * c0 + up.y() * s0,
					z + left.z() * c0 + up.z() * s0).setColor(r, g, b, 0);
		}
	}

	static void quad(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2,
			float z2, float x3, float y3, float z3, float x4, float y4, float z4, int rgb, int alpha) {
		int r = rgb >> 16 & 0xFF, g = rgb >> 8 & 0xFF, b = rgb & 0xFF, a = Math.clamp(alpha, 0, 255);
		consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
		consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
		consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
		consumer.addVertex(matrix, x4, y4, z4).setColor(r, g, b, a);
	}
}
