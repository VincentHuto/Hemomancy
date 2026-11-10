package com.vincenthuto.hemomancy.common.entity.npc.trade;

import java.util.Map;
import java.util.Optional;

/**
 * Which {@link NpcTradeAppraisals} grader reads a want's stacks, and the {@code appraisal_*} fields it was given
 * (keyed without the prefix: {@code appraisal_temper} is {@code temper}).
 */
public record NpcTradeAppraisal(String grader, Map<String, String> params) {
	public static final NpcTradeAppraisal NONE = new NpcTradeAppraisal(NpcTradeAppraisals.NONE, Map.of());

	public NpcTradeAppraisal {
		params = Map.copyOf(params);
	}

	public Optional<String> param(String name) {
		return Optional.ofNullable(params.get(name));
	}
}
