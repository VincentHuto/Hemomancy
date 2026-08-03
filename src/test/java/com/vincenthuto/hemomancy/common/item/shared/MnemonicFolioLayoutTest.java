package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MnemonicFolioLayoutTest {
	@Test
	void folioProvidesThirtySlotsInThreeRowsOfTen() {
		assertEquals(30, MnemonicFolioLayout.SLOT_COUNT);
		assertEquals(10, MnemonicFolioLayout.COLUMNS);
		assertEquals(3, MnemonicFolioLayout.ROWS);
		assertEquals(new MnemonicFolioLayout.Point(7, 18), MnemonicFolioLayout.folioSlot(0));
		assertEquals(new MnemonicFolioLayout.Point(169, 54), MnemonicFolioLayout.folioSlot(29));
	}

	@Test
	void playerInventoryFitsBelowTheFolio() {
		assertEquals(new MnemonicFolioLayout.Point(16, 90), MnemonicFolioLayout.playerSlot(0));
		assertEquals(new MnemonicFolioLayout.Point(160, 126), MnemonicFolioLayout.playerSlot(26));
		assertEquals(new MnemonicFolioLayout.Point(16, 148), MnemonicFolioLayout.hotbarSlot(0));
		assertEquals(new MnemonicFolioLayout.Point(160, 148), MnemonicFolioLayout.hotbarSlot(8));
	}
}
