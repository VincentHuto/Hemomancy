package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
import com.vincenthuto.hemomancy.common.entity.boss.FootprintFrame;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * The troupe's effect set, drawn with the Hemomancy particle suite and {@link BossTelegraphs} in the pavilion's
 * crimson, bone and torch-fire. Telegraphs and impacts are focal and bold; the idle acts stay light.
 */
public final class CircusEffects {
	public static final ParticleColor CRIMSON = new ParticleColor(176, 20, 48);
	public static final ParticleColor CLOT = new ParticleColor(122, 16, 32);
	public static final ParticleColor BONE = new ParticleColor(232, 218, 192);
	public static final ParticleColor FLAME = new ParticleColor(238, 98, 30);
	public static final ParticleColor FLAME_CORE = new ParticleColor(255, 196, 120);
	/** ARGB for the HutosLib thread tendrils: a crimson core with a bone glow. */
	public static final int THREAD_CORE_ARGB = 0xFFB01430;
	public static final int THREAD_GLOW_ARGB = 0xFFE8DAC0;
	private static final double FOOTPRINT_STEP = 0.4D;

	private CircusEffects() {
	}

	/** A light puff of bone-coloured motes, for an acrobat's take-off or landing during an idle act. */
	public static void dust(ServerLevel level, Vec3 at, int count) {
		level.sendParticles(HemoParticleData.glow(BONE), at.x, at.y + 0.1D, at.z, count, 0.3D, 0.06D, 0.3D, 0.02D);
	}

	/** The gathering glow a performer draws into its hands while it winds up; pulses harder as the strike nears. */
	public static void gather(ServerLevel level, Vec3 at, ParticleColor color, float charge) {
		int count = 2 + Math.round(charge * 6.0F);
		level.sendParticles(HemoParticleData.glow(color), at.x, at.y, at.z, count, 0.18D, 0.18D, 0.18D, 0.01D);
		if (charge > 0.5F) level.sendParticles(HemoParticleData.bloodCell(CRIMSON), at.x, at.y, at.z, 2, 0.12D,
				0.12D, 0.12D, 0.02D);
	}

	/**
	 * A troupe floor ring, drawn every wind-up tick: a bone edge at the full radius that reads on venous stone, over a
	 * crimson body that fills out to the edge as the charge nears the strike.
	 */
	public static void ringFootprint(ServerLevel level, Vec3 center, double radius, float charge) {
		int points = Math.max(12, (int) Math.ceil(radius * 9.0D));
		for (int i = 0; i < points; i++) {
			double angle = Math.PI * 2.0D * i / points;
			level.sendParticles(HemoParticleData.glow(BONE), center.x + Math.cos(angle) * radius, center.y + 0.08D,
					center.z + Math.sin(angle) * radius, 1, 0.01D, 0.0D, 0.01D, 0.0D);
		}
		double fill = radius * Math.max(0.15F, charge);
		int body = Math.max(6, points / 2);
		for (int i = 0; i < body; i++) {
			double angle = Math.PI * 2.0D * (i + 0.5D) / body;
			level.sendParticles(i % 3 == 0 ? HemoParticleData.bloodCell(CRIMSON) : HemoParticleData.glow(CRIMSON),
					center.x + Math.cos(angle) * fill, center.y + 0.06D, center.z + Math.sin(angle) * fill, 1, 0.04D,
					0.0D, 0.04D, 0.0D);
		}
	}

