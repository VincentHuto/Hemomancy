package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/**
 * Putriciel's server-sent accents. Geometry carries every effect ({@code PUTRICIEL_OFFERING} bursts drawn by the
 * client's Putriciel geometry); particles are Hemomancy accents only, fine glow motes for traces, never
 * {@code hitGlow} and never a vanilla particle. Rot accents are olive, ochre and cream; only fire bursts carry the
 * Flammeus crimson and pink-white.
 */
public final class PutricielEffects {
	private static final ParticleColor SPORE = new ParticleColor(145, 139, 78);
	private static final ParticleColor CREAM = new ParticleColor(232, 216, 160);
	/** The Flammeus pyre fire: a crimson body, pink-white heat, black soot. */
	private static final ParticleColor FLAME = new ParticleColor(214, 30, 46);
	private static final ParticleColor HEAT = new ParticleColor(255, 168, 190);
	private static final ParticleColor SOOT = new ParticleColor(22, 10, 12);
	/** The overgrown bands' spore glow, the one green the ruling allows. */
	private static final ParticleColor SPORE_GLOW = new ParticleColor(118, 206, 92);

	private PutricielEffects() {
	}

	/** A geometry burst of {@code kind} ({@link PutricielVisualRules}) with a few matching motes. */
	public static void burst(ServerLevel level, Vec3 at, int kind, float radius) {
		int ticks = kind == PutricielVisualRules.REST ? PutricielVisualRules.REST_TICKS : PutricielVisualRules.BURST_TICKS;
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.PUTRICIEL_OFFERING, at, at, radius, ticks, kind);
		if (PutricielVisualRules.fireBurst(kind)) {
			level.sendParticles(HemoParticleData.emberSpark(FLAME, .12F, .85F, 22), at.x, at.y, at.z, 4, .15, .2, .15, .03);
			level.sendParticles(HemoParticleData.glow(HEAT), at.x, at.y, at.z, 1, 0, 0, 0, 0);
		} else {
			level.sendParticles(HemoParticleData.glow(CREAM), at.x, at.y, at.z, 1, 0, 0, 0, 0);
			level.sendParticles(HemoParticleData.sporiticSpore(SPORE), at.x, at.y, at.z, 4, .2, .3, .2, .015);
		}
	}

	/** Soot lifting where the fire gutters. */
	public static void soot(ServerLevel level, Vec3 at, int count) {
		level.sendParticles(HemoParticleData.darkGlow(SOOT), at.x, at.y, at.z, count, .3, .4, .3, .015);
	}

	/**
	 * A chamber light switching with the fire cycle: a pyre flaring back up in crimson and pink, or guttering into
	 * soot; a spore shelf breathing out its green glow, or fading.
	 */
	public static void lightShift(ServerLevel level, BlockPos pos, PutricielChamberLightingRules.Light light, boolean on) {
		double x = pos.getX() + .5, z = pos.getZ() + .5;
		if (light == PutricielChamberLightingRules.Light.PYRE) {
			double y = pos.getY() + .7;
			if (on) {
				level.sendParticles(HemoParticleData.emberSpark(FLAME, .16F, .9F, 26), x, y, z, 7, .18, .15, .18, .03);
				level.sendParticles(HemoParticleData.glow(HEAT), x, y + .2, z, 2, .08, .1, .08, .01);
			} else {
				level.sendParticles(HemoParticleData.darkGlow(SOOT), x, y + .3, z, 6, .2, .25, .2, .02);
				level.sendParticles(HemoParticleData.emberSpark(FLAME, .08F, .6F, 18), x, y, z, 3, .15, .05, .15, .01);
			}
		} else {
			double y = pos.getY() + .5;
			if (on) {
				level.sendParticles(HemoParticleData.glow(SPORE_GLOW), x, y, z, 3, .25, .2, .25, .005);
				level.sendParticles(HemoParticleData.sporiticSpore(SPORE_GLOW), x, y, z, 6, .3, .25, .3, .01);
			} else {
				level.sendParticles(HemoParticleData.darkGlow(SPORE_GLOW), x, y, z, 2, .2, .15, .2, .005);
			}
		}
	}
}
