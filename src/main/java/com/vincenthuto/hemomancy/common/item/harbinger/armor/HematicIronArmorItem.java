package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

public class HematicIronArmorItem extends ArmorItem {

	public HematicIronArmorItem(Holder<ArmorMaterial> materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().fireResistant().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.hematic_iron_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}
}
