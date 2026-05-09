package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PuppeteersSpindleMenu extends AbstractContainerMenu {

	public PuppeteersSpindleMenu(int windowId, Inventory playerInventory) {
		super(ContainerInit.puppeteers_spindle.get(), windowId);
	}

	public PuppeteersSpindleMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
