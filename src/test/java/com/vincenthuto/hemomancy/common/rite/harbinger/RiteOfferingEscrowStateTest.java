package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class RiteOfferingEscrowStateTest {
	@Test
	void openingEscrowRecordsAnIdentityAndAnEmptyLedger() {
		CompoundTag state = new CompoundTag();
		var identity = RiteOfferingEscrow.open(state);
		assertTrue(RiteOfferingEscrow.isEscrowed(state));
		assertFalse(RiteOfferingEscrow.isConsumed(state));
		assertEquals(identity, state.getUUID("RiteIdentity"));
		assertTrue(state.getList("Offerings", 10).isEmpty());
	}

	@Test
	void consumingSettlesTheEscrowOnce() {
		CompoundTag state = new CompoundTag();
		RiteOfferingEscrow.open(state);
		RiteOfferingEscrow.markConsumed(state);
		assertTrue(RiteOfferingEscrow.isConsumed(state));
		assertFalse(RiteOfferingEscrow.isEscrowed(state));
		assertFalse(state.contains("Offerings"), "consumed offerings are gone, not returnable");
	}

	@Test
	void anUnopenedStateIsNeitherEscrowedNorConsumed() {
		CompoundTag state = new CompoundTag();
		assertFalse(RiteOfferingEscrow.isEscrowed(state));
		assertFalse(RiteOfferingEscrow.isConsumed(state));
	}
}
