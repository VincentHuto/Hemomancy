package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class RhizovittaCommunisItem extends ItemFungalScar {

	public RhizovittaCommunisItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("Roots into fungal ground and returns a little spent blood to the circle.")
				.withStyle(ChatFormatting.ITALIC));
	}
}
