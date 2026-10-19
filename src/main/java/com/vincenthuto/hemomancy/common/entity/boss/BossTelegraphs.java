package com.vincenthuto.hemomancy.common.entity.boss;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Ground telegraphs and accent bursts shared by the endgame bosses, all drawn with the Hemomancy particle suite: a
 * line for a committed lunge, a ring for a burst, a wedge for a swing. Vesper and the Mycophant both read from here.
 */
public final class BossTelegraphs {
	private BossTelegraphs() {
	}

	public static void line(ServerLevel level, Vec3 start, Vec3 end, ParticleColor color) {
		Vec3 delta = end.subtract(start).multiply(1.0D, 0.0D, 1.0D);
		if (delta.lengthSqr() < 0.01D) return;
		int points = Math.min(24, Math.max(1, (int) Math.ceil(delta.length() / 0.55D)));
		Vec3 step = delta.scale(1.0D / points);
		for (int i = 1; i <= points; i++) {
			Vec3 point = start.add(step.scale(i));
			ParticleOptions particle = i % 4 == 0 ? HemoParticleData.bloodCell(color) : HemoParticleData.darkGlow(color);
			level.sendParticles(particle, point.x, point.y + 0.07D, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
		}
	}

	public static void ring(ServerLevel level, Vec3 center, double radius, ParticleColor color, int points) {
		for (int i = 0; i < points; i++) {
			double angle = Mth.TWO_PI * i / points;
			Vec3 point = center.add(Math.cos(angle) * radius, 0.08D, Math.sin(angle) * radius);
			ParticleOptions particle = i % 5 == 0 ? HemoParticleData.bloodCell(color) : HemoParticleData.darkGlow(color);
			level.sendParticles(particle, point.x, point.y, point.z, 1, 0.02D, 0.01D, 0.02D, 0.0D);
		}
	}

	/** A ground wedge: radial spokes plus the outer arc, so the struck sector reads before the swing. */
	public static void cone(ServerLevel level, Vec3 origin, Vec3 facing, double radius, double degrees,
			ParticleColor color) {
		Vec3 flat = facing.multiply(1.0D, 0.0D, 1.0D);
		if (flat.lengthSqr() < 1.0E-4D) return;
		double base = Math.atan2(flat.z, flat.x);
		double half = Math.toRadians(degrees * 0.5D);
		int arcPoints = Math.max(6, (int) (radius * degrees / 30.0D));
		for (int i = 0; i <= arcPoints; i++) {
			double angle = base - half + (2.0D * half) * i / arcPoints;
			Vec3 point = origin.add(Math.cos(angle) * radius, 0.08D, Math.sin(angle) * radius);
			level.sendParticles(i % 4 == 0 ? HemoParticleData.bloodCell(color) : HemoParticleData.darkGlow(color),
					point.x, point.y, point.z, 1, 0.02D, 0.01D, 0.02D, 0.0D);
		}
		for (double edge : new double[] { base - half, base + half }) {
			line(level, origin, origin.add(Math.cos(edge) * radius, 0.0D, Math.sin(edge) * radius), color);
		}
	}

	public static void glow(ServerLevel level, Vec3 center, ParticleColor color, int count, double spread) {
		level.sendParticles(HemoParticleData.glow(color), center.x, center.y, center.z, count, spread, spread, spread, 0.01D);
	}

	public static void bloodCells(ServerLevel level, Vec3 center, ParticleColor color, int count, double spread,
			double speed) {
		level.sendParticles(HemoParticleData.bloodCell(color), center.x, center.y, center.z, count, spread, spread,
				spread, speed);
	}

	public static void spores(ServerLevel level, Vec3 center, ParticleColor color, int count, double spread,
			double speed) {
		level.sendParticles(HemoParticleData.sporiticSpore(color), center.x, center.y, center.z, count, spread,
				spread * 0.6D, spread, speed);
	}

	/** A short-lived HutosLib tendril between two points; {@code core}/{@code glow} are ARGB. */
	public static void tendril(ServerLevel level, Vec3 start, Vec3 end, int core, int glow, long seed) {
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(core, glow)
				.withRange((float) Math.max(2.0D, start.distanceTo(end) + 2.0D))
				.withLifecycle(3, 5, 8)
				.withShape(14, 2, 0.08F, 0.05F)
				.withBranching(2, 1, 0.24F, 0.7F)
				.withWrithe(0.14F, 0.06F, 0.8F, 0.05F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}
}
