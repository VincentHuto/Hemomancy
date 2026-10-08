package com.vincenthuto.hemomancy.common.worldgen.pelagic;

public enum PelagicLayer {
    SHORE("rockpool_shore", "Rockpool Shore"), REEF("erythrocoral_reef", "Erythrocoral Reef"),
    OPEN("pelagic_ocean", "Pelagic Ocean"), TWILIGHT("twilight_ocean", "Twilight Ocean"),
    MIDNIGHT("midnight_ocean", "Midnight Ocean"), CARRION("carrion_depths", "Carrion Depths"),
    HYDROTHERMAL("hydrothermal_depths", "Hydrothermal Depths");
    public final String id;
    public final String title;
    PelagicLayer(String id, String title) { this.id = id; this.title = title; }
    public static PelagicLayer atWaterY(int y) {
        return y >= 34 ? OPEN : y >= 13 ? TWILIGHT : y >= -8 ? MIDNIGHT : y >= -30 ? CARRION : HYDROTHERMAL;
    }
}
