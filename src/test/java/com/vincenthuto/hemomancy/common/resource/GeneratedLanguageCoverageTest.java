package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * processResources excludes duplicates, so the authored en_us.json completely shadows the
 * datagen copy. A key that only exists in the generated file renders as a raw key in game.
 */
class GeneratedLanguageCoverageTest {
	private static final Path MAIN_LANGUAGE = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path GENERATED_LANGUAGE = Path.of(
			"src/generated/resources/assets/hemomancy/lang/en_us.json");

	@Test
	void everyGeneratedKeyIsPresentInTheAuthoredLanguageFile() throws IOException {
		JsonObject main = JsonParser.parseString(Files.readString(MAIN_LANGUAGE)).getAsJsonObject();
		JsonObject generated = JsonParser.parseString(Files.readString(GENERATED_LANGUAGE)).getAsJsonObject();

		List<String> missing = generated.keySet().stream()
				.filter(key -> !main.has(key))
				.sorted()
				.toList();

		assertTrue(missing.isEmpty(), () -> missing.size()
				+ " generated lang keys are shadowed by src/main en_us.json and will render raw: "
				+ String.join(", ", missing));
	}
}
