package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.Map;

/** Stable authored silhouette vocabulary for the six skill-family fruit. */
public final class ArborFruitGeometry {
    private static final Map<String, Profile> PROFILES = Map.of(
            "core", new Profile(1.05, 1.10, .18, 1, Shape.HEART_POME),
            "living_staff", new Profile(.76, 1.48, .16, 1, Shape.HOOKED_PEAR),
            "summons", new Profile(.48, .62, .12, 5, Shape.THREAD_BERRIES),
            "covenant", new Profile(.72, .92, .14, 2, Shape.JOINED_FRUIT),
            "scars", new Profile(.88, .74, .12, 1, Shape.FISSURED_NUT),
            "mycelial", new Profile(.60, .82, 1.18, 1, Shape.GILLED_POD));

    private ArborFruitGeometry() { }

    public static Map<String, Profile> familyProfiles() { return PROFILES; }
    public static Profile profile(String family) { return PROFILES.getOrDefault(family, PROFILES.get("core")); }

    public enum Shape { HEART_POME, HOOKED_PEAR, THREAD_BERRIES, JOINED_FRUIT, FISSURED_NUT, GILLED_POD }
    public record Profile(double bodyWidth, double bodyHeight, double capWidth, int lobes, Shape shape) { }
}
