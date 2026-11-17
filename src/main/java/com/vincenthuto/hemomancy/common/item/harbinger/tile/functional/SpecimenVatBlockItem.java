package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * The Specimen Vat as an item (core-loop Track 5). A vat picked up with its creature carries it, with its Vigor, and
 * stacks no further. A creature goes in by being led to a placed vat, not by the item.
 */
public class SpecimenVatBlockItem extends BlockItem {
	public SpecimenVatBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return SpecimenJarData.hasSpecimen(stack) ? 1 : super.getMaxStackSize(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		SpecimenJarBlockItem.appendKept(stack, tooltip);
		if (!SpecimenJarData.hasSpecimen(stack)) {
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_vat.lead_in"));
			tooltip.add(Component.translatable("tooltip.hemomancy.specimen_vat.joins"));
		}
	}
}
