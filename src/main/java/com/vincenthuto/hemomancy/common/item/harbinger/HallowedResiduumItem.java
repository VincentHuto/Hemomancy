package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Hallowed Residuum — a hybridized saint-enzyme extracted from a Preserved Corpus.
 * Unlike standard enzymes, these carry the Saint's two tempers, the pair its
 * Canon Memory is woven from.
 */
public class HallowedResiduumItem extends Item {

	private final EnumSaintType saintType;
	private final float amount;

	public HallowedResiduumItem(EnumSaintType saintType, float amount) {
		super(new Item.Properties().stacksTo(16));
		this.saintType = saintType;
		this.amount = amount;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("tooltip.hemomancy.hallowed_residuum.saint_s", saintType.getDisplayName())
				.withStyle(ChatFormatting.GOLD));
		tooltip.add(HemoText.translatable("tooltip.hemomancy.hallowed_residuum.primary",
				saintType.getPrimaryTemper().name()).withStyle(ChatFormatting.DARK_RED));
		tooltip.add(HemoText.translatable("tooltip.hemomancy.hallowed_residuum.secondary",
				saintType.getSecondaryTemper().name()).withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.translatable("tooltip.hemomancy.hallowed_residuum.potency_s", amount)
				.withStyle(ChatFormatting.AQUA));
	}

	public EnumSaintType getSaintType() {
		return saintType;
	}

	public EnumBloodTemper getPrimaryTemper() {
		return saintType.getPrimaryTemper();
	}

	public EnumBloodTemper getSecondaryTemper() {
		return saintType.getSecondaryTemper();
	}

	public float getAmount() {
		return amount;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
