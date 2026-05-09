package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PuppeteersSpindleBlock extends Block {
	private static final Component TITLE = Component.translatable("container.hemomancy.puppeteers_spindle");

	public PuppeteersSpindleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
											  Player player, InteractionHand hand, BlockHitResult hitResult) {
		return openSpindle(level, pos, player).consumesAction()
				? ItemInteractionResult.SUCCESS
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		return openSpindle(level, pos, player);
	}

	private InteractionResult openSpindle(Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		if (player instanceof ServerPlayer serverPlayer) {
			MenuProvider provider = new SimpleMenuProvider(
					(windowId, inventory, p) -> new PuppeteersSpindleMenu(windowId, inventory),
					TITLE);
			serverPlayer.openMenu(provider);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}
}
