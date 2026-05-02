package com.vincenthuto.hemomancy.common.item.unstained.tool;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

/**
 * Pale Silver Pickaxe — an Unstained utility tool.
 * <p>
 * Crafted from pale silver, the metal refined of all hematic taint.
 * While held in the mainhand, the resonant purity of the metal suppresses
 * hemomancy mob spawns within a 4-block radius — the cleansed do not
 * disturb the blood.
 * <p>
 * Spawn suppression is handled server-side by {@link PaleSilverPickaxeEvents}.
 */
public class PaleSilverPickaxeItem extends PickaxeItem {

	public PaleSilverPickaxeItem(Tier tier, int attackDamageIn, float attackSpeedIn, Properties properties) {
		super(tier, properties.attributes(PickaxeItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal(
				"Pale silver, refined of all hematic taint. The cleansed do not disturb the blood.")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Passive: suppresses mob spawns within 4 blocks while held.")
				.withStyle(ChatFormatting.WHITE));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}
}
