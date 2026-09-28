package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VicarBloodCraftingAssignmentSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private VicarBloodCraftingAssignmentSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String vicar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
		String vicarEntity = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java");
		String ledger = read("src/main/java/com/vincenthuto/hemomancy/client/screen/item/HarbingerAssignmentLedgerScreen.java");
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/mission/OpenHarbingerAssignmentLedgerPacket.java");
		String item = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/HarbingerAssignmentLedgerItem.java");
		String blood = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java");
		String advancements = read("src/main/java/com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("Vicar offers blood crafting lesson", vicar, "option.ask_about_blood_crafting");
		assertContains("Vicar explains absorption", vicar, "hemomancy.vicar.blood_crafting.absorption");
		assertContains("Vicar explains projection", vicar, "hemomancy.vicar.blood_crafting.projection");
		assertContains("Vicar explains structure requirements", vicar, "hemomancy.vicar.blood_crafting.structure");
		assertContains("Vicar dialogue is selected by player degree", vicarEntity, "forDegree(degree");

		assertContains("5000 ml milestone exists", advancements, "ADV_VESSEL_FILLED");
		assertContains("Blood volume awards 5000 ml milestone", blood, "ADV_VESSEL_FILLED");
		assertContains("Ledger receives Vicar proof state", packet, "FirstBloodcraftLedgerProgress firstBloodcraft");
		assertContains("Ledger item sends Vicar proof state", item, "FirstBloodcraftAssignment.progress(serverPlayer)");

		assertContains("Ledger renders Fill the Vessel", ledger, "step.fill_vessel");
		assertContains("Ledger renders formation projection", ledger, "step.project_formation");
		assertContains("Ledger renders Venous Stone projection", ledger, "step.project_venous_stone");
		assertContains("Ledger renders either blood formation craft", ledger, "step.first_structure_craft");
		assertContains("Ledger renders First Bloodcraft as D1 main assignment", ledger, "renderFirstBloodcraft");
		assertContains("Ledger renders Hermit Road as D1 side assignment", ledger, "renderHermitRoad");
		assertContains("Ledger renders separate projection work", ledger, "renderFirstProjection");
		assertContains("Ledger renders separate formation craft", ledger, "renderFirstFormationCraft");
		assertContains("Hermit Road computes side progress", ledger, "hermitRoadProgress()");
		assertContains("Hermit Road counts two side steps", ledger, "progress, 2, progress >= 2");

		assertContains("Blood crafting option has lang", lang, "hemomancy.dialogue.vicar.option.ask_about_blood_crafting");
		assertContains("First Bloodcraft main title has lang", lang,
				"screen.hemomancy.harbinger_assignment_ledger.first_bloodcraft.title");
		assertContains("Hermit Road side title has lang", lang,
				"screen.hemomancy.harbinger_assignment_ledger.hermit_road.side_title");
		assertContains("Fill vessel step has lang", lang, "screen.hemomancy.harbinger_assignment_ledger.step.fill_vessel");
		assertContains("Formation step has lang", lang, "screen.hemomancy.harbinger_assignment_ledger.step.project_formation");
		assertContains("Venous Stone step has lang", lang, "screen.hemomancy.harbinger_assignment_ledger.step.project_venous_stone");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
