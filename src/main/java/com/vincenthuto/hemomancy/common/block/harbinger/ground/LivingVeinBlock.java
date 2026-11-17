package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.world.ground.VeinLedger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Tier 3 of the ground network: a gouge its owner has grown tissue into, from a {@link VeinNodeBlock}. It carries
 * everything a gouge can, and between two nodes it moves a little blood toward a tank that is drawing while its owner
 * is near (CL-14); left alone long enough it withers back to a gouge. Who grew each cell is kept in {@link VeinLedger},
 * which holds a player's total length to their degree.
 *
 * <p>Water does not break a living vein, and it does not dam water either (CQ-44, NQ-02-4): the cell holds any water
 * state, a source or flowing water of any level ({@link #WATER}), so a stream runs over the vein as it runs over open
 * ground. A vanilla fluid tick would turn flowing water into a water block and the vein with it; for a vein cell the
 * tick only changes {@link #WATER} ({@code MixinLivingVeinWater}). Filling in the last face of a wet cell, or the vein
 * withering back to gouge, leaves the water.
 */
public class LivingVeinBlock extends BloodGougeBlock implements LiquidBlockContainer, BucketPickup {
	public static final MapCodec<LivingVeinBlock> CODEC = simpleCodec(LivingVeinBlock::new);
	/** The water over the vein: 0 dry, 1 to 7 flowing water of that level, 8 a source, 9 falling water. */
	public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 9);
	public static final int DRY = 0;
	public static final int SOURCE = 8;
	public static final int FALLING = 9;

	public LivingVeinBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(WATER, DRY));
	}

	@Override
	protected MapCodec<LivingVeinBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(WATER);
	}

	/** The fluid a vein holding {@code water} shows. */
	public static FluidState fluidOf(int water) {
		if (water <= DRY) return Fluids.EMPTY.defaultFluidState();
		if (water == SOURCE) return Fluids.WATER.getSource(false);
		if (water == FALLING) return Fluids.WATER.getFlowing(8, true);
		return Fluids.WATER.getFlowing(water, false);
	}

	/** The {@link #WATER} value that holds {@code fluid}: dry for anything but water. */
	public static int waterOf(FluidState fluid) {
		if (fluid.isEmpty() || !fluid.getType().isSame(Fluids.WATER)) return DRY;
		if (fluid.isSource()) return SOURCE;
		if (fluid.getValue(net.minecraft.world.level.material.FlowingFluid.FALLING)) return FALLING;
		return Math.clamp(fluid.getAmount(), 1, 7);
	}

	/** Whether {@code state} is a vein with water over it. */
	public static boolean wet(BlockState state) {
		return state.getBlock() instanceof LivingVeinBlock && state.getValue(WATER) != DRY;
	}

	/**
	 * The fluid tick of a vein cell settled on {@code fluid}: the vein keeps its faces and takes the new water, where a
	 * plain cell would be replaced by a water block or air.
	 */
	public static void holdWater(Level level, BlockPos pos, BlockState vein, FluidState fluid) {
		level.setBlock(pos, vein.setValue(WATER, waterOf(fluid)), Block.UPDATE_ALL);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return fluidOf(state.getValue(WATER));
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getFluidState().isEmpty();
	}

	/** Any water may come in: a stream flows onto the vein's cell as onto open ground. */
	@Override
	public boolean canPlaceLiquid(@Nullable Player player, BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
		return fluid.isSame(Fluids.WATER);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluid) {
		int water = waterOf(fluid);
		if (water == DRY || water == state.getValue(WATER) || state.getValue(WATER) == SOURCE) return false;
		if (!level.isClientSide()) {
			level.setBlock(pos, state.setValue(WATER, water), Block.UPDATE_ALL);
			level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
		}
		return true;
	}

	/** A bucket takes up a source lying over the vein, as from any waterlogged block; flowing water cannot be taken. */
	@Override
	public ItemStack pickupBlock(@Nullable Player player, LevelAccessor level, BlockPos pos, BlockState state) {
		if (state.getValue(WATER) != SOURCE) return ItemStack.EMPTY;
		level.setBlock(pos, state.setValue(WATER, DRY), Block.UPDATE_ALL);
		return new ItemStack(Items.WATER_BUCKET);
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Fluids.WATER.getPickupSound();
	}

	/** Water over the vein keeps moving, and a cell that loses its last face keeps its water. */
	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		FluidState fluid = state.getFluidState();
		if (!fluid.isEmpty()) level.scheduleTick(pos, fluid.getType(), fluid.getType().getTickDelay(level));
		BlockState updated = super.updateShape(state, direction, neighborState, level, pos, neighborPos);
		return updated.isAir() && !fluid.isEmpty() ? fluid.createLegacyBlock() : updated;
	}

	/** What a withered cell leaves: a gouge where it was dry, its water where it was not (water breaks a gouge). */
	public static BlockState withered(BlockState vein, BlockState gouge) {
		FluidState fluid = vein.getFluidState();
		return fluid.isEmpty() ? gouge : fluid.createLegacyBlock();
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
		super.onRemove(state, level, pos, replacement, moved);
		if (level instanceof ServerLevel server && !(replacement.getBlock() instanceof LivingVeinBlock)) {
			VeinLedger.get(server).release(server, pos);
		}
	}
}
