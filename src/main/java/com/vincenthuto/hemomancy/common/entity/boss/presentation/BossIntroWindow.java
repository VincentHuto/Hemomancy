package com.vincenthuto.hemomancy.common.entity.boss.presentation;

import net.minecraft.nbt.CompoundTag;

/**
 * A boss's opening on the server: a fixed run of ticks during which the boss is passive and untouchable and its bar is
 * hidden, while its opening scene plays for the players around it. The boss holds one, ticks it, mirrors
 * {@link #elapsed()} into its own synced data when its model poses from it, and asks {@link #active()} in its damage
 * and AI hooks. It saves, so a reload mid-opening carries on from where it was.
 */
public final class BossIntroWindow {
	private final int length;
	private int elapsed = -1;

	public BossIntroWindow(int length) {
		this.length = length;
	}

	public int length() {
		return length;
	}

	public void begin() {
		elapsed = 0;
	}

	/** Advances the window; true on the tick it ends, when the boss shows its bar and starts fighting. */
	public boolean tick() {
		if (elapsed < 0) return false;
		if (++elapsed < length) return false;
		elapsed = -1;
		return true;
	}

	public boolean active() {
		return elapsed >= 0;
	}

	/** Ticks into the window, or -1 when it has ended or never begun. */
	public int elapsed() {
		return elapsed;
	}

	public void save(CompoundTag tag, String key) {
		if (active()) tag.putInt(key, elapsed);
	}

	public void load(CompoundTag tag, String key) {
		elapsed = tag.contains(key) ? tag.getInt(key) : -1;
	}
}
