package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	@Test
	void eachIssuerSpeaksItsOwnObservanceLines() {
		Set<String> lines = new HashSet<>();
		for (UnstainedObservances.Issuer issuer : UnstainedObservances.Issuer.values()) {
			String available = UnstainedObservanceDialogueDecorator.promptKey(issuer, false, false);
			String active = UnstainedObservanceDialogueDecorator.promptKey(issuer, true, false);
			String ready = UnstainedObservanceDialogueDecorator.promptKey(issuer, true, true);
			assertEquals("hemomancy.dialogue." + issuer.id() + ".observance.available", available);
			assertEquals("hemomancy.dialogue." + issuer.id() + ".observance.active", active);
			assertEquals("hemomancy.dialogue." + issuer.id() + ".observance.ready", ready);
			for (String key : new String[] {available, active, ready}) {
				assertTrue(lines.add(EnglishText.of(key)), key + " repeats another speaker's line");
			}
		}
		assertEquals(9, lines.size());
	}
}
