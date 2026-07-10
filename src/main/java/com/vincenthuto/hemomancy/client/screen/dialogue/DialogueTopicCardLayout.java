package com.vincenthuto.hemomancy.client.screen.dialogue;

public record DialogueTopicCardLayout(DialogueLayout.Rect title, DialogueLayout.Rect summary,
		DialogueLayout.Rect topStatus) {
	public static final int TEXT_STATUS_GAP = 4;

	private static final int TEXT_X = 32;
	private static final int RIGHT_PAD = 8;
	private static final int STATUS_RIGHT_PAD = 6;
	private static final int STATUS_SIZE = 8;

	public static DialogueTopicCardLayout calculate(DialogueLayout.Rect card, boolean hasTopStatus) {
		int textX = card.x() + TEXT_X;
		DialogueLayout.Rect status = hasTopStatus
				? new DialogueLayout.Rect(card.right() - STATUS_RIGHT_PAD - STATUS_SIZE, card.y() + 5,
						STATUS_SIZE, STATUS_SIZE)
				: null;
		int titleRight = status != null ? status.x() - TEXT_STATUS_GAP : card.right() - RIGHT_PAD;
		DialogueLayout.Rect title = new DialogueLayout.Rect(textX, card.y() + 8,
				Math.max(1, titleRight - textX), 10);
		DialogueLayout.Rect summary = new DialogueLayout.Rect(textX, card.y() + 21,
				Math.max(1, card.right() - RIGHT_PAD - textX), Math.max(1, card.height() - 25));
		return new DialogueTopicCardLayout(title, summary, status);
	}
}
