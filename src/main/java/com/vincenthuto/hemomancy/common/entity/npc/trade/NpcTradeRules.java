package com.vincenthuto.hemomancy.common.entity.npc.trade;

import com.vincenthuto.hemomancy.common.capability.player.shared.trade.NpcTradeLedger;

/**
 * The numbers trading runs on. Rapport is per NPC, 0 to {@link #RAPPORT_MAX}; offers gate on the raw number
 * ({@code rapport_at_least}), lore on the tier. A request carries at most {@link #MAX_SLOTS} main-inventory slots and a
 * player may send one every {@link #REQUEST_INTERVAL_TICKS} ticks.
 */
public final class NpcTradeRules {
	public static final int RAPPORT_MAX = NpcTradeLedger.RAPPORT_MAX;
	public static final int STRANGER = 0;
	public static final int KNOWN = 1;
	public static final int TRUSTED = 2;
	public static final int CONFIDANT = 3;
	/** The lowest rapport of each tier, by tier. */
	private static final int[] TIER_FLOORS = { 0, 10, 30, 60 };
	private static final String[] TIER_NAMES = { "stranger", "known", "trusted", "confidant" };

	public static final int MAX_SLOTS = 36;
	public static final int REQUEST_INTERVAL_TICKS = 4;

	private NpcTradeRules() {}

	/** 0 Stranger (0-9), 1 Known (10-29), 2 Trusted (30-59), 3 Confidant (60-100). */
	public static int tier(int rapport) {
		for (int tier = TIER_FLOORS.length - 1; tier > 0; tier--) {
			if (rapport >= TIER_FLOORS[tier]) return tier;
		}
		return STRANGER;
	}

	/** The rapport a tier starts at. */
	public static int tierFloor(int tier) {
		return TIER_FLOORS[Math.clamp(tier, STRANGER, CONFIDANT)];
	}

	/** The tier's name, {@code hemomancy.trade.tier.<name>}. */
	public static String tierKey(int tier) {
		return "hemomancy.trade.tier." + TIER_NAMES[Math.clamp(tier, STRANGER, CONFIDANT)];
	}

	/** The day a restock window is counted in, from {@code level.getDayTime()}. */
	public static long day(long dayTime) {
		return Math.floorDiv(dayTime, NpcTradeStock.TICKS_PER_DAY);
	}
}
