package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SporeLoadAttachmentTest {
	@Test
	void loadClampsAndOnlyChangesMarkTheOwnerSyncDirty() {
		SporeLoad spores = new SporeLoad();
		assertFalse(spores.isSyncDirty());
		spores.add(140f);
		assertEquals(100f, spores.load());
		assertTrue(spores.isSyncDirty());
		spores.clearSyncDirty();
		spores.add(5f);
		assertFalse(spores.isSyncDirty(), "a full load that cannot grow is not a change");
	}

	@Test
	void movementIsMeasuredFromWhereTheBodyWasLastTick() {
		SporeLoad spores = new SporeLoad();
		assertEquals(0.0, spores.moveSinceLastTick(10.0, 5.0), "the first tick has nothing to compare");
		assertEquals(0.0, spores.moveSinceLastTick(10.0, 5.0));
		assertEquals(0.25, spores.moveSinceLastTick(10.3, 5.4), 1e-9);
	}

	@Test
	void spendingRefusesAShortLoadAndKeepsIt() {
		SporeLoad spores = new SporeLoad();
		spores.add(20f);
		assertFalse(spores.spend(25f));
		assertEquals(20f, spores.load());
		assertTrue(spores.spend(20f));
		assertEquals(0f, spores.load());
	}

	@Test
	void theLoadSurvivesSaveAndLoad() {
		SporeLoad spores = new SporeLoad();
		spores.add(42.5f);
		spores.setStillTicks(30);
		SporeLoad restored = new SporeLoad();
		restored.deserializeNBT(null, spores.serializeNBT(null));
		assertEquals(42.5f, restored.load());
		assertEquals(0, restored.stillTicks(), "stillness is never saved");
	}
}
