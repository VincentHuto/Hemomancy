package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MycophantTendrilItem extends VasculariumCharmItem {
	public MycophantTendrilItem(Properties properties) {
		super(properties, EnumBloodTendency.ANIMUS, 0.0F);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flagIn) {
		tooltip.add(Component.translatable("item.hemomancy.mycophant_tendril.tooltip"));
		tooltip.add(Component.translatable("item.hemomancy.mycophant_tendril.tooltip.slot"));
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		return Rarity.EPIC;
	}
}
