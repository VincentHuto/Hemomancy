package com.vincenthuto.hemomancy.common.block.harbinger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EngramBlockResourceTest {
	private static final String[] FACINGS = { "up", "north", "east", "south", "west" };
	private static final Map<String, Integer> EXPECTED_X_ROTATIONS = Map.of(
			"north", 90,
			"east", 90,
			"south", 270,
			"west", 90);

	private EngramBlockResourceTest() {
	}

	public static void main(String[] args) throws Exception {
		Path blockstate = Path.of("src/main/resources/assets/hemomancy/blockstates/engram_block.json");
		String json = Files.readString(blockstate);
		for (int character = 0; character < 26; character++) {
			for (String facing : FACINGS) {
				String variant = "\"character=" + character + ",facing=" + facing + "\"";
				if (!json.contains(variant)) {
					throw new AssertionError("Missing engram variant " + variant);
				}
				Integer expectedX = EXPECTED_X_ROTATIONS.get(facing);
				if (expectedX != null) {
					assertVariantContains(json, character, facing, "\"x\": " + expectedX);
				}
			}
		}
	}

	private static void assertVariantContains(String json, int character, String facing, String expected) {
		String key = Pattern.quote("\"character=" + character + ",facing=" + facing + "\"");
		Matcher matcher = Pattern.compile(key + "\\s*:\\s*\\{([^}]*)}", Pattern.DOTALL).matcher(json);
		if (!matcher.find()) {
			throw new AssertionError("Missing engram variant body for character=" + character + ",facing=" + facing);
		}
		if (!matcher.group(1).contains(expected)) {
			throw new AssertionError("Expected character=" + character + ",facing=" + facing + " to contain " + expected);
		}
	}
}
