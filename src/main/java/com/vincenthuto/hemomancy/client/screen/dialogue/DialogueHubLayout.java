package com.vincenthuto.hemomancy.client.screen.dialogue;

import java.util.ArrayList;
import java.util.List;

public record DialogueHubLayout(List<DialogueLayout.Rect> cards, DialogueLayout.Rect leave, DialogueLayout.Rect important) {
	private static final int PAD = 6;
	private static final int GAP = 6;
	private static final int CARD_HEIGHT = 76;
	private static final int LEAVE_WIDTH = 84;
	private static final int IMPORTANT_WIDTH = 150;
	private static final int IMPORTANT_HEIGHT = 24;
	/** Clear space around the Important tab, so its glow is not cut off. */
	static final int IMPORTANT_MARGIN = DialogueActionGlow.HALO + 2;

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
		// The Important tab: just above Leave, at the foot of the content; on a short window, under the last card, where the hub scrolls to it.
		int importantWidth = Math.min(IMPORTANT_WIDTH, content.width() - IMPORTANT_MARGIN * 2);
		DialogueLayout.Rect important = new DialogueLayout.Rect(content.x() + (content.width() - importantWidth) / 2,
				Math.max(cards.getLast().bottom() + IMPORTANT_MARGIN, content.bottom() - IMPORTANT_HEIGHT - IMPORTANT_MARGIN),
				importantWidth, IMPORTANT_HEIGHT);
		return new DialogueHubLayout(cards, leave, important);
	}
}
