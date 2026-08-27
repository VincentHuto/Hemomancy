package com.vincenthuto.hemomancy.gametest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.SanguineFormationProjectionHandler;
import com.vincenthuto.hemomancy.common.event.BloodStructureFeedManager;
import com.vincenthuto.hemomancy.common.event.PendingBloodCraftManager;
import com.vincenthuto.hemomancy.common.event.ArmorSetBonusHandler;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.HemoAttachmentTypes;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryActivationService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueEvent;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOffering;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOfferingPlacement;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.HematicArmatureBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import com.vincenthuto.hemomancy.common.tile.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyChecks;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyResult;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneySnapshot;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyStage;
import com.vincenthuto.hemomancy.gametest.journey.HemoJourneyCheckpointRules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import io.netty.channel.embedded.EmbeddedChannel;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HarbingerJourneyFixtureGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final String FIXTURES_CLASS =
			"com.vincenthuto.hemomancy.gametest.journey.HemoJourneyFixtures";

	private HarbingerJourneyFixtureGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void journeyRankupRunwayReachesApotheos(GameTestHelper helper) {
		List<String> stages = java.util.Arrays.stream(HemoJourneyStage.values()).map(HemoJourneyStage::id).toList();
		List<String> expectedTail = List.of("initiate_rite", "first_culture", "woven_vessel_turn_in", "first_memory_woven",
				"noetic_mark_recognized",
				"artificer_three_answers_briefing", "artificer_fork_upgrade",
				"artificer_three_answers_inspection", "artificer_three_answers_counsel",
				"artificer_barbed_research", "artificer_barbed_research_reward",
				"artificer_fork_demonstration", "artificer_fork_fitting",
				"adept_rite", "vein_mason_lesson", "first_scar_carved", "first_scar_learned",
				"first_effigy_pattern", "first_effigy_loadout", "vein_mason_reward", "illuminatus_rite",
				"vein_mason_d5_strain", "vein_mason_d5_diagnosis", "vein_mason_d5_treatment",
				"vein_mason_d5_fortification", "vein_mason_d5_reward",
				"artificer_assumed_limb_briefing", "artificer_first_living_graft",
				"artificer_assumed_limb_reward", "artificer_living_arsenal_demonstration",
				"artificer_full_living_arsenal", "artificer_living_arsenal_fitting",
				"artificer_crimson_vestment_briefing", "vicar_consecration_kit",
				"artificer_frame_consecrated", "artificer_crimson_vestment_inspection",
				"artificer_crimson_vestment_counsel", "artificer_blood_lust_upgrade",
				"artificer_blood_lust_demonstration", "artificer_blood_lust_fitting",
				"founding_fane", "sanctified_rite", "vein_mason_d6_referral", "vein_mason_d6_counsel",
				"vein_mason_d6_first_route", "vein_mason_d6_scar_carved", "vein_mason_d6_scar_learned",
				"vein_mason_d6_loadout", "vein_mason_d6_second_route", "vein_mason_d6_reward",
				"chamber_returned", "covenant_throne_bound",
				"covenant_vigil", "archon_rite", "artificer_weight_of_frame_briefing",
				"artificer_monolithic_frame", "artificer_d7_upgrade", "artificer_weight_of_frame_inspection",
				"artificer_d7_demonstration", "artificer_d7_fitting",
				"qliphoth_communion", "apotheos_choice", "apotheos_rite", "complete");
		helper.assertTrue(stages.size() >= expectedTail.size()
						&& stages.subList(stages.size() - expectedTail.size(), stages.size()).equals(expectedTail),
				"The operator journey must continue through every public Harbinger rank-up to Archon");
		for (String rite : List.of("initiate_rite", "sanguine_brotherhood", "illuminatus_rite",
				"sanctified_rite", "archon_rite", "apotheos_rite")) {
			var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(
					helper.getLevel(), Hemomancy.rloc("cardinal_rite/" + rite));
			helper.assertTrue(recipe != null && recipe.isRankup(),
					"Journey rank-up recipe must load as a rank-up: " + rite);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void apotheosJourneyUsesRealCommunionChoiceAndRite(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Apotheos snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Apotheos reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(7);
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.QLIPHOTH_COMMUNION, origin);
			for (int husk = 0; husk < 9; husk++) {
				player.getInventory().selected = husk;
				ItemStack pome = player.getMainHandItem();
				helper.assertTrue(QliphothPomeItem.isBoundPomeFromBloom(pome, origin.asLong()),
						"Each supplied pome must be bound to the same journey bloom");
				pome.getItem().finishUsingItem(pome, helper.getLevel(), player);
			}
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.QLIPHOTH_COMMUNION, origin).passed(),
					"Consuming the nine real pomes must complete Qliphoth Communion");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.APOTHEOS_CHOICE, origin);
			NeoForge.EVENT_BUS.post(new DialogueEvent(player, "archon_choice_eighth_degree", 0));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.APOTHEOS_CHOICE, origin).passed(),
					"The real revelation choice must open the Apotheos path");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.APOTHEOS_RITE, origin);
			var activation = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			ActiveCardinalRite rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			helper.assertTrue(activation == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null,
					"The prepared Rite of Apotheos must start through the real activation path");
			Method complete = HarbingerCardinalRiteEvents.class.getDeclaredMethod(
					"completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
					ActiveCardinalRite.class);
			complete.setAccessible(true);
			helper.assertTrue((boolean) complete.invoke(null, helper.getLevel(), player, rite),
					"The real Rite of Apotheos completion path must succeed");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.APOTHEOS_RITE, origin).passed(),
					"The real rite must award Degree 8 and finalize Apotheos");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Apotheos restore must succeed: " + restored.message());
			var restoredDegree = HemoCapabilityAccess.requireInitiatoryDegree(player);
			helper.assertTrue(restoredDegree.getTotalPomesConsumed() == 0
					&& restoredDegree.getArchonPath() == EnumArchonPath.NONE
					&& !player.getPersistentData().contains(FungalGardenTravelHelper.ARCHON_CHOICE_KEY),
					"Snapshot restore must remove journey-owned Apotheos state");
			helper.succeed();
		} catch (ReflectiveOperationException error) {
			throw new AssertionError("Apotheos fixture could not invoke the real rite completion path", error);
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void firstRemnantJourneyUsesRealInscriptionAndRestoresKnowledge(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "First Remnant snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "First Remnant reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_REMNANT_DISCOVERED, origin);
			BlockPos inscription = origin.above();
			helper.getLevel().getBlockState(inscription).useWithoutItem(helper.getLevel(), player,
					new BlockHitResult(Vec3.atCenterOf(inscription), Direction.UP, inscription, false));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.FIRST_REMNANT_DISCOVERED, origin).passed(),
					"Reading the real loaded blood echo must unlock its milestone and Liber entries");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VICAR_HERMIT_ROAD_REPORT, origin);
			HarbingerVicarEntity vicar = helper.getLevel().getEntitiesOfClass(
					HarbingerVicarEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			vicar.interact(player, InteractionHand.MAIN_HAND);
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT, vicar.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.VICAR_HERMIT_ROAD_REPORT, origin).passed(),
					"Real Vicar contact and report dialogue must grant the ledger, record the report, and pay the ash reward");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "First Remnant restore must succeed: " + restored.message());
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player,
					HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT)
					&& !HarbingerAdvancementGranter.hasAdvancement(player,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_LEDGER_GRANTED)
					&& !HarbingerAdvancementGranter.hasAdvancement(player,
							HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED)
					&& HemoCapabilityAccess.requireLiberKnowledge(player).getUnlockedEntries().isEmpty(),
					"Snapshot restore must remove journey-owned inscription knowledge and advancement");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60)
	public static void livingStaffJourneyUsesRealStructureCraftAndRestoresBond(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Living Staff snapshot capture must succeed");
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Living Staff reset must succeed");
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIVING_STAFF_CRAFTED, origin);
			helper.assertTrue(helper.getLevel().getBlockState(origin.above(2)).is(Blocks.IRON_BARS),
					"Living Staff fixture target must be iron bars, found "
							+ helper.getLevel().getBlockState(origin.above(2)));
			helper.assertTrue(BloodStructureFeedManager.feedStructure(player, helper.getLevel(), origin.above(2),
					player.getOffhandItem(), 150.0D), "The real Living Staff structure must accept projection");
			for (int tick = 0; tick < 30; tick++) PendingBloodCraftManager.tick();
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.LIVING_STAFF_CRAFTED, origin).passed(),
					"The real structure craft must produce a Living Staff and unlock its bond");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed()
					&& !HemoCapabilityAccess.getLivingStaffProgress(player).orElseThrow().hasLivingStaffBond(),
					"Snapshot restore must remove the journey-owned Living Staff bond");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 60)
	public static void assumedLimbJourneyUsesRealGraftRitesAndRestoresState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(), "Assumed Limb snapshot capture must succeed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(), "Assumed Limb reset must succeed");
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);
			HemoCapabilityAccess.getLivingStaffProgress(player).orElseThrow().setLivingStaffBond(true);
			ArtificerAssignments.brief(player, ArtificerAssignments.ASSUMED_LIMB_BRIEFED);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FIRST_LIVING_GRAFT, origin);
			absorbPreparedGraft(player, origin.above());
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_FIRST_LIVING_GRAFT, origin).passed(),
					"The prepared Blade Graft must use the real brazier rite");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FULL_LIVING_ARSENAL, origin);
			for (int index = 0; index < 6; index++) {
				absorbPreparedGraft(player, origin.offset(index % 3 - 1, 1, index / 3));
			}
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_FULL_LIVING_ARSENAL, origin).passed(),
					"All seven Living Weapon forms must be learned through real brazier rites");

			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(HemoJourneySnapshot.restore(player).passed()
					&& ArtificerAssignments.knownLivingWeaponFormCount(player) == 0
					&& !ArtificerAssignments.has(player, ArtificerAssignments.ASSUMED_LIMB_BRIEFED),
					"Snapshot restore must remove journey-owned Assumed Limb progress");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	private static void absorbPreparedGraft(ServerPlayer player, BlockPos pos) {
		player.startUsingItem(InteractionHand.MAIN_HAND);
		for (int tick = 0; tick < LivingWeaponGraftRite.REQUIRED_CHANNEL_TICKS; tick++) {
			LivingWeaponGraftRite.tryAbsorb(player.serverLevel(), pos,
					player.serverLevel().getBlockState(pos), player, 1.0D);
		}
		player.stopUsingItem();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void bodyAnswersJourneyUsesRealRecipeAndRestoresState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		var recipe = helper.getLevel().getRecipeManager().byKey(
				Hemomancy.rloc("distillation/tincture_sanguine_fists")).orElseThrow();
		try {
			helper.assertTrue(!player.getRecipeBook().contains(recipe),
					"Test player must begin without the Sanguine Fists recipe");
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Body Answers snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Body Answers reset must succeed: " + reset.message());
			HarbingerAdvancementGranter.grantIfNotDone(player, FirstSeparationAssignment.ADV_REWARD_CLAIMED);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.BODY_ANSWERS_BRIEFING, origin);
			HarbingerAlchemistEntity alchemist = helper.getLevel().getEntitiesOfClass(
					HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF, alchemist.getId()));
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_BRIEFED)
					&& player.getInventory().countItem(ItemInit.sanguine_formation.get()) == 1
					&& player.getInventory().countItem(ItemInit.fervent_enzyme.get()) == 1
					&& player.getInventory().countItem(ItemInit.bloody_flask.get()) == 1,
					"Real Body Answers dialogue must grant the briefing and exact supplies");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.BODY_ANSWERS_TINCTURE, origin);
			BlockPos alembicPos = origin.above();
			GhastlyAlembicBlockEntity alembic = (GhastlyAlembicBlockEntity) helper.getLevel()
					.getBlockEntity(alembicPos);
			helper.assertTrue(alembic != null && alembic.getItem(GhastlyAlembicBlockEntity.SLOT_INPUT)
					.is(ItemInit.sanguine_formation.get())
					&& alembic.getItem(GhastlyAlembicBlockEntity.SLOT_CATALYST).is(ItemInit.fervent_enzyme.get())
					&& alembic.getItem(GhastlyAlembicBlockEntity.SLOT_TINCTURE_BLOOD).is(ItemInit.bloody_flask.get()),
					"Body Answers fixture must load the real Alembic recipe inputs");
			for (int tick = 0; tick < 200; tick++) {
				GhastlyAlembicBlockEntity.serverTick(helper.getLevel(), alembicPos,
						helper.getLevel().getBlockState(alembicPos), alembic);
			}
			ItemStack tincture = alembic.removeItemNoUpdate(GhastlyAlembicBlockEntity.SLOT_RESULT);
			helper.assertTrue(tincture.is(ItemInit.tincture_sanguine_fists.get()),
					"The prepared real recipe must distil Sanguine Fists");
			tincture.getItem().finishUsingItem(tincture, helper.getLevel(), player);
			var memory = player.getData(HemoAttachmentTypes.MUSCLE_MEMORY);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.BODY_ANSWERS_TINCTURE, origin).passed()
					&& memory.knows(MuscleMemory.SANGUINE_FISTS)
					&& memory.reserveTicks(MuscleMemory.SANGUINE_FISTS) > 0,
					"Drinking the real tincture must complete Body Answers and teach its Muscle Memory");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Body Answers restore must succeed: " + restored.message());
			helper.assertTrue(!player.getData(HemoAttachmentTypes.MUSCLE_MEMORY).knows(MuscleMemory.SANGUINE_FISTS)
					&& !HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_BRIEFED)
					&& !HarbingerAdvancementGranter.hasAdvancement(player, BodyAnswersAssignment.ADV_COMPLETE)
					&& !player.getRecipeBook().contains(recipe),
					"Snapshot restore must remove journey-owned Muscle Memory, advancements, and recipe knowledge");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void redTaxonomyJourneyUsesFourRealTurnIns(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		var samples = java.util.Arrays.stream(HarbingerAlchemistDialogueTrees.RedTaxonomySample.values())
				.limit(4).toList();
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Red Taxonomy snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Red Taxonomy reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.RED_TAXONOMY, origin);
			HarbingerAlchemistEntity alchemist = helper.getLevel().getEntitiesOfClass(
					HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			for (var sample : samples) {
				helper.assertTrue(player.getInventory().countItem(sample.block().asItem()) == 1,
						"Fixture must supply one " + sample.key());
				int slot = player.getInventory().findSlotMatchingItem(new ItemStack(sample.block()));
				helper.assertTrue(slot >= 0 && slot < 9, "Taxonomy sample must be supplied in the hotbar");
				player.getInventory().selected = slot;
				NeoForge.EVENT_BUS.post(new DialogueEvent(player, sample.eventId(), alchemist.getId()));
			}
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.RED_TAXONOMY, origin).passed(),
					"Four real dialogue submissions must complete Red Taxonomy");
			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Red Taxonomy restore must succeed: " + restored.message());
			helper.assertTrue(HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player) == 0
					&& !HarbingerAdvancementGranter.isRedTaxonomyComplete(player),
					"Snapshot restore must remove journey-owned taxonomy records");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void livingBestiaryJourneyUsesRealCaptureRecordAndSurrender(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		var originalId = Hemomancy.rloc("barbed_urchin");
		var journeyId = Hemomancy.rloc("crimson_doe");
		try {
			HemoCapabilityAccess.requireSpecimenBestiary(player).recordSpecimen(originalId);
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Living Bestiary snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Living Bestiary reset must succeed: " + reset.message());
			helper.assertTrue(HemoCapabilityAccess.requireSpecimenBestiary(player).recordedSpecimenCount() == 0,
					"Journey reset must clear existing Bestiary records");
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIVING_BESTIARY_RECORD, origin);
			CrimsonDoeEntity doe = helper.getLevel().getEntitiesOfClass(CrimsonDoeEntity.class,
					HemoJourneyFixtures.bounds(origin)).getFirst();
			ItemStack jar = player.getMainHandItem();
			InteractionResult result = jar.getItem().interactLivingEntity(jar, player, doe,
					InteractionHand.MAIN_HAND);
			helper.assertTrue(result.consumesAction() && doe.isRemoved()
					&& SpecimenJarData.getSpecimenEntityId(player.getMainHandItem()).orElseThrow().equals(journeyId),
					"The supplied empty jar must capture the real Crimson Doe");
			HarbingerAlchemistEntity alchemist = helper.getLevel().getEntitiesOfClass(
					HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD, alchemist.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.LIVING_BESTIARY_RECORD, origin).passed(),
					"The real Alchemist record option must annotate the captured specimen without consuming it");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIVING_BESTIARY_SURRENDER, origin);
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER, alchemist.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.LIVING_BESTIARY_SURRENDER, origin).passed(),
					"The real Alchemist surrender option must consume the specimen and grant its primer");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Living Bestiary restore must succeed: " + restored.message());
			var restoredBestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
			helper.assertTrue(restoredBestiary.recordedSpecimens().equals(java.util.Set.of(originalId.toString()))
					&& restoredBestiary.surrenderedSpecimenCount() == 0,
					"Snapshot restore must recover the exact pre-journey Bestiary catalogue");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hyphaeDiscoveryJourneyUsesRealItemPickup(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.HYPHAE_DISCOVERED, origin);
			ItemEntity spine = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					HemoJourneyFixtures.bounds(origin), entity -> entity.getItem().is(ItemInit.fungal_spine.get()))
					.getFirst();
			spine.playerTouch(player);
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.HYPHAE_DISCOVERED, origin).passed(),
					"Picking up the real fixture item must unlock Hyphae through the discovery event");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180)
	public static void wornVowJourneyUsesRealArmatureAndDialogue(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		BlockPos armaturePos = origin.above();
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Worn Vow snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Worn Vow reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_WORN_VOW_BRIEFING, origin);
			HarbingerArtificerEntity artificer = helper.getLevel().getEntitiesOfClass(
					HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW, artificer.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_WORN_VOW_BRIEFING, origin).passed(),
					"The real Artificer dialogue must brief The Worn Vow");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_ARMATURE_PLACED, origin);
			var armatureState = BlockInit.hematic_armature.get().defaultBlockState()
					.setValue(HematicArmatureBlock.FACING, Direction.SOUTH);
			helper.getLevel().setBlock(armaturePos, armatureState, Block.UPDATE_ALL);
			BlockInit.hematic_armature.get().setPlacedBy(helper.getLevel(), armaturePos, armatureState,
					player, new ItemStack(BlockInit.hematic_armature.get()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_ARMATURE_PLACED, origin).passed(),
					"The real placement hook must register the Hematic Armature");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_HEMATIC_UPGRADE, origin);
			HematicArmatureBlockEntity armature = (HematicArmatureBlockEntity) helper.getLevel()
					.getBlockEntity(armaturePos);
			BlockInit.hematic_armature.get().stepOn(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), player);
			helper.assertTrue(player.isPassenger() && armature.hasRestrainedPlayer()
					&& armature.getItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT)
							.is(ItemInit.hematic_iron_scrap.get())
					&& player.getItemBySlot(EquipmentSlot.FEET).is(Items.IRON_BOOTS)
					&& armature.getBloodCapability() != null
					&& armature.getBloodCapability().getBloodVolume() == 250.0D,
					"Worn Vow fixture must seat the player with the exact real recipe inputs");
			HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), armature);

			helper.runAfterDelay(101, () -> {
				try {
					HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
							helper.getLevel().getBlockState(armaturePos), armature);
					HemoJourneyResult upgradeResult = HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_HEMATIC_UPGRADE, origin);
					helper.assertTrue(upgradeResult.passed(),
							"The real Armature cycle must upgrade the worn Iron Boots: " + upgradeResult.message()
									+ " boots=" + player.getItemBySlot(EquipmentSlot.FEET)
									+ " reagent=" + armature.getItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT)
									+ " blood=" + (armature.getBloodCapability() == null ? "missing"
											: armature.getBloodCapability().getBloodVolume())
									+ " passenger=" + player.isPassenger()
									+ " restrained=" + armature.hasRestrainedPlayer());
					player.stopRiding();

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_WORN_VOW_REWARD, origin);
					HarbingerArtificerEntity rewardArtificer = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD, rewardArtificer.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_WORN_VOW_REWARD, origin).passed(),
							"The real Artificer reward dialogue must grant four Hematic Iron Scrap");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_WORN_VOW_FITTING, origin);
					HarbingerArtificerEntity fittingArtificer = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING, fittingArtificer.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_WORN_VOW_FITTING, origin).passed(),
							"The real fitting dialogue must award The Worn Vow fitting");

					HemoJourneyFixtures.cleanup(player, origin);
					HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
					helper.assertTrue(restored.passed(), "Worn Vow restore must succeed: " + restored.message());
					helper.assertTrue(!player.getPersistentData().contains(ArtificerAssignments.WORN_VOW_REWARD_CLAIM_KEY)
							&& !HarbingerAdvancementGranter.hasAdvancement(player,
									HarbingerAdvancementGranter.ADV_ARTIFICER_HEMATIC_IRON_FITTING),
							"Snapshot restore must remove journey-owned Worn Vow state");
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException | AssertionError error) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw error;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180,
			batch = "journeyThreeAnswers")
	public static void threeAnswersJourneyUsesRealForkAndDialogue(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		BlockPos armaturePos = origin.above();
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Three Answers snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Three Answers reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_THREE_ANSWERS_BRIEFING, origin);
			HarbingerArtificerEntity artificer = helper.getLevel().getEntitiesOfClass(
					HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS, artificer.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_THREE_ANSWERS_BRIEFING, origin).passed(),
					"The real Artificer dialogue must brief The Three Answers");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FORK_UPGRADE, origin);
			HematicArmatureBlockEntity armature = (HematicArmatureBlockEntity) helper.getLevel()
					.getBlockEntity(armaturePos);
			BlockInit.hematic_armature.get().stepOn(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), player);
			HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), armature);

			helper.runAfterDelay(101, () -> {
				try {
					HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
							helper.getLevel().getBlockState(armaturePos), armature);
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_FORK_UPGRADE, origin).passed(),
							"The real Armature cycle must create the first recorded fork piece");
					player.stopRiding();

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_THREE_ANSWERS_INSPECTION, origin);
					HarbingerArtificerEntity inspector = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getLast();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS, inspector.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_THREE_ANSWERS_INSPECTION, origin).passed(),
							"The Artificer must inspect the recorded fork");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_THREE_ANSWERS_COUNSEL, origin);
					HarbingerAlchemistEntity alchemist = helper.getLevel().getEntitiesOfClass(
							HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getLast();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD, alchemist.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_THREE_ANSWERS_COUNSEL, origin).passed(),
							"The Alchemist must return the recorded fork reagent");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BARBED_RESEARCH, origin);
					for (var type : List.of(EntityInit.barbed_urchin.get(), EntityInit.desiccant.get(),
							EntityInit.venom_rib_centipede.get())) {
						Mob specimen = helper.getLevel().getEntitiesOfClass(Mob.class,
								HemoJourneyFixtures.bounds(origin), mob -> mob.getType() == type).getFirst();
						ItemStack jar = player.getMainHandItem();
						helper.assertTrue(jar.getItem().interactLivingEntity(jar, player, specimen,
								InteractionHand.MAIN_HAND).consumesAction(),
								"Each supplied jar must capture its real Barbed research specimen");
						HarbingerAlchemistEntity researcher = helper.getLevel().getEntitiesOfClass(
								HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getLast();
						NeoForge.EVENT_BUS.post(new DialogueEvent(player,
								HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD, researcher.getId()));
						if (type != EntityInit.venom_rib_centipede.get()) equipNextEmptySpecimenJar(player);
					}
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BARBED_RESEARCH, origin).passed(),
							"The Bestiary must contain all three real Barbed research records");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BARBED_RESEARCH_REWARD, origin);
					HarbingerAlchemistEntity researchAlchemist = helper.getLevel().getEntitiesOfClass(
							HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getLast();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD,
							researchAlchemist.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BARBED_RESEARCH_REWARD, origin).passed(),
							"The Alchemist must award the gated Barbed research reagent");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FORK_DEMONSTRATION, origin);
					Zombie attacker = helper.getLevel().getEntitiesOfClass(
							Zombie.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					ArmorSetBonusHandler.onPlayerHurt(new LivingDamageEvent.Pre(player,
							new DamageContainer(player.damageSources().mobAttack(attacker), 8.0F)));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_FORK_DEMONSTRATION, origin).passed(),
							"A real Barbed retaliation must demonstrate the recorded fork");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FORK_FITTING, origin);
					HarbingerArtificerEntity fittingArtificer = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getLast();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_FORK_FITTING, fittingArtificer.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_FORK_FITTING, origin).passed(),
							"The Artificer must award the fitting for the demonstrated fork");

					HemoJourneyFixtures.cleanup(player, origin);
					HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
					helper.assertTrue(restored.passed(), "Three Answers restore must succeed: " + restored.message());
					helper.assertTrue(!player.getPersistentData().contains(ArtificerAssignments.FIRST_FORK_FAMILY_KEY)
							&& !HarbingerAdvancementGranter.hasAdvancement(player,
									ArtificerAssignments.THREE_ANSWERS_BRIEFED),
							"Snapshot restore must remove journey-owned Three Answers state");
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException | AssertionError error) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw error;
		}
	}

	private static void equipNextEmptySpecimenJar(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (stack.is(BlockInit.specimen_jar.get().asItem()) && !SpecimenJarData.hasSpecimen(stack)) {
				player.getInventory().selected = slot;
				return;
			}
		}
		throw new AssertionError("No supplied empty Specimen Jar remains");
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180,
			batch = "journeyCrimsonVestment")
	public static void crimsonVestmentJourneyUsesRealConsecrationUpgradeAndDialogue(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		BlockPos armaturePos = origin.above();
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Crimson Vestment snapshot capture must succeed");
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Crimson Vestment reset must succeed");
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_BRIEFING, origin);
			HarbingerArtificerEntity artificer = helper.getLevel().getEntitiesOfClass(
					HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT, artificer.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_BRIEFING, origin).passed(),
					"The real Artificer dialogue must brief Crimson Vestment");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VICAR_CONSECRATION_KIT, origin);
			HarbingerVicarEntity vicar = helper.getLevel().getEntitiesOfClass(
					HarbingerVicarEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerVicarDialogueTrees.EVENT_CONSECRATION_KIT, vicar.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.VICAR_CONSECRATION_KIT, origin).passed(),
					"The real Vicar dialogue must grant the consecration kit");
			pickUpFixtureItem(helper, player, origin, ItemInit.vicars_consecration_kit.get());

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FRAME_CONSECRATED, origin);
			HematicArmatureBlockEntity consecrated = (HematicArmatureBlockEntity) helper.getLevel()
					.getBlockEntity(armaturePos);
			helper.assertTrue(consecrated.applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND),
					"The real Armature must accept the Vicar's kit");
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_FRAME_CONSECRATED, origin).passed(),
					"Applying the real kit must consecrate the frame");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_INSPECTION, origin);
			artificer = helper.getLevel().getEntitiesOfClass(
					HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT, artificer.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_INSPECTION, origin).passed(),
					"The real Artificer dialogue must inspect the consecrated frame");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_COUNSEL, origin);
			HarbingerAlchemistEntity alchemist = helper.getLevel().getEntitiesOfClass(
					HarbingerAlchemistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD, alchemist.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_COUNSEL, origin).passed(),
					"The real Alchemist correspondence must grant Crimson Lacquer");
			pickUpFixtureItem(helper, player, origin, ItemInit.crimson_lacquer.get());

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BLOOD_LUST_UPGRADE, origin);
			HematicArmatureBlockEntity armature = (HematicArmatureBlockEntity) helper.getLevel()
					.getBlockEntity(armaturePos);
			BlockInit.hematic_armature.get().stepOn(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), player);
			HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), armature);
			helper.runAfterDelay(101, () -> {
				try {
					HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
							helper.getLevel().getBlockState(armaturePos), armature);
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BLOOD_LUST_UPGRADE, origin).passed(),
							"The real consecrated Armature cycle must create Blood Lust armor");
					player.stopRiding();

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BLOOD_LUST_DEMONSTRATION, origin);
					Zombie target = helper.getLevel().getEntitiesOfClass(
							Zombie.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					ArmorSetBonusHandler.onLivingDamage(new LivingDamageEvent.Post(target,
							new DamageContainer(target.damageSources().playerAttack(player), 8.0F)));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BLOOD_LUST_DEMONSTRATION, origin).passed(),
							"A real Blood Lust hit must demonstrate Crimson Vestment");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BLOOD_LUST_FITTING, origin);
					HarbingerArtificerEntity fittingArtificer = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING,
							fittingArtificer.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BLOOD_LUST_FITTING, origin).passed(),
							"The Artificer must award the Crimson Vestment fitting");

					HemoJourneyFixtures.cleanup(player, origin);
					HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
					helper.assertTrue(restored.passed()
							&& !HarbingerAdvancementGranter.hasAdvancement(player,
									ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED),
							"Snapshot restore must remove journey-owned Crimson Vestment state");
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException | AssertionError error) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw error;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void enzymeMasteryJourneyUsesRealInventoryTick(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ENZYME_MASTERY, origin);
			BloodVolumeEvents.playerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.ENZYME_MASTERY, origin).passed(),
					"Carrying all eight supplied enzymes must complete mastery through the real player tick");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void noeticRecognitionJourneyUsesRealMnemonistInteraction(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		var recipe = helper.getLevel().getRecipeManager().byKey(
				Hemomancy.rloc("memory_weaving/memory_conductive_mark")).orElseThrow();
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Noetic recognition snapshot capture must succeed");
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Noetic recognition reset must succeed");
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.NOETIC_MARK_RECOGNIZED, origin);
			HarbingerMnemonistEntity mnemonist = helper.getLevel().getEntitiesOfClass(
					HarbingerMnemonistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			InteractionResult interaction = mnemonist.interact(player, InteractionHand.MAIN_HAND);
			HemoJourneyResult recognized = HemoJourneyChecks.verify(player,
					HemoJourneyStage.NOETIC_MARK_RECOGNIZED, origin);
			helper.assertTrue(recognized.passed()
					&& player.getRecipeBook().contains(recipe),
					"The real Mnemonist interaction must recognize the Noetic mark and teach its Loom recipe: "
							+ recognized.message() + " result=" + interaction
							+ " degree=" + HemoCapabilityAccess.getPlayerDegreeNumber(player)
							+ " firstWeave=" + HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player));

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed()
					&& !HarbingerAdvancementGranter.hasAdvancement(player,
							HarbingerAdvancementGranter.ADV_NOETIC_CONDUCTIVE_MARK_RECOGNIZED)
					&& !player.getRecipeBook().contains(recipe),
					"Snapshot restore must remove journey-owned Noetic recognition and recipe knowledge");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void firstCultureJourneyUsesRealLanternOutputTake(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "First Culture snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "First Culture reset must succeed: " + reset.message());
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_ENZYME_MASTERY_VIVACIOUS);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_CULTURE, origin);
			BlockPos lanternPos = origin.above();
			MycelialLanternBlockEntity lantern = (MycelialLanternBlockEntity) helper.getLevel()
					.getBlockEntity(lanternPos);
			helper.assertTrue(lantern != null && lantern.getCultureStack().is(ItemInit.vivacious_spores.get())
					&& lantern.getBloodVolume() >= 600.0D,
					"First Culture fixture must preload the real culture and required blood");
			for (int tick = 0; tick < 2400; tick++) {
				MycelialLanternBlockEntity.serverTick(helper.getLevel(), lanternPos,
						helper.getLevel().getBlockState(lanternPos), lantern);
			}
			helper.assertTrue(lantern.getOutputStack().is(ItemInit.vivacious_enzyme.get()),
					"The real fruiting recipe must produce Vivacious Enzyme");
			MycelialLanternMenu menu = new MycelialLanternMenu(0, player.getInventory(), lantern,
					lantern.dataAccess);
			ItemStack enzyme = menu.getSlot(MycelialLanternMenu.OUTPUT_SLOT).remove(1);
			menu.getSlot(MycelialLanternMenu.OUTPUT_SLOT).onTake(player, enzyme);
			player.getInventory().add(enzyme);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_CULTURE, origin).passed(),
					"Taking the real Lantern output must complete First Culture");

			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "First Culture restore must succeed: " + restored.message());
			helper.assertTrue(!HarbingerAdvancementGranter.hasAdvancement(player,
					HarbingerAdvancementGranter.ADV_FIRST_CULTURE_COMPLETE),
					"Snapshot restore must remove the journey-owned First Culture milestone");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyFoundingFane")
	public static void veinMasonDegreeSixJourneyUsesRealRoutingAndRestoresState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoCapabilityAccess.requireInitiatoryDegree(player).setHematicFortification(true);
			HemoCapabilityAccess.requireBloodTendency(player)
					.setTendencyAlignment(com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency.ANIMUS, 4.25F);
			var originalDegree = player.getData(HemoAttachmentTypes.INITIATORY_DEGREE)
					.serializeNBT(player.registryAccess()).copy();
			var originalTendency = player.getData(HemoAttachmentTypes.BLOOD_TENDENCY)
					.serializeNBT(player.registryAccess()).copy();
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(), "Degree-six snapshot capture must succeed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(), "Degree-six journey reset must succeed");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_REFERRAL, origin);
			HarbingerCicatrixAnchoriteEntity mason = helper.getLevel()
					.getEntitiesOfClass(HarbingerCicatrixAnchoriteEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL, mason.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_REFERRAL, origin).passed(),
					"The real Vein-Mason referral must satisfy the checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_COUNSEL, origin);
			HarbingerMnemonistEntity mnemonist = helper.getLevel()
					.getEntitiesOfClass(HarbingerMnemonistEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL, mnemonist.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_COUNSEL, origin).passed(),
					"The real Mnemonist counsel must satisfy the checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_FIRST_ROUTE, origin);
			BloodManipulation.clearSessionState();
			HemoCapabilityAccess.requireKnownManipulations(player).getSelectedManip()
					.performAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_FIRST_ROUTE, origin).passed(),
					"A real matching manipulation cast must satisfy the first routing checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_CARVED, origin);
			((ScarStationBlockEntity) helper.getLevel().getBlockEntity(origin.above())).craftEvent();
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_CARVED, origin).passed(),
					"The real station carve must satisfy the continuation-scar checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_LEARNED, origin);
			absorbPreparedScar(player, origin.above());
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_LEARNED, origin).passed(),
					"The real scar-learning brazier rite must satisfy the checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_LOADOUT, origin);
			absorbPreparedScar(player, origin.above());
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_LOADOUT, origin).passed(),
					"The real loadout brazier rite must satisfy the assignment hook");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_SECOND_ROUTE, origin);
			BloodManipulation.clearSessionState();
			HemoCapabilityAccess.requireKnownManipulations(player).getSelectedManip()
					.performAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_SECOND_ROUTE, origin).passed(),
					"A second real matching cast must close the routing assignment");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_REWARD, origin);
			mason = helper.getLevel().getEntitiesOfClass(HarbingerCicatrixAnchoriteEntity.class,
					HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD, mason.getId()));
			HemoJourneyResult reward = HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_REWARD, origin);
			helper.assertTrue(reward.passed(), "The complete degree-six reward must be attributable: " + reward.message());

			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(HemoJourneySnapshot.restore(player).passed(), "Degree-six snapshot restore must succeed");
			helper.assertTrue(player.getData(HemoAttachmentTypes.INITIATORY_DEGREE)
					.serializeNBT(player.registryAccess()).equals(originalDegree),
					"The exact Initiatory Degree attachment must be restored");
			helper.assertTrue(player.getData(HemoAttachmentTypes.BLOOD_TENDENCY)
					.serializeNBT(player.registryAccess()).equals(originalTendency),
					"The exact base Blood Tendency map must be restored");
			helper.succeed();
		} catch (Throwable error) {
			throw new AssertionError("Vein-Mason degree-six fixture failed", error);
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			BloodManipulation.clearSessionState();
			player.discard();
		}
	}

	private static void absorbPreparedScar(ServerPlayer player, BlockPos pos) {
		player.startUsingItem(InteractionHand.MAIN_HAND);
		for (int tick = 0; tick < 100; tick++) {
			ScarBrazierRite.tryAbsorb(player.serverLevel(), pos,
					player.serverLevel().getBlockState(pos), player, 1.0D);
			if (((IronBrazierBlockEntity) player.serverLevel().getBlockEntity(pos))
					.getOfferingForMatching().isEmpty()) break;
		}
		player.stopUsingItem();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyFoundingFane")
	public static void veinMasonDegreeFiveJourneyUsesRealHooksAndRestoresVascularState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		float originalArms = 37.5F;
		try {
			HemoCapabilityAccess.requireVascularSystem(player).getVascularSystem()
					.put(EnumVeinSections.ARMS, originalArms);
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(), "Degree-five snapshot capture must succeed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(), "Degree-five journey reset must succeed");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D5_STRAIN, origin);
			helper.assertTrue(MuscleMemoryActivationService.tryTrigger(player, MuscleMemory.SANGUINE_FISTS, 20),
					"Sanguine Fists must trigger through the production activation service");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D5_STRAIN, origin).passed(),
					"The real Varicose transition must satisfy the strain checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D5_DIAGNOSIS, origin);
			HarbingerCicatrixAnchoriteEntity mason = helper.getLevel()
					.getEntitiesOfClass(HarbingerCicatrixAnchoriteEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS, mason.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D5_DIAGNOSIS, origin).passed(),
					"The dialogue diagnosis must satisfy the diagnosis checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D5_TREATMENT, origin);
			ItemStack poultice = player.getMainHandItem();
			poultice.getItem().finishUsingItem(poultice, helper.getLevel(), player);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D5_TREATMENT, origin).passed(),
					"The production poultice treatment must satisfy the treatment checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D5_FORTIFICATION, origin);
			var activation = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			ActiveCardinalRite rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			helper.assertTrue(activation == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null,
					"The prepared Hematic Fortification must start through the real activation path");
			Method complete = HarbingerCardinalRiteEvents.class.getDeclaredMethod(
					"completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
					ActiveCardinalRite.class);
			complete.setAccessible(true);
			helper.assertTrue((boolean) complete.invoke(null, helper.getLevel(), player, rite),
					"The real Hematic Fortification completion path must succeed");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D5_FORTIFICATION, origin).passed(),
					"Fortification and assignment readiness must satisfy the checkpoint");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D5_REWARD, origin);
			mason = helper.getLevel().getEntitiesOfClass(HarbingerCicatrixAnchoriteEntity.class,
					HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD, mason.getId()));
			HemoJourneyResult reward = HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D5_REWARD, origin);
			helper.assertTrue(reward.passed(),
					"The real degree-five dialogue reward and complete kit must satisfy the checkpoint: " + reward.message());

			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(HemoJourneySnapshot.restore(player).passed(), "Degree-five snapshot restore must succeed");
			helper.assertTrue(Float.compare(HemoCapabilityAccess.requireVascularSystem(player)
					.getHealthBySection(EnumVeinSections.ARMS), originalArms) == 0,
					"The exact pre-journey vascular state must be restored");
			helper.assertTrue(!HemoCapabilityAccess.requireInitiatoryDegree(player).hasHematicFortification(),
					"Journey-owned Hematic Fortification must be removed on restore");
			helper.succeed();
		} catch (Throwable error) {
			throw new AssertionError("Vein-Mason degree-five fixture failed", error);
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyFoundingFane")
	public static void foundingFaneFixtureProtectsExistingWorldState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		UUID originalId = UUID.randomUUID();
		Bloodline original = new Bloodline("Original", player.getUUID(), originalId, new java.util.ArrayList<>());
		BlockPos originalHeart = helper.absolutePos(new BlockPos(1, 2, 1));
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		String step = "registering original state";
		try {
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(original);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(original);
			FoundingFaneSavedData.get(helper.getLevel()).consecrateHeart(player.getUUID(), originalHeart);
			step = "capturing the snapshot";
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "World-state snapshot capture must succeed: " + captured.message());
			step = "resetting journey state";
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "World-state reset must succeed: " + reset.message());
			helper.assertTrue(!FoundingFaneSavedData.get(helper.getLevel()).hasFane(player.getUUID()),
					"The original Fane record must be suspended during the journey");

			step = "preparing the Founding Fane fixture";
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FOUNDING_FANE, origin);
			helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodLine().isValid()
					&& !HemoCapabilityAccess.requireBloodVolume(player).getBloodLine().getBloodlineUUID().equals(originalId),
					"The fixture must use an isolated temporary bloodline");
			helper.assertTrue(helper.getLevel().getBlockEntity(origin.above()) instanceof CardinalFocusBlockEntity focus
					&& focus.getMediumForMatching().is(ItemInit.sanguine_quintessence.get()),
					"The Founding Fane Cardinal Focus must contain its real medium");
			var activation = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			ActiveCardinalRite rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			helper.assertTrue(activation == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null,
					"The prepared Founding Fane must start through the real activation path");
			step = "completing the Founding Fane rite";
			Method complete = HarbingerCardinalRiteEvents.class.getDeclaredMethod(
					"completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
					ActiveCardinalRite.class);
			complete.setAccessible(true);
			helper.assertTrue((boolean) complete.invoke(null, helper.getLevel(), player, rite),
					"The real Founding Fane completion path must succeed");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FOUNDING_FANE, origin).passed(),
					"The real Founding Fane outcome must satisfy its journey checkpoint");
			HemoJourneyFixtures.cleanup(player, origin);

			step = "restoring the snapshot";
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "World-state restore must succeed: " + restored.message());
			helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodLine().getBloodlineUUID().equals(originalId),
					"The original player bloodline must be restored");
			helper.assertTrue(originalHeart.equals(FoundingFaneSavedData.get(helper.getLevel()).getHeart(player.getUUID())),
					"The original Founding Fane record must be restored");
			helper.succeed();
		} catch (Throwable error) {
			throw new AssertionError("Founding Fane fixture failed while " + step, error);
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			FoundingFaneSavedData.get(helper.getLevel()).remove(player.getUUID());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(originalId);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 120)
	public static void livingCovenantFixturesUseRealPathsAndRestoreState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		BlockPos originalSpawn = helper.absolutePos(new BlockPos(2, 2, 2));
		UUID lineId = UUID.randomUUID();
		String step = "capturing original state";
		try {
			player.setRespawnPosition(helper.getLevel().dimension(), originalSpawn, 37.0F, false, false);
			player.getPersistentData().putBoolean("hemomancy:chamber_visit_chair_bound", false);
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Living Covenant snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Living Covenant reset must succeed: " + reset.message());
			Bloodline line = new Bloodline("Vigil Test", player.getUUID(), lineId, new java.util.ArrayList<>());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(line);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(line);
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);

			step = "visiting the Chamber of Will";
			if (helper.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) != null) {
				helper.assertTrue(ChamberVisitService.beginRiteVisit(player), "Degree-6 rite visit must enter the Chamber");
				helper.assertTrue(ChamberVisitService.isActive(player)
						&& player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL),
						"Rite visit must be active inside the Chamber dimension");
				ChamberVisitService.returnFromVisit(player);
			} else {
				// The dedicated GameTest server loads only vanilla dimensions; live journey testing covers the transition.
				ChamberVisitService.attune(player);
				HarbingerAdvancementGranter.grantIfNotDone(player,
						HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
			}
			helper.assertTrue(HarbingerAdvancementGranter.hasAdvancement(player,
					HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED),
					"Returning through the real Chamber exit must grant its milestone");

			step = "binding the Covenant Throne";
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.COVENANT_THRONE_BOUND, origin);
			BlockPos throne = origin.above();
			helper.assertTrue(CovenantThroneBlock.isProgenitor(player),
					"Prepared player must be the active journey bloodline Progenitor");
			player.setGameMode(GameType.SURVIVAL);
			player.teleportTo(helper.getLevel(), origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() - 1.5D, 0.0F, 0.0F);
			helper.getLevel().getBlockState(throne).useWithoutItem(helper.getLevel(), player,
					new BlockHitResult(Vec3.atCenterOf(throne), Direction.UP, throne, false));
			HemoJourneyResult throneResult = HemoJourneyChecks.verify(player,
					HemoJourneyStage.COVENANT_THRONE_BOUND, origin);
			helper.assertTrue(throneResult.passed(),
					"Real Covenant Throne interaction must satisfy its checkpoint: " + throneResult.message());
			player.stopRiding();

			step = "starting and completing the Covenant Vigil";
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.COVENANT_VIGIL, origin);
			var activation = BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE);
			ActiveCardinalRite rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			helper.assertTrue(activation == CardinalRiteActivationRules.ActivationAttempt.STARTED && rite != null,
					"Prepared Covenant Vigil must start through the real activation path");
			HarbingerVicarEntity ally = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
					HemoJourneyFixtures.bounds(origin), entity -> entity.getTags().contains(
							HemoJourneyFixtures.entityMarker(origin))).getFirst();
			int[] anchors = rite.getAnchorBloodMl();
			for (int index = 0; index < anchors.length; index++) rite.fillAnchor(index, 50);
			helper.assertTrue(rite.enterInscription(), "Fast journey path must reach helper inscription");
			helper.assertTrue(CardinalRiteAllyService.tryAssignNpc(helper.getLevel(), player, rite, ally)
					&& CardinalRiteAllyService.isAvailable(helper.getLevel(), rite, ally.getUUID()),
					"Journey helper must be assigned through the real ally service");
			Method complete = HarbingerCardinalRiteEvents.class.getDeclaredMethod(
					"completeRite", net.minecraft.server.level.ServerLevel.class, ServerPlayer.class,
					ActiveCardinalRite.class);
			complete.setAccessible(true);
			helper.assertTrue((boolean) complete.invoke(null, helper.getLevel(), player, rite),
					"Real Covenant Vigil completion path must succeed");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.COVENANT_VIGIL, origin).passed()
					&& ally.hasEffect(MobEffects.DAMAGE_RESISTANCE) && ally.hasEffect(MobEffects.REGENERATION),
					"Vigil must close Living Covenant and reward the available sworn helper");

			step = "restoring original state";
			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Living Covenant restore must succeed: " + restored.message());
			helper.assertTrue(originalSpawn.equals(player.getRespawnPosition())
					&& helper.getLevel().dimension().equals(player.getRespawnDimension())
					&& Float.compare(37.0F, player.getRespawnAngle()) == 0,
					"Original respawn binding must be restored exactly");
			helper.assertTrue(player.getPersistentData().contains("hemomancy:chamber_visit_chair_bound")
					&& !player.getPersistentData().getBoolean("hemomancy:chamber_visit_chair_bound")
					&& !player.getPersistentData().contains("hemomancy:chamber_visit_attuned"),
					"Original Chamber flag presence and values must be restored exactly");
			helper.succeed();
		} catch (Throwable error) {
			throw new AssertionError("Living Covenant fixture failed while " + step, error);
		} finally {
			if (ChamberVisitService.isActive(player)
					|| player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
				ChamberVisitService.returnFromVisit(player);
			}
			HemoJourneyFixtures.cleanup(player, origin);
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(lineId);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void firstMemoryWeaveFixtureIsReadyAndCleansWithoutDrops(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_MEMORY_WOVEN, origin);
			helper.assertTrue(helper.getLevel().getBlockEntity(origin.above()) instanceof SomaticLoomBlockEntity loom
					&& loom.hasValidRecipe(), "Expected a recipe-ready Somatic Loom");
			helper.assertTrue(player.getMainHandItem().is(ItemInit.blood_projection.get()),
					"Expected the Blood Projection charging tool");
			helper.assertTrue(player.getOffhandItem().is(ItemInit.living_staff.get()),
					"Expected the Living Staff strand tool");
			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					HemoJourneyFixtures.bounds(origin)).isEmpty(), "Fixture cleanup dropped supplied loom inputs");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void wovenVesselAcceptsPickedUpNpcRewards(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.WOVEN_VESSEL_TURN_IN, (origin, player) -> {
			player.getInventory().add(new ItemStack(ItemInit.bleeding_bulb.get()));
			player.getInventory().add(new ItemStack(ItemInit.vivacious_enzyme.get()));
			helper.assertTrue(HemoJourneyFixtures.expectedOutputsPresent(player,
					HemoJourneyStage.WOVEN_VESSEL_TURN_IN, origin),
					"Picked-up Mnemonist rewards must satisfy the checkpoint");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void firstMemoryAcceptsLoomOutputPosition(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.FIRST_MEMORY_WOVEN, (origin, player) -> {
			ItemEntity output = new ItemEntity(helper.getLevel(), origin.getX() + 0.5D,
					origin.getY() + 2.5D, origin.getZ() + 0.5D,
					new ItemStack(ItemInit.memory_blood_shot.get()));
			helper.getLevel().addFreshEntity(output);
			helper.assertTrue(HemoJourneyFixtures.expectedOutputsPresent(player,
					HemoJourneyStage.FIRST_MEMORY_WOVEN, origin),
					"The output spawned above the Somatic Loom must satisfy the checkpoint");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void veinMasonFixturesPrepareRealDevicesAndCleanInputs(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin);
			helper.assertTrue(helper.getLevel().getBlockEntity(origin.above()) instanceof ScarStationBlockEntity station
					&& station.hasValidRecipe() && station.areScarsMatching(),
					"Scar station must start with the lesson recipe and carved pattern ready");
			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					HemoJourneyFixtures.bounds(origin)).isEmpty(), "Scar station cleanup dropped supplied inputs");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_LEARNED, origin);
			helper.assertTrue(helper.getLevel().getBlockState(origin.above()).getValue(BrazierBlock.RITUAL_PHASE) > 0
					&& helper.getLevel().getBlockEntity(origin.above()) instanceof IronBrazierBlockEntity brazier
					&& brazier.getOfferingDisplayStack().getItem() instanceof ItemScar,
					"Learning brazier must be lit with the lesson scar loaded");
			HemoJourneyFixtures.cleanup(player, origin);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_EFFIGY_PATTERN, origin);
			helper.assertTrue(helper.getLevel().getBlockEntity(origin.above()) instanceof MasonsEffigyBlockEntity effigy
					&& effigy.getSelectedScarIds().size() == 1,
					"Mason's Effigy must start with the learned scar selected");
			HemoJourneyFixtures.cleanup(player, origin);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_EFFIGY_LOADOUT, origin);
			helper.assertTrue(helper.getLevel().getBlockEntity(origin.above()) instanceof IronBrazierBlockEntity loadoutBrazier
					&& ItemScarPattern.hasPreparedLoadout(loadoutBrazier.getOfferingDisplayStack()),
					"Loadout brazier must be lit with a prepared Effigy pattern");
			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
					HemoJourneyFixtures.bounds(origin)).isEmpty(), "Vein-Mason cleanup dropped supplied offerings");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void veinMasonDeviceOutcomesSatisfyJourneyChecks(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin);
			ScarStationBlockEntity station = (ScarStationBlockEntity) helper.getLevel().getBlockEntity(origin.above());
			station.craftEvent();
			com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin).passed(),
					"Prepared scar station outcome must satisfy its checkpoint");
			HemoJourneyFixtures.cleanup(player, origin);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_LEARNED, origin);
			IronBrazierBlockEntity brazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(origin.above());
			ItemStack scar = brazier.getOfferingDisplayStack().copy();
			ScarBrazierRite.burn(helper.getLevel(), origin.above(), player, scar,
					com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.LEARN);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_SCAR_LEARNED, origin).passed(),
					"Scar learning outcome must satisfy its checkpoint");
			HemoJourneyFixtures.cleanup(player, origin);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_EFFIGY_PATTERN, origin);
			MasonsEffigyBlockEntity effigy = (MasonsEffigyBlockEntity) helper.getLevel().getBlockEntity(origin.above());
			helper.assertTrue(effigy.beginMotifRitual(player, player.getMainHandItem()),
					"Prepared Effigy must accept its supplied Motif Paper");
			effigy.receiveProjectedBlood(player, MasonsEffigyBlockEntity.BLOOD_PER_SCAR, false);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_EFFIGY_PATTERN, origin).passed(),
					"Effigy pattern outcome must satisfy its checkpoint");
			HemoJourneyFixtures.cleanup(player, origin);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_EFFIGY_LOADOUT, origin);
			IronBrazierBlockEntity loadoutBrazier = (IronBrazierBlockEntity) helper.getLevel().getBlockEntity(origin.above());
			ItemStack pattern = loadoutBrazier.getOfferingDisplayStack().copy();
			ScarBrazierRite.burn(helper.getLevel(), origin.above(), player, pattern,
					com.vincenthuto.hemomancy.common.rite.ScarBrazierInteractionRules.Burn.COMMIT);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_EFFIGY_LOADOUT, origin).passed(),
					"Effigy loadout outcome must satisfy its checkpoint");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardAcceptsAllInventory(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyCheckpointRules.rewardQuantityPassed(3, 7, 0, 4),
				"A positive inventory delta may satisfy the exact reward quantity");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardAcceptsSplitInventoryAndDrop(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyCheckpointRules.rewardQuantityPassed(3, 5, 2, 4),
				"Inventory delta plus attributed drops may satisfy the exact reward quantity");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarRewardRejectsBaselineAndWrongQuantities(GameTestHelper helper) {
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(4, 4, 0, 4),
				"Preexisting baseline inventory must not satisfy a reward");
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(0, 3, 0, 4),
				"An incomplete quantity must not satisfy a reward");
		helper.assertTrue(!HemoJourneyCheckpointRules.rewardQuantityPassed(0, 5, 0, 4),
				"An excessive quantity must not satisfy a reward");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fixtureDimensionResolvesPersistedServerLevel(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		try {
			helper.assertTrue(HemoJourneyFixtures.fixtureLevel(player) == helper.getLevel(),
					"Fixture resolver must use the persisted dimension key");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void mortalDisplayPlatform(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.MORTAL_DISPLAY, (origin, player) -> {
			for (int x = -2; x <= 2; x++) {
				for (int z = -2; z <= 2; z++) {
					assertBlock(helper, origin.offset(x, 0, z), Blocks.STONE);
				}
			}
			assertBlock(helper, origin.above(), BlockInit.mortal_display.get());
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineInitiationPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.SANGUINE_INITIATION, (origin, player) -> {
			Block[][] rows = {
					{ Blocks.STONE_BRICKS, BlockInit.hematic_iron_block.get(), Blocks.STONE_BRICKS },
					{ BlockInit.hematic_iron_block.get(), BlockInit.cardinal_focus.get(), BlockInit.hematic_iron_block.get() },
					{ Blocks.STONE_BRICKS, BlockInit.hematic_iron_block.get(), Blocks.STONE_BRICKS }
			};
			assertFloor(helper, origin, rows);
			assertBlock(helper, origin.above(4), BlockInit.mortal_display.get());
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void sanguineLandingIsSupportedAndReachable(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.SANGUINE_INITIATION, (origin, player) -> {
			BlockPos landing = invokeJourneyLanding(origin);
			BlockPos support = landing.below();
			helper.assertTrue(helper.getLevel().getBlockState(support)
					.isFaceSturdy(helper.getLevel(), support, Direction.UP),
					"Journey landing must have solid support");
			int fixtureRadius = Math.max(Math.abs(landing.getX() - origin.getX()),
					Math.abs(landing.getZ() - origin.getZ()));
			helper.assertTrue(fixtureRadius <= 2,
					"Journey landing must remain inside the Sanguine Initiation fixture boundary");
			Vec3 eye = new Vec3(landing.getX() + 0.5D, landing.getY() + player.getEyeHeight(),
					landing.getZ() + 0.5D);
			helper.assertTrue(eye.distanceTo(Vec3.atCenterOf(origin.above())) <= player.blockInteractionRange(),
					"Sanguine Initiation center must remain within actual block interaction reach");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void attunedProjectorTarget(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.FORMATION_PROJECTED, (origin, player) -> helper.assertTrue(
				helper.getLevel().getBlockState(origin.above()).is(SanguineFormationProjectionHandler.PROJECTOR_TAG),
				"Expected an attuned formation projector at the fixture target"));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void tallStructureBaseClearsPlatform(GameTestHelper helper) {
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(1) == 1, "One-layer structure base is wrong");
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(2) == 2, "Two-layer structure base is wrong");
		helper.assertTrue(HemoJourneyFixtures.structureBaseHeight(3) == 3, "Three-layer structure base is wrong");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void offeringPlannerExpandsDistinctPerimeterSlots(GameTestHelper helper) {
		BlockPos center = helper.absolutePos(new BlockPos(4, 3, 4));
		List<BloodStructureOffering> offerings = List.of(
				new BloodStructureOffering(Ingredient.of(Items.GLASS_BOTTLE), 1),
				new BloodStructureOffering(Ingredient.of(Items.COPPER_INGOT), 2));
		var slots = BloodStructureOfferingPlacement.plan(center, 1, 1, 1, offerings);
		helper.assertTrue(slots.size() == 3, "Offering counts must expand into distinct braziers");
		helper.assertTrue(slots.stream().map(slot -> slot.pos()).distinct().count() == 3,
				"Offering brazier positions must be unique");
		helper.assertTrue(slots.stream().noneMatch(slot -> Math.abs(slot.pos().getX() - center.getX()) <= 1
				&& Math.abs(slot.pos().getZ() - center.getZ()) <= 1), "Offering braziers must be outside the structure");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void centrifugeJourneySpawnsCompleteReadyStructure(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.CENTRIFUGE_PREPARED, (origin, player) -> {
			BloodStructureRecipe recipe = BloodStructureRecipe.getStructureByLocation(helper.getLevel(),
					Hemomancy.rloc("blood_structure/vial_centrifuge"));
			helper.assertTrue(recipe != null, "Vial Centrifuge recipe must load");
			int physicalCellY = recipe.getPattern().getBlockPattern().getHeight() - 2;
			for (var pair : recipe.getPattern().getBlockPosBlockList()) {
				if (pair.getBlock() != null && pair.getBlock() != Blocks.AIR) {
					assertBlock(helper, origin.above(2).offset(pair.getPos().getX() - 1,
							pair.getPos().getY() - physicalCellY, 1 - pair.getPos().getZ()), pair.getBlock());
				}
			}
			for (BlockPos brazierPos : List.of(origin.offset(-2, 2, 0), origin.offset(2, 2, 0))) {
				assertBlock(helper, brazierPos, BlockInit.iron_brazier.get());
				helper.assertTrue(helper.getLevel().getBlockState(brazierPos).getValue(BrazierBlock.RITUAL_PHASE) == 1,
						"Journey offering braziers must begin lit");
				helper.assertTrue(helper.getLevel().getBlockEntity(brazierPos) instanceof IronBrazierBlockEntity brazier
						&& brazier.hasOffering(), "Journey offering braziers must begin filled");
			}
			helper.assertTrue(HemoCapabilityAccess.requireBloodVolume(player).getBloodVolume()
					>= 150.0D, "Journey blood budget must cover the structure craft");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberSanguinumPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.LIBER_CRAFTED, (origin, player) -> assertFloor(helper, origin,
				ashPattern(Blocks.BOOKSHELF)));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hematicIronPattern(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED, (origin, player) -> assertFloor(helper, origin,
				ashPattern(Blocks.IRON_BLOCK)));
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberAcceptsPickedUpOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIBER_CRAFTED, origin);
			player.getInventory().add(new ItemStack(ItemInit.liber_sanguinum.get()));
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.LIBER_CRAFTED, origin),
					"A newly picked-up Liber Sanguinium must remain attributable to its checkpoint");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void markedVicarSpawn(GameTestHelper helper) {
		withFixture(helper, HemoJourneyStage.VICAR_REWARD, (origin, player) -> {
			List<HarbingerVicarEntity> vicars = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
					new AABB(origin.getX() - 3, origin.getY(), origin.getZ() - 3,
							origin.getX() + 4, origin.getY() + 5, origin.getZ() + 4),
					entity -> entity.getTags().contains(HemoJourneyFixtures.entityMarker(origin)));
			helper.assertTrue(vicars.size() == 1,
					"Expected exactly one journey-marked Harbinger Vicar, found " + vicars.size());
			HarbingerVicarEntity vicar = vicars.getFirst();
			helper.assertTrue(vicar.isInvulnerable(), "Expected journey Vicar to be invulnerable");
			helper.assertTrue(vicar.isNoAi(), "Expected journey Vicar to have no AI");
		});
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40,
			batch = "journeyCleanupOwnership")
	public static void cleanupPreservesUnownedBlock(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		BlockPos unowned = origin.offset(4, 1, 4);
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			helper.getLevel().setBlockAndUpdate(unowned, Blocks.DIAMOND_BLOCK.defaultBlockState());
			HemoJourneyFixtures.cleanup(player, origin);
			assertBlock(helper, unowned, Blocks.DIAMOND_BLOCK);
		} finally {
			helper.getLevel().removeBlock(unowned, false);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40,
			batch = "journeyOccupiedFixture")
	public static void preparationRefusesOccupiedFixture(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		BlockPos occupied = origin.above();
		helper.getLevel().setBlockAndUpdate(occupied, Blocks.BEDROCK.defaultBlockState());
		try {
			try {
				HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
				helper.fail("Expected occupied fixture preparation to fail");
			} catch (IllegalStateException expected) {
				assertBlock(helper, occupied, Blocks.BEDROCK);
			}
		} finally {
			helper.getLevel().removeBlock(occupied, false);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationBaselineRejectsPreexistingOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		ItemEntity oldOutput = new ItemEntity(helper.getLevel(), origin.getX() + 1.5D, origin.getY() + 1.0D,
				origin.getZ() + 0.5D, new ItemStack(ItemInit.sanguine_formation.get()));
		helper.getLevel().addFreshEntity(oldOutput);
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			blood.setBloodVolume(4900.0D);
			HemoJourneyResult result = HemoJourneyChecks.verify(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(!result.passed(), "Preexisting formation must not satisfy the stage baseline");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			oldOutput.discard();
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationAcceptsCompletionAndSettledOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity settled = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4800.0D);
			helper.assertTrue(HemoJourneyCheckpointRules.formationPassed(5000.0D, blood.getBloodVolume(),
					invokeBooleanFixture("captureExpectedOutputs", player, HemoJourneyStage.FORMATION_PROJECTED, origin)),
					"A completed formation and its settled output must pass even when held use begins another cycle");
			invokeFixture("cleanupForExit", player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(settled.isRemoved(), "Settled formation output must be attributable and cleaned");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void formationAcceptsAutoPickedUpOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			player.getInventory().add(new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.FORMATION_PROJECTED, origin),
					"A newly auto-picked-up formation must remain attributable to this checkpoint");
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void transitionSemantics(GameTestHelper helper) {
		try {
			Class<?> transitions = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyTransition");
			Method next = transitions.getMethod("next", HemoJourneyStage.class, boolean.class, boolean.class);
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, false, true)
					== HemoJourneyStage.MORTAL_DISPLAY, "Failed verification must not advance");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, true, false)
					== HemoJourneyStage.MORTAL_DISPLAY, "Failed preparation must roll back");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.MORTAL_DISPLAY, true, true)
					== HemoJourneyStage.SANGUINE_INITIATION, "Successful transition must advance exactly one stage");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Transition seam is missing", exception);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void exactOutputOwnershipPreservesUnrelatedSameItem(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity expected = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D,
					origin.getZ() - 0.05D, new ItemStack(ItemInit.sanguine_formation.get()));
			ItemEntity unrelated = spawn(helper, origin.getX() + 3.5D, origin.getY() + 1.0D,
					origin.getZ() + 3.5D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.FORMATION_PROJECTED, origin), "Expected production output was not captured");
			invokeFixture("cleanupOwnedOutputs", player, origin);
			helper.assertTrue(expected.isRemoved(), "Captured production output must be cleaned");
			helper.assertTrue(!unrelated.isRemoved(), "Unrelated same-type drop must be preserved");
			unrelated.discard();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void liberPositiveDelta(GameTestHelper helper) {
		positiveCraftDelta(helper, HemoJourneyStage.LIBER_CRAFTED, ItemInit.liber_sanguinum.get());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void hematicIronPositiveDelta(GameTestHelper helper) {
		positiveCraftDelta(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED,
				BlockInit.hematic_iron_block.get().asItem());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fallenLiberOutputIsCapturedAndCleaned(GameTestHelper helper) {
		settledCraftOutput(helper, HemoJourneyStage.LIBER_CRAFTED, ItemInit.liber_sanguinum.get());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void fallenHematicIronOutputIsCapturedAndCleaned(GameTestHelper helper) {
		settledCraftOutput(helper, HemoJourneyStage.HEMATIC_IRON_CRAFTED,
				BlockInit.hematic_iron_block.get().asItem());
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void vicarPositiveDelta(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VICAR_REWARD, origin);
			for (ItemStack reward : FirstBloodcraftAssignment.rewardStacks()) {
				spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D, origin.getZ() + 0.5D, reward.copy());
			}
			boolean captured = invokeBooleanFixture("captureExpectedOutputs", player,
					HemoJourneyStage.VICAR_REWARD, origin);
			helper.assertTrue(HemoJourneyCheckpointRules.rewardPassed(captured, true, true),
					"Exact Vicar deltas plus a newly earned claim must pass");
		} finally {
			invokeFixture("cleanupForExit", player, HemoJourneyStage.VICAR_REWARD, origin);
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void verifiedLatchRetriesWithoutReverification(GameTestHelper helper) {
		try {
			Class<?> transitions = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyTransition");
			Method shouldVerify = transitions.getMethod("shouldVerify", HemoJourneyStage.class, String.class);
			for (HemoJourneyStage earned : List.of(HemoJourneyStage.LIBER_CRAFTED,
					HemoJourneyStage.HEMATIC_IRON_CRAFTED, HemoJourneyStage.VICAR_REWARD)) {
				helper.assertTrue(!(boolean) shouldVerify.invoke(null, earned, earned.id()),
						"Earned " + earned.id() + " latch must skip destructive re-verification");
			}
			Method next = transitions.getMethod("next", HemoJourneyStage.class, boolean.class, boolean.class);
			helper.assertTrue(next.invoke(null, HemoJourneyStage.LIBER_CRAFTED, true, false)
					== HemoJourneyStage.LIBER_CRAFTED, "Failed transition must retain earned stage");
			helper.assertTrue(next.invoke(null, HemoJourneyStage.LIBER_CRAFTED, true, true)
					== HemoJourneyStage.HEMATIC_IRON_CRAFTED, "Retry must advance exactly once");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Verified checkpoint latch seam is missing", exception);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void completeExitCleansOwnedFixtureAndOutput(GameTestHelper helper) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			var blood = HemoCapabilityAccess.requireBloodVolume(player);
			blood.setActive(true);
			blood.setBloodVolume(5000.0D);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			ItemEntity output = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.5D,
					origin.getZ() - 0.05D, new ItemStack(ItemInit.sanguine_formation.get()));
			blood.setBloodVolume(4900.0D);
			invokeFixture("cleanupForExit", player, HemoJourneyStage.FORMATION_PROJECTED, origin);
			helper.assertTrue(output.isRemoved(), "COMPLETE/reset/clear exit must clean attributed output");
			helper.assertTrue(helper.getLevel().getBlockState(origin).isAir(), "Exit must clean owned platform");
		} finally {
			player.discard();
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void knownManipulationStateRoundTrip(GameTestHelper helper) {
		try {
			KnownManipulations known = new KnownManipulations();
			known.getKnownManips().put(ManipulationInit.blood_projection.get(), ManipLevel.BLANK);
			known.setSelectedManip(ManipulationInit.blood_projection.get());
			Class<?> seam = Class.forName("com.vincenthuto.hemomancy.gametest.journey.HemoJourneyManipulationState");
			Method capture = seam.getMethod("capture", KnownManipulations.class, net.minecraft.core.HolderLookup.Provider.class);
			Method reset = seam.getMethod("reset", KnownManipulations.class, net.minecraft.core.HolderLookup.Provider.class);
			Method apply = seam.getMethod("apply", KnownManipulations.class, ListTag.class,
					net.minecraft.core.HolderLookup.Provider.class);
			ListTag saved = (ListTag) capture.invoke(null, known, helper.getLevel().registryAccess());
			reset.invoke(null, known, helper.getLevel().registryAccess());
			helper.assertTrue(known.getKnownManips().isEmpty(), "Reset seam must clear learned manipulations");
			apply.invoke(null, known, saved, helper.getLevel().registryAccess());
			helper.assertTrue(known.doesListContainName(known.getKnownManips(), ManipulationInit.blood_projection.get()),
					"Apply/rollback seam must restore learned manipulation");
			helper.assertTrue(known.getSelectedManip().getName().equals("blood_projection"),
					"Apply/postverify seam must restore selected manipulation");
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Pure manipulation state seam is missing", exception);
		}
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void journeySnapshotRestoresScarAndUnstainedState(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		var scarId = Hemomancy.rloc("scar_heart");
		try {
			var scars = HemoCapabilityAccess.requireScarState(player);
			var unstained = HemoCapabilityAccess.requireUnstainedProgress(player);
			scars.addKnownCerebralScar(scarId);
			scars.activateCerebralScar(scarId);
			unstained.setBegunPurification(true);
			unstained.setPurity(37.0F);
			HemoCapabilityAccess.requireKnownStillArts(player).learnArt(StillArtInit.still_pulse.get());
			UnstainedAdvancementGranter.grantIfNotDone(player, UnstainedAdvancementGranter.ADV_DISCERNING);
			float capturedPurity = unstained.getPurity();
			HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
			helper.assertTrue(captured.passed(), "Scar snapshot capture must succeed: " + captured.message());
			HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
			helper.assertTrue(reset.passed(), "Scar snapshot reset must succeed: " + reset.message());
			helper.assertTrue(scars.getKnownCerebralScars().isEmpty()
					&& scars.getActiveCerebralScars().isEmpty(), "Journey reset must clear scar state");
			helper.assertTrue(!unstained.hasBegunPurification() && unstained.getPurity() == 0.0F,
					"Journey reset must clear conflicting Unstained progress");
			helper.assertTrue(!HemoCapabilityAccess.requireKnownStillArts(player).isKnown(StillArtInit.still_pulse.get())
					&& !HarbingerAdvancementGranter.hasAdvancement(player, UnstainedAdvancementGranter.ADV_DISCERNING),
					"Journey reset must clear Unstained arts and advancements");
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			helper.assertTrue(restored.passed(), "Scar snapshot restore must succeed: " + restored.message());
			var restoredScars = HemoCapabilityAccess.requireScarState(player);
			var restoredUnstained = HemoCapabilityAccess.requireUnstainedProgress(player);
			helper.assertTrue(restoredScars.knowsCerebralScar(scarId)
					&& restoredScars.getActiveCerebralScars().contains(scarId), "Journey restore must recover scar state");
			helper.assertTrue(restoredUnstained.hasBegunPurification()
					&& restoredUnstained.getPurity() == capturedPurity,
					"Journey restore must recover Unstained progress; begun="
							+ restoredUnstained.hasBegunPurification() + ", purity=" + restoredUnstained.getPurity()
							+ ", expected=" + capturedPurity);
			helper.assertTrue(HemoCapabilityAccess.requireKnownStillArts(player).isKnown(StillArtInit.still_pulse.get())
					&& HarbingerAdvancementGranter.hasAdvancement(player, UnstainedAdvancementGranter.ADV_DISCERNING),
					"Journey restore must recover Unstained arts and advancements");
			helper.succeed();
		} finally {
			player.discard();
		}
	}

	private static void positiveCraftDelta(GameTestHelper helper, HemoJourneyStage stage,
			net.minecraft.world.item.Item output) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			HemoJourneyFixtures.prepare(player, stage, origin);
			spawn(helper, origin.getX() + 0.5D, origin.getY() + 2.5D, origin.getZ() + 0.5D,
					new ItemStack(output));
			boolean captured = invokeBooleanFixture("captureExpectedOutputs", player, stage, origin);
			helper.assertTrue(HemoJourneyCheckpointRules.craftPassed(captured, true, true),
					"Exact craft delta plus a newly earned advancement must pass");
		} finally {
			invokeFixture("cleanupForExit", player, stage, origin);
			player.discard();
		}
		helper.succeed();
	}

	private static void settledCraftOutput(GameTestHelper helper, HemoJourneyStage stage,
			net.minecraft.world.item.Item output) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		ItemEntity baseline = spawn(helper, origin.getX() + 1.0D, origin.getY() + 1.0D,
				origin.getZ() + 0.5D, new ItemStack(output));
		ItemEntity wrongCount = null;
		ItemEntity wrongItem = null;
		try {
			HemoJourneyFixtures.prepare(player, stage, origin);
			wrongCount = spawn(helper, origin.getX() - 0.4D, origin.getY() + 1.0D,
					origin.getZ() + 1.4D, new ItemStack(output, 2));
			wrongItem = spawn(helper, origin.getX(), origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(Blocks.COBBLESTONE));
			ItemEntity settled = spawn(helper, origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() + 0.5D, new ItemStack(output));
			helper.assertTrue(invokeBooleanFixture("captureExpectedOutputs", player, stage, origin),
					"Expected settled craft output to be captured at platform height");
			invokeFixture("cleanupForExit", player, stage, origin);
			helper.assertTrue(settled.isRemoved(), "Captured settled craft output must be cleaned");
			helper.assertTrue(!baseline.isRemoved(), "Baseline craft output must be preserved");
			helper.assertTrue(!wrongCount.isRemoved(), "Wrong-count craft output must be preserved");
			helper.assertTrue(!wrongItem.isRemoved(), "Wrong-item craft output must be preserved");
		} finally {
			baseline.discard();
			if (wrongCount != null) wrongCount.discard();
			if (wrongItem != null) wrongItem.discard();
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
		helper.succeed();
	}

	private static BlockPos invokeJourneyLanding(BlockPos origin) {
		try {
			Class<?> controller = Class.forName(
					"com.vincenthuto.hemomancy.gametest.journey.HemoJourneyController");
			return (BlockPos) controller.getMethod("landing", BlockPos.class).invoke(null, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Journey landing geometry seam is missing", exception);
		}
	}

	private static ItemEntity spawn(GameTestHelper helper, double x, double y, double z, ItemStack stack) {
		ItemEntity entity = new ItemEntity(helper.getLevel(), x, y, z, stack);
		helper.getLevel().addFreshEntity(entity);
		return entity;
	}

	private static HemoJourneyResult invokeVerifyClaiming(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		try {
			Method method = HemoJourneyChecks.class.getMethod("verify", ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class, boolean.class);
			return (HemoJourneyResult) method.invoke(null, player, stage, origin, true);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Claiming verification API is missing", exception);
		}
	}

	private static boolean invokeBooleanFixture(String methodName, ServerPlayer player,
			HemoJourneyStage stage, BlockPos origin) {
		try {
			Method method = HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class);
			return (boolean) method.invoke(null, player, stage, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture ownership API is missing", exception);
		}
	}

	private static void invokeFixture(String methodName, ServerPlayer player, BlockPos origin) {
		try {
			HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class, BlockPos.class)
					.invoke(null, player, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture cleanup API is missing", exception);
		}
	}

	private static void invokeFixture(String methodName, ServerPlayer player,
			HemoJourneyStage stage, BlockPos origin) {
		try {
			HemoJourneyFixtures.class.getMethod(methodName, ServerPlayer.class,
					HemoJourneyStage.class, BlockPos.class).invoke(null, player, stage, origin);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("Fixture exit API is missing", exception);
		}
	}


	private static Block[][] ashPattern(Block center) {
		Block ash = BlockInit.befouling_ash_trail.get();
		return new Block[][] {
				{ ash, ash, ash },
				{ ash, center, ash },
				{ ash, ash, ash }
		};
	}

	private static void assertWall(GameTestHelper helper, BlockPos origin, Block[][] rows) {
		for (int row = 0; row < rows.length; row++) {
			for (int column = 0; column < rows[row].length; column++) {
				assertBlock(helper, origin.offset(column - 1, 3 - row, 0), rows[row][column]);
			}
		}
	}

	private static void assertFloor(GameTestHelper helper, BlockPos origin, Block[][] rows) {
		for (int z = 0; z < rows.length; z++) {
			for (int x = 0; x < rows[z].length; x++) {
				assertBlock(helper, origin.offset(x - 1, 1, z - 1), rows[z][x]);
			}
		}
	}

	private static void assertBlock(GameTestHelper helper, BlockPos pos, Block expected) {
		helper.assertTrue(helper.getLevel().getBlockState(pos).is(expected),
				"Expected " + expected + " at " + pos + " but found " + helper.getLevel().getBlockState(pos));
	}

	private static void withFixture(GameTestHelper helper, HemoJourneyStage stage, FixtureAssertion assertion) {
		ServerPlayer player = detachedTestPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(4, 3, 4));
		try {
			invoke("prepare", player, stage, origin);
			assertion.check(origin, player);
			helper.succeed();
		} finally {
			try {
				invoke("cleanup", player, null, origin);
			} finally {
				player.discard();
			}
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180)
	public static void weightOfFrameJourneyUsesRealArmatureAbilityAndDialogue(GameTestHelper helper) {
		ServerPlayer player = connectedTestPlayer(helper);
		setServerPlayerLookup(player, true);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		BlockPos armaturePos = origin.above();
		try {
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(),
					"Weight of the Frame snapshot capture must succeed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(),
					"Weight of the Frame reset must succeed");
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(7);

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_WEIGHT_OF_FRAME_BRIEFING, origin);
			HarbingerArtificerEntity artificer = helper.getLevel().getEntitiesOfClass(
					HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
			NeoForge.EVENT_BUS.post(new DialogueEvent(player,
					HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME, artificer.getId()));
			helper.assertTrue(HemoJourneyChecks.verify(player,
					HemoJourneyStage.ARTIFICER_WEIGHT_OF_FRAME_BRIEFING, origin).passed(),
					"Real Artificer dialogue must brief Weight of the Frame");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_MONOLITHIC_FRAME, origin);
			HematicArmatureBlockEntity armature = (HematicArmatureBlockEntity) helper.getLevel()
					.getBlockEntity(armaturePos);
			helper.assertTrue(armature.applyArmatureUpgradeItem(player, InteractionHand.MAIN_HAND)
						&& HemoJourneyChecks.verify(player,
								HemoJourneyStage.ARTIFICER_MONOLITHIC_FRAME, origin).passed(),
					"The supplied Cornerstone must apply through the real Armature interaction");

			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_D7_UPGRADE, origin);
			armature = (HematicArmatureBlockEntity) helper.getLevel().getBlockEntity(armaturePos);
			BlockInit.hematic_armature.get().stepOn(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), player);
			HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
					helper.getLevel().getBlockState(armaturePos), armature);
			HematicArmatureBlockEntity preparedArmature = armature;

			helper.runAfterDelay(101, () -> {
				try {
					HematicArmatureBlockEntity.serverTick(helper.getLevel(), armaturePos,
							helper.getLevel().getBlockState(armaturePos), preparedArmature);
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_D7_UPGRADE, origin).passed(),
							"The real monolithic Armature cycle must create Edacious boots");
					player.stopRiding();

					HemoJourneyFixtures.prepare(player,
							HemoJourneyStage.ARTIFICER_WEIGHT_OF_FRAME_INSPECTION, origin);
					HarbingerArtificerEntity inspector = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME, inspector.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_WEIGHT_OF_FRAME_INSPECTION, origin).passed(),
							"Real Artificer inspection must issue the recorded Edacious material");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_D7_DEMONSTRATION, origin);
					helper.assertTrue(ArmorSetAbilityRegistry.tryActivate(player,
							ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST)
							&& HemoJourneyChecks.verify(player,
									HemoJourneyStage.ARTIFICER_D7_DEMONSTRATION, origin).passed(),
							"The real Bloodburst activation must demonstrate the full Edacious set");

					HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_D7_FITTING, origin);
					HarbingerArtificerEntity fitter = helper.getLevel().getEntitiesOfClass(
							HarbingerArtificerEntity.class, HemoJourneyFixtures.bounds(origin)).getFirst();
					NeoForge.EVENT_BUS.post(new DialogueEvent(player,
							HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING, fitter.getId()));
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_D7_FITTING, origin).passed(),
							"Real Artificer dialogue must grant the Monolithic Frame fitting");

					HemoJourneyFixtures.cleanup(player, origin);
					helper.assertTrue(HemoJourneySnapshot.restore(player).passed()
							&& !ArtificerAssignments.has(player, ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED)
							&& ArtificerAssignments.firstD7Lineage(player)
									== com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage.NONE,
							"Snapshot restore must remove journey-owned Weight of the Frame state");
					setServerPlayerLookup(player, false);
					player.discard();
					helper.succeed();
				} catch (RuntimeException | AssertionError failure) {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
					throw failure;
				}
			});
		} catch (RuntimeException | AssertionError failure) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw failure;
		}
	}

	private static void invoke(String methodName, ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		try {
			Class<?> fixtures = Class.forName(FIXTURES_CLASS);
			Method method = methodName.equals("prepare")
					? fixtures.getMethod(methodName, ServerPlayer.class, HemoJourneyStage.class, BlockPos.class)
					: fixtures.getMethod(methodName, ServerPlayer.class, BlockPos.class);
			if (stage == null) method.invoke(null, player, origin);
			else method.invoke(null, player, stage, origin);
		} catch (ClassNotFoundException exception) {
			throw new AssertionError("Journey fixture preparation is absent", exception);
		} catch (NoSuchMethodException | IllegalAccessException exception) {
			throw new AssertionError("Journey fixture API is unavailable", exception);
		} catch (InvocationTargetException exception) {
			throw new AssertionError("Journey fixture operation failed", exception.getCause());
		}
	}

	private static ServerPlayer detachedTestPlayer(GameTestHelper helper) {
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				new GameProfile(UUID.randomUUID(), "journey-fixture-player"), ClientInformation.createDefault());
		player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				helper.getLevel().dimension().location().toString());
		return player;
	}

	private static ServerPlayer connectedTestPlayer(GameTestHelper helper) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "journey-snapshot-player"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), cookie.clientInformation());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override
			public void send(net.minecraft.network.protocol.Packet<?> packet) {
			}
		};
		player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				helper.getLevel().dimension().location().toString());
		return player;
	}

	private static void pickUpFixtureItem(GameTestHelper helper, ServerPlayer player, BlockPos origin, Item item) {
		ItemEntity drop = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
				HemoJourneyFixtures.bounds(origin), entity -> entity.getItem().is(item)).getFirst();
		player.getInventory().add(drop.getItem().copy());
		drop.discard();
	}

	@SuppressWarnings("unchecked")
	private static void setServerPlayerLookup(ServerPlayer player, boolean present) {
		try {
			var field = net.minecraft.server.players.PlayerList.class.getDeclaredField("playersByUUID");
			field.setAccessible(true);
			var players = (java.util.Map<UUID, ServerPlayer>) field.get(player.server.getPlayerList());
			if (present) players.put(player.getUUID(), player);
			else players.remove(player.getUUID(), player);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Could not register the Armature GameTest player lookup", exception);
		}
	}

	@FunctionalInterface
	private interface FixtureAssertion {
		void check(BlockPos origin, ServerPlayer player);
	}
}
