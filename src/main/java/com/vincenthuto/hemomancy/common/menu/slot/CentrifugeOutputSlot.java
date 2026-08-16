package com.vincenthuto.hemomancy.common.menu.slot;

import com.vincenthuto.hemomancy.common.mission.FirstSeparationAssignmentHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CentrifugeOutputSlot extends OutputSlot {
	public CentrifugeOutputSlot(Container inventory, int index, int x, int y) {
		super(inventory, index, x, y);
	}

	@Override
	public void onTake(Player player, ItemStack stack) {
		if (player instanceof ServerPlayer serverPlayer) {
			FirstSeparationAssignmentHelper.tryRecoverAssignmentOutput(serverPlayer, stack);
		}
		super.onTake(player, stack);
	}
}