	/** A troupe floor wedge, drawn every wind-up tick: a bone outline over a crimson body that fills with the charge. */
	public static void coneFootprint(ServerLevel level, Vec3 origin, Vec3 facing, double radius, double degrees,
			float charge) {
		Vec3 flat = facing.multiply(1.0D, 0.0D, 1.0D);
		if (flat.lengthSqr() < 1.0E-4D) return;
		double base = Math.atan2(flat.z, flat.x);
		double half = Math.toRadians(degrees * 0.5D);
		int arc = Math.max(8, (int) (radius * degrees / 22.0D));
		for (int i = 0; i <= arc; i++) {
			double angle = base - half + 2.0D * half * i / arc;
			level.sendParticles(HemoParticleData.glow(BONE), origin.x + Math.cos(angle) * radius, origin.y + 0.08D,
					origin.z + Math.sin(angle) * radius, 1, 0.01D, 0.0D, 0.01D, 0.0D);
		}
		for (double edge : new double[] { base - half, base + half }) {
			Vec3 end = origin.add(Math.cos(edge) * radius, 0.0D, Math.sin(edge) * radius);
			lineFootprint(level, origin, end, BONE);
		}
		double fill = radius * Math.max(0.15F, charge);
		for (int i = 0; i <= arc / 2; i++) {
			double angle = base - half + 2.0D * half * i / Math.max(1, arc / 2);
			level.sendParticles(i % 3 == 0 ? HemoParticleData.bloodCell(CRIMSON) : HemoParticleData.glow(CRIMSON),
					origin.x + Math.cos(angle) * fill, origin.y + 0.06D, origin.z + Math.sin(angle) * fill, 1, 0.04D,
					0.0D, 0.04D, 0.0D);
		}
	}

	/**
	 * Draws a {@link BossFootprint} on the floor from the same {@code hits} test the strike uses, so the drawn edge is
	 * the hit edge: a bone edge where the shape ends, and a crimson body that fills out from {@code centre} with the
	 * charge. {@code reach} bounds the sampled square round the centre.
	 */
	public static void footprint(ServerLevel level, BossFootprint shape, FootprintFrame frame, Vec3 centre, double reach,
			float charge) {
		double step = FOOTPRINT_STEP;
		int cells = (int) Math.ceil(reach / step) + 1;
		double fill = reach * Math.max(0.15F, charge);
		double y = frame.floorY() + 0.08D;
		for (int i = -cells; i <= cells; i++) {
			for (int j = -cells; j <= cells; j++) {
				Vec3 point = new Vec3(centre.x + i * step, frame.floorY(), centre.z + j * step);
				if (!shape.hits(frame, point, 0.0D)) continue;
				boolean edge = !shape.hits(frame, point.add(step, 0.0D, 0.0D), 0.0D)
						|| !shape.hits(frame, point.add(-step, 0.0D, 0.0D), 0.0D)
						|| !shape.hits(frame, point.add(0.0D, 0.0D, step), 0.0D)
						|| !shape.hits(frame, point.add(0.0D, 0.0D, -step), 0.0D);
				if (edge) {
					level.sendParticles(HemoParticleData.glow(BONE), point.x, y, point.z, 1, 0.01D, 0.0D, 0.01D, 0.0D);
				} else if (Math.floorMod(i + j, 3) == 0 && point.distanceTo(new Vec3(centre.x, point.y, centre.z)) <= fill) {
					level.sendParticles(Math.floorMod(i, 4) == 0 ? HemoParticleData.bloodCell(CRIMSON) : HemoParticleData.glow(CRIMSON),
							point.x, y - 0.02D, point.z, 1, 0.04D, 0.0D, 0.04D, 0.0D);
				}
			}
		}
	}

	/** The Spotlight's beam: a column of bone light falling from above onto the pool it lights. */
	public static void spotlight(ServerLevel level, Vec3 floor, double radius, float strength) {
		for (int i = 0; i < 6; i++) {
			double height = 1.0D + i * 1.1D;
			double spread = radius * (0.25D + 0.12D * i);
			level.sendParticles(HemoParticleData.glow(BONE), floor.x, floor.y + height, floor.z,
					1 + Math.round(strength * 2.0F), spread * 0.4D, 0.25D, spread * 0.4D, 0.0D);
		}
		BossTelegraphs.ring(level, floor.add(0.0D, 0.1D, 0.0D), radius, BONE, 24);
	}

