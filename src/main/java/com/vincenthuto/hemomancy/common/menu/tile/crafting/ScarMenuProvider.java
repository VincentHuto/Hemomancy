package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.menu.HarbingerEquipmentMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import javax.annotation.Nullable;

public class ScarMenuProvider implements MenuProvider {

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player playerEntity) {
		return new HarbingerEquipmentMenu(id, playerInventory);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("PlayerScarInv");
	}
}