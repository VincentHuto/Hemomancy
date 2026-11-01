package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** docs/npc_trade/03: the Trade box a speaker shows, and its wire form. */
class NpcTradeCardsTest {
	private static final long DAY = 7L;

	@Test
	void anUnstainedRefusalShowsNoCardsOnlyTheLine() {
		NpcTradeBoard board = NpcTradeCards.build("zealot", true, List.of(offer("zealot/linen", false, NpcTradeStock.UNLIMITED_STOCK)),
				new NpcTradeLedger(), DAY, offer -> Optional.empty());
		assertEquals(List.of(), board.cards());
		assertEquals(Optional.of("hemomancy.trade.zealot.refuse_harbinger"), board.refusalKey());
		assertTrue(board.present());
	}

	@Test
	void unmetCardsAreHiddenUnlessTheyShowLockedAndSpentCardsSellOut() {
		NpcTradeOffer hidden = offer("alchemist/hidden", false, NpcTradeStock.UNLIMITED_STOCK);
		NpcTradeOffer shown = offer("alchemist/shown", true, NpcTradeStock.UNLIMITED_STOCK);
		NpcTradeOffer limited = offer("alchemist/limited", false, new NpcTradeStock(1, 3));
		NpcTradeLedger ledger = new NpcTradeLedger();
		ledger.addRapport("alchemist", 31);
		ledger.recordUse(limited.id(), DAY - 1, 3);

		NpcTradeBoard board = NpcTradeCards.build("alchemist", false, List.of(hidden, shown, limited), ledger, DAY,
				offer -> offer == limited ? Optional.empty() : Optional.of(NpcTradeRequirements.LOCKED_RAPPORT));

		assertEquals(List.of(shown.id(), limited.id()), board.cards().stream().map(NpcTradeCard::offerId).toList());
		NpcTradeCard locked = board.cards().get(0);
		assertEquals(NpcTradeCard.State.LOCKED, locked.state());
		assertEquals(Optional.of(NpcTradeRequirements.LOCKED_RAPPORT), locked.lockKey());
		assertEquals(NpcTradeCard.UNLIMITED, locked.usesLeft());
		NpcTradeCard soldOut = board.cards().get(1);
		assertEquals(NpcTradeCard.State.SOLD_OUT, soldOut.state());
		assertEquals(2, soldOut.daysToRestock());
		assertEquals(0, soldOut.usesLeft());
		assertEquals(31, board.rapport());
		assertEquals(NpcTradeRules.TRUSTED, board.tier());
	}

	@Test
	void aBoardSurvivesTheWire() {
		NpcTradeWant.Bundle bundle = new NpcTradeWant.Bundle(List.of(
				new NpcTradeWant.Items(new NpcTradeMatch(List.of(rl("minecraft:bone")), List.of(rl("minecraft:swords"))), 2,
						new NpcTradeAppraisal(NpcTradeAppraisals.DURABILITY, Map.of("min", "0.5"))),
				new NpcTradeWant.Items(new NpcTradeMatch(List.of(rl("minecraft:redstone")), List.of()), 1, NpcTradeAppraisal.NONE)));
		List<NpcTradeCard> cards = List.of(
				new NpcTradeCard(rl("hemomancy:alchemist/a"), "t.a", "l.a", 3, bundle,
						List.of(new NpcTradeGive(rl("minecraft:redstone"), 4, Optional.of("{\"minecraft:rarity\":\"rare\"}"))),
						List.of(), NpcTradeCard.State.OPEN, Optional.empty(), 0, 2, Optional.empty()),
				new NpcTradeCard(rl("hemomancy:alchemist/b"), "t.b", "l.b", 4, new NpcTradeWant.Blood(150, NpcTradeWant.Blood.TITHE),
						List.of(), List.of(), NpcTradeCard.State.LOCKED, Optional.of(NpcTradeRequirements.LOCKED_BLOOD), 0,
						NpcTradeCard.UNLIMITED, Optional.empty()),
				new NpcTradeCard(rl("hemomancy:court_gardener/c"), "t.c", "l.c", 0, new NpcTradeWant.Custom("custom_food"),
						List.of(), List.of(), NpcTradeCard.State.SOLD_OUT, Optional.empty(), 2, 0, Optional.of("o.c")));
		NpcTradeBoard board = new NpcTradeBoard("alchemist", cards, Optional.empty(), 12, NpcTradeRules.KNOWN);

		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		board.toNetwork(buf);
		assertEquals(board, NpcTradeBoard.fromNetwork(buf));
		assertEquals(0, buf.readableBytes());

		NpcTradeBoard refusal = new NpcTradeBoard("scout", List.of(), Optional.of("hemomancy.trade.scout.refuse_harbinger"), 0, 0);
		FriendlyByteBuf second = new FriendlyByteBuf(Unpooled.buffer());
		refusal.toNetwork(second);
		assertEquals(refusal, NpcTradeBoard.fromNetwork(second));
	}

	private static NpcTradeOffer offer(String path, boolean showLocked, NpcTradeStock stock) {
		String speaker = path.substring(0, path.indexOf('/'));
		return new NpcTradeOffer(rl("hemomancy:" + path), speaker, NpcTradeSides.side(speaker).orElseThrow(), "t", "l", 0,
				NpcTradeRequirements.NONE, new NpcTradeWant.Items(new NpcTradeMatch(List.of(rl("minecraft:bone")), List.of()),
						1, NpcTradeAppraisal.NONE),
				List.of(new NpcTradeGive(rl("minecraft:redstone"), 1, Optional.empty())), List.of(), 1, stock, showLocked,
				Optional.empty());
	}

	private static ResourceLocation rl(String id) {
		return ResourceLocation.parse(id);
	}
}
