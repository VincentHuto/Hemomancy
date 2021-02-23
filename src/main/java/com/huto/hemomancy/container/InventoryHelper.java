package com.huto.hemomancy.container;

import javax.annotation.Nullable;

import com.huto.hemomancy.tile.TileSimpleInventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class InventoryHelper {

	@Nullable
	public static IItemHandler getInventory(World world, BlockPos pos, Direction side) {
		TileEntity te = world.getTileEntity(pos);

		if (te == null) {
			return null;
		}

		LazyOptional<IItemHandler> ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
		if (!ret.isPresent()) {
			ret = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		}
		return ret.orElse(null);
	}

	public static void withdrawFromInventory(TileSimpleInventory inv, PlayerEntity player) {
		for (int i = inv.inventorySize() - 1; i >= 0; i--) {
			ItemStack stackAt = inv.getItemHandler().getStackInSlot(i);
			if (!stackAt.isEmpty()) {
				ItemStack copy = stackAt.copy();
				player.inventory.placeItemBackInInventory(player.world, copy);
				inv.getItemHandler().setInventorySlotContents(i, ItemStack.EMPTY);
				break;
			}
		}
	}
}
