package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class IchorianSigilRenderPaletteTest {
	@Test
	void innerIchorRetainsItsAuthoredSigilColorInBothRenderPaths() {
		var ground = IchorianSigilRenderPalette.authoredIchor(0x42D9D2, false);
		var awakened = IchorianSigilRenderPalette.authoredIchor(0x42D9D2, false);

		assertEquals(ground, awakened);
		assertEquals(0x42 / 255.0F, ground.red(), 0.00001F);
		assertEquals(0xD9 / 255.0F, ground.green(), 0.00001F);
		assertEquals(0xD2 / 255.0F, ground.blue(), 0.00001F);
	}

	@Test
	void vesselsStayBloodRedRegardlessOfSigilColor() {
		var cyanVessel = IchorianSigilRenderPalette.vessel(false);
		var purpleVessel = IchorianSigilRenderPalette.vessel(false);
		var cyanNode = IchorianSigilRenderPalette.authoredIchor(0x42D9D2, false);

		assertEquals(cyanVessel, purpleVessel);
		assertNotEquals(cyanNode, cyanVessel);
		assertEquals(0.58F, cyanVessel.red(), 0.00001F);
		assertEquals(0.015F, cyanVessel.green(), 0.00001F);
		assertEquals(0.02F, cyanVessel.blue(), 0.00001F);
	}

	@Test
	void darkClottedTissueDominatesTheOuterLandmark() {
		var tissue = IchorianSigilRenderPalette.tissue(false);
		var ichor = IchorianSigilRenderPalette.authoredIchor(0x42D9D2, false);

		assertTrue(tissue.red() < ichor.red());
		assertTrue(tissue.green() < 0.05F);
		assertTrue(tissue.blue() < 0.05F);
	}

	@Test
	void membraneIsPaleAndLowSaturation() {
		var membrane = IchorianSigilRenderPalette.membrane();

		assertTrue(membrane.red() > 0.5F);
		assertTrue(Math.abs(membrane.red() - membrane.green()) < 0.2F);
		assertTrue(Math.abs(membrane.green() - membrane.blue()) < 0.2F);
	}
}
