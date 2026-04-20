package com.vincenthuto.hemomancy.common.capability.player.degree;

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
}
