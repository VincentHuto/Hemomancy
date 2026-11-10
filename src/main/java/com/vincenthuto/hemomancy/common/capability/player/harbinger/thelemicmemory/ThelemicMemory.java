package com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

import java.util.Locale;
import java.util.Optional;

public enum ThelemicMemory {
    SANGUINE_FISTS(EnumVeinSections.ARMS, EnumBloodTemper.FLAMMEUS, EnumBloodTemper.ANIMUS, 3, .25F, 0),
    LABORING_ARMS(EnumVeinSections.ARMS, EnumBloodTemper.FERRIC, EnumBloodTemper.DUCTILIS, 2, .15F, 10),
    COURSING_LEGS(EnumVeinSections.LEGS, EnumBloodTemper.DUCTILIS, EnumBloodTemper.FLAMMEUS, 2, .15F, 0),
    HUSHED_GAIT(EnumVeinSections.LEGS, EnumBloodTemper.TENEBRIS, EnumBloodTemper.DUCTILIS, 1, .10F, 0),
    PREDATORY_EYES(EnumVeinSections.HEAD, EnumBloodTemper.LUX, EnumBloodTemper.TENEBRIS, 2, .15F, 100),
    SECOND_PULSE(EnumVeinSections.HEART, EnumBloodTemper.ANIMUS, EnumBloodTemper.MORTEM, 12, .75F, 600),
    ENDURING_VISCERA(EnumVeinSections.BODY, EnumBloodTemper.CONGEATIO, EnumBloodTemper.MORTEM, 3, .20F, 100),
    CARRION_METABOLISM(EnumVeinSections.BODY, EnumBloodTemper.MORTEM, EnumBloodTemper.ANIMUS, 2, .20F, 0);

    private final EnumVeinSections section;
    private final EnumBloodTemper primaryTemper;
    private final EnumBloodTemper secondaryTemper;
    private final double bloodCost;
    private final float vascularStrain;
    private final int cooldownTicks;

    ThelemicMemory(EnumVeinSections section, EnumBloodTemper primaryTemper,
            EnumBloodTemper secondaryTemper, double bloodCost, float vascularStrain, int cooldownTicks) {
        this.section = section;
        this.primaryTemper = primaryTemper;
        this.secondaryTemper = secondaryTemper;
        this.bloodCost = bloodCost;
        this.vascularStrain = vascularStrain;
        this.cooldownTicks = cooldownTicks;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EnumVeinSections section() {
        return section;
    }

    public EnumBloodTemper primaryTemper() {
        return primaryTemper;
    }

    public EnumBloodTemper secondaryTemper() {
        return secondaryTemper;
    }

    public double bloodCost() { return bloodCost; }

    public float vascularStrain() { return vascularStrain; }

    public int cooldownTicks() { return cooldownTicks; }

    public static Optional<ThelemicMemory> byId(String id) {
        for (ThelemicMemory memory : values()) {
            if (memory.id().equals(id)) {
                return Optional.of(memory);
            }
        }
        return Optional.empty();
    }
}
