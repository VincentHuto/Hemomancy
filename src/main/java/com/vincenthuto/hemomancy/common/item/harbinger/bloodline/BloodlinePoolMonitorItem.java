package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import com.vincenthuto.hemomancy.client.screen.item.BloodlinePoolScreen;
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

/**
 * A hand-held item that opens the Bloodline Pool management screen.
 * Allows the player to view the shared pool, donate, and configure trickle/auto-draw.
 */
public class BloodlinePoolMonitorItem extends Item {

	public BloodlinePoolMonitorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide) {
			BloodlinePoolScreen.openScreen();
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.literal("Right-click to manage your Bloodline Pool").withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("View, donate, and configure auto-draw settings").withStyle(ChatFormatting.GRAY));
	}
}
