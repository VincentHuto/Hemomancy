package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Tier 2 of the ground network: a trench cut into the faces round an air cell, permanent until Absorption fills it in.
 * Dormant by default. Blood projected into any cell of a run sends a pulse along it, lighting the connected cells a
 * few a tick for a time set by the catalyst dissolved with it; the catalyst gives the lit run its {@link #FUNCTION}
 * ({@code data/hemomancy/gouge_catalyst/}). A lit cell keeps its own clock in {@code GroundRunSavedData} and a
 * scheduled tick that does its work and puts it out on time.
 *
 * <p>Water breaks a gouge (CQ-44, NQ-02-5): water that flows into its cell, or a bucket poured onto it, takes the cell
 * and the gouge is gone with its clock. Rain and water standing beside it do not wash it out as they wash a stain. A
 * living vein is the tier water does not break ({@link LivingVeinBlock}).
 */
public class BloodGougeBlock extends GroundMarkBlock {
	public static final MapCodec<BloodGougeBlock> CODEC = simpleCodec(BloodGougeBlock::new);
	public static final EnumProperty<GroundFunction> FUNCTION = EnumProperty.create("function", GroundFunction.class);
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public BloodGougeBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(FUNCTION, GroundFunction.NONE).setValue(LIT, false));
	}

	@Override
	protected MapCodec<? extends BloodGougeBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FUNCTION, LIT);
	}

	/** The block light a gouge or vein cell gives: its function's while lit, none dark. */
	public static int lightOf(BlockState state) {
		return state.getValue(LIT) ? state.getValue(FUNCTION).light() : 0;
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		GroundNetwork.tickCell(level, pos, state);
	}

	/**
	 * A cell that turns from gouge to vein or back (tissue grown, a vein withered) keeps its clock, but a scheduled tick
	 * belongs to the block that set it and the old one's is dropped, so the new block takes the cell's tick up again:
	 * otherwise a lit cell would stay lit for good.
	 */
	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
		super.onPlace(state, level, pos, old, moved);
		if (level instanceof ServerLevel server && !old.is(this) && old.getBlock() instanceof BloodGougeBlock
				&& (state.getValue(LIT) || GroundRunSavedData.get(server).get(pos) != null)) {
			server.scheduleTick(pos, this, 1);
		}
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
		super.onRemove(state, level, pos, replacement, moved);
		if (level instanceof ServerLevel server && !(replacement.getBlock() instanceof BloodGougeBlock)) {
			GroundNetwork.forgetCell(server, pos);
		}
	}
}
