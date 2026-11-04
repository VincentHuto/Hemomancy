package com.vincenthuto.hemomancy.common.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Every {@code "translate"} key a data file names (advancements, loot, dialogue) has English (cleanup audit RS-5). */
class DataTranslateKeysTest {
	@Test
	void everyTranslateKeyInDataHasEnglish() throws Exception {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		TreeSet<String> missing = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.DATA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				JsonElement root = JsonParser.parseString(Files.readString(file));
				collect(root, lang, missing, RepoPaths.display(file));
			}
		}
		assertEquals(new TreeSet<>(), missing, "data names a translate key that en_us.json lacks");
	}

	private static void collect(JsonElement element, JsonObject lang, TreeSet<String> missing, String file) {
		if (element.isJsonObject()) {
			JsonObject object = element.getAsJsonObject();
			if (object.has("translate") && object.get("translate").isJsonPrimitive()) {
				String key = object.get("translate").getAsString();
				if (key.contains("hemomancy") && !lang.has(key)) missing.add(file + ": " + key);
			}
			for (var entry : object.entrySet()) collect(entry.getValue(), lang, missing, file);
		} else if (element.isJsonArray()) {
			for (JsonElement child : element.getAsJsonArray()) collect(child, lang, missing, file);
		}
	}
}
