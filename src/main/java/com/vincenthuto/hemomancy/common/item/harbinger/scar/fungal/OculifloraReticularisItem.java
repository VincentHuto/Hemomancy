package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class OculifloraReticularisItem extends ItemFungalScar {

	public OculifloraReticularisItem(Properties properties,
			DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	public static boolean networkSightActive(Player player) {
		return player != null && HemoCapabilityAccess.getScarState(player)
				.map(scars -> scars.getFungalScar().is(ItemInit.oculiflora_reticularis.get()))
				.orElse(false);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Opens a local sightline into blooms, blood-marked forms, and morphic nectar.")
				.withStyle(ChatFormatting.ITALIC));
	}
}
