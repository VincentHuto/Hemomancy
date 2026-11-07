package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.BossEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Phases by health, meter or the Saint; the ratchet; queued against immediate changes; cards once; save and load. */
final class SaintPhaseTableTest {
	private static final ResourceLocation CARD = ResourceLocation.fromNamespaceAndPath("hemomancy", "test_card");

	private static SaintPhaseTable.Phase phase(int number, float at, ResourceLocation card) {
		return new SaintPhaseTable.Phase(number, "boss.test.phase_" + number, BossEvent.BossBarColor.RED, at, card);
	}

	@Test
	void healthPhasesFallThroughTheirThresholds() {
		SaintPhaseTable table = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.6F, CARD), phase(3, 0.25F, null));
		assertEquals(1, table.target(0.61F, 0.0F, 0));
		assertEquals(2, table.target(0.6F, 0.0F, 0));
		assertEquals(3, table.target(0.1F, 0.0F, 0));
		assertEquals(1, table.target(0.9F, 0.0F, 0), "an unratcheted table can go back");
	}

	@Test
	void meterAndSaintSources() {
		SaintPhaseTable meter = SaintPhaseTable.byMeter(phase(1, 0.0F, null), phase(2, 0.5F, null), phase(3, 0.9F, null));
		assertEquals(1, meter.target(1.0F, 0.49F, 0));
		assertEquals(2, meter.target(1.0F, 0.5F, 0));
		assertEquals(3, meter.target(1.0F, 0.95F, 0));
		SaintPhaseTable saint = SaintPhaseTable.bySaint(s -> 0, phase(1, 0.0F, null), phase(2, 0.0F, null));
		assertEquals(2, saint.target(1.0F, 0.0F, 2), "the Saint names its phase");
		assertEquals(SaintPhaseTable.Source.SAINT, saint.source());
	}

	@Test
	void theRatchetKeepsAPhaseOnceReached() {
		SaintPhaseTable table = SaintPhaseTable.byMeter(phase(1, 0.0F, null), phase(2, 0.5F, null)).ratchet();
		assertTrue(table.update(table.target(1.0F, 0.6F, 0), true));
		assertEquals(2, table.phase());
		assertEquals(2, table.target(1.0F, 0.1F, 0), "never back down");
	}

	@Test
	void changesQueueToTheBoundaryUnlessImmediate() {
		SaintPhaseTable table = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, null));
		assertFalse(table.update(2, false), "mid-move: the change waits");
		assertTrue(table.pending());
		assertTrue(table.update(2, true), "at the boundary it lands");
		assertEquals(2, table.phase());
		assertFalse(table.pending());
		SaintPhaseTable dropped = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, null));
		dropped.update(2, false);
		dropped.update(1, false);
		assertFalse(dropped.pending(), "a queued change the source takes back is dropped");
		SaintPhaseTable forced = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, null));
		forced.advance(2, SaintCombatController.Cut.IMMEDIATE);
		assertTrue(forced.update(forced.target(1.0F, 0.0F, 0), false), "an immediate advance does not wait");
		assertEquals(2, forced.target(1.0F, 0.0F, 0), "and the source cannot take it back");
		SaintPhaseTable queued = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, null));
		queued.advance(2, SaintCombatController.Cut.QUEUE);
		assertFalse(queued.update(queued.target(1.0F, 0.0F, 0), false));
		assertTrue(queued.update(queued.target(1.0F, 0.0F, 0), true));
		assertEquals(0, queued.ticksInPhase());
	}

	@Test
	void cardsPlayOnce() {
		SaintPhaseTable table = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, CARD));
		assertTrue(table.playCardOnce(2));
		assertEquals(SaintPhaseTable.CARD_TICKS, table.cardTicks());
		assertFalse(table.playCardOnce(2), "a card never plays twice");
		table.update(1, true);
		assertEquals(SaintPhaseTable.CARD_TICKS - 1, table.cardTicks(), "the card's time runs down");
	}

	@Test
	void saveAndLoad() {
		SaintPhaseTable table = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, CARD), phase(3, 0.2F, null));
		table.advance(2, SaintCombatController.Cut.QUEUE);
		table.update(2, true);
		table.playCardOnce(2);
		CompoundTag tag = new CompoundTag();
		table.save(tag);
		SaintPhaseTable loaded = SaintPhaseTable.byHealth(phase(1, 1.0F, null), phase(2, 0.5F, CARD),
				phase(3, 0.2F, null));
		loaded.load(tag);
		assertEquals(2, loaded.phase());
		assertEquals("boss.test.phase_2", loaded.current().nameKey());
		assertFalse(loaded.playCardOnce(2), "the played card is saved");
		assertEquals(2, loaded.target(1.0F, 0.0F, 0), "the forced floor is saved");
	}
}
