package com.vincenthuto.hemomancy.common.rite.zone;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Where a rite space is made and released: a completed rite makes it; only its floor or Focus going ends it. */
final class RiteSpaceSourceTest {
	private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void onlyACompletedRiteConsecratesItsSpace() throws IOException {
		String termination = Files.readString(MAIN.resolve("common/rite/harbinger/CardinalRiteTermination.java"));
		int completed = termination.indexOf("if (outcome == Outcome.COMPLETED)");
		int consecrate = termination.indexOf("RiteSpaces.consecrate(level, rite");
		assertTrue(completed >= 0 && consecrate > completed, "the space is consecrated inside the COMPLETED branch");
	}

	@Test
	void removingTheFocusReleasesButUnloadingItsChunkDoesNot() throws IOException {
		String block = Files.readString(MAIN.resolve("common/block/harbinger/functional/CardinalFocusBlock.java"));
		int replaced = block.indexOf("if (!state.is(newState.getBlock())");
		int release = block.indexOf("RiteSpaces.release(");
		assertTrue(replaced >= 0 && release > replaced, "release only when the Focus block is actually replaced");
		String entity = Files.readString(MAIN.resolve("common/tile/harbinger/functional/CardinalFocusBlockEntity.java"));
		assertTrue(entity.contains("RiteSpaces.onFloorCheck("), "the periodic floor check releases a space whose floor changed");
		assertFalse(entity.contains("setRemoved"), "chunk unload calls setRemoved; it must not end a space");
	}

	@Test
	void riteSpacesAreSyncedLikeTheOtherZones() throws IOException {
		String handler = Files.readString(MAIN.resolve("common/network/PacketHandler.java"));
		assertTrue(handler.contains("RiteSpaceSyncPacket.TYPE"), "the rite space packet is registered");
		String events = Files.readString(MAIN.resolve("common/rite/zone/RiteZoneEvents.java"));
		assertTrue(events.contains("RiteSpaces.sendTo("), "players learn the spaces of their dimension on login and travel");
	}
}
