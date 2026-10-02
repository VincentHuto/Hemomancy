package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.station.UpgradeableStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Prepares, escrows, completes and recovers every station-upgrade Cardinal Rite. */
public final class StationUpgradeRites {
    private static final String ESCROWED = "ESCROWED";
    private static final String CONSUMED = "CONSUMED";
    private static final String RETURNED = "RETURNED";

    private StationUpgradeRites() {}

    public static boolean isRite(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).isPresent();
    }

    public static int circuits(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).map(StationUpgradeTier::circuits).orElse(0);
    }

    public static int bloodPerCircuit(ResourceLocation id) {
        return StationUpgradeCatalog.forRite(id).map(StationUpgradeTier::bloodPerCircuit).orElse(0);
    }

    private static Optional<StationUpgradeTier> tier(ActiveCardinalRite rite) {
        return StationUpgradeCatalog.forRite(rite.getRecipeId());
    }

    private static Direction forward(ActiveCardinalRite rite) {
        return rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
    }

    public static BlockPos seat(ActiveCardinalRite rite) {
        int distance = tier(rite).map(t -> t.station().seatDistance()).orElse(2);
        return rite.getCenterPos().above().relative(forward(rite).getOpposite(), distance);
    }

    public static BlockPos target(ActiveCardinalRite rite, int circuit) {
        Direction forward = forward(rite);
        Direction right = forward.getClockWise();
        BlockPos center = rite.getCenterPos();
        return circuits(rite.getRecipeId()) == 2
                ? center.relative(forward).relative(right, circuit == 0 ? -1 : 1)
                : center.relative(forward, 2).relative(right, circuit * 2 - 2);
    }

    public static Vec3 targetSurface(ServerLevel level, ActiveCardinalRite rite, int circuit) {
        BlockPos target = target(rite, circuit);
        BlockPos center = rite.getCenterPos();
        BlockPos air = CardinalRiteSigilRules.surfaceAirPosition(level, center,
                target.getX() - center.getX(), target.getZ() - center.getZ());
        return new Vec3(target.getX() + .5D, air.getY() + .1D, target.getZ() + .5D);
    }

    public static UpgradeableStation station(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        return tier.isPresent() && level.getBlockEntity(seat(rite)) instanceof UpgradeableStation station
                && station.upgradeStation() == tier.get().station() ? station : null;
    }

    public static boolean prepare(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        if (tier.isEmpty()) return true;
        UpgradeableStation station = station(level, rite);
        if (station == null || station.isRiteLocked() || station.upgradeTier() != tier.get().tier() - 1
                || !station.readyForUpgradeRite(level, forward(rite))) return false;
        List<ActiveCardinalRite.RiteOffering> offerings = rite.getOfferingItinerary();
        if (offerings.size() != StationUpgradeCatalog.OFFERINGS_PER_RITE) return false;
        for (var offering : offerings) {
            if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)
                    || offering.stack() == null
                    || !ItemStack.isSameItemSameComponents(offering.stack(), brazier.getOfferingForMatching()))
                return false;
        }
        CompoundTag state = rite.upgrade();
        state.putUUID("RiteIdentity", UUID.randomUUID());
        state.putUUID("SubjectIdentity", station.machineIdentity());
        state.putInt("SourceTier", station.upgradeTier());
        state.put("SubjectSnapshot", station.upgradeSnapshot(level.registryAccess()));
        ListTag escrow = new ListTag();
        state.put("Offerings", escrow);
        state.putString("EscrowState", ESCROWED);
        state.putInt("Stage", 0);
        state.putInt("BloodAtStage", 0);
        station.setRiteLocked(true);
        for (var offering : offerings) {
            ItemStack consumed = ((IronBrazierBlockEntity) level.getBlockEntity(offering.pos())).consumeOffering();
            if (consumed.isEmpty()) {
                recover(level, rite);
                return false;
            }
            escrow.add(consumed.save(level.registryAccess()));
        }
        return true;
    }

    public static boolean subjectPresent(ServerLevel level, ActiveCardinalRite rite) {
        UpgradeableStation station = station(level, rite);
        CompoundTag state = rite.upgrade();
        return station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.upgradeTier() == state.getInt("SourceTier");
    }

    public static boolean focusPresent(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockState(rite.getCenterPos()).is(BlockInit.cardinal_focus.get());
    }

    public static boolean complete(ServerLevel level, ActiveCardinalRite rite) {
        Optional<StationUpgradeTier> tier = tier(rite);
        if (tier.isEmpty()) return true;
        CompoundTag state = rite.upgrade();
        UpgradeableStation station = station(level, rite);
        UUID riteId = state.hasUUID("RiteIdentity") ? state.getUUID("RiteIdentity") : null;
        if (state.getString("EscrowState").equals(CONSUMED)) {
            if (station == null || !station.wasUpgradedBy(riteId)) return false;
            releaseStaff(level, rite, riteId);
            station.setRiteLocked(false);
            return true;
        }
        if (!rite.isComplete() || !subjectPresent(level, rite)
                || state.getInt("Stage") != tier.get().circuits()
                || !comparableSnapshot(station.upgradeSnapshot(level.registryAccess()))
                        .equals(comparableSnapshot(state.getCompound("SubjectSnapshot")))
                || !station.completeUpgrade(tier.get().tier(), riteId)) {
            recover(level, rite);
            return false;
        }
        notifyOwner(level, rite, station);
        state.putString("EscrowState", CONSUMED);
        state.remove("Offerings");
        releaseStaff(level, rite, riteId);
        CardinalRiteSavedData.get(level).setDirty();
        return true;
    }

    public static void recover(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return;
        CompoundTag state = rite.upgrade();
        String escrowState = state.getString("EscrowState");
        if (escrowState.isEmpty()) {
            // Started before offerings were escrowed (pre-consolidation Scriptorium): only the offerings its
            // procession already absorbed from braziers were taken, so return those with the staff.
            List<ItemStack> items = new ArrayList<>();
            for (var absorbed : rite.getAbsorbedOfferings())
                if (absorbed.stack() != null) items.add(absorbed.stack().copy());
            ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
            if (!staff.isEmpty()) items.add(staff);
            if (!items.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld()).addRecovery(
                    rite.getPlayerUUID(), UUID.nameUUIDFromBytes((rite.getPlayerUUID() + ":" + rite.getRecipeId()
                            + ":legacy").getBytes(StandardCharsets.UTF_8)), items);
            UpgradeableStation station = station(level, rite);
            if (station != null) station.setRiteLocked(false);
            return;
        }
        if (!escrowState.equals(ESCROWED)) return;
        UpgradeableStation station = station(level, rite);
        UUID riteId = state.getUUID("RiteIdentity");
        if (station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.wasUpgradedBy(riteId)) {
            state.putString("EscrowState", CONSUMED);
            state.remove("Offerings");
            notifyOwner(level, rite, station);
            releaseStaff(level, rite, riteId);
            station.setRiteLocked(false);
            CardinalRiteSavedData.get(level).setDirty();
            return;
        }
        state.putString("EscrowState", RETURNED);
        List<ItemStack> items = new ArrayList<>();
        ListTag escrow = state.getList("Offerings", Tag.TAG_COMPOUND);
        for (int i = 0; i < escrow.size(); i++)
            items.add(ItemStack.parseOptional(level.registryAccess(), escrow.getCompound(i)));
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) items.add(staff);
        CardinalRiteSavedData saved = CardinalRiteSavedData.get(level.getServer().overworld());
        saved.addRecovery(rite.getPlayerUUID(), riteId, items);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null && owner.level() == level && owner.isAlive()) saved.deliverRecovery(owner);
        cleanup(level, rite);
    }

    public static void cleanup(ServerLevel level, ActiveCardinalRite rite) {
        if (!isRite(rite.getRecipeId())) return;
        UpgradeableStation station = station(level, rite);
        if (station != null && rite.upgrade().hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(rite.upgrade().getUUID("SubjectIdentity")))
            station.setRiteLocked(false);
    }

    /**
     * Drops keys that may legitimately differ between prepare and completion: legacy tier keys written by
     * pre-consolidation snapshots, and the Forge operator, which a refused button press can still set.
     */
    private static CompoundTag comparableSnapshot(CompoundTag snapshot) {
        CompoundTag copy = snapshot.copy();
        copy.remove("AlembicTier");
        copy.remove("Tier");
        copy.remove("ArmatureTier");
        copy.remove("Operator");
        return copy;
    }

    private static void notifyOwner(ServerLevel level, ActiveCardinalRite rite, UpgradeableStation station) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null) station.onUpgraded(owner);
    }

    private static void releaseStaff(ServerLevel level, ActiveCardinalRite rite, UUID riteId) {
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld())
                .addRecovery(rite.getPlayerUUID(), UUID.nameUUIDFromBytes(
                        (riteId + ":staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
    }
}
