package com.vincenthuto.hemomancy.gametest.journey;

public final class HemoJourneyCheckpointRules {
	private HemoJourneyCheckpointRules() { }

	public static boolean craftPassed(boolean outputCaptured, boolean baselineAdvancementIncomplete,
			boolean advancementComplete) {
		return outputCaptured && baselineAdvancementIncomplete && advancementComplete;
	}

	/** A held projection may immediately begin another cycle after completing the first formation. */
	public static boolean formationPassed(double baselineBlood, double currentBlood, boolean outputCaptured) {
		return outputCaptured && baselineBlood > 0.0D
				&& baselineBlood - currentBlood >= 100.0D;
	}

	public static boolean rewardPassed(boolean outputsCaptured, boolean baselineClaimIncomplete,
			boolean claimComplete) {
		return outputsCaptured && baselineClaimIncomplete && claimComplete;
	}

	public static boolean rewardQuantityPassed(int baselineInventory, int currentInventory,
			int attributedDropQuantity, int expectedQuantity) {
		int inventoryDelta = currentInventory - baselineInventory;
		return inventoryDelta >= 0 && attributedDropQuantity >= 0
				&& inventoryDelta + attributedDropQuantity == expectedQuantity;
	}
}
