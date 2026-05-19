package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

public final class SporiticThuribleRulesTest {
	private SporiticThuribleRulesTest() {
	}

	public static void main(String[] args) {
		sporeIdsMapToTendencies();
		matchingTendencyReceivesResonanceDiscounts();
		nonMatchingTendencyDoesNotReceiveResonanceDiscounts();
		swingIntensityScalesDrainAndRadius();
		burnFractionIsClamped();
		burnTimeUsesEndTickWithoutPerTickStateMutation();
		sporeParticleColorsUseByteRgbValues();
		burningSporeDisplayStackUsesCatalystItemId();
	}

	private static void sporeIdsMapToTendencies() {
		assertEquals("vivacious tendency", EnumBloodTendency.ANIMUS,
				SporiticThuribleSpore.byItemPath("vivacious_spores").orElseThrow().tendency());
		assertEquals("fervent tendency", EnumBloodTendency.FLAMMEUS,
				SporiticThuribleSpore.byItemPath("fervent_spores").orElseThrow().tendency());
		assertEquals("neurotic tendency", EnumBloodTendency.DUCTILIS,
				SporiticThuribleSpore.byItemPath("neurotic_spores").orElseThrow().tendency());
		assertEquals("incandescent tendency", EnumBloodTendency.LUX,
				SporiticThuribleSpore.byItemPath("incandescent_spores").orElseThrow().tendency());
		assertEquals("ruinous tendency", EnumBloodTendency.MORTEM,
				SporiticThuribleSpore.byItemPath("ruinous_spores").orElseThrow().tendency());
		assertEquals("frigid tendency", EnumBloodTendency.CONGEATIO,
				SporiticThuribleSpore.byItemPath("frigid_spores").orElseThrow().tendency());
		assertEquals("ferric tendency", EnumBloodTendency.FERRIC,
				SporiticThuribleSpore.byItemPath("ferric_spores").orElseThrow().tendency());
		assertEquals("umbral tendency", EnumBloodTendency.TENEBRIS,
				SporiticThuribleSpore.byItemPath("umbral_spores").orElseThrow().tendency());
		assertTrue("unknown item path is not a valid catalyst",
				SporiticThuribleSpore.byItemPath("spore_sac").isEmpty());
	}

	private static void matchingTendencyReceivesResonanceDiscounts() {
		assertDouble("matching cost multiplier", 0.85,
				SporiticThuribleResonanceRules.costMultiplier(EnumBloodTendency.MORTEM, EnumBloodTendency.MORTEM));
		assertDouble("matching cooldown multiplier", 0.9,
				SporiticThuribleResonanceRules.cooldownMultiplier(EnumBloodTendency.MORTEM, EnumBloodTendency.MORTEM));
	}

	private static void nonMatchingTendencyDoesNotReceiveResonanceDiscounts() {
		assertDouble("nonmatching cost multiplier", 1.0,
				SporiticThuribleResonanceRules.costMultiplier(EnumBloodTendency.MORTEM, EnumBloodTendency.LUX));
		assertDouble("nonmatching cooldown multiplier", 1.0,
				SporiticThuribleResonanceRules.cooldownMultiplier(EnumBloodTendency.MORTEM, EnumBloodTendency.LUX));
	}

	private static void swingIntensityScalesDrainAndRadius() {
		assertDouble("idle blood drain", 4.0, SporiticThuribleRules.bloodDrainPerSecond(0.0));
		assertDouble("full swing blood drain", 16.0, SporiticThuribleRules.bloodDrainPerSecond(1.0));
		assertDouble("idle aura radius", 2.5, SporiticThuribleRules.auraRadius(0.0));
		assertDouble("full swing aura radius", 5.0, SporiticThuribleRules.auraRadius(1.0));
	}

	private static void burnFractionIsClamped() {
		assertDouble("full burn fraction", 1.0,
				SporiticThuribleRules.burnFraction(SporiticThuribleRules.DEFAULT_BURN_TICKS,
						SporiticThuribleRules.DEFAULT_BURN_TICKS));
		assertDouble("half burn fraction", 0.5,
				SporiticThuribleRules.burnFraction(SporiticThuribleRules.DEFAULT_BURN_TICKS / 2,
						SporiticThuribleRules.DEFAULT_BURN_TICKS));
		assertDouble("empty burn fraction", 0.0, SporiticThuribleRules.burnFraction(0,
				SporiticThuribleRules.DEFAULT_BURN_TICKS));
		assertDouble("missing max burn fraction", 0.0, SporiticThuribleRules.burnFraction(100, 0));
	}

	private static void burnTimeUsesEndTickWithoutPerTickStateMutation() {
		long litAt = 1_000L;
		long endTick = litAt + SporiticThuribleRules.DEFAULT_BURN_TICKS;
		assertEquals("full remaining burn", SporiticThuribleRules.DEFAULT_BURN_TICKS,
				SporiticThuribleRules.remainingBurnTicks(litAt, endTick));
		assertEquals("half remaining burn", SporiticThuribleRules.DEFAULT_BURN_TICKS / 2,
				SporiticThuribleRules.remainingBurnTicks(litAt + SporiticThuribleRules.DEFAULT_BURN_TICKS / 2, endTick));
		assertEquals("expired remaining burn", 0,
				SporiticThuribleRules.remainingBurnTicks(endTick + 20, endTick));
	}

	private static void sporeParticleColorsUseByteRgbValues() {
		SporiticThuribleSpore vivacious = SporiticThuribleSpore.VIVACIOUS;
		assertDouble("vivacious red byte", 184.0, vivacious.red());
		assertDouble("vivacious green byte", 58.0, vivacious.green());
		assertDouble("vivacious blue byte", 75.0, vivacious.blue());
	}

	private static void burningSporeDisplayStackUsesCatalystItemId() {
		try {
			Object displayStack = SporiticThuribleSpore.RUINOUS.getClass().getMethod("displayStack")
					.invoke(SporiticThuribleSpore.RUINOUS);
			boolean isEmpty = (boolean) displayStack.getClass().getMethod("isEmpty").invoke(displayStack);
			assertTrue("display stack exists for catalyst", !isEmpty);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError("burning spore display stack is available", exception);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
