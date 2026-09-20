package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.*;
import static org.junit.jupiter.api.Assertions.*;

class EntityBloodProfileTest {
    private EntityBloodProfile parse(String json) {
        return EntityBloodProfile.parse(JsonParser.parseString(json).getAsJsonObject());
    }

    @Test void defaultsToUnrestrictedEmptyProfile() {
        assertEquals(EntityBloodProfile.EMPTY, parse("{}"));
    }

    @Test void parsesAndDeduplicatesCanonicalTendenciesAndProperties() {
        var profile = parse("""
                {"tendencies":["animus","congeatio","animus"],
                 "properties":["hemomancy:fungal","addon:blood_properties/custom","hemomancy:fungal"],
                 "requires_living_syringe":true}
                """);
        assertEquals(List.of(ANIMUS, CONGEATIO), profile.tendencies());
        assertEquals(List.of(ResourceLocation.parse("addon:blood_properties/custom"), EntityBloodProfile.FUNGAL), profile.properties());
        assertTrue(profile.requiresLivingSyringe());
    }

    @Test void rejectsMalformedFieldsInsteadOfCoercingThem() {
        for (String json : List.of("{\"tendencies\":null}", "{\"tendencies\":[\"ANIMUS\"]}",
                "{\"tendencies\":[\"unknown\"]}", "{\"properties\":[1]}", "{\"properties\":[\"aquatic\"]}",
                "{\"requires_living_syringe\":\"true\"}", "{\"requires_living_syringe\":null}")) {
            assertThrows(RuntimeException.class, () -> parse(json), json);
        }
    }

    @Test void defensivelyCopiesMemberships() {
        var tendencies = new ArrayList<>(List.of(ANIMUS));
        var properties = new ArrayList<>(List.of(EntityBloodProfile.FUNGAL));
        var profile = new EntityBloodProfile(tendencies, properties, true);
        tendencies.clear();
        properties.clear();
        assertEquals(List.of(ANIMUS), profile.tendencies());
        assertEquals(List.of(EntityBloodProfile.FUNGAL), profile.properties());
        assertThrows(UnsupportedOperationException.class, () -> profile.tendencies().clear());
    }
}
