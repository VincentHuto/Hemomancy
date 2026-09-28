package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnglishLocalizationEncodingTest {
	private static final Path LANGUAGE = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");
	private static final Path DIALOGUE_SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue");
	private static final Pattern PROMPT_KEY = Pattern.compile(
			"DialogueOptionPresentation\\.(?:prompt|attention)\\([^;]*?\\\"(hemomancy\\.[^\\\"]+)\\\"\\)",
			Pattern.DOTALL);
	private static final String LEGACY_FORMAT_CODES = "0123456789abcdefklmnor";
	private static final List<String> MOJIBAKE_SEQUENCES = List.of(
			"\u00c2\u00a7",
			"\u00c2\u00b7",
			"\u00e2\u20ac",
			"\u00e2\u2020",
			"\ufffd");

	@Test
	void playerFacingTextDoesNotContainMojibake() throws IOException {
		JsonObject language = JsonParser.parseString(Files.readString(LANGUAGE)).getAsJsonObject();

		for (var entry : language.entrySet()) {
			String value = entry.getValue().getAsString();
			for (String sequence : MOJIBAKE_SEQUENCES) {
				assertFalse(value.contains(sequence),
						() -> entry.getKey() + " contains malformed encoded text");
			}
		}
	}

	@Test
	void legacyFormattingMarkersHaveAValidCode() throws IOException {
		JsonObject language = JsonParser.parseString(Files.readString(LANGUAGE)).getAsJsonObject();

		for (var entry : language.entrySet()) {
			String value = entry.getValue().getAsString();
			for (int index = value.indexOf('\u00a7'); index >= 0; index = value.indexOf('\u00a7', index + 2)) {
				assertTrue(index + 1 < value.length(),
						entry.getKey() + " ends with an incomplete formatting marker");
				char code = Character.toLowerCase(value.charAt(index + 1));
				assertTrue(LEGACY_FORMAT_CODES.indexOf(code) >= 0,
						entry.getKey() + " contains an invalid formatting code: " + code);
			}
		}
	}

	@Test
	void authoredDialogueActionPromptsHaveEnglishText() throws IOException {
		JsonObject language = JsonParser.parseString(Files.readString(LANGUAGE)).getAsJsonObject();
		assertTrue(language.has("hemomancy.dialogue.action.confirm"));
		assertTrue(language.has("hemomancy.dialogue.conversation.prompt"));

		try (var paths = Files.walk(DIALOGUE_SOURCE)) {
			for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
				var matcher = PROMPT_KEY.matcher(Files.readString(path));
				while (matcher.find()) {
					String key = matcher.group(1);
					assertTrue(language.has(key), () -> path.getFileName() + " references missing prompt " + key);
				}
			}
		}

		for (String lesson : List.of("microscope", "injection", "cabinet", "field_referral",
				"field_case", "echo_referral", "clairaudiograph")) {
			String key = "hemomancy.clinical." + lesson + ".prompt";
			assertTrue(language.has(key), () -> "Clinical lesson references missing prompt " + key);
		}
	}
}
