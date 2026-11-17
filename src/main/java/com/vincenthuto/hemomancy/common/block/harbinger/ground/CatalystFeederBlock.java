package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.CatalystFeederBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A small hopper-like block set beside a gouge or vein run (core-loop Track 6). It holds one stack of gouge catalysts
 * and dissolves one into a lit run each time the run would go dark, so a lit hallway or a hot run stays up while it is
 * stocked. Each catalyst it dissolves costs the run's lighter 15 mL, paid while they are online and alive in its level,
 * and it never lights a run that has gone dark: that takes blood again.
 * Fill it by hand or from a hopper; an empty hand takes the stack back.
 */
public class CatalystFeederBlock extends BaseEntityBlock {
	public static final MapCodec<CatalystFeederBlock> CODEC = simpleCodec(CatalystFeederBlock::new);
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

	public CatalystFeederBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected MapCodec<CatalystFeederBlock> codec() {
		return CODEC;
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CatalystFeederBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.catalyst_feeder.get(), CatalystFeederBlockEntity::serverTick);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CatalystFeederBlockEntity feeder)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (!feeder.accepts(stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if (!level.isClientSide) {
			ItemStack left = feeder.stock().insertItem(0, stack.copy(), false);
			if (!player.getAbilities().instabuild) player.setItemInHand(hand, left);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CatalystFeederBlockEntity feeder) || feeder.stock().getStackInSlot(0).isEmpty()) {
			return InteractionResult.PASS;
		}
		if (!level.isClientSide) {
			ItemStack taken = feeder.stock().extractItem(0, 64, false);
			if (!player.getInventory().add(taken)) player.drop(taken, false);
		}
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moved) {
		if (!state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof CatalystFeederBlockEntity feeder) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), feeder.stock().getStackInSlot(0).copy());
		}
		super.onRemove(state, level, pos, replacement, moved);
	}
}
