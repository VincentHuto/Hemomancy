package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PuppeteeringThreadInquiryOwnershipTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private PuppeteeringThreadInquiryOwnershipTest() {
	}

	public static void main(String[] args) throws IOException {
		Path alchemistInquiry = RESOURCE_ROOT.resolve(
				"data/hemomancy/dialogue_inquiry/alchemist/hemomancy/enthralling_filament.json");
		Path mnemonistInquiry = RESOURCE_ROOT.resolve(
				"data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/enthralling_filament.json");
		String language = Files.readString(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertFalse("Alchemist should not own puppeteering thread inquiry", Files.exists(alchemistInquiry));
		assertTrue("Mnemonist should own puppeteering thread inquiry", Files.exists(mnemonistInquiry));

		String inquiry = Files.readString(mnemonistInquiry);
		assertContains("Mnemonist inquiry uses Mnemonist line 1", inquiry,
				"hemomancy.mnemonist.item_inquiry.enthralling_filament.line1");
		assertContains("Mnemonist inquiry uses Mnemonist line 2", inquiry,
				"hemomancy.mnemonist.item_inquiry.enthralling_filament.line2");
		assertContains("Mnemonist line 1 exists", language,
				"hemomancy.mnemonist.item_inquiry.enthralling_filament.line1");
		assertContains("Mnemonist line 2 exists", language,
				"hemomancy.mnemonist.item_inquiry.enthralling_filament.line2");
		assertFalse("Alchemist puppeteering thread lines should be removed", language.contains(
				"hemomancy.alchemist.item_inquiry.enthralling_filament"));
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " missing: " + needle);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
