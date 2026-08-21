package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UnstainedObservanceDialogueDecoratorTest {
	@Test
	void individualObservanceOnlyCallsForAttentionWhenNewOrReady() {
		assertEquals(DialogueAttention.NOTICE,
				UnstainedObservanceDialogueDecorator.observanceAttention(false, false));
		assertEquals(DialogueAttention.NONE,
				UnstainedObservanceDialogueDecorator.observanceAttention(true, false));
		assertEquals(DialogueAttention.URGENT,
				UnstainedObservanceDialogueDecorator.observanceAttention(true, true));
	}
}
