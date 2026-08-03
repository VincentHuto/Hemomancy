package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicBlueprintCompletionNoticeTest {
	@Test
	void completedNoticeExpiresAfterTwoAndAHalfSeconds() {
		MnemonicBlueprintCompletionNotice notice = new MnemonicBlueprintCompletionNotice();
		notice.updateRemaining(0);

		for (int tick = 0; tick < 49; tick++) notice.tick();
		assertTrue(notice.shouldRender(0));

		notice.tick();
		assertFalse(notice.shouldRender(0));
	}

	@Test
	void missingBlocksResetTheNoticeForTheNextCompletion() {
		MnemonicBlueprintCompletionNotice notice = new MnemonicBlueprintCompletionNotice();
		notice.updateRemaining(0);
		for (int tick = 0; tick < 50; tick++) notice.tick();
		assertFalse(notice.shouldRender(0));

		notice.updateRemaining(1);
		assertTrue(notice.shouldRender(1));
		notice.updateRemaining(0);
		assertTrue(notice.shouldRender(0));
	}
}
