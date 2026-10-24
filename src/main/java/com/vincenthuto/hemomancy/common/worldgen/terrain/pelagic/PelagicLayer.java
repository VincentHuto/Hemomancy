package com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic;

public enum PelagicLayer {
    SHORE("rockpool_shore"), REEF("erythrocoral_reef"), OPEN("pelagic_ocean"), TWILIGHT("twilight_ocean"),
    MIDNIGHT("midnight_ocean"), CARRION("carrion_depths"), HYDROTHERMAL("hydrothermal_depths");
    public final String id;
    PelagicLayer(String id) { this.id = id; }
    public static PelagicLayer atWaterY(int y) {
        return y >= 34 ? OPEN : y >= 13 ? TWILIGHT : y >= -8 ? MIDNIGHT : y >= -30 ? CARRION : HYDROTHERMAL;
    }
}
