package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** docs/npc_trade/06: the daily want is the same for a seed and day, changes across days, and is one card a pool. */
class NpcTradeRotationTest {
	private static final long SEED = 0x5EEDL;
	private static final NpcTradeOffer STEADY = offer("steady", Optional.empty());
	private static final NpcTradeOffer REEF = offer("reef", pool("daily", 1));
	private static final NpcTradeOffer OIL = offer("oil", pool("daily", 1));
	private static final NpcTradeOffer CORAL = offer("coral", pool("daily", 2));
	private static final NpcTradeOffer NIGHT = offer("night", pool("night", 1));
	private static final List<NpcTradeOffer> BOARD = List.of(STEADY, REEF, OIL, CORAL, NIGHT);

	@Test
	void theDailyPickIsStableForASeedAndDay() {
		for (long day = 0; day < 40; day++) {
			assertEquals(NpcTradeRotation.today(BOARD, SEED, day), NpcTradeRotation.today(BOARD, SEED, day));
			// Order in the registry list doesn't move the pick.
			assertEquals(Set.copyOf(NpcTradeRotation.today(BOARD, SEED, day)),
					Set.copyOf(NpcTradeRotation.today(List.of(NIGHT, CORAL, OIL, REEF, STEADY), SEED, day)));
		}
	}

	@Test
	void thePickChangesAcrossDaysAndWorlds() {
		Set<NpcTradeOffer> picked = new HashSet<>();
		for (long day = 0; day < 40; day++) picked.add(dailyPick(SEED, day));
		assertEquals(Set.of(REEF, OIL, CORAL), picked, "every member of the pool comes up over forty days");

		boolean differs = false;
		for (long day = 0; day < 40 && !differs; day++) differs = dailyPick(SEED, day) != dailyPick(SEED + 1, day);
		assertTrue(differs, "another world seed draws another sequence");
	}

	@Test
	void eachPoolShowsOneCardAndOrdinaryCardsAlwaysShow() {
		for (long day = 0; day < 20; day++) {
			List<NpcTradeOffer> today = NpcTradeRotation.today(BOARD, SEED, day);
			assertTrue(today.contains(STEADY));
			assertTrue(today.contains(NIGHT), "a pool of one always shows its card");
			assertEquals(1, today.stream().filter(offer -> offer.rotation().map(r -> r.pool().equals("daily"))
					.orElse(false)).count());
			for (NpcTradeOffer offer : BOARD) {
				assertEquals(today.contains(offer), NpcTradeRotation.offeredToday(offer, BOARD, SEED, day));
			}
		}
	}

	@Test
	void weightTiltsTheDraw() {
		int coral = 0;
		for (long day = 0; day < 400; day++) if (dailyPick(SEED, day) == CORAL) coral++;
		assertTrue(coral > 140 && coral < 260, "weight 2 of 4 should come up about half the time, got " + coral);
	}

	@Test
	void aRotatingCardIsMarkedDailyAndFreshUntilSeen() {
		var ledger = new com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger();
		NpcTradeBoard board = NpcTradeCards.build("alchemist", false, List.of(STEADY, NIGHT), ledger, 7L,
				offer -> Optional.empty());
		NpcTradeCard steady = board.card(STEADY.id()).orElseThrow();
		NpcTradeCard night = board.card(NIGHT.id()).orElseThrow();
		assertFalse(steady.daily());
		assertFalse(steady.fresh());
		assertTrue(night.daily());
		assertTrue(night.fresh());

		assertTrue(ledger.markRotationSeen(NIGHT.id(), 7L));
		assertFalse(ledger.markRotationSeen(NIGHT.id(), 7L), "a second sighting the same day is not new");
		assertFalse(NpcTradeCards.build("alchemist", false, List.of(NIGHT), ledger, 7L, offer -> Optional.empty())
				.card(NIGHT.id()).orElseThrow().fresh());
		assertTrue(NpcTradeCards.build("alchemist", false, List.of(NIGHT), ledger, 8L, offer -> Optional.empty())
				.card(NIGHT.id()).orElseThrow().fresh(), "the next day's sighting is new again");
	}

	private static NpcTradeOffer dailyPick(long seed, long day) {
		return NpcTradeRotation.today(BOARD, seed, day).stream()
				.filter(offer -> offer.rotation().map(r -> r.pool().equals("daily")).orElse(false))
				.findFirst().orElseThrow();
	}

	private static Optional<NpcTradeRotation.Entry> pool(String pool, int weight) {
		return Optional.of(new NpcTradeRotation.Entry(pool, weight));
	}

	private static NpcTradeOffer offer(String name, Optional<NpcTradeRotation.Entry> rotation) {
		NpcTradeWant.Items want = new NpcTradeWant.Items(new NpcTradeMatch(
				List.of(ResourceLocation.parse("minecraft:" + name)), List.of()), 1, NpcTradeAppraisal.NONE);
		return new NpcTradeOffer(ResourceLocation.parse("hemomancy:alchemist/" + name), "alchemist",
				NpcTradeSides.Side.HARBINGER, "t", "l", 0, NpcTradeRequirements.NONE, want,
				List.of(new NpcTradeGive(ResourceLocation.parse("minecraft:bread"), 1, Optional.empty())), List.of(), 1,
				NpcTradeStock.UNLIMITED_STOCK, false, Optional.empty(), rotation);
	}
}
