package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

public enum UpgradeStation {
    ALEMBIC("alembic", "ghastly_alembic", 1),
    CENTRIFUGE("centrifuge", "vial_centrifuge", 2),
    RESONANT_FORGE("resonant_forge", "resonant_forge", 2),
    ARMATURE("armature", "hematic_armature", 2),
    SCRIPTORIUM("scriptorium", "enzymatic_scriptorium", 2);

    private final String serializedName;
    private final String blockPath;
    private final int seatDistance;

    UpgradeStation(String serializedName, String blockPath, int seatDistance) {
        this.serializedName = serializedName;
        this.blockPath = blockPath;
        this.seatDistance = seatDistance;
    }

    public String serializedName() { return serializedName; }
    public ResourceLocation blockId() { return ResourceLocation.fromNamespaceAndPath("hemomancy", blockPath); }
    /** Blocks behind the Cardinal Focus (opposite the rite's forward direction) where the station must sit. */
    public int seatDistance() { return seatDistance; }

    public static UpgradeStation byName(String name) {
        for (UpgradeStation station : values()) if (station.serializedName.equals(name)) return station;
        return null;
    }
}
