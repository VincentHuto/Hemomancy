package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CircusDiscoveryIntegrationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private CircusDiscoveryIntegrationSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String dialogue = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java");
		String handler = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");
		String discovery = read("src/main/java/com/vincenthuto/hemomancy/common/worldgen/CircusDiscoveryProgress.java");
		String waybill = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/CircusWaybillItem.java");

		assertContains(dialogue, "EVENT_CIRCUS_WAYBILL");
		assertContains(dialogue, "hemomancy.mnemonist.circus.undiscovered.line1");
		assertContains(dialogue, "hemomancy.mnemonist.circus.discovered.line1");
		assertContains(handler, "ItemInit.circus_waybill");
		assertContains(discovery, "getStructureWithPieceAt");
		assertContains(discovery, "circus_pavilion");
		assertContains(waybill, "circus_waybill_targets");
		assertContains(read("src/main/resources/data/hemomancy/tags/worldgen/structure/circus_waybill_targets.json"),
				"hemomancy:circus_pavilion");
		assertContains(read("src/main/resources/assets/hemomancy/models/item/circus_waybill.json"),
				"hemomancy:item/blood_structure_hint");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) throw new AssertionError("Missing " + expected);
	}
}
