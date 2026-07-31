package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import javax.annotation.Nullable;

/**
 * Player capability that tracks which initiatory degree the player has attained
 * within the Hematic Order. Degrees range from 0 (uninitiated) to 8 (Apotheos).
 */
public interface IInitiatoryDegree {

	/**
	 * Returns the player's current degree number (0 = uninitiated, 1–8 = a
	 * named degree).
	 */
	int getDegreeNumber();

	/**
	 * Returns the player's current degree enum value, or {@code null} if they
	 * have not yet been initiated (degree 0).
	 */
	@Nullable
	EnumInitiatoryDegree getDegree();

	/** Returns {@code true} if the player has been initiated (degree ≥ 1). */
	boolean isInitiated();

	/** Returns {@code true} if the player has reached the maximum degree (8). */
	boolean isMaxDegree();

	/**
	 * Sets the degree directly. Use {@code 0} to mark the player as uninitiated,
	 * or 1–8 to set a specific degree.
	 */
	void setDegreeNumber(int degree);

	/**
	 * Advances the player to the next degree, if one exists.
	 *
	 * @return {@code true} if the degree was advanced, {@code false} if already at
	 *         the maximum or uninitiated with no next step.
	 */
	boolean advanceDegree();

	// ── Qliphoth Pome communion tracking ──────────────────────────────────────

	/** Returns {@code true} if the player has completed the Qliphoth Communion. */
	boolean isQliphothCommunionDone();

	/** Sets the Qliphoth Communion completion flag. */
	void setQliphothCommunionDone(boolean done);

	/**
	 * Records one pome consumed from the given bloom origin. Returns the new
	 * per-bloom consumption count after the increment.
	 */
	int recordPomeConsumed(long bloomOrigin);

	/**
	 * Returns total pomes consumed (0–9, capped). Used for HUD display.
	 * The server increments this on every normal pome eat; the client receives
	 * the value via {@code PacketSyncPomeProgress}.
	 */
	int getTotalPomesConsumed();

	/** Increments the total-consumed counter (server-side, capped at 9). */
	void incrementTotalPomesConsumed();

	/**
	 * Overwrites the stored total with a server-synced value.
	 * Called client-side by {@code PacketSyncPomeProgress}.
	 */
	void syncTotalPomesConsumed(int count);

	/** Clears pome counters, per-bloom progress, and the Communion completion flag. */
	void resetPomeCommunion();

	// ── Qliphoth Pome empowerment ──────────────────────────────────────────────

	/**
	 * Returns the game tick at which the pome manipulation-cost discount expires.
	 * A value of {@code 0} (or less) means no active empowerment.
	 */
	long getPomeEmpowermentExpiry();

	/** Sets the game tick at which the pome empowerment expires. */
	void setPomeEmpowermentExpiry(long tick);

	boolean hasFoundedBloodline();

	void setHasFoundedBloodline(boolean founded);

	boolean isFounderIntegrationSevered();

	void setFounderIntegrationSevered(boolean severed);

	boolean hasWitnessedFungalRevelation();

	void setFungalRevelationWitnessed(boolean witnessed);

	boolean hasFungalSpineGranted();

	void setFungalSpineGranted(boolean granted);

	boolean hasHematicFortification();

	void setHematicFortification(boolean fortified);

	int getAncestralCommunions();

	void setAncestralCommunions(int communions);

	EnumArchonPath getArchonPath();

	void setArchonPath(EnumArchonPath path);
}
