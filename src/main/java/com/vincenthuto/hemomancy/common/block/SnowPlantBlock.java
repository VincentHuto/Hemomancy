package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hutoslib.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Snow Plant (Sarcodes sanguinea) — a striking, bright-red myco-heterotroph.
 * Found in forested biomes, gains carbon from fungi.
 * Grows in the dark on mycelium, erythrocytic mycelium, or infested wood.
 * Gives off a bleeding particle effect when a player is nearby.
 */
public class SnowPlantBlock extends FlowerBlock {

	public SnowPlantBlock(MobEffect effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		if (state.getBlock() == this) {
			return belowState.canSustainPlant(level, below, Direction.UP, this)
					|| belowState.getBlock() == BlockInit.erythrocytic_mycelium.get()
					|| belowState.getBlock() == BlockInit.infested_wood.get()
					|| belowState.is(net.minecraft.world.level.block.Blocks.MYCELIUM);
		}
		return this.mayPlaceOn(belowState, level, below);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);
		if (random.nextInt(3) == 0) {
			double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
			double y = pos.getY() + 0.4 + random.nextDouble() * 0.3;
			double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
			level.addParticle(BloodCellParticleFactory.createData(ParticleColor.BLOOD),
					x, y, z, 0.0, -0.04, 0.0);
		}
	}
}
