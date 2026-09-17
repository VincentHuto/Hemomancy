package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MicroscopeCompositionTest {
    @Test void equivalentSpecimensKeepTheirCompositionAfterIdentification() {
        var source = ResourceLocation.parse("minecraft:pig");
        var a = new BloodSampleData.Profile(source, List.of(EnumBloodTendency.FERRIC, EnumBloodTendency.ANIMUS),
                List.of(ResourceLocation.parse("addon:blood_properties/unknown")), false);
        var b = new BloodSampleData.Profile(source, List.of(EnumBloodTendency.ANIMUS, EnumBloodTendency.FERRIC), a.properties(), true);
        assertEquals(MicroscopeComposition.of(a), MicroscopeComposition.of(b));
        assertEquals(MicroscopeComposition.Motif.NEUTRAL, MicroscopeComposition.motif(a.properties().getFirst()));
    }

    @Test void visualCapsDoNotDiscardTraitsFromTheSeedOrProfile() {
        var properties = List.of(ResourceLocation.parse("hemomancy:fungal"),
                ResourceLocation.parse("hemomancy:blood_properties/flying"), ResourceLocation.parse("hemomancy:blood_properties/aquatic"));
        var profile = new BloodSampleData.Profile(ResourceLocation.parse("minecraft:pig"), List.of(EnumBloodTendency.values()), properties, false);
        var composition = MicroscopeComposition.of(profile);
        assertEquals(List.of(EnumBloodTendency.ANIMUS, EnumBloodTendency.FLAMMEUS, EnumBloodTendency.DUCTILIS), composition.tendencies());
        assertEquals(List.of(properties.get(2), properties.get(1)), composition.properties());
        assertEquals(8, profile.tendencies().size());
        assertNotEquals(composition.seed(), MicroscopeComposition.of(new BloodSampleData.Profile(profile.sourceEntityId(),
                composition.tendencies(), composition.properties(), false)).seed());
    }
}
