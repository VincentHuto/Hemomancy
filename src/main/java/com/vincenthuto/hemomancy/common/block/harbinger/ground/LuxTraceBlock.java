package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * A fleeting line of light Hematic Beacon's Sculpting spreads across a surface: a gouge that was never cut, only lit.
 * Real light, dimming a step every {@link #FADE_STEP_TICKS} until it goes out. No baked model; the ground run renderer
 * draws it as thin lines joining its neighbours.
 */
public class LuxTraceBlock extends GroundMarkBlock {
	public static final MapCodec<LuxTraceBlock> CODEC = simpleCodec(LuxTraceBlock::new);
	public static final int MAX_FADE = 3;
	public static final IntegerProperty FADE = IntegerProperty.create("fade", 0, MAX_FADE);
	/** Four steps of a minute: a trace lights a cave for about four minutes. */
	public static final int FADE_STEP_TICKS = 1200;

	public LuxTraceBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FADE, 0));
	}

	@Override
	protected MapCodec<LuxTraceBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FADE);
	}

	/** Bright when new, a step dimmer each minute. */
	public static int lightOf(BlockState state) {
		return 13 - 3 * state.getValue(FADE);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (!level.isClientSide && !old.is(this)) level.scheduleTick(pos, this, FADE_STEP_TICKS);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int fade = state.getValue(FADE);
		if (fade >= MAX_FADE) {
			level.removeBlock(pos, false);
			return;
		}
		level.setBlock(pos, state.setValue(FADE, fade + 1), Block.UPDATE_CLIENTS);
		level.scheduleTick(pos, this, FADE_STEP_TICKS);
	}
}
