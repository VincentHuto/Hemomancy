package com.vincenthuto.hemomancy.common.menu;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RuneMenuProvider implements MenuProvider {

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new CharmGourdMenu(id, playerInventory);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("PlayerRuneInv");
	}
}