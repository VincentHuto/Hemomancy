package com.vincenthuto.hemomancy.common.event;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ScratchEngramInteractionSourceTest {
	@Test
	void normalRightClickReachesTheClickedMachine() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/ScratchEngramHandler.java"));
		int crouchGuard = source.indexOf("if (!player.isShiftKeyDown()) return;");
		int cancellation = source.indexOf("event.setCanceled(true)");

		assertTrue(crouchGuard >= 0, "scratch engraving must require crouching");
		assertTrue(crouchGuard < cancellation, "normal clicks must return before the event is cancelled");
	}
}
