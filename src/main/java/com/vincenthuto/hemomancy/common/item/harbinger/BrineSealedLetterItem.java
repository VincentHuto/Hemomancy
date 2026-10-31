package com.vincenthuto.hemomancy.common.item.harbinger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * An undelivered letter of introduction to the Brined Court, sealed with Sunburst crust, carried by a drowned envoy.
 * A Brined Votary drops one, once, for a player who doesn't know the Court; a Vicar, Alchemist or Voyager reads it.
 */
public final class BrineSealedLetterItem extends Item {
	public BrineSealedLetterItem(Properties properties) {
		super(properties.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, context, tooltip, flag);
		tooltip.add(Component.translatable("item.hemomancy.brine_sealed_letter.tooltip")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("item.hemomancy.brine_sealed_letter.tooltip.use")
				.withStyle(ChatFormatting.DARK_GRAY));
	}
}
