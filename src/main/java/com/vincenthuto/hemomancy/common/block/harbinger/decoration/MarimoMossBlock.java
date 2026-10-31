package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Marimo's moss (Brined Court Track 10): the mass that fills the Lighthouse Garden undercroft's lowest chamber and
 * threads up through the walls of the tiers above. It is part of Marimo himself, so it can't be broken outside
 * Creative (its block properties make it unbreakable and blast-proof, and it refuses pistons). Now and then a gold
 * mote lifts off an open face, the Giving Light's lichen in the old moss.
 */
public class MarimoMossBlock extends Block {
	static final ParticleColor GOLD = new ParticleColor(236, 186, 92);
	static final ParticleColor PALE = new ParticleColor(196, 210, 150);

	public MarimoMossBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextFloat() > 0.08F) return;
		Direction face = Direction.getRandom(random);
		BlockPos beyond = pos.relative(face);
		if (!level.getBlockState(beyond).isAir()) return;
		double x = pos.getX() + 0.5D + face.getStepX() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D;
		double y = pos.getY() + 0.5D + face.getStepY() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D;
		double z = pos.getZ() + 0.5D + face.getStepZ() * 0.55D + (random.nextDouble() - 0.5D) * 0.8D;
		level.addParticle(HemoParticleData.glow(random.nextBoolean() ? GOLD : PALE), x, y, z, 0.0D, 0.006D, 0.0D);
	}
}
