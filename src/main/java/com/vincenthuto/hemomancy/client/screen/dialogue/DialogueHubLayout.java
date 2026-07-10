package com.vincenthuto.hemomancy.client.screen.dialogue;

import java.util.ArrayList;
import java.util.List;

public record DialogueHubLayout(List<DialogueLayout.Rect> cards, DialogueLayout.Rect leave) {
	private static final int PAD = 6;
	private static final int GAP = 6;
	private static final int CARD_HEIGHT = 76;
	private static final int LEAVE_WIDTH = 84;

	public DialogueHubLayout {
		cards = List.copyOf(cards);
	}

	public static DialogueHubLayout calculate(DialogueLayout shell) {
		var content = shell.content();
		int columns = shell.categoryColumns();
		int cardWidth = (content.width() - PAD * 2 - GAP * (columns - 1)) / columns;
		int cardHeight = Math.min(CARD_HEIGHT,
				Math.max(42, (content.height() - PAD * 2 - GAP) / 2));
		List<DialogueLayout.Rect> cards = new ArrayList<>(4);
		for (int i = 0; i < 4; i++) {
			int row = i / columns;
			int column = i % columns;
			cards.add(new DialogueLayout.Rect(
					content.x() + PAD + column * (cardWidth + GAP),
					content.y() + PAD + row * (cardHeight + GAP),
					cardWidth, cardHeight));
		}
		var footer = shell.footer();
		int leaveWidth = Math.min(LEAVE_WIDTH, footer.width());
		DialogueLayout.Rect leave = new DialogueLayout.Rect(
				footer.x() + (footer.width() - leaveWidth) / 2, footer.y(), leaveWidth, footer.height());
		return new DialogueHubLayout(cards, leave);
	}
}
