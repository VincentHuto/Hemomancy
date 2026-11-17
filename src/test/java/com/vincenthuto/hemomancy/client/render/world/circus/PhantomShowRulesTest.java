package com.vincenthuto.hemomancy.client.render.world.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusProgressRules.Stage;
import org.junit.jupiter.api.Test;

class PhantomShowRulesTest {
	@Test
	void stagesGrowTowardTheFullShow() {
		var disturbed = PhantomShowRules.look(Stage.DISTURBED, false);
		var acclimating = PhantomShowRules.look(Stage.ACCLIMATING, false);
		var attuned = PhantomShowRules.look(Stage.ATTUNED, false);
		assertTrue(disturbed.audienceShare() < acclimating.audienceShare());
		assertTrue(acclimating.audienceShare() < attuned.audienceShare());
		assertTrue(disturbed.spotlights() < acclimating.spotlights() && acclimating.spotlights() < attuned.spotlights());
		assertTrue(disturbed.house() < acclimating.house() && acclimating.house() < attuned.house());
		assertTrue(disturbed.peripheralOnly());
		var unaware = PhantomShowRules.look(Stage.UNAWARE, false);
		assertEquals(0.0F, unaware.house());
		assertEquals(0.0F, unaware.phantomAlpha());
		assertEquals(0, unaware.performers());
		assertEquals(0, unaware.glimpses());
	}

	@Test
	void ruinIsAHollowEcho() {
		var ruin = PhantomShowRules.look(Stage.ATTUNED, true);
		var disturbed = PhantomShowRules.look(Stage.DISTURBED, false);
		assertEquals(0, ruin.performers());
		assertEquals(disturbed.house(), ruin.house());
		assertEquals(disturbed.bulbLit(), ruin.bulbLit());
		assertEquals(disturbed.glimpses(), ruin.glimpses());
		assertEquals(0.0F, PhantomShowRules.look(Stage.UNAWARE, true).house());
	}

	@Test
	void lookBlendsBetweenStages() {
		var a = PhantomShowRules.look(Stage.DISTURBED, false);
		var b = PhantomShowRules.look(Stage.ATTUNED, false);
		var mid = PhantomShowRules.blend(a, b, 0.5F);
		assertEquals((a.house() + b.house()) / 2.0F, mid.house(), 1.0E-5F);
		assertEquals(b.spotlights(), mid.spotlights());
		assertEquals(a, PhantomShowRules.blend(a, b, 0.0F));
		assertEquals(b, PhantomShowRules.blend(a, b, 1.0F));
	}

	@Test
	void peripheralFadeHidesTheCrosshair() {
		assertEquals(0.0F, PhantomShowRules.peripheralFade(10.0F));
		assertEquals(1.0F, PhantomShowRules.peripheralFade(40.0F));
		float mid = PhantomShowRules.peripheralFade(27.5F);
		assertTrue(mid > 0.0F && mid < 1.0F);
	}

	@Test
	void nearFadeKeepsPhantomsUntouchable() {
		assertEquals(0.0F, PhantomShowRules.nearFade(1.0D));
		assertEquals(1.0F, PhantomShowRules.nearFade(4.0D));
	}

	@Test
	void bulbsAreStableAndShared() {
		var look = PhantomShowRules.look(Stage.DISTURBED, false);
		assertEquals(PhantomShowRules.bulb(look, 1234L), PhantomShowRules.bulb(look, 1234L));
		int dead = 0;
		for (long seed = 0; seed < 4000; seed++)
			if (PhantomShowRules.bulb(look, seed) == PhantomShowRules.Bulb.DEAD) dead++;
		assertEquals(1.0F - look.bulbLit(), dead / 4000.0F, 0.05F);
		var attuned = PhantomShowRules.look(Stage.ATTUNED, false);
		for (long seed = 0; seed < 200; seed++)
			assertEquals(PhantomShowRules.Bulb.LIT, PhantomShowRules.bulb(attuned, seed));
	}

	@Test
	void fogNeverUnder32() {
		assertTrue(PhantomShowRules.fogFar(48.0F, 1.0F, 1.0D) >= 32.0F);
		assertTrue(PhantomShowRules.fogFar(256.0F, 1.0F, 1.0D) < 256.0F);
	}

	@Test
	void darknessScaleZeroLeavesFog() {
		assertEquals(48.0F, PhantomShowRules.fogFar(48.0F, 1.0F, 0.0D));
		assertEquals(48.0F, PhantomShowRules.fogFar(48.0F, 0.0F, 1.0D));
	}

	@Test
	void fogInactiveOutside() {
		assertEquals(0.0F, PhantomShowRules.look(Stage.UNAWARE, false).house());
	}
}
