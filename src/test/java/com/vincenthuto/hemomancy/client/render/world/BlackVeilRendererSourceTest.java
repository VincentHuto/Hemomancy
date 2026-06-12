package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BlackVeilRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/BlackVeilRenderer.java");
	private static final Path FANE_RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/FaneBoundaryRenderer.java");

	private BlackVeilRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		String faneRenderer = Files.readString(FANE_RENDERER).replace("\r\n", "\n");

		assertContains("black veil should delegate to the exact Fane Sight: Revealed renderer",
				renderer, "FaneBoundaryRenderer.drawRevealedFaneStyleDome");
		assertContains("Fane renderer should expose a reusable revealed-dome helper",
				faneRenderer, "drawRevealedFaneStyleDome");
		assertContains("revealed-dome helper should use the OUTSIDER relation style",
				faneRenderer, "ShellStyle.forRelation(FaneBoundaryRelation.OUTSIDER");
		assertContains("revealed-dome helper should keep the same glow pass as Fane Sight: Revealed",
				faneRenderer, "style.withAlpha(style.alpha() * 0.34F)");
		assertNotContains("black veil should not carry a separate Fane-like shader profile",
				renderer, "BLACK_VEIL_CORE_ALPHA");
		assertNotContains("black veil should not carry custom thread width tuning",
				renderer, "BLACK_VEIL_CORE_THREAD_WIDTH");
		assertNotContains("black veil should not render its own sphere mesh",
				renderer, "drawVeilSphere");
		assertNotContains("black veil should not use old rite-boundary ring core",
				renderer, "RITE_BOUNDARY_CORE");
		assertNotContains("black veil should not use old rite-boundary ring glow",
				renderer, "RITE_BOUNDARY_GLOW");
		assertNotContains("black veil should not render the old horizontal ring lattice",
				renderer, "drawRing");
		assertNotContains("black veil should not render the old hanging vein lattice",
				renderer, "drawVein");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertNotContains(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + ": still contains " + unexpected);
		}
	}
}
