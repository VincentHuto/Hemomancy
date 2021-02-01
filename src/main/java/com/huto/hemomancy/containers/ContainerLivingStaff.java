package com.huto.hemomancy.containers;

import com.huto.hemomancy.containers.slots.SlotMorphlingJar;
import com.huto.hemomancy.item.tool.ItemLivingStaff;
import com.huto.hemomancy.itemhandler.LivingStaffItemHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ContainerLivingStaff extends Container {
	public ContainerLivingStaff(final int windowId, final PlayerInventory playerInventory) {
		this(windowId, playerInventory.player.world, playerInventory.player.getPosition(), playerInventory,
				playerInventory.player);
	}

	public ContainerLivingStaff(int windowId, World world, BlockPos pos, PlayerInventory playerInventory,
			PlayerEntity playerEntity) {
		super(type, windowId);

		playerInv = playerInventory;
		ItemStack stack = findLivingStaff(playerEntity);

		if (stack == null || stack.isEmpty()) {
			playerEntity.closeScreen();
			return;
		}

		IItemHandler tmp = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);

		if (tmp instanceof LivingStaffItemHandler) {
			handler = (LivingStaffItemHandler) tmp;
			handler.load();
			slotcount = tmp.getSlots();
			itemKey = stack.getTranslationKey();

			addMySlots(stack);
			addPlayerSlots(playerInv);
		} else
			playerEntity.closeScreen();
	}

	public ContainerLivingStaff(int openType, int windowId, World world, BlockPos pos, PlayerInventory playerInventory,
			PlayerEntity playerEntity) {
		this(windowId, world, pos, playerInventory, playerEntity);
	}

	public int slotcount = 0;
	private int slotID;
	public String itemKey = "";
	@SuppressWarnings("rawtypes")
	public static final ContainerType type = new ContainerType<>(ContainerLivingStaff::new)
			.setRegistryName("living_staff_container");
	private PlayerInventory playerInv;
	public LivingStaffItemHandler handler;

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		if (slotID == -106)
			return playerIn.getHeldItemOffhand().getItem() instanceof ItemLivingStaff;
		return playerIn.inventory.getStackInSlot(slotID).getItem() instanceof ItemLivingStaff;
	}

	@Override
	public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		if (slot >= 0) {
			if (getSlot(slot).getStack().getItem() instanceof ItemLivingStaff)
				return ItemStack.EMPTY;
		}
		if (clickTypeIn == ClickType.SWAP)
			return ItemStack.EMPTY;

		if (slot >= 0)
			getSlot(slot).inventory.markDirty();
		return super.slotClick(slot, dragType, clickTypeIn, player);
	}

	private void addPlayerSlots(PlayerInventory playerInventory) {
		int originX = 0;
		int originY = 0;
		switch (slotcount) {
		case 1:
			originX = 7;
			originY = 145;
			break;
		case 16:
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

		int cols = slotcount % 1 == 0 ? 1 : 10;
		int rows = slotcount / cols;
		int slotindex = 0;

		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				int x = 70 + col * 18;
				int y = 95 + row * 18;

				this.addSlot(new SlotMorphlingJar(handler, slotindex, x + 1, y + 1));
				slotindex++;
				if (slotindex >= slotcount)
					break;
			}
		}

	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			int bagslotcount = inventorySlots.size() - playerIn.inventory.mainInventory.size();
			ItemStack itemstack1 = slot.getStack();
			if (itemstack1.getCount() < 1) {
				itemstack = itemstack1.copy();
				if (index < bagslotcount) {
					if (!this.mergeItemStack(itemstack1, bagslotcount, this.inventorySlots.size(), true))
						return ItemStack.EMPTY;
				} else if (!this.mergeItemStack(itemstack1, 0, bagslotcount, false)) {
					return ItemStack.EMPTY;
				}
				if (itemstack1.isEmpty())
					slot.putStack(ItemStack.EMPTY);
				else
					slot.onSlotChanged();
			}
		}
		return itemstack;
	}

	private ItemStack findLivingStaff(PlayerEntity playerEntity) {
		PlayerInventory inv = playerEntity.inventory;

		if (playerEntity.getHeldItemMainhand().getItem() instanceof ItemLivingStaff) {
			for (int i = 0; i <= 35; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack == playerEntity.getHeldItemMainhand()) {
					slotID = i;
					return stack;
				}
			}
		} else if (playerEntity.getHeldItemOffhand().getItem() instanceof ItemLivingStaff) {
			slotID = -106;
			return playerEntity.getHeldItemOffhand();
		} else {
			for (int i = 0; i <= 35; i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack.getItem() instanceof ItemLivingStaff) {
					slotID = i;
					return stack;
				}
			}
		}
		return ItemStack.EMPTY;
	}
}