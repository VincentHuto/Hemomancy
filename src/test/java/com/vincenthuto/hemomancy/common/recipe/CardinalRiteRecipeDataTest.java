package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;

public final class CardinalRiteRecipeDataTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources/data");

	private CardinalRiteRecipeDataTest() {
	}

    @Test
    void allChecksPass() throws IOException {
        for (String retired : new String[]{"sanguine_initiation", "votary_rite"}) {
            assertFalse(Files.exists(RESOURCE_ROOT.resolve("hemomancy/recipe/cardinal_rite/" + retired + ".json")), "retired rank recipe still shipped");
        }
        assertContains("first staff rite", read("hemomancy/recipe/cardinal_rite/initiate_rite.json"), "\"required_degree\": 2");
        assertContains("bloom medium", read("hemomancy/recipe/cardinal_rite/bloom_of_qliphoth.json"), "\"item\": \"hemomancy:qliphoth_seed\"");
        assertContains("founding fane medium", read("hemomancy/recipe/cardinal_rite/founding_fane.json"), "\"item\": \"hemomancy:sanguine_quintessence\"");
    }

	private static String read(String path) throws IOException {
		return Files.readString(RESOURCE_ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertMatches(String label, String text, String regex) {
		if (!Pattern.compile(regex).matcher(text).find()) {
			throw new AssertionError(label + ": missing pattern " + regex);
		}
	}

	private static void assertFalse(boolean value, String message) {
		if (value) {
			throw new AssertionError(message);
		}
	}
}
