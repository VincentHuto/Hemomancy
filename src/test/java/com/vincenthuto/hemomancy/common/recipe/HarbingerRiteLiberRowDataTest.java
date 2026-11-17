package com.vincenthuto.hemomancy.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** DS-4: completing any Harbinger rite opens a Liber page that carries a line about it. */
final class HarbingerRiteLiberRowDataTest {
	@Test
	void everyHarbingerRiteOpensALiberPage() throws IOException {
		Path rites = RepoPaths.DATA.resolve("recipe/cardinal_rite");
		List<String> missing = new ArrayList<>();
		int checked = 0;
		try (Stream<Path> files = Files.list(rites)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
				if (json.has("unstained") && json.get("unstained").getAsBoolean()) continue;
				String name = file.getFileName().toString();
				String path = "cardinal_rite/" + name.substring(0, name.length() - ".json".length());
				checked++;
				if (LiberEntryDefinitions.forRite(path).isEmpty()) missing.add(path);
			}
		}
		assertTrue(checked >= 49, "the scan found too few Harbinger rites: " + checked);
		assertEquals(List.of(), missing, "Harbinger rites whose completion opens no Liber page");
	}
}
