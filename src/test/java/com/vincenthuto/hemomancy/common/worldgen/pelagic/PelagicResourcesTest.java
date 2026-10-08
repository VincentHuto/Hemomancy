package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PelagicResourcesTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private static final String[] SPECIES = {"chiton", "pyrosome", "pelagic_herring", "siphonophore", "bloody_belly_comb_jelly", "hagfish"};
    private JsonObject json(String path) throws Exception { return JsonParser.parseString(Files.readString(ROOT.resolve(path))).getAsJsonObject(); }

    @Test void pyrosomesHavePopulationRoomIndependentOfLargeSwimmers() throws Exception {
        for (var layer : List.of(PelagicLayer.OPEN, PelagicLayer.TWILIGHT)) {
            var entry = PelagicPopulation.entries(layer).stream()
                    .filter(e -> e.entity().equals("hemomancy:pyrosome")).findFirst().orElseThrow();
            assertEquals("hemomancy:pyrosomes", entry.category());
        }
        var extension = json("META-INF/enumextensions.json").getAsJsonArray("entries").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .filter(e -> e.get("name").getAsString().equals("HEMOMANCY_PYROSOMES"))
                .findFirst().orElseThrow();
        var parameters = extension.getAsJsonArray("parameters");
        assertEquals("hemomancy:pyrosomes", parameters.get(0).getAsString());
        assertEquals(8, parameters.get(1).getAsInt());
        assertTrue(parameters.get(2).getAsBoolean());
        assertFalse(parameters.get(3).getAsBoolean());
        assertEquals(128, parameters.get(4).getAsInt());
    }

    @Test void biomeResourcesMatchBootstrapPacksAndDoNotDuplicateSpecies() throws Exception {
        for (var layer : PelagicLayer.values()) {
            if (layer == PelagicLayer.REEF) continue;
            var spawns = json("data/hemomancy/worldgen/biome/" + layer.id + ".json").getAsJsonObject("spawners");
            Set<String> actual = new HashSet<>();
            for (var category : spawns.entrySet()) for (var element : category.getValue().getAsJsonArray()) {
                var row = element.getAsJsonObject();
                assertTrue(actual.add(row.get("type").getAsString()), layer + " duplicate spawn");
            }
            assertEquals(PelagicPopulation.entries(layer).size(), actual.size());
            for (var entry : PelagicPopulation.entries(layer)) {
                var row = spawns.getAsJsonArray(entry.category()).asList().stream().map(JsonElement::getAsJsonObject)
                        .filter(j -> j.get("type").getAsString().equals(entry.entity())).findFirst().orElseThrow();
                assertEquals(entry.weight(), row.get("weight").getAsInt());
                assertEquals(entry.min(), row.get("minCount").getAsInt());
                assertEquals(entry.max(), row.get("maxCount").getAsInt());
                if (entry.entity().endsWith("pelagic_herring")) { assertEquals(6, entry.min()); assertEquals(10, entry.max()); }
            }
        }
        var modifierBiomes = json("data/hemomancy/tags/worldgen/biome/prism_cuttle_spawnlist.json").getAsJsonArray("values");
        assertFalse(modifierBiomes.asList().stream().anyMatch(v -> v.getAsString().equals("hemomancy:erythrocoral_reef")));
        assertTrue(modifierBiomes.asList().stream().anyMatch(v -> v.getAsString().equals("minecraft:warm_ocean")));
    }

    @Test void sharedMaterialsDoNotInventAnIngredientForEveryCreature() throws Exception {
        var allowed = Set.of("hemomancy:chitinous_husk", "hemomancy:cuttlefish_chromatophores",
                "hemomancy:ganglion_cluster", "hemomancy:raw_pelagic_herring");
        for (var id : SPECIES) {
            var pools = json("data/hemomancy/loot_table/entities/" + id + ".json").getAsJsonArray("pools");
            if (id.equals("pyrosome") || id.equals("hagfish")) assertTrue(pools.isEmpty(), id);
            for (var pool : pools) for (var entry : pool.getAsJsonObject().getAsJsonArray("entries"))
                assertTrue(allowed.contains(entry.getAsJsonObject().get("name").getAsString()), id);
        }
    }

    @Test void editableRigsUseQuarterGridAndConnectedUniqueOutliners() throws Exception {
        var rigs = new ArrayList<>(List.of(SPECIES));
        rigs.addAll(List.of("tidepool_anemone", "bone_worm_colony", "giant_tube_worm_colony"));
        for (var id : rigs) {
            var model = json("assets/hemomancy/models/entity/bbmodel/pelagic/" + id + ".bbmodel");
            Set<String> cubes = new HashSet<>();
            for (var element : model.getAsJsonArray("elements")) {
                var cube = element.getAsJsonObject(); assertTrue(cubes.add(cube.get("uuid").getAsString()));
                for (var key : List.of("from", "to", "origin")) for (var coordinate : cube.getAsJsonArray(key))
                    assertEquals(Math.rint(coordinate.getAsDouble() * 4), coordinate.getAsDouble() * 4, id + " off-grid geometry");
                for (int i = 0; i < 3; i++) assertTrue(cube.getAsJsonArray("to").get(i).getAsDouble() - cube.getAsJsonArray("from").get(i).getAsDouble() >= .25);
            }
            var references = new ArrayList<String>(); collect(model.getAsJsonArray("outliner"), references);
            assertEquals(cubes.size(), references.size(), id + " orphaned or duplicate cube");
            assertEquals(cubes, new HashSet<>(references));
            assertTrue(Files.exists(ROOT.resolve("assets/hemomancy/textures/entity/pelagic/" + id + ".png")));
        }
    }
    private void collect(JsonArray nodes, List<String> cubes) {
        for (var node : nodes) {
            if (node.isJsonPrimitive()) cubes.add(node.getAsString());
            else collect(node.getAsJsonObject().getAsJsonArray("children"), cubes);
        }
    }
}
