package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;
import java.util.Optional;

/**
 * Loaded representation of one item-inquiry JSON file.
 * <p>
 * Two shapes are supported:
 * <ol>
 *   <li><b>Simple</b> — a single {@code lines} array; unconditional.</li>
 *   <li><b>Conditional</b> — a {@code conditions} array, evaluated
 *       top-to-bottom; first matching branch wins.</li>
 * </ol>
 *
 * @param conditions ordered list of conditional branches (may contain a
 *                   single unconditional entry for the simple case)
 */
public record ItemInquiryEntry(List<ItemInquiryCondition> conditions) {

	/**
	 * Resolves the appropriate dialogue lines for the supplied context by
	 * evaluating branches in order.
	 *
	 * @param degree player's initiatory degree
	 * @param purity player's Unstained purity (0–100)
	 * @return the first matching branch's lines, or empty if no branch matches
	 */
	public Optional<List<String>> resolve(int degree, float purity) {
		for (ItemInquiryCondition branch : conditions) {
			if (branch.matches(degree, purity)) {
				return Optional.of(branch.lines());
			}
		}
		return Optional.empty();
	}
}
