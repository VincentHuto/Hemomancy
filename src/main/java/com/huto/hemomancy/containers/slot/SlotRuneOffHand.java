package com.huto.hemomancy.containers.slot;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRuneOffHand extends Slot {

	public SlotRuneOffHand(Container inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return super.isItemValid(stack);
	}

	@Nullable
	@Override
	public Pair<ResourceLocation, ResourceLocation> getBackground() { // getSlotTexture
		return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
	}
}
