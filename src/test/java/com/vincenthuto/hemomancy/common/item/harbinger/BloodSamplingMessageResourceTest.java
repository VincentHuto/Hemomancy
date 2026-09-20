package com.vincenthuto.hemomancy.common.item.harbinger;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloodSamplingMessageResourceTest {
    @Test void restrictedSamplingExplainsThatTheTargetIsTooToughForAStandaloneNeedle() throws Exception {
        var language = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        assertEquals("Their hide is too tough to pierce with a needle alone.",
                language.get(BloodSamplingResult.REQUIRES_LIVING_SYRINGE.translationKey()).getAsString());
    }
}
