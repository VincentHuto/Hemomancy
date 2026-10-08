package com.vincenthuto.hemomancy.common.worldgen.pelagic;

public final class PelagicDensity {
    private PelagicDensity() {}
    public static double shape(double original, PelagicTerrainSampler.Column column, int y, boolean preliminary, boolean hollow) {
        if (column.influence() == 0) return original;
        double distance = column.floor() - y;
        double linear = distance / 8;
        // A saturated target can flip an entire water column solid at one blend weight.
        // Retain its vertical gradient on transitional coasts; full-strength terrain stays capped.
        double capped = Math.max(-1, Math.min(1, linear));
        double shaped = preliminary ? .390625 + linear : linear + (capped - linear) * column.influence();
        if (!preliminary && hollow) shaped = -.35;
        // Keep caves beneath fully shaped shelves, with an unbroken skin under the ocean.
        double skin = 1 - PelagicTerrainSampler.smooth(8, 18, distance);
        // Connect a transitional shelf to the old seabed instead of exposing a detached rock wall.
        skin += (1 - skin) * (1 - PelagicTerrainSampler.smooth(.5, 1, column.influence()));
        if (column.floor() < -45) skin = 1;
        double influence = column.influence() * (preliminary ? 1 : skin);
        return original + influence * (shaped - original);
    }
}
