package com.vincenthuto.hemomancy.common.tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.menu.VialCentrifugeMenu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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

	public NonNullList<ItemStack> inventory = NonNullList.withSize(19, ItemStack.EMPTY);
	IBloodVolume volume = getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
	int spinningProgress;
	int spinningTotalTime;
	int canSpin;
//	private static final int[] INPUT_SLOTS = new int[] { 2, 3, 4, 5, 6, 7, 8, 9 };
//	private static final int[] OUTPUT_SLOTS = new int[] { 10, 11, 12, 13, 14, 15, 16, 17 };
	Map<Integer, Integer> inOutMap = new HashMap<Integer, Integer>() {
		{
			put(2, 10);
			put(3, 11);
			put(4, 12);
			put(5, 13);
			put(6, 14);
			put(7, 15);
			put(8, 16);
			put(9, 17);

		}
	};
	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			switch (index) {
			case 0:
				return spinningProgress;
			case 1:
				return spinningTotalTime;
			case 2:
				return canSpin;
			default:
				return 0;
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public void set(int index, int val) {
			switch (index) {
			case 0:
				spinningProgress = val;
				break;
			case 1:
				spinningTotalTime = val;
				break;
			case 2:
				canSpin = val;
			}
		}
	};

	public VialCentrifugeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.vial_centrifuge.get(), pos, state);
	}

	static final String TAG_BLOOD_LEVEL = "bloodLevel";

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			VialCentrifugeBlockEntity self) {
	}

	public static void serverTick(Level level, BlockPos pos, BlockState p_155016_, VialCentrifugeBlockEntity te) {
		// System.out.println(te.spinningProgress);
		if (te.spinningProgress > 0) {
			te.spinningProgress--;
			// System.out.println(te.spinningProgress);
			te.sendUpdates();
			setChanged(level, pos, p_155016_);
			if (te.spinningProgress == 11) {
				te.outputResults();
			}
			if (!te.inventory.isEmpty()) {
				if (!((te.checkBalancedSpots(2, 6) && te.checkBalancedSpots(3, 7) && te.checkBalancedSpots(4, 8)
						&& te.checkBalancedSpots(9, 5)))) {
					if (te.dataAccess.get(0) > 0) {
						te.dataAccess.set(0, 0);
					}
				}
			}
		}
	}

	private void outputResults() {
		System.out.println("Done");
		for (int i = 0; i < getVialSlots().size(); i++) {
			ItemStack vialStack = getVialSlots().get(i);
			if (!getVialSlots().get(i).isEmpty()) {
				if (vialStack.getItem() instanceof BloodVialItem) {
					EntityType<?> sampledMob = BloodVialItem.getEntityType(vialStack);
					ItemStack resultStack = getResultFromVial(sampledMob);
					System.out.println(resultStack);
					vialStack = new ItemStack(ItemInit.bloody_vial.get(), 1);
					// Only outputs to slot if it is not already occupied
					if (inventory.get(inOutMap.get(i + 2)).isEmpty()) {
						inventory.set(i + 2, vialStack);
						inventory.set(inOutMap.get(i + 2), resultStack);
					} else {
						if (inventory.get(inOutMap.get(i + 2)).getItem() == resultStack.getItem()
								&& !(resultStack.getCount() + inventory.get(inOutMap.get(i + 2))
										.getCount() > resultStack.getMaxStackSize())) {
							inventory.get(inOutMap.get(i + 2)).grow(resultStack.getCount());
							inventory.set(i + 2, vialStack);
						}
					}
					// Auxillary output chance 
					if (level.random.nextInt(1, 5) %2 == 0) {
						if (inventory.get(18).isEmpty()) {
							inventory.set(18, new ItemStack(ItemInit.befouling_ash.get()));
						} else if (inventory.get(18).getCount() < 64
								&& inventory.get(18).getItem() == ItemInit.befouling_ash.get()) {
							inventory.get(18).grow(1);
						}
					}
				}
			}
		}
	}

	public List<ItemStack> getVialSlots() {
		return inventory.subList(2, 10);
	}

	public List<ItemStack> getOutputSlots() {
		return inventory.subList(10, 18);
	}

	public boolean isCentrifugeEmpty() {
		return getVialSlots().stream().allMatch(element -> element.isEmpty());
	}

	public int findEmptyOutputSlot() {
		return 10;
	}

	public ItemStack getResultFromVial(EntityType<?> sampledMob) {
		if (sampledMob != null) {
			if (sampledMob.create(level) instanceof LivingEntity living) {
				float maxHealth = living.getMaxHealth();
				// Check what enzyme tags contain entity and run based on that
				ArrayList<ItemStack> outputList = new ArrayList<ItemStack>();
				if (sampledMob.is(EntityInit.FUNGAL_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(BlockInit.infected_fungus.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.UMBRAL_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.umbral_enzyme.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.INCANDESCENT_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.incandescent_enzyme.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.FERRIC_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.ferric_enzyme.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.VIVACIOUS_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.vivacious_enzyme.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.RUINOUS_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.ruinous_enzyme.get(), amountOfEnzyme));

				}
				if (sampledMob.is(EntityInit.NEUROTIC_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.neurotic_enzyme.get(), amountOfEnzyme));

				}
				if (sampledMob.is(EntityInit.FERVENT_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.fervent_enzyme.get(), amountOfEnzyme));
				}
				if (sampledMob.is(EntityInit.FRIGID_TAG)) {
					int amountOfEnzyme = (int) (((maxHealth / 10) * level.getRandom().nextInt(5 - 1) + 1));
					outputList.add(new ItemStack(ItemInit.frigid_enzyme.get(), amountOfEnzyme));
				}
				if (!outputList.isEmpty()) {
					if (!outputList.get(new Random().nextInt(outputList.size())).isEmpty()) {
						return outputList.get(new Random().nextInt(outputList.size()));
					} else {
						return ItemStack.EMPTY;
					}
				} else {
					return ItemStack.EMPTY;
				}

			}
		}
		return ItemStack.EMPTY;

	}

	public boolean attemptStartup() {
		// 2 6
		// 3 7
		// 4 8
		// 9 5
		// These are the Slots that are across and need to be balanced
		if (isCentrifugeEmpty()) {
			System.out.println("Centrifuge is Empty");
			return false;
		} else {
			if ((checkBalancedSpots(2, 6) && checkBalancedSpots(3, 7) && checkBalancedSpots(4, 8)
					&& checkBalancedSpots(9, 5))) {
				if (dataAccess.get(0) <= 0) {
					dataAccess.set(0, 200);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public boolean checkBalancedSpots(int a, int b) {
		return !((!inventory.get(a).isEmpty() && inventory.get(b).isEmpty())
				|| (inventory.get(a).isEmpty() && !inventory.get(b).isEmpty()));
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