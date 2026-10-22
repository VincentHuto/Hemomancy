package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class VialCentrifugeModelSourceTest {
    private static final Path MODELS = Path.of("src/main/resources/assets/hemomancy/models/block");
    private static final Path RENDERER = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/VialCentrifugeRenderer.java");
    private static final Path RULES = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/VialCentrifugeRotorRules.java");
    private static final Path GENERATOR = Path.of("tools/model_export/vial_centrifuge.py");

    private static String read(Path path) throws IOException {
        return Files.readString(path).replace("\r\n", "\n");
    }

    private static List<String> partModels() throws IOException {
        var matcher = Pattern.compile("PART_MODELS = \\{(.*?)};", Pattern.DOTALL).matcher(read(RENDERER));
        assertTrue(matcher.find(), "renderer should list its part models");
        List<String> parts = new ArrayList<>();
        var names = Pattern.compile("\"([a-z_]+)\"").matcher(matcher.group(1));
        while (names.find()) parts.add(names.group(1));
        return parts;
    }

    @Test void everyRenderedPartIsGeneratedAndRegistered() throws IOException {
        var parts = partModels();
        assertEquals(16, parts.size());
        String generator = read(GENERATOR);
        for (String part : parts) {
            Path model = MODELS.resolve("vial_centrifuge_" + part + ".json");
            assertTrue(Files.isRegularFile(model), model + " should be exported");
            assertTrue(generator.contains("'" + part + "'"), part + " should be authored by the generator");
        }
        assertTrue(read(Path.of("src/main/java/com/vincenthuto/hemomancy/client/registry/ModelRegistrar.java"))
                .contains("for (String part : VialCentrifugeRenderer.PART_MODELS)"));
    }

    @Test void stageBodiesAndBlockstateKeepTheirParts() throws IOException {
        for (String body : List.of("vial_centrifuge", "vial_centrifuge_calibrated", "vial_centrifuge_fractionating")) {
            String json = read(MODELS.resolve(body + ".json"));
            assertTrue(json.contains("\"name\": \"bowl_wall\""), body + " should keep the splash bowl");
            assertTrue(json.contains("\"name\": \"jar_glass\""), body + " should keep the reservoir jar");
        }
        assertTrue(read(MODELS.resolve("vial_centrifuge_calibrated.json")).contains("\"name\": \"dial_rim\""));
        assertTrue(read(MODELS.resolve("vial_centrifuge_fractionating.json")).contains("\"name\": \"receiver_plasma\""));
        String states = read(Path.of("src/main/resources/assets/hemomancy/blockstates/vial_centrifuge.json"));
        for (String facing : List.of("north", "east", "south", "west"))
            for (int stage = 0; stage < 3; stage++)
                assertTrue(states.contains("\"facing=" + facing + ",stage=" + stage + "\""), facing + stage);
    }

    @Test void rendererPivotsMatchTheAuthoredModels() throws IOException {
        String renderer = read(RENDERER), generator = read(GENERATOR);
        assertTrue(generator.contains("PIVOT = (12.0, 14.75, 8.0)") && renderer.contains("PIVOT = {12F, 14.75F, 8F}"));
        assertTrue(generator.contains("CRANK_AXIS = (4.5, 8.0)") && renderer.contains("CRANK_AXIS = {0F, 4.5F, 8F}"));
        assertTrue(generator.contains("DIAL = (8.0, 4.75, 1.375)") && renderer.contains("DIAL = {8F, 4.75F, 1.375F}"));
        assertTrue(generator.contains("HINGE = (8.5, 22.0, 8.0)") && renderer.contains("HINGE = {8.5F, 22F, 8F}"));
        assertTrue(generator.contains("SPLEEN = (8.0, 4.5, 14.125)") && renderer.contains("SPLEEN = {8F, 4.5F, 14.125F}"));
        assertTrue(generator.contains("JAR_FLUID = (1.3, 2.4, 6.2)") && renderer.contains("JAR_DEPTH = 6.2F - 2.4F"));
        assertTrue(generator.contains("MAX_SWING = 50.0") && read(RULES).contains("MAX_SWING = 50F"),
                "the generator checks bucket clearance at the rules' full swing");
    }

    @Test void legacyEntityModelsAreGone() {
        for (String stale : List.of(
                "src/main/java/com/vincenthuto/hemomancy/client/model/tile/crafting/CentrifugeArmsModel.java",
                "src/main/java/com/vincenthuto/hemomancy/client/model/tile/crafting/CentrifugeStandModel.java",
                "src/main/resources/assets/hemomancy/textures/entity/model_centrifuge_arms.png",
                "src/main/resources/assets/hemomancy/textures/entity/model_centrifuge_stand.png",
                "tools/model_export/export_centrifuge_upgrades.py"))
            assertFalse(Files.exists(Path.of(stale)), stale);
    }
}
