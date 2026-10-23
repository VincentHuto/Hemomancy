package com.vincenthuto.hemomancy.common.item.unstained.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

import java.util.List;

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
		tooltip.add(Component.translatable("tooltip.hemomancy.pale_silver_pickaxe.pale_silver_refined_of_all_hematic")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.pale_silver_pickaxe.passive_suppresses_mob_spawns_within_4")
				.withStyle(ChatFormatting.WHITE));
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.WHITE);
	}
}
