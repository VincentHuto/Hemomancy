package com.vincenthuto.hemomancy.client.screen.dialogue;

import java.util.ArrayList;
import java.util.List;

public record DialogueInquiryGridLayout(List<DialogueLayout.Rect> cells, int columns, int contentHeight) {
	private static final int PAD = 6;
	private static final int GAP = 4;
	private static final int CELL_SIZE = 28;

	public static DialogueInquiryGridLayout calculate(DialogueLayout.Rect content, int itemCount) {
		int usableWidth = Math.max(CELL_SIZE, content.width() - PAD * 2);
		int columns = Math.max(1, (usableWidth + GAP) / (CELL_SIZE + GAP));
		List<DialogueLayout.Rect> cells = new ArrayList<>(Math.max(0, itemCount));
		for (int index = 0; index < itemCount; index++) {
			int column = index % columns;
			int row = index / columns;
			cells.add(new DialogueLayout.Rect(
					content.x() + PAD + column * (CELL_SIZE + GAP),
					content.y() + PAD + row * (CELL_SIZE + GAP), CELL_SIZE, CELL_SIZE));
		}
		int rows = itemCount == 0 ? 0 : (itemCount + columns - 1) / columns;
		int height = PAD * 2 + rows * CELL_SIZE + Math.max(0, rows - 1) * GAP;
		return new DialogueInquiryGridLayout(List.copyOf(cells), columns, height);
	}
}
