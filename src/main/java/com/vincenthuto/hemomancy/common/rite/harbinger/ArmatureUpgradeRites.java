package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
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
import java.util.UUID;

public final class ArmatureUpgradeRites {
    private static final String CONSECRATION = "cardinal_rite/armature_consecration";
    private static final String CORNERSTONE = "cardinal_rite/monolithic_armature";

    private ArmatureUpgradeRites() {}

    public static boolean isRite(ResourceLocation id) {
        return id != null && id.getNamespace().equals("hemomancy")
                && (id.getPath().equals(CONSECRATION) || id.getPath().equals(CORNERSTONE));
    }

    public static int projections(ResourceLocation id) {
        return id != null && id.getPath().equals(CORNERSTONE) ? 3 : 2;
    }

    public static ArmatureUpgradeRules.ArmatureTier sourceTier(ResourceLocation id) {
        return projections(id) == 3 ? ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED
                : ArmatureUpgradeRules.ArmatureTier.BASE;
    }

    public static ArmatureUpgradeRules.ArmatureTier targetTier(ResourceLocation id) {
        return projections(id) == 3 ? ArmatureUpgradeRules.ArmatureTier.MONOLITHIC
                : ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED;
    }

    public static BlockPos seat(ActiveCardinalRite rite) {
        Direction forward = rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
        return rite.getCenterPos().above().relative(forward.getOpposite(), 2);
    }

    public static BlockPos target(ActiveCardinalRite rite, int stage) {
        Direction forward = rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
        Direction right = forward.getClockWise();
        return rite.getCenterPos().relative(forward).relative(right, stage == 0 ? -1 : 1);
    }

    public static Vec3 targetSurface(ServerLevel level, ActiveCardinalRite rite, int stage) {
        BlockPos target = target(rite, stage);
        BlockPos center = rite.getCenterPos();
        BlockPos air = com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules.surfaceAirPosition(
                level, center, target.getX() - center.getX(), target.getZ() - center.getZ());
        return new Vec3(target.getX() + .5D, air.getY() + .1D, target.getZ() + .5D);
    }

    public static HematicArmatureBlockEntity station(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockEntity(seat(rite)) instanceof HematicArmatureBlockEntity station ? station : null;
    }

    public static boolean prepare(ServerLevel level, ActiveCardinalRite rite) {
        HematicArmatureBlockEntity station = station(level, rite);
        Direction forward = rite.getFloorForwards() == null ? Direction.NORTH : rite.getFloorForwards();
        if (station == null || station.getArmatureTier() != sourceTier(rite.getRecipeId())
                || !station.idleForRite() || station.getBlockState().getValue(HematicArmatureBlock.FACING) != forward)
            return false;
        List<ActiveCardinalRite.RiteOffering> offerings = rite.getOfferingItinerary();
        if (offerings.size() != 1) return false;
        ActiveCardinalRite.RiteOffering offering = offerings.getFirst();
        if (!(level.getBlockEntity(offering.pos()) instanceof IronBrazierBlockEntity brazier)
                || offering.stack() == null
                || !ItemStack.isSameItemSameComponents(offering.stack(), brazier.getOfferingForMatching())) return false;

        ItemStack consumed = brazier.consumeOffering();
        if (consumed.isEmpty()) return false;

        CompoundTag state = rite.alembic();
        state.putUUID("RiteIdentity", UUID.randomUUID());
        state.putUUID("SubjectIdentity", station.machineIdentity());
        state.putInt("SourceTier", station.getArmatureTier().id());
        state.put("SubjectSnapshot", station.upgradeSnapshot(level.registryAccess()));
        state.putString("EscrowState", "ESCROWED");
        ListTag escrow = new ListTag();
        escrow.add(consumed.save(level.registryAccess()));
        state.put("Offerings", escrow);
        state.putInt("Stage", 0);
        state.putInt("BloodAtStage", 0);
        station.setRiteLocked(true);
        return true;
    }

    public static boolean subjectPresent(ServerLevel level, ActiveCardinalRite rite) {
        HematicArmatureBlockEntity station = station(level, rite);
        CompoundTag state = rite.alembic();
        return station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.getArmatureTier().id() == state.getInt("SourceTier");
    }

    public static boolean focusPresent(ServerLevel level, ActiveCardinalRite rite) {
        return level.getBlockState(rite.getCenterPos()).is(BlockInit.cardinal_focus.get());
    }

    public static boolean complete(ServerLevel level, ActiveCardinalRite rite) {
        CompoundTag state = rite.alembic();
        HematicArmatureBlockEntity station = station(level, rite);
        UUID riteId = state.hasUUID("RiteIdentity") ? state.getUUID("RiteIdentity") : null;
        if (state.getString("EscrowState").equals("CONSUMED")) {
            if (station == null || !station.wasUpgradedBy(riteId)) return false;
            awardTier(level, rite, station.getArmatureTier());
            releaseStaff(level, rite, riteId);
            station.setRiteLocked(false);
            return true;
        }
        if (!rite.isComplete() || !subjectPresent(level, rite)
                || state.getInt("Stage") != projections(rite.getRecipeId()) || station == null
                || !station.upgradeSnapshot(level.registryAccess()).equals(state.getCompound("SubjectSnapshot"))) {
            recover(level, rite);
            return false;
        }
        if (!station.completeUpgrade(targetTier(rite.getRecipeId()), riteId)) {
            recover(level, rite);
            return false;
        }
        awardTier(level, rite, station.getArmatureTier());
        state.putString("EscrowState", "CONSUMED");
        state.remove("Offerings");
        releaseStaff(level, rite, riteId);
        station.setRiteLocked(false);
        CardinalRiteSavedData.get(level).setDirty();
        return true;
    }

    public static void recover(ServerLevel level, ActiveCardinalRite rite) {
        CompoundTag state = rite.alembic();
        if (!state.getString("EscrowState").equals("ESCROWED")) return;
        HematicArmatureBlockEntity station = station(level, rite);
        if (station != null && state.hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(state.getUUID("SubjectIdentity"))
                && station.wasUpgradedBy(state.getUUID("RiteIdentity"))) {
            state.putString("EscrowState", "CONSUMED");
            state.remove("Offerings");
            awardTier(level, rite, station.getArmatureTier());
            releaseStaff(level, rite, state.getUUID("RiteIdentity"));
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
        HematicArmatureBlockEntity station = station(level, rite);
        if (station != null && rite.alembic().hasUUID("SubjectIdentity")
                && station.machineIdentity().equals(rite.alembic().getUUID("SubjectIdentity")))
            station.setRiteLocked(false);
    }

    private static void releaseStaff(ServerLevel level, ActiveCardinalRite rite, UUID riteId) {
        ItemStack staff = rite.releaseEscrowedStaff(level.registryAccess());
        if (!staff.isEmpty()) CardinalRiteSavedData.get(level.getServer().overworld())
                .addRecovery(rite.getPlayerUUID(), UUID.nameUUIDFromBytes(
                        (riteId + ":staff").getBytes(StandardCharsets.UTF_8)), List.of(staff));
    }

    private static void awardTier(ServerLevel level, ActiveCardinalRite rite,
            ArmatureUpgradeRules.ArmatureTier tier) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(rite.getPlayerUUID());
        if (owner != null) ArtificerAssignments.onArmatureTierApplied(owner, tier);
    }
}
