package com.vincenthuto.hemomancy.common.rite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArchonPathRiteSourceTest {
	private ArchonPathRiteSourceTest() {}

	public static void main(String[] args) throws IOException {
		String dialogue = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");
		String rites = read("src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");
		assertContains(dialogue, "EnumArchonPath.SILENT_PENDING");
		assertContains(dialogue, "EnumArchonPath.APOTHEOS_PENDING");
		assertContains(rites, "EnumArchonPath.SILENT_ARCHON");
		assertContains(rites, "boolean pruned = completePruningOfQliphoth");
		assertContains(rites, "if (pruned)");
		assertContains(rites, "EnumArchonPath.APOTHEOS");
		assertContains(rites, "degree.getArchonPath() != EnumArchonPath.APOTHEOS_PENDING");
		assertNotContains(rites, "popFungalSpineFromBack(level, player)");
	}

	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
	private static void assertNotContains(String source, String forbidden) {
		if (source.contains(forbidden)) throw new AssertionError("still contains " + forbidden);
	}
}
