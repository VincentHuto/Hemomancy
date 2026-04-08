package com.vincenthuto.hemomancy.common.capability.player.visceral;

import java.util.Map;

/**
 * Player capability that tracks which organs have been extracted via the
 * Visceral Mirror ritual and what modification level each has attained.
 * <p>
 * Modification level 0 means the organ is still in the body (unextracted).
 * Levels 1–3 represent increasing degrees of sanguine modification.
 * </p>
 */
public interface IVisceralOrgans {

	/**
	 * Returns the modification level for the given organ.
	 * 0 = unextracted, 1–3 = modified.
	 */
	int getOrganLevel(EnumOrgan organ);

	/**
	 * Sets the modification level for the given organ.
	 */
	void setOrganLevel(EnumOrgan organ, int level);

	/**
	 * Returns {@code true} if the given organ has been extracted at least once
	 * (level ≥ 1).
	 */
	boolean isExtracted(EnumOrgan organ);

	/**
	 * Returns {@code true} if the heart has been successfully extracted,
	 * placing the player in the Cardiac Autonomy state.
	 */
	boolean isHeartless();

	/**
	 * Returns an unmodifiable view of all organ modification levels.
	 */
	Map<EnumOrgan, Integer> getAllOrgans();

	/**
	 * Resets all organs to level 0 (unextracted).
	 */
	void resetAll();
}
