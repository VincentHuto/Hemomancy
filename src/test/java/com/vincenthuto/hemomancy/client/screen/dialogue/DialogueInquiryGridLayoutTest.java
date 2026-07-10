package com.vincenthuto.hemomancy.client.screen.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DialogueInquiryGridLayoutTest {
	@Test
	void inquiryItemsUseCompactWrappingIconCells() {
		DialogueLayout.Rect content = DialogueLayout.calculate(854, 480).content();
		DialogueInquiryGridLayout grid = DialogueInquiryGridLayout.calculate(content, 12);

		assertEquals(12, grid.cells().size());
		assertEquals(28, grid.cells().getFirst().width());
		assertEquals(28, grid.cells().getFirst().height());
		assertTrue(grid.columns() >= 6);
		assertEquals(grid.cells().getFirst().y(), grid.cells().get(1).y());
		assertTrue(grid.cells().get(grid.columns()).y() > grid.cells().getFirst().y());
		assertTrue(grid.cells().stream().allMatch(cell -> cell.x() >= content.x()
				&& cell.right() <= content.right()));
	}
}
