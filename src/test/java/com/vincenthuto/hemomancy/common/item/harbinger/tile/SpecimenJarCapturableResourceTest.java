package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class SpecimenJarCapturableResourceTest {
	private static final Path TAG_PATH = Path.of(
			"src/main/resources/data/hemomancy/tags/entity_type/specimen_jar_capturable.json");

	@Test
	void capturableTagListsTheSpecimenMobs() throws IOException {
		String tag = Files.readString(TAG_PATH).replace("\r\n", "\n");
		assertContains("wild morphling polyps are capturable", tag, "\"hemomancy:morphling_polyp\"");
		assertContains("barbed urchins are capturable", tag, "\"hemomancy:barbed_urchin\"");
		assertContains("hemojellies are capturable", tag, "\"hemomancy:hemojelly\"");
		assertContains("crimson does are capturable", tag, "\"hemomancy:crimson_doe\"");
		assertContains("venous striders are capturable", tag, "\"hemomancy:venous_strider\"");
		assertContains("scarlet serpents are capturable", tag, "\"hemomancy:scarlet_serpent\"");
		assertContains("venom rib centipedes are capturable", tag, "\"hemomancy:venom_rib_centipede\"");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
