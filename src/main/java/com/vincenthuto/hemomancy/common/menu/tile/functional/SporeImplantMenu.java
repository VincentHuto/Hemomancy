package com.vincenthuto.hemomancy.common.menu.tile.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.IScars;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.ItemFungalScar;
import com.vincenthuto.hemomancy.common.menu.slot.SelectiveScarTypeSlot;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SporeImplantMenu extends AbstractContainerMenu {
	public static final int FUNGAL_SLOT_UI = 0;
	public static final int SLOT_COUNT = 1;

	private final Player player;

	public IScars scars;

	public SporeImplantMenu(final int windowId, final Inventory playerInventory) {
		this(windowId, playerInventory.player.level(), playerInventory.player.blockPosition(), playerInventory,
				playerInventory.player);
	}

	public SporeImplantMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory);
	}

	public SporeImplantMenu(int windowId, Level world, BlockPos pos, Inventory playerInventory, Player playerEntity) {
		super(ContainerInit.fungal_implantation.get(), windowId);
		this.player = playerInventory.player;

		this.scars = HemoCapabilityAccess.requireScarState(this.player);

		// Slot layout (scar/fungus slots only):
		// 0        : fungal scar slot (scar cap slot 0) — center
		// 1-4      : scar slots       (scar cap slots 1-4) — surrounding
		this.addSlot(new SelectiveScarTypeSlot(player, ItemFungalScar.class, scars, IScars.FUNGAL_SLOT, 80, 35));

		// Player inventory (27 slots)
		for (int l = 0; l < 3; ++l) {
			for (int j1 = 0; j1 < 9; ++j1) {
				this.addSlot(new Slot(playerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
			}
		}

		// Hotbar (9 slots)
		for (int i1 = 0; i1 < 9; ++i1) {
			this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 142));
		}
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack stackInSlot = slot.getItem();
		ItemStack originalStack = stackInSlot.copy();

		// Slot layout:
		// 0        : fungal scar slot (scar cap slot 0)
		// 1-4      : scar slots       (scar cap slots 1-4)
		// 5-31     : player main inventory (27 slots)
		// 32-40    : hotbar (9 slots)
		final int containerEnd   = SLOT_COUNT;   // first player-inv slot
		final int playerInvStart = SLOT_COUNT;
		final int hotbarStart    = 28;
		final int hotbarEnd      = 36;  // inclusive

		if (index < containerEnd) {
			// Moving FROM a scar slot → player inventory
			// Use explicit slot manipulation so that slot.set(ItemStack.EMPTY)
			// fires onUnequipped while the slot still has its item.
			boolean placed = false;
			// Try hotbar first (prefer hotbar for quick access)
			for (int i = hotbarStart; i <= hotbarEnd; i++) {
				Slot target = this.slots.get(i);
				if (!target.hasItem() && target.mayPlace(stackInSlot)) {
					target.set(stackInSlot.copy());
					slot.set(ItemStack.EMPTY);
					placed = true;
					break;
				}
			}
			// Then try main inventory
			if (!placed) {
				for (int i = playerInvStart; i < hotbarStart; i++) {
					Slot target = this.slots.get(i);
					if (!target.hasItem() && target.mayPlace(stackInSlot)) {
						target.set(stackInSlot.copy());
						slot.set(ItemStack.EMPTY);
						placed = true;
						break;
					}
				}
			}
			if (!placed) {
				return ItemStack.EMPTY;
			}
			return originalStack;
		} else {
			// Moving FROM player inventory / hotbar → scar slots
			boolean moved = false;

			// Fungal scar → fungal slot
			if (!moved && stackInSlot.getItem() instanceof ItemFungalScar) {
				Slot fungalSlot = this.slots.get(FUNGAL_SLOT_UI);
				if (!fungalSlot.hasItem() && fungalSlot.mayPlace(stackInSlot)) {
					fungalSlot.set(stackInSlot.split(1));
					moved = true;
				}
			}
// Nothing matched — swap between hotbar and main inventory
			if (!moved) {
				if (index >= playerInvStart && index < hotbarStart) {
					if (!this.moveItemStackTo(stackInSlot, hotbarStart, hotbarEnd + 1, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= hotbarStart && index <= hotbarEnd) {
					if (!this.moveItemStackTo(stackInSlot, playerInvStart, hotbarStart, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}
			}
		}

		if (stackInSlot.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		return originalStack;
	}

	@Override
	public boolean stillValid(Player p_38974_) {
		return this.player.inventoryMenu.stillValid(p_38974_);
	}

}
