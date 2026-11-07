package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import net.minecraft.nbt.CompoundTag;

/**
 * The four wells' contents (fight values on the Saint, never the basin blocks), the pair cooldowns and a running draw.
 * Index i is the arena's i-th well; wells 0 and 1 are one aisle's pair, 2 and 3 the other's. A chamber with fewer wells
 * simply has fewer indices in use ({@link #count}). JVM-tested.
 */
public final class HemorathWells {
	private final float[] content = new float[HemorathWellRules.WELLS];
	private final long[] drawnAt = new long[HemorathWellRules.WELLS];
	private final float[] drawStart = new float[HemorathWellRules.WELLS];
	private int count;
	private int drawMask;
	private int drawTicks;

	public HemorathWells() {
		java.util.Arrays.fill(content, HemorathWellRules.START);
		java.util.Arrays.fill(drawnAt, Long.MIN_VALUE / 2);
	}

	/** How many wells the chamber has (0 to 4); set once the arena is scanned. */
	public void count(int wells) {
		count = Math.max(0, Math.min(HemorathWellRules.WELLS, wells));
	}

	public int count() {
		return count;
	}

	public float content(int well) {
		return valid(well) ? content[well] : 0.0F;
	}

	public float[] contents() {
		float[] copy = new float[HemorathWellRules.WELLS];
		for (int i = 0; i < count; i++) copy[i] = content[i];
		return copy;
	}

	/** Adds up to the well's room; returns what went in. */
	public float credit(int well, float amount) {
		if (!valid(well) || amount <= 0.0F) return 0.0F;
		float added = Math.min(amount, HemorathWellRules.CAPACITY - content[well]);
		content[well] += added;
		return added;
	}

	/** Takes up to the well's content; returns what came out. */
	public float take(int well, float amount) {
		if (!valid(well) || amount <= 0.0F) return 0.0F;
		float taken = Math.min(amount, content[well]);
		content[well] -= taken;
		return taken;
	}

	/** Empties every well (the Downfall deed's start); returns the total drunk. */
	public float takeAll() {
		float total = 0.0F;
		for (int i = 0; i < count; i++) total += take(i, content[i]);
		return total;
	}

	/** The pair a well belongs to: 0 for wells 0 and 1, 1 for wells 2 and 3. */
	public static int pair(int well) {
		return well / 2;
	}

	/** The pair's two wells as a mask. */
	public static int pairMask(int pair) {
		return 0b11 << (pair * 2);
	}

	public float pairContent(int pair) {
		return content(pair * 2) + content(pair * 2 + 1);
	}

	/** The pair worth drawing now (most content, off its cooldown), or -1. */
	public int bestPair(long now) {
		int best = -1;
		float most = 0.0F;
		for (int pair = 0; pair * 2 < count; pair++) {
			if (now - drawnAt[pair * 2] < HemorathWellRules.PAIR_COOLDOWN) continue;
			float held = pairContent(pair);
			if (held > most) {
				most = held;
				best = pair;
			}
		}
		return best;
	}

	public float bestPairContent(long now) {
		int pair = bestPair(now);
		return pair < 0 ? 0.0F : pairContent(pair);
	}

	/** Every well's mask (the Litany deed draws all four at once). */
	public int allMask() {
		return (1 << count) - 1;
	}

	/** Starts drawing the masked wells; each gives its content at the start over DRAW_TICKS. */
	public void startDraw(int mask, long now) {
		drawMask = mask & allMask();
		drawTicks = 0;
		for (int i = 0; i < count; i++) {
			if ((drawMask & (1 << i)) == 0) continue;
			drawStart[i] = content[i];
			drawnAt[i] = now;
		}
	}

	public boolean drawing() {
		return drawMask != 0;
	}

	public int drawMask() {
		return drawMask;
	}

	/** The flow each drawn well gives this tick (before denial), by index; 0 for the others. */
	public float flow(int well) {
		if (!valid(well) || (drawMask & (1 << well)) == 0) return 0.0F;
		return Math.min(content[well], HemorathWellRules.drawPerTick(drawStart[well]));
	}

	/** Counts a draw tick; the draw ends after DRAW_TICKS. */
	public void tickDraw() {
		if (drawMask == 0) return;
		if (++drawTicks >= HemorathWellRules.DRAW_TICKS) stopDraw();
	}

	public void stopDraw() {
		drawMask = 0;
		drawTicks = 0;
	}

	/** The Litany deed: every well refills from the marrow. */
	public void refill() {
		for (int i = 0; i < count; i++) content[i] = HemorathWellRules.litany(content[i]);
	}

	public void save(CompoundTag tag) {
		CompoundTag wells = new CompoundTag();
		for (int i = 0; i < HemorathWellRules.WELLS; i++) {
			wells.putFloat("c" + i, content[i]);
			wells.putLong("at" + i, drawnAt[i]);
		}
		wells.putInt("count", count);
		tag.put("HemorathWells", wells);
	}

	public void load(CompoundTag tag) {
		if (!tag.contains("HemorathWells")) return;
		CompoundTag wells = tag.getCompound("HemorathWells");
		for (int i = 0; i < HemorathWellRules.WELLS; i++) {
			content[i] = wells.getFloat("c" + i);
			drawnAt[i] = wells.getLong("at" + i);
		}
		count = wells.getInt("count");
		drawMask = 0;
	}

	private boolean valid(int well) {
		return well >= 0 && well < count;
	}
}
