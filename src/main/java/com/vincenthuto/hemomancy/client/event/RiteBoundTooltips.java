package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.zone.RiteBound;
import com.vincenthuto.hemomancy.common.rite.zone.RiteBoundRules;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Names the rite space a rite-bound item or block works in. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RiteBoundTooltips {
	private RiteBoundTooltips() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		RiteBoundRules.Requirement requirement = RiteBound.of(event.getItemStack());
		if (!requirement.bound()) return;
		event.getToolTip().add(Component.translatable("hemomancy.rite_bound.tooltip", RiteBoundRules.describe(requirement))
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
	}
}
