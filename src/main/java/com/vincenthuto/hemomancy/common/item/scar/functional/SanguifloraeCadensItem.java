package com.vincenthuto.hemomancy.common.item.scar.functional;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.scar.ItemFungalScar;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class SanguifloraeCadensItem extends ItemFungalScar {

	/** Chance that killing an entity spawns a blood growth node. */
	public static final float ORCHARD_CHANCE = 0.30f;

	public SanguifloraeCadensItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}

	@Override
	public void onEquipped(LivingEntity player) {
		super.onEquipped(player);
	}

	@Override
	public void onUnequipped(LivingEntity player) {
		super.onUnequipped(player);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Vein Orchard").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Where blood falls, something takes root. Brief, but real.").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal("✦ 30% chance on kill: blood growth blooms at death site").withStyle(ChatFormatting.DARK_GREEN));
	}
}
