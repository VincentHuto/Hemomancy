package com.vincenthuto.hemomancy.client.data;

/**
 * Until when the local player's verb HUD shows a refused claim, in game time ({@code VerbRefusalPacket}). A paid
 * pulse does not clear it early; the server stops refreshing it and it lapses within a cadence.
 */
public final class VerbRefusalClientState {
	private static long refusedUntil = Long.MIN_VALUE;

	private VerbRefusalClientState() {
	}

	public static void refuse(long now, int ticks) {
		refusedUntil = now + Math.max(1, ticks);
	}

	public static boolean isRefused(long now) {
		return now < refusedUntil;
	}

	public static void clear() {
		refusedUntil = Long.MIN_VALUE;
	}
}
