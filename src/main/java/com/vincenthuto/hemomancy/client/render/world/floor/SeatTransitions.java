package com.vincenthuto.hemomancy.client.render.world.floor;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * How far each seated brazier's mound and moat have grown, so a brazier set on the floor or taken off it eases the
 * trench out round it and back instead of snapping. Each cell eases from where it is toward whole (1) or gone (0)
 * over {@link #TICKS}; a brazier put back while its mound is still sinking carries on from there. Pure state, keyed
 * by game tick.
 */
final class SeatTransitions {
	/** Game ticks for a mound and its moat to grow in or sink away. */
	static final int TICKS = 24;

	private final Map<BlockPos, Easing> cells = new HashMap<>();

	private SeatTransitions() {
	}

	/** Braziers already standing when the floor is first seen: whole at once, with nothing to animate. */
	static SeatTransitions settledWith(Set<BlockPos> present) {
		SeatTransitions transitions = new SeatTransitions();
		for (BlockPos cell : present) transitions.cells.put(cell.immutable(), new Easing(1.0F, 1.0F, Long.MIN_VALUE));
		return transitions;
	}

	/** Sets which cells hold a seated brazier now; any that changed start easing from where they are. */
	void target(Set<BlockPos> present, long tick) {
		for (BlockPos cell : present) {
			Easing easing = cells.get(cell);
			if (easing == null) cells.put(cell.immutable(), new Easing(0.0F, 1.0F, tick));
			else if (easing.to < 1.0F) cells.put(cell, new Easing(easing.at(tick), 1.0F, tick));
		}
		for (Map.Entry<BlockPos, Easing> entry : cells.entrySet()) {
			Easing easing = entry.getValue();
			if (!present.contains(entry.getKey()) && easing.to > 0.0F) {
				entry.setValue(new Easing(easing.at(tick), 0.0F, tick));
			}
		}
	}

	/** How far this cell's mound and moat have grown at {@code tick}, 0 to 1. */
	float weight(BlockPos cell, long tick) {
		Easing easing = cells.get(cell);
		return easing == null ? 0.0F : easing.at(tick);
	}

	/** Cells with a mound to draw: standing braziers and those still sinking away. */
	Set<BlockPos> cells() {
		return cells.keySet();
	}

	/** Whether nothing is easing at {@code tick}. */
	boolean settled(long tick) {
		for (Easing easing : cells.values()) {
			if (easing.at(tick) != easing.to) return false;
		}
		return true;
	}

	/** Forgets cells whose mound has sunk away. */
	void prune(long tick) {
		cells.entrySet().removeIf(entry -> entry.getValue().to == 0.0F && entry.getValue().at(tick) == 0.0F);
	}

	private record Easing(float from, float to, long start) {
		float at(long tick) {
			if (start == Long.MIN_VALUE || tick - start >= TICKS) return to;
			float t = Math.max(0.0F, (tick - start) / (float) TICKS);
			float eased = t * t * (3.0F - 2.0F * t);
			return from + (to - from) * eased;
		}
	}
}
