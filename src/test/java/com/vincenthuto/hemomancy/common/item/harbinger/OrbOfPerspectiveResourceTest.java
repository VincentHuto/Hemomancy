package com.vincenthuto.hemomancy.common.item.harbinger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

final class OrbOfPerspectiveResourceTest {
	@Test
	void exactRecipeRegistrationModelTextureTranslationTooltipAndDiscoveryExist() throws IOException {
		JsonObject recipe = json("src/main/resources/data/hemomancy/recipe/orb_of_perspective.json");
		assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
		assertArrayEquals(new String[] {"MEM", "EBE", "MEM"},
				recipe.getAsJsonArray("pattern").asList().stream().map(e -> e.getAsString()).toArray(String[]::new));
		assertEquals("hemomancy:monolith_fragment", recipe.getAsJsonObject("key").getAsJsonObject("M").get("item").getAsString());
		assertEquals("minecraft:echo_shard", recipe.getAsJsonObject("key").getAsJsonObject("E").get("item").getAsString());
		assertEquals("hemomancy:blood_crystal_shard", recipe.getAsJsonObject("key").getAsJsonObject("B").get("item").getAsString());
		assertEquals("hemomancy:orb_of_perspective", recipe.getAsJsonObject("result").get("id").getAsString());

		JsonObject model = json("src/main/resources/assets/hemomancy/models/item/orb_of_perspective.json");
		assertEquals("minecraft:item/generated", model.get("parent").getAsString());
		assertEquals("hemomancy:item/orb_of_perspective",
				model.getAsJsonObject("textures").get("layer0").getAsString());
		BufferedImage texture = ImageIO.read(Path.of(
				"src/main/resources/assets/hemomancy/textures/item/orb_of_perspective.png").toFile());
		assertEquals(32, texture.getWidth());
		assertEquals(32, texture.getHeight());
		assertTrue(texture.getColorModel().hasAlpha());
		JsonObject language = json("src/main/resources/assets/hemomancy/lang/en_us.json");
		assertTrue(language.has("item.hemomancy.orb_of_perspective"));
		assertTrue(language.has("item.hemomancy.orb_of_perspective.tooltip"));
		assertTrue(Files.isRegularFile(Path.of("src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/orb_of_perspective.json")));

		String itemInit = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String itemClass = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/OrbOfPerspectiveItem.java"));
		String events = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/ChamberOfWillEvents.java"));
		assertTrue(itemInit.contains("orb_of_perspective"));
		assertTrue(itemInit.contains("stacksTo(1)"));
		assertTrue(itemClass.contains("appendHoverText"));
		assertTrue(itemClass.contains("item.hemomancy.orb_of_perspective.tooltip"));
		assertTrue(events.contains("OrbOfPerspectiveRules.activation"));
		assertTrue(events.contains("HANDLED_KEY"));
		assertTrue(events.contains("MycophantEncounterManager.isActive(owner)"));
		assertTrue(events.contains("VesperOrdealManager.isActive(owner)"));
	}

	private static JsonObject json(String path) throws IOException {
		return JsonParser.parseString(Files.readString(Path.of(path))).getAsJsonObject();
	}
}
