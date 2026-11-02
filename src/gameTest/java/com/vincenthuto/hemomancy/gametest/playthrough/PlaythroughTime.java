package com.vincenthuto.hemomancy.gametest.playthrough;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;

/**
 * ARRANGE only: server-wide time and difficulty, each put back when the test ends. Everything here changes the whole
 * server, so a test that uses it runs in a batch of its own (the {@code _clock} batches).
 */
public final class PlaythroughTime {
	private PlaythroughTime() {
	}

	/**
	 * Moves the overworld's game time on by {@code ticks} (a Champion's day-long wait, a rest cooldown, a vacancy),
	 * without ticking anything in between. When the test ends the skip is taken back off, so the clock keeps only the
	 * ticks that really ran.
	 */
	public static void skipGameTime(GameTestHelper helper, long ticks) {
		MinecraftServer server = helper.getLevel().getServer();
		var data = server.getWorldData().overworldData();
		data.setGameTime(data.getGameTime() + ticks);
		PlaythroughLog.note(helper, "ARRANGE skipped " + ticks + " ticks of game time");
		PlaythroughCleanup.onEnd(helper, () -> data.setGameTime(Math.max(0L, data.getGameTime() - ticks)));
	}

	/** Sets the overworld's time of day to {@code dayTime}; the old time of day is put back when the test ends. */
	public static void setDayTime(GameTestHelper helper, long dayTime) {
		var overworld = helper.getLevel().getServer().overworld();
		long before = overworld.getDayTime();
		overworld.setDayTime(dayTime);
		overworld.updateSkyBrightness();
		PlaythroughCleanup.onEnd(helper, () -> {
			overworld.setDayTime(before);
			overworld.updateSkyBrightness();
		});
	}

	/** Sets the server difficulty (Peaceful for the practical probes); the old one is put back when the test ends. */
	public static void difficulty(GameTestHelper helper, Difficulty difficulty) {
		MinecraftServer server = helper.getLevel().getServer();
		Difficulty before = server.getWorldData().getDifficulty();
		server.setDifficulty(difficulty, true);
		PlaythroughCleanup.onEnd(helper, () -> server.setDifficulty(before, true));
	}
}
