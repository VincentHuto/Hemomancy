package com.vincenthuto.hemomancy.common.entity.npc.trade;

/**
 * How often a player may complete one offer: {@code uses} trades per window of {@code restockDays} in-game days
 * ({@link #TICKS_PER_DAY} ticks of {@code level.getDayTime()}). {@code uses} of {@link #UNLIMITED} never sells out.
 */
public record NpcTradeStock(int uses, int restockDays) {
	public static final int UNLIMITED = 0;
	public static final long TICKS_PER_DAY = 24000L;
	public static final NpcTradeStock UNLIMITED_STOCK = new NpcTradeStock(UNLIMITED, 1);

	public boolean limited() {
		return uses != UNLIMITED;
	}

	public long windowTicks() {
		return restockDays * TICKS_PER_DAY;
	}
}
