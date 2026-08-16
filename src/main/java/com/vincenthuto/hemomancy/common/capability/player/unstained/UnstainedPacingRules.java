package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedPacingRules {
	public static final long XP_REWARD_COOLDOWN_TICKS = 100L;
	public static final long CROP_REWARD_COOLDOWN_TICKS = 200L;
	public static final long PET_HEAL_REWARD_COOLDOWN_TICKS = 600L;
	public static final long EMPTY_BLOOD_REWARD_COOLDOWN_TICKS = 1_200L;
	private static final float[] LETHEAN_BREW_REWARDS = {8.0F, 6.0F, 4.0F, 2.0F, 1.0F};

	private UnstainedPacingRules() {}

	public static float paleSilverBellReward(boolean claimed) { return claimed ? 0.0F : 30.0F; }
	public static float silverChaliceReward(boolean offered) { return offered ? 0.0F : 10.0F; }
	public static float letheanBrewReward(int priorOfferings) {
		return priorOfferings >= 0 && priorOfferings < LETHEAN_BREW_REWARDS.length
				? LETHEAN_BREW_REWARDS[priorOfferings] : 0.0F;
	}
	public static boolean cooldownReady(long currentGameTime, long lastRewardGameTime, long cooldownTicks) {
		return lastRewardGameTime <= 0L || currentGameTime < lastRewardGameTime
				|| currentGameTime - lastRewardGameTime >= cooldownTicks;
	}
}
