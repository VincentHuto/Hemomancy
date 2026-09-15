package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PhlegethonticBombardierResourceTest {
    private static final Path ROOT = Path.of("src/main/resources");

    @Test void naturalSpawnIsBasinOnlyAndLootIsExplicitlyEmpty() throws Exception {
        var spawn = JsonParser.parseString(Files.readString(ROOT.resolve(
                "data/hemomancy/neoforge/biome_modifier/add_phlegethontic_bombardier.json"))).getAsJsonObject();
        assertEquals("neoforge:add_spawns", spawn.get("type").getAsString());
        assertEquals("hemomancy:phlegethontic_basin", spawn.get("biomes").getAsString());
        var entry = spawn.getAsJsonObject("spawners");
        assertEquals("hemomancy:phlegethontic_bombardier", entry.get("type").getAsString());
        assertEquals(8, entry.get("weight").getAsInt());
        assertEquals(1, entry.get("minCount").getAsInt());
        assertEquals(3, entry.get("maxCount").getAsInt());

        var loot = JsonParser.parseString(Files.readString(ROOT.resolve(
                "data/hemomancy/loot_table/entities/phlegethontic_bombardier.json"))).getAsJsonObject();
        assertEquals(0, loot.getAsJsonArray("pools").size());
    }

    @Test void entityHasAuthoredClientResourcesAndFerventClassification() throws Exception {
        var image = ImageIO.read(ROOT.resolve(
                "assets/hemomancy/textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png").toFile());
        assertEquals(128, image.getWidth());
        assertEquals(128, image.getHeight());
        var blockbench = JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/hemomancy/models/entity/bbmodel/phlegethontic_bombardier.bbmodel"))).getAsJsonObject();
        assertEquals(128, blockbench.getAsJsonObject("resolution").get("width").getAsInt());
        assertEquals(128, blockbench.getAsJsonObject("resolution").get("height").getAsInt());
        var texture = blockbench.getAsJsonArray("textures").get(0).getAsJsonObject();
        assertEquals(128, texture.get("width").getAsInt());
        assertEquals(128, texture.get("height").getAsInt());
        assertEquals(128, texture.get("uv_width").getAsInt());
        assertEquals(128, texture.get("uv_height").getAsInt());
        assertArrayEquals(Files.readAllBytes(ROOT.resolve(
                        "assets/hemomancy/textures/entity/phlegethontic_bombardier/phlegethontic_bombardier.png")),
                java.util.Base64.getDecoder().decode(texture.get("source").getAsString().split(",", 2)[1]));
        var fervent = JsonParser.parseString(Files.readString(ROOT.resolve(
                "data/hemomancy/tags/entity_type/fervent.json"))).getAsJsonObject().getAsJsonArray("values");
        long matches = fervent.asList().stream().filter(value ->
                value.getAsString().equals("hemomancy:phlegethontic_bombardier")).count();
        assertEquals(1, matches);
        assertTrue(JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/hemomancy/lang/en_us.json"))).getAsJsonObject()
                .has("entity.hemomancy.phlegethontic_bombardier"));
    }

    @Test void stableStateNamesMatchPersistenceContract() {
        assertArrayEquals(new String[]{"IDLE", "CAMOUFLAGED", "WARNING", "GRAZING", "WINDUP", "FIRING", "COOLING"},
                java.util.Arrays.stream(BombardierState.values()).map(Enum::name).toArray(String[]::new));
    }

    @Test void spawnEggHasClientModelAndName() throws Exception {
        var model = JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/hemomancy/models/item/spawn_egg_phlegethontic_bombardier.json"))).getAsJsonObject();
        assertEquals("minecraft:item/template_spawn_egg", model.get("parent").getAsString());
        var language = JsonParser.parseString(Files.readString(ROOT.resolve(
                "assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
        assertEquals("Phlegethontic Bombardier Spawn Egg",
                language.get("item.hemomancy.spawn_egg_phlegethontic_bombardier").getAsString());
    }
}
