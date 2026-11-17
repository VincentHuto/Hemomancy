package com.vincenthuto.hemomancy.client.screen.dialogue;

public record DialogueTopicCardLayout(DialogueLayout.Rect title, DialogueLayout.Rect summary,
		DialogueLayout.Rect topStatus) {
	public static final int TEXT_STATUS_GAP = 4;

	private static final int TEXT_X = 32;
	private static final int RIGHT_PAD = 8;
	private static final int STATUS_RIGHT_PAD = 6;
	private static final int STATUS_SIZE = 8;
	/** The line step a title and a summary wrap at; a card's base height holds one title line and two summary lines. */
	static final int LINE = 12;
	private static final int BASE_SUMMARY_LINES = 2;

	public static DialogueTopicCardLayout calculate(DialogueLayout.Rect card, boolean hasTopStatus) {
		return calculate(card, hasTopStatus, 1);
	}

	/** The card's parts, with the summary set below {@code titleLines} lines of title. */
	public static DialogueTopicCardLayout calculate(DialogueLayout.Rect card, boolean hasTopStatus, int titleLines) {
		int textX = card.x() + TEXT_X;
		DialogueLayout.Rect status = hasTopStatus
				? new DialogueLayout.Rect(card.right() - STATUS_RIGHT_PAD - STATUS_SIZE, card.y() + 5,
						STATUS_SIZE, STATUS_SIZE)
				: null;
		int titleRight = status != null ? status.x() - TEXT_STATUS_GAP : card.right() - RIGHT_PAD;
		int extraTitle = Math.max(0, titleLines - 1) * LINE;
		DialogueLayout.Rect title = new DialogueLayout.Rect(textX, card.y() + 8,
				Math.max(1, titleRight - textX), 10 + extraTitle);
		DialogueLayout.Rect summary = new DialogueLayout.Rect(textX, card.y() + 21 + extraTitle,
				Math.max(1, card.right() - RIGHT_PAD - textX), Math.max(1, card.height() - 25 - extraTitle));
		return new DialogueTopicCardLayout(title, summary, status);
	}

	/** How wide a card {@code cardWidth} wide wraps its summary. */
	public static int summaryWidth(int cardWidth) {
		return Math.max(1, cardWidth - TEXT_X - RIGHT_PAD);
	}

	/** How wide a card {@code cardWidth} wide wraps its title, which gives way to the status marker. */
	public static int titleWidth(int cardWidth, boolean hasTopStatus) {
		int right = hasTopStatus ? STATUS_RIGHT_PAD + STATUS_SIZE + TEXT_STATUS_GAP : RIGHT_PAD;
		return Math.max(1, cardWidth - TEXT_X - right);
	}

	/** A card's height grown from {@code baseHeight} so every one of its {@code summaryLines} shows. */
	public static int height(int baseHeight, int summaryLines) {
		return height(baseHeight, 1, summaryLines);
	}

	/** A card's height grown from {@code baseHeight} so every title line and summary line shows. */
	public static int height(int baseHeight, int titleLines, int summaryLines) {
		return baseHeight + Math.max(0, titleLines - 1) * LINE
				+ Math.max(0, summaryLines - BASE_SUMMARY_LINES) * LINE;
	}
}
