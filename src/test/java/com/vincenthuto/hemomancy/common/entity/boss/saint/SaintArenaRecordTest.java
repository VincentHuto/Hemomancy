package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The arena's pure record on string states: the original recorded once, the per-cell interval, schedules, timed swaps,
 * budgets reverting their oldest edit, the three restore orders spread over ticks, and save and load.
 */
final class SaintArenaRecordTest {
	/** A row of cells 0..n with stone everywhere, logging every restoring write. */
	private static final class Row implements SaintArenaRecord.World<String> {
		final Map<Long, String> cells = new HashMap<>();
		final List<Long> restored = new ArrayList<>();

		@Override
		public String read(long pos) {
			return cells.getOrDefault(pos, "stone");
		}

		@Override
		public void write(long pos, String state, boolean restoring) {
			cells.put(pos, state);
			if (restoring) restored.add(pos);
		}
	}

	private static SaintArenaRecord<String> record() {
		return new SaintArenaRecord<>(SaintArena.MIN_CELL_INTERVAL);
	}

	@Test
	void theOriginalIsRecordedOnceAndTheIntervalHolds() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		assertTrue(record.set(row, 1, "blood", 0));
		assertFalse(record.set(row, 1, "iron", 5), "one change per cell per 10 ticks");
		assertTrue(record.set(row, 1, "iron", 10));
		assertEquals("iron", row.read(1));
		record.restoreNow(row);
		assertEquals("stone", row.read(1), "the first original, not the blood");
		assertTrue(record.clean());
	}

	@Test
	void aPositionAPlayerChangedSinceIsStillRestored() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		record.set(row, 4, "ash", 0);
		row.cells.put(4L, "player_torch");
		record.restoreNow(row);
		assertEquals("stone", row.read(4));
	}

	@Test
	void schedulesLandAndSwapsReturn() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		record.schedule(2, "spike", 3, 0);
		record.tick(row, 2);
		assertEquals("stone", row.read(2));
		record.tick(row, 3);
		assertEquals("spike", row.read(2), "a travelling front lands on its tick");
		record.swapFor(row, 5, "glow", 20, 0);
		record.tick(row, 19);
		assertEquals("glow", row.read(5));
		record.tick(row, 20);
		assertEquals("stone", row.read(5), "the original returns when the swap runs out");
		assertFalse(record.changed(5));
	}

	@Test
	void budgetsCapTheirWindowAndRevertTheOldest() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		SaintArenaRecord.Budget budget = record.budget("colonies", 2, 20, 3);
		assertTrue(record.edit(row, budget, 1, "colony", 0));
		assertTrue(record.edit(row, budget, 2, "colony", 0));
		assertFalse(record.edit(row, budget, 3, "colony", 1), "two a window");
		assertTrue(record.edit(row, budget, 3, "colony", 20));
		assertTrue(record.edit(row, budget, 4, "colony", 21));
		assertEquals("stone", row.read(1), "past three standing, the oldest reverts first");
		assertEquals("colony", row.read(4));
		assertEquals(3, budget.standing());
	}

	@Test
	void restoreOrdersSpreadOverTicks() {
		for (SaintArena.RestoreOrder order : SaintArena.RestoreOrder.values()) {
			Row row = new Row();
			SaintArenaRecord<String> record = record();
			for (long pos = 1; pos <= 6; pos++) record.set(row, pos, "healed", 0);
			record.restore(row, order, 3, pos -> (double) pos);
			if (order == SaintArena.RestoreOrder.IMMEDIATE) {
				assertTrue(record.clean(), "immediate restores everything at once");
				continue;
			}
			assertTrue(record.restoring());
			record.tick(row, 100);
			assertEquals(2, row.restored.size(), "six over three ticks: two a tick");
			record.tick(row, 101);
			record.tick(row, 102);
			assertFalse(record.restoring());
			List<Long> expected = order == SaintArena.RestoreOrder.OUTERMOST_FIRST ? List.of(6L, 5L, 4L, 3L, 2L, 1L)
					: List.of(1L, 2L, 3L, 4L, 5L, 6L);
			assertEquals(expected, row.restored, order.name());
		}
	}

	@Test
	void aRestoreDropsPendingSchedulesAndSwaps() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		record.schedule(7, "spike", 5, 0);
		record.swapFor(row, 8, "glow", 50, 0);
		record.restore(row, SaintArena.RestoreOrder.IMMEDIATE, 0, pos -> 0.0D);
		record.tick(row, 10);
		assertEquals("stone", row.read(7), "nothing lands after the restore begins");
		assertEquals("stone", row.read(8));
	}

	@Test
	void saveAndLoad() {
		Row row = new Row();
		SaintArenaRecord<String> record = record();
		record.set(row, 1, "blood", 0);
		record.schedule(2, "spike", 5, 0);
		record.swapFor(row, 3, "glow", 30, 0);
		CompoundTag tag = new CompoundTag();
		record.save(tag, state -> {
			CompoundTag entry = new CompoundTag();
			entry.putString("s", state);
			return entry;
		});
		SaintArenaRecord<String> loaded = record();
		loaded.load(tag, entry -> entry.getString("s"));
		assertEquals(2, loaded.size());
		loaded.tick(row, 5);
		assertEquals("spike", row.read(2), "a saved schedule still lands");
		loaded.restoreNow(row);
		assertEquals("stone", row.read(1));
		assertEquals("stone", row.read(2));
		assertEquals("stone", row.read(3));
	}
}
