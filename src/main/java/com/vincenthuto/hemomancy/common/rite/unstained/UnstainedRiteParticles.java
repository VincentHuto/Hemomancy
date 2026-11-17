package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.common.particle.HemoPalette;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * The particles each Unstained cardinal rite completes with: pale still-water glows, Lethean drops and a fine verdigris
 * film on the ground. Speeds are half the old vanilla figures, since the glow doubles its starting velocity.
 */
public final class UnstainedRiteParticles {
	private UnstainedRiteParticles() {}

	private static final ParticleColor CLOT = new ParticleColor(70, 6, 14);
	private static final ParticleColor FROST_FINE = new ParticleColor(236, 246, 255);

	public static void letheanBaptism(ServerLevel level, Entity caster) {
		level.sendParticles(HemoParticleData.glow(HemoPalette.STILL_WHITE), caster.getX(), caster.getY() + 1.0, caster.getZ(),
				30, 0.5, 1.0, 0.5, 0.025);
		level.sendParticles(HemoParticleData.letheanDrip(), caster.getX(), caster.getY() + 2.0, caster.getZ(),
				20, 0.5, 0.2, 0.5, 0.0);
	}

	public static void silverVeil(ServerLevel level, Entity caster) {
		level.sendParticles(HemoParticleData.glow(HemoPalette.STILL_SILVER), caster.getX(), caster.getY() + 1.0, caster.getZ(),
				50, 1.0, 1.5, 1.0, 0.01);
		level.sendParticles(HemoParticleData.emberSpark(HemoPalette.STILL_WHITE, 0.2F, 0.8F, 30),
				caster.getX(), caster.getY() + 1.0, caster.getZ(), 12, 1.0, 1.5, 1.0, 0.01);
	}

	/** Blood magic leaving the caster: still light rising, dark clots falling away below. */
	public static void clarityAscension(ServerLevel level, Entity caster) {
		level.sendParticles(HemoParticleData.glow(HemoPalette.STILL_WHITE), caster.getX(), caster.getY() + 1.0, caster.getZ(),
				80, 1.5, 2.0, 1.5, 0.04);
		level.sendParticles(HemoParticleData.darkGlow(CLOT), caster.getX(), caster.getY() + 0.2, caster.getZ(),
				30, 1.0, 0.2, 1.0, 0.01);
	}

	public static void closedVein(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.PALE_GLOW), center, 120, radius * 0.25, 2.0, 0.0075);
	}

	public static void antisepticGround(ServerLevel level, BlockPos center, double radius) {
		film(level, center, 120, 0.9F, radius * 0.35, 0.5, 0.005);
	}

	public static void glassLungs(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(FROST_FINE), center, 140, radius * 0.25, 2.5, 0.0075);
	}

	/** Silver under the moon, verdigris by day. */
	public static void moonWashedCopper(ServerLevel level, BlockPos center, double radius, boolean night) {
		above(level, HemoParticleData.glow(night ? HemoPalette.STILL_SILVER : HemoPalette.VERDIGRIS), center,
				200, radius * 0.22, 3.5, 0.009);
	}

	public static void letheanJudgment(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_WHITE), center, 100, radius * 0.5, 2.0, 0.005);
		drips(level, center, 30, radius * 0.5);
	}

	public static void silverDawn(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_SILVER), center, 150, radius * 0.5, 3.0, 0.01);
		above(level, HemoParticleData.hitGlow(HemoPalette.STILL_WHITE), center, 1, 0.0, 0.0, 0.0);
		film(level, center, 80, 0.8F, radius * 0.4, 1.5, 0.005);
	}

	public static void stillWaters(ServerLevel level, BlockPos center, double radius) {
		drips(level, center, 60, radius * 0.3);
		above(level, HemoParticleData.glow(HemoPalette.PALE_GLOW), center, 30, radius * 0.3, 1.5, 0.0025);
	}

	public static void paleConsecration(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.PALE_GLOW), center, 80, radius * 0.4, 1.5, 0.005);
		film(level, center, 40, 0.8F, radius * 0.3, 0.5, 0.0025);
	}

	public static void silthmereRemembrance(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_WHITE), center, 120, radius * 0.3, 3.0, 0.01);
	}

	public static void letheCovenant(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_WHITE), center, 200, radius * 0.2, 4.0, 0.01);
		drips(level, center, 40, radius * 0.2);
	}

	public static void letheanTide(ServerLevel level, Entity caster) {
		level.sendParticles(HemoParticleData.glow(HemoPalette.PALE_GLOW), caster.getX(), caster.getY() + 1.0, caster.getZ(),
				120, 7.0, 5.0, 7.0, 0.02);
	}

	public static void paleVigil(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_SILVER), center, 160, radius * 0.25, 4.0, 0.01);
		film(level, center, 80, 0.8F, radius * 0.2, 2.0, 0.005);
	}

	public static void letheanFont(ServerLevel level, BlockPos center, double radius) {
		above(level, HemoParticleData.glow(HemoPalette.STILL_WHITE), center, 300, radius * 0.15, 5.0, 0.0125);
		drips(level, center, 60, radius * 0.15);
		above(level, HemoParticleData.hitGlow(HemoPalette.STILL_WHITE), center, 1, 0.0, 0.0, 0.0);
		film(level, center, 150, 0.8F, radius * 0.1, 3.0, 0.0075);
	}

	private static void above(ServerLevel level, ParticleOptions particle, BlockPos center, int count, double spread,
			double height, double speed) {
		level.sendParticles(particle, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, count, spread, height,
				spread, speed);
	}

	/** Lethean drops falling from a block above the rite's glow. */
	private static void drips(ServerLevel level, BlockPos center, int count, double spread) {
		level.sendParticles(HemoParticleData.letheanDrip(), center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
				count, spread, 1.0, spread, 0.0);
	}

	/** The fine verdigris film a cleansing rite leaves on the ground. */
	private static void film(ServerLevel level, BlockPos center, int count, float alpha, double spread, double height,
			double speed) {
		level.sendParticles(HemoParticleData.emberSpark(HemoPalette.VERDIGRIS, 0.031F, alpha, 40),
				center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, count, spread, height, spread, speed);
	}
}
