package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import javax.annotation.Nullable;

public final class PelagicAquifer implements Aquifer {
    private final Aquifer original;
    private final PelagicContext context;
    private boolean delegated;
    public PelagicAquifer(Aquifer original, PelagicContext context) { this.original = original; this.context = context; }

    @Override @Nullable public BlockState computeSubstance(DensityFunction.FunctionContext point, double density) {
        var column = context.column(point.blockX(), point.blockZ());
        if (column.influence() > 0 && point.blockY() >= column.floor() - 6) {
            delegated = false;
            return density > 0 ? null : point.blockY() < 63 ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        }
        delegated = true;
        return original.computeSubstance(point, density);
    }
    @Override public boolean shouldScheduleFluidUpdate() { return delegated && original.shouldScheduleFluidUpdate(); }
    public boolean protectsSeabed(int x, int y, int z) {
        var column = context.column(x, z);
        return column.influence() >= .99 && y <= column.floor() && y >= column.floor() - 8;
    }
}
