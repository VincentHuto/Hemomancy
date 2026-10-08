package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class OsteophageResourcesTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private JsonObject json(String path) throws Exception {
        return JsonParser.parseString(Files.readString(ROOT.resolve(path))).getAsJsonObject();
    }

    @Test void valleyScavengerSharesPinionsAndSupportsClinicalResearch() throws Exception {
        var tag = json("data/hemomancy/tags/worldgen/biome/osteophage_spawnlist.json");
        assertEquals(List.of("minecraft:soul_sand_valley"), tag.getAsJsonArray("values").asList().stream().map(JsonElement::getAsString).toList());
        var loot = json("data/hemomancy/loot_table/entities/osteophage.json");
        var entry = loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("hemomancy:venous_pinion", entry.get("name").getAsString());
        assertTrue(json("data/hemomancy/tags/entity_type/specimen_jar_capturable.json").getAsJsonArray("values").asList().stream()
                .anyMatch(v -> v.getAsString().equals("hemomancy:osteophage")));
        assertEquals("mortem", json("data/hemomancy/blood_profiles/osteophage.json").getAsJsonArray("tendencies").get(0).getAsString());
        var lang = json("assets/hemomancy/lang/en_us.json");
        assertEquals("Osteophage Vulture", lang.get("entity.hemomancy.osteophage").getAsString());
        for (String key : List.of("title", "description", "source")) assertTrue(lang.has("bestiary.hemomancy.specimen.osteophage." + key));
    }

    @Test void editableRigUsesConnectedWingsValidUvsAndQuarterGridGeometry() throws Exception {
        var model = json("assets/hemomancy/models/entity/bbmodel/Osteophage.bbmodel");
        var cubes = new HashSet<String>();
        for (var element : model.getAsJsonArray("elements")) {
            var cube = element.getAsJsonObject(); assertTrue(cubes.add(cube.get("uuid").getAsString()));
            for (String key : List.of("from", "to", "origin")) for (var coordinate : cube.getAsJsonArray(key))
                assertEquals(Math.rint(coordinate.getAsDouble()*4), coordinate.getAsDouble()*4, "Geometry must lie on the quarter grid");
            for (int i=0; i<3; i++) assertTrue(cube.getAsJsonArray("to").get(i).getAsDouble()-cube.getAsJsonArray("from").get(i).getAsDouble() >= .25);
            for (var face : cube.getAsJsonObject("faces").entrySet()) {
                var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
                for (int i=0; i<4; i++) assertTrue(uv.get(i).getAsDouble() >= 0 && uv.get(i).getAsDouble() <= 256, "UV outside atlas");
            }
        }
        var references = new ArrayList<String>(); var groups = new HashMap<String, JsonObject>();
        collect(model.getAsJsonArray("outliner"), references, groups);
        assertEquals(cubes.size(), references.size()); assertEquals(cubes, new HashSet<>(references));
        for (String side : List.of("left", "right")) {
            assertTrue(groups.get(side+"_wing").getAsJsonArray("children").asList().stream().anyMatch(n -> n.isJsonObject() && n.getAsJsonObject().get("name").getAsString().equals(side+"_forearm")));
            assertTrue(groups.get(side+"_forearm").getAsJsonArray("children").asList().stream().anyMatch(n -> n.isJsonObject() && n.getAsJsonObject().get("name").getAsString().equals(side+"_primaries")));
        }
        var ids = groups.values().stream().map(g -> g.get("uuid").getAsString()).toList();
        for (var animation : model.getAsJsonArray("animations")) for (var target : animation.getAsJsonObject().getAsJsonObject("animators").entrySet())
            assertTrue(ids.contains(target.getKey()));
        assertTrue(Files.size(ROOT.resolve("assets/hemomancy/textures/entity/osteophage.png")) > 0);
    }

    private void collect(JsonArray nodes, List<String> cubes, Map<String, JsonObject> groups) {
        for (var node : nodes) {
            if (node.isJsonPrimitive()) cubes.add(node.getAsString());
            else { var group = node.getAsJsonObject(); assertNull(groups.put(group.get("name").getAsString(), group)); collect(group.getAsJsonArray("children"), cubes, groups); }
        }
    }
}
