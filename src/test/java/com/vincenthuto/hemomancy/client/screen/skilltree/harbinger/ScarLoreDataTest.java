package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ScarLoreDataTest {
	private static final Path RESOURCES = Path.of("src/main/resources");

	@Test
	void everyScarRecipeUsesLoreFromTheLanguageFile() throws IOException {
		JsonObject language = JsonParser.parseString(Files.readString(
				RESOURCES.resolve("assets/hemomancy/lang/en_us.json"))).getAsJsonObject();

		for (String directory : new String[] {"scar", "fungal_scar"}) {
			try (Stream<Path> recipes = Files.list(RESOURCES.resolve("data/hemomancy/recipe").resolve(directory))) {
				recipes.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
					String id = path.getFileName().toString().replaceFirst("\\.json$", "");
					TranslatableContents lore = assertInstanceOf(TranslatableContents.class,
							ScarLoreData.getLore(id).getContents());
					assertTrue(language.has(lore.getKey()), () -> "Missing scar lore translation: " + lore.getKey());
				});
			}
		}
	}

	@Test
	void scarLoreAvoidsEmDashes() throws IOException {
		JsonObject language = JsonParser.parseString(Files.readString(
				RESOURCES.resolve("assets/hemomancy/lang/en_us.json"))).getAsJsonObject();
		language.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("screen.hemomancy.scars.lore."))
				.forEach(entry -> assertTrue(!entry.getValue().getAsString().contains("—"),
						() -> entry.getKey() + " still contains an em dash"));
	}
}
