package com.vincenthuto.hemomancy.common.item.unstained;

import com.vincenthuto.hemomancy.client.screen.skilltree.unstained.UnstainedProgressScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Portable journal for Acolyte observances; opens the Unstained progress ledger. */
public class BookOfObservancesItem extends Item {
	public BookOfObservancesItem(Properties properties) { super(properties); }

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level.isClientSide) UnstainedProgressScreen.openScreen();
		return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(Component.translatable("item.hemomancy.book_of_observances.tooltip").withStyle(ChatFormatting.GRAY));
	}
}
