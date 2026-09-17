package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** Stable specimen layout; presentation limits never alter the shared profile. */
public record MicroscopeComposition(long seed, List<EnumBloodTendency> tendencies, List<ResourceLocation> properties) {
    public enum Motif { AQUATIC, FLYING, VENOMOUS, ARTHROPOD, COLD_NATIVE, NETHER_NATIVE, ENDER, UNDEAD, FUNGAL, EXPLOSIVE, BURROWING, NEUTRAL }

    public static MicroscopeComposition of(BloodSampleData.Profile profile) {
        StringBuilder key = new StringBuilder(String.valueOf(profile.sourceEntityId()));
        profile.tendencies().forEach(t -> key.append('|').append(t.name()));
        profile.properties().forEach(p -> key.append('|').append(p));
        long seed = 0xcbf29ce484222325L;
        for (int i = 0; i < key.length(); i++) seed = (seed ^ key.charAt(i)) * 0x100000001b3L;
        return new MicroscopeComposition(seed, profile.tendencies().stream().limit(3).toList(), profile.properties().stream().limit(2).toList());
    }

    public static Motif motif(ResourceLocation property) {
        if (!property.getNamespace().equals("hemomancy")) return Motif.NEUTRAL;
        if (property.getPath().equals("fungal")) return Motif.FUNGAL;
        return switch (property.getPath()) {
            case "blood_properties/aquatic" -> Motif.AQUATIC;
            case "blood_properties/flying" -> Motif.FLYING;
            case "blood_properties/venomous" -> Motif.VENOMOUS;
            case "blood_properties/arthropod" -> Motif.ARTHROPOD;
            case "blood_properties/cold_native" -> Motif.COLD_NATIVE;
            case "blood_properties/nether_native" -> Motif.NETHER_NATIVE;
            case "blood_properties/ender" -> Motif.ENDER;
            case "blood_properties/undead" -> Motif.UNDEAD;
            case "blood_properties/explosive" -> Motif.EXPLOSIVE;
            case "blood_properties/burrowing" -> Motif.BURROWING;
            default -> Motif.NEUTRAL;
        };
    }
}
