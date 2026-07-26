package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.block.unstained.crafting.VerdigrisLatticeBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.StillwaterCondenserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class StillwaterCondenserBlockEntity extends BaseContainerBlockEntity {
	public static final int SLOT_BOTTLES = 0;
	public static final int SLOT_DEW = 1;
	public static final int SLOT_COUNT = 2;
	public static final int DATA_PROGRESS = 0;
	public static final int DATA_TOTAL = 1;
	public static final int DATA_WATER = 2;
	public static final int DATA_GHOST_PIPE = 3;
	public static final int DATA_LATTICE = 4;
	public static final int DATA_COUNT = 5;
	private static final int BASE_PROCESS_TIME = 200;

	private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
	private int progress;
	private boolean hasWater;
	private boolean hasGhostPipe;
	private boolean latticeBoosted;

	public final ContainerData dataAccess = new ContainerData() {
		@Override public int get(int index) {
			return switch (index) {
				case DATA_PROGRESS -> progress;
				case DATA_TOTAL -> latticeBoosted ? BASE_PROCESS_TIME / 2 : BASE_PROCESS_TIME;
				case DATA_WATER -> hasWater ? 1 : 0;
				case DATA_GHOST_PIPE -> hasGhostPipe ? 1 : 0;
				case DATA_LATTICE -> latticeBoosted ? 1 : 0;
				default -> 0;
			};
		}
		@Override public void set(int index, int value) {
			if (index == DATA_PROGRESS) progress = value;
			else if (index == DATA_WATER) hasWater = value != 0;
			else if (index == DATA_GHOST_PIPE) hasGhostPipe = value != 0;
			else if (index == DATA_LATTICE) latticeBoosted = value != 0;
		}
		@Override public int getCount() { return DATA_COUNT; }
	};

	public StillwaterCondenserBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.stillwater_condenser.get(), pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, StillwaterCondenserBlockEntity be) {
		boolean previousWater = be.hasWater;
		boolean previousGhostPipe = be.hasGhostPipe;
		boolean previousLattice = be.latticeBoosted;
		be.hasWater = hasStillWater(level, pos);
		be.hasGhostPipe = hasGhostPipe(level, pos);
		be.latticeBoosted = VerdigrisLatticeBlock.hasActiveLattice(level, pos, 5);
		ItemStack bottles = be.items.get(SLOT_BOTTLES);
		ItemStack output = be.items.get(SLOT_DEW);
		int amount = be.latticeBoosted ? 2 : 1;
		boolean canOutput = output.isEmpty() || output.is(ItemInit.lethean_dew.get())
				&& output.getCount() + amount <= output.getMaxStackSize();
		if (be.hasWater && be.hasGhostPipe && bottles.is(Items.GLASS_BOTTLE) && canOutput) {
			be.progress++;
			be.setChanged();
			if (be.progress >= (be.latticeBoosted ? BASE_PROCESS_TIME / 2 : BASE_PROCESS_TIME)) {
				bottles.shrink(1);
				if (output.isEmpty()) be.items.set(SLOT_DEW, new ItemStack(ItemInit.lethean_dew.get(), amount));
				else output.grow(amount);
				level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.7f,
						be.latticeBoosted ? 1.35f : 1.15f);
				be.progress = 0;
				be.setChanged();
			}
		} else if (be.progress != 0) {
			be.progress = Math.max(0, be.progress - 2);
			be.setChanged();
		} else if (previousWater != be.hasWater || previousGhostPipe != be.hasGhostPipe
				|| previousLattice != be.latticeBoosted) {
			be.setChanged();
		}
	}

	private static boolean hasStillWater(Level level, BlockPos pos) {
		for (BlockPos check : new BlockPos[] { pos.below(), pos.north(), pos.south(), pos.east(), pos.west() })
			if (level.getFluidState(check).is(FluidTags.WATER) && level.getFluidState(check).isSource()) return true;
		return false;
	}

	private static boolean hasGhostPipe(Level level, BlockPos pos) {
		for (BlockPos check : BlockPos.betweenClosed(pos.offset(-4, -2, -4), pos.offset(4, 2, 4)))
			if (level.getBlockState(check).is(BlockInit.ghost_pipe.get())) return true;
		return false;
	}

	@Override protected Component getDefaultName() { return Component.translatable("container.hemomancy.stillwater_condenser"); }
	@Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new StillwaterCondenserMenu(id, inventory, this, dataAccess);
	}
	@Override public int getContainerSize() { return SLOT_COUNT; }
	@Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
	@Override public ItemStack getItem(int slot) { return items.get(slot); }
	@Override public ItemStack removeItem(int slot, int amount) { return ContainerHelper.removeItem(items, slot, amount); }
	@Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
	@Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
	@Override public boolean stillValid(Player player) { return level != null && level.getBlockEntity(worldPosition) == this
			&& player.distanceToSqr(worldPosition.getCenter()) <= 64.0; }
	@Override public void clearContent() { items.clear(); }
	@Override protected NonNullList<ItemStack> getItems() { return items; }
	@Override protected void setItems(NonNullList<ItemStack> stacks) { items = stacks; }
	@Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == SLOT_BOTTLES && stack.is(Items.GLASS_BOTTLE); }

	@Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, items, registries);
		progress = tag.getInt("Progress");
	}
	@Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ContainerHelper.saveAllItems(tag, items, registries);
		tag.putInt("Progress", progress);
	}
}
