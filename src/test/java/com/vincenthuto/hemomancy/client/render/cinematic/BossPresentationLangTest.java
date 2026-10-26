package com.vincenthuto.hemomancy.client.render.cinematic;

import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every title card a scene can show has its name and epithet in en_us.json, and so does the skip hint. */
class BossPresentationLangTest {
	private static final Pattern CARD = Pattern.compile("(?:TitleCard\\.of|new PhaseCardScene)\\(\"([a-z0-9_]+)\"");
	/** The Saints' card ids come from their entity ids at run time. */
	private static final Set<String> SAINTS = Set.of("hemorath", "putriciel", "velorum", "seraphae");

	@Test
	void everyCardHasItsLines() throws IOException {
		JsonObject lang = ResourceReader.asset("lang/en_us.json");
		Set<String> ids = new TreeSet<>(SAINTS);
		try (Stream<Path> files = Files.walk(RepoPaths.mainJava("client/render/cinematic"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = CARD.matcher(SourceAssertions.read(file));
				while (matcher.find()) ids.add(matcher.group(1));
			}
		}
		assertTrue(ids.size() > SAINTS.size(), "found the scenes' cards");
		Set<String> missing = new TreeSet<>();
		for (String id : ids) {
			for (String line : new String[] { ".name", ".epithet" }) {
				String key = "boss.hemomancy." + id + line;
				if (!lang.has(key)) missing.add(key);
			}
		}
		assertEquals(Set.of(), missing);
		assertTrue(lang.has("boss.hemomancy.scene.skip"));
	}
}
