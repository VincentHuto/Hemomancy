package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch;
import com.vincenthuto.hemomancy.common.event.AntecedentEvents;
import com.vincenthuto.hemomancy.common.network.AntecedentEffectPacket;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.vincenthuto.hemomancy.common.capability.PlayerStateSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.BloodRoutingSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing.DirectBloodLinkData;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerHermitDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodFlowDiagnostics;
import com.vincenthuto.hemomancy.common.network.dialogue.DialogueOptionPacket;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** Server-side hardening from the 2026-11-02 cleanup audit: rows A-5 to A-8 of its ledger. */
@GameTestHolder("hardening_validation")
@PrefixGameTestTemplate(false)
public final class HardeningGameTests {
	/** A-8 (RT-2): a saved link in an unloaded chunk must not pull that chunk in every routing tick. */
	@GameTest(template = "empty")
	public static void routingTickLeavesUnloadedLinksAlone(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos far = helper.absolutePos(BlockPos.ZERO).offset(100_000, 0, 100_000);
		ChunkPos chunk = new ChunkPos(far);
		helper.assertTrue(!level.getChunkSource().hasChunk(chunk.x, chunk.z), "the far chunk was already loaded");
		BloodRoutingSavedData.get(level).setLink(far, DirectBloodLinkData.nearby(UUID.randomUUID()));
		helper.onEachTick(() -> {
			if (level.getChunkSource().hasChunk(chunk.x, chunk.z)) {
				BloodRoutingSavedData.get(level).removeLink(far);
				helper.fail("the routing tick loaded an unloaded link's chunk");
			}
		});
		helper.runAfterDelay(BloodRoutingHelper.ROUTE_INTERVAL_TICKS * 2 + 2, () -> {
			BloodRoutingSavedData.get(level).removeLink(far);
			helper.succeed();
		});
	}

