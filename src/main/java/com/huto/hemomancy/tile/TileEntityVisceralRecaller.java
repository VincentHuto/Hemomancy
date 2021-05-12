package com.huto.hemomancy.tile;

import javax.annotation.Nonnull;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.container.ContainerVisceralRecaller;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.ItemBloodyFlask;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityVisceralRecaller extends LockableLootTileEntity
		implements ITickableTileEntity, INamedContainerProvider {
	public NonNullList<ItemStack> contents = NonNullList.<ItemStack>withSize(5, ItemStack.EMPTY);
	public SimpleItemStackHandler itemHandler = createItemHandler();
	public static final String TAG_BLOOD_LEVEL = "bloodLevel";
	public float clientBloodLevel = 0.0f;
	public IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);

	public TileEntityVisceralRecaller() {
		super(TileEntityInit.visceral_artificial_recaller.get());
	}

	@Override
	public void tick() {

	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		return super.receiveClientEvent(id, type);
	}

	@Override
	public void onLoad() {
	}

	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, true);
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public float getBloodVolume() {
		return volume.getBloodVolume();
	}

	public float getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	// NBT
	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		readPacketNBT(compound);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(compound)) {
			ItemStackHelper.loadAllItems(compound, this.contents);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		writePacketNBT(compound);
		if (!this.checkLootAndWrite(compound)) {
			ItemStackHelper.saveAllItems(compound, this.contents);
		}
		compound.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return compound;
	}

	public void readPacketNBT(CompoundNBT par1CompoundNBT) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(par1CompoundNBT);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(par1CompoundNBT)) {
			ItemStackHelper.loadAllItems(par1CompoundNBT, this.contents);
		}
		volume.setBloodVolume(par1CompoundNBT.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = par1CompoundNBT.getFloat(TAG_BLOOD_LEVEL);

	}

	public void writePacketNBT(CompoundNBT par1CompoundNBT) {
		par1CompoundNBT.merge(itemHandler.serializeNBT());

	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		CompoundNBT tag = pkt.getNbtCompound();
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);
	}

	@Override
	public final CompoundNBT getUpdateTag() {
		return write(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT tag = new CompoundNBT();
		writePacketNBT(tag);
		tag.putFloat(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return new SUpdateTileEntityPacket(pos, -999, tag);
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.contents = NonNullList.withSize(this.getSizeInventory(), ItemStack.EMPTY);
		if (!this.checkLootAndRead(tag)) {
			ItemStackHelper.loadAllItems(tag, this.contents);
		}
		volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		clientBloodLevel = tag.getFloat(TAG_BLOOD_LEVEL);

	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nonnull Direction side) {
		return super.getCapability(cap, side);

	}

	public static void swapContents(TileEntityVisceralRecaller te, TileEntityVisceralRecaller otherTe) {
		NonNullList<ItemStack> list = te.getItems();
		te.setItems(otherTe.getItems());
		otherTe.setItems(list);
	}

	public void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, getBlockState(), getBlockState());
		world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
		markDirty();
	}

	@Override
	public int getSizeInventory() {
		return 5;
	}

	@Override
	public NonNullList<ItemStack> getItems() {

		if (!contents.get(2).isEmpty()) {
			ItemStack stack = contents.get(2);
			if (stack.getItem() instanceof ItemBloodyFlask) {
				ItemBloodyFlask flask = (ItemBloodyFlask) stack.getItem();
				if (this.getBloodVolume() < this.getMaxBloodVolume()) {
					volume.addBloodVolume(flask.getAmount());
					stack.shrink(1);
				}
			}
		}

		return this.contents;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> itemsIn) {
		this.contents = itemsIn;

	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.visceral_recaller");
	}

	@Override
	protected Container createMenu(int id, PlayerInventory player) {
		return new ContainerVisceralRecaller(id, player, this);
	}

	public static class SimpleItemStackHandler extends ItemStackHandler {

		private final boolean allowWrite;
		private final TileEntity tile;

		public SimpleItemStackHandler(TileEntity inv, boolean allowWrite) {
			super(5);
			this.allowWrite = allowWrite;
			tile = inv;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (allowWrite) {
				return super.insertItem(slot, stack, simulate);
			} else
				return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (allowWrite) {

				return super.extractItem(slot, amount, simulate);
			} else
				return ItemStack.EMPTY;
		}

		@Override
		public void onContentsChanged(int slot) {
			tile.markDirty();
		}
	}

}