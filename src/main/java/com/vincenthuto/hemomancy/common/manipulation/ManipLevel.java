package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.nbt.CompoundTag;

public class ManipLevel {

	// ── Level thresholds: cumulative uses required to reach each level ──
	/** Uses required to advance from level N-1 to level N (1-indexed). */
	public static final int[] XP_THRESHOLDS = { 10, 25, 50, 100 };

	/** Maximum attainable level (length of {@link #XP_THRESHOLDS}). */
	public static final int MAX_LEVEL = XP_THRESHOLDS.length;

	/**
	 * Blood-cost reduction applied per level as a multiplier on top of other
	 * discounts. Level 4 yields a 20% reduction in manipulation cost.
	 */
	public static final double COST_REDUCTION_PER_LEVEL = 0.05;

	/**
	 * Cooldown reduction applied per level on top of the Blood Flow skill.
	 * Level 4 yields a 20% reduction in cooldown duration.
	 */
	public static final double COOLDOWN_REDUCTION_PER_LEVEL = 0.05;

	public static ManipLevel BLANK = new ManipLevel(0, 0);

	public static ManipLevel deserialize(CompoundTag nbt) {
		if (nbt != null && !nbt.isEmpty()) {
			if (nbt.contains("level") && nbt.contains("xpcost")) {
				ManipLevel manip = new ManipLevel(nbt.getInt("level"), nbt.getDouble("xpcost"));
				return manip;
			}
		}
		return null;
	}

	int currentLevel;
	double xp;

	public ManipLevel(int currentLevel, double xp) {
		this.currentLevel = currentLevel;
		this.xp = xp;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public double getXp() {
		return xp;
	}

	/**
	 * Attempts to advance {@link #currentLevel} based on accumulated {@link #xp}.
	 *
	 * <p>XP is cumulative — the total uses since the manipulation was learned. Each
	 * threshold represents the total uses needed to reach that level.
	 * XP is never reset; the check simply looks at whether the running total
	 * has crossed the next threshold.</p>
	 *
	 * @return {@code true} if at least one level-up occurred (caller should notify player)
	 */
	public boolean tryLevelUp() {
		if (currentLevel >= MAX_LEVEL) {
			return false;
		}
		int newLevel = currentLevel;
		// Compute cumulative threshold for each level
		int cumulative = 0;
		for (int i = 0; i < XP_THRESHOLDS.length; i++) {
			cumulative += XP_THRESHOLDS[i];
			if (xp >= cumulative) {
				newLevel = i + 1;
			} else {
				break;
			}
		}
		if (newLevel > currentLevel) {
			currentLevel = Math.min(newLevel, MAX_LEVEL);
			return true;
		}
		return false;
	}

	/**
	 * Returns the cost multiplier derived from this level.
	 * A level-4 manipulation costs 20% less blood than base.
	 */
	public double getCostMultiplier() {
		return Math.max(0.0, 1.0 - currentLevel * COST_REDUCTION_PER_LEVEL);
	}

	/**
	 * Returns the cooldown multiplier derived from this level.
	 * A level-4 manipulation has 20% shorter cooldowns.
	 */
	public double getCooldownMultiplier() {
		return Math.max(0.0, 1.0 - currentLevel * COOLDOWN_REDUCTION_PER_LEVEL);
	}

	/**
	 * XP remaining until the next level-up, or 0 if already at max level.
	 */
	public double getXpToNextLevel() {
		if (currentLevel >= MAX_LEVEL) return 0;
		int cumulative = 0;
		for (int i = 0; i <= currentLevel && i < XP_THRESHOLDS.length; i++) {
			cumulative += XP_THRESHOLDS[i];
		}
		return Math.max(0, cumulative - xp);
	}

	public CompoundTag serialize() {
		CompoundTag nbt = new CompoundTag();
		nbt.putInt("level", currentLevel);
		nbt.putDouble("xpcost", xp);
		return nbt;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public void setXp(double xp) {
		this.xp = xp;
	}

	@Override
	public String toString() {
		return HLTextUtils.convertInitToLang("Level: " + currentLevel + ", Xp: " + xp);
	}
}
