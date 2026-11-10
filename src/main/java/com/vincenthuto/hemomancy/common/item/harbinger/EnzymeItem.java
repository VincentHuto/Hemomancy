package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.util.HemoText;
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

	EnumBloodTemper tend;
	float amount;

	public EnzymeItem(EnumBloodTemper tend, float amount) {
		super(new Item.Properties());
		this.tend = tend;
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(HemoText.translatable("tooltip.hemomancy.enzyme.temper_s", tend).withStyle(ChatFormatting.DARK_GREEN));
		tooltip.add(Component.translatable("tooltip.hemomancy.enzyme.amount_s", amount).withStyle(ChatFormatting.GOLD));

	}

	public float getAmount() {
		return amount;
	}

	public EnumBloodTemper getTend() {
		return tend;
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}
}
