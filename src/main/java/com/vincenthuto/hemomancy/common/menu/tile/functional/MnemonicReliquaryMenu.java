package com.vincenthuto.hemomancy.common.menu.tile.functional;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.tile.functional.MnemonicReliquaryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class MnemonicReliquaryMenu extends AbstractContainerMenu {

	private final Level level;
	private final BlockPos blockPos;

	private MnemonicReliquaryMenu(@Nullable MenuType<?> type, int id, Inventory playerInv, ItemStack heldItem) {
		super(type, id);
		this.level = playerInv.player.level();
		this.blockPos = playerInv.player.blockPosition();
	}

	public MnemonicReliquaryMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		super(ContainerInit.mnemonic_reliquary.get(), windowId);
		this.level = world;
		this.blockPos = pos;
	}

	public MnemonicReliquaryMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public MnemonicReliquaryMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory.player.level(), data.readBlockPos(), playerInventory, playerInventory.player);
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return true;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if (!level.isClientSide) {
			BlockEntity be = level.getBlockEntity(blockPos);
			if (be instanceof MnemonicReliquaryBlockEntity reliquary) {
				reliquary.stopOpen();
			}
		}
	}

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY;
	}
}