	/** A downed performer's thread drawn taut to his hand, and a pulse of its blood along it. */
	public static void pullThread(ServerLevel level, Vec3 from, Vec3 hand, long seed) {
		BossTelegraphs.tendril(level, from, hand, THREAD_CORE_ARGB, THREAD_GLOW_ARGB, seed);
		BossTelegraphs.bloodCells(level, from, CRIMSON, 8, 0.2D, 0.06D);
	}

	/** A floor line, drawn every wind-up tick: glowing points in {@code color} stepped from start to end. */
	public static void lineFootprint(ServerLevel level, Vec3 start, Vec3 end, ParticleColor color) {
		Vec3 delta = end.subtract(start).multiply(1.0D, 0.0D, 1.0D);
		if (delta.lengthSqr() < 0.01D) return;
		int points = Math.min(30, Math.max(2, (int) Math.ceil(delta.length() / 0.4D)));
		for (int i = 1; i <= points; i++) {
			Vec3 point = start.add(delta.scale(i / (double) points));
			level.sendParticles(HemoParticleData.glow(color), point.x, start.y + 0.08D, point.z, 1, 0.01D, 0.0D,
					0.01D, 0.0D);
		}
	}

	/** A bold contact burst: blood cells thrown out, a crimson flash and a ring on the ground. */
	public static void impact(ServerLevel level, Vec3 at, double radius) {
		BossTelegraphs.bloodCells(level, at, CRIMSON, 18, 0.35D, 0.12D);
		level.sendParticles(HemoParticleData.hitGlow(CRIMSON), at.x, at.y, at.z, 3, 0.15D, 0.15D, 0.15D, 0.0D);
		BossTelegraphs.glow(level, at, BONE, 6, 0.3D);
		BossTelegraphs.ring(level, new Vec3(at.x, Math.floor(at.y), at.z), radius, CRIMSON, 16);
	}

	/** A vault's vanish or arrival: a column of dark glow and blood cells where the body was. */
	public static void vault(ServerLevel level, Vec3 feet) {
		for (int i = 0; i < 4; i++) {
			Vec3 point = feet.add(0.0D, 0.45D * i, 0.0D);
			level.sendParticles(HemoParticleData.darkGlow(CLOT), point.x, point.y, point.z, 4, 0.22D, 0.12D, 0.22D,
					0.02D);
		}
		BossTelegraphs.bloodCells(level, feet.add(0.0D, 1.0D, 0.0D), CRIMSON, 14, 0.3D, 0.08D);
	}

	/** Fire along a breath: ember sparks stepped out from the mouth, a hot core inside a wider flame. */
	public static void flameStream(ServerLevel level, Vec3 mouth, Vec3 direction, int steps, double spacing,
			int perStep, double spread) {
		Vec3 step = direction.normalize().scale(spacing);
		for (int i = 1; i <= steps; i++) {
			Vec3 point = mouth.add(step.scale(i));
			double widen = spread * i / steps;
			level.sendParticles(HemoParticleData.emberSpark(FLAME, 0.32F, 0.9F, 14), point.x, point.y, point.z,
					perStep, widen, widen, widen, 0.02D);
			if (i % 2 == 0) level.sendParticles(HemoParticleData.emberSpark(FLAME_CORE, 0.2F, 1.0F, 10), point.x,
					point.y, point.z, 1, widen * 0.4D, widen * 0.4D, widen * 0.4D, 0.01D);
		}
	}

	/** The ruin of a carousel: a broad blood-cell blast and two rings rolling out from the hub. */
	public static void collapse(ServerLevel level, Vec3 hub) {
		BossTelegraphs.bloodCells(level, hub.add(0.0D, 1.0D, 0.0D), CRIMSON, 60, 1.6D, 0.18D);
		BossTelegraphs.glow(level, hub.add(0.0D, 1.5D, 0.0D), BONE, 20, 1.4D);
		BossTelegraphs.ring(level, hub, 2.5D, CRIMSON, 28);
		BossTelegraphs.ring(level, hub, 4.0D, CLOT, 36);
	}
}
