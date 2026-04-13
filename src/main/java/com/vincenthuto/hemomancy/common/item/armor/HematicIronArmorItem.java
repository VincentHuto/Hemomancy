package com.vincenthuto.hemomancy.common.item.armor;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class HematicIronArmorItem extends ArmorItem {

	public HematicIronArmorItem(ArmorMaterial materialIn, Type slot) {
		super(materialIn, slot, new Item.Properties().fireResistant());
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		tooltip.add(Component.translatable("tooltip.hemomancy.hematic_iron_set_bonus").withStyle(ChatFormatting.DARK_RED));
	}
}
