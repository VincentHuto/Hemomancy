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
			"sanguine_excavation",
			"ferric_resonance",
			"glacial_bastion",
			"blood_eclipse_mantle",
			"crimson_sight",
			"glacial_circulation",
			"ferric_transmutation",
			"vigil_of_glass",
			"hematic_ballast",
			"summon_thrall",
			"venous_travel"
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
			"memory_sanguine_excavation",
			"memory_ferric_resonance",
			"memory_glacial_bastion",
			"memory_blood_eclipse_mantle",
			"memory_crimson_sight",
			"memory_glacial_circulation",
			"memory_ferric_transmutation",
			"memory_vigil_of_glass",
			"memory_hematic_ballast",
			"memory_summon_thrall",
			"memory_venous_travel"
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
		assertNotContains("activation potential is represented by the synaptic family", tree,
				"register(\"activation_potential\"");
		assertContains("lux reroutes unclosing eye through hematic flare", tree,
				"register(\"unclosing_eye\",1340,180, \"hematic_flare\")");
		assertContains("lux capstone bypasses vigil of glass", tree,
				"register(\"white_verdict\",1300,300, \"unclosing_eye\", \"prismatic_reproof\")");
		assertNotContains("cryogenic pulse is represented by the cryogenic family", tree,
				"register(\"cryogenic_pulse\"");
		assertContains("ferric capstone bypasses hematic ballast", tree,
				"register(\"iron_choir\",820,20, \"iron_retort\", \"ironhearted\")");
		assertContains("mortem reroutes grave debt away from vital reservoir", tree,
				"register(\"grave_debt\",2170,81, \"hemorrhage\", \"exsanguinate\")");
		assertContains("ferric magnetism no longer depends on excavation", tree,
				"register(\"sanguine_magnetism\",790,200, \"iron_retort\")");
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
		String effigyRecipe = read(
				"src/main/resources/data/hemomancy/recipe/memory_weaving/blood_thrall_effigy.json");
		assertContains("blood thrall effigy inherits the retired manipulation route", effigyRecipe,
				"hemomancy:blood_thrall_effigy");
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
