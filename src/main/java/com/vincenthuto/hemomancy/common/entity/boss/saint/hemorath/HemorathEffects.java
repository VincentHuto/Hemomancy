package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * The one place the server sends Hemorath's world effects: bursts of the {@code HEMORATH_RELIQUARY} form (drawn by
 * {@code HemorathReliquaryGeometry} as layered blood and iron geometry), fine {@link HemoParticleData} motes as accents
 * that survive Minimal particles, and his sounds. No vanilla particles.
 */
public final class HemorathEffects {
	private static final ParticleColor BLOOD = new ParticleColor(159, 20, 35), CORE = new ParticleColor(255, 210, 196);
	private static final ParticleColor RUST = new ParticleColor(112, 40, 28);
	/** A drained player's motes drift to his ribs at this speed. */
	private static final double MOTE_SPEED = 0.08D;

	private HemorathEffects() {
	}

	public static void burst(ServerLevel level, Vec3 at, int kind, float radius) {
		int ticks = kind == HemorathVisualRules.EXSANGUINATE ? HemorathVisualRules.FINALE_TICKS : HemorathVisualRules.BURST_TICKS;
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.HEMORATH_RELIQUARY, at, at, radius, ticks, kind);
		level.sendParticles(HemoParticleData.glow(CORE), at.x, at.y, at.z, 1, 0, 0, 0, 0);
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z,
				kind == HemorathVisualRules.EXSANGUINATE ? 10 : 4, .15, .2, .15, .035);
	}

	/** A short-lived flow from one point to another: an intake line, a well's stream, the road home. */
	public static void flow(ServerLevel level, Vec3 from, Vec3 to, int kind, int ticks, double radius) {
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.HEMORATH_RELIQUARY, from, to, radius, ticks, kind);
	}

	public static void intake(ServerLevel level, Vec3 from, Vec3 to) {
		flow(level, from, to, HemorathVisualRules.INTAKE, 16, .8D);
	}

	/** One pulse of the drain: a blood cell and a fine glow mote lift off the player and drift toward his ribs. */
	public static void drainMotes(HemorathEntity saint, Player player) {
		if (!(saint.level() instanceof ServerLevel level)) return;
		Vec3 from = player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
		Vec3 heading = saint.chest().subtract(from).normalize();
		level.sendParticles(HemoParticleData.bloodCell(BLOOD), from.x, from.y, from.z, 0, heading.x, heading.y + 0.15D,
				heading.z, MOTE_SPEED);
		level.sendParticles(HemoParticleData.glow(CORE), from.x, from.y, from.z, 0, heading.x, heading.y + 0.1D, heading.z,
				MOTE_SPEED * 0.8D);
	}

	/** Rust flakes off a torn or burst patch. */
	public static void flakes(ServerLevel level, Vec3 at, int count) {
		level.sendParticles(HemoParticleData.bloodCell(RUST), at.x, at.y, at.z, count, .12, .12, .12, .03);
	}

	public static void sound(Entity at, SoundEvent sound, float volume, float pitch) {
		at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
	}

	public static void sound(Entity at, Holder<SoundEvent> sound, float volume, float pitch) {
		at.level().playSound(null, at.getX(), at.getY(), at.getZ(), sound, SoundSource.HOSTILE, volume, pitch);
	}

	public static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
		level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
	}

	public static void sound(ServerLevel level, Vec3 at, Holder<SoundEvent> sound, float volume, float pitch) {
		level.playSound(null, at.x, at.y, at.z, sound, SoundSource.HOSTILE, volume, pitch);
	}
}
