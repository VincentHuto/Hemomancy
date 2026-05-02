package com.vincenthuto.hemomancy.common.item.harbinger;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class EnzymeItem extends Item {

	EnumBloodTendency tend;
	float amount;

	public EnzymeItem(EnumBloodTendency tend, float amount) {
		super(new Item.Properties());
		this.tend = tend;
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
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