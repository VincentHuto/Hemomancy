package com.vincenthuto.hemomancy.common.menu;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hemomancy.common.tile.crafting.PuppeteersSpindleBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

public class PuppeteersSpindleMenu extends AbstractContainerMenu {
	public static final int CROSSBAR_SLOT = PuppeteersSpindleBlockEntity.SLOT_CROSSBAR;
	public static final int THREAD_SLOT = PuppeteersSpindleBlockEntity.SLOT_THREAD;
	public static final int SLOT_COUNT = PuppeteersSpindleBlockEntity.INVENTORY_SIZE;
	public static final int DATA_COUNT = 1;

	private final PuppeteersSpindleBlockEntity spindle;
	private final ContainerData dataAccess;
	protected final Level level;

	public PuppeteersSpindleMenu(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public PuppeteersSpindleMenu(int windowId, Inventory playerInventory, PuppeteersSpindleBlockEntity spindle) {
		this(windowId, playerInventory, spindle, spindle.dataAccess);
	}

	public PuppeteersSpindleMenu(int windowId, Inventory playerInventory,
			PuppeteersSpindleBlockEntity spindle, ContainerData data) {
		super(ContainerInit.puppeteers_spindle.get(), windowId);
		this.spindle = spindle;
		this.dataAccess = data;
		this.level = playerInventory.player.level();

		checkContainerSize(spindle, SLOT_COUNT);
		checkContainerDataCount(data, DATA_COUNT);

		this.addSlot(new Slot(spindle, CROSSBAR_SLOT, 44, 38) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof MarionetteCrossbarItem;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});

		this.addSlot(new Slot(spindle, THREAD_SLOT, 134, 38) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.is(ItemInit.puppeteering_thread.get());
			}
		});

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
						8 + col * 18, 112 + row * 18));
			}
		}

		for (int col = 0; col < 9; ++col) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 170));
		}

		this.addDataSlots(data);
	}

	private static PuppeteersSpindleBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf data) {
		Objects.requireNonNull(inventory);
		Objects.requireNonNull(data);
		BlockEntity blockEntity = inventory.player.level().getBlockEntity(data.readBlockPos());
		if (blockEntity instanceof PuppeteersSpindleBlockEntity spindle) {
			return spindle;
		}
		throw new IllegalStateException("Block entity is not a PuppeteersSpindleBlockEntity: " + blockEntity);
	}

	public PuppeteersSpindleBlockEntity getSpindle() {
		return spindle;
	}

	public ItemStack getCrossbarStack() {
		return spindle.getItem(CROSSBAR_SLOT);
	}

	public int getThreadBuffer() {
		return dataAccess.get(0);
	}

	public int getCrossbarThread() {
		ItemStack crossbar = getCrossbarStack();
		return crossbar.getItem() instanceof MarionetteCrossbarItem
				? MarionetteCrossbarItem.getThread(crossbar)
				: 0;
	}

	public String getSelectedSummonName() {
		ItemStack crossbar = getCrossbarStack();
		return crossbar.getItem() instanceof MarionetteCrossbarItem
				? MarionetteCrossbarItem.getSelectedSummonName(crossbar)
				: "";
	}

	public int getScaledThreadBufferWidth(int width) {
		return getThreadBuffer() * width / PuppeteersSpindleBlockEntity.THREAD_BUFFER_CAPACITY;
	}

	public int getScaledCrossbarThreadWidth(int width) {
		return getCrossbarThread() * width / PuppeteerSummonRules.THREAD_CAPACITY;
	}

	@Override
	public boolean stillValid(Player player) {
		return spindle.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return result;
		}

		ItemStack slotStack = slot.getItem();
		result = slotStack.copy();

		if (index < SLOT_COUNT) {
			if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 36, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			if (slotStack.getItem() instanceof MarionetteCrossbarItem) {
				if (!this.moveItemStackTo(slotStack, CROSSBAR_SLOT, CROSSBAR_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotStack.is(ItemInit.puppeteering_thread.get())) {
				if (!this.moveItemStackTo(slotStack, THREAD_SLOT, THREAD_SLOT + 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index < SLOT_COUNT + 27) {
				if (!this.moveItemStackTo(slotStack, SLOT_COUNT + 27, SLOT_COUNT + 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(slotStack, SLOT_COUNT, SLOT_COUNT + 27, false)) {
				return ItemStack.EMPTY;
			}
		}

		if (slotStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (slotStack.getCount() == result.getCount()) {
			return ItemStack.EMPTY;
		}
		slot.onTake(player, slotStack);
		return result;
	}
}
