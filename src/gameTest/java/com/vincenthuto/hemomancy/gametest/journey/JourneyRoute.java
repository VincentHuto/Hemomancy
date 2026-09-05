package com.vincenthuto.hemomancy.gametest.journey;

import net.minecraft.server.level.ServerPlayer;

public final class JourneyRoute {
	public static final String KEY = "hemomancy.dev_test.journey.route";
	public static final String HARBINGER = "harbinger";
	public static final String UNSTAINED = "unstained";
	public static final String CIRCUS = "circus";

	private JourneyRoute() { }

	public static boolean is(ServerPlayer player, String route) {
		return route.equals(player.getPersistentData().getString(KEY));
	}
}
