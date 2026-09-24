package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/** Red fragments burst away while chewing; pale gold light gathers into a repaired fiber. */
final class MyelinBorerWorkEffects {
	private static final ParticleColor CHEWING = new ParticleColor(202, 32, 48);
	private static final ParticleColor REPAIRING = new ParticleColor(245, 228, 142);

	private MyelinBorerWorkEffects() {
	}

	static void chewing(ServerLevel level, BlockPos target, BlockState state) {
		double x = target.getX() + 0.5D;
		double y = target.getY() + 0.75D;
		double z = target.getZ() + 0.5D;
		level.sendParticles(HemoParticleData.glow(CHEWING), x, y, z, 4, 0.22D, 0.12D, 0.22D, 0.02D);
		level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y, z,
				3, 0.18D, 0.10D, 0.18D, 0.07D);
	}

	static void repairing(ServerLevel level, BlockPos target) {
		level.sendParticles(HemoParticleData.glow(REPAIRING),
				target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D,
				6, 0.34D, 0.34D, 0.34D, 0.0D);
	}
}
