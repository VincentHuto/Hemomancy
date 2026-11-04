package com.vincenthuto.hemomancy.common.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.support.RepoPaths;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** A parent that does not resolve silently drops the advancement and every descendant at load, so nothing can earn them. */
class AdvancementParentResourceTest {
	@Test
	void everyHemomancyAdvancementParentExists() throws IOException {
		Path root = RepoPaths.DATA.resolve("advancement");
		List<String> broken = new ArrayList<>();
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
				var parent = JsonParser.parseString(Files.readString(file)).getAsJsonObject().get("parent");
				if (parent == null || !parent.getAsString().startsWith("hemomancy:")) continue;
				String path = parent.getAsString().substring("hemomancy:".length());
				if (!Files.exists(root.resolve(path + ".json"))) broken.add(root.relativize(file) + " -> " + parent.getAsString());
			}
		}
		assertEquals(List.of(), broken);
	}
}
