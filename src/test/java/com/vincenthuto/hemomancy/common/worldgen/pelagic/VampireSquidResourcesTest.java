package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import com.google.gson.*;
import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class VampireSquidResourcesTest {
    private static final Path ROOT = Path.of("src/main/resources");
    private JsonObject json(String path) throws Exception { return JsonParser.parseString(Files.readString(ROOT.resolve(path))).getAsJsonObject(); }

    @Test void squidSharesExistingSpinesAndSupportsCaptureAndBreathing() throws Exception {
        var loot = json("data/hemomancy/loot_table/entities/vampire_squid.json");
        var entry = loot.getAsJsonArray("pools").get(0).getAsJsonObject().getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals("hemomancy:calcified_blood_spine", entry.get("name").getAsString());
        var lang = json("assets/hemomancy/lang/en_us.json");
        assertEquals("Barbed Splinter", lang.get("item.hemomancy.calcified_blood_spine").getAsString());
        assertEquals("Vampire Squid", lang.get("entity.hemomancy.vampire_squid").getAsString());
        assertTrue(lang.has("bestiary.hemomancy.specimen.vampire_squid.description"));
        for (var tag : List.of("data/hemomancy/tags/entity_type/specimen_jar_capturable.json",
                "data/minecraft/tags/entity_type/aquatic.json", "data/minecraft/tags/entity_type/can_breathe_under_water.json"))
            assertTrue(json(tag).getAsJsonArray("values").asList().stream().anyMatch(v -> v.getAsString().equals("hemomancy:vampire_squid")), tag);
    }

    @Test void editableSquidHasEightConnectedArmChainsAndAnAtlas() throws Exception {
        var model = json("assets/hemomancy/models/entity/bbmodel/pelagic/vampire_squid.bbmodel");
        var cubes = new HashSet<String>();
        for (var element : model.getAsJsonArray("elements")) {
            var cube = element.getAsJsonObject(); assertTrue(cubes.add(cube.get("uuid").getAsString()));
            for (var key : List.of("from", "to", "origin")) for (var coordinate : cube.getAsJsonArray(key))
                assertEquals(Math.rint(coordinate.getAsDouble() * 4), coordinate.getAsDouble() * 4);
            for (int i = 0; i < 3; i++) assertTrue(cube.getAsJsonArray("to").get(i).getAsDouble() - cube.getAsJsonArray("from").get(i).getAsDouble() >= .25);
        }
        var referenced = new ArrayList<String>(); var groups = new HashSet<String>();
        collect(model.getAsJsonArray("outliner"), referenced, groups);
        assertEquals(cubes.size(), referenced.size(), "Every cube must be attached exactly once");
        assertEquals(cubes, new HashSet<>(referenced));
        for (int arm = 0; arm < 8; arm++) for (int joint = 0; joint < 4; joint++)
            assertTrue(groups.contains("arm" + arm + "_" + joint));
        for (var animation : model.getAsJsonArray("animations")) for (var target : animation.getAsJsonObject().getAsJsonObject("animators").entrySet())
            assertTrue(groupIds(model.getAsJsonArray("outliner")).contains(target.getKey()));
        assertTrue(Files.size(ROOT.resolve("assets/hemomancy/textures/entity/pelagic/vampire_squid.png")) > 0);
    }
    private void collect(JsonArray nodes, List<String> cubes, Set<String> groups) {
        for (var node : nodes) {
            if (node.isJsonPrimitive()) cubes.add(node.getAsString());
            else { assertTrue(groups.add(node.getAsJsonObject().get("name").getAsString())); collect(node.getAsJsonObject().getAsJsonArray("children"), cubes, groups); }
        }
    }
    @Test void blockbenchCloakPreviewAlsoCoversTheMantle() throws Exception {
        var model = json("assets/hemomancy/models/entity/bbmodel/pelagic/vampire_squid.bbmodel");
        var animation = model.getAsJsonArray("animations").asList().stream().map(JsonElement::getAsJsonObject)
                .filter(a -> a.get("name").getAsString().equals("cloak")).findFirst().orElseThrow();
        var poses = new HashMap<String, JsonObject>(); collectGroups(model.getAsJsonArray("outliner"), poses);
        double angle = 0, y = 0, radius = 2.75;
        for (int joint = 0; joint < 4; joint++) {
            String name = "arm0_" + joint;
            var group = poses.get(name);
            var keys = animation.getAsJsonObject("animators").getAsJsonObject(group.get("uuid").getAsString()).getAsJsonArray("keyframes");
            double rest = group.getAsJsonArray("rotation").get(0).getAsDouble();
            double delta = keys.get(keys.size() - 1).getAsJsonObject().getAsJsonArray("data_points").get(0).getAsJsonObject().get("x").getAsDouble();
            // Blockbench's upward Y axis reverses the Java X-rotation sign.
            angle += Math.toRadians(-rest - delta);
            y += Math.cos(angle) * 2.75; radius += Math.sin(angle) * 2.75;
        }
        assertTrue(y < -8, "Editable cloak preview must lift the arm tips above the crown");
        assertTrue(Math.abs(radius) < 5, "Editable cloak must cup the mantle");
    }
    private void collectGroups(JsonArray nodes, Map<String, JsonObject> groups) {
        for (var node : nodes) if (node.isJsonObject()) {
            var group = node.getAsJsonObject(); groups.put(group.get("name").getAsString(), group);
            collectGroups(group.getAsJsonArray("children"), groups);
        }
    }
    private Set<String> groupIds(JsonArray nodes) {
        var ids = new HashSet<String>();
        for (var node : nodes) if (node.isJsonObject()) {
            ids.add(node.getAsJsonObject().get("uuid").getAsString()); ids.addAll(groupIds(node.getAsJsonObject().getAsJsonArray("children")));
        }
        return ids;
    }
}
