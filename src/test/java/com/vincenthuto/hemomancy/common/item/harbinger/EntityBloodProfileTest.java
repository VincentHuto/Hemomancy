package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper.*;
import static org.junit.jupiter.api.Assertions.*;

class EntityBloodProfileTest {
    private EntityBloodProfile parse(String json) {
        return EntityBloodProfile.parse(JsonParser.parseString(json).getAsJsonObject());
    }

    @Test void defaultsToUnrestrictedEmptyProfile() {
        assertEquals(EntityBloodProfile.EMPTY, parse("{}"));
    }

    @Test void parsesAndDeduplicatesCanonicalTempersAndProperties() {
        var profile = parse("""
                {"tempers":["animus","congeatio","animus"],
                 "properties":["hemomancy:fungal","addon:blood_properties/custom","hemomancy:fungal"],
                 "requires_living_syringe":true}
                """);
        assertEquals(List.of(ANIMUS, CONGEATIO), profile.tempers());
        assertEquals(List.of(ResourceLocation.parse("addon:blood_properties/custom"), EntityBloodProfile.FUNGAL), profile.properties());
        assertTrue(profile.requiresLivingSyringe());
    }

    @Test void rejectsMalformedFieldsInsteadOfCoercingThem() {
        for (String json : List.of("{\"tempers\":null}", "{\"tempers\":[\"ANIMUS\"]}",
                "{\"tempers\":[\"unknown\"]}", "{\"properties\":[1]}", "{\"properties\":[\"aquatic\"]}",
                "{\"requires_living_syringe\":\"true\"}", "{\"requires_living_syringe\":null}")) {
            assertThrows(RuntimeException.class, () -> parse(json), json);
        }
    }

    @Test void defensivelyCopiesMemberships() {
        var tempers = new ArrayList<>(List.of(ANIMUS));
        var properties = new ArrayList<>(List.of(EntityBloodProfile.FUNGAL));
        var profile = new EntityBloodProfile(tempers, properties, true);
        tempers.clear();
        properties.clear();
        assertEquals(List.of(ANIMUS), profile.tempers());
        assertEquals(List.of(EntityBloodProfile.FUNGAL), profile.properties());
        assertThrows(UnsupportedOperationException.class, () -> profile.tempers().clear());
    }
}
