package com.vincenthuto.hemomancy.common.rite.sigil;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class IchorianSigilFormRulesTest {
	@Test
	void theHeartbeatIsSharpAndBeatsAboutOnceASecond() {
		int beats = 0;
		boolean rising = false;
		float previous = 0.0F;
		for (int tick = 0; tick < 200; tick++) {
			float beat = IchorianSigilFormRules.heartbeat(tick);
			assertTrue(beat >= 0.0F && beat <= 1.0F);
			if (beat > previous) rising = true;
			if (beat < previous && rising) {
				beats++;
				rising = false;
			}
			previous = beat;
		}
		assertTrue(beats >= 8 && beats <= 12, "ten seconds hold about ten beats, held " + beats);
		assertEquals(0.0F, IchorianSigilFormRules.heartbeat(15.0F), 1.0E-6F, "it rests between beats");
	}

	@Test
	void repeatedPartsNeverFlickerInLockstep() {
		float first = IchorianSigilFormRules.flicker(37.0F, 0, 11L);
		float second = IchorianSigilFormRules.flicker(37.0F, 1, 11L);
		assertNotEquals(first, second, 1.0E-4F);
		for (int tick = 0; tick < 300; tick += 7) {
			float flicker = IchorianSigilFormRules.flicker(tick, tick % 5, 11L);
			assertTrue(flicker >= 0.55F && flicker <= 1.0F);
		}
	}

	@Test
	void bolusesRideTheirVesselEndToEndAndAreSpacedEvenly() {
		for (int tick = 0; tick < 200; tick += 3) {
			float position = IchorianSigilFormRules.bolusPosition(tick, 2, 0, 3, 2.4F);
			assertTrue(position >= 0.0F && position < 1.0F);
		}
		float a = IchorianSigilFormRules.bolusPosition(10.0F, 0, 0, 3, 1.0F);
		float b = IchorianSigilFormRules.bolusPosition(10.0F, 0, 1, 3, 1.0F);
		assertEquals(1.0F / 3.0F, (b - a + 1.0F) % 1.0F, 1.0E-4F);
		assertEquals(0.0F, IchorianSigilFormRules.bolusPresence(0.0F), 1.0E-6F, "it is gone at the vessel's ends");
		assertEquals(1.0F, IchorianSigilFormRules.bolusPresence(0.5F), 1.0E-6F);
	}

	@Test
	void everyBodyStyleHasItsOwnSignature() {
		Set<IchorianSigilFormRules.Flourish> seen = EnumSet.noneOf(IchorianSigilFormRules.Flourish.class);
		for (IchorianSigilAnatomy.Style style : IchorianSigilAnatomy.Style.values()) {
			seen.add(IchorianSigilFormRules.flourish(style));
		}
		assertEquals(IchorianSigilAnatomy.Style.values().length, seen.size());
		assertEquals(3, IchorianSigilFormRules.Flourish.RACE.boluses(), "the Shunt races three boluses");
	}

	@Test
	void onlyTheShutterClosesAndOnlyTheShieldPullsTautOnTheBeat() {
		float closest = 0.0F;
		for (int tick = 0; tick < 400; tick += 5) {
			closest = Math.max(closest, IchorianSigilFormRules.shutter(IchorianSigilFormRules.Flourish.SHUTTER, tick));
			assertEquals(0.0F, IchorianSigilFormRules.shutter(IchorianSigilFormRules.Flourish.CONTRACT, tick));
		}
		assertTrue(closest > 0.4F && closest <= 0.55F, "the Seal's lips draw most of the way closed");

		float beat = 5.0F; // sin(1.5) is near its peak
		float shield = IchorianSigilFormRules.membraneBillow(IchorianSigilFormRules.Flourish.CONTRACT, beat, 0);
		float other = IchorianSigilFormRules.membraneBillow(IchorianSigilFormRules.Flourish.DRIP, beat, 0);
		assertTrue(shield < other, "the Bastion's membranes pull in on the beat");
	}

	@Test
	void aDropGathersBeforeItFalls() {
		boolean gathered = false, fell = false;
		for (int tick = 0; tick < 70; tick++) {
			float fall = IchorianSigilFormRules.drip(tick, 0);
			if (fall < 0.0F) gathered = true;
			else {
				fell = true;
				assertTrue(fall <= 1.0F);
			}
		}
		assertTrue(gathered && fell);
	}
}
