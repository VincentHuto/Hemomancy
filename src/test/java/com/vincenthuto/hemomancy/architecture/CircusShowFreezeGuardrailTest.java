package com.vincenthuto.hemomancy.architecture;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.readMainJava;

import org.junit.jupiter.api.Test;

/**
 * A-4 for the Circus phantom show: the world pass hands {@code getGameTimeDeltaPartialTick(true)}, which keeps cycling
 * under {@code /tick freeze}, so the show and its fog read {@code AnimationTime.partialTick()} instead.
 */
class CircusShowFreezeGuardrailTest {
	@Test
	void showTimeHoldsUnderTickFreeze() {
		String overlay = readMainJava("client/screen/overlay/CircusPerceptionOverlay.java");
		String fog = readMainJava("client/event/CircusShowFog.java");
		assertContains("CircusPerceptionOverlay", overlay, "AnimationTime.partialTick()");
		assertNotContains("CircusPerceptionOverlay", overlay, "PhantomShow.render(poseStack, partialTick");
		assertNotContains("CircusShowFog", fog, "event.getPartialTick()");
	}
}
