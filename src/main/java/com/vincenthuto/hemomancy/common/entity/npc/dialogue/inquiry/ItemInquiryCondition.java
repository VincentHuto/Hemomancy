package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;

/**
 * One conditional response branch inside an {@link ItemInquiryEntry}.
 * <p>
 * All fields except {@code lines} are optional — a missing value means
 * "no constraint in this direction".  Branches are evaluated top-to-bottom;
 * the first branch whose constraints all pass is used.
 * <p>
 * Supported constraints:
 * <ul>
 *   <li>{@code minDegree} / {@code maxDegree} — player initiatory degree</li>
 *   <li>{@code minPurity}  / {@code maxPurity} — player Unstained purity (0–100)</li>
 * </ul>
 *
 * @param minDegree minimum degree (inclusive); -1 = unconstrained
 * @param maxDegree maximum degree (inclusive); -1 = unconstrained
 * @param minPurity minimum purity (inclusive); -1 = unconstrained
 * @param maxPurity maximum purity (inclusive); -1 = unconstrained
 * @param lines     translation keys for the NPC's dialogue lines
 */
public record ItemInquiryCondition(
		int minDegree,
		int maxDegree,
		float minPurity,
		float maxPurity,
		List<String> lines
) {

	/** A condition with no degree/purity constraints — always matches. */
	public static ItemInquiryCondition unconditional(List<String> lines) {
		return new ItemInquiryCondition(-1, -1, -1f, -1f, lines);
	}

	/**
	 * Returns {@code true} if the given context passes all constraints defined
	 * on this branch.
	 *
	 * @param degree player's initiatory degree (0–8+)
	 * @param purity player's Unstained purity (0–100); ignored when no purity
	 *               constraint is defined
	 */
	public boolean matches(int degree, float purity) {
		if (minDegree >= 0 && degree < minDegree) return false;
		if (maxDegree >= 0 && degree > maxDegree) return false;
		if (minPurity >= 0 && purity < minPurity) return false;
		if (maxPurity >= 0 && purity > maxPurity) return false;
		return true;
	}
}
