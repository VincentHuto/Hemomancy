package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/**
 * The waking: the player comes to bound in the Nursery while she tends a fungling with her back turned, notices,
 * turns, lets it fall, roars and rushes. Rematches skip the waking: she turns, roars and rushes.
 */
public final class MycophantIntroRules {
	public enum Beat { BOUND, FIBERS_SNAP, NOTICE, TURN, DROP, CONTROL, ROAR, RUSH, DONE }

	private MycophantIntroRules() {
	}

	/** The tick a beat starts, or -1 when that beat is skipped. */
	public static int tickOf(Beat beat, boolean rematch) {
		if (rematch) {
			return switch (beat) {
				case NOTICE, CONTROL -> 0;
				case TURN -> 4;
				case ROAR -> 20;
				case RUSH -> 60;
				case DONE -> 61;
				default -> -1;
			};
		}
		return switch (beat) {
			case BOUND -> 0;
			case FIBERS_SNAP -> 75;
			case NOTICE -> 85;
			case TURN -> 95;
			case DROP -> 120;
			case CONTROL -> 130;
			case ROAR -> 150;
			case RUSH -> 190;
			case DONE -> 191;
		};
	}

	public static Beat beatAt(int tick, boolean rematch) {
		Beat current = rematch ? Beat.NOTICE : Beat.BOUND;
		for (Beat beat : Beat.values()) {
			int start = tickOf(beat, rematch);
			if (start >= 0 && tick >= start) current = beat;
		}
		return current;
	}

	/** The boss theme starts with her roar, not during the waking. */
	public static boolean musicPlays(int tick, boolean rematch) {
		return tick >= tickOf(Beat.ROAR, rematch);
	}

	public static boolean bound(int tick, boolean rematch) {
		return !rematch && tick < tickOf(Beat.CONTROL, false);
	}

	public static boolean invulnerable(int tick, boolean rematch) {
		return tick < tickOf(Beat.ROAR, rematch);
	}

	/** How long her turn to face the player takes; she drops the fungling once it is done. */
	public static int turnTicks(boolean rematch) {
		return rematch ? 14 : 24;
	}

	/** Degrees per tick that finish a half turn inside {@link #turnTicks}. */
	public static float turnRateDegrees(boolean rematch) {
		return 180.0F / turnTicks(rematch) + 0.5F;
	}

	/** The dropped fungling's fall from her hand into the nectar, after which it dissolves. */
	public static int dropFallTicks() {
		return 9;
	}

	public static int length(boolean rematch) {
		return tickOf(Beat.RUSH, rematch);
	}
}
