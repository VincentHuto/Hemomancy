package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;

/**
 * The spike patches standing out of his bone: how many (in the fixed site order of {@link HemorathIronRules#SITES}),
 * the leak counted toward the next, and the tick the count last changed (the model's eruption and tear beat). JVM-tested.
 */
public final class HemorathPatches {
	private int count;
	private float leaked;
	private int changedAt = -1000;

	public int count() {
		return count;
	}

	public int changedAt() {
		return changedAt;
	}

	/** Counts a leak; returns how many patches erupted now. */
	public int leak(float amount, int now) {
		if (amount <= 0.0F) return 0;
		leaked += amount;
		int erupted = 0;
		while (leaked >= HemorathIronRules.LEAK_PER_PATCH && count < HemorathIronRules.MAX_PATCHES) {
			leaked -= HemorathIronRules.LEAK_PER_PATCH;
			count++;
			erupted++;
		}
		// at the cap the iron stops accruing; the leak toward a tenth is not kept
		if (count >= HemorathIronRules.MAX_PATCHES) leaked = 0.0F;
		if (erupted > 0) changedAt = now;
		return erupted;
	}

	/** Tears out up to {@code n} of the newest patches; returns how many came out. */
	public int tear(int n, int now) {
		int torn = Math.max(0, Math.min(n, count));
		count -= torn;
		if (torn > 0) changedAt = now;
		return torn;
	}

	/** Every patch bursts out (the phase-1 overload, the ending). */
	public int burstAll(int now) {
		return tear(count, now);
	}

	public void save(CompoundTag tag) {
		CompoundTag patches = new CompoundTag();
		patches.putInt("count", count);
		patches.putFloat("leaked", leaked);
		tag.put("HemorathPatches", patches);
	}

	public void load(CompoundTag tag) {
		CompoundTag patches = tag.getCompound("HemorathPatches");
		count = Math.max(0, Math.min(HemorathIronRules.MAX_PATCHES, patches.getInt("count")));
		leaked = patches.getFloat("leaked");
	}
}
