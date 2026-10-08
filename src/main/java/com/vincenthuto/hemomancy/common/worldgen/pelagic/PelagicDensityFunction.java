package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/** Runtime wrapper, installed after seed wiring; never part of saved generator settings. */
public record PelagicDensityFunction(DensityFunction original, PelagicContext context, boolean preliminary) implements DensityFunction {
    @Override public double compute(FunctionContext point) {
        var column = context.sample(point.blockX(), point.blockZ()).column();
        boolean fullyShaped = column.influence() == 1 && (preliminary || column.floor() < -45
                || column.floor() - point.blockY() <= 8);
        double vanilla = fullyShaped ? 0 : original.compute(point);
        return PelagicDensity.shape(vanilla, column, point.blockY(), preliminary,
                !preliminary && context.terrain().hollow(column, point.blockX(), point.blockY(), point.blockZ()));
    }
    @Override public void fillArray(double[] values, ContextProvider provider) { provider.fillAllDirectly(values, this); }
    @Override public DensityFunction mapAll(Visitor visitor) {
        return visitor.apply(new PelagicDensityFunction(original.mapAll(visitor), context, preliminary));
    }
    @Override public double minValue() { return Math.min(original.minValue(), -64); }
    @Override public double maxValue() { return Math.max(original.maxValue(), 64); }
    @Override public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        throw new UnsupportedOperationException("Pelagic density is a seeded runtime wrapper, not a saved density function");
    }
}
