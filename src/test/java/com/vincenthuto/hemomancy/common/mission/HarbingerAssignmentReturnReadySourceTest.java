package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HarbingerAssignmentReturnReadySourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerAssignmentReturnReadySourceTest() {
	}

	public static void main(String[] args) throws IOException {
		returnReadyAdvancementConstantsExist();
		returnReadyHooksAreWired();
		returnReadyAdvancementResourcesExist();
		returnReadyDocsExplainPlayerPrompts();
	}

	private static void returnReadyAdvancementConstantsExist() throws IOException {
		String advancementGranter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/HarbingerAdvancementGranter.java"));

		for (String constant : new String[] {
				"ADV_FIRST_SEPARATION_COMPLETE",
				"ADV_MNEMONIST_WOVEN_VESSEL_FINISHED",
				"ADV_VEIN_MASON_CONTINUATION_READY",
				"ADV_ARTIFICER_WORN_VOW_LESSON_READY",
				"ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY",
				"ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY",
				"ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY",
				"ADV_ARTIFICER_WORN_VOW_FITTING_READY",
				"ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY",
				"ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY",
				"ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY",
				"ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY"
		}) {
			assertContains("return-ready advancement constant exists: " + constant, advancementGranter, constant);
		}

		for (String helper : new String[] {
				"isFirstSeparationComplete",
				"isMnemonistWovenVesselFinished",
				"isVeinMasonContinuationReady",
				"isArtificerWornVowLessonReady",
				"isArtificerThreeAnswersLessonReady",
				"isArtificerCrimsonVestmentLessonReady",
				"isArtificerAssumedLimbLessonReady",
				"isArtificerWornVowFittingReady",
				"isArtificerThreeAnswersFittingReady",
				"isArtificerCrimsonVestmentFittingReady",
				"isArtificerWeightOfTheFrameFittingReady",
				"isArtificerAssumedLimbFittingReady"
		}) {
			assertContains("return-ready helper exists: " + helper, advancementGranter, helper);
		}
	}

	private static void returnReadyHooksAreWired() throws IOException {
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/mission/HarbingerArtificerAssignmentHelper.java"));
		String bloodVolumeEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/capability/player/harbinger/bloodvolume/BloodVolumeEvents.java"));
		String somaticLoom = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/tile/crafting/SomaticLoomBlockEntity.java"));
		String brazier = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/rite/ScarBrazierRite.java"));
		String dialogueHandler = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java"));

		assertContains("Artificer helper syncs ready-to-claim advancements", helper,
				"syncReadyToClaimAdvancements");
		assertContains("Hematic Iron upgrade grants lesson-ready prompt", helper,
				"ADV_ARTIFICER_WORN_VOW_LESSON_READY");
		assertContains("Fork upgrade grants lesson-ready prompt", helper,
				"ADV_ARTIFICER_THREE_ANSWERS_LESSON_READY");
		assertContains("Blood Lust upgrade grants lesson-ready prompt", helper,
				"ADV_ARTIFICER_CRIMSON_VESTMENT_LESSON_READY");
		assertContains("Living graft grants lesson-ready prompt", helper,
				"ADV_ARTIFICER_ASSUMED_LIMB_LESSON_READY");
		assertContains("Full Hematic Iron set grants fitting-ready prompt", helper,
				"ADV_ARTIFICER_WORN_VOW_FITTING_READY");
		assertContains("Full fork set grants fitting-ready prompt", helper,
				"ADV_ARTIFICER_THREE_ANSWERS_FITTING_READY");
		assertContains("Full Blood Lust set grants fitting-ready prompt", helper,
				"ADV_ARTIFICER_CRIMSON_VESTMENT_FITTING_READY");
		assertContains("Full D7 set grants fitting-ready prompt", helper,
				"ADV_ARTIFICER_WEIGHT_OF_THE_FRAME_FITTING_READY");
		assertContains("Full living arsenal grants fitting-ready prompt", helper,
				"ADV_ARTIFICER_ASSUMED_LIMB_FITTING_READY");
		assertContains("Blood volume tick invokes Artificer ready sync", bloodVolumeEvents,
				"HarbingerArtificerAssignmentHelper.syncReadyToClaimAdvancements(serverPlayer)");
		assertContains("Blood volume tick detects First Separation completion", bloodVolumeEvents,
				"ADV_FIRST_SEPARATION_COMPLETE");
		assertContains("Somatic Loom grants Woven Vessel finished prompt", somaticLoom,
				"ADV_MNEMONIST_WOVEN_VESSEL_FINISHED");
		assertContains("Somatic Loom requires Mnemonist indexing before final prompt", somaticLoom,
				"isMnemonistWovenVesselComplete(serverPlayer)");
		assertContains("Brazier grants Vein-Mason continuation prompt", brazier,
				"ADV_VEIN_MASON_CONTINUATION_READY");
		assertContains("Artificer material rewards check shared claim helper", dialogueHandler,
				"isArtificerLessonRewardClaimed");
		assertContains("Artificer material rewards record shared claim helper", dialogueHandler,
				"markArtificerLessonRewardClaimed");
	}

	private static void returnReadyAdvancementResourcesExist() throws IOException {
		String language = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		for (String id : new String[] {
				"first_separation_complete",
				"mnemonist_woven_vessel_finished",
				"vein_mason_continuation_ready",
				"artificer_worn_vow_lesson_ready",
				"artificer_three_answers_lesson_ready",
				"artificer_crimson_vestment_lesson_ready",
				"artificer_assumed_limb_lesson_ready",
				"artificer_worn_vow_fitting_ready",
				"artificer_three_answers_fitting_ready",
				"artificer_crimson_vestment_fitting_ready",
				"artificer_weight_of_the_frame_fitting_ready",
				"artificer_assumed_limb_fitting_ready"
		}) {
			String json = read(RESOURCE_ROOT.resolve(
					"data/hemomancy/advancement/hemomancy/" + id + ".json"));
			assertContains("return-ready advancement " + id + " uses impossible trigger", json,
					"\"trigger\": \"minecraft:impossible\"");
			assertContains("return-ready advancement " + id + " shows toast", json,
					"\"show_toast\": true");
			assertContains("return-ready advancement " + id + " is not chat announced", json,
					"\"announce_to_chat\": false");
			assertContains("return-ready advancement " + id + " has hidden display", json,
					"\"hidden\": true");
			assertContains("return-ready title lang exists for " + id, language,
					"advancements.hemomancy." + id + ".title");
			assertContains("return-ready description lang exists for " + id, language,
					"advancements.hemomancy." + id + ".description");
		}
	}

	private static void returnReadyDocsExplainPlayerPrompts() throws IOException {
		String docs = read(Path.of("docs/HEMOMANCY_REFERENCE.md"));
		assertContains("docs describe visible return-ready prompts", docs,
				"visible return-ready advancement prompts");
		assertContains("docs describe Artificer fitting prompts", docs,
				"full-set Artificer fitting prompts");
		assertContains("docs describe First Separation completion prompt", docs,
				"first_separation_complete");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String needle) {
		if (!text.contains(needle)) {
			throw new AssertionError(label + " missing: " + needle);
		}
	}
}
