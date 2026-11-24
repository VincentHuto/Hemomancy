package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.manipulation.mortem.BlackheartedEvents;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncChamberOfWill;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncVesperFightScene;
import com.vincenthuto.hemomancy.common.network.particle.ManipulationVisualPacket;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteSync;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * FX-14 (RT-7): the periodic broadcasts send a player their state once, then only when it changes. A move to another
 * level or a new player object (a login or a respawn) always gets a full send.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PeriodicBroadcastGameTests {
	private PeriodicBroadcastGameTests() {
	}

	/**
	 * An idle player, alone in its batch so no fane changes around it: one boundary list over 200 ticks (five passes),
	 * then one more after a move to another level.
	 */
	@GameTest(template = "empty", timeoutTicks = 400, batch = "periodic_broadcast_fane")
	public static void faneBoundariesSendOnceThenOnlyOnChange(GameTestHelper helper) {
		ServerLevel nether = helper.getLevel().getServer().getLevel(Level.NETHER);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		captured.player.setPos(helper.absoluteVec(new Vec3(1.5D, 2.0D, 1.5D)));
		captured.joinLevel();
		helper.runAfterDelay(200, () -> {
			int idle = captured.sent(PacketSyncFaneBoundaries.class).size();
			if (idle != 1) {
				captured.close();
				helper.fail("200 idle ticks sent " + idle + " fane boundary lists, not 1");
				return;
			}
			captured.leaveLevel();
			captured.player.setServerLevel(nether);
			captured.joinLevel();
		});
		helper.runAfterDelay(260, () -> {
			int total = captured.sent(PacketSyncFaneBoundaries.class).size();
			captured.close();
			helper.assertTrue(total == 2, "a level change sent " + (total - 1) + " fane boundary lists, not 1");
			helper.succeed();
		});
	}

	/** An unlit Black Heart is sent once over ten passes; a new player object gets it again. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_black_heart")
	public static void unlitBlackHeartIsSentOnce(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper);
				CapturedPlayer respawned = CapturedPlayer.create(helper, captured.player.getGameProfile())) {
			captured.player.setPos(helper.absoluteVec(new Vec3(1.5D, 2.0D, 1.5D)));
			respawned.player.setPos(captured.player.position());
			captured.joinLevel();
			for (int pass = 0; pass < 10; pass++) BlackheartedEvents.onPlayerTick(new PlayerTickEvent.Post(captured.player));
			int idle = blackHearts(captured, captured.player);
			helper.assertTrue(idle == 1, "ten idle passes sent " + idle + " Black Heart cues, not 1");
			captured.leaveLevel();
			respawned.joinLevel();
			BlackheartedEvents.onPlayerTick(new PlayerTickEvent.Post(respawned.player));
			BlackheartedEvents.onPlayerTick(new PlayerTickEvent.Post(respawned.player));
			int fresh = blackHearts(respawned, respawned.player);
			helper.assertTrue(fresh == 1, "a new player object got " + fresh + " Black Heart cues, not 1");
			helper.succeed();
		}
	}

	/** The Chamber's 40-tick pass sends its two payloads once, then again only after a change. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_chamber")
	public static void chamberStateSendsOnceThenOnlyOnChange(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			ChamberOfWillManager manager = ChamberOfWillManager.get(helper.getLevel().getServer());
			for (int pass = 0; pass < 10; pass++) manager.syncIfChanged(player);
			helper.assertTrue(captured.sent(PacketSyncChamberOfWill.class).size() == 1
					&& captured.sent(PacketSyncVesperFightScene.class).size() == 1,
					"ten idle passes sent " + captured.sent(PacketSyncChamberOfWill.class).size() + " Chamber states");
			player.setServerLevel(helper.getLevel().getServer().getLevel(Level.NETHER));
			manager.syncIfChanged(player);
			manager.syncIfChanged(player);
			helper.assertTrue(captured.sent(PacketSyncChamberOfWill.class).size() == 2,
					"a level change sent " + (captured.sent(PacketSyncChamberOfWill.class).size() - 1) + " Chamber states, not 1");
			helper.succeed();
		}
	}

	/** A lit Black Heart refreshes on every pass, since its cue lasts 25 ticks; going dark sends one last cue. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_black_heart_lit")
	public static void litBlackHeartRefreshesEveryPass(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setPos(helper.absoluteVec(new Vec3(1.5D, 2.0D, 1.5D)));
			captured.joinLevel();
			var known = HemoCapabilityAccess.getKnownManipulations(player);
			known.setEquippedManipNames(List.of("blackhearted"));
			known.togglePassive("blackhearted");
			HemoCapabilityAccess.getBloodVolume(player).setActive(true);
			HemoCapabilityAccess.getBloodTemperament(player).setTemperAlignment(EnumBloodTemper.MORTEM, 35);
			HemoCapabilityAccess.getPowerGuardrails(player).setNecroticSaturation(4.0F);
			for (int pass = 0; pass < 5; pass++) BlackheartedEvents.onPlayerTick(new PlayerTickEvent.Post(player));
			int lit = blackHearts(captured, player);
			helper.assertTrue(lit == 5, "five lit passes sent " + lit + " Black Heart cues, not 5");
			HemoCapabilityAccess.getPowerGuardrails(player).setNecroticSaturation(0.0F);
			for (int pass = 0; pass < 5; pass++) BlackheartedEvents.onPlayerTick(new PlayerTickEvent.Post(player));
			int dark = blackHearts(captured, player) - lit;
			helper.assertTrue(dark == 1, "going dark sent " + dark + " Black Heart cues, not 1");
			helper.succeed();
		}
	}

	/**
	 * A fight scene sent outside the 40-tick pass (the Vesper ordeal's own sends) makes the next pass resend the whole
	 * Chamber state, so the two cannot leave the client disagreeing.
	 */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_chamber_scene")
	public static void directFightSceneMakesThePassResend(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			ChamberOfWillManager manager = ChamberOfWillManager.get(helper.getLevel().getServer());
			manager.syncIfChanged(player);
			manager.syncIfChanged(player);
			ChamberOfWillManager.sendScene(player, PacketSyncVesperFightScene.activate(player.blockPosition()));
			manager.syncIfChanged(player);
			manager.syncIfChanged(player);
			int states = captured.sent(PacketSyncChamberOfWill.class).size();
			int scenes = captured.sent(PacketSyncVesperFightScene.class).size();
			helper.assertTrue(states == 2 && scenes == 3,
					"after a direct scene the pass sent " + states + " Chamber states and " + scenes + " scenes, not 2 and 3");
			helper.succeed();
		}
	}

	/**
	 * FX-14, rite sync: a viewer sees a running rite within its view range, a far viewer does not, and the rite's
	 * caster sees it from anywhere in the level.
	 */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_rite_range")
	public static void riteSyncReachesOnlyNearViewersAndTheCaster(GameTestHelper helper) {
		try (CapturedPlayer viewer = CapturedPlayer.create(helper); CapturedPlayer caster = CapturedPlayer.create(helper)) {
			BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
			ActiveCardinalRite rite = new ActiveCardinalRite(caster.player.getUUID(), center,
					Hemomancy.rloc("cardinal_rite/still_waters"), 200, 3);
			viewer.player.setPos(Vec3.atCenterOf(center).add(20.0D, 0.0D, 0.0D));
			helper.assertTrue(CardinalRiteSync.sees(rite, viewer.player), "a viewer 20 blocks off does not see the rite");
			viewer.player.setPos(Vec3.atCenterOf(center).add(CardinalRiteSync.VIEW_RANGE + 8.0D, 0.0D, 0.0D));
			helper.assertFalse(CardinalRiteSync.sees(rite, viewer.player), "a viewer past the view range still sees the rite");
			caster.player.setPos(viewer.player.position());
			helper.assertTrue(CardinalRiteSync.sees(rite, caster.player), "the caster lost sight of its own rite");
			helper.succeed();
		}
	}

	/** FX-14: the rite list goes to a viewer again only when what it would show has changed. */
	@GameTest(template = "empty", timeoutTicks = 40, batch = "periodic_broadcast_rite")
	public static void riteSyncSendsAnUnchangedListOnce(GameTestHelper helper) {
		try (CapturedPlayer viewer = CapturedPlayer.create(helper)) {
			ServerLevel level = helper.getLevel();
			BlockPos center = helper.absolutePos(new BlockPos(1, 1, 1));
			viewer.player.setPos(Vec3.atCenterOf(center).add(4.0D, 0.0D, 0.0D));
			viewer.joinLevel();
			ActiveCardinalRite rite = new ActiveCardinalRite(java.util.UUID.randomUUID(), center,
					Hemomancy.rloc("cardinal_rite/still_waters"), 200, 3);
			var saved = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
			saved.startRite(rite);
			try {
				int before = viewer.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites.class).size();
				CardinalRiteSync.syncActiveRites(level);
				int first = viewer.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites.class).size();
				CardinalRiteSync.syncActiveRites(level);
				int second = viewer.sent(com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncActiveRites.class).size();
				helper.assertTrue(first == before + 1, "a list with a new rite in it was not sent");
				helper.assertTrue(second == first, "the same list was sent again");
			} finally {
				saved.removeRite(rite.getPlayerUUID());
			}
			helper.succeed();
		}
	}

	private static int blackHearts(CapturedPlayer viewer, ServerPlayer owner) {
		return (int) viewer.sent(ManipulationVisualPacket.class).stream()
				.filter(packet -> packet.form() == ManipulationVisuals.Form.BLACK_HEART && packet.entityId() == owner.getId())
				.count();
	}
}
