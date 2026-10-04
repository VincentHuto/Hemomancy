package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientFontCodepointResourceTest {
    @Test void bitmapProvidersDoNotRepeatNonzeroCodepoints() throws Exception {
        var duplicates = new ArrayList<String>();
        try (var paths = Files.walk(Path.of("src/main/resources/assets/hemomancy/font"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                var definition = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
                for (var provider : definition.getAsJsonArray("providers")) {
                    var bitmap = provider.getAsJsonObject();
                    if (!bitmap.get("type").getAsString().endsWith("bitmap")) continue;
                    var seen = new HashSet<Integer>();
                    for (var row : bitmap.getAsJsonArray("chars")) {
                        for (int codepoint : row.getAsString().codePoints().toArray()) {
                            if (codepoint != 0 && !seen.add(codepoint))
                                duplicates.add(path + " repeats " + Integer.toHexString(codepoint));
                        }
                    }
                }
            }
        }
        assertEquals(List.of(), duplicates);
    }
}
