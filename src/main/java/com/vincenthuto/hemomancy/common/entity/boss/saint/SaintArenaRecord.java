package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * The arena's pure record of what a fight changed, by packed block position: each position's original state, taken
 * the first time it changes; the per-cell interval; scheduled edits; timed swaps; edit budgets; and an ordered restore
 * spread over ticks. It knows nothing of the level: {@link World} reads and writes states. Generic over the state so
 * the JVM tests run it on strings.
 */
public final class SaintArenaRecord<S> {
	public interface World<S> {
		S read(long pos);

		/** {@code restoring} writes an original back: without drops and without neighbour updates. */
		void write(long pos, S state, boolean restoring);
	}

	private record Scheduled<S>(long pos, S state, long due) {
	}

	/** A cap on one kind of edit: at most perWindow per windowTicks, and maxRecorded standing (oldest reverts first). */
	public static final class Budget {
		private final String key;
		private final int perWindow;
		private final int windowTicks;
		private final int maxRecorded;
		private long windowStart = Long.MIN_VALUE;
		private int inWindow;
		private final Deque<Long> recorded = new ArrayDeque<>();

		Budget(String key, int perWindow, int windowTicks, int maxRecorded) {
			this.key = key;
			this.perWindow = perWindow;
			this.windowTicks = windowTicks;
			this.maxRecorded = maxRecorded;
		}

		public String key() {
			return key;
		}

		public int standing() {
			return recorded.size();
		}
	}

	private final int minInterval;
	private final Map<Long, S> originals = new LinkedHashMap<>();
	private final Map<Long, Long> lastChange = new HashMap<>();
	private final List<Scheduled<S>> scheduled = new ArrayList<>();
	private final Map<Long, Long> swaps = new LinkedHashMap<>();
	private final Map<String, Budget> budgets = new LinkedHashMap<>();
	private final Deque<Long> restoreQueue = new ArrayDeque<>();
	private int restorePerTick = 1;

	public SaintArenaRecord(int minInterval) {
		this.minInterval = minInterval;
	}

	/** Changes {@code pos}, recording its original the first time; false inside the cell's interval. */
	public boolean set(World<S> world, long pos, S state, long now) {
		Long last = lastChange.get(pos);
		if (last != null && now - last < minInterval) return false;
		if (!originals.containsKey(pos)) originals.put(pos, world.read(pos));
		world.write(pos, state, false);
		lastChange.put(pos, now);
		swaps.remove(pos);
		restoreQueue.remove(pos);
		return true;
	}

	/** A travelling front: the edit lands {@code delay} ticks from now (later if its cell is still in its interval). */
	public void schedule(long pos, S state, int delay, long now) {
		scheduled.add(new Scheduled<>(pos, state, now + Math.max(0, delay)));
	}

	/** Changes {@code pos} for {@code ticks}, then its original returns. */
	public boolean swapFor(World<S> world, long pos, S state, int ticks, long now) {
		if (!set(world, pos, state, now)) return false;
		swaps.put(pos, now + Math.max(1, ticks));
		return true;
	}

	/** Writes the original back and forgets the position; false when it was never changed. */
	public boolean revert(World<S> world, long pos) {
		if (!originals.containsKey(pos)) return false;
		S original = originals.remove(pos);
		world.write(pos, original, true);
		lastChange.remove(pos);
		swaps.remove(pos);
		restoreQueue.remove(pos);
		for (Budget budget : budgets.values()) budget.recorded.remove(pos);
		return true;
	}

	public Budget budget(String key, int perWindow, int windowTicks, int maxRecorded) {
		return budgets.computeIfAbsent(key, ignored -> new Budget(key, perWindow, windowTicks, maxRecorded));
	}

	/** An edit counted against {@code budget}; past maxRecorded the budget's oldest edit reverts first. */
	public boolean edit(World<S> world, Budget budget, long pos, S state, long now) {
		if (budget.windowStart == Long.MIN_VALUE || now - budget.windowStart >= budget.windowTicks) {
			budget.windowStart = now;
			budget.inWindow = 0;
		}
		if (budget.inWindow >= budget.perWindow) return false;
		if (!set(world, pos, state, now)) return false;
		budget.inWindow++;
		budget.recorded.remove(pos);
		budget.recorded.addLast(pos);
		while (budget.recorded.size() > budget.maxRecorded) {
			Long oldest = budget.recorded.pollFirst();
			if (oldest != null) revert(world, oldest);
		}
		return true;
	}

