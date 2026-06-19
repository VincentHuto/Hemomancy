package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class AnastocordycepsNexusItem extends ItemFungalScar {

	/** Fraction of damage echoed to each other tethered enemy. */
	public static final float ECHO_FRACTION = 0.20f;

	/** Tether duration in ticks (6 seconds). */
	public static final long TETHER_DURATION_TICKS = 120L;

	/** Maximum number of enemies tethered per strike. */
	public static final int MAX_TETHER_TARGETS = 4;

	/** Radius in blocks within which nearby enemies are also tethered on strike. */
	public static final double TETHER_RADIUS = 6.0;

	public AnastocordycepsNexusItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
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
		tooltip.add(Component.literal("Latching Vein").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
		tooltip.add(Component.literal("The mycelium threads between them. Hurt one — the others feel it.").withStyle(ChatFormatting.ITALIC));
		tooltip.add(Component.literal("✦ Striking an enemy tethers nearby foes for 6s").withStyle(ChatFormatting.DARK_GREEN));
		tooltip.add(Component.literal("✦ Tethered foes share 20% of damage taken").withStyle(ChatFormatting.DARK_GREEN));
	}
}
