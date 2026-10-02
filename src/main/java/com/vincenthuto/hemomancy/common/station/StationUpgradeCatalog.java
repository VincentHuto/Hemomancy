package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public final class StationUpgradeCatalog {
    public static final int OFFERINGS_PER_RITE = 6;

    public static final String DISTILL = "distill";
    public static final String REFINE = "refine";
    public static final String COMPOUND = "compound";
    public static final String SEPARATE = "separate";
    public static final String SEPARATE_CALIBRATED = "separate_calibrated";
    public static final String GRIND = "grind";
    public static final String APPLY = "apply";
    public static final String GRIND_SELECTED = "grind_selected";
    public static final String ARMOR_UPGRADE = "armor_upgrade";
    public static final String ARMOR_UPGRADE_CONSECRATED = "armor_upgrade_consecrated";
    public static final String ENCHANT = "enchant";
    public static final String ENCHANT_TARGETED = "enchant_targeted";

    private static final List<StationUpgradeTier> TIERS = List.of(
            new StationUpgradeTier(UpgradeStation.ALEMBIC, 1, 4, "hematic_condenser_kit",
                    "cardinal_rite/first_condensation", List.of(DISTILL), 2, 50,
                    "hemomancy.alchemist.brewing.condenser"),
            new StationUpgradeTier(UpgradeStation.ALEMBIC, 2, 6, "sanguine_athanor_kit",
                    "cardinal_rite/sanguine_athanor", List.of(REFINE, COMPOUND), 3, 50,
                    "hemomancy.alchemist.brewing.athanor"),
            new StationUpgradeTier(UpgradeStation.CENTRIFUGE, 1, 4, "centrifugal_governor_kit",
                    "cardinal_rite/steady_separation", List.of(SEPARATE), 2, 50,
                    "hemomancy.alchemist.centrifuge.governor"),
            new StationUpgradeTier(UpgradeStation.CENTRIFUGE, 2, 6, "fractionating_rotor_kit",
                    "cardinal_rite/second_fraction", List.of(SEPARATE_CALIBRATED), 3, 50,
                    "hemomancy.alchemist.centrifuge.fractionating"),
            new StationUpgradeTier(UpgradeStation.RESONANT_FORGE, 1, 5, "precision_governor_kit",
                    "cardinal_rite/true_groove", List.of(GRIND, APPLY), 2, 250,
                    "hemomancy.artificer.resonant_forge.precision"),
            new StationUpgradeTier(UpgradeStation.RESONANT_FORGE, 2, 7, "master_cam_kit",
                    "cardinal_rite/enduring_pattern", List.of(GRIND_SELECTED), 3, 250,
                    "hemomancy.artificer.resonant_forge.master"),
            new StationUpgradeTier(UpgradeStation.ARMATURE, 1, 5, "vicars_consecration_kit",
                    "cardinal_rite/armature_consecration", List.of(ARMOR_UPGRADE), 2, 50,
                    "hemomancy.artificer.armature.consecration"),
            new StationUpgradeTier(UpgradeStation.ARMATURE, 2, 7, "monolithic_cornerstone",
                    "cardinal_rite/monolithic_armature", List.of(ARMOR_UPGRADE_CONSECRATED), 3, 50,
                    "hemomancy.monolith.cornerstone"),
            new StationUpgradeTier(UpgradeStation.SCRIPTORIUM, 1, 5, "rubricators_quill",
                    "cardinal_rite/eightfold_script", List.of(ENCHANT), 2, 50,
                    "hemomancy.mnemonist.scriptorium.rubricator"),
            new StationUpgradeTier(UpgradeStation.SCRIPTORIUM, 2, 7, "palimpsest_burin",
                    "cardinal_rite/palimpsest", List.of(ENCHANT_TARGETED), 3, 50,
                    "hemomancy.mnemonist.scriptorium.palimpsest"));

    private StationUpgradeCatalog() {}

    public static List<StationUpgradeTier> all() { return TIERS; }

    public static Optional<StationUpgradeTier> forRite(ResourceLocation riteId) {
        if (riteId == null || !riteId.getNamespace().equals("hemomancy")) return Optional.empty();
        return TIERS.stream().filter(tier -> tier.ritePath().equals(riteId.getPath())).findFirst();
    }

    public static StationUpgradeTier get(UpgradeStation station, int tier) {
        return TIERS.stream().filter(entry -> entry.station() == station && entry.tier() == tier).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(station + " has no upgrade tier " + tier));
    }

    public static boolean isUpgradeRitePath(String path) {
        return TIERS.stream().anyMatch(tier -> tier.ritePath().equals(path));
    }
}
