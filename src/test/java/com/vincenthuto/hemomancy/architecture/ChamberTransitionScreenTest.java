package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * There is no loading screen into or out of the Chamber of Will: NeoForge's dimension transition hook hands the
 * change to our own screen, which draws the crossing's black hole until the chunk arrives and never the dirt
 * screen or its "Downloading terrain" text. The crossing's colours survive the level swap for it to draw.
 */
class ChamberTransitionScreenTest {
	@Test
	void theChamberTransitionIsOursBothWays() {
		String registrar = SourceAssertions.readMainJava("client/registry/DimensionTransitionRegistrar.java");
		assertTrue(registrar.contains("RegisterDimensionTransitionScreenEvent"), "registered on NeoForge's transition event");
		assertTrue(registrar.contains("registerIncomingEffect(ChamberOfWillManager.CHAMBER_OF_WILL"), "into the Chamber");
		assertTrue(registrar.contains("registerOutgoingEffect(ChamberOfWillManager.CHAMBER_OF_WILL"), "and out of it");
	}

	@Test
	void theScreenDrawsTheVoidAndNothingOfTheDirtScreen() {
		String screen = SourceAssertions.readMainJava("client/screen/ChamberTransitionScreen.java");
		assertTrue(screen.contains("extends ReceivingLevelScreen"), "it is the screen the level load closes");
		assertTrue(screen.contains("LidlessPassageView.renderVeil("), "it draws the crossing view");
		assertFalse(screen.contains("super.render("), "never the dirt screen's text");
		assertTrue(screen.contains("public void renderBackground(") && !screen.contains("super.renderBackground("),
				"never the panorama or the blur");
	}

	@Test
	void theCrossingsColoursSurviveTheLevelSwap() {
		String session = SourceAssertions.readMainJava("client/event/ClientSessionEvents.java");
		assertFalse(session.contains("ChamberVeil.clear()"), "the level unload leaves the armed veil for the screen");
		String state = SourceAssertions.readMainJava("client/data/LidlessPassageClientState.java");
		assertTrue(state.contains("ChamberVeil.arm("), "a crossing that leaves the level arms the veil as it ends");
	}
}
