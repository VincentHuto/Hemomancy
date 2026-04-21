package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.slot.CentrifugeSlot;
import com.vincenthuto.hemomancy.common.menu.slot.OutputSlot;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hutoslib.common.container.SlotSelectiveType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VialCentrifugeMenu extends AbstractContainerMenu {

	// Slot indices
	public static final int INPUT_SLOT = 0;
	public static final int BLOOD_SLOT = 1;
	public static final int VIAL_START = 2;
	public static final int VIAL_END = 9;
	public static final int OUTPUT_START = 10;
	public static final int OUTPUT_END = 17;
	public static final int AUX_OUTPUT_SLOT = 18;
	public static final int FLASK_OUTPUT_SLOT = 19;
	public static final int SLOT_COUNT = 20;
	public static final int DATA_COUNT = 3;

	private static final int INV_START = SLOT_COUNT;
	private static final int INV_END = INV_START + 27;
	private static final int HOTBAR_END = INV_END + 9;

	private static VialCentrifugeBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof VialCentrifugeBlockEntity) {
			return (VialCentrifugeBlockEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	private final VialCentrifugeBlockEntity te;
	private final ContainerData data;

	public VialCentrifugeMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public VialCentrifugeMenu(int windowIdIn, Inventory playerInventoryIn,
			VialCentrifugeBlockEntity blockEntity) {
		this(windowIdIn, playerInventoryIn, blockEntity, new SimpleContainerData(DATA_COUNT));
	}

	public VialCentrifugeMenu(final int windowId, final Inventory playerInv, final VialCentrifugeBlockEntity te,
			final ContainerData containerData) {
		super(ContainerInit.vial_centrifuge.get(), windowId);
		this.te = te;
		this.data = containerData;

		// SLOTS — positioned for programmatic UI layout
		// Input slot (above blood bar, left side)
		addSlot(new Slot(te, INPUT_SLOT, 8, 14));
		// Blood flask slot (bottom-left, below blood bar)
		addSlot(new SlotSelectiveType(te, BloodyFlaskItem.class, BLOOD_SLOT, 16, 8, 90));

		// Vial slots — ring arrangement centered around (77, 50)
		// Radius 28, 8 slots at 45° increments (clockwise from top)
		// Slot coords are center minus 8 to get top-left corner
		addSlot(new CentrifugeSlot(te, 2, 69, 14));   // top
		addSlot(new CentrifugeSlot(te, 3, 89, 22));   // top-right
		addSlot(new CentrifugeSlot(te, 4, 97, 42));   // right
		addSlot(new CentrifugeSlot(te, 5, 89, 62));   // bottom-right
		addSlot(new CentrifugeSlot(te, 6, 69, 70));   // bottom
		addSlot(new CentrifugeSlot(te, 7, 49, 62));   // bottom-left
		addSlot(new CentrifugeSlot(te, 8, 41, 42));   // left
		addSlot(new CentrifugeSlot(te, 9, 49, 22));   // top-left

		// Output slots (2x4 grid on the right, 18px spacing)
		addSlot(new OutputSlot(te, 10, 128, 14));
		addSlot(new OutputSlot(te, 11, 146, 14));
		addSlot(new OutputSlot(te, 12, 128, 32));
		addSlot(new OutputSlot(te, 13, 146, 32));
		addSlot(new OutputSlot(te, 14, 128, 50));
		addSlot(new OutputSlot(te, 15, 146, 50));
		addSlot(new OutputSlot(te, 16, 128, 68));
		addSlot(new OutputSlot(te, 17, 146, 68));

		// Aux output (below output grid, within craft area)
		addSlot(new OutputSlot(te, AUX_OUTPUT_SLOT, 137, 88));

		// Flask output (next to blood input slot — empty flasks from consumed bloody flasks)
		addSlot(new OutputSlot(te, FLASK_OUTPUT_SLOT, 26, 90));

		// INVENTORY
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 122 + y * 18));
			}
		}
		// HOTBAR
		for (int x = 0; x < 9; x++) {
			this.addSlot(new Slot(playerInv, x, 8 + x * 18, 180));
		}

		this.addDataSlots(containerData);
	}

	@Override
	public void broadcastChanges() {
		te.sendUpdates();
		super.broadcastChanges();
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, dragType, clickTypeIn, player);
		te.sendUpdates();
	}

	/** Progress scaled 0–24 for rendering (0 = idle, 24 = complete) */
	public int getSpinProgress() {
		int remaining = this.data.get(0);
		int total = VialCentrifugeBlockEntity.SPIN_TOTAL_TIME;
		if (remaining <= 0) return 0;
		return (total - remaining) * 24 / total;
	}

	/** Whether the centrifuge is currently spinning */
	public boolean isSpinning() {
		return this.data.get(0) > 0;
	}

	public VialCentrifugeBlockEntity getTe() {
		return this.te;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		te.sendUpdates();
		if (te.isSpinning() && index >= VIAL_START && index <= VIAL_END) {
			return ItemStack.EMPTY;
		}
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack itemStack = slot.getItem();
			stack = itemStack.copy();

			// From container slots → player inventory
			if (index < SLOT_COUNT) {
				if (!this.moveItemStackTo(itemStack, INV_START, HOTBAR_END, true)) {
					return ItemStack.EMPTY;
				}
			}
			// From player inventory → container slots
			else {
				if (!this.moveItemStackTo(itemStack, 0, SLOT_COUNT, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return stack;
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
	}

	@Override
	public void setItem(int p_182407_, int p_182408_, ItemStack p_182409_) {
		super.setItem(p_182407_, p_182408_, p_182409_);
		te.sendUpdates();
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return this.te.stillValid(playerIn);
	}

}
