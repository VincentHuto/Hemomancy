package com.vincenthuto.hemomancy.common.worldgen.config;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthConfigurationTest {
    @Test void legacyCupConfigurationKeepsHabitatAndUsesNewGeometryDefaults() {
        var json = JsonParser.parseString("""
                {"min_blocks":45,"max_blocks":180,"min_secondary_lobes":2,"max_secondary_lobes":5,
                 "max_shelf_strokes":3,"min_cavities":1,"max_cavities":3,"ichor_radius":7,
                 "anchor_search_budget":128,"min_height":2,"max_height":6,"min_span":6,
                 "max_span":14,"max_elongated_span":20}
                """);

        var decoded = EscharianOvergrowthConfiguration.CODEC.parse(JsonOps.INSTANCE, json).result();

        assertTrue(decoded.isPresent());
        assertEquals(10, decoded.orElseThrow().minPatchBlocks());
        assertEquals(30, decoded.orElseThrow().maxPatchBlocks());
        assertEquals(7, decoded.orElseThrow().ichorRadius());
        assertEquals(128, decoded.orElseThrow().anchorSearchBudget());
        assertEquals(8, decoded.orElseThrow().groundPileChance());
    }
    @Test void defaultsRoundTripAndReversedPatchBoundsFail() {
        var defaults=EscharianOvergrowthConfiguration.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{}"));
        assertEquals(EscharianOvergrowthConfiguration.DEFAULT,defaults.result().orElseThrow());
        var encoded=EscharianOvergrowthConfiguration.CODEC.encodeStart(JsonOps.INSTANCE,EscharianOvergrowthConfiguration.DEFAULT).result().orElseThrow();
        assertEquals(EscharianOvergrowthConfiguration.DEFAULT,EscharianOvergrowthConfiguration.CODEC.parse(JsonOps.INSTANCE,encoded).result().orElseThrow());
        assertTrue(EscharianOvergrowthConfiguration.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"min_patch_blocks\":30,\"max_patch_blocks\":10}")).error().isPresent());
    }
}
