package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Everything the Trade box shows for one speaker: the cards, the speaker's refusal line when they won't deal with this
 * player at all (there are no cards then), and the player's rapport and tier with them. Sent inside the hub's
 * presentation on open, and again with every trade result.
 */
public record NpcTradeBoard(String speaker, List<NpcTradeCard> cards, Optional<String> refusalKey, int rapport,
		int tier) {
	public static final NpcTradeBoard NONE = new NpcTradeBoard("", List.of(), Optional.empty(), 0, NpcTradeRules.STRANGER);

	public NpcTradeBoard {
		cards = List.copyOf(cards);
	}

	/** Whether the Trade box has anything to open on: a card or a refusal line. */
	public boolean present() {
		return !cards.isEmpty() || refusalKey.isPresent();
	}

	public Optional<NpcTradeCard> card(ResourceLocation offerId) {
		return cards.stream().filter(card -> card.offerId().equals(offerId)).findFirst();
	}

	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeUtf(speaker);
		buf.writeVarInt(cards.size());
		cards.forEach(card -> card.toNetwork(buf));
		NpcTradeCard.writeOptional(buf, refusalKey);
		buf.writeVarInt(rapport);
		buf.writeVarInt(tier);
	}

	public static NpcTradeBoard fromNetwork(FriendlyByteBuf buf) {
		String speaker = buf.readUtf();
		int size = buf.readVarInt();
		List<NpcTradeCard> cards = new ArrayList<>(size);
		for (int i = 0; i < size; i++) cards.add(NpcTradeCard.fromNetwork(buf));
		Optional<String> refusal = NpcTradeCard.readOptional(buf);
		int rapport = buf.readVarInt();
		int tier = buf.readVarInt();
		return new NpcTradeBoard(speaker, cards, refusal, rapport, tier);
	}
}
