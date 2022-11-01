package com.vincenthuto.hemomancy.gui;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.container.CharmGourdMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class RuneMenuProvider implements MenuProvider {

	@Override
	public Component getDisplayName() {
		return Component.literal("PlayerRuneInv");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new CharmGourdMenu(id, playerInventory);
	}
}