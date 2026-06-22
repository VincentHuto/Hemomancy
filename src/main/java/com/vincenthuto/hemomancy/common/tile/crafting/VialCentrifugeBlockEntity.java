package com.vincenthuto.hemomancy.common.tile.crafting;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.tile.IBloodContainerSlotAccess;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

import javax.annotation.Nullable;
import java.util.*;

public class VialCentrifugeBlockEntity extends BaseContainerBlockEntity
		implements StackedContentsCompatible, IBloodReservoir, IBloodContainerSlotAccess {

	public static final int SLOT_INPUT = 0;
	public static final int SLOT_BLOOD = 1;
	public static final int SLOT_FLASK_OUTPUT = 19;
	public static final int INVENTORY_SIZE = 20;
	private static final double BLOOD_GAIN_PER_SPIN = 250D;

	public NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	public static final int SPIN_TOTAL_TIME = 200;
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

	// ---- Lazy capability access ----

	@Nullable
	private IBloodVolume resolveVolume() {
		return HemoCapabilityAccess.getBloodVolume(this).orElse(null);
	}

	static final String TAG_BLOOD_LEVEL = "bloodLevel";

	public static void clientTick(Level level, BlockPos worldPosition, BlockState state,
			VialCentrifugeBlockEntity self) {
	}

	public static void serverTick(Level level, BlockPos pos, BlockState p_155016_, VialCentrifugeBlockEntity te) {
		// Process blood flask in the blood slot
		te.processBloodSlot();

		if (te.spinningProgress > 0) {
			te.spinningProgress--;
			te.sendUpdates();
			setChanged(level, pos, p_155016_);
			if (te.spinningProgress == 0) {
				te.outputResults();
				te.addOperationBlood();
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

	private void addOperationBlood() {
		IBloodVolume vol = resolveVolume();
		if (vol == null || vol.isFull()) return;
		double spaceRemaining = vol.getMaxBloodVolume() - vol.getBloodVolume();
		double amountToAdd = Math.min(BLOOD_GAIN_PER_SPIN, Math.max(0D, spaceRemaining));
		if (amountToAdd <= 0) return;
		vol.fill(amountToAdd);
		sendUpdates();
	}

	private void outputResults() {
		for (int i = 0; i < getVialSlots().size(); i++) {
			ItemStack vialStack = getVialSlots().get(i);
			if (!getVialSlots().get(i).isEmpty()) {
				if (vialStack.getItem() instanceof BloodVialItem) {
					EntityType<?> sampledMob = BloodVialItem.getEntityType(vialStack);
					ItemStack resultStack = getResultFromVial(sampledMob);
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
							inventory.set(18, new ItemStack(BlockInit.befouling_ash_trail.get()));
						} else if (inventory.get(18).getCount() < 64
								&& inventory.get(18).getItem() == BlockInit.befouling_ash_trail.get().asItem()) {
							inventory.get(18).grow(1);
						}
					}
				} else if (vialStack.getItem() instanceof ConsecratedSyringeItem) {
					EnumSaintType saint = ConsecratedSyringeItem.getSaintType(vialStack);
					if (saint != null) {
						ItemStack resultStack = getResultFromSyringe(saint);
						if (!resultStack.isEmpty()) {
							ItemStack outputSlot = inventory.get(inOutMap.get(i + 2));
							if (outputSlot.isEmpty()) {
								inventory.set(i + 2, ItemStack.EMPTY);
								inventory.set(inOutMap.get(i + 2), resultStack);
							} else if (outputSlot.getItem() == resultStack.getItem()
									&& outputSlot.getCount() + resultStack.getCount() <= outputSlot.getMaxStackSize()) {
								outputSlot.grow(resultStack.getCount());
								inventory.set(i + 2, ItemStack.EMPTY);
							}
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

	public int insertVialsFromRack(ItemStack rackStack) {
		if (!(rackStack.getItem() instanceof VialRackItem)) {
			return 0;
		}
		NonNullList<ItemStack> rackVials = VialRackItem.getVials(rackStack);
		int moved = 0;
		for (int i = 0; i < rackVials.size(); i++) {
			ItemStack rackVial = rackVials.get(i);
			if (!rackVial.isEmpty() && !VialRackItem.isEmptyVial(rackVial)) {
				int destination = firstEmptyCentrifugeVialSlot();
				if (destination == -1) {
					break;
				}
				inventory.set(destination, rackVial.copyWithCount(1));
				rackVials.set(i, VialRackItem.createDefaultVial());
				moved++;
			}
		}
		if (moved > 0) {
			VialRackItem.setVials(rackStack, rackVials);
			sendUpdates();
		}
		return moved;
	}

	private int firstEmptyCentrifugeVialSlot() {
		// Slots 2-9 are the centrifuge's 8 vial input positions.
		for (int i = 2; i <= 9; i++) {
			if (inventory.get(i).isEmpty()) {
				return i;
			}
		}
		return -1;
	}

	// ---- Blood slot processing ----

	private void processBloodSlot() {
		if (processBloodContainerInputSlot(this, this)) sendUpdates();
	}

	@Override
	public int getBloodContainerInputSlot() {
		return SLOT_BLOOD;
	}

	@Override
	public int getEmptyBloodContainerOutputSlot() {
		return SLOT_FLASK_OUTPUT;
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

	private ItemStack getResultFromSyringe(EnumSaintType saint) {
		switch (saint) {
		case HEMORATH:
			return new ItemStack(ItemInit.hallowed_residuum_hemorath.get());
		case SERAPHAE:
			return new ItemStack(ItemInit.hallowed_residuum_seraphae.get());
		case PUTRICIEL:
			return new ItemStack(ItemInit.hallowed_residuum_putriciel.get());
		case VELORUM:
			return new ItemStack(ItemInit.hallowed_residuum_velorum.get());
		default:
			return ItemStack.EMPTY;
		}
	}

	public boolean attemptStartup() {
		// 2 6
		// 3 7
		// 4 8
		// 9 5
		// These are the Slots that are across and need to be balanced
		if (isCentrifugeEmpty() || !hasAtLeastOneProcessableVial()) {
			return false;
		} else {
			if ((checkBalancedSpots(2, 6) && checkBalancedSpots(3, 7) && checkBalancedSpots(4, 8)
					&& checkBalancedSpots(9, 5))) {
				if (dataAccess.get(0) <= 0) {
					dataAccess.set(0, SPIN_TOTAL_TIME);
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	private boolean hasAtLeastOneProcessableVial() {
		for (int inputSlot = 2; inputSlot <= 9; inputSlot++) {
			ItemStack vialStack = inventory.get(inputSlot);
			if (vialStack.isEmpty() || !(vialStack.getItem() instanceof BloodVialItem)) {
				continue;
			}
			EntityType<?> sampledMob = BloodVialItem.getEntityType(vialStack);
			if (sampledMob == null) {
				continue;
			}
			ItemStack resultStack = getResultFromVial(sampledMob);
			if (resultStack.isEmpty()) {
				continue;
			}
			Integer outputSlot = inOutMap.get(inputSlot);
			if (outputSlot != null && canFitOutput(outputSlot, resultStack)) {
				return true;
			}
		}
		return false;
	}

	private boolean canFitOutput(int outputSlot, ItemStack resultStack) {
		ItemStack outputStack = inventory.get(outputSlot);
		if (outputStack.isEmpty()) {
			return true;
		}
		int maxAllowed = Math.min(outputStack.getMaxStackSize(), resultStack.getMaxStackSize());
		return ItemStack.isSameItemSameComponents(outputStack, resultStack)
				&& outputStack.getCount() + resultStack.getCount() <= maxAllowed;
	}

	public boolean checkBalancedSpots(int a, int b) {
		return !((!inventory.get(a).isEmpty() && inventory.get(b).isEmpty())
				|| (inventory.get(a).isEmpty() && !inventory.get(b).isEmpty()));
	}

	public boolean isSpinning() {
		return this.spinningProgress > 0;
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

	@Nullable
	public IBloodVolume getBloodCapability() {
		return resolveVolume();
	}

	public double getBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getBloodVolume() : 0;
	}

	// CONTAINER
	@Override
	public int getContainerSize() {
		return this.inventory.size();
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.hemomancy.vialcentrifuge");
	}

	@Override
	public ItemStack getItem(int pSlot) {
		return this.inventory.get(pSlot);
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.inventory;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.inventory = items;
	}

	public double getMaxBloodVolume() {
		IBloodVolume vol = resolveVolume();
		return vol != null ? vol.getMaxBloodVolume() : 0;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putInt("SpinTime", this.spinningProgress);
		tag.putInt("SpinTimeTotal", this.spinningTotalTime);
		ContainerHelper.saveAllItems(tag, this.inventory, registries);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			tag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
		return tag;
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		super.handleUpdateTag(tag, registries);
		if (tag != null) {
			IBloodVolume vol = resolveVolume();
			if (vol != null) {
				vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			}
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
	protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
		super.loadAdditional(pTag, registries);
		this.spinningProgress = pTag.getInt("SpinTime");
		this.spinningTotalTime = pTag.getInt("SpinTimeTotal");
		this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(pTag, this.inventory, registries);
		IBloodVolume vol = resolveVolume();
		if (vol != null && pTag != null) {
			vol.setBloodVolume(pTag.getFloat(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		super.onDataPacket(net, pkt, registries);
		if (pkt.getTag() != null) {
			CompoundTag tag = pkt.getTag();
			IBloodVolume vol = resolveVolume();
			if (vol != null) {
				vol.setBloodVolume(tag.getFloat(TAG_BLOOD_LEVEL));
			}
		}

	}

	@Override
	public void onLoad() {
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setActive(true);
			vol.setMaxBloodVolume(2000f);
		}
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
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
		super.saveAdditional(pTag, registries);
		pTag.putInt("SpinTime", this.spinningProgress);
		pTag.putInt("SpinTimeTotal", this.spinningTotalTime);
		ContainerHelper.saveAllItems(pTag, this.inventory, registries);
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			pTag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
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
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

}
