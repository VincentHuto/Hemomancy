package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.EnzymeItem;
import com.vincenthuto.hemomancy.common.item.RecycledEnzymeItem;
import com.vincenthuto.hemomancy.common.item.morphlings.IMorphling;
import com.vincenthuto.hemomancy.common.item.morphlings.ItemMorphlingPolyp;
import com.vincenthuto.hemomancy.common.item.tool.BloodGourdItem;
import com.vincenthuto.hemomancy.common.menu.slot.IncubatorCatalystSlot;
import com.vincenthuto.hemomancy.common.menu.slot.IncubatorCenterSlot;
import com.vincenthuto.hemomancy.common.tile.crafting.MorphlingIncubatorBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MorphlingIncubatorMenu extends AbstractContainerMenu {

	// Slot indices
	public static final int CENTER_SLOT = 0;      // Polyp or Morphling input
	public static final int CATALYST_SLOT_1 = 1;  // Catalyst / Enzyme
	public static final int CATALYST_SLOT_2 = 2;
	public static final int CATALYST_SLOT_3 = 3;
	public static final int CATALYST_SLOT_4 = 4;
	public static final int OUTPUT_SLOT = 5;       // Result output
	public static final int BLOOD_SLOT = 6;        // Blood flask / gourd slot
	public static final int FLASK_OUTPUT_SLOT = 7;  // Empty flask output
	public static final int SLOT_COUNT = 8;
	public static final int DATA_COUNT = 3;        // progress, totalTime, mode

	private static MorphlingIncubatorBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof MorphlingIncubatorBlockEntity) {
			return (MorphlingIncubatorBlockEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	private final MorphlingIncubatorBlockEntity te;
	private final ContainerData dataAccess;
	protected final Level level;

	public MorphlingIncubatorMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public MorphlingIncubatorMenu(int windowId, Inventory playerInventory, MorphlingIncubatorBlockEntity te) {
		this(windowId, playerInventory, te, new SimpleContainerData(DATA_COUNT));
	}

	public MorphlingIncubatorMenu(final int windowId, final Inventory playerInventory,
			final MorphlingIncubatorBlockEntity te, final ContainerData containerData) {
		super(ContainerInit.morphling_incubator.get(), windowId);
		this.te = te;
		checkContainerSize(te, SLOT_COUNT);
		checkContainerDataCount(containerData, DATA_COUNT);
		this.dataAccess = containerData;
		this.level = playerInventory.player.level();

		// Center input slot (polyp or morphling) — center of the GUI
		this.addSlot(new IncubatorCenterSlot(te, CENTER_SLOT, 80, 40));

		// 4 catalyst/enzyme slots around the center — spaced out to clear the progress ring
		this.addSlot(new IncubatorCatalystSlot(te, CATALYST_SLOT_1, 80, 6));     // Top
		this.addSlot(new IncubatorCatalystSlot(te, CATALYST_SLOT_2, 114, 40));   // Right
		this.addSlot(new IncubatorCatalystSlot(te, CATALYST_SLOT_3, 80, 74));    // Bottom
		this.addSlot(new IncubatorCatalystSlot(te, CATALYST_SLOT_4, 46, 40));    // Left

		// Output slot
		this.addSlot(new FurnaceResultSlot(playerInventory.player, te, OUTPUT_SLOT, 148, 40));

		// Blood flask slot
		this.addSlot(new Slot(te, BLOOD_SLOT, 8, 74) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof BloodyFlaskItem || stack.getItem() instanceof BloodGourdItem;
			}
		});

		// Flask output slot (empty flasks from consumed bloody flasks)
		this.addSlot(new Slot(te, FLASK_OUTPUT_SLOT, 26, 74) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}
		});

		// Player inventory
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
			}
		}
		// Player hotbar
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 162));
		}

		this.addDataSlots(containerData);
	}

	@Override
	public void broadcastChanges() {
		te.sendUpdates();
		super.broadcastChanges();
	}

	public int getCraftProgress() {
		int progress = this.dataAccess.get(0);
		int total = this.dataAccess.get(1);
		return total != 0 && progress != 0 ? progress * 24 / total : 0;
	}

	public boolean isCrafting() {
		return this.dataAccess.get(0) > 0;
	}

	/**
	 * 0 = idle, 1 = polyp crafting, 2 = enzyme feeding
	 */
	public int getMode() {
		return this.dataAccess.get(2);
	}

	public MorphlingIncubatorBlockEntity getTe() {
		return te;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return itemstack;
		}

		ItemStack slotStack = slot.getItem();
		itemstack = slotStack.copy();

		// ── From block slots → player inventory ──
		if (index < SLOT_COUNT) {
			if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, true)) {
				return ItemStack.EMPTY;
			}
			if (index == OUTPUT_SLOT) {
				slot.onQuickCraft(slotStack, itemstack);
			}
		}
		// ── From player inventory/hotbar → block slots ──
		else {
			// Polyps → center slot
			if (slotStack.getItem() instanceof ItemMorphlingPolyp) {
				if (!this.moveItemStackTo(slotStack, CENTER_SLOT, CENTER_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Formed morphlings → center slot
			else if (slotStack.getItem() instanceof IMorphling) {
				if (!this.moveItemStackTo(slotStack, CENTER_SLOT, CENTER_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Blood flasks / gourds → blood slot
			else if (slotStack.getItem() instanceof BloodyFlaskItem || slotStack.getItem() instanceof BloodGourdItem) {
				if (!this.moveItemStackTo(slotStack, BLOOD_SLOT, BLOOD_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Enzymes → catalyst/enzyme slots
			else if (slotStack.getItem() instanceof EnzymeItem || slotStack.getItem() instanceof RecycledEnzymeItem) {
				if (!this.moveItemStackTo(slotStack, CATALYST_SLOT_1, CATALYST_SLOT_4 + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Everything else (recipe catalysts) → catalyst slots
			else {
				if (!this.moveItemStackTo(slotStack, CATALYST_SLOT_1, CATALYST_SLOT_4 + 1, false)) {
					// If catalyst slots are full, try inventory ↔ hotbar
					if (index < SLOT_COUNT + 27) {
						// Main inventory → hotbar
						if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false)) {
							return ItemStack.EMPTY;
						}
					} else {
						// Hotbar → main inventory
						if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false)) {
							return ItemStack.EMPTY;
						}
					}
				}
			}
		}

		if (slotStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (slotStack.getCount() == itemstack.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, slotStack);
		return itemstack;
	}

	@Override
	public boolean stillValid(Player player) {
		return this.te.stillValid(player);
	}
}
