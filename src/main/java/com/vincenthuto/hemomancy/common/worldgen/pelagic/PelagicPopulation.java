package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import java.util.List;

/** Spawn packs use native caps; visitors carry much smaller weights than home populations. */
public final class PelagicPopulation {
    public static final String BENTHOS = "hemomancy:pelagic_benthos";
    public static final String PYROSOMES = "hemomancy:pyrosomes";
    private PelagicPopulation() {}
    public record Entry(String category, String entity, int weight, int min, int max) {}
    public static List<Entry> entries(PelagicLayer layer) {
        return switch (layer) {
            case SHORE -> List.of(
                    new Entry("ambient", "hemomancy:hemolymphopoda", 30, 2, 5),
                    new Entry(BENTHOS, "hemomancy:chiton", 26, 2, 4));
            case OPEN -> List.of(
                    new Entry("water_ambient", "hemomancy:pelagic_herring", 45, 6, 10),
                    new Entry("water_creature", "hemomancy:mnemonic_whale", 12, 1, 1),
                    new Entry(PYROSOMES, "hemomancy:pyrosome", 18, 1, 2),
                    new Entry("water_creature", "hemomancy:prism_cuttle", 2, 1, 1),
                    new Entry("water_creature", "minecraft:squid", 6, 1, 3));
            case TWILIGHT -> List.of(
                    new Entry("water_creature", "hemomancy:prism_cuttle", 28, 1, 3),
                    new Entry("water_creature", "hemomancy:siphonophore", 18, 1, 2),
                    new Entry(PYROSOMES, "hemomancy:pyrosome", 2, 1, 1),
                    new Entry("water_creature", "hemomancy:mnemonic_whale", 1, 1, 1),
                    new Entry("water_ambient", "hemomancy:pelagic_herring", 3, 6, 10),
                    new Entry("water_ambient", "hemomancy:blood_lantern_jelly", 3, 1, 2));
            case MIDNIGHT -> List.of(
                    new Entry("water_ambient", "hemomancy:blood_lantern_jelly", 28, 2, 4),
                    new Entry("water_ambient", "hemomancy:bloody_belly_comb_jelly", 24, 1, 2),
                    new Entry("water_creature", "hemomancy:siphonophore", 2, 1, 1),
                    new Entry("water_creature", "hemomancy:prism_cuttle", 1, 1, 1));
            case CARRION -> List.of(
                    new Entry("water_ambient", "hemomancy:vampire_squid", 24, 1, 2),
                    new Entry(BENTHOS, "hemomancy:hagfish", 30, 2, 4),
                    new Entry("water_ambient", "hemomancy:bloody_belly_comb_jelly", 2, 1, 1),
                    new Entry("water_ambient", "hemomancy:blood_lantern_jelly", 1, 1, 1),
                    new Entry(BENTHOS, "hemomancy:chalybeate_snail", 2, 1, 2));
            case HYDROTHERMAL -> List.of(
                    new Entry("water_ambient", "hemomancy:vampire_squid", 24, 1, 2),
                    new Entry(BENTHOS, "hemomancy:chalybeate_snail", 32, 2, 4),
                    new Entry(BENTHOS, "hemomancy:hagfish", 2, 1, 2));
            case REEF -> List.of(); // Existing reef bootstrap retains its ordinary fish and coral.
        };
    }
}
