package com.vincenthuto.hemomancy.common.lore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LoreCanonAuthoritySourceTest {
	private static final Path LORE = Path.of("docs/LORE_REFERENCE.md");
	private static final Path DEVELOPER = Path.of("docs/HEMOMANCY_REFERENCE.md");

	private LoreCanonAuthoritySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String lore = read(LORE);
		String developer = read(DEVELOPER);

		assertContains("lore reference declares canon precedence", lore,
				"LORE_REFERENCE.md is the authority for canon");
		assertContains("wiki is explicitly downstream", lore,
				"The wiki is a downstream presentation of these docs");
		assertContains("Annetta's canonical creature is the Tooth Peck", lore,
				"Annetta's canonical creature is the Tooth Peck");
		assertContains("Verdigris precedes Silver Ward", lore,
				"Verdigris Aura is the early Purity defense; Silver Ward is its advanced Clarity form");
		assertContains("first spine visit is temporary", lore,
				"first Fungal Spine visit is a two-minute consciousness projection");
		assertContains("ordinary cure closes at founding", lore,
				"ordinary Unstained cure closes when a Harbinger founds a bloodline");
		assertContains("developer reference follows lore docs", developer,
				"gameplay, dialogue, inquiries, item text, and wiki pages must be corrected to match the docs");
		assertNotContains("code must not outrank settled lore", developer,
				"Current code and data are authoritative when older prose");
	}

	private static String read(Path path) throws IOException {
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": still contains " + forbidden);
		}
	}
}
