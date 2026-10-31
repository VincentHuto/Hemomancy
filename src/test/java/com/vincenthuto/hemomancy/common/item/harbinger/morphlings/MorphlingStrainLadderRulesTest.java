package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** One rules test per strain: the spec's per-stage numbers, home multipliers and caps (track 02, "Tests and proof"). */
final class MorphlingStrainLadderRulesTest {
	private static final double E = 1.0E-9D;

	@Test
	void sunburst() {
		assertEquals(0.25D, SunburstRules.sunFedHunger(0), E);
		assertEquals(1.0D, SunburstRules.sunFedHunger(5), E);
		assertEquals(3.5D, SunburstRules.sunFedBlood(5), E);
		assertEquals(0.0F, SunburstRules.sharedTableGift(1));
		assertEquals(2.0F, SunburstRules.sharedTableGift(2));
		assertEquals(5.0F, SunburstRules.sharedTableGift(5));
		assertTrue(SunburstRules.waivesUpkeep(true, false));
		assertFalse(SunburstRules.waivesUpkeep(true, true), "Puppet Interference cancels the waiver");
		assertFalse(SunburstRules.waivesUpkeep(false, false));
		assertTrue(SunburstRules.surplusBecomesBlood(20, true));
		assertFalse(SunburstRules.surplusBecomesBlood(20, false), "surplus only in daylight");
		assertFalse(SunburstRules.surplusBecomesBlood(19, true));
	}

	@Test
	void holdfast() {
		assertEquals(0.1D, HoldfastRules.gripKnockback(0), E);
		assertEquals(0.6D, HoldfastRules.gripKnockback(5), E);
		assertEquals(0.6D, HoldfastRules.gripBreakSpeed(5), E);
		assertTrue(HoldfastRules.gripBreakSpeed(5) < 0.9D, "below the old all-block bonus");
		assertEquals(10.0D, HoldfastRules.HOLDING_HOME_RANGE, E);
	}

	@Test
	void mermaidsPurse() {
		assertEquals(1.0D, MermaidsPurseRules.siphon(0), E);
		assertEquals(0.0D, MermaidsPurseRules.swimBonus(0, true), E);
		assertEquals(0.25D, MermaidsPurseRules.swimBonus(5, false), E);
		assertEquals(0.35D, MermaidsPurseRules.swimBonus(5, true), E, "never past the Prismatic ceiling");
		assertEquals(0.15D, MermaidsPurseRules.swimBonus(2, true), E);
		assertEquals(0.35D, MermaidsPurseRules.swimBonus(5, true, 1.0D), E, "no set: the strain's own ceiling");
		assertEquals(0.0D, MermaidsPurseRules.swimBonus(5, true, 1.35D), E, "the full set already sits at +35%");
		assertEquals(0.0D, MermaidsPurseRules.swimBonus(2, false, 1.35D), E);
		assertEquals(6.0F, MermaidsPurseRules.schoolHeal(3));
		assertEquals(12.0F, MermaidsPurseRules.schoolHeal(5));
		assertEquals(60.0D, MermaidsPurseRules.bankDeposit(2, 10.0F, false, false), E);
		assertEquals(72.0D, MermaidsPurseRules.bankDeposit(3, 10.0F, false, false), E);
		assertEquals(108.0D, MermaidsPurseRules.bankDeposit(3, 10.0F, true, false), E, "sprint-swimming banks 1.5x");
		assertEquals(168.0D, MermaidsPurseRules.bankDeposit(4, 10.0F, false, true), E, "a bill strike banks double");
	}

	@Test
	void deadMansRope() {
		assertEquals(16.0D, DeadMansRopeRules.revealRange(0, false), E);
		assertEquals(45.0D, DeadMansRopeRules.revealRange(5, true), E);
		assertEquals(1.75F, DeadMansRopeRules.repair(5));
		assertTrue(DeadMansRopeRules.lifelineCatches(2, 12.0F));
		assertFalse(DeadMansRopeRules.lifelineCatches(2, 13.0F));
		assertTrue(DeadMansRopeRules.lifelineCatches(3, 24.0F));
		assertFalse(DeadMansRopeRules.lifelineCatches(3, 25.0F));
		assertTrue(DeadMansRopeRules.lifelineCatches(4, 200.0F));
		assertEquals(0, DeadMansRopeRules.maxLinks(2));
		assertEquals(1, DeadMansRopeRules.maxLinks(3));
		assertEquals(3, DeadMansRopeRules.maxLinks(4));
	}

	@Test
	void inkCap() {
		assertEquals(0.10D, InkCapRules.veilReduction(0, false), E);
		assertEquals(0.35D, InkCapRules.veilReduction(5, false), E);
		assertEquals(0.45D, InkCapRules.veilReduction(4, true), E);
		assertEquals(0.50D, InkCapRules.veilReduction(5, true), E, "capped at half even at home");
		assertEquals(30, InkCapRules.inkWakeTicks(2));
		assertEquals(60, InkCapRules.inkWakeTicks(5));
	}

	@Test
	void hushbell() {
		assertEquals(0.1D, HushbellRules.hush(0), E);
		assertEquals(0.6D, HushbellRules.hush(5), E);
		assertEquals(-1, HushbellRules.hideAmplifier(1, true, 5.0F, 20.0F), "Hide starts at Developing");
		assertEquals(0, HushbellRules.hideAmplifier(2, true, 20.0F, 20.0F));
		assertEquals(-1, HushbellRules.hideAmplifier(4, false, 20.0F, 20.0F), "no Resistance II baseline later");
		assertEquals(0, HushbellRules.hideAmplifier(4, false, 9.0F, 20.0F));
		assertEquals(1, HushbellRules.hideAmplifier(2, false, 6.0F, 20.0F));
		assertTrue(HushbellRules.foldShuts(4, 8.0F, 20.0F));
		assertFalse(HushbellRules.foldShuts(3, 8.0F, 20.0F));
		assertFalse(HushbellRules.foldShuts(4, 9.0F, 20.0F));
	}

	@Test
	void marrowplume() {
		assertEquals(0.5F, MarrowplumeRules.feeding(0, false));
		assertEquals(2.625F, MarrowplumeRules.feeding(5, true), 1.0E-6F);
		assertEquals(1.5F, MarrowplumeRules.plumeHeal(3));
		assertEquals(2.0F, MarrowplumeRules.plumeHeal(9));
		assertEquals(6.0D, MarrowplumeRules.plumeRadius(3), E);
		assertEquals(0.0F, MarrowplumeRules.inheritanceChance(3));
		assertEquals(0.25F, MarrowplumeRules.inheritanceChance(4));
		assertEquals(0.40F, MarrowplumeRules.inheritanceChance(5));
		assertTrue(MarrowplumeRules.mayInherit(false, false, false));
		assertFalse(MarrowplumeRules.mayInherit(true, false, false));
		assertFalse(MarrowplumeRules.mayInherit(false, true, false));
		assertFalse(MarrowplumeRules.mayInherit(false, false, true));
	}

	@Test
	void kinbud() {
		assertEquals(0, KinbudRules.stingTicks(1, false));
		assertEquals(60, KinbudRules.stingTicks(2, false));
		assertEquals(120, KinbudRules.stingTicks(5, false));
		assertEquals(90, KinbudRules.stingTicks(2, true));
		assertEquals(2, KinbudRules.kindledHeads(true));
		assertEquals(1, KinbudRules.kindledHeads(false));
		assertEquals(1, KinbudRules.stingAmplifier(true));
		assertEquals(0, KinbudRules.stingAmplifier(false));
	}
}
