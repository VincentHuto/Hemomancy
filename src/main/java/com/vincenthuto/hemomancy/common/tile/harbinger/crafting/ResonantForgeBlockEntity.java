package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeTier;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeTransfer;
import com.vincenthuto.hemomancy.common.enchanting.ResonantPattern;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.WorldlyContainer;
import net.minecraft.core.Direction;

import java.util.UUID;

public class ResonantForgeBlockEntity extends BaseContainerBlockEntity implements IBloodReservoir, WorldlyContainer {
    private static final int[] NO_AUTOMATION_SLOTS = new int[0];
    public static final int APPLICATION_ITEM = 0;
    public static final int APPLICATION_CYLINDER = 1;
    public static final int APPLICATION_OUTPUT = 2;
    public static final int GRINDING_ITEM = 3;
    public static final int GRINDING_CYLINDER = 4;
    public static final int GRINDING_EQUIPMENT_OUTPUT = 5;
    public static final int GRINDING_CYLINDER_OUTPUT = 6;
    public static final int SLOT_COUNT = 7;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private ResonantForgeTier tier = ResonantForgeTier.BASE;
    private Operation operation = Operation.NONE;
    private int progress;
    private int totalTicks;
    private int reservedBlood;
    private int hammerUses;
    private int wheelUses;
    private String selection = "";
    private boolean masterMode;
    private boolean hammerIron;
    private boolean hammerAsh;
    private double hammerRepairBlood;
    private boolean wheelAsh;
    private boolean riteLocked;
    private boolean needsFootprintCleanup = true;
    private UUID machineIdentity = UUID.randomUUID();
    private UUID lastUpgradeRite;
    private Status status = Status.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> totalTicks;
                case 2 -> (int) getBloodVolume();
                case 3 -> (int) getMaxBloodVolume();
                case 4 -> hammerUses;
                case 5 -> wheelUses;
                case 6 -> tier.ordinal();
                case 7 -> operation.ordinal();
                case 8 -> status.ordinal();
                case 9 -> masterMode ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) progress = value;
            else if (index == 1) totalTicks = value;
        }
        @Override public int getCount() { return 10; }
    };

    public ResonantForgeBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.resonant_forge.get(), pos, state);
    }

    @Override public com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume getBloodCapability() {
        var volume = getData(com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes.BLOCK_BLOOD_VOLUME);
        volume.setActive(true);
        volume.setMaxBloodVolume(ResonantForgeRules.BLOOD_CAPACITY);
        return volume;
    }

    @Override public void onLoad() { super.onLoad(); getBloodCapability(); }
    @Override public boolean canReceiveBlood() { return !riteLocked; }
    @Override public boolean canProvideBlood() { return !riteLocked && operation == Operation.NONE; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResonantForgeBlockEntity forge) {
        if (forge.needsFootprintCleanup) {
            forge.needsFootprintCleanup = false;
            BlockPos above = pos.above();
            if (level.getBlockState(above).is(BlockInit.filler_block.get())
                    && level.getBlockEntity(above) instanceof FillerBlockEntity filler
                    && pos.equals(filler.getMainBlockPos())) {
                // Unlink first: removing a linked filler also destroys its controller.
                filler.setMainBlockPos(null);
                level.setBlockAndUpdate(above, level.getFluidState(above).createLegacyBlock());
            }
        }
        if (forge.operation == Operation.NONE || forge.riteLocked) return;
        forge.progress++;
        if (forge.progress >= forge.totalTicks) forge.completeOperation();
        else if ((forge.progress & 7) == 0) forge.sync();
    }

    public boolean startApply() {
        if (!idle()) return false;
        if (hammerWorn()) return fail(Status.HAMMER_WORN);
        if (!items.get(APPLICATION_OUTPUT).isEmpty()) return fail(Status.OUTPUT_BLOCKED);
        ItemStack target = items.get(APPLICATION_ITEM);
        ItemStack cylinder = items.get(APPLICATION_CYLINDER);
        ResonantPattern pattern = cylinder.get(DataComponentInit.RESONANT_PATTERN.get());
        if (target.isEmpty() || !cylinder.is(ItemInit.ambergris_cylinder.get()) || pattern == null)
            return fail(Status.MISSING_INPUT);
        if (pattern.master() && !ResonantForgeRules.canUseMaster(tier)) return fail(Status.MASTER_REQUIRES_D7);
        var preview = ResonantForgeTransfer.apply(target, pattern, level.registryAccess());
        if (!preview.success()) return fail(map(preview.failure()));
        int cost = ResonantForgeRules.hammeringCost(pattern);
        return begin(Operation.APPLY, ResonantForgeRules.OPERATION_TICKS, cost);
    }

    public boolean startGrinding() {
        if (!idle()) return false;
        if (wheelWorn()) return fail(Status.WHEEL_WORN);
        if (!items.get(GRINDING_EQUIPMENT_OUTPUT).isEmpty() || !items.get(GRINDING_CYLINDER_OUTPUT).isEmpty())
            return fail(Status.OUTPUT_BLOCKED);
        ItemStack source = items.get(GRINDING_ITEM);
        ItemStack cylinder = items.get(GRINDING_CYLINDER);
        if (source.isEmpty() || !cylinder.is(ItemInit.ambergris_cylinder.get())) return fail(Status.MISSING_INPUT);
        if (!ResonantForgeRules.isBlankCylinder(cylinder.has(DataComponentInit.ANCIENT_RECORDING.get()),
                cylinder.has(DataComponentInit.CLAIRAUDIOGRAPH_RECORDING.get()),
                cylinder.has(DataComponentInit.RESONANT_PATTERN.get()))) return fail(Status.CYLINDER_NOT_BLANK);
        if (!selection.isBlank() && !ResonantForgeRules.canSelectIndividual(tier)) return fail(Status.SELECTION_REQUIRES_D5);
        if (masterMode && !ResonantForgeRules.canUseMaster(tier)) return fail(Status.MASTER_REQUIRES_D7);
        var preview = ResonantForgeTransfer.capture(source, level.registryAccess(), selection, masterMode);
        if (!preview.success()) return fail(Status.NO_PATTERN);
        int cost = ResonantForgeRules.grindingCost(preview.pattern())
                + (masterMode ? ResonantForgeRules.MASTER_SURCHARGE : 0);
        return begin(Operation.GRIND, masterMode ? ResonantForgeRules.MASTER_OPERATION_TICKS
                : ResonantForgeRules.OPERATION_TICKS, cost);
    }

    public boolean startStabilizing() {
        if (!idle()) return false;
        if (wheelWorn()) return fail(Status.WHEEL_WORN);
        if (!ResonantForgeRules.canUseMaster(tier)) return fail(Status.MASTER_REQUIRES_D7);
        if (!items.get(GRINDING_CYLINDER_OUTPUT).isEmpty()) return fail(Status.OUTPUT_BLOCKED);
        ItemStack cylinder = items.get(GRINDING_CYLINDER);
        ResonantPattern pattern = cylinder.get(DataComponentInit.RESONANT_PATTERN.get());
        if (!cylinder.is(ItemInit.ambergris_cylinder.get()) || pattern == null || pattern.isEmpty() || pattern.master())
            return fail(Status.MISSING_INPUT);
        return begin(Operation.STABILIZE, ResonantForgeRules.MASTER_OPERATION_TICKS,
                ResonantForgeRules.MASTER_SURCHARGE);
    }

    private boolean begin(Operation next, int ticks, int blood) {
        var volume = getBloodCapability();
        if (volume.getBloodVolume() < blood || !volume.drain(blood)) return fail(Status.INSUFFICIENT_BLOOD);
        operation = next;
        progress = 0;
        totalTicks = ticks;
        reservedBlood = blood;
        status = Status.WORKING;
        sync();
        return true;
    }

    private void completeOperation() {
        boolean success = switch (operation) {
            case APPLY -> finishApply();
            case GRIND -> finishGrinding();
            case STABILIZE -> finishStabilizing();
            default -> false;
        };
        if (!success) refundAndReset(Status.INPUT_CHANGED);
        else {
            operation = Operation.NONE;
            progress = totalTicks = reservedBlood = 0;
            status = Status.IDLE;
            sync();
        }
    }

    private boolean finishApply() {
        ItemStack target = items.get(APPLICATION_ITEM);
        ItemStack cylinder = items.get(APPLICATION_CYLINDER);
        ResonantPattern pattern = cylinder.get(DataComponentInit.RESONANT_PATTERN.get());
        if (target.isEmpty() || pattern == null || !items.get(APPLICATION_OUTPUT).isEmpty()) return false;
        var result = ResonantForgeTransfer.apply(target, pattern, level.registryAccess());
        if (!result.success()) return false;
        items.set(APPLICATION_OUTPUT, result.equipment());
        items.set(APPLICATION_ITEM, ItemStack.EMPTY);
        if (!pattern.master()) items.set(APPLICATION_CYLINDER, ItemStack.EMPTY);
        hammerUses++;
        return true;
    }

    private boolean finishGrinding() {
        ItemStack source = items.get(GRINDING_ITEM);
        ItemStack cylinder = items.get(GRINDING_CYLINDER);
        if (source.isEmpty() || cylinder.isEmpty() || !items.get(GRINDING_EQUIPMENT_OUTPUT).isEmpty()
                || !items.get(GRINDING_CYLINDER_OUTPUT).isEmpty()) return false;
        var result = ResonantForgeTransfer.capture(source, level.registryAccess(), selection, masterMode);
        if (!result.success()) return false;
        ItemStack recorded = cylinder.copy();
        recorded.setCount(1);
        recorded.set(DataComponentInit.RESONANT_PATTERN.get(), result.pattern());
        items.set(GRINDING_EQUIPMENT_OUTPUT, result.equipment());
        items.set(GRINDING_CYLINDER_OUTPUT, recorded);
        items.set(GRINDING_ITEM, ItemStack.EMPTY);
        items.set(GRINDING_CYLINDER, ItemStack.EMPTY);
        wheelUses++;
        return true;
    }

    private boolean finishStabilizing() {
        ItemStack cylinder = items.get(GRINDING_CYLINDER);
        ResonantPattern pattern = cylinder.get(DataComponentInit.RESONANT_PATTERN.get());
        if (pattern == null || pattern.master() || !items.get(GRINDING_CYLINDER_OUTPUT).isEmpty()) return false;
        ItemStack result = cylinder.copy();
        result.set(DataComponentInit.RESONANT_PATTERN.get(), pattern.asMaster());
        items.set(GRINDING_CYLINDER_OUTPUT, result);
        items.set(GRINDING_CYLINDER, ItemStack.EMPTY);
        wheelUses++;
        return true;
    }

    public boolean cancelOperation() {
        if (operation == Operation.NONE) return false;
        refundAndReset(Status.IDLE);
        return true;
    }

    private void refundAndReset(Status nextStatus) {
        if (reservedBlood > 0) getBloodCapability().fill(reservedBlood);
        operation = Operation.NONE;
        progress = totalTicks = reservedBlood = 0;
        status = nextStatus;
        sync();
    }

    public boolean toggleMasterMode() {
        if (!idle() || !ResonantForgeRules.canUseMaster(tier)) return false;
        masterMode = !masterMode;
        sync();
        return true;
    }

    public boolean setSelection(String value) {
        if (!idle() || (!value.isBlank() && !ResonantForgeRules.canSelectIndividual(tier))) return false;
        selection = value == null ? "" : value;
        sync();
        return true;
    }

    public boolean depositHammerIron(ItemStack held) {
        if (!hammerWorn() || hammerIron || !held.is(com.vincenthuto.hemomancy.common.init.BlockInit.hematic_iron_block.get().asItem())) return false;
        held.shrink(1); hammerIron = true; sync(); return true;
    }

    public boolean depositHammerAsh(ItemStack held) {
        if (!hammerWorn() || hammerAsh || !held.is(com.vincenthuto.hemomancy.common.init.BlockInit.smouldering_ash_trail.get().asItem())) return false;
        held.shrink(1); hammerAsh = true; sync(); return true;
    }

    public double receiveHammerRepairBlood(double amount) {
        if (!hammerWorn() || !hammerIron || !hammerAsh || amount <= 0) return 0;
        double accepted = Math.min(amount, ResonantForgeRules.HAMMER_REPAIR_BLOOD - hammerRepairBlood);
        hammerRepairBlood += accepted;
        if (hammerRepairBlood >= ResonantForgeRules.HAMMER_REPAIR_BLOOD) {
            hammerIron = hammerAsh = false;
            hammerRepairBlood = 0;
            hammerUses = 0;
        }
        sync();
        return accepted;
    }

    public boolean depositWheelAsh(ItemStack held) {
        if (!wheelWorn() || wheelAsh || !held.is(com.vincenthuto.hemomancy.common.init.BlockInit.befouling_ash_trail.get().asItem())) return false;
        held.shrink(1); wheelAsh = true; sync(); return true;
    }

    public boolean redressWheel(Player player, net.minecraft.world.InteractionHand hand, ItemStack scalpel) {
        if (!wheelWorn() || !wheelAsh || !scalpel.is(ItemInit.vivianite_scalpel.get())) return false;
        scalpel.hurtAndBreak(ResonantForgeRules.SCALPEL_REPAIR_DAMAGE, player,
                net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
        wheelAsh = false;
        wheelUses = 0;
        sync();
        return true;
    }

    public boolean hammerWorn() { return ResonantForgeRules.hammerServiceRequired(hammerUses); }
    public boolean wheelWorn() { return ResonantForgeRules.wheelServiceRequired(wheelUses); }
    public boolean idle() { return operation == Operation.NONE && !riteLocked; }
    public ResonantForgeTier tier() { return tier; }
    public Operation operation() { return operation; }
    public int progress() { return progress; }
    public int totalTicks() { return totalTicks; }
    public int hammerUses() { return hammerUses; }
    public int wheelUses() { return wheelUses; }
    public String selection() { return selection; }
    public boolean masterMode() { return masterMode; }
    public boolean hammerIronDeposited() { return hammerIron; }
    public boolean hammerAshDeposited() { return hammerAsh; }
    public double hammerRepairBlood() { return hammerRepairBlood; }
    public boolean wheelAshDeposited() { return wheelAsh; }
    public boolean isRiteLocked() { return riteLocked; }
    public UUID machineIdentity() { return machineIdentity; }
    public boolean wasUpgradedBy(UUID id) { return id != null && id.equals(lastUpgradeRite); }

    public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("RiteLocked");
        tag.remove("LastUpgradeRite");
        tag.remove("neoforge:attachments");
        return tag;
    }

    public void setRiteLocked(boolean value) { riteLocked = value; sync(); }
    public boolean completeUpgrade(ResonantForgeTier target, UUID riteId) {
        if (target == null || riteId == null || target.ordinal() != tier.ordinal() + 1 || operation != Operation.NONE) return false;
        tier = target; lastUpgradeRite = riteId; riteLocked = false; sync(); return true;
    }

    private boolean fail(Status failure) { status = failure; sync(); return false; }
    private static Status map(ResonantForgeTransfer.Failure failure) {
        return switch (failure) {
            case UNSUPPORTED_ITEM -> Status.UNSUPPORTED_ENCHANTMENT;
            case UNSUPPORTED_ENCHANTMENT -> Status.UNSUPPORTED_ENCHANTMENT;
            case INCOMPATIBLE_ENCHANTMENTS -> Status.INCOMPATIBLE_ENCHANTMENTS;
            case DIFFERENT_SCRIPTORIUM_CURSE -> Status.DIFFERENT_CURSE;
            case UNSUPPORTED_SCRIPTORIUM_CURSE -> Status.UNSUPPORTED_CURSE;
            case NO_CHANGE -> Status.NO_CHANGE;
            default -> Status.NO_PATTERN;
        };
    }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override public void setItem(int slot, ItemStack stack) {
        if (!idle()) return;
        items.set(slot, stack);
        sync();
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        if (!idle()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.removeItem(items, slot, amount); sync(); return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (!idle()) return ItemStack.EMPTY;
        ItemStack result = ContainerHelper.takeItem(items, slot); sync(); return result;
    }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public int[] getSlotsForFace(Direction side) { return NO_AUTOMATION_SLOTS; }
    @Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) { return false; }
    @Override protected NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(NonNullList<ItemStack> items) { this.items = items; }
    @Override protected Component getDefaultName() { return Component.translatable("container.hemomancy.resonant_forge"); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new ResonantForgeMenu(id, inventory, this, data); }
    @Override public boolean stillValid(Player player) {
        return !hammerWorn() && !isRemoved() && player.distanceToSqr(worldPosition.getX() + .5,
                worldPosition.getY() + .5, worldPosition.getZ() + .5) <= 64;
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        tier = ResonantForgeTier.values()[Math.clamp(tag.getInt("Tier"), 0, ResonantForgeTier.values().length - 1)];
        operation = Operation.values()[Math.clamp(tag.getInt("Operation"), 0, Operation.values().length - 1)];
        progress = tag.getInt("Progress"); totalTicks = tag.getInt("TotalTicks"); reservedBlood = tag.getInt("ReservedBlood");
        hammerUses = tag.getInt("HammerUses"); wheelUses = tag.getInt("WheelUses"); selection = tag.getString("Selection");
        masterMode = tag.getBoolean("MasterMode"); hammerIron = tag.getBoolean("HammerIron"); hammerAsh = tag.getBoolean("HammerAsh");
        hammerRepairBlood = tag.getDouble("HammerRepairBlood");
        wheelAsh = tag.getBoolean("WheelAsh"); riteLocked = tag.getBoolean("RiteLocked");
        if (tag.hasUUID("MachineIdentity")) machineIdentity = tag.getUUID("MachineIdentity");
        if (tag.hasUUID("LastUpgradeRite")) lastUpgradeRite = tag.getUUID("LastUpgradeRite");
        getBloodCapability().setBloodVolume(Math.clamp(tag.getDouble("BloodVolume"), 0, ResonantForgeRules.BLOOD_CAPACITY));
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Tier", tier.ordinal()); tag.putInt("Operation", operation.ordinal()); tag.putInt("Progress", progress);
        tag.putInt("TotalTicks", totalTicks); tag.putInt("ReservedBlood", reservedBlood); tag.putInt("HammerUses", hammerUses);
        tag.putInt("WheelUses", wheelUses); tag.putString("Selection", selection); tag.putBoolean("MasterMode", masterMode);
        tag.putBoolean("HammerIron", hammerIron); tag.putBoolean("HammerAsh", hammerAsh); tag.putBoolean("WheelAsh", wheelAsh);
        tag.putDouble("HammerRepairBlood", hammerRepairBlood);
        tag.putBoolean("RiteLocked", riteLocked); tag.putUUID("MachineIdentity", machineIdentity);
        if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
        tag.putDouble("BloodVolume", getBloodVolume());
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

    public enum Operation { NONE, APPLY, GRIND, STABILIZE }
    public enum Status {
        IDLE, WORKING, MISSING_INPUT, OUTPUT_BLOCKED, INSUFFICIENT_BLOOD, HAMMER_WORN, WHEEL_WORN,
        CYLINDER_NOT_BLANK, SELECTION_REQUIRES_D5, MASTER_REQUIRES_D7, NO_PATTERN, UNSUPPORTED_ENCHANTMENT,
        INCOMPATIBLE_ENCHANTMENTS, DIFFERENT_CURSE, UNSUPPORTED_CURSE, NO_CHANGE, INPUT_CHANGED
    }
}
