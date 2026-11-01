package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/**
 * One want card, loaded from {@code data/<ns>/npc_trade/<speaker>/<offer>.json}. {@code title}, {@code line} and
 * {@code opens} are lang keys. {@code giveFine} is empty when a fine grade pays one more rapport instead.
 * {@code opens} is a Court card's note on what its standing leads to (a Court card gives no goods). {@code rotation}
 * puts the card in a daily pool ({@link NpcTradeRotation}).
 */
public record NpcTradeOffer(ResourceLocation id, String speaker, NpcTradeSides.Side side, String title, String line,
		int order, NpcTradeRequirements requirements, NpcTradeWant want, List<NpcTradeGive> give,
		List<NpcTradeGive> giveFine, int rapport, NpcTradeStock stock, boolean showLocked, Optional<String> opens,
		Optional<NpcTradeRotation.Entry> rotation) {
	public NpcTradeOffer {
		give = List.copyOf(give);
		giveFine = List.copyOf(giveFine);
		if (rotation == null) rotation = Optional.empty();
	}

	/** An ordinary card, in no rotation. */
	public NpcTradeOffer(ResourceLocation id, String speaker, NpcTradeSides.Side side, String title, String line,
			int order, NpcTradeRequirements requirements, NpcTradeWant want, List<NpcTradeGive> give,
			List<NpcTradeGive> giveFine, int rapport, NpcTradeStock stock, boolean showLocked, Optional<String> opens) {
		this(id, speaker, side, title, line, order, requirements, want, give, giveFine, rapport, stock, showLocked,
				opens, Optional.empty());
	}
}
