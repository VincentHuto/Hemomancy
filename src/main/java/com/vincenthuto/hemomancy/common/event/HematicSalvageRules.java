package com.vincenthuto.hemomancy.common.event;

public final class HematicSalvageRules {
	private HematicSalvageRules() {
	}

	public static double calculateChance(int damagedIronPieces, double baseChance, double chancePerPiece,
			double maxChance) {
		double chance = baseChance + Math.max(0, damagedIronPieces) * chancePerPiece;
		return Math.min(maxChance, Math.max(0.0, chance));
	}

	public static boolean isEligible(float damage, double minimumDamage, int playerDegree, int maximumDegree,
			long gameTime, long lastDropTick, int damagedIronPieces, int cooldownTicks) {
		if (damage < minimumDamage) return false;
		if (playerDegree > maximumDegree) return false;
		if (damagedIronPieces <= 0) return false;
		if (lastDropTick < 0) return true;
		return gameTime - lastDropTick >= cooldownTicks;
	}
}
