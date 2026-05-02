package com.vincenthuto.hemomancy.common.item.unstained;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

public class CleansingHemolymphItem extends Item {

	public CleansingHemolymphItem(Properties prop) {
		super(prop.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("item.hemomancy.cleansing_hemolymph.tooltip"));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
