package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.StationTierProperty;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BloodDrawRights;
import com.vincenthuto.hemomancy.common.tile.HeldFormations;
import com.vincenthuto.hemomancy.common.tile.IBloodReservoir;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
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
public class SanguineCondenserBlockEntity extends HemoBlockEntity implements IBloodReservoir, UpgradeableStation {
    private static final int MAX_CONTRIBUTORS = 16;

    /** Formations set and waiting; the shared clot-to-Formation component (core-loop Track 3). */
    private final HeldFormations formations = new HeldFormations();
    private int progress;
    @Nullable private UUID owner;
    private int ownerCrystallization;
    /** Set once projection first fills it; only a cauldron that has held blood goes back to plain iron. */
    private boolean everFilled;
    private final Set<UUID> contributors = new LinkedHashSet<>();
    private boolean riteLocked;
    private UUID machineIdentity = UUID.randomUUID();
    @Nullable private UUID lastUpgradeRite;
    private final IItemHandler extraction = formations.extraction(() -> riteLocked, () -> {
        BlockEntitySync.markAndSync(this);
        revertIfSpent();
    });

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
        com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(this).setOwner(owner);
        ownerCrystallization = SkillPointHelper.getSanguineCrystallizationLevel(player);
        BlockEntitySync.markAndSync(this);
    }

    /**
     * A drip seated this Condenser from {@code owner}'s tank: it is theirs, with their crystallization when they are
     * online and none otherwise. Only {@link SanguineCondenserBlock#convertCauldronByDrip} calls it, on a fresh seat, so
     * a drip never changes an existing Condenser's owner.
     */
    public void claimOwner(UUID owner) {
        this.owner = owner;
        com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(this).setOwner(owner);
        ServerPlayer online = level instanceof net.minecraft.server.level.ServerLevel server
                ? server.getServer().getPlayerList().getPlayer(owner) : null;
        ownerCrystallization = online == null ? 0 : SkillPointHelper.getSanguineCrystallizationLevel(online);
        BlockEntitySync.markAndSync(this);
    }

    @Nullable
    public UUID owner() {
        return owner;
    }

    public int heldFormations() {
        return formations.count();
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

    /** How much of {@code amount} the pool would take now: none while a rite holds it, never past full. */
    public double acceptable(double amount) {
        if (riteLocked || amount <= 0.0D) return 0.0D;
        IBloodVolume volume = getBloodCapability();
        return Math.min(amount, Math.max(0.0D, volume.getMaxBloodVolume() - volume.getBloodVolume()));
    }

    /**
     * One scab drip's drop: it lands and sets as clot for Formations, and counts as the Condenser having been filled,
     * so a drip-made Condenser goes back to a cauldron once spent like any other. Returns what the pool took.
     */
    public double receiveDrip(double amount) {
        double room = acceptable(amount);
        if (room <= 0.0D) return 0.0D;
        com.vincenthuto.hemomancy.common.tile.ReservoirBlood.settled(this).addClot(room);
        everFilled = true;
        BlockEntitySync.markAndSync(this);
        return room;
    }

    /** Takes projected blood from {@code player}, who has already paid it; returns what the pool took. */
    public double receiveProjectedBlood(ServerPlayer player, double amount) {
        double accepted = acceptable(amount);
        IBloodVolume volume = getBloodCapability();
        if (accepted <= 0.0D || !volume.fill(accepted)) return 0.0D;
        everFilled = true;
        if (contributors.size() < MAX_CONTRIBUTORS || contributors.contains(player.getUUID()))
            contributors.add(player.getUUID());
        if (player.getUUID().equals(owner))
            ownerCrystallization = SkillPointHelper.getSanguineCrystallizationLevel(player);
        BlockEntitySync.markAndSync(this);
        return accepted;
    }

    /** Moves held formations into {@code player}'s inventory; counts only what actually fit. */
    public int collect(ServerPlayer player) {
        if (riteLocked || formations.count() <= 0) return 0;
        if (!BloodDrawRights.mayDraw(player, this)) {
            player.displayClientMessage(Component.translatable("message.hemomancy.living_blood.not_yours"), true);
            return 0;
        }
        int moved = formations.collect(player);
        if (moved <= 0) return 0;
        var practice = HemoCapabilityAccess.stationUpgrades(player);
        practice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE);
        if (stage() >= 1) practice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE_LATTICED);
        if (isContributor(player.getUUID())) FirstBloodcraftAssignment.recordFormation(player);
        BlockEntitySync.markAndSync(this);
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
        if (level == null || level.isClientSide || riteLocked || !everFilled || stage() != 0 || formations.count() > 0
                || progress > 0 || condensable() > 1.0E-6D) return false;
        return level.setBlock(worldPosition, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
    }

    public void dropFormations() {
        if (level == null) return;
        formations.drop(level, worldPosition);
    }

    /** The Condenser sets clot as well as fresh blood: it is the deliberate station, at the best yield. */
    private double condensable() {
        IBloodVolume volume = getBloodCapability();
        return volume.getBloodVolume() + (volume instanceof BlockBloodVolume block ? block.clotted() : 0.0D);
    }

    private boolean condensing() {
        int stage = stage();
        return !riteLocked && formations.count() < SanguineCondenserRules.heldCapacity(stage)
                && condensable() >= SanguineCondenserRules.formationCost(stage);
    }

    /** Takes one formation's blood, clot first: what has already set is what condenses. */
    private void takeFormationBlood(double cost) {
        IBloodVolume volume = getBloodCapability();
        double fromClot = volume instanceof BlockBloodVolume block ? block.scrapeClot(cost) : 0.0D;
        if (cost - fromClot > 0.0D) volume.drain(cost - fromClot);
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
        condenser.takeFormationBlood(SanguineCondenserRules.formationCost(stage));
        if (level.random.nextDouble() < SanguineCondenserRules.collapseChance(stage, condenser.ownerCrystallization)) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8F, 0.55F);
            SanguineCondenserEffects.collapse((ServerLevel) level, condenser);
        } else {
            condenser.formations.add(1);
            if (condenser.formations.count() < SanguineCondenserRules.heldCapacity(stage)
                    && level.random.nextDouble() < SanguineCondenserRules.bonusChance(stage))
                condenser.formations.add(1);
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.BLOCKS, 0.7F, 0.8F);
            SanguineCondenserEffects.condensed((ServerLevel) level, condenser);
        }
        BlockEntitySync.markAndSync(condenser);
        condenser.revertIfSpent();
    }

    // ── UpgradeableStation ─────────────────────────────────────────────────────

    /** Only Blood Projection fills a Condenser ({@link #receiveProjectedBlood}); routing and couriers may not. */
    @Override public boolean canReceiveBlood() { return false; }
    @Override public boolean canProvideBlood() { return !riteLocked; }
    @Override public UpgradeStation upgradeStation() { return UpgradeStation.CONDENSER; }
    @Override public UUID machineIdentity() { return machineIdentity; }
    @Override public boolean isRiteLocked() { return riteLocked; }
    @Override public void setRiteLocked(boolean locked) { riteLocked = locked; BlockEntitySync.markAndSync(this); }
    @Override public boolean readyForUpgradeRite(ServerLevel level, Direction riteForward) { return !riteLocked; }
    @Override public boolean wasUpgradedBy(UUID riteId) { return riteId != null && riteId.equals(lastUpgradeRite); }
    @Override public void markUpgradedBy(UUID riteId) { lastUpgradeRite = riteId; BlockEntitySync.markAndSync(this); }

    @Override
    public CompoundTag upgradeSnapshot(HolderLookup.Provider registries) {
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("RiteLocked");
        tag.remove("LastUpgradeRite");
        tag.remove("Progress");
        tag.remove("neoforge:attachments");
        // Blood lives in the attachment; keep it in the snapshot under its old key so a mid-rite change still aborts.
        tag.putDouble("BloodVolume", getBloodVolume());
        return tag;
    }

    // ── Persistence ────────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Formations", formations.count());
        tag.putInt("Progress", progress);
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
        formations.set(tag.getInt("Formations"));
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
        // Saves before the attachment became the only store still carry their own copy.
        if (tag.contains("BloodVolume"))
            volume.setBloodVolume(Math.clamp(tag.getDouble("BloodVolume"), 0.0D, volume.getMaxBloodVolume()));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        if (packet.getTag() != null) loadAdditional(packet.getTag(), registries);
    }
}
