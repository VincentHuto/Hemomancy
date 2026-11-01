package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeRules;

import java.util.ArrayList;
import java.util.List;

/**
 * Where the Trade box puts things: the rapport bar, then one 54-px card per want card. The expanded card grows a
 * selection strip under its body (an optional note row for a refusal, then wrapping slot wells, with the Offer button
 * on the right). Pure maths, so {@code DialogueTradeLayoutTest} can pin it.
 */
public record DialogueTradeLayout(DialogueLayout.Rect rapport, List<Card> cards, int contentHeight) {
	public static final int PAD = 6;
	public static final int GAP = 6;
	public static final int CARD_HEIGHT = 54;
	public static final int RAPPORT_HEIGHT = 14;
	/** A slot well's step: the 16-px item, its one-pixel frame, and a gap. */
	public static final int WELL_STEP = 20;
	public static final int NOTE_HEIGHT = 12;
	public static final int OFFER_WIDTH = 56;
	public static final int OFFER_HEIGHT = 18;
	private static final int STRIP_INSET = 8;
	private static final int STRIP_BOTTOM = 6;

	public DialogueTradeLayout {
		cards = List.copyOf(cards);
	}

	/**
	 * One card: its whole frame, the 54-px body, and (only when expanded) the note row, the wells and the Offer
	 * button. {@code wells} holds the top-left of each 16-px item.
	 */
	public record Card(DialogueLayout.Rect frame, DialogueLayout.Rect body, DialogueLayout.Rect note,
			List<DialogueLayout.Rect> wells, DialogueLayout.Rect offer) {
		public Card {
			wells = List.copyOf(wells);
		}

		public boolean expanded() {
			return offer != null;
		}
	}

	/**
	 * Lays out {@code cardCount} cards in {@code content} (already scrolled). {@code expanded} is the open card's
	 * index or -1; it shows {@code wellCount} wells and a note row when {@code note} is set.
	 */
	public static DialogueTradeLayout calculate(DialogueLayout.Rect content, int cardCount, int expanded,
			int wellCount, boolean note) {
		int x = content.x() + PAD;
		int width = Math.max(1, content.width() - PAD * 2);
		DialogueLayout.Rect rapport = new DialogueLayout.Rect(x, content.y() + PAD, width, RAPPORT_HEIGHT);
		int y = rapport.bottom() + GAP;
		List<Card> cards = new ArrayList<>(Math.max(0, cardCount));
		for (int i = 0; i < cardCount; i++) {
			DialogueLayout.Rect body = new DialogueLayout.Rect(x, y, width, CARD_HEIGHT);
			Card card = i == expanded ? expand(body, Math.max(0, wellCount), note)
					: new Card(body, body, null, List.of(), null);
			cards.add(card);
			y = card.frame().bottom() + GAP;
		}
		int height = y - content.y() - GAP + PAD;
		return new DialogueTradeLayout(rapport, cards, Math.max(height, rapport.bottom() - content.y() + PAD));
	}

	private static Card expand(DialogueLayout.Rect body, int wellCount, boolean note) {
		int stripX = body.x() + STRIP_INSET;
		int stripY = body.bottom();
		int stripWidth = Math.max(WELL_STEP, body.width() - STRIP_INSET * 2 - OFFER_WIDTH - 4);
		DialogueLayout.Rect noteRow = note ? new DialogueLayout.Rect(stripX, stripY, body.width() - STRIP_INSET * 2,
				NOTE_HEIGHT) : null;
		int wellsY = stripY + (note ? NOTE_HEIGHT : 0);
		int columns = columns(stripWidth);
		List<DialogueLayout.Rect> wells = new ArrayList<>(wellCount);
		for (int i = 0; i < wellCount; i++) {
			wells.add(new DialogueLayout.Rect(stripX + 1 + (i % columns) * WELL_STEP,
					wellsY + 1 + (i / columns) * WELL_STEP, 16, 16));
		}
		int rows = wellCount == 0 ? 1 : (wellCount + columns - 1) / columns;
		int wellsHeight = Math.max(rows * WELL_STEP, OFFER_HEIGHT + 2);
		DialogueLayout.Rect offer = new DialogueLayout.Rect(body.right() - STRIP_INSET - OFFER_WIDTH,
				wellsY + 1, OFFER_WIDTH, OFFER_HEIGHT);
		int frameHeight = wellsY + wellsHeight + STRIP_BOTTOM - body.y();
		return new Card(new DialogueLayout.Rect(body.x(), body.y(), body.width(), frameHeight), body, noteRow, wells,
				offer);
	}

	/** How many wells fit across a strip {@code width} wide; always at least one. */
	public static int columns(int width) {
		return Math.max(1, width / WELL_STEP);
	}

	/** How far the box scrolls in a viewport {@code viewportHeight} tall. */
	public int maxScroll(int viewportHeight) {
		return Math.max(0, contentHeight - viewportHeight);
	}

	/** How full the rapport bar is: progress from this tier's floor to the next; full at Confidant. */
	public static float rapportFill(int rapport) {
		int tier = NpcTradeRules.tier(rapport);
		if (tier >= NpcTradeRules.CONFIDANT) return 1F;
		int floor = NpcTradeRules.tierFloor(tier);
		int next = NpcTradeRules.tierFloor(tier + 1);
		return Math.clamp((rapport - floor) / (float) (next - floor), 0F, 1F);
	}
}
