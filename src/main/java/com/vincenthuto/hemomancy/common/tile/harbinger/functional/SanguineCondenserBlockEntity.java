package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.station.StationTierProperty;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import com.vincenthuto.hemomancy.common.station.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Holds projected blood and crystallizes it into Sanguine Formations that wait in the vessel until a player
 * gathers them. Pace, pool and yield follow {@link SanguineCondenserRules} for the installed stage.
 */
public class SanguineCondenserBlockEntity extends BlockEntity implements IBloodReservoir, UpgradeableStation {
    private static final int MAX_CONTRIBUTORS = 16;

    private int formations;
    private int progress;
    @Nullable private UUID owner;
    private int ownerCrystallization;
    /** Set once projection first fills it; only a cauldron that has held blood goes back to plain iron. */
    private boolean everFilled;
    private final Set<UUID> contributors = new LinkedHashSet<>();
    private boolean riteLocked;
    private UUID machineIdentity = UUID.randomUUID();
    @Nullable private UUID lastUpgradeRite;
    private final IItemHandler extraction = new Extraction();

    public SanguineCondenserBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityInit.sanguine_condenser.get(), pos, state);
    }

    public int stage() {
        return StationTierProperty.stage(getBlockState());
    }

    @Override
    public IBloodVolume getBloodCapability() {
        IBloodVolume volume = IBloodReservoir.super.getBloodCapability();
        volume.setActive(true);
        volume.setMaxBloodVolume(SanguineCondenserRules.poolCapacity(stage()));
        return volume;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        getBloodCapability();
    }

    public void claimOwner(ServerPlayer player) {
        owner = player.getUUID();
        ownerCrystallization = SkillPointHelper.getSanguineCrystallizationLevel(player);
        sync();
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public int heldFormations() {
        return formations;
    }

    public boolean isContributor(UUID id) {
        return contributors.contains(id);
    }

    /** Fraction of the current formation already condensed, for the renderer. */
    public float progress() {
        return Math.min(1.0F, progress / (float) SanguineCondenserRules.ticksPerFormation(stage()));
    }

    public IItemHandler extraction() {
        return extraction;
    }

    /** Takes projected blood from {@code player}; the caller drains the player by the returned amount. */
    public double receiveProjectedBlood(ServerPlayer player, double amount) {
        if (riteLocked || amount <= 0.0D) return 0.0D;
        IBloodVolume volume = getBloodCapability();
        double accepted = Math.min(amount, Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume()));
        if (accepted <= 0.0D || !volume.fill(accepted)) return 0.0D;
        everFilled = true;
        if (contributors.size() < MAX_CONTRIBUTORS || contributors.contains(player.getUUID()))
            contributors.add(player.getUUID());
        if (player.getUUID().equals(owner))
            ownerCrystallization = SkillPointHelper.getSanguineCrystallizationLevel(player);
        sync();
        return accepted;
    }

    /** Moves held formations into {@code player}'s inventory; counts only what actually fit. */
    public int collect(ServerPlayer player) {
        if (riteLocked || formations <= 0) return 0;
        Inventory inventory = player.getInventory();
        ItemStack template = new ItemStack(ItemInit.sanguine_formation.get());
        int offered = Math.min(formations, roomFor(inventory, template));
        if (offered <= 0) return 0;
        ItemStack stack = template.copyWithCount(offered);
        inventory.add(stack);
        int moved = offered - stack.getCount();
        if (moved <= 0) return 0;
        formations -= moved;
        var practice = HemoCapabilityAccess.stationUpgrades(player);
        practice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE);
        if (stage() >= 1) practice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE_LATTICED);
        if (isContributor(player.getUUID())) FirstBloodcraftAssignment.recordFormation(player);
        sync();
        revertIfSpent();
        return moved;
    }

    @Override
    public double drainBlood(double amount) {
        double drained = IBloodReservoir.super.drainBlood(amount);
        if (drained > 0.0D) revertIfSpent();
        return drained;
    }

    /**
     * A bare cauldron that has given up all its blood and formations goes back to plain iron. One that has never
     * held blood (freshly seated, or Creative-placed) waits to be filled instead.
     */
    public boolean revertIfSpent() {
        if (level == null || level.isClientSide || riteLocked || !everFilled || stage() != 0 || formations > 0
                || progress > 0 || getBloodVolume() > 1.0E-6D) return false;
        return level.setBlock(worldPosition, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static int roomFor(Inventory inventory, ItemStack template) {
        int room = 0;
        for (ItemStack slot : inventory.items) {
            if (slot.isEmpty()) room += template.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(slot, template))
                room += Math.max(0, slot.getMaxStackSize() - slot.getCount());
        }
        return room;
    }

    public void dropFormations() {
        if (level == null || formations <= 0) return;
        Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                new ItemStack(ItemInit.sanguine_formation.get(), formations));
        formations = 0;
    }

    private boolean condensing() {
        int stage = stage();
        return !riteLocked && formations < SanguineCondenserRules.heldCapacity(stage)
                && getBloodVolume() >= SanguineCondenserRules.formationCost(stage);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SanguineCondenserBlockEntity condenser) {
        if (condenser.condensing()) condenser.progress = Math.min(condenser.progress + 1,
                SanguineCondenserRules.ticksPerFormation(condenser.stage()));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SanguineCondenserBlockEntity condenser) {
        if (condenser.riteLocked) return;
        if (!condenser.condensing()) {
            // Absorption and couriers drain the pool directly, so a spent cauldron is noticed here too.
            condenser.revertIfSpent();
            return;
        }
        int stage = condenser.stage();
        if (++condenser.progress < SanguineCondenserRules.ticksPerFormation(stage)) {
            condenser.setChanged();
            return;
        }
        condenser.progress = 0;
        condenser.getBloodCapability().drain(SanguineCondenserRules.formationCost(stage));
        if (level.random.nextDouble() < SanguineCondenserRules.collapseChance(stage, condenser.ownerCrystallization)) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.55F);
            SanguineCondenserEffects.collapse((ServerLevel) level, condenser);
        } else {
            condenser.formations++;
            if (condenser.formations < SanguineCondenserRules.heldCapacity(stage)
                    && level.random.nextDouble() < SanguineCondenserRules.bonusChance(stage))
                condenser.formations++;
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.7F, 0.8F);
            SanguineCondenserEffects.condensed((ServerLevel) level, condenser);
        }
        condenser.sync();
        condenser.revertIfSpent();
    }

    public void sync() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void sendUpdates() {
        sync();
    }

    // ── UpgradeableStation ─────────────────────────────────────────────────────

    /** Only Blood Projection fills a Condenser ({@link #receiveProjectedBlood}); routing and couriers may not. */
    @Override public boolean canReceiveBlood() { return false; }
    @Override public boolean canProvideBlood() { return !riteLocked; }
    @Override public UpgradeStation upgradeStation() { return UpgradeStation.CONDENSER; }
    @Override public UUID machineIdentity() { return machineIdentity; }
    @Override public boolean isRiteLocked() { return riteLocked; }
    @Override public void setRiteLocked(boolean locked) { riteLocked = locked; sync(); }
    @Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) { return !riteLocked; }
    @Override public boolean wasUpgradedBy(UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
    @Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; sync(); }

    @Override
    public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("RiteLocked");
        tag.remove("LastUpgradeRite");
        tag.remove("Progress");
        tag.remove("neoforge:attachments");
        return tag;
    }

    // ── Persistence ────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Formations", formations);
        tag.putInt("Progress", progress);
        tag.putDouble("BloodVolume", getBloodVolume());
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putInt("OwnerCrystallization", ownerCrystallization);
        tag.putBoolean("EverFilled", everFilled);
        ListTag list = new ListTag();
        contributors.forEach(id -> list.add(NbtUtils.createUUID(id)));
        tag.put("Contributors", list);
        tag.putBoolean("RiteLocked", riteLocked);
        tag.putUUID("MachineIdentity", machineIdentity);
        if (lastUpgradeRite != null) tag.putUUID("LastUpgradeRite", lastUpgradeRite);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        formations = Math.max(0, tag.getInt("Formations"));
        progress = Math.max(0, tag.getInt("Progress"));
        owner = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        ownerCrystallization = tag.getInt("OwnerCrystallization");
        everFilled = tag.getBoolean("EverFilled");
        contributors.clear();
        for (Tag id : tag.getList("Contributors", Tag.TAG_INT_ARRAY)) contributors.add(NbtUtils.loadUUID(id));
        riteLocked = tag.getBoolean("RiteLocked");
        if (tag.hasUUID("MachineIdentity")) machineIdentity = tag.getUUID("MachineIdentity");
        lastUpgradeRite = tag.hasUUID("LastUpgradeRite") ? tag.getUUID("LastUpgradeRite") : null;
        IBloodVolume volume = getBloodCapability();
        volume.setBloodVolume(Math.clamp(tag.getDouble("BloodVolume"), 0.0D, volume.getMaxBloodVolume()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        if (packet.getTag() != null) loadAdditional(packet.getTag(), registries);
    }

    /** Hoppers and pipes may draw finished formations; nothing can be put in. */
    private final class Extraction implements IItemHandler {
        @Override public int getSlots() { return 1; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return formations <= 0 ? ItemStack.EMPTY : new ItemStack(ItemInit.sanguine_formation.get(), formations);
        }

        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int taken = riteLocked ? 0 : Math.min(amount, Math.min(formations, 64));
            if (taken <= 0) return ItemStack.EMPTY;
            if (!simulate) {
                formations -= taken;
                sync();
                revertIfSpent();
            }
            return new ItemStack(ItemInit.sanguine_formation.get(), taken);
        }

        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }
}
