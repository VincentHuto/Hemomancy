package com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class TalaromycesMinusItem extends ItemFungalScar {

	public TalaromycesMinusItem(Properties properties, DeferredHolder<ScarDefinition, ScarDefinition> scarDefinition) {
		super(properties, scarDefinition);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
			TooltipFlag flagIn) {
		super.appendHoverText(stack, context, tooltip, flagIn);
		tooltip.add(Component.literal("A quiet decomposer. Shift-mining ore lets its hyphae follow the vein.")
				.withStyle(ChatFormatting.ITALIC));
	}
}
