package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.itemhandler.MnemonicFolioItemHandler;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicFolioItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicFolioLayout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class MnemonicFolioMenu extends AbstractContainerMenu {
	private final Inventory playerInventory;
	private final ItemStack folio;
	private final MnemonicFolioItemHandler handler;

	public MnemonicFolioMenu(int id, Inventory inventory, FriendlyByteBuf ignored) {
		this(id, inventory, findFolio(inventory.player));
	}

	public MnemonicFolioMenu(int id, Inventory inventory, ItemStack folio) {
		super(ContainerInit.mnemonic_folio.get(), id);
		this.playerInventory = inventory;
		this.folio = folio;
		var capability = folio.getCapability(Capabilities.ItemHandler.ITEM);
		this.handler = capability instanceof MnemonicFolioItemHandler found ? found : null;
		if (handler == null) {
			inventory.player.closeContainer();
			return;
		}
		handler.loadIfNotLoaded();
		for (int index = 0; index < MnemonicFolioLayout.SLOT_COUNT; index++) {
			MnemonicFolioLayout.Point point = MnemonicFolioLayout.folioSlot(index);
			addSlot(new SlotItemHandler(handler, index, point.x() + 1, point.y() + 1));
		}
		for (int index = 0; index < 27; index++) {
			MnemonicFolioLayout.Point point = MnemonicFolioLayout.playerSlot(index);
			addSlot(new Slot(inventory, index + 9, point.x() + 1, point.y() + 1));
		}
		for (int index = 0; index < 9; index++) {
			MnemonicFolioLayout.Point point = MnemonicFolioLayout.hotbarSlot(index);
			addSlot(new Slot(inventory, index, point.x() + 1, point.y() + 1));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return handler != null && containsExactFolio(player);
	}

	@Override
	public void clicked(int slotId, int button, ClickType clickType, Player player) {
		if (wouldMoveOpenFolio(slotId, button, clickType)) return;
		super.clicked(slotId, button, clickType, player);
		if (handler != null) handler.save();
	}

	private boolean wouldMoveOpenFolio(int slotId, int button, ClickType clickType) {
		if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem() == folio) return true;
		if (clickType != ClickType.SWAP) return false;
		if (button == 40) return playerInventory.player.getOffhandItem() == folio;
		return button >= 0 && button < 9 && playerInventory.getItem(button) == folio;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		if (handler == null || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (!slot.hasItem() || slot.getItem() == folio) return ItemStack.EMPTY;
		ItemStack original = slot.getItem();
		ItemStack copy = original.copy();
		if (index < MnemonicFolioLayout.SLOT_COUNT) {
			if (!moveItemStackTo(original, MnemonicFolioLayout.SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(original, 0, MnemonicFolioLayout.SLOT_COUNT, false)) {
			return ItemStack.EMPTY;
		}
		if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
		handler.save();
		return copy;
	}

	@Override
	public void removed(Player player) {
		if (handler != null) handler.save();
		super.removed(player);
	}

	private boolean containsExactFolio(Player player) {
		return player.getOffhandItem() == folio || player.getInventory().items.stream().anyMatch(stack -> stack == folio);
	}

	private static ItemStack findFolio(Player player) {
		if (player.getMainHandItem().getItem() instanceof MnemonicFolioItem) return player.getMainHandItem();
		if (player.getOffhandItem().getItem() instanceof MnemonicFolioItem) return player.getOffhandItem();
		return player.getInventory().items.stream()
				.filter(stack -> stack.getItem() instanceof MnemonicFolioItem).findFirst().orElse(ItemStack.EMPTY);
	}
}
