package com.vincenthuto.hemomancy.common.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

/** Shared deterministic perimeter layout for recipe-defined offering braziers. */
public final class BloodStructureOfferingPlacement {
	private BloodStructureOfferingPlacement() {
	}

	public static List<OfferingSlot> plan(BlockPos center, int halfWidth, int halfDepth, int gap,
			List<BloodStructureOffering> offerings) {
		List<OfferingSlot> result = new ArrayList<>();
		int index = 0;
		for (BloodStructureOffering offering : offerings) {
			ItemStack[] accepted = offering.ingredient().getItems();
			if (accepted.length == 0) throw new IllegalArgumentException("Offering ingredient has no concrete item");
			for (int count = 0; count < offering.count(); count++) {
				int ring = index / 4;
				int side = index % 4;
				int xDistance = halfWidth + gap + ring;
				int zDistance = halfDepth + gap + ring;
				BlockPos pos = switch (side) {
					case 0 -> center.offset(-xDistance, 0, 0);
					case 1 -> center.offset(xDistance, 0, 0);
					case 2 -> center.offset(0, 0, -zDistance);
					default -> center.offset(0, 0, zDistance);
				};
				result.add(new OfferingSlot(pos, offering, accepted[0].copyWithCount(1)));
				index++;
			}
		}
		return List.copyOf(result);
	}

	public record OfferingSlot(BlockPos pos, BloodStructureOffering offering, ItemStack representativeStack) {
	}
}
