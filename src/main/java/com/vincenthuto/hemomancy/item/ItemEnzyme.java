package com.vincenthuto.hemomancy.item;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.vincenthuto.hemomancy.capa.player.tendency.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemEnzyme extends Item {

	EnumBloodTendency tend;
	float amount;

	public ItemEnzyme(EnumBloodTendency tend, float amount) {
		super(new Item.Properties().tab(HemomancyItemGroup.instance));
		this.tend = tend;
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
		tooltip.add(new TextComponent("Tendency: " + tend).withStyle(ChatFormatting.DARK_GREEN));
		tooltip.add(new TextComponent("Amount: " + amount).withStyle(ChatFormatting.GOLD));

	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	public float getAmount() {
		return amount;
	}

}