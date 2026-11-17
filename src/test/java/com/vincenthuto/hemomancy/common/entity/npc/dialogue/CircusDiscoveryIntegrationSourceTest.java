package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class CircusDiscoveryIntegrationSourceTest {

	private CircusDiscoveryIntegrationSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String dialogue = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java");
		String handler = com.vincenthuto.hemomancy.support.SourceAssertions.readDialogueRoutes();
		String discovery = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/circus/CircusDiscoveryProgress.java");
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

	private static void assertContains(String text, String expected) {
		if (!text.contains(expected)) throw new AssertionError("Missing " + expected);
	}
}
