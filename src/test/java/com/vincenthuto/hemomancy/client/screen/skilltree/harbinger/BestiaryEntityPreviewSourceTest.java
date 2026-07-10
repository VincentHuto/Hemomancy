package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BestiaryEntityPreviewSourceTest {
	private BestiaryEntityPreviewSourceTest() {
	}

	public static void main(String[] args) throws Exception {
		String previewSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/BestiaryEntityPreview.java"));
		assertTrue("chitinite should have a preview entity", previewSource.contains("\"chitinite\""));
		assertTrue("crimson doe should have a preview entity", previewSource.contains("\"crimson_doe\""));
		assertTrue("morphling strain keys should not be treated as direct specimens",
				!previewSource.contains("case \"bat\""));

		BestiaryTabState state = new BestiaryTabState();
		state.previewPanX = 20.0F;
		state.previewPanY = -12.0F;
		state.previewZoom = 1.75F;
		state.previewRotationAngle = 1.0F;
		state.previewPitchAngle = 0.5F;
		state.previewDragging = true;
		state.previewDragMode = BestiaryTabState.PreviewDragMode.ROTATE;
		state.resetPreviewView();

		assertEquals("pan x resets", 0.0F, state.previewPanX);
		assertEquals("pan y resets", 0.0F, state.previewPanY);
		assertEquals("zoom resets", 1.0F, state.previewZoom);
		assertEquals("rotation resets", 0.0F, state.previewRotationAngle);
		assertEquals("pitch resets", 0.0F, state.previewPitchAngle);
		assertTrue("dragging resets", !state.previewDragging);
		assertTrue("drag mode resets", state.previewDragMode == BestiaryTabState.PreviewDragMode.NONE);

		assertTrue("new state should not have a selected preview",
				!BestiaryTabView.hasPreviewForSelected(state));
		assertTrue("null context cannot be over a preview",
				!BestiaryTabView.isOverPreview(null, state, 0.0D, 0.0D));

		state.previewDragMode = BestiaryTabState.PreviewDragMode.ROTATE;
		BestiaryTabController.applyPreviewRotation(state, 14.0D, -9.0D);
		assertEquals("rotation changes with horizontal movement", 0.28F, state.previewRotationAngle);
		assertEquals("pitch changes with vertical movement", 0.18F, state.previewPitchAngle);
		assertEquals("pan x stays fixed during rotation", 0.0F, state.previewPanX);
		assertEquals("pan y stays fixed during rotation", 0.0F, state.previewPanY);
		assertTrue("preview controls should only offer no-op and rotate modes",
				BestiaryTabState.PreviewDragMode.values().length == 2);
		assertEquals("hint stays full size when it fits", 1.0F, BestiaryTabView.previewHintScale(80, 90));
		assertEquals("hint shrinks when it would overflow", 0.75F, BestiaryTabView.previewHintScale(120, 90));
		assertTrue("animation age combines world time and partial tick",
				previewSource.contains("gameTime + partialTick"));
		assertTrue("preview animation tick loops instead of using huge absolute world time",
				previewSource.contains("gameTime % ANIMATION_LOOP_TICKS"));
		assertTrue("preview uses a deeper gui depth window than vanilla inventory preview",
				previewSource.contains("PREVIEW_GUI_DEPTH = 150.0F"));
		assertTrue("preview renderer should animate using client partial tick time",
				previewSource.contains("getGameTimeDeltaPartialTick"));
		assertTrue("preview renderer should keep vanilla's stable preview camera orientation",
				previewSource.contains("overrideCameraOrientation"));
	}

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}

	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
