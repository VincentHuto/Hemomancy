package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossTelegraphs;
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
