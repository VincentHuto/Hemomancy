package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer.Field;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer.Palette;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer.Speckles;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer.Style;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The shared vein background's named styles. Each one reproduces the private copy it replaced, value for value, so a
 * changed constant here changes pixels on the screens that use it.
 */
final class VeinBackgroundStyleTest {
	@Test
	void progressStyleDeepensToBruisedViolet() {
		assertEquals(new Field(28, 42L, 0.3f, 0.7f, 8f, 18f, 60, 120, 3, 1, 0, true), Style.PROGRESS.field());
		assertEquals(0xFF0A0204, Style.PROGRESS.palette().background());
		assertEquals(0xFF0E020D, Style.PROGRESS.deep().background());
		assertEquals(34f, Style.PROGRESS.deep().redBase());
		assertEquals(24f, Style.PROGRESS.deep().redBright());
		assertEquals(0.86f, Style.PROGRESS.deep().redScale());
		assertEquals(0.74f, Style.PROGRESS.deep().blueScale());
		assertEquals(new Speckles.Deepening(12345L, 120), Style.PROGRESS.speckles());
		assertFalse(Style.PROGRESS.fillOrder(), "the progress screens were batched before they shared the renderer");
	}

	@Test
	void crimsonStyleIsTheHarbingerScreensField() {
		assertEquals(Field.STANDARD, Style.CRIMSON.field());
		assertEquals(new Field(28, 42L, 0.3f, 0.7f, 8f, 18f, 60, 120, 3, 1, 0, false), Field.STANDARD);
		assertEquals(new Palette(0xFF0A0204, 4, 35, 40, 0, 0, 40, 50, 2, 8, 5, 5, 1f, 0.3f), Style.CRIMSON.palette());
		assertSame(Style.CRIMSON.palette(), Style.CRIMSON.deep());
		assertEquals(new Speckles.Scattered(12345L, 120, 10, 20, 6, 15, 25), Style.CRIMSON.speckles());
		assertTrue(Style.CRIMSON.fillOrder(), "the crimson screens filled each rect, so they keep fill's triangle split");
	}

	@Test
	void machineStyleIsFinerAndDimmer() {
		assertEquals(new Field(20, 27182L, 0.3f, 0.7f, 8f, 16f, 50, 110, 2, 1, 0, false), Style.MACHINE.field());
		assertEquals(Palette.CRIMSON.rings(30, 35, 0, 0), Style.MACHINE.palette());
		assertEquals(new Speckles.Scattered(13579L, 70, 10, 20, 6, 15, 20), Style.MACHINE.speckles());
		assertEquals(new Field(16, 31415L, 0.3f, 0.7f, 8f, 14f, 50, 100, 2, 1, 0, false), Field.STATION);
	}

	@Test
	void buildersChangeOnlyTheirComponent() {
		assertEquals(new Field(18, 77L, 0.3f, 0.7f, 8f, 18f, 60, 120, 3, 1, 0, false), Field.STANDARD.count(18).seed(77L));
		assertEquals(new Speckles.Scattered(54321L, 60, 10, 20, 6, 15, 20), Speckles.Scattered.MACHINE.seed(54321L).count(60));
		Palette panel = Palette.CRIMSON.background(0xF00A0204).ringStep(12);
		assertEquals(0xF00A0204, panel.background());
		assertEquals(12, panel.ringStep());
		assertEquals(Palette.CRIMSON.tendrils(40, 50, 2, 8, 5, 5), Palette.CRIMSON);
		assertInstanceOf(Speckles.Fixed.class, new Speckles.Fixed(1L, 1, 1, 1, 0, 1, 0, 4, 0, 1));
	}
}
