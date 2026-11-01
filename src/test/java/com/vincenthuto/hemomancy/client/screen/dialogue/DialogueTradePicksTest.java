package com.vincenthuto.hemomancy.client.screen.dialogue;

import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueTradePicks.Carried;
import com.vincenthuto.hemomancy.client.screen.dialogue.DialogueTradePicks.Stack;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeAppraisals.Grade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DialogueTradePicksTest {
	private static Stack one(int slot, int count, Grade grade) {
		return new Stack(slot, count, new Grade[] { grade });
	}

	@Test
	void prePickTakesTheBestGradesAndSkipsPoorStacks() {
		List<Stack> stacks = List.of(one(0, 3, Grade.POOR), one(4, 1, Grade.FAIR), one(7, 2, Grade.FINE),
				one(9, 2, Grade.FAIR));

		List<Integer> picked = DialogueTradePicks.prePick(stacks, new int[] { 3 });

		assertEquals(List.of(7, 9), picked);
		assertEquals(Optional.of(Grade.FAIR), DialogueTradePicks.predict(stacks, new int[] { 3 }, picked));
	}

	@Test
	void predictMirrorsTheServerOrderAndLowestGrade() {
		List<Stack> stacks = List.of(one(1, 2, Grade.FINE), one(2, 2, Grade.POOR), one(3, 1, Grade.FINE));

		assertEquals(Optional.of(Grade.FINE), DialogueTradePicks.predict(stacks, new int[] { 3 }, List.of(1, 3)));
		assertEquals(Optional.of(Grade.POOR), DialogueTradePicks.predict(stacks, new int[] { 3 }, List.of(2, 1)));
		assertEquals(Optional.empty(), DialogueTradePicks.predict(stacks, new int[] { 3 }, List.of(1)));
		// A slot the request lists but the trade never needs is not graded.
		assertEquals(Optional.of(Grade.FINE), DialogueTradePicks.predict(stacks, new int[] { 2 }, List.of(1, 2)));
	}

	@Test
	void aBundleSlotPaysOnlyOnePart() {
		Stack both = new Stack(5, 4, new Grade[] { Grade.FAIR, Grade.FAIR });
		Stack second = new Stack(6, 1, new Grade[] { null, Grade.FINE });
		int[] counts = { 2, 1 };

		assertEquals(Optional.empty(), DialogueTradePicks.predict(List.of(both), counts, List.of(5)));
		List<Integer> picked = DialogueTradePicks.prePick(List.of(both, second), counts);
		assertEquals(List.of(5, 6), picked);
		assertEquals(Carried.ENOUGH, DialogueTradePicks.carried(List.of(both, second), counts));
	}

	@Test
	void carriedReadsEnoughSomeOrNone() {
		int[] counts = { 4 };
		assertEquals(Carried.ENOUGH, DialogueTradePicks.carried(List.of(one(0, 4, Grade.FAIR)), counts));
		assertEquals(Carried.SOME, DialogueTradePicks.carried(List.of(one(0, 2, Grade.FAIR)), counts));
		assertEquals(Carried.SOME, DialogueTradePicks.carried(List.of(one(0, 9, Grade.POOR)), counts));
		assertEquals(Carried.NONE, DialogueTradePicks.carried(List.of(), counts));
		assertEquals(Carried.NONE, DialogueTradePicks.carried(List.of(), new int[0]));
	}

	@Test
	void toggleAndRetainFollowThePlayerAndTheInventory() {
		assertEquals(List.of(1, 3), DialogueTradePicks.toggle(List.of(1), 3));
		assertEquals(List.of(3), DialogueTradePicks.toggle(List.of(1, 3), 1));
		assertEquals(List.of(3), DialogueTradePicks.retain(List.of(1, 3), List.of(one(3, 1, Grade.FAIR))));
	}
}
