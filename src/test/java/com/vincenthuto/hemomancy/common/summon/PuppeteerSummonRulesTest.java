package com.vincenthuto.hemomancy.common.summon;

import java.util.UUID;

public final class PuppeteerSummonRulesTest {
	private PuppeteerSummonRulesTest() {
	}

	public static void main(String[] args) {
		assertTrue("follow command mode round trip",
				PuppeteerCommandMode.FOLLOW == PuppeteerCommandMode.fromSerializedName("follow"));
		assertTrue("unknown command mode defaults to follow",
				PuppeteerCommandMode.FOLLOW == PuppeteerCommandMode.fromSerializedName("old_or_bad"));
		assertFalse("network parsing rejects an unknown command mode",
				PuppeteerCommandMode.tryParse("old_or_bad").isPresent());
		assertTrue("follow retains a nearby defensive target",
				PuppeteerCommandMode.FOLLOW.retainsAutomaticTarget());
		assertFalse("passive does not retain an automatic target",
				PuppeteerCommandMode.PASSIVE.retainsAutomaticTarget());
		assertTrue("guard retains a valid anchor-area target",
				PuppeteerCommandMode.GUARD.retainsAutomaticTarget());
		assertTrue("hunt retains an automatic hostile target",
				PuppeteerCommandMode.HUNT.retainsAutomaticTarget());
		assertEquals("four v1 summons", 4, PuppeteerSummonDefinitions.all().size());

		PuppeteerSummonDefinition vulture = PuppeteerSummonDefinitions.byName("veinwing_vulture")
				.orElseThrow(() -> new AssertionError("missing veinwing vulture"));
		PuppeteerSummonDefinition spitter = PuppeteerSummonDefinitions.byName("marrow_spitter")
				.orElseThrow(() -> new AssertionError("missing marrow spitter"));
		PuppeteerSummonDefinition hulk = PuppeteerSummonDefinitions.byName("gorebound_hulk")
				.orElseThrow(() -> new AssertionError("missing gorebound hulk"));
		PuppeteerSummonDefinition puppet = PuppeteerSummonDefinitions.byName("mnemonist_puppet")
				.orElseThrow(() -> new AssertionError("missing mnemonist puppet"));

		assertEquals("vulture degree", 3, vulture.requiredDegree());
		assertEquals("spitter degree", 3, spitter.requiredDegree());
		assertEquals("hulk degree", 4, hulk.requiredDegree());
		assertEquals("mnemonist puppet degree", 5, puppet.requiredDegree());
		assertFalse("degree 2 only foreshadows puppeteering", PuppeteerSummonRules.canUnlockAtDegree(vulture, 2));
		assertTrue("degree 3 can unlock vulture", PuppeteerSummonRules.canUnlockAtDegree(vulture, 3));
		assertFalse("degree 2 cannot unlock spitter", PuppeteerSummonRules.canUnlockAtDegree(spitter, 2));
		assertTrue("degree 4 can unlock hulk", PuppeteerSummonRules.canUnlockAtDegree(hulk, 4));
		assertFalse("degree 4 cannot unlock mnemonist puppet", PuppeteerSummonRules.canUnlockAtDegree(puppet, 4));
		assertTrue("degree 5 can unlock mnemonist puppet", PuppeteerSummonRules.canUnlockAtDegree(puppet, 5));

		assertEquals("base active cap", 1, PuppeteerSummonRules.activeSummonCap(0));
		assertEquals("puppet skein level 3 cap", 4, PuppeteerSummonRules.activeSummonCap(3));
		assertTrue("a claimed Will does not consume an unused shaped-body slot",
				PuppeteerSummonRules.canRetainBody(false, 1, 0, 1, 1));
		assertFalse("a shaped body cannot consume a claimed-Will-only bonus slot",
				PuppeteerSummonRules.canRetainBody(false, 1, 1, 1, 1));
		assertTrue("a claimed Will may occupy the Silent Archon bonus slot",
				PuppeteerSummonRules.canRetainBody(true, 1, 1, 1, 1));
		assertFalse("no body may exceed the combined cap",
				PuppeteerSummonRules.canRetainBody(true, 2, 1, 1, 1));
		assertEquals("hot swap removes only its matching cohort", 3,
				PuppeteerSummonRules.projectedShapedCount(5, 3, 1));
		assertEquals("malformed cohort count cannot create capacity", 6,
				PuppeteerSummonRules.projectedShapedCount(5, -2, 1));
		assertDouble("living sinew health scale", 1.45, PuppeteerSummonRules.healthMultiplier(3));
		assertDouble("living sinew damage scale", 1.30, PuppeteerSummonRules.damageMultiplier(3));
		assertDouble("far tether range", 40.0, PuppeteerSummonRules.commandRange(3));
		assertDouble("bound command extends range after far tether", 52.0, PuppeteerSummonRules.commandRange(3, 3));
		assertDouble("morphling gnawing reduces effective tether range", 39.0,
				PuppeteerSummonRules.effectiveCommandRange(3, 3, true));
		assertDouble("ordinary tether range is unchanged without a morphling", 52.0,
				PuppeteerSummonRules.effectiveCommandRange(3, 3, false));

		assertEquals("thread refill respects capacity", 256, PuppeteerSummonRules.refilledThread(240, 40));
		assertEquals("one physical thread supplies eight charge", 8, PuppeteerSummonRules.THREAD_PER_ITEM);
		assertEquals("three physical threads supply twenty-four charge", 24,
				PuppeteerSummonRules.threadChargeFromItems(3));
		assertEquals("less than one item's space accepts no thread", 0,
				PuppeteerSummonRules.threadItemsAccepted(7, 64));
		assertEquals("charge space accepts only whole thread items", 2,
				PuppeteerSummonRules.threadItemsAccepted(20, 64));
		assertEquals("bound command raises thread capacity", 352, PuppeteerSummonRules.threadCapacity(3));
		assertEquals("thread refill respects upgraded capacity", 300, PuppeteerSummonRules.refilledThread(240, 60, 3));
		assertEquals("capacity downgrade clamps stored charge", 352,
				PuppeteerSummonRules.clampThreadToCapacity(400, 352));
		assertDouble("thread economy reduces call cost", 0.85, PuppeteerSummonRules.threadCostMultiplier(3));
		assertEquals("thread economy rounds a summon cost up to whole charge", 24,
				PuppeteerSummonRules.adjustedThreadCost(28, 3));
		assertEquals("thread economy rounds upkeep up to whole charge", 16,
				PuppeteerSummonRules.adjustedThreadCost(18, 3));
		assertEquals("morphling gnawing rounds modified upkeep up", 24,
				PuppeteerSummonRules.interferedThreadUpkeep(16, true));
		assertEquals("ordinary upkeep is unchanged without a morphling", 16,
				PuppeteerSummonRules.interferedThreadUpkeep(16, false));
		assertEquals("thread economy never reduces a positive payment below one", 1,
				PuppeteerSummonRules.adjustedThreadCost(1, 99));
		assertEquals("thread refill ignores negative input", 120, PuppeteerSummonRules.refilledThread(120, -5));
		assertTrue("vulture has higher upkeep than hulk", vulture.threadUpkeepPerMinute() > hulk.threadUpkeepPerMinute());

		assertEquals("missing crossbar dismissal lasts five seconds", 100, PuppeteerSummonRules.CROSSBAR_DISMISSAL_TICKS);
		assertEquals("bound command gives longer dismissal grace", 160, PuppeteerSummonRules.dismissalGraceTicks(3));
		assertDouble("stable summon renders fully", 1.0, PuppeteerSummonRules.dismissalAlpha(0, 0.0F));
		assertDouble("half dismissed summon renders half strength", 0.5, PuppeteerSummonRules.dismissalAlpha(50, 0.0F));
		assertDouble("expired dismissal is transparent", 0.0, PuppeteerSummonRules.dismissalAlpha(1, 1.0F));
		assertTrue("fresh dismissal is visible", PuppeteerSummonRules.shouldRenderDismissingSummon(20, 100));
		assertFalse("late dismissal flickers out", PuppeteerSummonRules.shouldRenderDismissingSummon(20, 10));

		UUID owner = UUID.randomUUID();
		UUID stranger = UUID.randomUUID();
		assertTrue("an unbound crossbar may be attuned", PuppeteerSummonRules.canAttuneCrossbar(null, owner));
		assertTrue("an owner may re-attune their crossbar", PuppeteerSummonRules.canAttuneCrossbar(owner, owner));
		assertFalse("a stranger may not overwrite crossbar ownership",
				PuppeteerSummonRules.canAttuneCrossbar(owner, stranger));
		assertEquals("persistent upkeep deadline is one minute later", 1700,
				(int) PuppeteerSummonRules.nextUpkeepGameTime(500));
		assertTrue("persistent upkeep is due at its deadline", PuppeteerSummonRules.upkeepDue(1700L, 1700L));
		assertFalse("persistent upkeep is not due before its deadline", PuppeteerSummonRules.upkeepDue(1699L, 1700L));
		assertTrue("changing dimension deliberately unravels a summon",
				PuppeteerSummonRules.shouldUnravelForDimension(false));
		assertFalse("remaining in the owner's dimension preserves a summon",
				PuppeteerSummonRules.shouldUnravelForDimension(true));
		assertTrue("loaded owned shaped body qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, true, false, true, true));
		assertTrue("commandeered Will qualifies through the same owned tether rule",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, true, false, true, true));
		assertFalse("trial body never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, true, true, true, true));
		assertFalse("foreign body never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, false, false, true, true));
		assertFalse("stale owner session never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, true, false, true, false));
		assertFalse("cross-dimension body never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, true, true, false, false, true));
		assertFalse("dead body never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(false, true, true, false, true, true));
		assertFalse("unloaded body never qualifies for interference",
				PuppeteerSummonRules.qualifiesForMorphlingInterference(true, false, true, false, true, true));
		assertFalse("a player-owned body survives Peaceful difficulty",
				PuppeteerSummonRules.shouldDespawnInPeaceful(false, owner));
		assertTrue("an unbound trial retains hostile Peaceful despawn semantics",
				PuppeteerSummonRules.shouldDespawnInPeaceful(true, null));
		assertTrue("a malformed unowned body may despawn in Peaceful",
				PuppeteerSummonRules.shouldDespawnInPeaceful(false, null));
		assertEquals("commandeered Will has explicit elite upkeep", 16,
				PuppeteerSummonRules.CLAIMED_WILL_UPKEEP_PER_MINUTE);
		assertTrue("a focus target inside tether range is valid",
				PuppeteerSummonRules.withinTetherRange(100.0, 16.0));
		assertFalse("a focus target beyond tether range is rejected",
				PuppeteerSummonRules.withinTetherRange(300.0, 16.0));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
