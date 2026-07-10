package com.vincenthuto.hemomancy.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class GameTestHarnessSourceContractTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void devOnlyHarnessProvidesCommandsAndEarlyHarbingerScenarios() throws IOException {
		String build = read("build.gradle");
		String catalog = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestScenarioCatalog.java");
		String commands = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestCommands.java");
		String gameTests = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HarbingerPilotGameTests.java");
		String guide = read("docs/TESTING.md");

		assertTrue(build.contains("gameTest"), "build must declare the isolated gameTest source set");
		assertTrue(build.contains("tasks.register('alphaCheck')"));
		assertTrue(commands.contains("literal(\"setup\")"));
		assertTrue(commands.contains("literal(\"verify\")"));
		assertTrue(commands.contains("literal(\"run\")"));
		assertTrue(commands.contains("literal(\"run_all\")"));
		assertTrue(commands.contains("runAll("));
		assertTrue(commands.contains("literal(\"status\")"));
		assertTrue(commands.contains("literal(\"clear\")"));
		assertTrue(gameTests.contains("@GameTestHolder(Hemomancy.MOD_ID)"));

		var ids = Pattern.compile("new HemoTestScenario\\(\\s*\"([^\"]+)\"")
				.matcher(catalog).results().map(match -> match.group(1)).toList();
		assertEquals(7, ids.size());
		assertTrue(ids.contains("blood_structure_locked"));
		assertTrue(ids.contains("blood_structure_unlocked"));
		assertTrue(ids.contains("artificer_assignment_ready"));
		assertTrue(ids.contains("artificer_reward_claimed"));
		assertTrue(ids.contains("uninitiated_cannot_pass_bloodcraft_degree_gate"));
		assertTrue(ids.contains("sanguine_initiation_recipe_loaded"));
		assertTrue(ids.contains("sanguine_initiation_degree_mapping"));
		assertTrue(guide.contains("./gradlew.bat alphaCheck"));
		assertTrue(guide.contains("/hemo test setup"));
	}

	@Test
	void journeyCommandsDelegateAcrossExactOrderedStages() throws IOException {
		Path stagePath = ROOT.resolve(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyStage.java");
		assertTrue(Files.exists(stagePath), "journey stage model must remain in the gameTest source set");

		String stage = Files.readString(stagePath).replace("\r\n", "\n");
		String commands = read("src/gameTest/java/com/vincenthuto/hemomancy/gametest/HemoTestCommands.java");
		String controller = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java");
		String result = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyResult.java");

		var stageIds = Pattern.compile("[A-Z_]+\\(\"([^\"]+)\"\\)")
				.matcher(stage).results().map(match -> match.group(1)).toList();
		assertEquals(java.util.List.of(
				"mortal_display",
				"sanguine_initiation",
				"vessel_filled",
				"formation_projected",
				"liber_crafted",
				"hematic_iron_crafted",
				"vicar_reward", "votary_rite", "degree_2_reached", "alchemist_briefing",
				"centrifuge_prepared", "separation_started", "enzyme_recovered", "alchemist_reward",
				"complete"), stageIds);

		int journeyStart = commands.indexOf("literal(\"journey\")");
		assertTrue(journeyStart >= 0);
		String journeyCommands = commands.substring(journeyStart, commands.indexOf(")));", journeyStart));
		assertTrue(journeyCommands.contains("literal(\"start\")"));
		assertTrue(journeyCommands.contains("literal(\"next\")"));
		assertTrue(journeyCommands.contains("literal(\"status\")"));
		assertTrue(journeyCommands.contains("literal(\"reset\")"));
		assertTrue(commands.contains("HemoJourneyController.start(player)"));
		assertTrue(commands.contains("HemoJourneyController.next(player)"));
		assertTrue(commands.contains("HemoJourneyController.status(player)"));
		assertTrue(commands.contains("HemoJourneyController.reset(player)"));
		assertTrue(commands.contains("HemoJourneyController.clear(player)"));
		assertTrue(commands.indexOf("HemoJourneyController.clear(player)")
				< commands.indexOf("HemoTestScenarioCatalog.clearActive(player)", commands.indexOf("private static int clear(")));
		assertTrue(controller.contains("HemoJourneyResult start(ServerPlayer player)"));
		assertTrue(controller.contains("HemoJourneyResult next(ServerPlayer player)"));
		assertTrue(controller.contains("HemoJourneyResult status(ServerPlayer player)"));
		assertTrue(controller.contains("HemoJourneyResult reset(ServerPlayer player)"));
		assertTrue(controller.contains("HemoJourneyResult clear(ServerPlayer player)"));
		assertTrue(commands.contains("if (!journeyClear.passed())"));
		assertTrue(commands.indexOf("return 0;", commands.indexOf("if (!journeyClear.passed())"))
				< commands.indexOf("HemoTestScenarioCatalog.clearActive(player)", commands.indexOf("private static int clear(")));
		assertTrue(result.contains("boolean passed"));
		assertTrue(result.contains("HemoJourneyStage stage"));
		assertTrue(result.contains("String message"));
	}

	@Test
	void journeySnapshotOwnsOnlyItsExactRestorablePlayerState() throws IOException {
		Path snapshotPath = ROOT.resolve(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneySnapshot.java");
		assertTrue(Files.exists(snapshotPath), "journey snapshot must remain development-only");

		String snapshot = Files.readString(snapshotPath).replace("\r\n", "\n");
		String controller = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java");
		String fixtures = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java");

		assertTrue(snapshot.contains("hemomancy.dev_test.journey.snapshot"));
		assertTrue(snapshot.contains("hemomancy.dev_test.journey.stage"));
		assertTrue(snapshot.contains("capture(ServerPlayer player)"));
		assertTrue(snapshot.contains("resetForJourney(ServerPlayer player)"));
		assertTrue(snapshot.contains("restore(ServerPlayer player)"));
		assertTrue(snapshot.contains("blood_active"));
		assertTrue(snapshot.contains("blood_current"));
		assertTrue(snapshot.contains("blood_max"));
		assertTrue(snapshot.contains("degree"));
		assertTrue(snapshot.contains("origin_dimension"));
		assertTrue(snapshot.contains("origin_position"));
		assertTrue(snapshot.contains("current_stage"));
		assertTrue(snapshot.contains("skill_progress"));
		assertTrue(snapshot.contains("liber_knowledge"));
		assertTrue(snapshot.contains("known_manipulations"));
		assertTrue(snapshot.contains("vasc_equipment"));
		assertTrue(snapshot.contains("getContainerSize()"));
		assertTrue(snapshot.contains("saveOptional(player.registryAccess())"));
		assertTrue(snapshot.contains("ItemStack.parse(player.registryAccess(), saved)"));
		assertTrue(snapshot.contains("requireSkillProgress(player).serializeNBT(player.registryAccess())"));
		assertTrue(snapshot.contains("requireEquipment(player).getStackInSlot(VASC_SLOT)"));
		assertTrue(snapshot.contains("requireInitiatoryDegree(player).getDegreeNumber()"));
		assertTrue(snapshot.contains("preflightSnapshot(player, snapshot)"));
		assertTrue(snapshot.contains("validateSkillProgressSchema"));
		assertTrue(snapshot.contains("validateLiberKnowledgeSchema"));
		assertTrue(snapshot.contains("validateKnownManipulationsSchema"));
		assertTrue(snapshot.contains("requireKnownManipulations(player) instanceof KnownManipulations"));
		assertTrue(snapshot.contains("HemoJourneyManipulationState.capture(knownManipulations"));
		assertTrue(snapshot.contains("HemoJourneyManipulationState.apply(manipulations"));
		assertTrue(snapshot.contains("HemoJourneyManipulationState.matches(manipulations"));
		assertTrue(snapshot.contains("new KnownManipulations()"));
		assertTrue(snapshot.contains("new KnownManipulationServerPacket"));
		assertTrue(snapshot.contains("validateAdvancementOperations"));
		assertTrue(snapshot.contains("captureLiveState(player)"));
		assertTrue(snapshot.contains("rollbackLiveState"));
		assertTrue(snapshot.contains("applyStateSafely"));
		assertTrue(snapshot.contains("progress.isDone() != targetComplete"));
		assertTrue(snapshot.contains("getAdvancements().revoke(advancement, criterion)"));
		assertTrue(snapshot.contains("if (!teleportMatches(player, target))"));
		assertTrue(snapshot.contains("boolean wasEventBlocked = equipment.isEventBlocked()"));
		assertTrue(snapshot.contains("getCompletedCriteria()"));
		assertTrue(snapshot.contains("getAdvancements().revoke(advancement, criterion)"));
		assertTrue(snapshot.contains("HarbingerAdvancementGranter.grantIfNotDone(player, id)"));
		assertTrue(snapshot.contains("ADV_REWARD_CLAIMED"));
		assertTrue(snapshot.contains("Snapshot restore failed: missing or invalid blood state."));
		assertTrue(snapshot.contains("Advancement restore failed: snapshot is missing "));
		assertTrue(snapshot.contains("Advancement restore failed after applying "));

		var advancementIds = Pattern.compile("Hemomancy\\.rloc\\(\"([^\"]+)\"\\)")
				.matcher(snapshot).results().map(match -> match.group(1)).toList();
		assertEquals(java.util.List.of(
				"hemomancy/the_first_awakening",
				"hemomancy/degree_1_neophyte",
				"hemomancy/vessel_filled",
				"hemomancy/fane_sanguinium",
				"hemomancy/iron_in_the_blood"), advancementIds);
		assertTrue(snapshot.contains("if (data.contains(SNAPSHOT_KEY))"));
		assertTrue(controller.contains("HemoJourneySnapshot.capture(player)"));
		assertTrue(controller.contains("HemoJourneySnapshot.resetForJourney(player)"));
		assertTrue(controller.contains("HemoJourneySnapshot.restore(player)"));
		assertTrue(controller.contains("VERIFIED_STAGE_KEY"));
		assertTrue(controller.contains("cleanupForExit(player, currentStage(player), origin"));
		assertTrue(controller.split("cleanupForExit", -1).length - 1 >= 3,
				"COMPLETE/reset/clear must share exact owned-output exit cleanup");
		assertTrue(fixtures.contains("hemomancy.dev_test.journey.fixture_dimension"));
		assertTrue(fixtures.contains("fixtureLevel(ServerPlayer player)"));
		assertTrue(fixtures.contains("player.getServer().getLevel(dimension)"));
		assertTrue(controller.contains("HemoJourneyFixtures.fixtureLevel(player)"));
		int stageCommit = controller.indexOf("putString(HemoJourneySnapshot.STAGE_KEY, next.id())");
		assertTrue(stageCommit >= 0 && stageCommit < controller.indexOf("remove(VERIFIED_STAGE_KEY)", stageCommit));
	}

	@Test
	void isolatedJourneyClientRunAndOperatorGuideRemainAvailable() throws IOException {
		String build = read("build.gradle");
		String guide = read("docs/TESTING.md");

		var runProfile = Pattern.compile("alphaJourneyClient\\s*\\{(?<profile>.*?)^        \\}", Pattern.DOTALL | Pattern.MULTILINE)
				.matcher(build);
		assertTrue(runProfile.find(), "build must declare the alphaJourneyClient run");
		String profile = runProfile.group("profile");
		assertTrue(profile.contains("client()"));
		assertTrue(profile.contains("gameDirectory = project.file('run-alpha-journey')"));
		assertTrue(profile.contains("additionalRuntimeClasspathConfiguration.extendsFrom(clientLocalRuntime)"));
		assertTrue(profile.contains("jvmArguments.addAll(\"-Xms8G\", \"-Xmx24G\")"));
		assertTrue(profile.contains("systemProperty 'neoforge.enabledGameTestNamespaces', project.mod_id"));

		var modBinding = Pattern.compile("\"\\$\\{mod_id\\}\"\\s*\\{(?<binding>.*?)^        \\}",
				Pattern.DOTALL | Pattern.MULTILINE).matcher(build);
		assertTrue(modBinding.find(), "build must bind Hemomancy development sources");
		assertTrue(modBinding.group("binding").contains("sourceSet(sourceSets.main)"));
		assertTrue(modBinding.group("binding").contains("sourceSet(sourceSets.gameTest)"));

		var publication = Pattern.compile("register\\('mavenJava', MavenPublication\\)\\s*\\{(?<publication>.*?)^        \\}",
				Pattern.DOTALL | Pattern.MULTILINE).matcher(build);
		assertTrue(publication.find(), "build must retain the public Java publication boundary");
		assertTrue(publication.group("publication").contains("from components.java"));
		assertTrue(!publication.group("publication").contains("gameTest"),
				"release publication must not include the development-only gameTest source set");

		assertTrue(guide.contains("./gradlew.bat runAlphaJourneyClient"));
		assertTrue(guide.contains("create or open a world"));
		assertTrue(guide.contains("/hemo test journey start"));
		assertTrue(guide.contains("/hemo test journey next"));
		assertTrue(guide.contains("/hemo test journey status"));
		assertTrue(guide.contains("/hemo test journey reset"));
		assertTrue(guide.contains("/hemo test clear"));
		assertTrue(guide.contains("restores the snapshot"));
		assertTrue(guide.contains("`alphaCheck` remains the automated"));
	}

	@Test
	void journeyUsesAFullJugAndToleratesACompletedAttunedProjection() throws IOException {
		String fixtures = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyFixtures.java");
		String checks = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyChecks.java");
		String controller = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyController.java");

		assertTrue(fixtures.contains("case VESSEL_FILLED -> player.setItemSlot(EquipmentSlot.OFFHAND,"));
		assertTrue(fixtures.contains("new ItemStack(ItemInit.bloody_jug.get())"),
				"The 5,000 mL vessel checkpoint must supply a 5,000 mL Bloody Jug");
		assertTrue(checks.contains("HemoJourneyCheckpointRules.formationPassed("),
				"Formation verification must accept real held-use completion rather than one exact tick delta");
		assertTrue(controller.contains("Bloody Jug"));
	}

	@Test
	void liberAdvancementUsesTheCraftedItemsPlayerFacingName() throws IOException {
		String language = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		String checks = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneyChecks.java");

		assertTrue(language.contains("\"advancements.hemomancy.fane_sanguinium.title\": \"Liber Sanguinum\""));
		assertTrue(checks.contains("\"Liber Sanguinum\", \"Liber Sanguinum\""));
	}

	@Test
	void journeyResetClearsCarriedPotionEffects() throws IOException {
		String snapshot = read(
				"src/gameTest/java/com/vincenthuto/hemomancy/gametest/journey/HemoJourneySnapshot.java");
		int resetStart = snapshot.indexOf("HemoJourneyResult resetForJourney(ServerPlayer player)");
		int restoreStart = snapshot.indexOf("HemoJourneyResult restore(ServerPlayer player)", resetStart);

		assertTrue(resetStart >= 0 && restoreStart > resetStart);
		assertTrue(snapshot.substring(resetStart, restoreStart).contains("player.removeAllEffects();"),
				"Journey reset must clear effects acquired during test stages, including Blood Drunkenness");
	}

	@Test
	void structureSpawnerLoadsAndLightsRecipeOfferings() throws IOException {
		String placement = read("src/main/java/com/vincenthuto/hemomancy/common/network/PlaceStructurePacket.java");
		String showcase = read(
				"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/DebugShowcaseItem.java");

		assertTrue(placement.contains("bloodStructure.getOfferings()"),
				"Blood structure offerings must be passed into Structure Spawner placement");
		assertTrue(placement.contains("BloodStructureOfferingPlacement.plan("),
				"Structure Spawner offerings must use the shared deterministic planner");
		assertTrue(placement.contains("BlockInit.iron_brazier.get().defaultBlockState()"));
		assertTrue(placement.contains("setValue(BrazierBlock.RITUAL_PHASE, 1)"),
				"Structure Spawner offering braziers must spawn lit");
		assertTrue(placement.contains("insertOffering(null, offeringStack)"),
				"Structure Spawner offering braziers must contain representative recipe items");
		assertTrue(showcase.contains("recipe.getOfferings()"),
				"Debug Showcase must pass each Blood Structure recipe's offerings into placement");
		assertTrue(showcase.contains("BloodStructureOfferingPlacement.plan("),
				"Debug Showcase offerings must use the shared deterministic planner");
		assertTrue(showcase.contains("setValue(BrazierBlock.RITUAL_PHASE, 1)"),
				"Debug Showcase offering braziers must spawn lit");
		assertTrue(showcase.contains("insertOffering(null, offeringStack)"),
				"Debug Showcase offering braziers must contain representative recipe items");
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}
}
