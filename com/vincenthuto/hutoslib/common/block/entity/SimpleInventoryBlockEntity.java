
package com.vincenthuto.hutoslib.common.block.entity;

import javax.annotation.Nullable;

import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleInventoryBlockEntity extends BaseContainerBlockEntity {

	int size;
	public NonNullList<ItemStack> inventory;

	protected SimpleInventoryBlockEntity(BlockEntityType<?> p_155076_, BlockPos p_155077_, BlockState p_155078_, int size) {
		super(p_155076_, p_155077_, p_155078_);
		this.size = size;
		this.inventory = NonNullList.withSize(size, ItemStack.EMPTY);
	}

	@Override
	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
		return null;
	}

	@Override
	public int getContainerSize() {
		return this.inventory.size();
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return this.inventory.get(pSlot);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return ContainerHelper.removeItem(this.inventory, pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return ContainerHelper.takeItem(this.inventory, pSlot);
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		this.inventory.set(pSlot, pStack);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void clearContent() {
		this.inventory.clear();

	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("hutoslib.display_pedestal.name");
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {

		boolean did = false;
		for (int i = 0; i < inventory.size(); i++) {
			if (inventory.get(i).isEmpty()) {
				did = true;
				ItemStack stackToAdd = stack.copy();
				stackToAdd.setCount(1);
				inventory.set(i, stackToAdd);

				if (player == null || !player.getAbilities().instabuild) {
					stack.shrink(1);
				}
				break;
			}
		}

		if (did) {
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}

		return true;
	}

	// NBT

	@Override
	@Nullable
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
	}

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(pTag, this.inventory);
	}

	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		ContainerHelper.saveAllItems(pTag, this.inventory);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, this.inventory);
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
	}

	public void sendUpdates() {
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	public void setChanged() {
		super.setChanged();
	}
}
