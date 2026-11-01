package com.vincenthuto.hemomancy.common.entity.npc.trade;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * What a card asks for: items, blood (Harbinger speakers only), a bundle of items, a Court house custom, or a Circus
 * act the player has watched through.
 */
public sealed interface NpcTradeWant {
	/** The items wants this card takes stacks for: itself, a bundle's parts, or none. */
	List<Items> itemParts();

	/** A want two cards on one speaker may not share outside of items ({@code blood}, {@code custom:<act>}). */
	Optional<String> exclusiveClaim();

	/** {@code count} stacks matching {@code match}, each graded by {@code appraisal}. */
	record Items(NpcTradeMatch match, int count, NpcTradeAppraisal appraisal) implements NpcTradeWant {
		@Override
		public List<Items> itemParts() {
			return List.of(this);
		}

		@Override
		public Optional<String> exclusiveClaim() {
			return Optional.empty();
		}
	}

	/** {@code amount} mB through {@code BloodAccounting.pay}, under the source named {@code source}. */
	record Blood(int amount, String source) implements NpcTradeWant {
		/** The one blood source trades pay through; Track 3 holds the {@code BloodAccounting.Source} constant. */
		public static final String TITHE = "npc_tithe";
		public static final Set<String> SOURCES = Set.of(TITHE);

		@Override
		public List<Items> itemParts() {
			return List.of();
		}

		@Override
		public Optional<String> exclusiveClaim() {
			return Optional.of("blood");
		}
	}

	/** Two or three items wants, all met and taken together. */
	record Bundle(List<Items> parts) implements NpcTradeWant {
		public static final int MIN_PARTS = 2;
		public static final int MAX_PARTS = 3;

		public Bundle {
			parts = List.copyOf(parts);
		}

		@Override
		public List<Items> itemParts() {
			return parts;
		}

		@Override
		public Optional<String> exclusiveClaim() {
			return Optional.empty();
		}
	}

	/** A Brined Court house custom ({@code CourtHospitality.customAct}): standing, not a price. */
	record Custom(String act) implements NpcTradeWant {
		@Override
		public List<Items> itemParts() {
			return List.of();
		}

		@Override
		public Optional<String> exclusiveClaim() {
			return Optional.of("custom:" + act);
		}
	}

	/**
	 * An act the player watched through to the end, banked in their {@code NpcTradeLedger} and spent by the trade
	 * (Circus speakers only). {@link #SPECTACLE} is a pavilion performance, recorded by
	 * {@code CircusPerformanceController} when a challenge completes.
	 */
	record Act(String act) implements NpcTradeWant {
		public static final String SPECTACLE = "spectacle";
		public static final Set<String> ACTS = Set.of(SPECTACLE);

		/** The lock reason a card shows while the player has no such act banked. */
		public String lockKey() {
			return "hemomancy.trade.locked.act." + act;
		}

		@Override
		public List<Items> itemParts() {
			return List.of();
		}

		@Override
		public Optional<String> exclusiveClaim() {
			return Optional.of("act:" + act);
		}
	}
}
