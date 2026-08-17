package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;

import java.util.Locale;
import java.util.Optional;

public enum MuscleMemory {
    SANGUINE_FISTS(EnumVeinSections.ARMS, EnumBloodTendency.FLAMMEUS, EnumBloodTendency.ANIMUS, 3, .25F, 0),
    LABORING_ARMS(EnumVeinSections.ARMS, EnumBloodTendency.FERRIC, EnumBloodTendency.DUCTILIS, 2, .15F, 10),
    COURSING_LEGS(EnumVeinSections.LEGS, EnumBloodTendency.DUCTILIS, EnumBloodTendency.FLAMMEUS, 2, .15F, 0),
    HUSHED_GAIT(EnumVeinSections.LEGS, EnumBloodTendency.TENEBRIS, EnumBloodTendency.DUCTILIS, 1, .10F, 0),
    PREDATORY_EYES(EnumVeinSections.HEAD, EnumBloodTendency.LUX, EnumBloodTendency.TENEBRIS, 2, .15F, 100),
    SECOND_PULSE(EnumVeinSections.HEART, EnumBloodTendency.ANIMUS, EnumBloodTendency.MORTEM, 12, .75F, 600),
    ENDURING_VISCERA(EnumVeinSections.BODY, EnumBloodTendency.CONGEATIO, EnumBloodTendency.MORTEM, 3, .20F, 100),
    CARRION_METABOLISM(EnumVeinSections.BODY, EnumBloodTendency.MORTEM, EnumBloodTendency.ANIMUS, 2, .20F, 0);

    private final EnumVeinSections section;
    private final EnumBloodTendency primaryTendency;
    private final EnumBloodTendency secondaryTendency;
    private final double bloodCost;
    private final float vascularStrain;
    private final int cooldownTicks;

    MuscleMemory(EnumVeinSections section, EnumBloodTendency primaryTendency,
            EnumBloodTendency secondaryTendency, double bloodCost, float vascularStrain, int cooldownTicks) {
        this.section = section;
        this.primaryTendency = primaryTendency;
        this.secondaryTendency = secondaryTendency;
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

    public EnumBloodTendency primaryTendency() {
        return primaryTendency;
    }

    public EnumBloodTendency secondaryTendency() {
        return secondaryTendency;
    }

    public double bloodCost() { return bloodCost; }

    public float vascularStrain() { return vascularStrain; }

    public int cooldownTicks() { return cooldownTicks; }

    public static Optional<MuscleMemory> byId(String id) {
        for (MuscleMemory memory : values()) {
            if (memory.id().equals(id)) {
                return Optional.of(memory);
            }
        }
        return Optional.empty();
    }
}
