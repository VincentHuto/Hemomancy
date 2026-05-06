package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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

	public SaprovittaVestigiumItem(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
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
		tooltip.add(Component.literal("Feeding Wake").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("Every step leaves something behind. The ground remembers where you've been.").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal("✦ Movement leaves a brief damaging blood-fungal trail").withStyle(ChatFormatting.DARK_GREEN));
	}
}
