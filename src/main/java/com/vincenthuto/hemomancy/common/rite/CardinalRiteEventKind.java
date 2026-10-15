package com.vincenthuto.hemomancy.common.rite;

/**
 * Discrete beats of an active Cardinal Rite, sent through
 * {@code CardinalRiteEventPacket}. Terminal kinds end the rite on the client.
 */
public enum CardinalRiteEventKind {
	/** The Living Staff struck the Focus. */
	STAFF_IMPACT,
	/** {@code index} anchor reached full blood. */
	ANCHOR_FILLED,
	/** {@code index} anchor broke under instability. */
	ANCHOR_BROKEN,
	/** {@code index} anchor was repaired. */
	ANCHOR_REPAIRED,
	/** Every anchor is consecrated; inscription opens. */
	CONSECRATED,
	/** The daemon was sealed. */
	SEALED,
	/** A support or response sigil awakened; {@code style} is its id. */
	SIGIL_AWAKENED,
	/** An ordeal wave began; {@code points} are its spawn positions, {@code style} its wave id. */
	WAVE_START,
	/** An ordeal wave was answered. */
	WAVE_CLEARED,
	/** {@code index} offering was drawn from the brazier at {@code points[0]} with {@code item}. */
	OFFERING_DRAWN,
	/** Culmination began; {@code style} is the culmination style. */
	CULMINATION,
	/** The rite completed; {@code style} is the culmination style. */
	COMPLETED,
	/** Instability or a failed ordeal collapsed the rite. */
	COLLAPSED,
	/** The structure was broken under the rite. */
	BROKEN,
	/** The caster drew the rite back into the staff. */
	CANCELLED,
	/** The rite pushed the world a step further; {@code index} is its new stage. */
	ESCALATION,
	/** A later anchor ring wakes: index is its rank, style the rite's ring count, points its anchors' aim points. */
	RING_OPENED,
	/** A world rite's daemon struck the sky at {@code points[0]}; {@code index} seeds the flash. */
	SKY_STRUCK,
	/**
	 * A world rite's daemon slammed into the ground at {@code points[0]}; {@code index} seeds the cracks and
	 * {@code style} is how far they reach, in blocks.
	 */
	GROUND_SLAMMED,
	/**
	 * A Working craft's daemon broke the escrowed {@code item} where it sat in the brazier at {@code points[0]},
	 * hanging over it at {@code points[1]}; {@code index} is the offering.
	 */
	OFFERING_DISSOLVED;

	private static final CardinalRiteEventKind[] VALUES = values();

	public static CardinalRiteEventKind byOrdinal(int ordinal) {
		return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : STAFF_IMPACT;
	}

	public boolean terminal() {
		return this == COMPLETED || this == COLLAPSED || this == BROKEN || this == CANCELLED;
	}
}
