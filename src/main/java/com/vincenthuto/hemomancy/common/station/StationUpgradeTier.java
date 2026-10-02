package com.vincenthuto.hemomancy.common.station;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record StationUpgradeTier(UpgradeStation station, int tier, int requiredDegree, String upgradeItemPath,
        String ritePath, List<String> requiredUsage, int circuits, int bloodPerCircuit, String dialogueKey) {
    public StationUpgradeTier {
        requiredUsage = List.copyOf(requiredUsage);
    }

    public ResourceLocation upgradeItem() { return ResourceLocation.fromNamespaceAndPath("hemomancy", upgradeItemPath); }
    public ResourceLocation rite() { return ResourceLocation.fromNamespaceAndPath("hemomancy", ritePath); }
    /** Repeat-craft recipes share the upgrade item's id. */
    public ResourceLocation craftingRecipe() { return upgradeItem(); }
    public boolean lesser() { return station == UpgradeStation.ALEMBIC && tier == 1; }
    public String riteType() { return lesser() ? "lesser" : "greater"; }
    public String floor() { return lesser() ? "hemomancy:working_lesser" : "hemomancy:working_greater"; }
    public int anchors() { return tier == 1 ? 4 : 8; }
}
