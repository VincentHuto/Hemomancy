package com.vincenthuto.hemomancy.common.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FungalProjectionSourceTest {
	private FungalProjectionSourceTest() {}

	public static void main(String[] args) throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/worldgen/FungalGardenTravelHelper.java");
		String events = read("src/main/java/com/vincenthuto/hemomancy/common/event/FungalProjectionEvents.java");
		String overlay = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/FungalWhisperVignetteOverlay.java");
		String clientEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/FungalProjectionClientEvents.java");
		assertContains(helper, "FungalProjectionRules.FIRST_VISIT_TICKS");
		assertContains(events, "performForcedProjectionReturn(player)");
		assertContains(events, "event.setCanceled(true)");
		assertContains(overlay, "projectionRemainingTicks");
		assertContains(overlay, "230");
		assertContains(overlay, "20");
		assertContains(clientEvents, "InventoryScreen");
		assertContains(clientEvents, "event.setCanceled(true)");
	}

	private static String read(String path) throws IOException { return Files.readString(Path.of(path)); }
	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
}
