package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SaintRelicItem extends Item {
	private final EnumSaintType saintType;

	public SaintRelicItem(EnumSaintType saintType) {
		super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
		this.saintType = saintType;
	}

	public EnumSaintType getSaintType() {
		return saintType;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Saint: " + saintType.getDisplayName()).withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.literal("Use on a Saint Sarcophagus to wake this Saint.")
				.withStyle(ChatFormatting.DARK_RED));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
