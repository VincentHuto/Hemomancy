package com.vincenthuto.hemomancy.common.mission.unstained;

public final class UnstainedDaggerReplacementRules {
	private UnstainedDaggerReplacementRules() {}

	public static boolean canExchange(boolean pledged, boolean alreadyHasDagger, int paleSilver, int shards) {
		return pledged && !alreadyHasDagger && paleSilver >= 2 && shards >= 1;
	}
}
