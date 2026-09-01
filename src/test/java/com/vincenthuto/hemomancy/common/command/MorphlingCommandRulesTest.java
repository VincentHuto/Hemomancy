package com.vincenthuto.hemomancy.common.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MorphlingCommandRulesTest {
	@Test
	void equippedMorphlingTakesPriorityOverHeldMorphlings() {
		assertEquals(MorphlingCommandRules.Source.EQUIPPED,
				MorphlingCommandRules.chooseSource(true, true, true));
	}

	@Test
	void mainHandIsUsedWhenNoMorphlingIsEquipped() {
		assertEquals(MorphlingCommandRules.Source.MAIN_HAND,
				MorphlingCommandRules.chooseSource(false, true, true));
	}

	@Test
	void offhandIsTheFinalHeldFallback() {
		assertEquals(MorphlingCommandRules.Source.OFF_HAND,
				MorphlingCommandRules.chooseSource(false, false, true));
	}

	@Test
	void missingMorphlingsProduceNoTarget() {
		assertEquals(MorphlingCommandRules.Source.NONE,
				MorphlingCommandRules.chooseSource(false, false, false));
	}
}
