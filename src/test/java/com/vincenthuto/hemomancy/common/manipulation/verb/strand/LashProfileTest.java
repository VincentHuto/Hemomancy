package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import com.vincenthuto.hemomancy.common.manipulation.verb.Sculptings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** A lash carries its tap and charge numbers on its registration, and each of the three leans its own way. */
class LashProfileTest {
	private static LashProfile profile(Object sculpting) {
		return ((StrandSculpting) sculpting).spec().lash();
	}

	@Test
	void aLashNeedsItsProfileAndNoOtherFormCarriesOne() {
		assertThrows(IllegalArgumentException.class,
				() -> new StrandSpec(StrandForm.LASH, 30.0D, 1.0F, StrandSpec.LONG_STATE, 25.0D, null));
		assertNull(((StrandSculpting) Strands.jet(20.0D, 1.0F)).spec().lash());
	}

	@Test
	void searingLashLeansOnTheCharge() {
		LashProfile searing = profile(Sculptings.searingLashStrand());
		assertEquals(1.75F, searing.chargedMultiplier());
		assertEquals(40, searing.chargeTicks());
		assertEquals(20, searing.tapCooldown(), "one slow, heavy scald");
		assertEquals(80, searing.tapStateTicks());
	}

	@Test
	void gloamLacerationLeansOnTheTap() {
		LashProfile gloam = profile(Sculptings.gloamLacerationLash());
		assertEquals(60, gloam.tapStateTicks());
		assertEquals(6, gloam.tapCooldown());
		assertEquals(1.25F, gloam.chargedMultiplier());
	}

	@Test
	void bloodLashIsTheCheapEvenOne() {
		LashProfile blood = profile(Sculptings.bloodLash());
		assertEquals(8.0D, blood.tapBlood());
		assertEquals(20, blood.chargeTicks());
	}

	@Test
	void aChargeScalesTheCrackUpToItsMultiplierAndNoFurther() {
		LashProfile profile = new LashProfile(4.5F, 20.0D, 12, 0, 30.0D, 40, 1.75F);
		assertEquals(1.0D, profile.chargeScale(0.0D), 1.0E-9D);
		assertEquals(1.375D, profile.chargeScale(0.5D), 1.0E-9D);
		assertEquals(1.75D, profile.chargeScale(2.0D), 1.0E-9D);
	}

	@Test
	void theViewIsCappedAtFourDegreesATickByDefault() {
		assertEquals(4.0D, LashTuning.DEFAULT.cameraCap());
		assertEquals(6.0D, LashTuning.DEFAULT.flickTurn());
	}
	/** A lash pays for what it did since its last pulse: resting just before the pulse does not make the stroke free. */
	@Test
	void aLashPaysForTheTicksItWasOut() {
		assertEquals(30.0D, StrandSculpting.lashPrice(30.0D, 60.0D, 0, 0, 0), 1.0E-9D, "the opening pulse is the charge rate");
		assertEquals(30.0D, StrandSculpting.lashPrice(30.0D, 60.0D, 10, 0, 0), 1.0E-9D);
		assertEquals(60.0D, StrandSculpting.lashPrice(30.0D, 60.0D, 0, 10, 0), 1.0E-9D);
		assertEquals(0.0D, StrandSculpting.lashPrice(30.0D, 60.0D, 0, 0, 10), 1.0E-9D, "a fade is free");
		assertEquals(24.0D, StrandSculpting.lashPrice(30.0D, 60.0D, 0, 4, 6), 1.0E-9D, "four live ticks before a rest went unpaid");
	}
}
