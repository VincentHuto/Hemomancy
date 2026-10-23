package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class SaprovittaVestigiumItem extends ItemFungalScar {

	/** Spawn a trail pulse every N ticks while moving. */
	public static final int TRAIL_INTERVAL_TICKS = 6;

	/** Damage dealt to hostile entities caught in the trail pulse. */
	public static final float TRAIL_DAMAGE = 1.5f;

	/** Radius of each trail pulse in blocks. */
	public static final double TRAIL_RADIUS = 1.5;

	/** Minimum horizontal movement per tick to consider the player "moving". */
	public static final double MOVEMENT_THRESHOLD_SQ = 0.005;

	public SaprovittaVestigiumItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.translatable("tooltip.hemomancy.saprovitta_vestigium.feeding_wake").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.saprovitta_vestigium.every_step_leaves_something_behind_the").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.translatable("tooltip.hemomancy.saprovitta_vestigium.movement_leaves_a_brief_damaging_blood").withStyle(ChatFormatting.DARK_GREEN));
	}
}
