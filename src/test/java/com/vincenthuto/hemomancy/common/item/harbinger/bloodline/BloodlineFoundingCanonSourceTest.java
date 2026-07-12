package com.vincenthuto.hemomancy.common.item.harbinger.bloodline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BloodlineFoundingCanonSourceTest {
	private BloodlineFoundingCanonSourceTest() {}
	public static void main(String[] args) throws IOException {
		String ledger = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/bloodline/UnsignedLedgerItem.java");
		String rites = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		assertNotContains(ledger, "new Bloodline(bloodLineName");
		assertContains(ledger, "Only the Bloodline Founding rite can write the first name");
		assertContains(rites, "degree.setHasFoundedBloodline(true)");
	}
	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) { if (!source.contains(expected)) throw new AssertionError("missing " + expected); }
	private static void assertNotContains(String source, String forbidden) { if (source.contains(forbidden)) throw new AssertionError("still contains " + forbidden); }
}
