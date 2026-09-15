package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.vincenthuto.hemomancy.common.worldgen.config.EscharianOvergrowthConfiguration;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EscharianOvergrowthResourceTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test void twoSimpleBlockResourcesSeparateTheDarkCenterFromThePaleRim() throws Exception {
        JsonObject state = json("assets/hemomancy/blockstates/escharian_overgrowth.json");
        JsonObject rimState = json("assets/hemomancy/blockstates/escharian_overgrowth_rim.json");
        assertTrue(state.has("variants"));
        assertTrue(rimState.has("variants"));
        assertFalse(state.has("multipart"));
        assertFalse(rimState.has("multipart"));
        assertEquals("hemomancy:block/escharian_overgrowth_center",
                state.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        assertEquals("hemomancy:block/escharian_overgrowth_rim",
                rimState.getAsJsonObject("variants").getAsJsonObject("").get("model").getAsString());
        for (String model : List.of("center", "rim")) {
            JsonObject blockModel = json("assets/hemomancy/models/block/escharian_overgrowth_" + model + ".json");
            assertEquals("minecraft:block/cube_all", blockModel.get("parent").getAsString());
            assertFalse(blockModel.has("elements"));
        }
        for (String layer : List.of("center", "rim")) {
            var texture = ROOT.resolve("assets/hemomancy/textures/block/escharian_overgrowth_" + layer + ".png");
            assertTrue(Files.isRegularFile(texture));
            var image = ImageIO.read(texture.toFile());
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
    }

    @Test void biomeOwnsTheOnlyPlacedReferenceAndNoUtilityResourcesExist() throws Exception {
        JsonObject biome = json("data/hemomancy/worldgen/biome/phlegethontic_basin.json");
        assertEquals("hemomancy:escharian_overgrowth",
                biome.getAsJsonArray("features").get(9).getAsJsonArray().get(0).getAsString());
        assertEquals(1, occurrences(biome.toString(), "hemomancy:escharian_overgrowth"));

        JsonObject placed = json("data/hemomancy/worldgen/placed_feature/escharian_overgrowth.json");
        assertEquals("minecraft:rarity_filter", placed.getAsJsonArray("placement").get(0).getAsJsonObject().get("type").getAsString());
        assertEquals(3, placed.getAsJsonArray("placement").get(0).getAsJsonObject().get("chance").getAsInt());
        assertFalse(Files.exists(ROOT.resolve("assets/hemomancy/models/item/escharian_overgrowth.json")));
        assertFalse(Files.exists(ROOT.resolve("assets/hemomancy/models/item/escharian_overgrowth_rim.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/loot_table/blocks/escharian_overgrowth.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/loot_table/blocks/escharian_overgrowth_rim.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/recipe/escharian_overgrowth.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/recipe/escharian_overgrowth_rim.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/advancement/escharian_overgrowth.json")));
        assertFalse(Files.exists(ROOT.resolve("data/hemomancy/advancement/escharian_overgrowth_rim.json")));
        JsonObject language = json("assets/hemomancy/lang/en_us.json");
        assertTrue(language.has("block.hemomancy.escharian_overgrowth"));
        assertTrue(language.has("block.hemomancy.escharian_overgrowth_rim"));
        assertFalse(language.has("item.hemomancy.escharian_overgrowth"));
        assertFalse(language.has("item.hemomancy.escharian_overgrowth_rim"));
        try (var modifiers = Files.list(ROOT.resolve("data/hemomancy/neoforge/biome_modifier"))) {
            assertEquals(0, modifiers.filter(path -> {
                try { return Files.readString(path).contains("escharian_overgrowth"); }
                catch (Exception exception) { throw new RuntimeException(exception); }
            }).count());
        }
    }

    @Test void configuredResourceAndJavaBiomeOrderMatchTheApprovedContract() throws Exception {
        JsonObject configured = json("data/hemomancy/worldgen/configured_feature/escharian_overgrowth.json");
        var decoded = EscharianOvergrowthConfiguration.CODEC.parse(JsonOps.INSTANCE, configured.get("config"))
                .result().orElseThrow();
        assertEquals(EscharianOvergrowthConfiguration.DEFAULT, decoded);

        String biomeSource = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/BiomeInit.java"));
        String registration = "generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,PlacedFeatureInit.ESCHARIAN_OVERGROWTH)";
        assertEquals(1, occurrences(biomeSource, registration));
    }

    @Test void scyphusResourcesKeepTheAuthoredModelAndSixFaceRotations() throws Exception {
        var variants=json("assets/hemomancy/blockstates/escharian_scyphus.json").getAsJsonObject("variants");
        String[] faces={"up","down","north","east","south","west"};
        int[] x={0,180,90,90,90,90},y={0,0,0,90,180,270};
        assertEquals(30,variants.size());
        int[] elements={0,9,18,27,36,9};
        for(int count=1;count<=5;count++) {
            String modelName="escharian_scyphus"+(count==5?"":"_"+count);
            for(int i=0;i<faces.length;i++) {
                var variant=variants.getAsJsonObject("count="+count+",facing="+faces[i]);
                assertEquals("hemomancy:block/"+modelName,variant.get("model").getAsString());
                assertEquals(x[i],variant.get("x").getAsInt());assertEquals(y[i],variant.get("y").getAsInt());
            }
            var model=json("assets/hemomancy/models/block/"+modelName+".json");
            assertEquals(elements[count],model.getAsJsonArray("elements").size());
            assertTrue(model.has("display"));
            assertEquals("hemomancy:block/escharian_overgrowth_center",model.getAsJsonObject("textures").get("0").getAsString());
            assertEquals("hemomancy:block/escharian_overgrowth_rim",model.getAsJsonObject("textures").get("1").getAsString());
        }
        assertEquals("hemomancy:block/escharian_scyphus",json("assets/hemomancy/models/item/escharian_scyphus.json").get("parent").getAsString());
        assertEquals("Escharian Scyphus",json("assets/hemomancy/lang/en_us.json").get("block.hemomancy.escharian_scyphus").getAsString());
        var loot=json("data/hemomancy/loot_table/blocks/escharian_scyphus.json").getAsJsonArray("pools").get(0).getAsJsonObject();
        assertEquals(1,loot.get("rolls").getAsInt());
        var entry=loot.getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("hemomancy:escharian_scyphus",entry.get("name").getAsString());
        var functions=entry.getAsJsonArray("functions");
        assertEquals(5,functions.size());
        for(int count=2;count<=5;count++) {
            var function=functions.get(count-2).getAsJsonObject();
            assertEquals(count,function.get("count").getAsInt());
            assertEquals(Integer.toString(count),function.getAsJsonArray("conditions").get(0).getAsJsonObject()
                    .getAsJsonObject("properties").get("count").getAsString());
        }
        assertEquals("minecraft:explosion_decay",functions.get(4).getAsJsonObject().get("function").getAsString());
    }

    private static JsonObject json(String relative) throws Exception {
        return JsonParser.parseString(Files.readString(ROOT.resolve(relative))).getAsJsonObject();
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int index = 0; (index = text.indexOf(needle, index)) >= 0; index += needle.length()) count++;
        return count;
    }
}
