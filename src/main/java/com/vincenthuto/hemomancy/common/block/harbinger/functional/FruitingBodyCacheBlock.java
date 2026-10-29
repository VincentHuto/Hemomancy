package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.harbinger.functional.FruitingBodyCacheBlockEntity;
import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/** A fruiting body grown over a dead Apotheos's gear. Only its owner opens it; it never despawns. */
public class FruitingBodyCacheBlock extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 12, 14);

	public FruitingBodyCacheBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FruitingBodyCacheBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof FruitingBodyCacheBlockEntity cache)) return InteractionResult.PASS;
		if (!HeartNodeRules.mayOpen(cache.owner(), player.getUUID())) {
			player.displayClientMessage(Component.translatable("message.hemomancy.fruiting_body_cache.not_yours")
					.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
			return InteractionResult.CONSUME;
		}
		for (ItemStack stack : cache.takeAll()) {
			if (!player.getInventory().add(stack) && !stack.isEmpty()) player.drop(stack, false);
		}
		level.playSound(null, pos, SoundEvents.FUNGUS_BREAK, SoundSource.BLOCKS, 1.0f, 0.6f);
		level.removeBlock(pos, false);
		return InteractionResult.CONSUME;
	}

	/** The owner breaking it (rather than opening it) spills what it held; nothing is ever lost with the block. */
	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock()) && !level.isClientSide
				&& level.getBlockEntity(pos) instanceof FruitingBodyCacheBlockEntity cache) {
			for (ItemStack stack : cache.takeAll()) {
				Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}
}
