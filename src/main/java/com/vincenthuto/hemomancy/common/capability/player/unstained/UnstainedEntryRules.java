package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedEntryRules {
	private UnstainedEntryRules() {}

	public static boolean canBeginCure(boolean hasFoundedBloodline, boolean founderIntegrationSevered) {
		return !hasFoundedBloodline || founderIntegrationSevered;
	}

	public static boolean canSuppressForCure(boolean activeBlood, int degree,
			boolean hasFoundedBloodline, boolean founderIntegrationSevered) {
		return activeBlood && (hasFoundedBloodline ? founderIntegrationSevered : degree <= 5);
	}
}
