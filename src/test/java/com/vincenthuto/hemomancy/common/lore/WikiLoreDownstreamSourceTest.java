package com.vincenthuto.hemomancy.common.lore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WikiLoreDownstreamSourceTest {
	private WikiLoreDownstreamSourceTest() {}
	public static void main(String[] args) throws IOException {
		String home = read("wiki/Home.md");
		String qliphoth = read("wiki/The-Qliphoth.md");
		String harbinger = read("wiki/Harbinger-Path.md");
		String unstained = read("wiki/Unstained-Path.md");
		String lore = read("wiki/Lore-and-Story.md");
		assertContains(home, "wiki is a downstream player-facing presentation");
		assertContains(qliphoth, "two-minute consciousness projection");
		assertContains(harbinger, "### Degree 8: Apotheos");
		assertNotContains(harbinger, "Automatically created when you reach Apostle");
		assertContains(unstained, "Lethean Baptism");
		assertNotContains(unstained, "blunt weapons only");
		assertNotContains(lore, "accessed at Apotheos via **Fungal Spine**");
	}
	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) { if (!source.contains(expected)) throw new AssertionError("missing " + expected); }
	private static void assertNotContains(String source, String forbidden) { if (source.contains(forbidden)) throw new AssertionError("still contains " + forbidden); }
}
