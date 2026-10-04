package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.stream.JsonReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressionResourceIntegrityTest {
    @Test void everyLiberRiteMappingNamesAShippedRecipe() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/capability/player/shared/knowledge/discovery/LiberEntryDefinitions.java"));
        var mappings = Pattern.compile("register(?:Rite|UpgradeLesson)\\(\"([^\"]+)\"").matcher(source);
        while (mappings.find()) {
            assertTrue(Files.exists(Path.of("src/main/resources/data/hemomancy/recipe", mappings.group(1) + ".json")), mappings.group(1));
        }
        assertTrue(source.contains("registerRite(\"cardinal_rite/sanctified_rite\""), "D6 discovery missing");
    }

    @Test void languageFilesContainNoDuplicateKeys() throws Exception {
        try (var paths = Files.list(Path.of("src/main/resources/assets/hemomancy/lang"))) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".json")).toList()) {
                try (var reader = new JsonReader(Files.newBufferedReader(path))) {
                    var keys = new HashSet<String>();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String key = reader.nextName();
                        assertTrue(keys.add(key), path + " duplicate: " + key);
                        reader.skipValue();
                    }
                    reader.endObject();
                }
            }
        }
    }

    @Test void ledgerTransmitsCanonicalEndingState() throws Exception {
        String packet = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java"));
        assertTrue(packet.contains("EnumArchonPath archonPath"), "Ledger loses Apotheos state on the wire");
    }

    @Test void recipeDecodingDoesNotRetainCrossReloadCaches() throws Exception {
        for (String serializer : new String[]{"MemoryWeavingRecipeSerializer", "ScarRecipeSerializer"}) {
            String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/recipe/serializer", serializer + ".java"));
            assertFalse(source.contains("ALL_RECIPES"), serializer + " retains decoded recipes across reloads");
        }
    }

    @Test void allEffigySocketsFitBeforeThePrepareButton() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/MasonsEffigyScreen.java"));
        var origin = Pattern.compile("SOCKET_X = (\\d+)").matcher(source);
        var stride = Pattern.compile("int sx = x \\+ i \\* (\\d+)").matcher(source);
        var size = Pattern.compile("int socketSize = (\\d+)").matcher(source);
        var button = Pattern.compile("PrepareIconButton\\(leftPos \\+ (\\d+)").matcher(source);
        assertTrue(origin.find() && stride.find() && size.find() && button.find());
        int rightEdge = Integer.parseInt(origin.group(1)) + 6 * Integer.parseInt(stride.group(1)) + Integer.parseInt(size.group(1));
        assertTrue(rightEdge <= Integer.parseInt(button.group(1)), "Seventh socket overlaps Prepare");
    }
}
