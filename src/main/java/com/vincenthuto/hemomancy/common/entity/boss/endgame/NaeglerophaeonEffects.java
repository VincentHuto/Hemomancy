package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.DuctilisLightningEffects;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import com.vincenthuto.hutoslib.common.lightning.LightningTesterSpawner;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Authored Ductilis light cues for the predator's charged core and attacks. */
final class NaeglerophaeonEffects {
	private static final LightningTestConfig ARC = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8F52D52, 0xE8F52D52, 0xFFD085FF,
			36.0F, 0.0F, 0.0F, 0.0F, 48.0F, 1.7F, 6, 6, 0.06F, 0.012F, false, 0L, false, 10);
	private static final ParticleColor GOLD = new ParticleColor(224, 40, 112);
	private static final ParticleColor BLOOD = new ParticleColor(175, 20, 38);
	private static final ParticleColor MENDING = new ParticleColor(255, 228, 112);
	private static final LightningTestConfig REPAIR_ARC = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xE8FFE84A, 0xE8FFE84A, 0xFFFFFFFF,
			16, 0, 0, 0, 46, 1.6F, 6, 5, .06F, .012F, false, 0, false, 10);
	private static final LightningTestConfig PURPLE = new LightningTestConfig(
			LightningTestConfig.Backend.BOLT, 0xDDAA35FF, 0xDDAA35FF, 0xFFF3CBFF,
			36, 0, 0, 0, 48, 1.7F, 6, 6, .04F, .009F, false, 0, false, 10);

	private NaeglerophaeonEffects() {}

	static void coil(ServerLevel level, Vec3 center) {
		for (int i = 0; i < 3; i++) {
			Vec3 tip = center.add((level.random.nextDouble() - 0.5) * 2.0,
					(level.random.nextDouble() - 0.5) * 1.4,
					(level.random.nextDouble() - 0.5) * 2.0);
			LightningTesterSpawner.spawn(level, center, tip, ARC);
		}
		level.sendParticles(HemoParticleData.glow(GOLD), center.x, center.y, center.z,
				6, 0.5, 0.4, 0.5, 0.0);
	}

	static void telegraph(ServerLevel level, Vec3 from, Vec3 to) {
		level.sendParticles(HemoParticleData.glow(GOLD), from.x, from.y, from.z,
				12, 0.45, 0.45, 0.45, 0.0);
		LightningTesterSpawner.spawn(level, from, from.lerp(to, 0.35), ARC);
		level.playSound(null, from.x, from.y, from.z, SoundInit.ENTITY_NAEGLEROPHAEON_CHARGE.get(),
				SoundSource.HOSTILE, 1.1F, 0.85F);
	}

	static void zap(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		LightningTesterSpawner.spawn(level, from.add(0, .12, 0), to, PURPLE);
		level.sendParticles(HemoParticleData.glow(GOLD), to.x, to.y, to.z,
				16, 0.3, 0.5, 0.3, 0.0);
		level.playSound(null, from.x, from.y, from.z, SoundInit.ENTITY_NAEGLEROPHAEON_ZAP.get(),
				SoundSource.HOSTILE, 1.1F, 1.0F);
	}

	static void blink(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		coil(level, to);
		level.playSound(null, to.x, to.y, to.z, SoundInit.ENTITY_NAEGLEROPHAEON_BLINK.get(),
				SoundSource.HOSTILE, 1.0F, 1.2F);
	}

	static void drain(ServerLevel level, Vec3 from, Vec3 to) {
		LightningTesterSpawner.spawn(level, from, to, ARC);
		level.sendParticles(HemoParticleData.glow(BLOOD), to.x, to.y, to.z,
				8, 0.4, 0.3, 0.4, 0.0);
		level.playSound(null, to.x, to.y, to.z, SoundInit.ENTITY_NAEGLEROPHAEON_DRAIN.get(),
				SoundSource.HOSTILE, 0.9F, 0.8F);
	}

	static void releaseBurst(Player player) {
		DuctilisLightningEffects.hemolymphalPulse(player);
	}

	static void repairing(ServerLevel level,Vec3 core,Vec3 gap) {
		LightningTesterSpawner.spawn(level,core,gap,REPAIR_ARC);
		level.sendParticles(HemoParticleData.glow(MENDING),gap.x,gap.y,gap.z,
				5,.22,.22,.22,0);
	}

	static void repaired(ServerLevel level,Vec3 gap) {
		level.sendParticles(HemoParticleData.glow(MENDING),gap.x,gap.y,gap.z,
				18,.45,.45,.45,0);
		for(int i=0;i<3;i++) {
			double angle=i*Math.PI*2/3;
			LightningTesterSpawner.spawn(level,gap,gap.add(Math.cos(angle)*.7,.25,Math.sin(angle)*.7),REPAIR_ARC);
		}
	}
}
