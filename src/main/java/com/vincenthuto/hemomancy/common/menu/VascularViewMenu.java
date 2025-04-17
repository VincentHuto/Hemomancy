package com.vincenthuto.hemomancy.common.menu;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VascularViewMenu extends AbstractContainerMenu {

    private VascularViewMenu(@Nullable MenuType<?> type, int id, Inventory playerInv, ItemStack heldItem) {
        super(type, id);
    }
    public VascularViewMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
        super(ContainerInit.vascular_view.get(), windowId);
    }
    public VascularViewMenu(final int windowId, final Inventory playerInventory) {
        this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
                playerInventory.player);
    }
    public VascularViewMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
        this(windowId, playerInventory);
    }


    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }
}
