package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ContainerHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public final class AlembicUpgradeRites {
    private static final String FIRST = "cardinal_rite/first_condensation";
    private static final String ATHANOR = "cardinal_rite/sanguine_athanor";

    private AlembicUpgradeRites() {}

    public static boolean isRite(ResourceLocation id) {
        return ArmatureUpgradeRites.isRite(id) || ResonantForgeUpgradeRites.isRite(id) || id != null && id.getNamespace().equals("hemomancy")
                && (id.getPath().equals(FIRST) || id.getPath().equals(ATHANOR));
    }

    public static int projections(ResourceLocation id) {
        if (ArmatureUpgradeRites.isRite(id)) return ArmatureUpgradeRites.projections(id);
        if (ResonantForgeUpgradeRites.isRite(id)) return ResonantForgeUpgradeRites.projections(id);
        return id != null && id.getPath().equals(ATHANOR) ? 3 : 2;
    }

    public static int bloodPerProjection(ResourceLocation id) {
        return ResonantForgeUpgradeRites.isRite(id) ? 250 : 50;
    }

    public static BlockPos seat(ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.seat(rite);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.seat(rite);
        Direction forward = rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
        return rite.getCenterPos().above().relative(forward.getOpposite());
    }

    public static BlockPos target(ActiveCardinalRite rite, int stage) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.target(rite, stage);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.target(rite, stage);
        Direction forward = rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
        Direction right = forward.getClockWise();
        BlockPos center = rite.getCenterPos();
        return projections(rite.getRecipeId()) == 2
                ? center.relative(forward).relative(right, stage == 0 ? -1 : 1)
                : center.relative(forward, 2).relative(right, stage * 2 - 2);
    }

    public static Vec3 targetSurface(ServerLevel level, ActiveCardinalRite rite, int stage) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId()))
            return ArmatureUpgradeRites.targetSurface(level, rite, stage);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId()))
            return ResonantForgeUpgradeRites.targetSurface(level, rite, stage);
        BlockPos target = target(rite, stage);
        BlockPos center = rite.getCenterPos();
        BlockPos air = CardinalRiteSigilRules.surfaceAirPosition(level, center,
                target.getX() - center.getX(), target.getZ() - center.getZ());
        return new Vec3(target.getX() + .5, air.getY() + .1, target.getZ() + .5);
    }

    public static GhastlyAlembicBlockEntity station(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockEntity(seat(rite)) instanceof GhastlyAlembicBlockEntity found ? found : null;
    }

    public static boolean prepare(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.prepare(level, rite);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.prepare(level, rite);
        if (!isRite(rite.getRecipeId())) return true;
        GhastlyAlembicBlockEntity station = station(level, rite);
        AlembicTier expected = projections(rite.getRecipeId()) == 2 ? AlembicTier.BASE : AlembicTier.CONDENSER;
        if (station == null || station.tier() != expected || station.isProcessing() || station.isRiteLocked()) return false;
        List<ActiveCardinalRite.RiteOffering> offerings = rite.getOfferingItinerary();
        if (offerings.size() != (expected == AlembicTier.BASE ? 1 : 5)) return false;
        for (var offering : offerings) {
            if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)
                    || offering.stack() == null || !ItemStack.isSameItemSameComponents(
                    offering.stack(), brazier.getOfferingForMatching())) return false;
        }
        CompoundTag state = rite.alembic();
        state.putUUID("RiteIdentity", UUID.randomUUID());
        state.putLong("SubjectPos", seat(rite).asLong());
        state.putUUID("SubjectIdentity", station.machineIdentity());
        state.putInt("SourceTier", station.tier().ordinal());
        station.setRiteLocked(true);
        state.put("SubjectSnapshot", snapshot(level, station));
        ListTag escrow = new ListTag();
        state.put("Offerings", escrow);
        state.putString("EscrowState", "ESCROWED");
        for (var offering : offerings) {
            IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) level.getBlockEntity(offering.pos());
            ItemStack stack = brazier.consumeOffering();
            if (stack.isEmpty()) {
                recover(level, rite);
                return false;
            }
            escrow.add(stack.save(level.registryAccess()));
        }
        state.putInt("Stage", 0);
        state.putInt("BloodAtStage", 0);
        return true;
    }

    public static boolean subjectPresent(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.subjectPresent(level, rite);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.subjectPresent(level, rite);
        GhastlyAlembicBlockEntity station = station(level, rite);
        CompoundTag state = rite.alembic();
        return station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.tier().ordinal() == state.getInt("SourceTier");
    }

    public static boolean focusPresent(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.focusPresent(level, rite);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.focusPresent(level, rite);
        return level.getBlockState(rite.getCenterPos()).is(BlockInit.cardinal_focus.get());
    }

    public static boolean complete(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) return ArmatureUpgradeRites.complete(level, rite);
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) return ResonantForgeUpgradeRites.complete(level, rite);
        if (!isRite(rite.getRecipeId())) return true;
        CompoundTag state = rite.alembic();
        GhastlyAlembicBlockEntity station = station(level, rite);
        UUID riteId = state.hasUUID("RiteIdentity") ? state.getUUID("RiteIdentity") : null;
        if (state.getString("EscrowState").equals("CONSUMED")) {
            if (station == null || !station.wasUpgradedBy(riteId)) return false;
            releaseCompletedStaff(level, rite, riteId);
            station.setRiteLocked(false);
            return true;
        }
        if (!rite.isComplete() || !subjectPresent(level, rite)
                || state.getInt("Stage") != projections(rite.getRecipeId())
                || station == null || !snapshot(level, station)
                .equals(state.getCompound("SubjectSnapshot"))) {
            recover(level, rite);
            return false;
        }
        if (!station.completeUpgrade(projections(rite.getRecipeId()) == 2
                ? AlembicTier.CONDENSER : AlembicTier.ATHANOR, riteId)) {
            recover(level, rite);
            return false;
        }
        state.putString("EscrowState", "CONSUMED");
        state.remove("Offerings");
        releaseCompletedStaff(level, rite, riteId);
        station.setRiteLocked(false);
        CardinalRiteSavedData.get(level).setDirty();
        return true;
    }

    public static void recover(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) {
            ArmatureUpgradeRites.recover(level, rite);
            return;
        }
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) {
            ResonantForgeUpgradeRites.recover(level, rite);
            return;
        }
        if (!isRite(rite.getRecipeId())) return;
        CompoundTag state = rite.alembic();
        if (!state.getString("EscrowState").equals("ESCROWED")) return;
        GhastlyAlembicBlockEntity station = station(level, rite);
        if (station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.wasUpgradedBy(state.getUUID("RiteIdentity"))) {
            state.putString("EscrowState", "CONSUMED");
            state.remove("Offerings");
            releaseCompletedStaff(level, rite, state.getUUID("RiteIdentity"));
            station.setRiteLocked(false);
            CardinalRiteSavedData.get(level).setDirty();
            return;
        }
        state.putString("EscrowState", "RETURNED");
        List<ItemStack> items = new ArrayList<>();
        ListTag escrow = state.getList("Offerings", Tag.TAG_COMPOUND);
        for (int i = 0; i < escrow.size(); i++)
            items.add(ItemStack.parseOptional(level.registryAccess(), escrow.getCompound(i)));
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) items.add(staff);
        CardinalRiteSavedData saved = CardinalRiteSavedData.get(level.getServer().overworld());
        saved.addRecovery(rite.getPlayerUUID(), state.getUUID("RiteIdentity"), items);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null && owner.level() == level && owner.isAlive()) saved.deliverRecovery(owner);
        cleanup(level, rite);
    }

    public static void cleanup(ServerLevel level, ActiveCardinalRite rite) {
        if (ArmatureUpgradeRites.isRite(rite.getRecipeId())) {
            ArmatureUpgradeRites.cleanup(level, rite);
            return;
        }
        if (ResonantForgeUpgradeRites.isRite(rite.getRecipeId())) {
            ResonantForgeUpgradeRites.cleanup(level, rite);
            return;
        }
        if (!isRite(rite.getRecipeId())) return;
        GhastlyAlembicBlockEntity station = station(level, rite);
        if (station != null && rite.alembic().hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(rite.alembic().getUUID("SubjectIdentity")))
            station.setRiteLocked(false);
    }

    private static void releaseCompletedStaff(ServerLevel level, ActiveCardinalRite rite, UUID riteId) {
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld())
                .addRecovery(rite.getPlayerUUID(), UUID.nameUUIDFromBytes(
                        (riteId + ":staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
    }

    private static CompoundTag snapshot(ServerLevel level, GhastlyAlembicBlockEntity station) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, station.items, level.registryAccess());
        tag.putDouble("BloodLevel", station.getBloodVolume());
        return tag;
    }
}
