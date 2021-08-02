package com.huto.hemomancy.item;

import java.util.List;

import com.huto.hemomancy.Hemomancy.HemomancyItemGroup;
import com.huto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemEnzyme extends Item {

	EnumBloodTendency tend;
	float amount;

	public ItemEnzyme(EnumBloodTendency tend, float amount) {
		super(new Item.Properties().group(HemomancyItemGroup.instance));
		this.tend = tend;
		this.amount = amount;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add(new StringTextComponent("Tendency: " + tend).mergeStyle(TextFormatting.DARK_GREEN));
		tooltip.add(new StringTextComponent("Amount: " + amount).mergeStyle(TextFormatting.GOLD));

	}

	public EnumBloodTendency getTend() {
		return tend;
	}

	public float getAmount() {
		return amount;
	}

}