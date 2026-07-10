package com.vincenthuto.hemomancy.common.mission;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

public final class FirstBloodcraftAssignmentSourceTest {
	private static final Path HELPER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/mission/FirstBloodcraftAssignmentHelper.java");
	private static final Path CLAIM_ADVANCEMENT = Path.of(
			"src/main/resources/data/hemomancy/advancement/hemomancy/first_bloodcraft_reward_claimed.json");
	private static final Path VICAR_ENTITY = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/harbinger/HarbingerVicarEntity.java");
	private static final Path VICAR_DIALOGUE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
	private static final Path DIALOGUE_HANDLER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueEventHandler.java");

	private FirstBloodcraftAssignmentSourceTest() {
	}

	@Test
	void firstBloodcraftEligibilityRewardsAndClaimRecordExist() throws IOException {
		String helper = read(HELPER);
		String advancement = read(CLAIM_ADVANCEMENT);

		assertContains("eligibility checks the filled vessel milestone", helper,
				"HarbingerAdvancementGranter.isVesselFilled(player)");
		assertContains("eligibility checks the Liber Sanguinum milestone", helper,
				"HarbingerAdvancementGranter.isLiberSanguinumCrafted(player)");
		assertContains("eligibility checks the Hematic Iron milestone", helper,
				"HarbingerAdvancementGranter.isHematicIronBlockCrafted(player)");
		assertContains("eligibility excludes claimed rewards", helper, "!isClaimed(player)");
		assertContains("claim advancement has the required id", helper,
				"Hemomancy.rloc(\"hemomancy/first_bloodcraft_reward_claimed\")");
		assertContains("claim delegates to the advancement granter", helper,
				"HarbingerAdvancementGranter.grantIfNotDone(player, ADV_REWARD_CLAIMED)");
		assertContains("claim persistence is verified", helper,
				"return isClaimed(player);");
		assertContains("claim reports persistence success", helper,
				"public static boolean markClaimed(ServerPlayer player)");

		assertContains("reward includes four Hematic Iron Scraps", helper,
				"new ItemStack(ItemInit.hematic_iron_scrap.get(), 4)");
		assertContains("reward includes eight Befouling Ash Trails", helper,
				"new ItemStack(BlockInit.befouling_ash_trail.get().asItem(), 8)");
		assertContains("reward includes two Sanguine Formations", helper,
				"new ItemStack(ItemInit.sanguine_formation.get(), 2)");

		assertContains("claim advancement is hidden", advancement, "\"hidden\": true");
		assertContains("claim advancement is granted only by code", advancement,
				"\"trigger\": \"minecraft:impossible\"");
	}

	@Test
	void vicarExposesAndSecurelyHandlesFirstBloodcraftRewardClaim() throws IOException {
		String entity = read(VICAR_ENTITY);
		String dialogue = read(VICAR_DIALOGUE);
		String handler = read(DIALOGUE_HANDLER);

		assertContains("dialogue publishes the claim event id", dialogue,
				"EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD = \"vicar_claim_first_bloodcraft_reward\"");
		assertContains("Degree 1 receives claim readiness", dialogue,
				"case 1 -> neophyte(entityId, hasFoundHermitRoadRemnant, hasHermitRoadLedger,\n"
						+ "\t\t\t\t\tcanClaimFirstBloodcraftReward, firstBloodcraftRewardClaimed);");
		assertContains("ready Neophytes receive the claim option", dialogue,
				"if (canClaimFirstBloodcraftReward)");
		assertContains("claim option fires the published event", dialogue,
				"\"first_bloodcraft_reward_granted\", EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD");
		assertContains("dialogue includes a claimed response", dialogue,
				"hemomancy.vicar.neophyte.first_bloodcraft_reward.claimed");
		assertContains("dialogue includes an unready response", dialogue,
				"hemomancy.vicar.neophyte.first_bloodcraft_reward.unready");

		assertContains("Vicar computes fresh claim readiness", entity,
				"FirstBloodcraftAssignmentHelper.canClaim(serverPlayer)");
		assertContains("Vicar computes claimed state", entity,
				"FirstBloodcraftAssignmentHelper.isClaimed(serverPlayer)");

		assertContains("handler routes the published event", handler,
				"case HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD");
		assertContains("handler re-checks eligibility", handler,
				"if (!FirstBloodcraftAssignmentHelper.canClaim(player))");
		assertContains("handler obtains rewards solely from the helper", handler,
				"for (ItemStack stack : FirstBloodcraftAssignmentHelper.rewardStacks())");
		assertContains("handler gives or drops every reward stack", handler,
				"giveOrDropAtEntity(player, entityId, stack);");
		assertContains("handler persists the one-time claim before mutation", handler,
				"if (!FirstBloodcraftAssignmentHelper.markClaimed(player))");
		assertContains("handler explains persistence failure", handler,
				"hemomancy.dialogue.event.vicar_first_bloodcraft_reward_claim_failed");

		assertOrdered("eligibility is checked before inventory mutation", handler,
				"if (!FirstBloodcraftAssignmentHelper.canClaim(player))",
				"for (ItemStack stack : FirstBloodcraftAssignmentHelper.rewardStacks())");
		assertOrdered("claim is persisted before any reward stack is granted", handler,
				"if (!FirstBloodcraftAssignmentHelper.markClaimed(player))",
				"for (ItemStack stack : FirstBloodcraftAssignmentHelper.rewardStacks())");
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertOrdered(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex >= secondIndex) {
			throw new AssertionError(label + ": expected " + first + " before " + second);
		}
	}
}
