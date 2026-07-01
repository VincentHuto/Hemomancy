package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SpecimenJarCapturableResourceTest {
	private static final Path TAG_PATH = Path.of(
			"src/main/resources/data/hemomancy/tags/entity_type/specimen_jar_capturable.json");
	private static final Path TAG_PROVIDER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/data/gen/HemoEntityTagProvider.java");

	private SpecimenJarCapturableResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String tag = Files.readString(TAG_PATH).replace("\r\n", "\n");
		assertContains("wild morphling polyps are capturable", tag, "\"hemomancy:morphling_polyp\"");
		assertContains("barbed urchins are capturable", tag, "\"hemomancy:barbed_urchin\"");
		assertContains("hemojellies are capturable", tag, "\"hemomancy:hemojelly\"");
		assertContains("crimson does are capturable", tag, "\"hemomancy:crimson_doe\"");
		assertContains("venous striders are capturable", tag, "\"hemomancy:venous_strider\"");
		assertContains("scarlet serpents are capturable", tag, "\"hemomancy:scarlet_serpent\"");
		assertContains("venom rib centipedes are capturable", tag, "\"hemomancy:venom_rib_centipede\"");

		String provider = Files.readString(TAG_PROVIDER).replace("\r\n", "\n");
		assertContains("datagen keeps venom rib centipedes capturable", provider,
				"EntityInit.venom_rib_centipede.getId()");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
