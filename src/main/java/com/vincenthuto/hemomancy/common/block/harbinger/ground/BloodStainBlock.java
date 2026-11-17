package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Tier 1 of the ground network: blood that had nowhere else to go, lying on the faces round an air cell. Wet, then
 * tacky, then dry, then gone, by random ticks; rain or water beside it washes it away at once. It stores nothing.
 * Plain Projection lays it where nothing else in the chain claims the blood; a death or heavy bleeding leaves one now and
 * then; Absorption takes part of a wet stain back up and cleans an older one.
 */
public class BloodStainBlock extends GroundMarkBlock {
	public static final MapCodec<BloodStainBlock> CODEC = simpleCodec(BloodStainBlock::new);
	/** 0 wet, 1 tacky, 2 dry ({@link GroundRunRules#WET}, {@link GroundRunRules#TACKY}, {@link GroundRunRules#DRY}). */
	public static final IntegerProperty AGE = IntegerProperty.create("age", GroundRunRules.WET, GroundRunRules.DRY);

	public BloodStainBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(AGE, GroundRunRules.WET));
	}

	@Override
	protected MapCodec<BloodStainBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AGE);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		GroundNetwork.ageStain(level, pos, state, random.nextFloat());
	}
}
