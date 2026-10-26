package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * Seraphae does not have traditional health-gated phases.
 * She cycles through instability states that the player must manage
 * through containment mechanics.
 */
public enum SeraphaeState {
	/** Single cohesive entity. Slowly loses containment integrity over time. */
	STABLE(200),
	/** Splitting into multiple fragments that act independently. */
	FRACTURING(100),
	/** Fully dispersed — untargetable, environmental hazards only. */
	DISPERSED(160),
	/** Fragments converging back into core — vulnerability window for large integrity gain. */
	CONDENSING(80);

	private final int duration;

	SeraphaeState(int duration) { this.duration = duration; }

	public int duration() { return duration; }

	public static SeraphaeState fromId(int id) {
		SeraphaeState[] values = values();
		return id >= 0 && id < values.length ? values[id] : STABLE;
	}
}
