package com.vincenthuto.hemomancy.common.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A blood-stained scroll dropped by the Harbinger Hermit upon death,
 * containing blueprints for the Rite of Sanguine Initiation.
 */
public class HarbingerRiteHintItem extends Item {

	public HarbingerRiteHintItem(Properties prop) {
		super(prop.stacksTo(1).rarity(Rarity.UNCOMMON));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.translatable("item.hemomancy.harbinger_rite_hint.tooltip"));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
