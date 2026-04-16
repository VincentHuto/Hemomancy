package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Consecrated Syringe — the ritual instrument used to petition a Preserved Corpus
 * for Hallowed Residuum. Not drawn, but petitioned; the outcome is never guaranteed.
 * Used by right-clicking on a Saint Sarcophagus while holding a filled Blood Vial.
 */
public class ConsecratedSyringeItem extends Item {

	public ConsecratedSyringeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("A syringe blessed by forgotten rites.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Use with a Blood Vial on a Saint's Sarcophagus.")
				.withStyle(ChatFormatting.DARK_PURPLE));
	}
}
