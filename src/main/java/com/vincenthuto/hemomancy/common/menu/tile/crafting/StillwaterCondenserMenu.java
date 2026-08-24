package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.tile.crafting.StillwaterCondenserBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;

import java.util.Objects;

public class StillwaterCondenserMenu extends AbstractContainerMenu {
	public static final int SLOT_COUNT = 2;
	private static final int INV_START = SLOT_COUNT;
	private static final int INV_END = INV_START + 27;
	private static final int HOTBAR_END = INV_END + 9;
	private final StillwaterCondenserBlockEntity blockEntity;
	private final ContainerData data;

	private static StillwaterCondenserBlockEntity blockEntity(Inventory inventory, FriendlyByteBuf buffer) {
		Objects.requireNonNull(buffer, "menu data");
		BlockEntity be = inventory.player.level().getBlockEntity(buffer.readBlockPos());
		if (be instanceof StillwaterCondenserBlockEntity condenser) return condenser;
		throw new IllegalStateException("Missing Stillwater Condenser at menu position");
	}

	public StillwaterCondenserMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
		this(id, inventory, blockEntity(inventory, buffer));
	}

	public StillwaterCondenserMenu(int id, Inventory inventory, StillwaterCondenserBlockEntity be) {
		this(id, inventory, be, new SimpleContainerData(StillwaterCondenserBlockEntity.DATA_COUNT));
	}

	public StillwaterCondenserMenu(int id, Inventory inventory, StillwaterCondenserBlockEntity be, ContainerData data) {
		super(ContainerInit.stillwater_condenser.get(), id);
		checkContainerSize(be, SLOT_COUNT);
		checkContainerDataCount(data, StillwaterCondenserBlockEntity.DATA_COUNT);
		this.blockEntity = be;
		this.data = data;
		addSlot(new Slot(be, StillwaterCondenserBlockEntity.SLOT_BOTTLES, 46, 35) {
			@Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.GLASS_BOTTLE); }
		});
		addSlot(new FurnaceResultSlot(inventory.player, be, StillwaterCondenserBlockEntity.SLOT_DEW, 116, 35) {
			@Override public void onTake(Player player, ItemStack stack) {
				if (player instanceof ServerPlayer serverPlayer && stack.getCount() > 0) {
					UnstainedObservances.recordDewProduced(serverPlayer, stack.getCount());
				}
				super.onTake(player, stack);
			}
		});
		for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
			addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
		for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
		addDataSlots(data);
	}

	public int progressPixels() {
		int total = data.get(StillwaterCondenserBlockEntity.DATA_TOTAL);
		return total <= 0 ? 0 : data.get(StillwaterCondenserBlockEntity.DATA_PROGRESS) * 38 / total;
	}
	public boolean hasWater() { return data.get(StillwaterCondenserBlockEntity.DATA_WATER) != 0; }
	public boolean hasGhostPipe() { return data.get(StillwaterCondenserBlockEntity.DATA_GHOST_PIPE) != 0; }
	public boolean hasLatticeBoost() { return data.get(StillwaterCondenserBlockEntity.DATA_LATTICE) != 0; }
	public StillwaterCondenserBlockEntity blockEntity() { return blockEntity; }

	@Override public ItemStack quickMoveStack(Player player, int index) {
		Slot slot = slots.get(index);
		if (!slot.hasItem()) return ItemStack.EMPTY;
		ItemStack original = slot.getItem();
		ItemStack copy = original.copy();
		if (index < SLOT_COUNT) {
			if (!moveItemStackTo(original, INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
		} else if (original.is(Items.GLASS_BOTTLE)) {
			if (!moveItemStackTo(original, 0, 1, false)) return ItemStack.EMPTY;
		} else if (index < INV_END) {
			if (!moveItemStackTo(original, INV_END, HOTBAR_END, false)) return ItemStack.EMPTY;
		} else if (!moveItemStackTo(original, INV_START, INV_END, false)) return ItemStack.EMPTY;
		if (original.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
		if (original.getCount() == copy.getCount()) return ItemStack.EMPTY;
		slot.onTake(player, original);
		return copy;
	}

	@Override public boolean stillValid(Player player) { return blockEntity.stillValid(player); }
}
