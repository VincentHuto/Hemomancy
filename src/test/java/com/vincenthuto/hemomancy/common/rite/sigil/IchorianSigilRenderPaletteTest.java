package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class IchorianSigilRenderPaletteTest {
	@Test
	void nodesRetainTheirAuthoredSigilColor() {
		var cyan = IchorianSigilRenderPalette.node(0x42D9D2, false);

		assertEquals(0x42 / 255.0F, cyan.red(), 0.00001F);
		assertEquals(0xD9 / 255.0F, cyan.green(), 0.00001F);
		assertEquals(0xD2 / 255.0F, cyan.blue(), 0.00001F);
	}

	@Test
	void vesselsStayBloodRedRegardlessOfSigilColor() {
		var cyanVessel = IchorianSigilRenderPalette.vessel(false);
		var purpleVessel = IchorianSigilRenderPalette.vessel(false);
		var cyanNode = IchorianSigilRenderPalette.node(0x42D9D2, false);

		assertEquals(cyanVessel, purpleVessel);
		assertNotEquals(cyanNode, cyanVessel);
		assertEquals(0.58F, cyanVessel.red(), 0.00001F);
		assertEquals(0.015F, cyanVessel.green(), 0.00001F);
		assertEquals(0.02F, cyanVessel.blue(), 0.00001F);
	}
}
