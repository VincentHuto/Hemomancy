package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class TendonLineRendererSourceTest {
	private static final Path CLIENT_EVENTS = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/WorldEffectDispatcher.java");
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/TendonLineRenderer.java");

	private TendonLineRendererSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String events = Files.readString(CLIENT_EVENTS);
		assertContains("tendon line renderer is called during translucent world render", events,
				"TendonLineRenderer.render(event.getPoseStack(), partialTick);");

		if (!Files.exists(RENDERER)) {
			throw new AssertionError("missing " + RENDERER);
		}
		String renderer = Files.readString(RENDERER);
		assertContains("renderer reads the tendon anchor from the player", renderer, "HemoCapabilityAccess.getTendonAnchor");
		assertNotContains("the Tendon Line is no longer an item", renderer, "TendonLineItem");
		assertContains("renderer opens the toolkit batch", renderer, "OutpouringBrushes.begin(time)");
		assertContains("renderer flushes the toolkit batch", renderer, "OutpouringBrushes.flush(");
		assertNotContains("no hairline strip for a focal tool", renderer, "RenderType.lineStrip()");
		// Round two P11: every rooted line in the dimension is drawn (anchors reach trackers), from the drawn palm, as a
		// sheathed body with one wound sinew, not from a guessed hand point.
		assertContains("renderer draws every player's line", renderer, "mc.level.players()");
		assertContains("renderer starts the cord at the drawn palm", renderer, "CellHandStreams.palm(");
		assertNotContains("no guessed hand point", renderer, "HAND_FORWARD_OFFSET");
		assertContains("the cord has a soft sheath", renderer, "OutpouringBrushes.WISP");
		assertContains("one sinew wound along the whole cord", renderer, "sinew(path, time, pulseSpeed)");
		assertContains("a haul pulls the cord taut", renderer, "VerbChannel.isChannelling(player, Verb.ABSORB)");
	}
}
