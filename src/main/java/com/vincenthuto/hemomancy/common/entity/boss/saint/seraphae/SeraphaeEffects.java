package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Supplier;

/**
 * The one place the server sends Seraphae's cues: {@code SERAPHAE_RELIQUARY} material cues her geometry draws (the
 * cue's count is the {@link SeraphaeVisualRules} kind), fine glow motes as traces (white for her vessel light, gold
 * only for a nerve's spark) and her sounds. No hit glow and no vanilla particles.
 */
public final class SeraphaeEffects {
	private static final ParticleColor GOLD = new ParticleColor(255, 214, 120);
	private static final ParticleColor WHITE = new ParticleColor(255, 248, 236);

	private SeraphaeEffects() {
	}

	/** A cue at one point. */
	public static void cue(ServerLevel level, Vec3 at, int kind, float radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.SERAPHAE_RELIQUARY, at, at, radius,
				SeraphaeVisualRules.life(kind), kind);
	}

	/** A cue that travels from one point to another (a knot along a chain, a capture, a transfusion). */
	public static void cue(ServerLevel level, Vec3 from, Vec3 to, int kind, float radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.SERAPHAE_RELIQUARY, from, to, radius,
				SeraphaeVisualRules.life(kind), kind);
	}

	/** One fine white mote: a trace of her light along the floor or into a hand. */
	public static void mote(ServerLevel level, Vec3 at, Vec3 drift) {
		level.sendParticles(HemoParticleData.glow(WHITE), at.x, at.y, at.z, 0, drift.x, drift.y, drift.z, 1.0D);
	}

	/** A few fine gold sparks: a nerve firing. */
	public static void spark(ServerLevel level, Vec3 at, int count) {
		level.sendParticles(HemoParticleData.glow(GOLD), at.x, at.y, at.z, count, 0.08D, 0.12D, 0.08D, 0.015D);
	}

	/** A soft scatter of white motes: a growth burst, a fragment struck apart. */
	public static void scatter(ServerLevel level, Vec3 at, int count) {
		level.sendParticles(HemoParticleData.glow(WHITE), at.x, at.y, at.z, count, 0.18D, 0.24D, 0.18D, 0.02D);
	}

	public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
		level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
	}

	public static void sound(ServerLevel level, Vec3 at, Supplier<SoundEvent> sound, float volume, float pitch) {
		sound(level, at, sound.get(), volume, pitch);
	}

	/** A sound only one player hears (a growth gained). */
	public static void soundTo(Entity player, SoundEvent sound, float volume, float pitch) {
		if (player instanceof net.minecraft.server.level.ServerPlayer server) {
			server.playNotifySound(sound, SoundSource.HOSTILE, volume, pitch);
		}
	}
}
