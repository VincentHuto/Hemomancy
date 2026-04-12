package com.vincenthuto.hemomancy.common.item.tool;

import java.util.List;

import com.vincenthuto.hemomancy.client.screen.VascularStatusScreen;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class VascularStatusGaugeItem extends Item {

	public VascularStatusGaugeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			VascularStatusScreen.openScreen();
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.literal("Right-click to view your Vascular Status").withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Shows the health of each vein section").withStyle(ChatFormatting.GRAY));
	}
}
