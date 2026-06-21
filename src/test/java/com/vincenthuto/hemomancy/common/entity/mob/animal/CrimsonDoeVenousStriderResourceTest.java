package com.vincenthuto.hemomancy.common.entity.mob.animal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CrimsonDoeVenousStriderResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private CrimsonDoeVenousStriderResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String entityInit = readSource("com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String itemInit = readSource("com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String clientEvents = readSource("com/vincenthuto/hemomancy/client/event/ClientEvents.java");
		String layerEvents = readSource("com/vincenthuto/hemomancy/client/event/LayerEvents.java");

		assertContains("crimson doe registry", entityInit, "crimson_doe = ENTITY_TYPES.register(");
		assertContains("crimson doe spawn placement", entityInit, "CrimsonDoeEntity::canSpawnHere");
		assertContains("crimson doe attributes", entityInit, "CrimsonDoeEntity.setAttributes().build()");
		assertContains("crimson doe spawn egg", itemInit, "spawn_egg_crimson_doe");
		assertContains("crimson doe renderer", clientEvents, "CrimsonDoeRenderer::new");
		assertContains("crimson doe model layer", layerEvents, "CrimsonDoeModel.LAYER_LOCATION");
		assertNaturalNoSkylightSpawns("crimson doe",
				readSource("com/vincenthuto/hemomancy/common/entity/mob/animal/CrimsonDoeEntity.java"));

		String crimsonModifier = readResource("data/hemomancy/neoforge/biome_modifier/add_crimson_doe.json");
		String crimsonTag = readResource("data/hemomancy/tags/worldgen/biome/crimson_doe_spawnlist.json");
		assertContains("crimson doe modifier type", crimsonModifier, "\"type\": \"neoforge:add_spawns\"");
		assertContains("crimson doe modifier entity", crimsonModifier, "\"type\": \"hemomancy:crimson_doe\"");
		assertContains("crimson doe plains spawn", crimsonTag, "\"minecraft:plains\"");
		assertContains("crimson doe meadow spawn", crimsonTag, "\"minecraft:meadow\"");
		assertContains("crimson doe fungal garden spawn", crimsonTag, "\"hemomancy:fungal_gardens\"");

		assertContains("venous strider registry", entityInit, "venous_strider = ENTITY_TYPES.register(");
		assertContains("venous strider spawn placement", entityInit, "VenousStriderEntity::canSpawnHere");
		assertContains("venous strider attributes", entityInit, "VenousStriderEntity.setAttributes().build()");
		assertContains("venous strider spawn egg", itemInit, "spawn_egg_venous_strider");
		assertContains("venous strider renderer", clientEvents, "VenousStriderRenderer::new");
		assertContains("venous strider model layer", layerEvents, "VenousStriderModel.LAYER_LOCATION");
		assertNaturalNoSkylightSpawns("venous strider",
				readSource("com/vincenthuto/hemomancy/common/entity/mob/animal/VenousStriderEntity.java"));

		String striderModifier = readResource("data/hemomancy/neoforge/biome_modifier/add_venous_strider.json");
		String striderTag = readResource("data/hemomancy/tags/worldgen/biome/venous_strider_spawnlist.json");
		String hemorrhagicPlateau = readResource("data/hemomancy/worldgen/biome/hemorrhagic_plateau.json");
		String striderLoot = readResource("data/hemomancy/loot_table/entities/venous_strider.json");
		String sabatonsRecipe = readResource("data/hemomancy/recipe/venous_strider_sabatons.json");
		String lang = readResource("assets/hemomancy/lang/en_us.json");
		String pinionModel = readResource("assets/hemomancy/models/item/venous_pinion.json");
		assertContains("venous strider modifier type", striderModifier, "\"type\": \"neoforge:add_spawns\"");
		assertContains("venous strider modifier entity", striderModifier, "\"type\": \"hemomancy:venous_strider\"");
		assertContains("venous strider mushroom spawn", striderTag, "\"minecraft:mushroom_fields\"");
		assertContains("venous strider swamp spawn", striderTag, "\"minecraft:mangrove_swamp\"");
		assertContains("venous strider fungal garden spawn", striderTag, "\"hemomancy:fungal_gardens\"");
		assertContains("venous strider fungal isles spawn", striderTag, "\"hemomancy:fungal_isles\"");
		assertContains("venous strider hemorrhagic plateau spawn", hemorrhagicPlateau,
				"\"type\": \"hemomancy:venous_strider\"");
		assertContains("venous pinion item registration", itemInit, "venous_pinion = BASEITEMS.register(");
		assertContains("venous pinion loot", striderLoot, "\"name\": \"hemomancy:venous_pinion\"");
		assertContains("sabatons recipe uses venous pinion", sabatonsRecipe, "\"item\": \"hemomancy:venous_pinion\"");
		assertNotContains("sabatons recipe no longer consumes chalybeate sclerite", sabatonsRecipe,
				"\"item\": \"hemomancy:chalybeate_sclerite\"");
		assertContains("venous pinion lang", lang, "\"item.hemomancy.venous_pinion\": \"Venous Pinion\"");
		assertContains("sabatons display name follows venous strider source", lang,
				"\"item.hemomancy.venous_strider_sabatons\": \"Venous Strider Sabatons\"");
		assertContains("venous pinion model texture", pinionModel, "\"layer0\": \"hemomancy:item/venous_pinion\"");
	}

	private static void assertNaturalNoSkylightSpawns(String label, String entitySource) {
		assertContains(label + " should allow natural no-skylight dimensions",
				entitySource, "!world.dimensionType().hasSkyLight()");
		assertContains(label + " should retain bright overworld spawn rule",
				entitySource, "world.getRawBrightness(pos, 0) > 8");
	}

	private static String readSource(String path) throws IOException {
		return read(SOURCE_ROOT.resolve(path));
	}

	private static String readResource(String path) throws IOException {
		return read(RESOURCE_ROOT.resolve(path));
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": found " + forbidden);
		}
	}
}
