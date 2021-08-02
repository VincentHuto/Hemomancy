package com.huto.hemomancy.gui.mindrunes;

import javax.annotation.Nullable;

import com.huto.hemomancy.container.PlayerExpandedContainer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class GuiProvider implements MenuProvider {

	@Override
	public Component getDisplayName() {
		return new TextComponent("PlayerRuneInv");
	}

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new PlayerExpandedContainer(id, playerInventory, !playerEntity.world.isRemote);
	}
}
