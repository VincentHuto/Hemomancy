package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.particle.factory.SporiticSporeParticleFactory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hutoslib.client.particle.factory.DarkGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectSpawner;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Authored particle, tendril, and lightning vocabulary for both Vesper forms. */
final class VesperVisualEffects {
	static final ParticleColor BLOOD = new ParticleColor(224, 0, 24);
	static final ParticleColor DEEP_BLOOD = new ParticleColor(88, 0, 18);
	static final ParticleColor BLACK = new ParticleColor(3, 0, 4);
	static final ParticleColor ICE = new ParticleColor(92, 202, 245);
	static final ParticleColor WHITE = new ParticleColor(242, 247, 255);
	static final ParticleColor EMBER = new ParticleColor(255, 62, 18);

	private static final int BLOOD_CORE = 0xF8E00018;
	private static final int BLOOD_GLOW = 0xB86A0018;
	private static final int VOID_CORE = 0xF8040008;
	private static final int VOID_GLOW = 0xB865128F;
	private static final int ICE_CORE = 0xF0EAFBFF;
	private static final int ICE_GLOW = 0xA852BFE8;

	private VesperVisualEffects() { }

	static ParticleColor tendencyColor(EnumBloodTendency tendency) {
		return color(VesperVisualRules.tendencyColorRgb(tendency));
	}

	static void glow(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, GlowParticleFactory.createData(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void darkGlow(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, DarkGlowParticleFactory.createData(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void bloodCells(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, BloodCellParticleFactory.createData(color), center, count, xSpread, ySpread, zSpread, speed);
	}

	static void embers(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed, float scale, int life) {
		send(level, EmberParticleFactory.createData(color, scale, 0.92F, life), center, count,
				xSpread, ySpread, zSpread, speed);
	}

	static void spores(ServerLevel level, Vec3 center, ParticleColor color, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		send(level, SporiticSporeParticleFactory.createData(color), center, count,
				xSpread, ySpread, zSpread, speed);
	}

	static void telegraphLine(ServerLevel level, Vec3 start, Vec3 end, ParticleColor color) {
		Vec3 delta = end.subtract(start).multiply(1.0D, 0.0D, 1.0D);
		if (delta.lengthSqr() < 0.01D) return;
		int points = Math.min(24, Math.max(1, (int) Math.ceil(delta.length() / 0.55D)));
		Vec3 step = delta.scale(1.0D / points);
		for (int i = 1; i <= points; i++) {
			Vec3 point = start.add(step.scale(i));
			ParticleOptions particle = i % 4 == 0
					? BloodCellParticleFactory.createData(color)
					: DarkGlowParticleFactory.createData(color);
			level.sendParticles(particle, point.x, point.y + 0.07D, point.z,
					1, 0.015D, 0.015D, 0.015D, 0.0D);
		}
	}

	static void telegraphRing(ServerLevel level, Vec3 center, double radius, ParticleColor color, int points) {
		for (int i = 0; i < points; i++) {
			double angle = Mth.TWO_PI * i / points;
			Vec3 point = center.add(Math.cos(angle) * radius, 0.08D, Math.sin(angle) * radius);
			ParticleOptions particle = i % 5 == 0
					? BloodCellParticleFactory.createData(color)
					: DarkGlowParticleFactory.createData(color);
			level.sendParticles(particle, point.x, point.y, point.z, 1, 0.02D, 0.01D, 0.02D, 0.0D);
		}
	}

	static void ambient(Level level, Vec3 center, double width, double height, boolean crowned,
			EnumBloodTendency tendency) {
		ParticleColor color = crowned ? BLOOD : tendencyColor(tendency);
		for (int i = 0; i < 3; i++) {
			double x = center.x + (level.random.nextDouble() - 0.5D) * width;
			double y = center.y + level.random.nextDouble() * height;
			double z = center.z + (level.random.nextDouble() - 0.5D) * width;
			ParticleOptions particle;
			if (crowned && i == 0) particle = SporiticSporeParticleFactory.createData(DEEP_BLOOD);
			else if (i == 0) particle = GlowParticleFactory.createData(color);
			else particle = i % 2 == 0 ? BloodCellParticleFactory.createData(color)
					: DarkGlowParticleFactory.createData(crowned ? BLACK : color);
			level.addParticle(particle, x, y, z, 0.0D, 0.008D, 0.0D);
		}
	}

	static void tendril(ServerLevel level, Vec3 start, Vec3 end, boolean glacial, long seed) {
		int core = glacial ? ICE_CORE : BLOOD_CORE;
		int glow = glacial ? ICE_GLOW : BLOOD_GLOW;
		float range = (float) Math.max(2.0D, start.distanceTo(end) + 2.0D);
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(core, glow)
				.withRange(range)
				.withLifecycle(3, 5, 8)
				.withShape(14, glacial ? 1 : 2, glacial ? 0.055F : 0.08F, 0.05F)
				.withBranching(glacial ? 1 : 3, 1, 0.24F, glacial ? 0.55F : 0.85F)
				.withWrithe(glacial ? 0.04F : 0.12F, 0.06F, glacial ? 0.22F : 0.72F, 0.05F)
				.withBlendColors(glacial)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}

	static void voidTendril(ServerLevel level, Vec3 start, Vec3 end, long seed) {
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withColors(VOID_CORE, VOID_GLOW)
				.withRange((float) Math.max(2.0D, start.distanceTo(end) + 2.0D))
				.withLifecycle(3, 5, 9)
				.withShape(16, 2, 0.09F, 0.05F)
				.withBranching(4, 2, 0.3F, 1.0F)
				.withWrithe(0.16F, 0.07F, 0.9F, 0.08F)
				.withBlendColors(false)
				.withFixedSeed(true, seed);
		TendrilEffectSpawner.spawn(level, new TendrilAnchor.Point(start), new TendrilAnchor.Point(end), config);
	}

	static void lightning(ServerLevel level, Vec3 start, Vec3 end, boolean glacial, long seed) {
		int outer = glacial ? 0xD8409DD8 : 0xE806020A;
		int inner = glacial ? 0xFFF0FBFF : 0xFFB80B18;
		LightningTestConfig config = new LightningTestConfig(LightningTestConfig.Backend.BOLT,
				outer, outer, inner, (float) Math.max(4.0D, start.distanceTo(end) + 4.0D),
				0.0F, 0.0F, 0.0F, 48.0F, 1.25F, 8, 7, 0.22F, 0.055F,
				true, seed, false, 20);
		LightningTesterSpawner.spawn(level, start, end, config);
	}

	private static ParticleColor color(int rgb) {
		return new ParticleColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
	}

	private static void send(ServerLevel level, ParticleOptions particle, Vec3 center, int count,
			double xSpread, double ySpread, double zSpread, double speed) {
		level.sendParticles(particle, center.x, center.y, center.z, count, xSpread, ySpread, zSpread, speed);
	}
}
