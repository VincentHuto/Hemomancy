package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CircusPerformerResourceTest {
    private static final Path RESOURCES = Path.of("src/main/resources");
    private static final Path ASSETS = RESOURCES.resolve("assets/hemomancy");
    private static final Path TEXTURES = ASSETS.resolve("textures/entity/npc/harbinger/circus");
    private static final Path MODELS = ASSETS.resolve("models/entity/bbmodel/npc/harbinger/circus");
    private static final Map<String, String> MODELS_BY_ROLE = Map.of(
            "acrobat", "Acrobat", "fire_eater", "FireEater", "knife_thrower", "KnifeThrower",
            "stilt_walker", "StiltWalker");

    @Test
    void everyPerformerShipsItsAuthoredAtlasAndCoherentVariants() throws Exception {
        for (var entry : MODELS_BY_ROLE.entrySet()) {
            String role = entry.getKey();
            Path primary = TEXTURES.resolve(role + "_0.png");
            Path alternate = TEXTURES.resolve(role + "_1.png");
            var variant0 = ImageIO.read(primary.toFile());
            var variant1 = ImageIO.read(alternate.toFile());
            assertEquals(128, variant0.getWidth(), role);
            assertEquals(128, variant0.getHeight(), role);
            assertEquals(variant0.getWidth(), variant1.getWidth(), role);
            assertEquals(variant0.getHeight(), variant1.getHeight(), role);
            assertFalse(java.util.Arrays.equals(Files.readAllBytes(primary), Files.readAllBytes(alternate)), role);

            JsonObject model = JsonParser.parseString(Files.readString(
                    MODELS.resolve("Circus" + entry.getValue() + "Model.bbmodel"))).getAsJsonObject();
            String embedded = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
            assertArrayEquals(Files.readAllBytes(primary), Base64.getDecoder().decode(embedded.split(",", 2)[1]), role);

            boolean[][] used = new boolean[128][128];
            for (var node : model.getAsJsonArray("elements")) {
                JsonObject cube = node.getAsJsonObject();
                JsonArray start = cube.getAsJsonArray("from");
                JsonArray end = cube.getAsJsonArray("to");
                double[] size = new double[3];
                for (int axis = 0; axis < 3; axis++) {
                    size[axis] = end.get(axis).getAsDouble() - start.get(axis).getAsDouble();
                }
                List<String> faces = List.of("north", "east", "south", "west", "up", "down");
                for (int face = 0; face < faces.size(); face++) {
                    JsonArray uv = cube.getAsJsonObject("faces").getAsJsonObject(faces.get(face)).getAsJsonArray("uv");
                    int x0 = uv.get(0).getAsInt(), y0 = uv.get(1).getAsInt();
                    int x1 = uv.get(2).getAsInt(), y1 = uv.get(3).getAsInt();
                    assertEquals(size[face == 1 || face == 3 ? 2 : 0], x1 - x0, role);
                    assertEquals(size[face < 4 ? 1 : 2], y1 - y0, role);
                    assertTrue(x0 >= 0 && y0 >= 0 && x1 <= 128 && y1 <= 128, role);
                    for (int x = x0; x < x1; x++) {
                        for (int y = y0; y < y1; y++) {
                            assertFalse(used[x][y], role + " overlapping UVs");
                            used[x][y] = true;
                            assertEquals(255, variant0.getRGB(x, y) >>> 24, role);
                            assertEquals(255, variant1.getRGB(x, y) >>> 24, role);
                        }
                    }
                }
            }
            assertTrue(Files.isRegularFile(RESOURCES.resolve(
                    "data/hemomancy/loot_table/entities/circus_" + role + ".json")));
        }
        assertTrue(Files.isRegularFile(RESOURCES.resolve("data/hemomancy/tags/entity_type/circus_performers.json")));
    }
}
