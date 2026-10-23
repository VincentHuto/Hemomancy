package com.vincenthuto.hemomancy.common.entity.boss;

/**
 * A synced tick counter replayed on the client clock. Server ticks arrive in bursts and gaps, so reading the synced
 * value plus the partial tick stalls and skips; this advances with the entity's own client ticks instead and only
 * re-bases when the synced value restarts, jumps, or drifts more than {@link #TOLERANCE} ticks away.
 * <p>
 * A boss either {@link #sample samples} once per frame, or {@link #sync syncs} once per client tick and reads
 * {@link #at} each frame. The clock holds no client classes, so an entity can own one.
 */
public final class BossReplayClock {
	public static final int TOLERANCE = 2;

	private boolean started;
	private int key;
	private int base;
	private int baseTick;
	private int direction = 1;

	/** A clock that re-bases on its first sync. */
	public BossReplayClock() {
	}

	/**
	 * A clock already running from local tick 0 for {@code key}: its first sync re-bases only on a new key or drift, as a
	 * counter would that has been running since the entity appeared.
	 */
	public BossReplayClock(int key) {
		this.started = true;
		this.key = key;
	}

	/**
	 * @param key       identifies what the counter is timing (an attack, action or stage ordinal); a change re-bases
	 *                  at once
	 * @param synced    the latest synced counter value
	 * @param direction +1 for counters that count up, -1 for countdowns
	 */
	public void sync(int key, int synced, int localTick, int direction) {
		this.direction = direction;
		if (!started || key != this.key || Math.abs(synced - predicted(localTick)) > TOLERANCE) {
			started = true;
			this.key = key;
			base = synced;
			baseTick = localTick;
		}
	}

	/** The replayed counter at a local tick and partial tick. */
	public float at(int localTick, float partial) {
		return predicted(localTick) + direction * partial;
	}

	/**
	 * Syncs and reads in one call. A countdown never runs past zero, and a counter that the server has reset reads as
	 * reset.
	 */
	public float sample(int key, int synced, int localTick, float partial, int direction) {
		sync(key, synced, localTick, direction);
		int predicted = predicted(localTick);
		float value = at(localTick, partial);
		if (direction < 0) return Math.max(0.0F, value);
		return synced <= 0 && predicted <= 0 ? synced : value;
	}

	private int predicted(int localTick) {
		return base + direction * (localTick - baseTick);
	}
}
