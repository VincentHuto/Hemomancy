package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The reusable rite focus stores recipe-authored media for staff-led rites.
 */
public class CardinalFocusBlock extends Block implements EntityBlock {

	public CardinalFocusBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CardinalFocusBlockEntity(pos, state);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (!player.isShiftKeyDown() && isActivationTool(stack)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		if (level.isClientSide) return ItemInteractionResult.SUCCESS;
		CardinalFocusBlockEntity focus = level.getBlockEntity(pos) instanceof CardinalFocusBlockEntity found
				? found : null;
		if (focus == null) return ItemInteractionResult.FAIL;
		CardinalRiteSavedData rites = CardinalRiteSavedData.get((ServerLevel) level);
		if (player.isShiftKeyDown() && focus.hasMedium()) {
			if (rites.hasRiteAt(pos)) {
				player.displayClientMessage(Component.literal("The active rite holds its medium fast.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
				return ItemInteractionResult.SUCCESS;
			}
			ItemStack extracted = focus.extractMedium();
			if (!player.getInventory().add(extracted)) player.drop(extracted, false);
			return ItemInteractionResult.SUCCESS;
		}
		if (rites.hasRiteAt(pos)) {
			player.displayClientMessage(Component.literal("The active rite will not accept another medium.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}
		if (focus.hasMedium()) {
			player.displayClientMessage(Component.literal("The Cardinal Focus already holds a medium.")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			return ItemInteractionResult.SUCCESS;
		}
		if (!focus.insertMedium(player, stack)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		player.displayClientMessage(Component.literal("The item seats in the Cardinal Focus as a rite medium.")
				.withStyle(ChatFormatting.DARK_RED), false);


		return ItemInteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hit) {
		if (!player.isShiftKeyDown()) return InteractionResult.PASS;
		return useItemOn(ItemStack.EMPTY, state, level, pos, player, InteractionHand.MAIN_HAND, hit).result();
	}

	private static boolean isActivationTool(ItemStack stack) {
		return stack.is(ItemInit.living_staff.get()) || stack.is(ItemInit.sanguine_formation.get());
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
			boolean movedByPiston) {
		if (!state.is(newState.getBlock())
				&& level.getBlockEntity(pos) instanceof CardinalFocusBlockEntity focus) {
			ItemStack medium = focus.extractMedium();
			if (!medium.isEmpty()) {
				Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.75D,
						pos.getZ() + 0.5D, medium);
			}
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

}
