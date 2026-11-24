package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.menu.HemoContainerMenu;
import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.slot.CentrifugeOutputSlot;
import com.vincenthuto.hemomancy.common.menu.slot.CentrifugeSlot;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import com.vincenthuto.hutoslib.common.container.SlotSelectiveType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class VialCentrifugeMenu extends HemoContainerMenu {

	// Slot indices
	public static final int BLOOD_SLOT = 0;
	public static final int VIAL_START = 1;
	public static final int VIAL_END = 8;
	public static final int OUTPUT_START = 9;
	public static final int OUTPUT_END = 16;
	public static final int AUX_OUTPUT_SLOT = 17;
	public static final int FLASK_OUTPUT_SLOT = 18;
	public static final int SLOT_COUNT = 19;
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

		// SLOTS — laid out as the machine: reservoir jar on the left, swing-bucket rotor in the splash bowl,
		// fraction receivers on the right and the powder tray under the bowl (VialCentrifugeScreen draws them).
		// Bloody flask pours in above the reservoir jar
		addSlot(new SlotSelectiveType(te, BloodyFlaskItem.class,
				VialCentrifugeBlockEntity.SLOT_BLOOD, 16, 12, 16) {
			@Override public boolean mayPlace(ItemStack stack) { return !te.isRiteLocked() && super.mayPlace(stack); }
			@Override public boolean mayPickup(Player player) { return !te.isRiteLocked(); }
		});

		// Buckets on the rotor: radius 28 around (80, 54), clockwise from the top, so each balanced pair
		// (2,6) (3,7) (4,8) (5,9) sits opposite itself as on the model (VialCentrifugeRotorRules.slotAngle)
		addSlot(new CentrifugeSlot(te, 2, 72, 18));   // top
		addSlot(new CentrifugeSlot(te, 3, 92, 26));   // top-right
		addSlot(new CentrifugeSlot(te, 4, 100, 46));  // right
		addSlot(new CentrifugeSlot(te, 5, 92, 66));   // bottom-right
		addSlot(new CentrifugeSlot(te, 6, 72, 74));   // bottom
		addSlot(new CentrifugeSlot(te, 7, 52, 66));   // bottom-left
		addSlot(new CentrifugeSlot(te, 8, 44, 46));   // left
		addSlot(new CentrifugeSlot(te, 9, 52, 26));   // top-left

		// Fraction receivers: a 2x4 rack fed by the bowl's spout
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 2; col++)
				addSlot(new CentrifugeOutputSlot(te, 10 + row * 2 + col, 130 + col * 18, 22 + row * 18));

		// Iron powder settles into the tray under the bowl
		addSlot(new CentrifugeOutputSlot(te, 18, 72, 100));

		// The emptied flask drops out under the reservoir jar
		addSlot(new CentrifugeOutputSlot(te, VialCentrifugeBlockEntity.SLOT_FLASK_OUTPUT, 12, 98));

		// INVENTORY
		addPlayerInventory(playerInv, 8, 134);

		this.addDataSlots(containerData);
	}

	@Override
	public void broadcastChanges() {
		te.sendUpdates();
		super.broadcastChanges();
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		if (te.isRiteLocked()) return;
		super.clicked(slotId, dragType, clickTypeIn, player);
		te.sendUpdates();
	}

	/** Progress scaled 0–24 for rendering (0 = idle, 24 = complete) */
	public int getSpinProgress() {
		int remaining = this.data.get(0);
		int total = this.data.get(1);
		if (remaining <= 0 || total <= 0) return 0;
		return (total - remaining) * 24 / total;
	}

	/** Whether the centrifuge is currently spinning */
	public boolean isSpinning() {
		return this.data.get(0) > 0;
	}

	public VialCentrifugeStartupResult getStartupResult() {
		return VialCentrifugeStartupResult.byId(this.data.get(2));
	}

	public VialCentrifugeBlockEntity getTe() {
		return this.te;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		if (te.isRiteLocked() || index < 0 || index >= slots.size()) return ItemStack.EMPTY;
		ItemStack stack = ItemStack.EMPTY;
		te.sendUpdates();
		if (te.isProcessing() && index >= VIAL_START && index <= VIAL_END) {
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
			if (index < SLOT_COUNT) slot.onTake(playerIn, stack.copyWithCount(stack.getCount() - itemStack.getCount()));
		}
		return stack;
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
