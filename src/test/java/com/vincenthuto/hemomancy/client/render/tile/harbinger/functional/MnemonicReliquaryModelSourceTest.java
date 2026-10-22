package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class MnemonicReliquaryModelSourceTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
    private static final Path MODELS = ASSETS.resolve("models/block");
    private static final Path RENDERER = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/MnemonicReliquaryRenderer.java");
    private static final Path BLOCK = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/MnemonicReliquaryBlock.java");
    private static final Path GENERATOR = Path.of("tools/model_export/mnemonic_reliquary.py");

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

    @Test void bodyLidAndRelicAreAuthoredModels() throws IOException {
        String body = read(MODELS.resolve("mnemonic_reliquary.json"));
        assertFalse(body.contains("cube_all"), "the body should be the authored casket, not a placeholder cube");
        for (String part : List.of("window_grille", "cushion", "hinge_knuckle"))
            assertTrue(body.contains("\"name\": \"" + part + "\""), part);
        assertTrue(Files.isRegularFile(ASSETS.resolve("textures/block/mnemonic_reliquary.png")));

        var parts = partModels();
        assertEquals(List.of("lid", "relic", "veins"), parts);
        String generator = read(GENERATOR);
        for (String part : parts) {
            Path model = MODELS.resolve("mnemonic_reliquary_" + part + ".json");
            assertTrue(Files.isRegularFile(model), model + " should be exported");
            assertTrue(generator.contains("'" + part + "'"), part + " should be authored by the generator");
        }
        assertTrue(read(Path.of("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"))
                .contains("for (String part : MnemonicReliquaryRenderer.PART_MODELS)"));
    }

    @Test void itemShowsTheClosedCasketAsABlockModel() throws IOException {
        String item = read(ASSETS.resolve("models/item/mnemonic_reliquary.json"));
        assertFalse(item.contains("builtin/entity"), "the item should no longer need a BEWLR");
        for (String part : List.of("window_grille", "ridge_beam", "brain_left"))
            assertTrue(item.contains("\"name\": \"" + part + "\""), "item should include " + part);
    }

    @Test void blockBakesTheBodyAndRotatesPerFacing() throws IOException {
        String block = read(BLOCK);
        assertTrue(block.contains("RenderShape.MODEL"));
        assertTrue(block.contains("getShape"), "the hitbox should follow the casket");
        String states = read(ASSETS.resolve("blockstates/mnemonic_reliquary.json"));
        assertTrue(states.contains("\"facing=north\""));
        assertTrue(states.contains("\"facing=east\"") && states.contains("\"y\": 90"));
        assertTrue(states.contains("\"facing=south\"") && states.contains("\"y\": 180"));
        assertTrue(states.contains("\"facing=west\"") && states.contains("\"y\": 270"));
    }

    @Test void rendererAnchorsMatchTheAuthoredModels() throws IOException {
        String renderer = read(RENDERER), generator = read(GENERATOR);
        assertTrue(generator.contains("HINGE = (8.0, 9.0, 12.5)") && renderer.contains("HINGE = {8F, 9F, 12.5F}"));
        assertTrue(generator.contains("LID_OPEN = 100.0") && renderer.contains("LID_OPEN = 100F"));
        assertTrue(generator.contains("RELIC_RISE = 4.5") && renderer.contains("RELIC_RISE = 4.5F"));
    }

    @Test void legacyEntityModelIsGone() {
        for (String stale : List.of(
                "src/main/java/com/vincenthuto/hemomancy/client/model/tile/functional/MnemonicReliquaryModel.java",
                "src/main/java/com/vincenthuto/hemomancy/client/render/item/tile/harbinger/functional/MnemonicReliquaryItemRenderer.java",
                "src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/functional/MnemonicReliquaryBlockItem.java",
                "src/main/resources/assets/hemomancy/textures/entity/model_mnemonic_reliquary.png",
                "src/main/resources/assets/hemomancy/models/block/bbmodel/MnemonicReliquaryModel.bbmodel"))
            assertFalse(Files.exists(Path.of(stale)), stale + " should be removed");
    }
}