	/**
	 * A-7 (RT-1): two blood changes in one tick send one volume packet with its diagnostics at the flush; a change in
	 * the next tick sends the volume again but not the diagnostics, which repeat at most once a second.
	 */
	@GameTest(template = "empty")
	public static void bloodSyncWaitsForTheEndOfTickFlush(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var volume = HemoCapabilityAccess.getBloodVolume(player);
			volume.setActive(true);
			volume.setBloodVolume(1000.0D);
			helper.assertTrue(!volume.isSyncDirty(), "a fresh volume is already dirty");
			BloodVolumeEvents.syncVolume(player, volume);
			BloodVolumeEvents.syncVolume(player, volume);
			helper.assertTrue(volume.isSyncDirty(), "syncVolume did not mark the volume dirty");
			helper.assertTrue(captured.sent(BloodVolumeServerPacket.class).isEmpty(), "syncVolume sent before the flush");
			PlayerStateSync.flushDirty(player);
			helper.assertTrue(!volume.isSyncDirty(), "the flush did not clear the dirty flag");
			helper.assertTrue(captured.sent(BloodVolumeServerPacket.class).size() == 1, "two changes sent more than one volume packet");
			helper.assertTrue(captured.sent(PacketSyncBloodFlowDiagnostics.class).size() == 1, "the first flush did not send diagnostics");
			captured.payloads.clear();
			BloodVolumeEvents.syncVolume(player, volume);
			PlayerStateSync.flushDirty(player);
			helper.assertTrue(captured.sent(BloodVolumeServerPacket.class).size() == 1, "a later change did not send the volume");
			helper.assertTrue(captured.sent(PacketSyncBloodFlowDiagnostics.class).isEmpty(), "diagnostics repeated within a second");
		}
		helper.succeed();
	}

	/** A-5 (RT-18): a memo id that is not defined is refused. */
	@GameTest(template = "empty")
	public static void forgedMemoIsNotNoted(GameTestHelper helper) {
		ServerPlayer player = mockServerPlayer(helper);
		DialogueOptionPacket.accept(player, "memo_capture:hemomancy:libersanguinium/the_infection/pages/the_truth", 0);
		helper.assertTrue(HemoCapabilityAccess.getLiberKnowledge(player).getPendingMemos().isEmpty(),
				"a forged memo id was noted");
		helper.succeed();
	}

	/** A-5 (RT-18): a defined memo that no open whisper or nearby speaker offers is refused. */
	@GameTest(template = "empty")
	public static void unofferedMemoIsNotNoted(GameTestHelper helper) {
		ServerPlayer player = mockServerPlayer(helper);
		DialogueOptionPacket.accept(player, "memo_capture:" + MemoDefinitions.FUNGAL_WHISPER_TRUTH.id(), 0);
		DialogueOptionPacket.accept(player, "memo_capture:" + MemoDefinitions.FIRST_RITE_NOTES.id(), 0);
		helper.assertTrue(HemoCapabilityAccess.getLiberKnowledge(player).getPendingMemos().isEmpty(),
				"an unoffered memo was noted");
		helper.succeed();
	}

	/** A-5 (RT-18): the open-ended {@code liber_unlock:<entry>} event no longer unlocks arbitrary Liber entries. */
	@GameTest(template = "empty")
	public static void liberUnlockPrefixDoesNotUnlockEntries(GameTestHelper helper) {
		ServerPlayer player = mockServerPlayer(helper);
		DialogueOptionPacket.accept(player, "liber_unlock:" + LiberEntryDefinitions.TRUTH, 0);
		helper.assertTrue(!HemoCapabilityAccess.getLiberKnowledge(player).hasEntry(LiberEntryDefinitions.TRUTH),
				"a client-named Liber entry was unlocked");
		helper.succeed();
	}

	/** A-5 control: the Hermit's own memo, chosen with the Hermit in reach, is still noted. */
	@GameTest(template = "empty")
	public static void hermitMemoInReachIsNoted(GameTestHelper helper) {
		ServerPlayer player = mockServerPlayer(helper);
		BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
		player.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		var hermit = helper.spawn(EntityInit.harbinger_hermit.get(), new BlockPos(2, 1, 1));
		// The Hermit's neophyte tree offers the first-rite memo; sending it records the offer.
		PacketHandler.sendToPlayer(player, new OpenDialoguePacket(
				HarbingerHermitDialogueTrees.forDegree(1, true, hermit.getId())));
		DialogueOptionPacket.accept(player, "memo_capture:" + MemoDefinitions.FIRST_RITE_NOTES.id(), hermit.getId());
		helper.assertTrue(HemoCapabilityAccess.getLiberKnowledge(player).getPendingMemos()
				.contains(MemoDefinitions.FIRST_RITE_NOTES.id()), "the Hermit's memo was refused");
		helper.succeed();
	}

	/** A-6 (RT-19): a player who may not build cannot place a Blood Lamp light. */
	/** A-6 control: a survival player still places the light. */
	/** A-6 (RT-19): a player who may not break blocks cannot excavate them. */
	/** A-26 (DC-58): the Antecedent epilogue is the hive speaking; a Silentium, cut from the hive, never hears it. */
	@GameTest(template = "empty")
	public static void silentiumDoesNotHearTheAntecedentEpilogue(GameTestHelper helper) {
		helper.assertTrue(epilogueReaches(helper, EnumArchonPath.APOTHEOS), "an Apotheos no longer hears the epilogue");
		helper.assertTrue(!epilogueReaches(helper, EnumArchonPath.SILENTIUM), "a Silentium heard the hive's epilogue");
		helper.succeed();
	}

	private static boolean epilogueReaches(GameTestHelper helper, EnumArchonPath path) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
			degree.setDegreeNumber(8);
			degree.setArchonPath(path);
			HemoCapabilityAccess.getBloodVolume(player).setActive(true);
			AntecedentKnowledge.record(player, AntecedentResearch.Evidence.ARCHIVE_RESPONSE);
			AntecedentKnowledge.record(player, AntecedentResearch.Evidence.VICAR_RECOGNITION);
			captured.payloads.clear();
			AntecedentEvents.epilogue(new PlayerTickEvent.Post(player));
			return !captured.sent(AntecedentEffectPacket.class).isEmpty();
		}
	}

	private static ServerPlayer mockServerPlayer(GameTestHelper helper) {
		return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "hardening"));
	}

	/** A mock server player at (1, 1, 1) looking south at a stone wall two blocks away; its eye-height ray hits (1, 2, 3). */
	private static ServerPlayer facingWall(GameTestHelper helper, GameType gameType) {
		for (int y = 1; y <= 2; y++) {
			for (int x = 0; x <= 2; x++) helper.setBlock(new BlockPos(x, y, 3), Blocks.STONE);
		}
		ServerPlayer player = mockServerPlayer(helper);
		player.setGameMode(gameType);
		BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
		player.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setBloodVolume(5000.0D);
		return player;
	}
}
