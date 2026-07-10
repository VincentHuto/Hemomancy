package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerRecruitmentDialogueSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String PLEDGE_OPTION = "hemomancy.dialogue.recruit.option.pledge_blood";
	private static final String RELEASE_OPTION = "hemomancy.dialogue.recruit.option.release_blood";

	private HarbingerRecruitmentDialogueSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertMutuallyExclusiveRecruitmentOptions("Vicar",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java"));
		assertMutuallyExclusiveRecruitmentOptions("Alchemist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java"));
		assertMutuallyExclusiveRecruitmentOptions("Mnemonist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java"));

		assertEntityPassesNpcMembership("Vicar",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java"));
		assertEntityPassesNpcMembership("Alchemist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		assertEntityPassesNpcMembership("Mnemonist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java"));
		assertEntityHidesDuplicateTypeRecruitment("Vicar",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java"));
		assertEntityHidesDuplicateTypeRecruitment("Alchemist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		assertEntityHidesDuplicateTypeRecruitment("Mnemonist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java"));
		assertBloodlineStoresNpcTypes(read(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/Bloodline.java"));
		assertBloodlineStoresNpcOutposts(read(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/Bloodline.java"));
		assertBloodlineSavedDataPropagatesNpcTypes(read(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineSavedData.java"));
		assertBloodlineSavedDataPropagatesNpcOutposts(read(
				"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodlineSavedData.java"));
		assertRecruitmentHandlerEnforcesNpcType(read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		assertRecruitmentHandlerEnforcesOutpost(read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));
		assertOutpostSpawnerMarksNpcOrigins(read(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/HarbingerOutpostStructure.java"));
		assertRecruitmentRulesLocateNpcOutposts(read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerRecruitmentRules.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Vicar",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Alchemist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerAlchemistEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Mnemonist",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerMnemonistEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Voyager",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVoyagerEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Votary Wayfarer",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVotaryWayfarerEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Zealot",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedZealotEntity.java"));
		assertEntityPreservesDialogueWhileHoldingItems("Guardian",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/unstained/UnstainedGuardianEntity.java"));
		assertAlchemistLateDegreesOfferItemInquiry(read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java"));

		assertNoRecruitmentOptions("Voyager",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVoyagerDialogueTrees.java"));
		assertNoRecruitmentOptions("Votary Wayfarer",
				read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVotaryWayfarerDialogueTrees.java"));
	}

	private static void assertMutuallyExclusiveRecruitmentOptions(String label, String source) {
		assertContains(label + " uses recruited-state helper", source, "isNpcRecruited");
		assertContains(label + " branches on recruited state", source, "? new DialogueOption(\"" + RELEASE_OPTION);
		assertCount(label + " has one pledge option construction", source, PLEDGE_OPTION, 1);
		assertCount(label + " has one release option construction", source, RELEASE_OPTION, 1);
	}

	private static void assertEntityPassesNpcMembership(String label, String source) {
		assertContains(label + " checks this NPC membership", source, "isNpcInPlayerBloodline(player, this)");
		assertContains(label + " checks NPC UUID membership", source, "hasNpcMember(npc.getUUID())");
	}

	private static void assertEntityHidesDuplicateTypeRecruitment(String label, String source) {
		assertContains(label + " computes recruitment visibility", source, "canShowRecruitment(player, this)");
		assertContains(label + " checks shared recruitment rules", source,
				"HarbingerRecruitmentRules.canRecruitNpc(vol.getBloodLine(), npc)");
	}

	private static void assertBloodlineStoresNpcTypes(String source) {
		assertContains("Bloodline stores recruited NPC type IDs", source, "List<ResourceLocation> npcMemberTypes");
		assertContains("Bloodline deserializes recruited NPC type IDs", source, "ResourceLocation.tryParse(comp.getString(\"type\" + i))");
		assertContains("Bloodline rejects duplicate recruited NPC types", source, "npcMemberTypes.contains(npcType)");
		assertContains("Bloodline serializes recruited NPC type IDs", source, "npc.putString(\"type\" + i");
	}

	private static void assertBloodlineStoresNpcOutposts(String source) {
		assertContains("Bloodline stores recruited NPC outpost keys", source, "List<String> npcMemberOutposts");
		assertContains("Bloodline deserializes recruited NPC outpost keys", source, "comp.getString(\"outpost\" + i)");
		assertContains("Bloodline rejects duplicate recruited NPC outposts", source, "npcMemberOutposts.contains(npcOutpost)");
		assertContains("Bloodline serializes recruited NPC outpost keys", source, "npc.putString(\"outpost\" + i");
	}

	private static void assertBloodlineSavedDataPropagatesNpcTypes(String source) {
		assertContains("saved data accepts recruited NPC type", source,
				"addNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType)");
		assertContains("saved data removes recruited NPC type", source,
				"removeNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType)");
	}

	private static void assertBloodlineSavedDataPropagatesNpcOutposts(String source) {
		assertContains("saved data accepts recruited NPC outpost", source,
				"addNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType, String npcOutpost)");
		assertContains("saved data removes recruited NPC outpost", source,
				"removeNpcMember(UUID bloodlineUUID, UUID npcUUID, ResourceLocation npcType, String npcOutpost)");
	}

	private static void assertRecruitmentHandlerEnforcesNpcType(String source) {
		assertContains("handler reads NPC entity type", source, "BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType())");
		assertContains("handler rejects duplicate NPC type", source, "bloodline.hasNpcMemberType(npcType)");
		assertContains("handler reports duplicate NPC type", source, "hemomancy.dialogue.recruit.type_already_member");
		assertContains("handler persists recruited NPC type", source,
				"bloodline.getBloodlineUUID(), entity.getUUID(), npcType");
	}

	private static void assertRecruitmentHandlerEnforcesOutpost(String source) {
		assertContains("handler reads NPC outpost", source, "HarbingerRecruitmentRules.findOutpostKey(entity)");
		assertContains("handler rejects duplicate NPC outpost", source, "bloodline.hasNpcMemberOutpost(npcOutpost)");
		assertContains("handler reports duplicate NPC outpost", source, "hemomancy.dialogue.recruit.outpost_already_member");
		assertContains("handler persists recruited NPC outpost", source,
				"bloodline.getBloodlineUUID(), entity.getUUID(), npcType, npcOutpost");
	}

	private static void assertOutpostSpawnerMarksNpcOrigins(String source) {
		assertContains("outpost spawner computes recruitment key", source, "HarbingerRecruitmentRules.createOutpostKey");
		assertContains("outpost spawner marks recruited NPC origins", source, "HarbingerRecruitmentRules.markOutpostOrigin");
	}

	private static void assertRecruitmentRulesLocateNpcOutposts(String source) {
		assertContains("recruitment rules define outpost origin tag", source, "NPC_OUTPOST_KEY");
		assertContains("recruitment rules locate structure fallback", source, "getStructureWithPieceAt");
		assertContains("recruitment rules check outpost duplicates", source, "canRecruitNpc");
	}

	private static void assertNoRecruitmentOptions(String label, String source) {
		assertNotContains(label + " does not offer pledge", source, PLEDGE_OPTION);
		assertNotContains(label + " does not offer release", source, RELEASE_OPTION);
	}

	private static void assertEntityPreservesDialogueWhileHoldingItems(String label, String source) {
		assertContains(label + " builds inventory inquiry nodes", source, "DialogueItemInquiryNodes.withInventoryItemInquiries");
		assertNotContains(label + " does not replace the whole tree for held items", source, "if (!held.isEmpty())");
		assertNotContains(label + " does not use held-item ternary tree selection", source, "held.isEmpty()");
		assertNotContains(label + " does not open item inquiry as root tree", source, "itemInquiry(held");
		assertNotContains(label + " does not open item-only guardian tree", source, "forItem(held");
	}

	private static void assertAlchemistLateDegreesOfferItemInquiry(String source) {
		assertMethodGreetingOption("Alchemist illuminatus", source, "illuminatus",
				"hemomancy.dialogue.alchemist.option.ask_about_item");
		assertMethodGreetingOption("Alchemist sanctified", source, "sanctified",
				"hemomancy.dialogue.alchemist.option.ask_about_item");
		assertMethodGreetingOption("Alchemist archon", source, "archon",
				"hemomancy.dialogue.alchemist.option.ask_about_item");
		assertMethodGreetingOption("Alchemist apotheos", source, "apotheos",
				"hemomancy.dialogue.alchemist.option.ask_about_item");
	}

	private static void assertMethodGreetingOption(String label, String source, String methodName, String optionKey) {
		int start = source.indexOf("public static DialogueTree " + methodName);
		if (start < 0) {
			throw new AssertionError(label + ": missing dialogue method");
		}
		int greetingEnd = source.indexOf(".addNode(new DialogueNode(\"greeting\"", start);
		if (greetingEnd < 0) {
			throw new AssertionError(label + ": missing greeting node");
		}
		String greetingOptions = source.substring(start, greetingEnd);
		assertContains(label + " offers item inquiry from greeting", greetingOptions, optionKey);
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": unexpected " + unexpected);
		}
	}

	private static void assertCount(String label, String text, String needle, int expectedCount) {
		int count = 0;
		int index = text.indexOf(needle);
		while (index >= 0) {
			count++;
			index = text.indexOf(needle, index + needle.length());
		}
		if (count != expectedCount) {
			throw new AssertionError(label + ": expected " + expectedCount + " but found " + count);
		}
	}
}
