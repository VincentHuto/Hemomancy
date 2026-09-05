package com.vincenthuto.hemomancy.gametest.journey;

import com.mojang.authlib.GameProfile;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.circus.CircusPerformanceController;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.network.discovery.OpenInscriptionPacket;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
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
			for (int tick = 0; tick < 8; tick++) JourneyAutoRunner.tickForTest(player);
			helper.assertTrue(HemoJourneyStage.VESSEL_FILLED.id().equals(
					player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)),
					"The Vicar report must grant the ledger and advance without waiting for a manual NPC interaction. "
							+ JourneyAutoRunner.describe(player) + " Current check: "
							+ HemoJourneyController.status(player).message());
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
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80,
			batch = "journeyAutomationFormation")
	public static void harbingerRunnerProjectsFormationWithoutPlayerInput(GameTestHelper helper) {
		ServerPlayer player = connectedPlayer(helper);
		try {
			helper.assertTrue(JourneyAutoRunner.runHarbinger(player), "Harbinger automation must start");
			for (int tick = 0; tick < 10; tick++) JourneyAutoRunner.tickForTest(player);
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
