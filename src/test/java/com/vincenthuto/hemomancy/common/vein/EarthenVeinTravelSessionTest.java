package com.vincenthuto.hemomancy.common.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class EarthenVeinTravelSessionTest {
	private static final ResourceLocation OVERWORLD = ResourceLocation.withDefaultNamespace("overworld");
	private static final UUID OWNER = UUID.randomUUID();
	private static final UUID OTHER = UUID.randomUUID();

	@Test
	void selectionReservesTheSourceAndOnlyItsOwnerCanFeedIt() {
		var destination = new EarthenVeinDestination(UUID.randomUUID(), "Far Vein", OVERWORLD,
				new BlockPos(900, 70, 0), 1_000);
		var session = EarthenVeinTravelSession.selecting(OWNER, OVERWORLD, BlockPos.ZERO, false, 100L);

		assertEquals(EarthenVeinTravelPhase.SELECTING, session.phase());
		assertTrue(session.select(OWNER, destination, 110L));
		assertEquals(EarthenVeinTravelPhase.FEEDING, session.phase());
		assertEquals(0.0D, session.feed(OTHER, 100.0D, 100.0D, 111L));
		assertEquals(100.0D, session.feed(OWNER, 100.0D, 100.0D, 111L));
	}

	@Test
	void exactFinalFeedChangesTheSessionToReady() {
		var destination = new EarthenVeinDestination(UUID.randomUUID(), "Near Vein", OVERWORLD,
				new BlockPos(10, 64, 0), 500);
		var session = EarthenVeinTravelSession.selecting(OWNER, OVERWORLD, BlockPos.ZERO, true, 100L);
		session.select(OWNER, destination, 101L);

		assertEquals(450.0D, session.feed(OWNER, 500.0D, 450.0D, 102L));
		assertEquals(50.0D, session.feed(OWNER, 100.0D, 100.0D, 103L));
		assertEquals(500.0D, session.fedBlood());
		assertEquals(EarthenVeinTravelPhase.READY, session.phase());
		assertFalse(session.select(OWNER, destination, 104L));
	}

	@Test
	void cinematicTransitionsHaveFixedDurations() {
		var destination = new EarthenVeinDestination(UUID.randomUUID(), "Near Vein", OVERWORLD,
				new BlockPos(10, 64, 0), 500);
		var session = EarthenVeinTravelSession.selecting(OWNER, OVERWORLD, BlockPos.ZERO, false, 100L);
		session.select(OWNER, destination, 101L);
		session.feed(OWNER, 500.0D, 500.0D, 102L);

		assertTrue(session.beginSwallow(OWNER, 200L));
		assertFalse(session.swallowComplete(200L + EarthenVeinTravelRules.SWALLOW_TICKS - 1L));
		assertTrue(session.swallowComplete(200L + EarthenVeinTravelRules.SWALLOW_TICKS));
		session.beginEjection(300L);
		assertFalse(session.ejectionComplete(300L + EarthenVeinTravelRules.EJECTION_TICKS - 1L));
		assertTrue(session.ejectionComplete(300L + EarthenVeinTravelRules.EJECTION_TICKS));
	}

	@Test
	void permanentSourceIdentityIsRetainedForReplacementValidation() {
		UUID sourceVeinId = UUID.randomUUID();
		var session = EarthenVeinTravelSession.selecting(OWNER, OVERWORLD, BlockPos.ZERO,
				false, sourceVeinId, 100L);

		assertEquals(sourceVeinId, session.sourceVeinId());
	}
}
