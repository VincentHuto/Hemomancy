package com.vincenthuto.hemomancy.gametest.playthrough;

import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.gametest.framework.GameTestRunner;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Work that must run when a test ends however it ends: passed, failed, or timed out. Hooks run newest first, each on
 * its own, so one failing hook does not leave the rest undone. Players, spawned NPCs, rites and fixture blocks all
 * register here when they are made.
 */
public final class PlaythroughCleanup {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<GameTestInfo, List<Runnable>> HOOKS = new IdentityHashMap<>();

	private PlaythroughCleanup() {
	}

	/** Runs {@code hook} when the test ends. */
	public static synchronized void onEnd(GameTestHelper helper, Runnable hook) {
		GameTestInfo info = helper.testInfo;
		List<Runnable> hooks = HOOKS.get(info);
		if (hooks == null) {
			hooks = new ArrayList<>();
			HOOKS.put(info, hooks);
			info.addListener(new GameTestListener() {
				@Override public void testStructureLoaded(GameTestInfo test) { }
				@Override public void testPassed(GameTestInfo test, GameTestRunner runner) { runAll(test); }
				@Override public void testFailed(GameTestInfo test, GameTestRunner runner) { runAll(test); }
				@Override public void testAddedForRerun(GameTestInfo oldTest, GameTestInfo newTest, GameTestRunner runner) { }
			});
		}
		hooks.add(hook);
	}

	/**
	 * Registers the world clean-up for a test player: whatever their play left in level-wide saved data, keyed by
	 * their UUID. When the test ends, every bloodline they lead is disbanded and they leave every other line; every
	 * Fane they own is removed with its Bloodwell heart and Hematic Stakes; a rite still running for them is cleared
	 * with its threats, and rite recovery owed to them dropped; an initiation ceremony is cancelled; Qliphoth Blooms
	 * they own are taken down with their saved entries and bound-pome returns; and Circus pavilion sites they hold are
	 * reset. {@code PlaythroughPlayer.create} calls this for every player; tests need not.
	 */
	static void worldOwnedBy(GameTestHelper helper, PlaythroughPlayer player) {
		onEnd(helper, () -> clearWorldOwnedBy(helper.getLevel().getServer(), player));
	}

	private static void clearWorldOwnedBy(net.minecraft.server.MinecraftServer server, PlaythroughPlayer player) {
		java.util.UUID id = player.uuid();
		var overworld = server.overworld();
		var lines = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData.get(overworld);
		for (var line : List.copyOf(lines.getAllBloodlines().values())) {
			if (id.equals(line.getLeaderUUID())) lines.disbandBloodline(line.getBloodlineUUID());
			else if (line.hasMember(id)) lines.removeMember(line.getBloodlineUUID(), id);
		}
		for (var level : server.getAllLevels()) {
			var fanes = com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(level);
			var footprint = fanes.getAllFootprints().get(id);
			if (footprint != null) {
				for (var stake : footprint.stakes()) {
					if (level.getBlockState(stake).is(com.vincenthuto.hemomancy.common.init.BlockInit.hematic_stake.get())) {
						level.setBlock(stake, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
					}
				}
				if (footprint.heart() != null && level.getBlockState(footprint.heart())
						.is(com.vincenthuto.hemomancy.common.init.BlockInit.consecrated_bloodwell.get())) {
					level.setBlock(footprint.heart(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
				}
			}
			fanes.remove(id);
			var rites = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(level);
			var rite = rites.getRite(id);
			if (rite != null) {
				com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.clearThreats(level, rite);
				rites.removeRite(id);
			}
			dropRecovery(rites, id);
			com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData.get(level).resetOwned(id);
		}
		com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.cancel(player.player());
		var blooms = com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData.get(overworld);
		for (var bloom : List.copyOf(blooms.getBlooms())) {
			if (!id.equals(bloom.ownerUUID())) continue;
			for (var level : server.getAllLevels()) {
				if (!level.dimension().location().toString().equals(bloom.dimension())) continue;
				for (int up = 0; up <= 7; up++) {
					var pos = bloom.center().above(up);
					var state = level.getBlockState(pos);
					if (state.is(com.vincenthuto.hemomancy.common.init.BlockInit.qliphoth_bloom.get())
							|| state.is(com.vincenthuto.hemomancy.common.init.BlockInit.filler_block.get())) {
						level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
			blooms.removeBloomInChunk(bloom.center(), bloom.dimension());
		}
		blooms.takePendingBoundPomeReturns(id);
		com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents.syncQliphothBlooms(server);
	}

	@SuppressWarnings("unchecked")
	private static void dropRecovery(com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData rites, java.util.UUID id) {
		try {
			var field = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.class.getDeclaredField("recovery");
			field.setAccessible(true);
			if (((Map<java.util.UUID, ?>) field.get(rites)).remove(id) != null) rites.setDirty();
		} catch (ReflectiveOperationException exception) {
			LOGGER.warn("[playthrough] could not clear rite recovery for {}", id, exception);
		}
	}

	/** Runs the hooks now (a test that ends synchronously may call this before {@code helper.succeed()}). */
	public static void runNow(GameTestHelper helper) {
		runAll(helper.testInfo);
	}

	private static void runAll(GameTestInfo info) {
		List<Runnable> hooks;
		synchronized (PlaythroughCleanup.class) {
			hooks = HOOKS.remove(info);
		}
		if (hooks == null) return;
		for (int i = hooks.size() - 1; i >= 0; i--) {
			try {
				hooks.get(i).run();
			} catch (RuntimeException exception) {
				LOGGER.warn("[playthrough] cleanup for {} failed", info.getTestName(), exception);
			}
		}
	}
}
