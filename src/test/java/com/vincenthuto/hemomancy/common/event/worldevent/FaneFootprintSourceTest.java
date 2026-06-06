package com.vincenthuto.hemomancy.common.event.worldevent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FaneFootprintSourceTest {
	private static final Path FOOTPRINT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FaneFootprint.java");
	private static final Path SAVED_DATA = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/event/worldevent/FoundingFaneSavedData.java");

	private FaneFootprintSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String footprint = Files.readString(FOOTPRINT).replace("\r\n", "\n");
		String savedData = Files.readString(SAVED_DATA).replace("\r\n", "\n");

		assertContains("declares heart radius", footprint, "HEART_RADIUS");
		assertContains("declares stake radius", footprint, "STAKE_RADIUS");
		assertContains("declares default stake budget", footprint, "BASE_STAKE_BUDGET");
		assertContains("declares stake budget cap", footprint, "MAX_STAKE_BUDGET");
		assertContains("checks inside footprint", footprint, "boolean contains(BlockPos pos)");
		assertContains("computes effect strength", footprint, "double effectStrength(BlockPos pos)");
		assertContains("validates connected stakes", footprint, "boolean canAddStake(BlockPos pos, int budget)");
		assertContains("computes member budget", footprint, "stakeBudget(int playerMembers, int npcMembers)");
		assertContains("stores heart position", savedData, "BlockPos heart");
		assertContains("stores stake positions", savedData, "List<BlockPos> stakes");
		assertContains("persists heart tag", savedData, "\"Heart\"");
		assertContains("persists stakes tag", savedData, "\"Stakes\"");
		assertContains("exposes footprint snapshot", savedData, "getAllFootprints()");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
