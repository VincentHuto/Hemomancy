package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerPilotGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private HarbingerPilotGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureLocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_locked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void bloodStructureUnlocked(GameTestHelper helper) {
		runScenario(helper, "blood_structure_unlocked");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerAssignmentReady(GameTestHelper helper) {
		runScenario(helper, "artificer_assignment_ready");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void artificerRewardClaimed(GameTestHelper helper) {
		runScenario(helper, "artificer_reward_claimed");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void uninitiatedCannotPassBloodcraftDegreeGate(GameTestHelper helper) {
		runScenario(helper, "uninitiated_cannot_pass_bloodcraft_degree_gate");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationRecipeLoaded(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_recipe_loaded");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationDegreeMapping(GameTestHelper helper) {
		runScenario(helper, "sanguine_initiation_degree_mapping");
	}

	private static void runScenario(GameTestHelper helper, String id) {
		HemoTestScenario scenario = HemoTestScenarioCatalog.find(id)
				.orElseThrow(() -> new IllegalArgumentException("Unknown scenario " + id));
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			scenario.setup().apply(player);
			HemoTestResult result = scenario.verify().check(player);
			helper.assertTrue(result.passed(), result.message());
			helper.succeed();
		} finally {
			scenario.clear().apply(player);
			player.discard();
		}
	}

	private static ServerPlayer detachedTestPlayer(GameTestHelper helper) {
		return new ServerPlayer(
				helper.getLevel().getServer(),
				helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "hemomancy-test-player"),
				ClientInformation.createDefault());
	}

}
