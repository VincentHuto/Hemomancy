package com.vincenthuto.hemomancy.common.item.harbinger;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Reusable Chamber theme focus; its interaction is authoritative on the tossed entity. */
public final class OrbOfPerspectiveItem extends Item {
	public OrbOfPerspectiveItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.orb_of_perspective.tooltip")
				.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
	}
}
