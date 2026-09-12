package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class BlackVeilRendererSourceTest {
	private static final Path RENDERER = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/client/render/world/BlackVeilRenderer.java");
	private BlackVeilRendererSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String renderer = Files.readString(RENDERER).replace("\r\n", "\n");
		assertContains("black veil should render its authored synthetic-darkness ground",
				renderer, "LuxUmbraGeometry.ground");
		assertContains("black veil should retain a readable owned boundary",
				renderer, "BloodCraftRingRenderer.drawBoundary");
		assertNotContains("black veil should not carry a separate Fane-like shader profile",
				renderer, "BLACK_VEIL_CORE_ALPHA");
		assertNotContains("black veil should not carry custom thread width tuning",
				renderer, "BLACK_VEIL_CORE_THREAD_WIDTH");
		assertNotContains("black veil should not render its own sphere mesh",
				renderer, "drawVeilSphere");
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
