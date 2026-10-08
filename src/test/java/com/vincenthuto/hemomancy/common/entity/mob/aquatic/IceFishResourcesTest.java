package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class IceFishResourcesTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private JsonObject json(String path) throws Exception {
        return JsonParser.parseString(Files.readString(ROOT.resolve(path))).getAsJsonObject();
    }

    @Test void coldFishSharesHemolymphAndHasTendencyFreeAquaticBlood() throws Exception {
        assertEquals(json("data/hemomancy/loot_table/entities/hemolymphopoda.json"),
                json("data/hemomancy/loot_table/entities/ice_fish.json"));
        var profile = json("data/hemomancy/blood_profiles/ice_fish.json");
        assertTrue(profile.getAsJsonArray("tendencies").isEmpty());
        assertEquals("hemomancy:blood_properties/aquatic", profile.getAsJsonArray("properties").get(0).getAsString());
        assertFalse(profile.get("requires_living_syringe").getAsBoolean());
        for (String path : List.of("data/hemomancy/tags/entity_type/specimen_jar_capturable.json",
                "data/minecraft/tags/entity_type/aquatic.json", "data/minecraft/tags/entity_type/can_breathe_under_water.json"))
            assertTrue(json(path).getAsJsonArray("values").asList().stream().anyMatch(v -> v.getAsString().equals("hemomancy:ice_fish")), path);
        var lang = json("assets/hemomancy/lang/en_us.json");
        assertTrue(lang.get("item.hemomancy.cleansing_hemolymph.tooltip").getAsString().contains("Ice Fish"));
        for (String key : List.of("title", "description", "source")) assertTrue(lang.has("bestiary.hemomancy.specimen.ice_fish."+key));
    }

    @Test void editableFishHasConnectedTailAndQuarterGridGeometry() throws Exception {
        var model = json("assets/hemomancy/models/entity/bbmodel/IceFish.bbmodel");
        var cubes = new HashSet<String>();
        for (var node : model.getAsJsonArray("elements")) {
            var cube = node.getAsJsonObject(); assertTrue(cubes.add(cube.get("uuid").getAsString()));
            for (String key : List.of("from", "to", "origin")) for (var coordinate : cube.getAsJsonArray(key))
                assertEquals(Math.rint(coordinate.getAsDouble()*4), coordinate.getAsDouble()*4);
            for (int i=0;i<3;i++) assertTrue(cube.getAsJsonArray("to").get(i).getAsDouble()-cube.getAsJsonArray("from").get(i).getAsDouble() >= .25);
        }
        var references = new ArrayList<String>(); var groups = new HashMap<String, JsonObject>();
        collect(model.getAsJsonArray("outliner"), references, groups);
        assertEquals(cubes.size(), references.size()); assertEquals(cubes, new HashSet<>(references));
        assertTrue(groups.get("tail").getAsJsonArray("children").asList().stream()
                .anyMatch(n -> n.isJsonObject() && n.getAsJsonObject().get("name").getAsString().equals("tail_tip")));
        var ids = groups.values().stream().map(g -> g.get("uuid").getAsString()).toList();
        for (var animation : model.getAsJsonArray("animations")) for (var target : animation.getAsJsonObject().getAsJsonObject("animators").entrySet())
            assertTrue(ids.contains(target.getKey()));
        assertTrue(Files.size(ROOT.resolve("assets/hemomancy/textures/entity/ice_fish.png")) > 0);
    }

    private void collect(JsonArray nodes, List<String> references, Map<String, JsonObject> groups) {
        for (var node : nodes) {
            if (node.isJsonPrimitive()) references.add(node.getAsString());
            else { var group=node.getAsJsonObject(); assertNull(groups.put(group.get("name").getAsString(), group)); collect(group.getAsJsonArray("children"), references, groups); }
        }
    }
}
