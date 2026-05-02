package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class AntiphonomycesResonansItem extends ItemFungalScar {

	/** Prevents the echo from triggering another echo within the same synchronous call stack. */
	public static boolean IS_ECHO_CAST = false;

	/** 20% chance to echo-cast on a successful manipulation use. */
	public static final float ECHO_CHANCE = 0.20f;

	public AntiphonomycesResonansItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
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
		tooltip.add(Component.literal("Crawling Choir").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Blood manipulations resonate through the mycelium. A second voice, quieter, follows.").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal("✦ 20% chance to echo-cast at reduced cost").withStyle(ChatFormatting.DARK_GREEN));
	}
}
