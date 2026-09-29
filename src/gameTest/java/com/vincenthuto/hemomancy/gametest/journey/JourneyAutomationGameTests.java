package com.vincenthuto.hemomancy.gametest.journey;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodCraftingKeyPressPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteActivationRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteNpcTravel;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class JourneyAutomationGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private JourneyAutomationGameTests() { }

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationHarbinger")
	public static void journeySnapshotSurvivesDeathClone(GameTestHelper helper) {
		CompoundTag original = new CompoundTag();
		CompoundTag snapshot = new CompoundTag();
		snapshot.putString("marker", "before death");
		original.put(HemoJourneySnapshot.SNAPSHOT_KEY, snapshot);
		original.putString(HemoJourneySnapshot.STAGE_KEY, HemoJourneyStage.SILENT_REFUSAL.id());
		original.putLong(HemoJourneyFixtures.ORIGIN_KEY, 123L);
		original.putString("unrelated", "leave behind");
		CompoundTag replacement = new CompoundTag();
		replacement.putString("unrelated", "keep replacement");

		HemoJourneySnapshot.copyJourneyData(original, replacement);

		original.getCompound(HemoJourneySnapshot.SNAPSHOT_KEY).putString("marker", "changed");
		helper.assertTrue("before death".equals(replacement.getCompound(HemoJourneySnapshot.SNAPSHOT_KEY)
				.getString("marker"))
				&& HemoJourneyStage.SILENT_REFUSAL.id().equals(replacement.getString(HemoJourneySnapshot.STAGE_KEY))
				&& replacement.getLong(HemoJourneyFixtures.ORIGIN_KEY) == 123L
				&& "keep replacement".equals(replacement.getString("unrelated")),
				"Death clone must retain an independent journey snapshot without replacing unrelated new-player data");
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationHarbinger")
	public static void harbingerToChoiceLeavesSnapshotForManualProjection(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			helper.assertTrue(JourneyAutoRunner.runHarbingerToChoice(player),
					"Harbinger to-choice automation must start");
			player.getInventory().setItem(0, new ItemStack(ItemInit.fungal_spine.get()));
			player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY,
					HemoJourneyStage.APOTHEOS_CHOICE.id());
			JourneyAutoRunner.tickForTest(player);
			helper.assertTrue(!JourneyAutoRunner.activeForTest(player)
					&& player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY)
					&& player.getInventory().getItem(0).is(ItemInit.fungal_spine.get())
					&& !player.getPersistentData().getBoolean(
							com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)
					&& HemoJourneyStage.APOTHEOS_CHOICE.id().equals(
							player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
					"To-choice run must stop before choosing an ending and retain the snapshot");
			helper.succeed();
		} finally {
			JourneyAutoRunner.cancel(player);
			HemoJourneyController.clear(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationSeparation")
	public static void journeySnapshotRestoresDistillationAndPendingRest(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			var brewing = HemoCapabilityAccess.advancedBrewing(player);
			brewing.record("distill");
			var persisted = new net.minecraft.nbt.CompoundTag();
			persisted.putBoolean("hemomancy:concentrated_blood_pending", true);
			player.getPersistentData().put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, persisted);
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(), "Journey snapshot capture failed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(), "Journey snapshot reset failed");
			helper.assertTrue(!HemoCapabilityAccess.advancedBrewing(player).distilled()
					&& !player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)
							.getBoolean("hemomancy:concentrated_blood_pending"),
					"Journey reset retained preexisting D3 proof or pending-rest state");
			helper.assertTrue(HemoJourneySnapshot.restore(player).passed(), "Journey snapshot restore failed");
			helper.assertTrue(HemoCapabilityAccess.advancedBrewing(player).distilled()
					&& player.getPersistentData().getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG)
							.getBoolean("hemomancy:concentrated_blood_pending"),
					"Journey restore lost preexisting D3 proof or pending-rest state");
			helper.succeed();
		} finally {
			if (player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY))
				HemoJourneySnapshot.restore(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 100,
			batch = "journeyAutomationSeparation")
	public static void d3JourneyUsesDistillationInjectionAndRest(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			HemoCapabilityAccess.requireEquipment(player).setStackInSlot(5,
					new ItemStack(ItemInit.charm_of_vascularium.get()));
			com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.activate(player);
			FirstSeparationAssignment.markClaimed(player);
			player.getInventory().add(new ItemStack(ItemInit.hematic_iron_scrap.get()));
			var distillation = HemoJourneyStage.valueOf("FIRST_DISTILLATION");
			HemoJourneyFixtures.prepare(player, distillation, origin);
			HarbingerJourneyAutomation.perform(player, distillation.id(), origin);
			var proof = HemoJourneyChecks.verify(player, distillation, origin);
			helper.assertTrue(proof.passed(), "Journey must collect a real ordinary distillation: " + proof.message());
			var rest = HemoJourneyStage.valueOf("CONCENTRATED_BLOOD_REST");
			HemoJourneyFixtures.prepare(player, rest, origin);
			var foot = player.serverLevel().getBlockState(origin.east().above());
			var head = player.serverLevel().getBlockState(origin.east().south().above());
			helper.assertTrue(foot.is(net.minecraft.world.level.block.Blocks.RED_BED)
					&& head.is(net.minecraft.world.level.block.Blocks.RED_BED)
					&& foot.getValue(net.minecraft.world.level.block.BedBlock.PART)
					== net.minecraft.world.level.block.state.properties.BedPart.FOOT
					&& head.getValue(net.minecraft.world.level.block.BedBlock.PART)
							== net.minecraft.world.level.block.state.properties.BedPart.HEAD,
					"Concentrated Blood rest fixture needs a complete bed: " + foot + " / " + head);
			for (int slot = 0; slot < 9; slot++) {
				player.getInventory().setItem(slot, new ItemStack(net.minecraft.world.item.Items.STONE));
			}
			player.getInventory().setItem(9, new ItemStack(ItemInit.bloody_vial.get()));
			HarbingerJourneyAutomation.perform(player, rest.id(), origin);
			var promotion = HemoJourneyChecks.verify(player, rest, origin);
			helper.assertTrue(promotion.passed(), "Journey must inject and complete rest for D3: "
					+ promotion.message());
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 120,
			batch = "journeyAutomationCircus")
	public static void circusRunnerStartsAndRestoresKnownSummons(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		List<String> originalSummons = List.copyOf(HemoCapabilityAccess.requireKnownSummons(player).getKnownSummonNames());
		try {
			helper.assertTrue(JourneyAutoRunner.runCircus(player), "Circus automation must start");
			JourneyAutoRunner.cancel(player);
			helper.assertTrue(CircusJourneyController.clear(player).passed(), "Circus clear must restore the snapshot");
			helper.assertTrue(!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY)
					&& originalSummons.equals(HemoCapabilityAccess.requireKnownSummons(player).getKnownSummonNames()),
					"Circus automation must restore the snapshot and known summons");
			helper.succeed();
		} finally {
			JourneyAutoRunner.cancel(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 600,
			batch = "journeyAutomationCircus")
	public static void circusLiberationRunnerCompletesAtDegreeFour(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			setServerPlayerLookup(player, true);
			helper.getLevel().addNewPlayer(player);
			helper.assertTrue(JourneyAutoRunner.runCircus(player, "liberation"),
					"Degree-4 Liberation automation must start");
			BlockPos fixtureOrigin = CircusJourneyController.origin(player);
			var fixtureRingmaster = CircusJourneyFixtures.ringmaster(player, fixtureOrigin);
			var fixtureCarousel = CircusJourneyFixtures.carousel(player, fixtureOrigin);
			for (int tick = 1; tick <= 560; tick++) {
				int expectedTicks = tick;
				helper.runAtTickTime(tick, () -> {
					if (!fixtureCarousel.isRemoved() && fixtureCarousel.tickCount < expectedTicks) fixtureCarousel.tick();
					if (!fixtureRingmaster.isRemoved() && fixtureRingmaster.tickCount < expectedTicks) fixtureRingmaster.tick();
					JourneyAutoRunner.tickForTest(player);
					CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
				});
			}
			helper.runAtTickTime(570, () -> {
				boolean complete = !JourneyAutoRunner.activeForTest(player)
						&& !player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY);
				if (complete) {
					helper.assertTrue(!com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData
							.get(player.serverLevel()).hasSite(player.serverLevel(), fixtureOrigin.above()),
						"Circus automation must remove its fixture pavilion state after restoring the player");
					setServerPlayerLookup(player, false);
					player.discard();
					helper.succeed();
					return;
				}
				helper.assertTrue(false,
						"Degree-4 Liberation must finish, award Thread Ripper, and restore the snapshot. "
								+ JourneyAutoRunner.describe(player));
				helper.succeed();
			});
		} catch (RuntimeException exception) {
			setServerPlayerLookup(player, false);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 1350,
			batch = "journeyAutomationCircus")
	public static void circusSuccessionRunnerCompletesAtDegreeFour(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			setServerPlayerLookup(player, true);
			helper.getLevel().addNewPlayer(player);
			helper.assertTrue(JourneyAutoRunner.runCircus(player, "succession"),
					"Degree-4 Succession automation must start");
			BlockPos fixtureOrigin = CircusJourneyController.origin(player);
			var fixtureRingmaster = CircusJourneyFixtures.ringmaster(player, fixtureOrigin);
			var fixtureCarousel = CircusJourneyFixtures.carousel(player, fixtureOrigin);
			for (int tick = 1; tick <= 1320; tick++) {
				int expectedTicks = tick;
				helper.runAtTickTime(tick, () -> {
					if (!fixtureCarousel.isRemoved() && fixtureCarousel.tickCount < expectedTicks) fixtureCarousel.tick();
					if (!fixtureRingmaster.isRemoved() && fixtureRingmaster.tickCount < expectedTicks) fixtureRingmaster.tick();
					JourneyAutoRunner.tickForTest(player);
					CircusPerformanceController.onPlayerTick(new PlayerTickEvent.Post(player));
				});
			}
			helper.runAtTickTime(1330, () -> {
				boolean complete = !JourneyAutoRunner.activeForTest(player)
						&& !player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY);
				helper.assertTrue(complete,
						"Degree-4 Succession must finish, award the Ringmaster Pattern, and restore the snapshot. "
								+ JourneyAutoRunner.describe(player) + " Challenges="
								+ com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress.challenges(player)
								+ " active=" + player.getPersistentData().getInt("hemomancy.circus_active_challenge")
								+ " ticks=" + player.getPersistentData().getInt("hemomancy.circus_challenge_ticks"));
				helper.assertTrue(!com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData
						.get(player.serverLevel()).hasSite(player.serverLevel(), fixtureOrigin.above()),
						"Circus automation must remove its fixture pavilion state after restoring the player");
				setServerPlayerLookup(player, false);
				player.discard();
				helper.succeed();
			});
		} catch (RuntimeException exception) {
			setServerPlayerLookup(player, false);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationHarbinger")
	public static void harbingerRunnerAdvancesThroughHermitRoadWithoutOpeningScreens(GameTestHelper helper) {
		List<Packet<?>> outbound = new ArrayList<>();
		ServerPlayer player = connectedPlayer(helper, outbound::add);
		player.getInventory().add(new ItemStack(ItemInit.fungal_spine.get(), 3));
		ItemStack original = player.getInventory().getItem(0).copy();
		try {
			helper.assertTrue(JourneyAutoRunner.runHarbinger(player), "Harbinger automation must start");
			for (int tick = 0; tick < 7; tick++) JourneyAutoRunner.tickForTest(player);
			helper.runAfterDelay(2, () -> {
				try {
					for (int tick = 0; tick < 2; tick++) JourneyAutoRunner.tickForTest(player);
					BlockPos fixtureOrigin = BlockPos.of(player.getPersistentData()
							.getLong(HemoJourneyFixtures.ORIGIN_KEY));
					helper.assertTrue(HemoJourneyStage.VESSEL_FILLED.id().equals(
							player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
							"The Vicar report must grant the ledger and advance after its world drop is indexed. "
									+ JourneyAutoRunner.describe(player) + " Current check: "
									+ HemoJourneyChecks.verify(player, HemoJourneyStage.VICAR_HERMIT_ROAD_REPORT,
											fixtureOrigin).message());
					helper.assertTrue(outbound.stream().noneMatch(JourneyAutomationGameTests::opensJourneyScreen),
						"Server-side automation must not send dialogue or inscription screens to the client");
					helper.assertTrue(CardinalRiteSavedData.get(player.serverLevel()).getRite(player.getUUID()) == null,
						"Automatic rite completion must retire the active rite before the next fixture is prepared");
				JourneyAutoRunner.cancel(player);
				helper.assertTrue(HemoJourneyController.clear(player).passed(), "Clear must restore the Harbinger snapshot");
				helper.assertTrue(!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY)
						&& ItemStack.isSameItemSameComponents(original, player.getInventory().getItem(0))
						&& player.getInventory().getItem(0).getCount() == original.getCount(),
						"Clear must restore the exact captured inventory and remove the snapshot");
				helper.succeed();
				} finally {
					JourneyAutoRunner.cancel(player);
					HemoJourneyController.clear(player);
					player.discard();
				}
			});
		} catch (RuntimeException exception) {
			JourneyAutoRunner.cancel(player);
			HemoJourneyController.clear(player);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationFormation")
	public static void harbingerRunnerProjectsFormationWithoutPlayerInput(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			setServerPlayerLookup(player, true);
			helper.assertTrue(JourneyAutoRunner.runHarbinger(player), "Harbinger automation must start");
			for (int tick = 0; tick < 20 && !HemoJourneyStage.FORMATION_PROJECTED.id().equals(
					player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)); tick++) {
				JourneyAutoRunner.tickForTest(player);
			}
			helper.assertTrue(HemoJourneyStage.FORMATION_PROJECTED.id().equals(
					player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
					"The four First Bloodcraft proofs must precede the formation retry check: "
							+ JourneyAutoRunner.describe(player));
			BlockPos origin = BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY));
			player.serverLevel().removeBlock(origin.above(), false);
			player.setYRot(180.0F);
			player.setXRot(0.0F);
			for (int tick = 0; tick < 2; tick++) JourneyAutoRunner.tickForTest(player);
			player.serverLevel().setBlockAndUpdate(origin.above(), BlockInit.venous_stone.get().defaultBlockState());
			for (int tick = 0; tick < 22; tick++) JourneyAutoRunner.tickForTest(player);
			helper.assertTrue(HemoJourneyStage.LIBER_CRAFTED.id().equals(
					player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
					"The automatic projection must pass formation_projected without player input. "
							+ JourneyAutoRunner.describe(player) + " Current check: "
							+ HemoJourneyController.status(player).message());
			helper.succeed();
		} finally {
			JourneyAutoRunner.cancel(player);
			HemoJourneyController.clear(player);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationSeparation")
	public static void automaticSeparationActionUsesPreparedStationAndSamples(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			FirstSeparationAssignment.markBriefed(player);
			FirstSeparationAssignment.giveBriefingSupplies(player);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.SEPARATION_STARTED, origin);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.SEPARATION_STARTED.id(), origin);
			HemoJourneyResult result = HemoJourneyChecks.verify(player, HemoJourneyStage.SEPARATION_STARTED, origin);
			helper.assertTrue(result.passed(), "Automatic First Separation must start with the prepared station and cows: "
					+ result.message());
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationUnstained")
	public static void unstainedRunnerUsesRealPodiumAndOwnsItsRoute(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			helper.assertTrue(JourneyAutoRunner.runUnstained(player, "cure"), "Cure automation must start");
			helper.assertTrue(!JourneyAutoRunner.runHarbinger(player), "An active cure run must reject route takeover");
			for (int tick = 0; tick < 2; tick++) JourneyAutoRunner.tickForTest(player);
			helper.assertTrue(UnstainedJourneyStage.LETHEAN_BAPTISM.id().equals(
					player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
					"The real Podium interaction must verify and transition to Lethean Baptism. "
							+ JourneyAutoRunner.describe(player) + " Current check: "
							+ UnstainedJourneyController.status(player).message());
			JourneyAutoRunner.cancel(player);
			helper.assertTrue(UnstainedJourneyController.clear(player).passed(), "Clear must restore the cure snapshot");
			helper.succeed();
		} finally {
			JourneyAutoRunner.cancel(player);
			UnstainedJourneyController.clear(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationFailure")
	public static void runnerLatchesFailureAndRetainsInspectionState(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			helper.assertTrue(JourneyAutoRunner.runHarbinger(player), "Harbinger automation must start");
			BlockPos origin = BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY));
			player.serverLevel().setBlock(origin.above(), Blocks.AIR.defaultBlockState(), 3);
			for (int tick = 0; tick < 1202; tick++) JourneyAutoRunner.tickForTest(player);
			helper.assertTrue(!JourneyAutoRunner.activeForTest(player)
					&& JourneyAutoRunner.describe(player).contains("Automation failed")
					&& player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY),
					"Timeout must latch its failure while retaining the snapshot and fixture for inspection");
			helper.assertTrue(HemoJourneyController.clear(player).passed(), "Failure inspection state must remain clearable");
			helper.succeed();
		} finally {
			JourneyAutoRunner.cancel(player);
			HemoJourneyController.clear(player);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationLivingStaff")
	public static void automaticLivingStaffActionPassesAuthoritativeCheck(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
					helper.getLevel().dimension().location().toString());
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(1);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.LIVING_STAFF_CRAFTED, origin);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.LIVING_STAFF_CRAFTED.id(), origin);
			helper.assertTrue(hasItem(player, ItemInit.living_staff.get()),
					"Automatic blood crafting must pick up its attributable output through the real pickup hook");
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.LIVING_STAFF_CRAFTED, origin).passed(),
					"Automatic async structure action must produce a bonded staff through the authoritative check");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationCentrifuge")
	public static void automaticCentrifugeActionPassesAuthoritativeCheck(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
					helper.getLevel().dimension().location().toString());
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(2);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.CENTRIFUGE_PREPARED, origin);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.CENTRIFUGE_PREPARED.id(), origin);
			HemoJourneyResult result = HemoJourneyChecks.verify(player, HemoJourneyStage.CENTRIFUGE_PREPARED, origin);
			helper.assertTrue(result.passed(), "Automatic Vial Centrifuge action must craft and place the station: "
					+ result.message() + " Center block: " + helper.getLevel().getBlockState(origin.above(2)));
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationLoom")
	public static void automaticLoomActionDrawsEveryStrandHome(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_MEMORY_WOVEN, origin);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.FIRST_MEMORY_WOVEN.id(), origin);
			helper.assertTrue(HemoJourneyFixtures.expectedOutputsPresent(player,
					HemoJourneyStage.FIRST_MEMORY_WOVEN, origin),
					"Automatic loom action must produce the real Blood Shot memory");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationLoomResume")
	public static void automaticLoomActionResumesPaidWeave(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_MEMORY_WOVEN, origin);
			var loom = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity)
					player.serverLevel().getBlockEntity(origin.above());
			helper.assertTrue(loom.startRitual(player) && loom.tryChargeRitualBlood(player, 10_000.0D, true),
					"Expected a paid weave before resuming");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.FIRST_MEMORY_WOVEN.id(), origin);
			helper.assertTrue(HemoJourneyFixtures.expectedOutputsPresent(player,
					HemoJourneyStage.FIRST_MEMORY_WOVEN, origin),
					"Resumed loom action must produce the real Blood Shot memory");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationScar")
	public static void automaticScarCraftRecordsFirstLesson(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.FIRST_SCAR_CARVED.id(), origin);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin).passed(),
					"Automatic scar craft must produce the lesson scar and record its advancement");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationScarResume")
	public static void automaticScarCraftResumesFinishedOutput(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin);
			var station = (com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity)
					player.serverLevel().getBlockEntity(origin.above());
			station.craftEvent();
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter
					.isVeinMasonFirstScarCarved(player), "Fixture should begin without the first-scar advancement");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.FIRST_SCAR_CARVED.id(), origin);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.FIRST_SCAR_CARVED, origin).passed(),
					"Resumed scar craft must recognize the existing lesson scar");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationD6Scar")
	public static void continuationScarUsesPersonalStationAccessAndRestoresCraftCredit(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		var crafted = net.minecraft.stats.Stats.ITEM_CRAFTED.get(BlockInit.scar_station.get().asItem());
		try {
			setServerPlayerLookup(player, true);
			player.getStats().setValue(player, crafted, 2);
			helper.assertTrue(HemoJourneySnapshot.capture(player).passed(), "Scar snapshot capture failed");
			helper.assertTrue(HemoJourneySnapshot.resetForJourney(player).passed(), "Scar snapshot reset failed");
			helper.assertTrue(player.getStats().getValue(crafted) == 0,
					"Journey reset must suspend preexisting station craft credit");
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_CARVED, origin);
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.MachineAccessEvents.hasPersonalAccess(
					player, BlockInit.scar_station.get()),
					"D6 fixture must establish personal Scarring Station access");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_CARVED.id(), origin);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.VEIN_MASON_D6_SCAR_CARVED, origin).passed(),
					"Continuation scar must be carved through the player-facing server action");
			HemoJourneyFixtures.cleanup(player, origin);
			helper.assertTrue(HemoJourneySnapshot.restore(player).passed(), "Scar snapshot restore failed");
			helper.assertTrue(player.getStats().getValue(crafted) == 2,
					"Journey restore must return the original station craft credit");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			if (player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY))
				HemoJourneySnapshot.restore(player);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationArsenal")
	public static void livingArsenalFixtureSurvivesDaylightAndCreditsBlade(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments.brief(player,
					com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments.ASSUMED_LIMB_BRIEFED);
			com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.grantIfNotDone(player,
					com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_LIVING_ARSENAL_DEMONSTRATION, origin);
			var target = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,
					HemoJourneyFixtures.bounds(origin), entity -> entity.getTags().contains(
							HemoJourneyFixtures.entityMarker(origin))).getFirst();
			helper.assertTrue(target instanceof net.minecraft.world.entity.monster.Husk,
					"Living Arsenal target must not burn before the blade demonstration");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_LIVING_ARSENAL_DEMONSTRATION.id(), origin);
			helper.assertTrue(com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments.has(player,
					com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments.ASSUMED_LIMB_DEMONSTRATED),
					"Living Blade kill must earn demonstration credit");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationLacquer")
	public static void alchemistLacquerRewardReachesArmatureInventory(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_COUNSEL, origin);
			var reward = new net.minecraft.world.entity.item.ItemEntity(helper.getLevel(),
					origin.getX() + 0.5D, origin.getY() + 1.5D, origin.getZ() + 0.5D,
					new ItemStack(ItemInit.crimson_lacquer.get()));
			helper.getLevel().addFreshEntity(reward);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_CRIMSON_VESTMENT_COUNSEL.id(), origin);
			helper.assertTrue(hasItem(player, ItemInit.crimson_lacquer.get()),
					"Alchemist's real dropped lacquer must enter inventory before the Armature fixture");
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180,
			batch = "journeyAutomationFork")
	public static void automaticForkUpgradeTargetsBootsAfterFullArmorFitting(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		setServerPlayerLookup(player, true);
		HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
				new ItemStack(ItemInit.hematic_iron_helm.get()));
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
				new ItemStack(ItemInit.hematic_iron_chestplate.get()));
		player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
				new ItemStack(ItemInit.hematic_iron_leggings.get()));
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_FORK_UPGRADE, origin);
			helper.assertTrue(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEmpty(),
					"Fork fixture must leave the Boots as the only eligible Armature piece");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_FORK_UPGRADE.id(), origin);
			helper.runAfterDelay(105, () -> {
				try {
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_FORK_UPGRADE, origin).passed(),
							"The Armature must produce Barbed Boots for the journey: feet="
									+ player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180,
			batch = "journeyAutomationBloodLust")
	public static void automaticBloodLustUpgradeTargetsBootsAfterFullBarbedFitting(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(5);
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
					new ItemStack(ItemInit.barbed_helm.get()));
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
					new ItemStack(ItemInit.barbed_chestplate.get()));
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
					new ItemStack(ItemInit.barbed_leggings.get()));
			player.getInventory().add(new ItemStack(ItemInit.crimson_lacquer.get()));
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BLOOD_LUST_UPGRADE, origin);
			helper.assertTrue(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEmpty(),
					"Blood Lust fixture must leave Boots as the only eligible Armature piece");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_BLOOD_LUST_UPGRADE.id(), origin);
			helper.runAfterDelay(105, () -> {
				try {
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_BLOOD_LUST_UPGRADE, origin).passed(),
							"The Armature must produce Blood Lust Boots for the journey");
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationCovenantVigil")
	public static void recruitedVicarFixtureSurvivesResidencyTick(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		UUID lineId = UUID.randomUUID();
		try {
			Bloodline line = new Bloodline("Recruited Vigil Helper", player.getUUID(), lineId, new ArrayList<>());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(line);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(line);
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.COVENANT_VIGIL, origin);
			helper.assertTrue(BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE)
					== CardinalRiteActivationRules.ActivationAttempt.STARTED,
					"Prepared Covenant Vigil must start");
			helper.assertTrue(HemoJourneyWorldState.assignSwornHelper(player, origin.above(),
					Hemomancy.rloc("cardinal_rite/covenant_vigil")),
					"Recruited fixture Vicar must take the Vigil station");
			var rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			HarbingerVicarEntity vicar = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
					HemoJourneyFixtures.bounds(origin), entity -> entity.getTags().contains(
							HemoJourneyFixtures.entityMarker(origin))).getFirst();
			com.vincenthuto.hemomancy.common.succession.SuccessionResidents.tick(helper.getLevel(), vicar);
			helper.assertTrue(!vicar.isSuccessor() && line.hasNpcMember(vicar.getUUID())
					&& CardinalRiteAllyService.isAvailable(helper.getLevel(), rite, vicar.getUUID()),
					"Recruited Vicar must remain a valid Vigil helper after its residency tick");
			helper.succeed();
		} finally {
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			HemoJourneyFixtures.cleanup(player, origin);
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(lineId);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationCovenantVigil")
	public static void completedVigilKeepsItsHelperUntilFinalCheck(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		UUID lineId = UUID.randomUUID();
		try {
			Bloodline line = new Bloodline("Vigil Helper", player.getUUID(), lineId, new ArrayList<>());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(line);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(line);
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
			FoundingFaneSavedData.get(helper.getLevel()).consecrateHeart(player.getUUID(), origin.above());
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.COVENANT_VIGIL, origin);
			HarbingerVicarEntity successor = helper.getLevel().getEntitiesOfClass(HarbingerVicarEntity.class,
					HemoJourneyFixtures.bounds(origin), entity -> entity.getTags().contains(
							HemoJourneyFixtures.entityMarker(origin))).getFirst();
			com.vincenthuto.hemomancy.gametest.SuccessionTestFixtures.resident(player, successor, origin.above());
			helper.assertTrue(BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE)
					== CardinalRiteActivationRules.ActivationAttempt.STARTED,
					"Prepared Covenant Vigil must start");
			helper.assertTrue(HemoJourneyWorldState.assignSwornHelper(player, origin.above(),
					Hemomancy.rloc("cardinal_rite/covenant_vigil")),
					"Fixture successor must take the Vigil station");
			var rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			helper.assertTrue(successor.getPersistentData().contains("HematicRiteHelperTrip"),
					"The successor must have an active rite trip before completion");
			rite.markComplete();
			CardinalRiteNpcTravel.tick(helper.getLevel(), successor);
			helper.assertTrue(rite.getAllyRoles().containsKey(successor.getUUID())
					&& successor.getPersistentData().contains("HematicRiteHelperTrip")
					&& CardinalRiteAllyService.isAvailable(helper.getLevel(), rite, successor.getUUID()),
					"A completed rite must retain its helper until the final required-helper check");
			helper.succeed();
		} finally {
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			FoundingFaneSavedData.get(helper.getLevel()).remove(player.getUUID());
			HemoJourneyFixtures.cleanup(player, origin);
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(lineId);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationArchonRetry")
	public static void swornHelperCanRetryAfterTemporaryStationObstruction(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		UUID lineId = UUID.randomUUID();
		BlockPos station = null;
		try {
			Bloodline line = new Bloodline("Archon Retry", player.getUUID(), lineId, new ArrayList<>());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).registerBloodline(line);
			HemoCapabilityAccess.requireBloodVolume(player).setBloodLine(line);
			HemoCapabilityAccess.requireBloodVolume(player).setActive(true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(6);
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARCHON_RITE, origin);
			var recipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(),
					Hemomancy.rloc("cardinal_rite/archon_rite"));
			station = origin.above().offset(CardinalRiteAllyService.markers(recipe).get(CardinalRiteAllyRole.ANCHOR));
			helper.assertTrue(BloodCraftingKeyPressPacket.tryStartCardinalRite(player, origin.above(),
					CardinalRiteActivationRules.Trigger.LIVING_STAFF_BLOCK_USE)
					== CardinalRiteActivationRules.ActivationAttempt.STARTED,
					"Prepared Archon rite must start before helper assignment");
			helper.getLevel().setBlock(station, Blocks.STONE.defaultBlockState(), 3);
			helper.assertTrue(!HemoJourneyWorldState.assignSwornHelper(player, origin.above(), recipe.getId()),
					"Blocked helper station must reject assignment");
			helper.getLevel().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
			helper.assertTrue(HemoJourneyWorldState.assignSwornHelper(player, origin.above(), recipe.getId()),
					"The same rite must accept its sworn helper after the station clears");
			var rite = CardinalRiteSavedData.get(helper.getLevel()).getRite(player.getUUID());
			BlockPos attendant = origin.above().offset(CardinalRiteAllyService.markers(recipe)
					.get(CardinalRiteAllyRole.ATTENDANT));
			var oldSupport = helper.getLevel().getBlockState(attendant.below());
			var oldStation = helper.getLevel().getBlockState(attendant);
			var oldHeadroom = helper.getLevel().getBlockState(attendant.above());
			try {
				helper.getLevel().setBlock(attendant.below(), Blocks.STONE.defaultBlockState(), 3);
				helper.getLevel().setBlock(attendant, Blocks.AIR.defaultBlockState(), 3);
				helper.getLevel().setBlock(attendant.above(), Blocks.AIR.defaultBlockState(), 3);
				helper.assertTrue(HemoJourneyWorldState.assignSwornHelper(player, origin.above(), recipe.getId())
						&& rite.getAllyRoles().containsValue(CardinalRiteAllyRole.ANCHOR),
						"A second assist must keep the available helper at its assigned Anchor station");
			} finally {
				helper.getLevel().setBlock(attendant.below(), oldSupport, 3);
				helper.getLevel().setBlock(attendant, oldStation, 3);
				helper.getLevel().setBlock(attendant.above(), oldHeadroom, 3);
			}
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARCHON_RITE.id(), origin);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.ARCHON_RITE, origin).passed(),
					"Resumed Archon rite must finish with the available helper");
			helper.succeed();
		} finally {
			if (station != null) helper.getLevel().setBlock(station, Blocks.AIR.defaultBlockState(), 3);
			HemoJourneyFixtures.cleanup(player, origin);
			CardinalRiteSavedData.get(helper.getLevel()).removeRite(player.getUUID());
			BloodlineSavedData.get(helper.getLevel().getServer().overworld()).disbandBloodline(lineId);
			player.discard();
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 180,
			batch = "journeyAutomationD7Upgrade")
	public static void automaticEdaciousUpgradeTargetsBootsAfterFullBloodLustFitting(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(7);
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
					new ItemStack(ItemInit.blood_lust_helm.get()));
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,
					new ItemStack(ItemInit.blood_lust_chest.get()));
			player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS,
					new ItemStack(ItemInit.blood_lust_legs.get()));
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_D7_UPGRADE, origin);
			helper.assertTrue(player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).isEmpty()
					&& player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS).isEmpty(),
					"Edacious fixture must leave Boots as the only eligible Armature piece");
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_D7_UPGRADE.id(), origin);
			helper.runAfterDelay(105, () -> {
				try {
					helper.assertTrue(HemoJourneyChecks.verify(player,
							HemoJourneyStage.ARTIFICER_D7_UPGRADE, origin).passed(),
							"The Armature must produce Edacious Blood Lust Boots for the journey");
					helper.succeed();
				} finally {
					HemoJourneyFixtures.cleanup(player, origin);
					setServerPlayerLookup(player, false);
					player.discard();
				}
			});
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
			throw exception;
		}
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationBarbedResearch")
	public static void barbedResearchResumesAfterFirstRecordedSpecimen(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		BlockPos origin = helper.absolutePos(new BlockPos(14, 3, 14));
		try {
			setServerPlayerLookup(player, true);
			HemoCapabilityAccess.requireInitiatoryDegree(player).setDegreeNumber(3);
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.ARTIFICER_BARBED_RESEARCH, origin);
			HemoCapabilityAccess.requireSpecimenBestiary(player).recordSpecimen(Hemomancy.rloc("barbed_urchin"));
			player.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
					HemoJourneyFixtures.bounds(origin), mob -> mob.getType()
							== com.vincenthuto.hemomancy.common.init.EntityInit.barbed_urchin.get())
							.forEach(net.minecraft.world.entity.Entity::discard);
			HarbingerJourneyAutomation.perform(player, HemoJourneyStage.ARTIFICER_BARBED_RESEARCH.id(), origin);
			var bestiary = HemoCapabilityAccess.requireSpecimenBestiary(player);
			helper.assertTrue(HemoJourneyChecks.verify(player, HemoJourneyStage.ARTIFICER_BARBED_RESEARCH,
					origin).passed(), "Resumed Barbed research must record the two remaining real specimens: "
						+ bestiary.serializeNBT(player.registryAccess()));
			helper.succeed();
		} finally {
			HemoJourneyFixtures.cleanup(player, origin);
			setServerPlayerLookup(player, false);
			player.discard();
		}
	}

	private static boolean hasItem(ServerPlayer player, net.minecraft.world.item.Item item) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			if (player.getInventory().getItem(slot).is(item)) return true;
		}
		return false;
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
			throw new IllegalStateException("Could not register the automatic journey GameTest player", exception);
		}
	}

	private static ServerPlayer connectedPlayer(GameTestHelper helper) {
		return connectedPlayer(helper, packet -> { });
	}

	private static ServerPlayer connectedPlayer(GameTestHelper helper, Consumer<Packet<?>> outbound) {
		CommonListenerCookie cookie = CommonListenerCookie.createInitial(
				new GameProfile(UUID.randomUUID(), "journey-auto-player"), false);
		ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
				cookie.gameProfile(), ClientInformation.createDefault());
		Connection connection = new Connection(PacketFlow.SERVERBOUND);
		new EmbeddedChannel(connection);
		new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie) {
			@Override public void send(Packet<?> packet) { outbound.accept(packet); }
		};
		BlockPos start = helper.absolutePos(new BlockPos(14, 3, 14));
		player.teleportTo(helper.getLevel(), start.getX() + 0.5D, start.getY(), start.getZ() + 0.5D, 0.0F, 0.0F);
		player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				helper.getLevel().dimension().location().toString());
		return player;
	}

	private static boolean opensJourneyScreen(Packet<?> packet) {
		if (!(packet instanceof ClientboundCustomPayloadPacket custom)) return false;
		return custom.payload() instanceof OpenDialoguePacket || custom.payload() instanceof OpenInscriptionPacket;
	}
}
