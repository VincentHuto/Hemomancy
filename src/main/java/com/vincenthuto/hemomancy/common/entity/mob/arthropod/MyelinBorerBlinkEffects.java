package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** The same yellow and white Ductilis bolt, gathered into a small glow at each end of a blink. */
final class MyelinBorerBlinkEffects {
	private static final LightningTestConfig ZAP = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
			32.0F, 0.0F, 0.0F, 0.0F, 42.0F, 1.4F, 5, 5, 0.045F, 0.009F, false, 0L, false, 8);
	private static final ParticleColor GOLD = new ParticleColor(255, 232, 74);
	private static final ParticleColor BLOOD = new ParticleColor(175, 20, 38);

	private MyelinBorerBlinkEffects() {
	}

	static void ball(ServerLevel level, Vec3 center) {
		level.sendParticles(HemoParticleData.glow(BLOOD), center.x, center.y, center.z,
				5, 0.16D, 0.16D, 0.16D, 0.0D);
		level.sendParticles(HemoParticleData.glow(GOLD), center.x, center.y, center.z,
				8, 0.12D, 0.12D, 0.12D, 0.0D);
		for (int i = 0; i < 2; i++) {
			Vec3 tip = center.add((level.random.nextDouble() - 0.5D) * 0.5D,
					(level.random.nextDouble() - 0.5D) * 0.5D,
					(level.random.nextDouble() - 0.5D) * 0.5D);
			LightningTesterSpawner.spawn(level, center, tip, ZAP);
		}
	}

	static void zap(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ZAP);
		level.playSound(null, from.x, from.y, from.z, SoundEvents.REDSTONE_TORCH_BURNOUT,
				SoundSource.HOSTILE, 0.6F, 1.6F);
		ball(level, to);
	}
}
