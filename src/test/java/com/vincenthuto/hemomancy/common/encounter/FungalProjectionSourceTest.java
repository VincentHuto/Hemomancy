package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class FungalProjectionSourceTest {
	private FungalProjectionSourceTest() {}

	@Test
	void allChecksPass() throws IOException {
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/encounter/FungalGardenTravelHelper.java");
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
	private static void assertContains(String source, String expected) {
		if (!source.contains(expected)) throw new AssertionError("missing " + expected);
	}
}
