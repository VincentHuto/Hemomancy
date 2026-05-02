package com.vincenthuto.hemomancy.common.item.harbinger;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumDoctrineTag;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Hallowed Residuum — a hybridized saint-enzyme extracted from a Preserved Corpus.
 * Unlike standard enzymes, these carry two tendency alignments and doctrine tags
 * that affect crafting in the Visceral Recaller.
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
		tooltip.add(Component.literal("Saint: " + saintType.getDisplayName())
				.withStyle(ChatFormatting.GOLD));
		tooltip.add(Component.literal("Primary: " + saintType.getPrimaryTendency().name())
				.withStyle(ChatFormatting.DARK_RED));
		tooltip.add(Component.literal("Secondary: " + saintType.getSecondaryTendency().name())
				.withStyle(ChatFormatting.DARK_PURPLE));
		tooltip.add(Component.literal("Doctrine: " + saintType.getPrimaryDoctrine().getDisplayName()
				+ " / " + saintType.getSecondaryDoctrine().getDisplayName())
				.withStyle(ChatFormatting.LIGHT_PURPLE));
		tooltip.add(Component.literal("Potency: " + amount)
				.withStyle(ChatFormatting.AQUA));
	}

	public EnumSaintType getSaintType() {
		return saintType;
	}

	public EnumBloodTendency getPrimaryTendency() {
		return saintType.getPrimaryTendency();
	}

	public EnumBloodTendency getSecondaryTendency() {
		return saintType.getSecondaryTendency();
	}

	public EnumDoctrineTag getPrimaryDoctrine() {
		return saintType.getPrimaryDoctrine();
	}

	public EnumDoctrineTag getSecondaryDoctrine() {
		return saintType.getSecondaryDoctrine();
	}

	public float getAmount() {
		return amount;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}
}
