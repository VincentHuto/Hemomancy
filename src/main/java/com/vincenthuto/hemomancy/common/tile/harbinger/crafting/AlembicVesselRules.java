package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

public final class AlembicVesselRules {
    public enum Vessel { FLASK, JUG }

    private AlembicVesselRules() {}

    public static int requiredBlood(Vessel vessel) {
        return vessel == Vessel.JUG ? 5_000 : 2_500;
    }
}
