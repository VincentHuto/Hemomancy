package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

/**
 * A synced tick counter replayed on the client clock. Server ticks arrive in bursts and gaps, so reading the synced
 * value plus the partial tick stalls and skips; this advances with the entity's own client ticks instead and only
 * re-bases when the synced value restarts, jumps, or drifts more than {@link #TOLERANCE} ticks away.
 */
final class VesperAnimClock {
	static final int TOLERANCE = 2;

	private boolean started;
	private int key;
	private int base;
	private int baseTick;

	/**
	 * @param key       identifies what the counter is timing (an attack or stage ordinal); a change re-bases at once
	 * @param synced    the latest synced counter value
	 * @param direction +1 for counters that count up, -1 for countdowns
	 */
	float sample(int key, int synced, int localTick, float partial, int direction) {
		int predicted = base + direction * (localTick - baseTick);
		if (!started || key != this.key || Math.abs(synced - predicted) > TOLERANCE) {
			started = true;
			this.key = key;
			base = synced;
			baseTick = localTick;
			predicted = synced;
		}
		float value = predicted + direction * partial;
		// A countdown never runs past zero, and a counter that the server has reset reads as reset.
		if (direction < 0) return Math.max(0.0F, value);
		return synced <= 0 && predicted <= 0 ? synced : value;
	}
}
