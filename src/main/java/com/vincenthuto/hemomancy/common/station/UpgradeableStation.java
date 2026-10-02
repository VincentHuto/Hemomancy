package com.vincenthuto.hemomancy.common.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

/** A crafting station that climbs the shared two-step upgrade ladder through a Cardinal Rite. */
public interface UpgradeableStation {
    UpgradeStation upgradeStation();
    BlockState getBlockState();
    BlockPos getBlockPos();
    Level getLevel();
    UUID machineIdentity();
    boolean isRiteLocked();
    void setRiteLocked(boolean locked);
    /** Idle, unlocked, and (where the station cares) facing the rite and structurally complete. */
    boolean readyForUpgradeRite(ServerLevel level, Direction riteForward);
    /** Everything the rite must find unchanged at completion; excludes lock and upgrade bookkeeping. */
    CompoundTag upgradeSnapshot(HolderLookup.Provider registries);
    boolean wasUpgradedBy(UUID riteId);
    void markUpgradedBy(UUID riteId);

    default int upgradeTier() {
        return StationTierProperty.stage(getBlockState());
    }

    default boolean completeUpgrade(int targetTier, UUID riteId) {
        if (riteId == null || !(this instanceof BlockEntity self) || !StationTierProperty.raiseTo(self, targetTier))
            return false;
        markUpgradedBy(riteId);
        setRiteLocked(false);
        self.setChanged();
        return true;
    }

    /** Hook for station-specific progression side effects after a rite raises the tier. */
    default void onUpgraded(ServerPlayer owner) {}
}
