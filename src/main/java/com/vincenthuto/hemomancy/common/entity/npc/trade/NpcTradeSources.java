package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;

import java.util.Map;
import java.util.Optional;

/**
 * The blood sources a trade pays through. A tithe counts as spend and leaves Sanguine Reserve's floor alone. No
 * category names trading, so it files under {@code OTHER}.
 */
public final class NpcTradeSources {
	public static final BloodAccounting.Source TITHE = BloodAccounting.Source.of(NpcTradeWant.Blood.TITHE,
			Category.OTHER, true, true);
	private static final Map<String, BloodAccounting.Source> BY_ID = Map.of(TITHE.id(), TITHE);

	private NpcTradeSources() {}

	/** The source a blood want names ({@link NpcTradeWant.Blood#source}). */
	public static Optional<BloodAccounting.Source> byId(String id) {
		return Optional.ofNullable(BY_ID.get(id));
	}
}
