package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelReader;

import java.util.List;

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

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}
}
