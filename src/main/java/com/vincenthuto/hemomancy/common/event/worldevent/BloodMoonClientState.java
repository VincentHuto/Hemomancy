package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBloodMoon;

/**
 * Client-side singleton holding the current blood moon state.
 * Updated by {@link PacketSyncBloodMoon}.
 */
public class BloodMoonClientState {

	private static volatile boolean active = false;

	public static boolean isActive() {
		return active;
	}

	public static void set(boolean isActive) {
		active = isActive;
	}
}
