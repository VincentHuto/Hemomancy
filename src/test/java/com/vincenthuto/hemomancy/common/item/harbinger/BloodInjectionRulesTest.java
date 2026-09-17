package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BloodInjectionRulesTest {
    private BloodInjectionRules.Response response(String json) {
        return BloodInjectionRules.parse(JsonParser.parseString(json).getAsJsonObject(), id -> true);
    }

    @Test void deduplicatesBeforeCapAndMergesStrongestContributions() {
        var a = response("""
            {"tendency":"ANIMUS","benefits":[{"effect":"minecraft:regeneration"},
              {"effect":"minecraft:fire_resistance"},{"effect":"minecraft:haste"}]}
            """);
        var b = response("""
            {"tendency":"FLAMMEUS","benefits":[{"effect":"minecraft:fire_resistance","duration":400,"amplifier":1},
              {"effect":"minecraft:strength","drawbacks":[{"effect":"minecraft:slowness"}]}]}
            """);
        var result = BloodInjectionRules.select(List.of(a,b));
        assertEquals(3, result.benefits().size());
        assertEquals(400, result.benefits().get(1).duration());
        assertEquals(1, result.benefits().get(1).amplifier());
        assertTrue(result.drawbacks().isEmpty());
    }

    @Test void revealConsumesASlotAndCannotBringAnOmittedDrawback() {
        var a = response("""
            {"tendency":"LUX","benefits":[{"effect":"minecraft:regeneration"},
              {"effect":"minecraft:haste"},{"effect":"minecraft:night_vision"},
              {"operation":"reveal","duration":40,"radius":8,"drawbacks":[{"effect":"minecraft:weakness"}]}]}
            """);
        assertEquals(3, BloodInjectionRules.select(List.of(a)).benefits().size());
        assertTrue(BloodInjectionRules.select(List.of(a)).drawbacks().isEmpty());
        var lux = response("""
            {"tendency":"LUX","benefits":[{"effect":"minecraft:night_vision"},
              {"operation":"reveal","duration":40,"radius":8}]}
            """);
        assertEquals(2, BloodInjectionRules.select(List.of(lux)).benefits().size());
    }

    @Test void duplicateSelectedBenefitRetainsAssociatedDrawback() {
        var a = response("""
            {"tendency":"CONGEATIO","benefits":[{"effect":"hemomancy:cryoprotection",
              "drawbacks":[{"effect":"minecraft:slowness"}]}]}
            """);
        var b = response("""
            {"property":"hemomancy:blood_properties/cold_native","benefits":[{"effect":"hemomancy:cryoprotection"}]}
            """);
        var result = BloodInjectionRules.select(List.of(a,b));
        assertEquals(1,result.benefits().size());
        assertEquals(1,result.drawbacks().size());
    }

    @Test void rejectsInvalidAndContradictoryDefinitions() {
        for (String json : List.of(
            "{\"tendency\":\"NINTH\",\"benefits\":[]}",
            "{\"property\":\"INVALID TAG\",\"benefits\":[]}",
            "{\"tendency\":\"LUX\",\"benefits\":[{\"operation\":\"script\"}]}",
            "{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"minecraft:regeneration\",\"duration\":0}]}",
            "{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"minecraft:regeneration\",\"amplifier\":256}]}",
            "{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"minecraft:regeneration\"}],\"drawbacks\":[{\"effect\":\"minecraft:regeneration\"}]}")) {
            assertThrows(IllegalArgumentException.class, () -> response(json));
        }
        assertThrows(IllegalArgumentException.class, () -> BloodInjectionRules.parse(
            JsonParser.parseString("{\"tendency\":\"ANIMUS\",\"benefits\":[{\"effect\":\"missing:effect\"}]}").getAsJsonObject(), id -> false));
    }
}
