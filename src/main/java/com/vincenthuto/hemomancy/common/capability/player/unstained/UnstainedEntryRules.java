package com.vincenthuto.hemomancy.common.capability.player.unstained;

/**
 * Who may still take the Unstained cure. Founding a bloodline closes it until the Severed Covenant
 * cuts the founder's integration; Degree alone never does.
 */
public final class UnstainedEntryRules {
	private UnstainedEntryRules() {}

	public static boolean canBeginCure(boolean hasFoundedBloodline, boolean founderIntegrationSevered) {
		return !hasFoundedBloodline || founderIntegrationSevered;
	}

	public static boolean canSuppressForCure(boolean activeBlood,
			boolean hasFoundedBloodline, boolean founderIntegrationSevered) {
		return activeBlood && canBeginCure(hasFoundedBloodline, founderIntegrationSevered);
	}
}
