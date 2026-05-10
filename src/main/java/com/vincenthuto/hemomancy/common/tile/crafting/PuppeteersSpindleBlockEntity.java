package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class PuppeteersSpindleBlockEntity extends BaseContainerBlockEntity {
	public static final int SLOT_CROSSBAR = 0;
	public static final int SLOT_THREAD = 1;
	public static final int INVENTORY_SIZE = 2;
	public static final int THREAD_BUFFER_CAPACITY = 512;

	private static final String TAG_THREAD_BUFFER = "ThreadBuffer";

	private NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	private int threadBuffer;

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			return index == 0 ? threadBuffer : 0;
		}

		@Override
		public void set(int index, int value) {
			if (index == 0) {
				threadBuffer = Math.max(0, Math.min(THREAD_BUFFER_CAPACITY, value));
			}
		}

		@Override
		public int getCount() {
			return 1;
		}
	};

	public PuppeteersSpindleBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.puppeteers_spindle.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, PuppeteersSpindleBlockEntity spindle) {
		boolean changed = spindle.consumeThreadInput();
		changed |= spindle.refillSlottedCrossbar();
		if (changed) {
			spindle.sendUpdates();
			setChanged(level, pos, state);
		}
	}

	private boolean consumeThreadInput() {
		ItemStack stack = inventory.get(SLOT_THREAD);
		if (stack.isEmpty() || !stack.is(ItemInit.puppeteering_thread.get()) || threadBuffer >= THREAD_BUFFER_CAPACITY) {
			return false;
		}
		int accepted = Math.min(stack.getCount(), THREAD_BUFFER_CAPACITY - threadBuffer);
		if (accepted <= 0) {
			return false;
		}
		threadBuffer += accepted;
		stack.shrink(accepted);
		if (stack.isEmpty()) {
			inventory.set(SLOT_THREAD, ItemStack.EMPTY);
		}
		return true;
	}

	private boolean refillSlottedCrossbar() {
		if (threadBuffer <= 0) {
			return false;
		}
		ItemStack crossbar = inventory.get(SLOT_CROSSBAR);
		if (!(crossbar.getItem() instanceof MarionetteCrossbarItem)) {
			return false;
		}
		int before = MarionetteCrossbarItem.getThread(crossbar);
		int space = Math.max(0, PuppeteerSummonRules.THREAD_CAPACITY - before);
		if (space <= 0) {
			return false;
		}
		int transfer = Math.min(space, threadBuffer);
		int after = MarionetteCrossbarItem.addThread(crossbar, transfer);
		int moved = Math.max(0, after - before);
		if (moved <= 0) {
			return false;
		}
		threadBuffer -= moved;
		return true;
	}

	public int getThreadBuffer() {
		return threadBuffer;
	}

	public ItemStack getCrossbar() {
		return inventory.get(SLOT_CROSSBAR);
	}

	public boolean hasSlottedCrossbar() {
		return getCrossbar().getItem() instanceof MarionetteCrossbarItem;
	}

	public void sendUpdates() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	public int getContainerSize() {
		return INVENTORY_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : inventory) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return inventory.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack removed = ContainerHelper.removeItem(inventory, slot, amount);
		if (!removed.isEmpty()) {
			setChanged();
		}
		return removed;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(inventory, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		inventory.set(slot, stack);
		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}
		setChanged();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return switch (slot) {
			case SLOT_CROSSBAR -> stack.getItem() instanceof MarionetteCrossbarItem;
			case SLOT_THREAD -> stack.is(ItemInit.puppeteering_thread.get());
			default -> false;
		};
	}

	@Override
	public boolean stillValid(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this) {
			return false;
		}
		return player.distanceToSqr(worldPosition.getX() + 0.5,
				worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
	}

	@Override
	public void clearContent() {
		inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		for (int i = 0; i < Math.min(items.size(), inventory.size()); i++) {
			inventory.set(i, items.get(i));
		}
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
		return new PuppeteersSpindleMenu(id, playerInventory, this, dataAccess);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.puppeteers_spindle");
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, inventory, registries);
		threadBuffer = Math.max(0, Math.min(THREAD_BUFFER_CAPACITY, tag.getInt(TAG_THREAD_BUFFER)));
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ContainerHelper.saveAllItems(tag, inventory, registries);
		tag.putInt(TAG_THREAD_BUFFER, threadBuffer);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, inventory, registries);
		tag.putInt(TAG_THREAD_BUFFER, threadBuffer);
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, inventory, registries);
		threadBuffer = Math.max(0, Math.min(THREAD_BUFFER_CAPACITY, tag.getInt(TAG_THREAD_BUFFER)));
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt,
			HolderLookup.Provider registries) {
		if (pkt.getTag() != null) {
			handleUpdateTag(pkt.getTag(), registries);
		}
	}
}
