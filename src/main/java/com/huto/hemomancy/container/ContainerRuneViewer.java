package com.huto.hemomancy.container;

import com.huto.hemomancy.containers.slot.SlotRuneBinder;
import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.itemhandler.RuneBinderItemHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.getInventory();
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerRuneViewer extends AbstractContainerMenu {
	public ContainerRuneViewer(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level, playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public ContainerRuneViewer(int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		super(type, windowId);

		playerInv = playerInventory;
		ItemStack stack = findRuneBinder(playerEntity);

		if (stack == null || stack.isEmpty()) {
			playerEntity.closeContainer();
			return;
		}

		IItemHandler tmp = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);

		if (tmp instanceof RuneBinderItemHandler) {
			handler = (RuneBinderItemHandler) tmp;
			handler.load();
			slotcount = tmp.getSlots();
			itemKey = stack.getDescriptionId();

			addMySlots(stack);
			addPlayerSlots(playerInv);
		} else
			playerEntity.closeContainer();
	}

	public ContainerRuneViewer(int openType, int windowId, Level world, BlockPos pos, Inventory playerInventory,
			Player playerEntity) {
		this(windowId, world, pos, playerInventory, playerEntity);
	}

	public int slotcount = 0;

	private int slotID;
	public String itemKey = "";
	@SuppressWarnings("rawtypes")
	public static final MenuType type = new MenuType<>(ContainerRuneViewer::new)
			.setRegistryName("rune_bindForSetuper_container");
	private Inventory playerInv;
	public RuneBinderItemHandler handler;

	@Override
	public boolean stillValid(Player playerIn) {
		if (slotID == -106)
			return playerIn.getOffhandItem().getItem() instanceof ItemRuneBinder;
		return playerIn.inventory.getItem(slotID).getItem() instanceof ItemRuneBinder;
	}

	@Override
	public ItemStack clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
		if (slot >= 0) {
			if (getSlot(slot).getItem().getItem() instanceof ItemRuneBinder)
				return ItemStack.EMPTY;
		}
		if (clickTypeIn == ClickType.SWAP)
			return ItemStack.EMPTY;

		if (slot >= 0)
			getSlot(slot).container.setChanged();
		return super.clicked(slot, dragType, clickTypeIn, player);
	}

	private void addPlayerSlots(Inventory playerInventory) {
		int originX = 0;
		int originY = 0;
		switch (slotcount) {
		case 18:
			originX = 7;
			originY = 67;
			break;
		case 27:
			originX = 7;
			originY = 85;
			break;
		default:
			originX = 25;
			originY = 193;
		}

		// Player Inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				int x = originX + col * 18;
				int y = originY + row * 18;
				int index = (col + row * 9) + 9;
				this.addSlot(new Slot(playerInventory, index, x + 1, y + 1));
			}
		}

		// Hotbar
		for (int col = 0; col < 9; col++) {
			int x = originX + col * 18;
			int y = originY + 58;
			this.addSlot(new Slot(playerInventory, col, x + 1, y + 1));
		}
	}

	private void addMySlots(ItemStack stack) {
		if (handler == null)
			return;

		int cols = slotcount % 9 == 0 ? 9 : 10;
		int rows = slotcount / cols;
		int slotindex = 0;

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				int x = 7 + col * 18;
				int y = 17 + row * 18;

				this.addSlot(new SlotRuneBinder(handler, slotindex, x + 1, y + 1));
				slotindex++;
				if (slotindex >= slotcount)
					break;
			}
		}

	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			int bagslotcount = slots.size() - playerIn.inventory.items.size();
			ItemStack itemstack1 = slot.getItem();
			if (itemstack1.getCount() < 1) {
				itemstack = itemstack1.copy();
				if (index < bagslotcount) {
					if (!this.moveItemStackTo(itemstack1, bagslotcount, this.slots.size(), true))
						return ItemStack.EMPTY;
				} else if (!this.moveItemStackTo(itemstack1, 0, bagslotcount, false)) {
					return ItemStack.EMPTY;
				}
				if (itemstack1.isEmpty())
					slot.set(ItemStack.EMPTY);
				else
					slot.setChanged();
			}
		}
		return itemstack;
	}

	private ItemStack findRuneBinder(Player playerEntity) {
		Inventory inv = playerEntity.inventory;

		if (playerEntity.getMainHandItem().getItem() instanceof ItemRuneBinder) {
			for (int i = 0; i <= 35; i++) {
				ItemStack stack = inv.getItem(i);
				if (stack == playerEntity.getMainHandItem()) {
					slotID = i;
					return stack;
				}
			}
		} else if (playerEntity.getOffhandItem().getItem() instanceof ItemRuneBinder) {
			slotID = -106;
			return playerEntity.getOffhandItem();
		} else {
			for (int i = 0; i <= 35; i++) {
				ItemStack stack = inv.getItem(i);
				if (stack.getItem() instanceof ItemRuneBinder) {
					slotID = i;
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}