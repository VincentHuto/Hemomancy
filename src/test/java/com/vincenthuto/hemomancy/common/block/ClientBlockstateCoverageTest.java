package com.vincenthuto.hemomancy.common.block;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientBlockstateCoverageTest {
    @Test void everyRegisteredEngramStateHasExactlyOneModel() throws Exception {
        var variants = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/blockstates/engram_block.json")))
                .getAsJsonObject().getAsJsonObject("variants");
        for (int character = 0; character < 26; character++) {
            for (String facing : new String[]{"up", "down", "north", "east", "south", "west"}) {
                for (boolean lit : new boolean[]{false, true}) {
                    for (boolean waterlogged : new boolean[]{false, true}) {
                        var state = Map.of("character", Integer.toString(character), "facing", facing,
                                "lit", Boolean.toString(lit), "waterlogged", Boolean.toString(waterlogged));
                        int matches = 0;
                        for (var variant : variants.entrySet()) {
                            boolean match = true;
                            for (String selector : variant.getKey().split(",")) {
                                var property = selector.split("=");
                                match &= property[1].equals(state.get(property[0]));
                            }
                            if (match) {
                                matches++;
                                if (facing.equals("down"))
                                    assertEquals("minecraft:block/air", variant.getValue().getAsJsonObject().get("model").getAsString());
                            }
                        }
                        assertEquals(1, matches, state.toString());
                    }
                }
            }
        }
    }

    @Test void invisibleEmitterHasAnEmptyModel() throws Exception {
        var state = JsonParser.parseString(Files.readString(Path.of(
                "src/main/resources/assets/hemomancy/blockstates/abocipher_emitter.json"))).getAsJsonObject();
        assertEquals("minecraft:block/air", state.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
    }
}
