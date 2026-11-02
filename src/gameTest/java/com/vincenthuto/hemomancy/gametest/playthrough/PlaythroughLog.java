package com.vincenthuto.hemomancy.gametest.playthrough;

import com.mojang.logging.LogUtils;
import net.minecraft.gametest.framework.GameTestHelper;
import org.slf4j.Logger;

/**
 * One line per observation in the server log, {@code [playthrough] <scenario>: <text>}, so the runner can quote what
 * a decision probe saw whether it passed or failed.
 */
public final class PlaythroughLog {
	private static final Logger LOGGER = LogUtils.getLogger();

	private PlaythroughLog() {
	}

	public static void note(GameTestHelper helper, String scenario, String text) {
		LOGGER.info("[playthrough] {}: {}", scenario, text);
	}

	/** The same line, naming the running test when no scenario name is at hand. */
	public static void note(GameTestHelper helper, String text) {
		note(helper, helper.testInfo.getTestName(), text);
	}
}