	/** Lands due schedules, ends expired swaps and runs the spread restore. */
	public void tick(World<S> world, long now) {
		if (!scheduled.isEmpty()) {
			Iterator<Scheduled<S>> it = scheduled.iterator();
			List<Scheduled<S>> due = new ArrayList<>();
			while (it.hasNext()) {
				Scheduled<S> next = it.next();
				if (next.due() <= now) {
					due.add(next);
					it.remove();
				}
			}
			for (Scheduled<S> next : due) {
				if (!set(world, next.pos(), next.state(), now)) scheduled.add(new Scheduled<>(next.pos(), next.state(), now + 1));
			}
		}
		if (!swaps.isEmpty()) {
			List<Long> expired = new ArrayList<>();
			swaps.forEach((pos, at) -> {
				if (at <= now) expired.add(pos);
			});
			for (Long pos : expired) revert(world, pos);
		}
		for (int i = 0; i < restorePerTick && !restoreQueue.isEmpty(); i++) {
			Long pos = restoreQueue.pollFirst();
			if (pos != null) revert(world, pos);
		}
	}

	/**
	 * Restores every record: at once for IMMEDIATE or no ticks, else in the order over {@code overTicks}. Pending
	 * schedules and swaps are dropped, so nothing lands after the restore begins.
	 */
	public void restore(World<S> world, SaintArena.RestoreOrder order, int overTicks, ToDoubleFunction<Long> distance) {
		scheduled.clear();
		swaps.clear();
		if (order == SaintArena.RestoreOrder.IMMEDIATE || overTicks <= 0) {
			restoreNow(world);
			return;
		}
		List<Long> positions = new ArrayList<>(originals.keySet());
		Comparator<Long> byDistance = Comparator.comparingDouble(distance);
		positions.sort(order == SaintArena.RestoreOrder.OUTERMOST_FIRST ? byDistance.reversed() : byDistance);
		restoreQueue.clear();
		restoreQueue.addAll(positions);
		restorePerTick = Math.max(1, (positions.size() + overTicks - 1) / overTicks);
	}

	/** Every record back now, in recorded order. */
	public void restoreNow(World<S> world) {
		scheduled.clear();
		swaps.clear();
		restoreQueue.clear();
		for (Long pos : new ArrayList<>(originals.keySet())) revert(world, pos);
	}

	public boolean restoring() {
		return !restoreQueue.isEmpty();
	}

	public boolean clean() {
		return originals.isEmpty() && scheduled.isEmpty();
	}

	public boolean changed(long pos) {
		return originals.containsKey(pos);
	}

	public int size() {
		return originals.size();
	}

	public java.util.Set<Long> positions() {
		return java.util.Collections.unmodifiableSet(originals.keySet());
	}

	public void save(CompoundTag tag, Function<S, CompoundTag> writer) {
		ListTag records = new ListTag();
		originals.forEach((pos, state) -> {
			CompoundTag entry = new CompoundTag();
			entry.putLong("pos", pos);
			entry.put("state", writer.apply(state));
			records.add(entry);
		});
		tag.put("records", records);
		ListTag pending = new ListTag();
		for (Scheduled<S> next : scheduled) {
			CompoundTag entry = new CompoundTag();
			entry.putLong("pos", next.pos());
			entry.put("state", writer.apply(next.state()));
			entry.putLong("due", next.due());
			pending.add(entry);
		}
		tag.put("scheduled", pending);
		ListTag swapList = new ListTag();
		swaps.forEach((pos, at) -> {
			CompoundTag entry = new CompoundTag();
			entry.putLong("pos", pos);
			entry.putLong("at", at);
			swapList.add(entry);
		});
		tag.put("swaps", swapList);
	}

	public void load(CompoundTag tag, Function<CompoundTag, S> reader) {
		originals.clear();
		lastChange.clear();
		scheduled.clear();
		swaps.clear();
		restoreQueue.clear();
		budgets.clear();
		ListTag records = tag.getList("records", Tag.TAG_COMPOUND);
		for (int i = 0; i < records.size(); i++) {
			CompoundTag entry = records.getCompound(i);
			originals.put(entry.getLong("pos"), reader.apply(entry.getCompound("state")));
		}
		ListTag pending = tag.getList("scheduled", Tag.TAG_COMPOUND);
		for (int i = 0; i < pending.size(); i++) {
			CompoundTag entry = pending.getCompound(i);
			scheduled.add(new Scheduled<>(entry.getLong("pos"), reader.apply(entry.getCompound("state")), entry.getLong("due")));
		}
		ListTag swapList = tag.getList("swaps", Tag.TAG_COMPOUND);
		for (int i = 0; i < swapList.size(); i++) {
			CompoundTag entry = swapList.getCompound(i);
			swaps.put(entry.getLong("pos"), entry.getLong("at"));
		}
	}
}
