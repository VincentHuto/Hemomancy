package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ScryingDiagnosticsMenu extends ViewOnlyMenu {
	public ScryingDiagnosticsMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		super(ContainerInit.scrying_diagnostics.get(), windowId);
	}

	public ScryingDiagnosticsMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public ScryingDiagnosticsMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}
}
