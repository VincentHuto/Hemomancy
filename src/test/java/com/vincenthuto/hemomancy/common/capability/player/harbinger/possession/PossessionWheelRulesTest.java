package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PossessionWheelRulesTest {
	@Test
	void everyFormHoldsTheVisitControlsOnTheInnerRing() {
		for (SurrogateForm form : SurrogateForm.values()) {
			assertEquals(List.of(RETURN, STILL), PossessionWheelRules.visit(form), form.name());
		}
	}

	@Test
	void eachFormLendsItsOwnGifts() {
		assertEquals(List.of(ECHOLOCATE, HOVER), PossessionWheelRules.body(SurrogateForm.BAT));
		assertEquals(List.of(STEP), PossessionWheelRules.body(SurrogateForm.ENDERMAN));
		assertEquals(List.of(SPIT, HOVER), PossessionWheelRules.body(SurrogateForm.BLAZE));
		assertEquals(List.of(SLAM), PossessionWheelRules.body(SurrogateForm.GOLEM));
		assertEquals(List.of(LEAP), PossessionWheelRules.body(SurrogateForm.HORSE));
		assertEquals(List.of(LUNGE), PossessionWheelRules.body(SurrogateForm.WOLF));
		assertEquals(List.of(CLIMB), PossessionWheelRules.body(SurrogateForm.SPIDER));
		assertEquals(List.of(), PossessionWheelRules.body(SurrogateForm.DONKEY));
		assertEquals(List.of(), PossessionWheelRules.body(SurrogateForm.ARMOR_STAND));
	}

	@Test
	void hoverBelongsOnlyToTheWingedForms() {
		for (SurrogateForm form : SurrogateForm.values()) {
			assertEquals(form.flies(), PossessionWheelRules.offers(form, HOVER), form.name());
		}
	}

	@Test
	void stillClimbAndHoverToggleWhileTheRestTrigger() {
		assertTrue(PossessionWheelRules.isToggle(STILL));
		assertTrue(PossessionWheelRules.isToggle(CLIMB));
		assertTrue(PossessionWheelRules.isToggle(HOVER));
		for (PossessionAction action : List.of(RETURN, ECHOLOCATE, STEP, SPIT, SLAM, LEAP, LUNGE)) {
			assertFalse(PossessionWheelRules.isToggle(action), action.name());
		}
	}

	@Test
	void onlyTriggeredGiftsCoolDownAtTheFormsPace() {
		assertEquals(SurrogateForm.BAT.abilityCooldown(), PossessionWheelRules.cooldownTicks(SurrogateForm.BAT, ECHOLOCATE));
		assertEquals(SurrogateForm.BLAZE.abilityCooldown(), PossessionWheelRules.cooldownTicks(SurrogateForm.BLAZE, SPIT));
		assertEquals(0, PossessionWheelRules.cooldownTicks(SurrogateForm.BLAZE, HOVER));
		assertEquals(0, PossessionWheelRules.cooldownTicks(SurrogateForm.WOLF, RETURN));
		assertEquals(0, PossessionWheelRules.cooldownTicks(SurrogateForm.WOLF, SPIT), "not the wolf's gift");
		for (SurrogateForm form : SurrogateForm.values()) {
			for (PossessionAction action : PossessionWheelRules.body(form)) {
				if (!PossessionWheelRules.isToggle(action)) {
					assertTrue(PossessionWheelRules.cooldownTicks(form, action) > 0, form + " " + action);
				}
			}
		}
	}

	@Test
	void theServerRefusesWhatTheWheelWouldNotOffer() {
		assertEquals(PossessionWheelRules.Verdict.NOT_POSSESSING, PossessionWheelRules.check(null, RETURN, 100L, 0L));
		assertEquals(PossessionWheelRules.Verdict.NOT_OFFERED,
				PossessionWheelRules.check(SurrogateForm.WOLF, STEP, 100L, 0L));
		assertEquals(PossessionWheelRules.Verdict.COOLING,
				PossessionWheelRules.check(SurrogateForm.WOLF, LUNGE, 100L, 101L));
		assertEquals(PossessionWheelRules.Verdict.USE, PossessionWheelRules.check(SurrogateForm.WOLF, LUNGE, 101L, 101L));
		assertEquals(PossessionWheelRules.Verdict.USE, PossessionWheelRules.check(SurrogateForm.DONKEY, RETURN, 0L, 0L));
	}

	@Test
	void aPossessedWillCastsNothingButMayAlwaysLetAChannelGo() {
		assertTrue(PossessionWheelRules.refusesManipulation(true, false));
		assertFalse(PossessionWheelRules.refusesManipulation(true, true));
		assertFalse(PossessionWheelRules.refusesManipulation(false, false));
	}

	@Test
	void theReturnHoldBreaksWhenTheBodyWalksAway() {
		assertFalse(PossessionWheelRules.returnBroken(0.3));
		assertTrue(PossessionWheelRules.returnBroken(PossessionWheelRules.RETURN_DRIFT + 0.01));
	}
}
