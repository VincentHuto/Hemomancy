package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DictationTableBlockEntity extends BlockEntity {
	private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

	public DictationTableBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.dictation_table.get(), pos, state);
	}

	public ItemStack getLiber() {
		return items.get(0);
	}

	public void setLiber(ItemStack stack) {
		items.set(0, stack);
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	public ItemStack removeLiber() {
		ItemStack stack = getLiber();
		setLiber(ItemStack.EMPTY);
		return stack;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		ContainerHelper.saveAllItems(tag, items, provider);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		for (int i = 0; i < items.size(); i++) {
			items.set(i, ItemStack.EMPTY);
		}
		ContainerHelper.loadAllItems(tag, items, provider);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		ContainerHelper.saveAllItems(tag, items, provider);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
