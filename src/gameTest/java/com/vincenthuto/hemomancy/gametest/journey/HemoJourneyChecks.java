package com.vincenthuto.hemomancy.gametest.journey;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.tile.crafting.VialCentrifugeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import com.vincenthuto.hemomancy.common.init.BlockInit;

/** Reads server-authoritative outcomes and optionally attributes exact output UUIDs. */
public final class HemoJourneyChecks {
	private HemoJourneyChecks() { }

	public static HemoJourneyResult verify(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		return verify(player, stage, origin, false);
	}

	public static HemoJourneyResult verify(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			boolean claimOutputs) {
		List<String> unmet = new ArrayList<>();
		switch (stage) {
			case MORTAL_DISPLAY -> verifyMortalDisplay(player, origin, unmet);
			case SANGUINE_INITIATION -> verifyInitiation(player, origin, unmet);
			case VESSEL_FILLED -> {
				require(unmet, HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume() >= 5000.0D,
						"Blood vessel has not reached 5,000 mL.");
				require(unmet, HarbingerAdvancementGranter.isVesselFilled(player),
						"Vessel Filled milestone is incomplete.");
			}
			case FORMATION_PROJECTED -> verifyFormation(player, origin, unmet, claimOutputs);
			case LIBER_CRAFTED -> verifyCraft(player, stage, origin, unmet, claimOutputs,
					HarbingerAdvancementGranter.isLiberSanguinumCrafted(player), "Liber Sanguinum", "Liber Sanguinum");
			case HEMATIC_IRON_CRAFTED -> verifyCraft(player, stage, origin, unmet, claimOutputs,
					HarbingerAdvancementGranter.isHematicIronBlockCrafted(player), "Hematic Iron Block", "Iron in the Blood");
			case VICAR_REWARD -> verifyVicarReward(player, origin, unmet, claimOutputs);
			case VOTARY_RITE -> require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 2
					&& HarbingerAdvancementGranter.hasAdvancement(player, HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY), "Rite of the Votary is incomplete.");
			case DEGREE_2_REACHED -> require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 2, "Initiatory degree is not exactly 2.");
			case ALCHEMIST_BRIEFING -> require(unmet, FirstSeparationAssignment.isBriefed(player), "The First Separation briefing was not accepted.");
			case CENTRIFUGE_PREPARED -> require(unmet, player.getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.vial_centrifuge.get().asItem())) > 0
					&& centrifuge(player, origin) != null, "Craft and place your own Vial Centrifuge at the fixture center.");
			case SEPARATION_STARTED -> require(unmet, centrifuge(player, origin) != null && centrifuge(player, origin).isSpinning()
					&& HarbingerAdvancementGranter.isFirstSeparationStarted(player), "Vial Centrifuge separation has not started.");
			case ENZYME_RECOVERED -> require(unmet, HarbingerAdvancementGranter.isFirstSeparationComplete(player), "Recover the enzyme from the centrifuge output.");
			case ALCHEMIST_REWARD -> require(unmet, FirstSeparationAssignment.isClaimed(player), "First Separation reward has not been claimed.");
			case INITIATE_RITE -> verifyRankup(player, 3, HarbingerAdvancementGranter.ADV_DEGREE_3_INITIATE,
					"Rite of the Incarnadine Fane", unmet);
			case ADEPT_RITE -> verifyRankup(player, 4, HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT,
					"Rite of the Sanguine Brotherhood", unmet);
			case ILLUMINATUS_RITE -> verifyRankup(player, 5, HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS,
					"Rite of the Crimson Lodge", unmet);
			case SANCTIFIED_RITE -> verifyRankup(player, 6, HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED,
					"Rite of the Bloodline Covenant", unmet);
			case ARCHON_RITE -> verifyRankup(player, 7, HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON,
					"Rite of the Hematic Order", unmet);
			case COMPLETE -> { return new HemoJourneyResult(true, stage, "Journey checkpoints complete; ready to restore the snapshot."); }
		}
		return unmet.isEmpty() ? new HemoJourneyResult(true, stage, "All checkpoint conditions passed.")
				: HemoJourneyResult.fail(stage, String.join("\n", unmet));
	}

	private static void verifyMortalDisplay(ServerPlayer player, BlockPos origin, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireBloodVolume(player).isActive(), "Blood magic is not active.");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
				Hemomancy.rloc("hemomancy/the_first_awakening")), "The First Awakening advancement is incomplete.");
		require(unmet, HemoJourneyFixtures.fixtureLevel(player).getBlockState(origin.above())
				.is(com.vincenthuto.hemomancy.common.init.BlockInit.placed_blood_stained_stone.get()),
				"The Mortal Display has not transformed.");
		boolean equipped = HemoCapabilityAccess.requireEquipment(player).getStackInSlot(5)
				.is(ItemInit.charm_of_vascularium.get());
		require(unmet, equipped || fixtureHasItem(player, origin, ItemInit.charm_of_vascularium.get()),
				"Charm of Vascularium is neither equipped nor dropped in the fixture.");
	}

	private static void verifyInitiation(ServerPlayer player, BlockPos origin, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == 1,
				"Initiatory degree is not exactly 1.");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player,
				HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE), "Degree-1 milestone is incomplete.");
		require(unmet, hasItem(player, origin, ItemInit.sanguine_conduit.get()),
				"Sanguine Conduit reward was not found on the player or in the fixture.");
	}

	private static void verifyFormation(ServerPlayer player, BlockPos origin, List<String> unmet,
			boolean claimOutputs) {
		double start = HemoJourneyFixtures.baselineBlood(player);
		double current = HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume();
		boolean output = outputPresent(player, HemoJourneyStage.FORMATION_PROJECTED, origin, claimOutputs);
		require(unmet, HemoJourneyCheckpointRules.formationPassed(start, current, output),
				"The attuned projection did not spend at least 100 mL and produce its formation.");
		require(unmet, output,
				"No attributable Sanguine Formation output was produced after preparation.");
	}

	private static void verifyCraft(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			List<String> unmet, boolean claimOutputs, boolean advancementComplete, String outputLabel,
			String advancementLabel) {
		boolean output = outputPresent(player, stage, origin, claimOutputs);
		require(unmet, output, "No attributable " + outputLabel + " output was produced after preparation.");
		require(unmet, HemoJourneyCheckpointRules.craftPassed(output,
				HemoJourneyFixtures.baselineAdvancementIncomplete(player), advancementComplete),
				advancementLabel + " milestone is incomplete; pick up the crafted " + outputLabel + ".");
	}

	private static void verifyVicarReward(ServerPlayer player, BlockPos origin, List<String> unmet,
			boolean claimOutputs) {
		require(unmet, HarbingerAdvancementGranter.isVesselFilled(player), "Vessel Filled milestone no longer holds.");
		require(unmet, HarbingerAdvancementGranter.isLiberSanguinumCrafted(player), "Fane Sanguinium milestone no longer holds.");
		require(unmet, HarbingerAdvancementGranter.isHematicIronBlockCrafted(player), "Iron in the Blood milestone no longer holds.");
		boolean outputs = outputPresent(player, HemoJourneyStage.VICAR_REWARD, origin, claimOutputs);
		require(unmet, HemoJourneyCheckpointRules.rewardPassed(outputs,
				HemoJourneyFixtures.baselineAdvancementIncomplete(player), FirstBloodcraftAssignment.isClaimed(player)),
				"First Bloodcraft reward has not been newly claimed with its exact kit.");
	}

	private static void verifyRankup(ServerPlayer player, int degree,
			net.minecraft.resources.ResourceLocation advancement, String riteName, List<String> unmet) {
		require(unmet, HemoCapabilityAccess.requireInitiatoryDegree(player).getDegreeNumber() == degree,
				riteName + " has not raised the Initiatory Degree to " + degree + ".");
		require(unmet, HarbingerAdvancementGranter.hasAdvancement(player, advancement),
				riteName + " advancement is incomplete.");
	}

	private static boolean outputPresent(ServerPlayer player, HemoJourneyStage stage, BlockPos origin,
			boolean claimOutputs) {
		return claimOutputs ? HemoJourneyFixtures.captureExpectedOutputs(player, stage, origin)
				: HemoJourneyFixtures.expectedOutputsPresent(player, stage, origin);
	}

	private static boolean hasItem(ServerPlayer player, BlockPos origin, Item item) {
		return inventoryCount(player, item) > 0 || fixtureHasItem(player, origin, item);
	}

	private static boolean fixtureHasItem(ServerPlayer player, BlockPos origin, Item item) {
		return !HemoJourneyFixtures.fixtureLevel(player).getEntitiesOfClass(ItemEntity.class, HemoJourneyFixtures.bounds(origin),
				entity -> entity.getItem().is(item)).isEmpty();
	}

	private static int inventoryCount(ServerPlayer player, Item item) {
		int count = 0;
		for (ItemStack stack : player.getInventory().items) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().offhand) if (stack.is(item)) count += stack.getCount();
		for (ItemStack stack : player.getInventory().armor) if (stack.is(item)) count += stack.getCount();
		return count;
	}

	private static void require(List<String> unmet, boolean condition, String message) {
		if (!condition) unmet.add(message);
	}

	private static VialCentrifugeBlockEntity centrifuge(ServerPlayer player, BlockPos origin) {
		return HemoJourneyFixtures.fixtureLevel(player).getBlockEntity(origin.above(2)) instanceof VialCentrifugeBlockEntity value ? value : null;
	}
}
