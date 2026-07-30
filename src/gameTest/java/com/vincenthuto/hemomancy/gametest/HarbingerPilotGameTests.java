package com.vincenthuto.hemomancy.gametest;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
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

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void administrativeKillRemovesProtectedNpcs(GameTestHelper helper) {
		List<Mob> npcs = List.of(
				EntityInit.unstained_zealot.get().create(helper.getLevel()),
				EntityInit.unstained_guardian.get().create(helper.getLevel()),
				EntityInit.unstained_acolyte.get().create(helper.getLevel()),
				EntityInit.unstained_scout.get().create(helper.getLevel()),
				EntityInit.harbinger_hermit.get().create(helper.getLevel()),
				EntityInit.harbinger_alchemist.get().create(helper.getLevel()),
				EntityInit.harbinger_artificer.get().create(helper.getLevel()),
				EntityInit.harbinger_cicatrix_anchorite.get().create(helper.getLevel()),
				EntityInit.harbinger_mnemonist.get().create(helper.getLevel()),
				EntityInit.harbinger_vicar.get().create(helper.getLevel()),
				EntityInit.harbinger_voyager.get().create(helper.getLevel()),
				EntityInit.harbinger_votary_wayfarer.get().create(helper.getLevel()));
		try {
			for (Mob npc : npcs) {
				helper.getLevel().addFreshEntity(npc);
				npc.kill();
				helper.assertTrue(npc.isDeadOrDying() || npc.isRemoved(),
						npc.getType() + " must honor administrative kill commands");
			}
			helper.succeed();
		} finally {
			npcs.forEach(Mob::discard);
		}
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
