package com.vincenthuto.hemomancy.common.manipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ManipulationRetirementSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String[] RETIRED_MANIPULATIONS = {
			"blood_lamp",
			"crimson_harvest",
			"hemosynthesis",
			"vital_reservoir",
			"sanguine_excavation"
	};
	private static final String[] RETIRED_MEMORY_ITEMS = {
			"memory_conjure_living_staff",
			"memory_blood_absorption",
			"memory_blood_projection",
			"memory_blood_lamp",
			"crude_memory_blood_lamp",
			"memory_crimson_harvest",
			"crude_memory_crimson_harvest",
			"memory_hemosynthesis",
			"memory_vital_reservoir",
			"memory_sanguine_excavation"
	};

	private ManipulationRetirementSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		retirementHelperIsUsedAtGameplayGates();
		retiredManipulationsAreAbsentFromTree();
		retiredAcquisitionResourcesAreRemoved();
		documentationMarksRetiredContentInactive();
	}

	private static void retirementHelperIsUsedAtGameplayGates() throws IOException {
		assertContains("equip helper blocks retired names",
				read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java"),
				"ManipulationRetirementRules.isRetiredManipulation");
		assertContains("memory item blocks retired memory items",
				read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/BloodMemoryItem.java"),
				"ManipulationRetirementRules.isRetiredMemoryItem");
		assertContains("crude memory item blocks retired memories",
				read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/CrudeMemoryShardItem.java"),
				"ManipulationRetirementRules.isRetiredMemoryItem");
		assertContains("blood manipulation blocks retired casts",
				read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/BloodManipulation.java"),
				"ManipulationRetirementRules.isRetiredManipulation(this)");
		assertContains("use packet blocks retired selected manipulation",
				read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UseManipKeyPacket.java"),
				"ManipulationRetirementRules.isRetiredManipulation(selectedManip)");
		assertContains("selection packet blocks retired manipulation",
				read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/UpdateCurrentManipPacket.java"),
				"ManipulationRetirementRules.isRetiredManipulation(target)");
		assertContains("known manipulation sync sanitizes old saves",
				read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/manips/KnownManipulationServerPacket.java"),
				"ManipulationRetirementRules.sanitizeKnownManipulations");
		assertContains("drudges reject retired memories",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/DrudgeEntity.java"),
				"ManipulationRetirementRules.isRetiredManipulation");
		assertContains("creative tab hides retired memories",
				read("src/main/java/com/vincenthuto/hemomancy/Hemomancy.java"),
				"ManipulationRetirementRules.isRetiredMemoryItem(item)");
	}

	private static void retiredManipulationsAreAbsentFromTree() throws IOException {
		String tree = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");
		for (String id : RETIRED_MANIPULATIONS) {
			assertNotContains(id + " absent from manipulation tree", tree, "register(\"" + id + "\"");
			assertNotContains(id + " absent as tree parent", tree, "\"" + id + "\"");
		}
		assertContains("ductilis reroutes activation potential through synaptic jolt", tree,
				"register(\"activation_potential\",1060,160, \"synaptic_jolt\")");
		assertContains("lux reroutes crimson sight through hematic flare", tree,
				"register(\"crimson_sight\",1270,160, \"hematic_flare\")");
		assertContains("mortem reroutes grave debt away from vital reservoir", tree,
				"register(\"grave_debt\",2180,70, \"hemorrhage\", \"exsanguinate\")");
		assertContains("ferric magnetism no longer depends on excavation", tree,
				"register(\"sanguine_magnetism\",790,60, \"iron_retort\")");
	}

	private static void retiredAcquisitionResourcesAreRemoved() throws IOException {
		String inheritedMemory = read("src/main/resources/data/hemomancy/advancement/hemomancy/inherited_memory.json");
		String loot = read("src/main/resources/data/hemomancy/loot_table/chests/harbinger_outpost.json")
				+ read("src/main/resources/data/hemomancy/loot_table/chests/blood_temple.json")
				+ read("src/main/resources/data/hemomancy/loot_table/chests/mausoleum.json");
		for (String itemId : RETIRED_MEMORY_ITEMS) {
			assertNotContains(itemId + " absent from inherited memory advancement", inheritedMemory, itemId);
			assertNotContains(itemId + " absent from loot tables", loot, itemId);
			assertMissing(itemId + " memory weaving recipe",
					"src/main/resources/data/hemomancy/recipe/memory_weaving/" + itemId.replace("crude_", "") + ".json");
			assertMissing(itemId + " Mnemonist inquiry",
					"src/main/resources/data/hemomancy/dialogue_inquiry/mnemonist/hemomancy/" + itemId + ".json");
		}
	}

	private static void documentationMarksRetiredContentInactive() throws IOException {
		String reference = read("docs/HEMOMANCY_REFERENCE.md");
		assertContains("reference has retired manipulation section", reference, "Retired Manipulations");
		for (String id : RETIRED_MANIPULATIONS) {
			assertContains(id + " documented as retired", reference, "`" + id + "`");
		}
		assertContains("reference mentions vital reservoir future rite", reference, "future rite candidate");
		assertContains("reference mentions morphling migration", reference, "future morphling ability");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertMissing(String label, String path) {
		if (Files.exists(ROOT.resolve(path))) {
			throw new AssertionError(label + " still exists at " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains `" + unexpected + "`");
		}
	}
}
