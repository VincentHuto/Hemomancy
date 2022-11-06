package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.container.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.item.BloodyFlaskItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VialCentrifugeBlockEntity extends BaseContainerBlockEntity
		implements StackedContentsCompatible, IBloodTile {

	static final String TAG_BLOOD_LEVEL = "bloodLevel";
	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			VialCentrifugeBlockEntity self) {
	}
	public static void serverTick(Level level, BlockPos pos, BlockState p_155016_, VialCentrifugeBlockEntity te) {
	//	System.out.println(te.spinningProgress);
		if (te.spinningProgress > 0) {
			te.spinningProgress--;
			te.sendUpdates();
			setChanged(level, pos, p_155016_);
		}
	}
	public NonNullList<ItemStack> inventory = NonNullList.withSize(19, ItemStack.EMPTY);
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	int spinningProgress;

	int spinningTotalTime;

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			switch (index) {
			case 0:
				return spinningProgress;
			case 1:
				return spinningTotalTime;
			default:
				return 0;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public void set(int index, int val) {
			switch (index) {
			case 0:
				spinningProgress = val;
				break;
			case 1:
				spinningTotalTime = val;
			}
		}
	};

	public VialCentrifugeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.vial_centrifuge.get(), pos, state);
	}

	@Override
	public void clearContent() {
		this.inventory.clear();

	}

	@Override
	protected AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory) {
		return new VialCentrifugeMenu(pContainerId, pInventory, this, this.dataAccess);
	}

	@Override
	public void fillStackedContents(StackedContents pHelper) {
		for (ItemStack itemstack : this.inventory) {
			pHelper.accountStack(itemstack);
		}
	}

	public IBloodVolume getBloodCapability() {
		return volume;
	}

	public double getBloodVolume() {
		return volume.getBloodVolume();
	}

	// CONTAINER
	@Override
	public int getContainerSize() {
		return this.inventory.size();
	}

	@Override
	protected Component getDefaultName() {
		return Component.literal("container.hemomancy.vialcentrifuge");
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return this.inventory.get(pSlot);
	}

	public double getMaxBloodVolume() {
		return volume.getMaxBloodVolume();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("SpinTime", this.spinningProgress);
		tag.putInt("SpinTimeTotal", this.spinningTotalTime);
		ContainerHelper.saveAllItems(tag, this.inventory);
		CompoundTag compoundtag = new CompoundTag();
		tag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		if (tag != null) {
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}
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

	@Override
	public void load(CompoundTag pTag) {
		super.load(pTag);
		this.spinningProgress = pTag.getInt("SpinTime");
		this.spinningTotalTime = pTag.getInt("SpinTimeTotal");
		this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(pTag, this.inventory);
		if (pTag != null) {
			volume.setBloodVolume(pTag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			volume.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
		}

	}

	@Override
	public void onLoad() {
		volume.setActive(true);
		volume.setMaxBloodVolume(2000f);
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		return ContainerHelper.removeItem(this.inventory, pSlot, pAmount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		return ContainerHelper.takeItem(this.inventory, pSlot);
	}

	// NBT and Data
	@Override
	protected void saveAdditional(CompoundTag pTag) {
		super.saveAdditional(pTag);
		pTag.putInt("SpinTime", this.spinningProgress);
		pTag.putInt("SpinTimeTotal", this.spinningTotalTime);
		ContainerHelper.saveAllItems(pTag, this.inventory);
		if (pTag != null) {
			pTag.putDouble(TAG_BLOOD_LEVEL, volume.getBloodVolume());
		}
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

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		ItemStack inventorytack = this.inventory.get(pSlot);
		boolean flag = !pStack.isEmpty() && pStack.sameItem(inventorytack)
				&& ItemStack.tagMatches(pStack, inventorytack);
		this.inventory.set(pSlot, pStack);
		if (pStack.getCount() > this.getMaxStackSize()) {
			pStack.setCount(this.getMaxStackSize());
		}
		if (pSlot == 1 && volume.getBloodVolume() <= 1900f) {
			if (this.inventory.get(1).getItem() instanceof BloodyFlaskItem flask) {
				this.inventory.get(1).shrink(1);
				volume.fill(flask.getAmount());
				sendUpdates();
			}
		}

	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

}