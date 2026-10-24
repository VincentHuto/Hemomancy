package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class AntiphonomycesResonansItem extends ItemFungalScar {

	/** Prevents the echo from triggering another echo within the same synchronous call stack. */
	public static boolean IS_ECHO_CAST = false;

	/** 20% chance to echo-cast on a successful manipulation use. */
	public static final float ECHO_CHANCE = 0.20f;

	public AntiphonomycesResonansItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("tooltip.hemomancy.antiphonomyces_resonans.crawling_choir").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.antiphonomyces_resonans.blood_manipulations_resonate_through_the_myceliu").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.antiphonomyces_resonans.20_chance_to_echo_cast_at").withStyle(ChatFormatting.DARK_GREEN));
	}
}
