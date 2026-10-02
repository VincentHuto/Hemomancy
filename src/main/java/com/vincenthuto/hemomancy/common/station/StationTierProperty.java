package com.vincenthuto.hemomancy.common.station;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Upgrade tier shared by every upgradeable station: 0 = base, 1 = first upgrade, 2 = second upgrade. */
public final class StationTierProperty {
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 2);

    private StationTierProperty() {}

    public static int stage(BlockState state) {
        return state.hasProperty(STAGE) ? state.getValue(STAGE) : 0;
    }

    public static boolean raiseTo(BlockEntity station, int targetStage) {
        Level level = station.getLevel();
        BlockState state = station.getBlockState();
        if (level == null || !state.hasProperty(STAGE)
                || !StationUpgradeRules.isNextTier(state.getValue(STAGE), targetStage)) return false;
        level.setBlock(station.getBlockPos(), state.setValue(STAGE, targetStage), 3);
        return true;
    }

    /** Moves a tier saved by pre-consolidation builds (block-entity NBT) into the blockstate. */
    public static void applyLegacyStage(BlockEntity station, int legacyStage) {
        Level level = station.getLevel();
        BlockState state = station.getBlockState();
        if (level == null || level.isClientSide || legacyStage <= 0 || !state.hasProperty(STAGE)
                || state.getValue(STAGE) >= legacyStage) return;
        level.setBlock(station.getBlockPos(), state.setValue(STAGE, Math.min(2, legacyStage)), 3);
    }
}
