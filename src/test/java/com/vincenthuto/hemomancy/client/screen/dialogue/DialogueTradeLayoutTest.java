package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueTradeLayoutTest {
	private static final DialogueLayout.Rect CONTENT = DialogueLayout.calculate(854, 480).content();

	@Test
	void cardsStackUnderTheRapportBarAtTheQuestHeight() {
		DialogueTradeLayout layout = DialogueTradeLayout.calculate(CONTENT, 3, -1, 0, false);

		assertEquals(CONTENT.y() + DialogueTradeLayout.PAD, layout.rapport().y());
		assertEquals(3, layout.cards().size());
		DialogueTradeLayout.Card first = layout.cards().getFirst();
		assertEquals(layout.rapport().bottom() + DialogueTradeLayout.GAP, first.frame().y());
		for (DialogueTradeLayout.Card card : layout.cards()) {
			assertEquals(54, card.frame().height());
			assertFalse(card.expanded());
			assertTrue(card.frame().x() >= CONTENT.x() && card.frame().right() <= CONTENT.right());
		}
		assertEquals(first.frame().bottom() + DialogueTradeLayout.GAP, layout.cards().get(1).frame().y());
		assertEquals(layout.cards().getLast().frame().bottom() + DialogueTradeLayout.PAD - CONTENT.y(),
				layout.contentHeight());
	}

	@Test
	void theExpandedCardGrowsAStripAndPushesTheRestDown() {
		DialogueTradeLayout closed = DialogueTradeLayout.calculate(CONTENT, 3, -1, 0, false);
		DialogueTradeLayout open = DialogueTradeLayout.calculate(CONTENT, 3, 1, 4, true);
		DialogueTradeLayout.Card card = open.cards().get(1);

		assertTrue(card.expanded());
		assertEquals(54, card.body().height());
		assertEquals(card.body().bottom(), card.note().y());
		assertEquals(4, card.wells().size());
		assertTrue(card.wells().getFirst().y() >= card.note().bottom());
		DialogueLayout.Rect lastWell = card.wells().getLast();
		assertTrue(lastWell.right() < card.offer().x(), "wells stay left of Offer");
		assertTrue(card.offer().right() <= card.frame().right() && card.offer().bottom() <= card.frame().bottom());
		int grown = card.frame().height() - 54;
		assertEquals(closed.cards().get(2).frame().y() + grown, open.cards().get(2).frame().y());
		assertEquals(closed.contentHeight() + grown, open.contentHeight());
	}

	@Test
	void aBloodOrCustomCardHasOnlyTheOfferRow() {
		DialogueTradeLayout.Card card = DialogueTradeLayout.calculate(CONTENT, 1, 0, 0, false).cards().getFirst();

		assertTrue(card.wells().isEmpty());
		assertNull(card.note());
		assertTrue(card.frame().height() > 54 && card.frame().height() < 54 + 2 * DialogueTradeLayout.WELL_STEP);
	}

	@Test
	void theStripWrapsAtNarrowWidths() {
		DialogueLayout.Rect narrow = DialogueLayout.calculate(200, 300).content();
		DialogueTradeLayout wide = DialogueTradeLayout.calculate(CONTENT, 1, 0, 12, false);
		DialogueTradeLayout thin = DialogueTradeLayout.calculate(narrow, 1, 0, 12, false);
		DialogueTradeLayout.Card wideCard = wide.cards().getFirst();
		DialogueTradeLayout.Card thinCard = thin.cards().getFirst();

		long wideRows = wideCard.wells().stream().mapToInt(DialogueLayout.Rect::y).distinct().count();
		long thinRows = thinCard.wells().stream().mapToInt(DialogueLayout.Rect::y).distinct().count();
		assertTrue(thinRows > wideRows, "a narrow box needs more rows");
		for (DialogueLayout.Rect well : thinCard.wells()) {
			assertTrue(well.x() >= thinCard.frame().x() && well.right() < thinCard.offer().x());
			assertTrue(well.bottom() <= thinCard.frame().bottom());
		}
		assertTrue(DialogueTradeLayout.columns(1) == 1, "at least one column however narrow");
	}

	@Test
	void scrollStopsAtTheLastCard() {
		DialogueTradeLayout few = DialogueTradeLayout.calculate(CONTENT, 1, -1, 0, false);
		DialogueTradeLayout many = DialogueTradeLayout.calculate(CONTENT, 20, -1, 0, false);

		assertEquals(0, few.maxScroll(CONTENT.height()));
		assertEquals(many.contentHeight() - CONTENT.height(), many.maxScroll(CONTENT.height()));
		assertTrue(many.maxScroll(CONTENT.height()) > 0);
	}

	@Test
	void rapportFillRunsFromEachTierFloorToTheNext() {
		assertEquals(0F, DialogueTradeLayout.rapportFill(0));
		assertEquals(0.5F, DialogueTradeLayout.rapportFill(5));
		assertEquals(0F, DialogueTradeLayout.rapportFill(10));
		assertEquals(0.5F, DialogueTradeLayout.rapportFill(20));
		assertEquals(1F, DialogueTradeLayout.rapportFill(60));
		assertEquals(1F, DialogueTradeLayout.rapportFill(100));
	}
}
