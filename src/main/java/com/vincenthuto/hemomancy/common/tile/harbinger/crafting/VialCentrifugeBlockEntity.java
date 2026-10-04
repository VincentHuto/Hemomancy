package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.item.harbinger.EntityBloodProfile;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodProfileData;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.harbinger.ConsecratedSyringeItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.station.*;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
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
import net.minecraft.world.Container;
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
		implements StackedContentsCompatible, IBloodReservoir, IBloodContainerSlotAccess, UpgradeableStation {

	public static final int SLOT_BLOOD = 1;
	public static final int SLOT_FLASK_OUTPUT = 19;
	public static final int INVENTORY_SIZE = 20;
	private static final double BLOOD_GAIN_PER_SPIN = 250D;

	public NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
	public static final int SPIN_TOTAL_TIME = 200;
	int spinningProgress;
	int spinningTotalTime;
	int startupResultId;
	UUID assignmentSpinId;
	UUID assignmentPlayerId;
	private boolean riteLocked;
	private UUID machineIdentity = UUID.randomUUID();
	private UUID lastUpgradeRite;
	private NonNullList<ItemStack> batchInputs = emptyBatch();
	private NonNullList<ItemStack> batchPrimary = emptyBatch();
	private NonNullList<ItemStack> batchSecondary = emptyBatch();
	private int batchPowder;
	private int batchStage;
	private boolean pendingBatch;
	private NonNullList<ItemStack> completedOutputs = NonNullList.withSize(8, ItemStack.EMPTY);
	private int[] completedQuantities = new int[24];

	private NonNullList<ItemStack> legacyRecovery = emptyBatch();
	private int[] legacyRecoveryQuantities = new int[24];

	private record Placement(int index, ItemStack stack, boolean ordinary) {}
	private record OutputPlan(VialCentrifugeStartupResult result, List<Placement> placements) {}

	private static NonNullList<ItemStack> emptyBatch() {
		return NonNullList.withSize(8, ItemStack.EMPTY);
	}

	public final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int index) {
			switch (index) {
			case 0:
				return spinningProgress;
			case 1:
				return spinningTotalTime;
			case 2:
				return startupResultId;
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
				startupResultId = val;
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

	public static void serverTick(Level level, BlockPos pos, BlockState state, VialCentrifugeBlockEntity te) {
		if (te.riteLocked) return;
		if (te.drainLegacyRecovery()) te.sendUpdates();
		te.processBloodSlot();
		if (te.spinningProgress <= 0) return;
		te.spinningProgress--;
		if (te.spinningProgress == 0 && te.outputResults()) te.addOperationBlood();
		te.sendUpdates();
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

	private static ItemStack insertFraction(NonNullList<ItemStack> outputs, ItemStack result,
			java.util.function.BiConsumer<Integer, ItemStack> inserted) {
		var remaining = result.copy();
		for (int pass = 0; pass < 2; pass++) {
			for (int i = 0; i < 8 && !remaining.isEmpty(); i++) {
				var existing = outputs.get(i);
				if (pass == 0 ? existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, remaining)
						: !existing.isEmpty()) continue;
				int count = Math.min(remaining.getCount(), remaining.getMaxStackSize() - existing.getCount());
				if (count <= 0) continue;
				var moved = remaining.copyWithCount(count);
				if (existing.isEmpty()) outputs.set(i, moved.copy());
				else existing.grow(count);
				inserted.accept(i, moved);
				remaining.shrink(count);
			}
		}
		return remaining;
	}

	private NonNullList<ItemStack> copyOutputs() {
		var outputs = emptyBatch();
		for (int i = 0; i < 8; i++) outputs.set(i, inventory.get(i + 10).copy());
		return outputs;
	}

	private OutputPlan planBatch() {
		if (!balanced()) return new OutputPlan(VialCentrifugeStartupResult.IMBALANCE, List.of());
		var outputs = copyOutputs();
		List<Placement> placements = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			if (!ItemStack.matches(inventory.get(i + 2), batchInputs.get(i)))
				return new OutputPlan(VialCentrifugeStartupResult.INVALID_SAMPLE, List.of());
			boolean ordinary = batchInputs.get(i).getItem() instanceof BloodVialItem;
			for (var fraction : List.of(batchPrimary.get(i), batchSecondary.get(i))) {
				var remaining = insertFraction(outputs, fraction,
						(index, stack) -> placements.add(new Placement(index, stack, ordinary)));
				if (!remaining.isEmpty()) return new OutputPlan(VialCentrifugeStartupResult.BLOCKED_ENZYME_OUTPUT, List.of());
			}
		}
		if (batchPowder > 0 && !canFitOutput(18, new ItemStack(ItemInit.hematic_iron_powder.get(), batchPowder)))
			return new OutputPlan(VialCentrifugeStartupResult.BLOCKED_POWDER_OUTPUT, List.of());
		return new OutputPlan(VialCentrifugeStartupResult.SUCCESS, placements);
	}

	private VialCentrifugeStartupResult validateBatch() { return planBatch().result(); }

	private boolean outputResults() {
		if (!pendingBatch) return false;
		var plan = planBatch();
		startupResultId = plan.result().ordinal();
		if (plan.result() != VialCentrifugeStartupResult.SUCCESS) return false;
		for (var placement : plan.placements()) {
			int slot = placement.index() + 10;
			if (placement.ordinary()) rememberOutput(placement.index(), slot, placement.stack());
			appendOutput(slot, placement.stack());
		}
		for (int i = 0; i < 8; i++) {
			if (batchInputs.get(i).isEmpty()) continue;
			boolean vial = batchInputs.get(i).getItem() instanceof BloodVialItem;
			inventory.set(i + 2, vial ? new ItemStack(ItemInit.bloody_vial.get()) : ItemStack.EMPTY);
		}
		if (batchPowder > 0) appendOutput(18, new ItemStack(ItemInit.hematic_iron_powder.get(), batchPowder));
		pendingBatch = false;
		batchInputs = emptyBatch();
		batchPrimary = emptyBatch();
		batchSecondary = emptyBatch();
		batchPowder = 0;
		assignmentSpinId = null;
		assignmentPlayerId = null;
		return true;
	}

	private void appendOutput(int slot, ItemStack result) {
		if (result.isEmpty()) return;
		if (inventory.get(slot).isEmpty()) inventory.set(slot, result.copy());
		else inventory.get(slot).grow(result.getCount());
	}

	private int rememberedCount(int index) {
		return completedQuantities[index * 3] + completedQuantities[index * 3 + 1] + completedQuantities[index * 3 + 2];
	}

	private void reconcileOutput(int index, int available) {
		int excess = Math.max(0, rememberedCount(index) - available);
		for (int stage = 0; stage < 3 && excess > 0; stage++) {
			int removed = Math.min(excess, completedQuantities[index * 3 + stage]);
			completedQuantities[index * 3 + stage] -= removed;
			excess -= removed;
		}
	}

	private void rememberOutput(int index, int slot, ItemStack result) {
		if (result.isEmpty()) return;
		if (!ItemStack.isSameItemSameComponents(completedOutputs.get(index), inventory.get(slot)))
			reconcileOutput(index, 0);
		else reconcileOutput(index, inventory.get(slot).getCount());
		completedOutputs.set(index, result.copyWithCount(1));
		completedQuantities[index * 3 + batchStage] += result.getCount();
	}

	public void onPlayerExtract(ServerPlayer player, int slot, ItemStack extracted) {
		int index = slot >= 10 && slot < 18 ? slot - 10 : -1;
		if (riteLocked || index < 0 || extracted.isEmpty()
				|| !ItemStack.isSameItemSameComponents(completedOutputs.get(index), extracted)) return;
		int before = inventory.get(slot).getCount() + extracted.getCount();
		// Unobserved hopper removals spend old results without granting personal practice.
		reconcileOutput(index, before);
		int unknown = Math.max(0, before - rememberedCount(index));
		int remaining = Math.max(0, extracted.getCount() - unknown);
		boolean recovered = false;
		boolean calibrated = false;
		for (int stage = 0; stage < 3 && remaining > 0; stage++) {
			int taken = Math.min(remaining, completedQuantities[index * 3 + stage]);
			completedQuantities[index * 3 + stage] -= taken;
			remaining -= taken;
			recovered |= taken > 0;
			calibrated |= taken > 0 && stage >= 1;
		}
		if (rememberedCount(index) == 0) completedOutputs.set(index, ItemStack.EMPTY);
		if (recovered) {
			var progress = HemoCapabilityAccess.stationUpgrades(player);
			progress.recordUse(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE);
			if (calibrated) progress.recordUse(UpgradeStation.CENTRIFUGE, StationUpgradeCatalog.SEPARATE_CALIBRATED);
			progress.sync(player, false);
		}
		setChanged();
	}

	private boolean balanced() {
		return checkBalancedSpots(2, 6) && checkBalancedSpots(3, 7)
				&& checkBalancedSpots(4, 8) && checkBalancedSpots(9, 5);
	}

	public boolean isProcessing() { return isSpinning() || pendingBatch; }

	public List<ItemStack> getVialSlots() {
		return inventory.subList(2, 10);
	}

	public List<ItemStack> getOutputSlots() {
		return inventory.subList(10, 18);
	}

	public int insertVialsFromRack(ItemStack rackStack) {
		if (riteLocked || isProcessing() || !(rackStack.getItem() instanceof VialRackItem)) {
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
				rackVials.set(i, ItemStack.EMPTY);
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

	private List<ItemStack> vialFractions(EntityType<?> sampledMob) {
		if (sampledMob == null || sampledMob == EntityType.WARDEN
				|| !(sampledMob.create(level) instanceof LivingEntity living)) return List.of();
		var profile = BloodProfileData.profile(sampledMob, level.isClientSide);
		List<ItemStack> outputs = new ArrayList<>();
		if (profile.properties().contains(EntityBloodProfile.FUNGAL))
			outputs.add(new ItemStack(BlockInit.infected_fungus.get()));
		for (var tendency : profile.tendencies())
			outputs.add(new ItemStack(EnumBloodTendency.getRepEnzyme(tendency)));
		// Keep the existing health-based base yield, using the server's random source.
		for (var output : outputs) {
			int amount = (int) ((living.getMaxHealth() / 10) * level.random.nextInt(4) + 1);
			output.setCount(Math.min(output.getMaxStackSize(), amount));
		}
		return outputs;
	}

	public ItemStack getResultFromVial(EntityType<?> sampledMob) {
		var outputs = vialFractions(sampledMob);
		if (outputs.isEmpty()) return ItemStack.EMPTY;
		var result = outputs.get(level.random.nextInt(outputs.size())).copy();
		result.setCount(Math.min(result.getMaxStackSize(),
				VialCentrifugeYieldRules.quantity(result.getCount(), upgradeTier(), level.random.nextDouble())));
		return result;
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

	public VialCentrifugeStartupResult attemptStartup(@Nullable ServerPlayer player) {
		if (riteLocked) return VialCentrifugeStartupResult.RITE_LOCKED;
		if (drainLegacyRecovery()) sendUpdates();
		if (legacyRecovery.stream().anyMatch(stack -> !stack.isEmpty()))
			return startup(VialCentrifugeStartupResult.BLOCKED_ENZYME_OUTPUT);
		if (isSpinning()) return startup(VialCentrifugeStartupResult.ALREADY_RUNNING);
		if (pendingBatch) {
			var result = validateBatch();
			if (result == VialCentrifugeStartupResult.SUCCESS && outputResults()) addOperationBlood();
			return startup(result);
		}
		if (!balanced()) return startup(VialCentrifugeStartupResult.IMBALANCE);
		boolean hasSample = false;
		batchStage = upgradeTier();
		batchInputs = emptyBatch();
		batchPrimary = emptyBatch();
		batchSecondary = emptyBatch();
		batchPowder = 0;
		boolean assignmentSpin = player != null && FirstSeparationAssignment.canBeginAssignmentSpin(player);
		for (int i = 0; i < 8; i++) {
			var sample = inventory.get(i + 2);
			if (sample.isEmpty()) continue;
			if (sample.getCount() != 1) return startup(VialCentrifugeStartupResult.BLOCKED_VIAL_RETURN);
			ItemStack primary = ItemStack.EMPTY;
			if (sample.getItem() instanceof BloodVialItem) {
				var fractions = vialFractions(BloodVialItem.getEntityType(sample));
				if (fractions.isEmpty()) return startup(VialCentrifugeStartupResult.INVALID_SAMPLE);
				int choice = level.random.nextInt(fractions.size());
				primary = fractions.remove(choice);
				primary.setCount(Math.min(primary.getMaxStackSize(),
						VialCentrifugeYieldRules.quantity(primary.getCount(), batchStage, level.random.nextDouble())));
				if (VialCentrifugeYieldRules.secondFraction(batchStage, fractions.size() + 1, level.random.nextDouble()))
					batchSecondary.set(i, fractions.get(level.random.nextInt(fractions.size())).copyWithCount(1));
				if (VialCentrifugeYieldRules.powder(batchStage, level.random.nextDouble())) batchPowder++;
			} else if (sample.getItem() instanceof ConsecratedSyringeItem) {
				var saint = ConsecratedSyringeItem.getSaintType(sample);
				if (saint != null) primary = getResultFromSyringe(saint);
			}
			if (primary.isEmpty()) return startup(VialCentrifugeStartupResult.INVALID_SAMPLE);
			if (assignmentSpin && getOutputSlots().stream().anyMatch(stack -> !stack.isEmpty()))
				return startup(VialCentrifugeStartupResult.BLOCKED_ENZYME_OUTPUT);
			batchInputs.set(i, sample.copy());
			batchPrimary.set(i, primary);
			hasSample = true;
		}
		if (!hasSample) return startup(VialCentrifugeStartupResult.NO_PROCESSABLE_SAMPLES);
		var result = validateBatch();
		if (result != VialCentrifugeStartupResult.SUCCESS) return startup(result);
		if (player != null) {
			assignmentSpinId = FirstSeparationAssignment.beginAssignmentSpin(player);
			assignmentPlayerId = assignmentSpinId == null ? null : player.getUUID();
		}
		for (int i = 0; i < 8; i++)
			if (batchInputs.get(i).getItem() instanceof BloodVialItem)
				FirstSeparationAssignment.markAssignmentOutput(batchPrimary.get(i), assignmentPlayerId, assignmentSpinId);
		result = validateBatch();
		if (result != VialCentrifugeStartupResult.SUCCESS) return startup(result);
		pendingBatch = true;
		spinningTotalTime = VialCentrifugeYieldRules.duration(batchStage);
		spinningProgress = spinningTotalTime;
		return startup(VialCentrifugeStartupResult.SUCCESS);
	}

	private VialCentrifugeStartupResult startup(VialCentrifugeStartupResult result) {
		startupResultId = result.ordinal();
		sendUpdates();
		return result;
	}

	private boolean canFitOutput(int outputSlot, ItemStack resultStack) {
		if (resultStack.isEmpty()) return true;
		ItemStack outputStack = inventory.get(outputSlot);
		if (outputStack.isEmpty()) return resultStack.getCount() <= resultStack.getMaxStackSize();
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
		if (!riteLocked && !isProcessing()) {
			this.inventory.clear();
			legacyRecovery = emptyBatch();
			legacyRecoveryQuantities = new int[24];
		}

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
		return saveWithoutMetadata(registries);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		loadAdditional(tag, registries);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.inventory) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return legacyRecovery.stream().allMatch(ItemStack::isEmpty);
	}

	@Override
	protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
		super.loadAdditional(pTag, registries);
		this.spinningProgress = pTag.getInt("SpinTime");
		this.spinningTotalTime = pTag.getInt("SpinTimeTotal");
		this.startupResultId = pTag.getInt("StartupResult");
		this.assignmentSpinId = pTag.hasUUID("AssignmentSpin") ? pTag.getUUID("AssignmentSpin") : null;
		this.assignmentPlayerId = pTag.hasUUID("AssignmentPlayer") ? pTag.getUUID("AssignmentPlayer") : null;
		var savedInventory = NonNullList.withSize(28, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(pTag, savedInventory, registries);
		this.inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
		for (int i = 0; i < INVENTORY_SIZE; i++) inventory.set(i, savedInventory.get(i));
		riteLocked = pTag.getBoolean("RiteLocked");
		if (pTag.hasUUID("MachineIdentity")) machineIdentity = pTag.getUUID("MachineIdentity");
		lastUpgradeRite = pTag.hasUUID("LastUpgradeRite") ? pTag.getUUID("LastUpgradeRite") : null;
		pendingBatch = pTag.contains("PendingBatch");
		var batch = pTag.getCompound("PendingBatch");
		batchInputs = loadStacks(batch.getCompound("Inputs"), registries);
		batchPrimary = loadStacks(batch.getCompound("Primary"), registries);
		batchSecondary = loadStacks(batch.getCompound("Secondary"), registries);
		batchStage = batch.getInt("Stage");
		batchPowder = batch.getInt("Powder");
		var savedCompleted = NonNullList.withSize(16, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(pTag.getCompound("CompletedOutputs"), savedCompleted, registries);
		var savedQuantities = Arrays.copyOf(pTag.getIntArray("CompletedQuantities"), 48);
		completedOutputs = emptyBatch();
		for (int i = 0; i < 8; i++) completedOutputs.set(i, savedCompleted.get(i));
		completedQuantities = Arrays.copyOf(savedQuantities, 24);
		legacyRecovery = loadStacks(pTag.getCompound("LegacyRecovery"), registries);
		legacyRecoveryQuantities = Arrays.copyOf(pTag.getIntArray("LegacyRecoveryQuantities"), 24);
		for (int i = 0; i < 8; i++) {
			var old = savedInventory.get(20 + i);
			if (old.isEmpty()) continue;
			legacyRecovery.set(i, old);
			if (ItemStack.isSameItemSameComponents(savedCompleted.get(i + 8), old)) {
				int excess = Math.max(0, savedQuantities[(i + 8) * 3] + savedQuantities[(i + 8) * 3 + 1]
						+ savedQuantities[(i + 8) * 3 + 2] - old.getCount());
				for (int stage = 0; stage < 3; stage++) {
					int count = Math.max(0, savedQuantities[(i + 8) * 3 + stage]);
					int removed = Math.min(excess, count);
					legacyRecoveryQuantities[i * 3 + stage] = count - removed;
					excess -= removed;
				}
			}
		}
		drainLegacyRecovery();
		// Old saves lack rolled output; leave their samples intact and allow a fresh startup.
		if (!pendingBatch) spinningProgress = 0;
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setBloodVolume(pTag.getDouble(TAG_BLOOD_LEVEL));
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		if (pkt.getTag() != null) loadAdditional(pkt.getTag(), registries);
	}

	@Override
	public void onLoad() {
		super.onLoad();
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			vol.setActive(true);
			vol.setMaxBloodVolume(2000f);
		}
	}

	@Override
	public ItemStack removeItem(int pSlot, int pAmount) {
		if (riteLocked || isProcessing() && pSlot >= 2 && pSlot <= 9) return ItemStack.EMPTY;
		var removed = ContainerHelper.removeItem(this.inventory, pSlot, pAmount);
		setChanged();
		return removed;
	}

	@Override
	public ItemStack removeItemNoUpdate(int pSlot) {
		if (riteLocked || isProcessing() && pSlot >= 2 && pSlot <= 9) return ItemStack.EMPTY;
		var removed = ContainerHelper.takeItem(this.inventory, pSlot);
		setChanged();
		return removed;
	}

	// NBT and Data
	@Override
	protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
		super.saveAdditional(pTag, registries);
		pTag.putInt("SpinTime", this.spinningProgress);
		pTag.putInt("SpinTimeTotal", this.spinningTotalTime);
		pTag.putInt("StartupResult", this.startupResultId);
		if (assignmentSpinId != null) pTag.putUUID("AssignmentSpin", assignmentSpinId);
		if (assignmentPlayerId != null) pTag.putUUID("AssignmentPlayer", assignmentPlayerId);
		ContainerHelper.saveAllItems(pTag, this.inventory, registries);
		pTag.putBoolean("RiteLocked", riteLocked);
		pTag.putUUID("MachineIdentity", machineIdentity);
		if (lastUpgradeRite != null) pTag.putUUID("LastUpgradeRite", lastUpgradeRite);
		pTag.put("CompletedOutputs", saveStacks(completedOutputs, registries));
		pTag.putIntArray("CompletedQuantities", completedQuantities);
		pTag.put("LegacyRecovery", saveStacks(legacyRecovery, registries));
		pTag.putIntArray("LegacyRecoveryQuantities", legacyRecoveryQuantities);
		if (pendingBatch) {
			var batch = new CompoundTag();
			batch.put("Inputs", saveStacks(batchInputs, registries));
			batch.put("Primary", saveStacks(batchPrimary, registries));
			batch.put("Secondary", saveStacks(batchSecondary, registries));
			batch.putInt("Stage", batchStage);
			batch.putInt("Powder", batchPowder);
			pTag.put("PendingBatch", batch);
		}
		IBloodVolume vol = resolveVolume();
		if (vol != null) {
			pTag.putDouble(TAG_BLOOD_LEVEL, vol.getBloodVolume());
		}
	}

	public void sendUpdates() {
		if (level == null) { setChanged(); return; }
		level.setBlocksDirty(worldPosition, getBlockState(), getBlockState());
		level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		setChanged();
	}

	@Override
	public void setItem(int pSlot, ItemStack pStack) {
		if (riteLocked || isProcessing() && pSlot >= 2 && pSlot <= 9) return;
		this.inventory.set(pSlot, pStack);
		setChanged();
		if (pStack.getCount() > this.getMaxStackSize()) {
			pStack.setCount(this.getMaxStackSize());
		}
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (riteLocked) return false;
		if (slot == SLOT_BLOOD) return stack.getItem() instanceof BloodyFlaskItem;
		return slot >= 2 && slot <= 9 && !isProcessing()
				&& (stack.getItem() instanceof BloodVialItem || stack.getItem() instanceof ConsecratedSyringeItem);
	}

	@Override
	public boolean canTakeItem(Container destination, int slot, ItemStack stack) {
		return !riteLocked && !(isProcessing() && slot >= 2 && slot <= 9);
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return (this.level.getBlockEntity(this.worldPosition) != this) ? false
				: pPlayer.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D,
						this.worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	private static CompoundTag saveStacks(NonNullList<ItemStack> stacks, HolderLookup.Provider registries) {
		var tag = new CompoundTag();
		ContainerHelper.saveAllItems(tag, stacks, registries);
		return tag;
	}

	private static NonNullList<ItemStack> loadStacks(CompoundTag tag, HolderLookup.Provider registries) {
		var stacks = emptyBatch();
		ContainerHelper.loadAllItems(tag, stacks, registries);
		return stacks;
	}

	private boolean drainLegacyRecovery() {
		if (riteLocked || legacyRecovery.stream().allMatch(ItemStack::isEmpty)) return false;
		boolean changed = false;
		var outputs = copyOutputs();
		for (int source = 0; source < 8; source++) {
			final int entry = source;
			var stack = legacyRecovery.get(source);
			if (stack.isEmpty()) continue;
			int[] unknown = {Math.max(0, stack.getCount() - legacyRecoveryQuantities[source * 3]
					- legacyRecoveryQuantities[source * 3 + 1] - legacyRecoveryQuantities[source * 3 + 2])};
			var remaining = insertFraction(outputs, stack, (index, moved) -> {
				int slot = index + 10;
				if (!ItemStack.isSameItemSameComponents(completedOutputs.get(index), inventory.get(slot)))
					reconcileOutput(index, 0);
				else reconcileOutput(index, inventory.get(slot).getCount());
				int uncredited = Math.min(unknown[0], moved.getCount());
				unknown[0] -= uncredited;
				int credit = moved.getCount() - uncredited;
				for (int stage = 0; stage < 3; stage++) {
					int taken = Math.min(credit, legacyRecoveryQuantities[entry * 3 + stage]);
					legacyRecoveryQuantities[entry * 3 + stage] -= taken;
					completedQuantities[index * 3 + stage] += taken;
					credit -= taken;
				}
				if (rememberedCount(index) > 0) completedOutputs.set(index, moved.copyWithCount(1));
				appendOutput(slot, moved);
			});
			changed |= remaining.getCount() != stack.getCount();
			legacyRecovery.set(source, remaining);
		}
		return changed;
	}

	public void dropLegacyRecovery(Level level, BlockPos pos) {
		for (var stack : legacyRecovery)
			net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
		legacyRecovery = emptyBatch();
		legacyRecoveryQuantities = new int[24];
	}

	@Override public UpgradeStation upgradeStation() { return UpgradeStation.CENTRIFUGE; }
	@Override public UUID machineIdentity() { return machineIdentity; }
	@Override public boolean isRiteLocked() { return riteLocked; }
	@Override public void setRiteLocked(boolean locked) { riteLocked = locked; sendUpdates(); }
	@Override public boolean canReceiveBlood() { return !riteLocked; }
	@Override public boolean canProvideBlood() { return !riteLocked; }
	@Override public boolean readyForUpgradeRite(ServerLevel level, Direction forward) {
		return !riteLocked && !isProcessing() && legacyRecovery.stream().allMatch(ItemStack::isEmpty) && getBlockState().getValue(
				com.vincenthuto.hemomancy.common.block.harbinger.crafting.VialCentrifugeBlock.FACING) == forward;
	}
	@Override public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
		var tag = saveWithoutMetadata(registries);
		tag.remove("RiteLocked");
		tag.remove("LastUpgradeRite");
		tag.remove("neoforge:attachments");
		return tag;
	}
	@Override public boolean wasUpgradedBy(UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
	@Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; sendUpdates(); }

}
