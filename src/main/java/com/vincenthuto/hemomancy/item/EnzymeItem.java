package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class EnzymeItem extends Item {

	EnumBloodTendency tend;
	float amount;

	public EnzymeItem(EnumBloodTendency tend, float amount) {
		super(new Item.Properties().tab(HemomancyItemGroup.instance));
		this.tend = tend;
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(Component.literal("Tendency: " + tend).withStyle(ChatFormatting.DARK_GREEN));
		tooltip.add(Component.literal("Amount: " + amount).withStyle(ChatFormatting.GOLD));

	}

	public float getAmount() {
		return amount;
	}

	public EnumBloodTendency getTend() {
		return tend;
	}

}