package com.vincenthuto.hemomancy.gametest.playthrough;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * For a scenario the GameTest server cannot host (the Chamber of Will, the Fungal Gardens, seeded worldgen): the test
 * fails with {@code NOT RUN: <reason>} and logs the proof still owed, so it never passes silently. Every test that
 * can end here is an optional GameTest ({@code required = false}). A test that runs a degraded form on this server (the first
 * Spine projection resolving in one tick) logs that it is degraded instead and is not a NOT RUN.
 */
public final class PlaythroughNotRun {
	/** The review clients and validation suites that prove what this server cannot. */
	public static final String CHAMBER = "chamberReviewClient";
	public static final String HEART_NODE = "heartNodeReviewClient";
	public static final String VESPER = "vesperReviewClient";
	public static final String MYCOPHANT = "mycophantReviewClient";
	public static final String POSSESSION = "possessionReviewClient";
	public static final String COURT_WORLDGEN = "court_worldgen_validation";
	public static final String BRINED_SEAT = "brinedSeatReviewClient";
	public static final String BRINED_OUTPOST = "brinedOutpostReviewClient";
	public static final String COURT_NPC = "courtNpcReviewClient";
	public static final String MAUSOLEUM = "mausoleumReviewClient";

	private PlaythroughNotRun() {
	}

	/**
	 * The level {@code dimension} when this server has it loaded; otherwise fails the test as
	 * {@code NOT RUN: <reason>} (and returns null, which the failure never lets the caller reach).
	 */
	public static ServerLevel require(GameTestHelper helper, ResourceKey<Level> dimension, String reason, String... proofOwed) {
		ServerLevel level = helper.getLevel().getServer().getLevel(dimension);
		if (level == null) notRun(helper, reason + " (" + dimension.location() + " is not loaded here)", proofOwed);
		return level;
	}

	/** Fails the test as {@code NOT RUN: <reason>}, logging the proof owed. */
	public static void notRun(GameTestHelper helper, String reason, String... proofOwed) {
		String owed = proofOwed.length == 0 ? "a client campaign" : String.join(", ", proofOwed);
		PlaythroughLog.note(helper, "NOT RUN: " + reason + "; proof owed: " + owed);
		helper.fail("NOT RUN: " + reason + "; proof owed: " + owed);
	}

	/** Logs that this run is a degraded form of the real one (it still runs and asserts what the server can see). */
	public static void degraded(GameTestHelper helper, String what) {
		PlaythroughLog.note(helper, "DEGRADED: " + what);
	}
}
