package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CentrifugeOutputSlot extends OutputSlot {
	public CentrifugeOutputSlot(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public boolean mayPickup(Player player) {
		return !(container instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity te)
				|| !te.isRiteLocked();
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		if (player instanceof ServerPlayer serverPlayer) {
			FirstSeparationAssignment.tryRecoverAssignmentOutput(serverPlayer, stack);
			if (container instanceof com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity te)
				te.onPlayerExtract(serverPlayer, getContainerSlot(), stack);
		}
		super.onTake(player, stack);
	}
}
