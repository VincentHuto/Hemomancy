package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.init.ContainerInit;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class StructureSpawnerMenu extends ViewOnlyMenu {

	public StructureSpawnerMenu(int windowId, Inventory playerInventory) {
		super(ContainerInit.structure_spawner.get(), windowId);
	}

	public StructureSpawnerMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}
}
