package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;
import net.minecraft.world.entity.AnimationState;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

final class VesperPhaseTransitionRulesTest {
	@Test
	void transformationAnimationUsesAContinuousPartialTickClientClock() {
		AnimationState state = new AnimationState();
		VesperPhaseTransitionRules.syncAnimationState(state, 200, 40);
		state.updateTime(200.5F, 1.0F);
		assertEquals(2_025L, state.getAccumulatedTime());

		VesperPhaseTransitionRules.syncAnimationState(state, 201, 41);
		state.updateTime(201.25F, 1.0F);
		assertEquals(2_062L, state.getAccumulatedTime(),
				"new synchronized ticks must not restart or quantize the client animation clock");

		VesperPhaseTransitionRules.syncAnimationState(state, 202, 0);
		assertFalse(state.isStarted());
	}

	@Test
	void dismountFinishesBeforeMountAbsorptionAndPhaseTwoWaitsForBoth() throws Exception {
		Class<?> rules;
		try {
			rules = Class.forName("com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules");
		} catch (ClassNotFoundException missing) {
			fail("Vesper phase transition rules are missing");
			return;
		}

		Method dismount = rules.getMethod("dismountProgress", float.class);
		Method absorption = rules.getMethod("absorptionProgress", float.class);
		Method complete = rules.getMethod("isComplete", int.class);

		assertEquals(0.0F, (float) dismount.invoke(null, 0.0F), 0.001F);
		assertEquals(0.5F, (float) dismount.invoke(null, 18.0F), 0.001F);
		assertEquals(1.0F, (float) dismount.invoke(null, 36.0F), 0.001F);
		assertEquals(0.0F, (float) absorption.invoke(null, 36.0F), 0.001F);
		assertEquals(0.5F, (float) absorption.invoke(null, 78.0F), 0.001F);
		assertEquals(1.0F, (float) absorption.invoke(null, 120.0F), 0.001F);
		assertFalse((boolean) complete.invoke(null, 119));
		assertTrue((boolean) complete.invoke(null, 120));
	}

	@Test
	void mountCollapsesDuringTheLeapAndRemainsCollapsedForAbsorption() {
		assertEquals(0.0F, VesperPhaseTransitionRules.collapseProgress(0.0F), 0.001F);
		assertEquals(0.5F, VesperPhaseTransitionRules.collapseProgress(18.0F), 0.001F);
		assertEquals(1.0F, VesperPhaseTransitionRules.collapseProgress(36.0F), 0.001F);
		assertEquals(1.0F, VesperPhaseTransitionRules.collapseProgress(90.0F), 0.001F);
	}

	@Test
	void awakeningRevealsEightSigilsOneAtATimeBeforeCombat() {
		assertEquals(0, VesperPhaseTransitionRules.awakeningSigilCount(11.0F));
		assertEquals(1, VesperPhaseTransitionRules.awakeningSigilCount(12.0F));
		assertEquals(2, VesperPhaseTransitionRules.awakeningSigilCount(18.0F));
		assertEquals(8, VesperPhaseTransitionRules.awakeningSigilCount(54.0F));
		assertFalse(VesperPhaseTransitionRules.isAwakeningComplete(71));
		assertTrue(VesperPhaseTransitionRules.isAwakeningComplete(72));
	}

	@Test
	void awakeningGrowsVesperByTwentyFivePercent() {
		assertEquals(1.0F, VesperPhaseTransitionRules.awakeningScale(12.0F), 0.001F);
		float midpoint = VesperPhaseTransitionRules.awakeningScale(36.0F);
		assertTrue(midpoint > 1.0F && midpoint < 1.25F);
		assertEquals(1.25F, VesperPhaseTransitionRules.awakeningScale(60.0F), 0.001F);
		assertEquals(1.25F, VesperPhaseTransitionRules.awakeningScale(72.0F), 0.001F);
	}
}
