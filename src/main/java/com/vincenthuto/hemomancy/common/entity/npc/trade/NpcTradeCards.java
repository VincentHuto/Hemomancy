package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds a speaker's Trade box for one player. An Unstained speaker facing a Harbinger gives no cards, only their
 * refusal line. Otherwise every loaded offer on today's board ({@link NpcTradeRotation}: one card per rotation pool)
 * that the player meets becomes a card; one they don't meet is drawn locked when it sets {@code show_locked} and left
 * out when it doesn't. An act card the player has no act banked for is locked on the act.
 */
public final class NpcTradeCards {
	private NpcTradeCards() {}

	public static NpcTradeBoard build(ServerPlayer player, String speaker) {
		NpcTradeLedger ledger = HemoCapabilityAccess.npcTrades(player);
		long day = NpcTradeRules.day(player.level().getDayTime());
		List<NpcTradeOffer> offers = NpcTradeRotation.today(NpcTradeRegistry.offers(speaker),
				player.serverLevel().getSeed(), day);
		return build(speaker, NpcTradeSides.refuses(speaker, player), offers, ledger, day,
				offer -> offer.requirements().firstFailure(player, ledger.rapport(speaker)));
	}

	/** Who meets an offer: the lock key of the first requirement they fail. */
	@FunctionalInterface
	public interface Gate {
		Optional<String> firstFailure(NpcTradeOffer offer);
	}

	public static NpcTradeBoard build(String speaker, boolean refused, List<NpcTradeOffer> offers,
			NpcTradeLedger ledger, long day, Gate gate) {
		int rapport = ledger.rapport(speaker);
		int tier = NpcTradeRules.tier(rapport);
		if (refused) {
			return new NpcTradeBoard(speaker, List.of(), Optional.of(NpcTradeSides.refusalKey(speaker)), rapport, tier);
		}
		List<NpcTradeCard> cards = new ArrayList<>();
		for (NpcTradeOffer offer : offers) {
			Optional<String> failure = gate.firstFailure(offer).or(() -> actFailure(offer, ledger));
			if (failure.isPresent() && !offer.showLocked()) continue;
			cards.add(card(offer, failure, ledger, day));
		}
		return new NpcTradeBoard(speaker, cards, Optional.empty(), rapport, tier);
	}

	/** An act card is locked while the player has none of its act banked. */
	static Optional<String> actFailure(NpcTradeOffer offer, NpcTradeLedger ledger) {
		return offer.want() instanceof NpcTradeWant.Act act && ledger.acts(act.act()) <= 0
				? Optional.of(act.lockKey()) : Optional.empty();
	}

	static NpcTradeCard card(NpcTradeOffer offer, Optional<String> failure, NpcTradeLedger ledger, long day) {
		NpcTradeStock stock = offer.stock();
		int usesLeft = stock.limited() ? ledger.usesLeft(offer.id(), stock.uses(), day, stock.restockDays()) : 0;
		long days = stock.limited() ? ledger.daysToRestock(offer.id(), day, stock.restockDays()) : 0L;
		boolean fresh = offer.rotation().isPresent() && !ledger.rotationSeen(offer.id(), day);
		return NpcTradeCard.of(offer, failure, usesLeft, days, fresh);
	}

	/**
	 * The player opened {@code speaker}'s Trade box: today's daily cards count as seen, so they lose the unread dot.
	 * Returns how many were new.
	 */
	public static int markRotationSeen(ServerPlayer player, String speaker) {
		NpcTradeLedger ledger = HemoCapabilityAccess.npcTrades(player);
		long day = NpcTradeRules.day(player.level().getDayTime());
		int marked = 0;
		for (NpcTradeOffer offer : NpcTradeRotation.today(NpcTradeRegistry.offers(speaker),
				player.serverLevel().getSeed(), day)) {
			if (offer.rotation().isPresent() && ledger.markRotationSeen(offer.id(), day)) marked++;
		}
		return marked;
	}
}
