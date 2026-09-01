package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingMetabolismRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.ToggleableAbsorptionRules;
import org.junit.jupiter.api.Test;

public final class ToggleableSkillRulesTest {
	@Test
	void agreedTechniqueRules() {
		parentlessLockedSkillsStillRequireUnlocking();
		toggleableSkillsAreEnabledWhenFirstUnlocked();
		distributedSiphonChangesTargetCountWithoutMultiplyingThroughput();
		sharedSiphonRedirectsRatherThanDuplicatesBlood();
		vascularMercyStopsOrdinaryDamageAtOneHealth();
		bloodReserveProtectsItsFloorFromPassiveSpending();
		symbioticMetabolismSplitsUpkeepAndProtectsLowHunger();
		dormantSymbioteSuspendsUpkeepOutsideUsefulActivity();
	}

	private static void parentlessLockedSkillsStillRequireUnlocking() {
		SkillPoint technique = new SkillPoint(901, "test_parentless_technique", 500, 1,
				EnumSkillStates.LOCKED, null).setToggleable(true);
		SkillProgress progress = new SkillProgress();
		assertFalse("parentless locked technique is not unlocked", progress.isUnlocked(technique));
		assertFalse("parentless locked technique cannot toggle", progress.toggleEnabled(technique));

		progress.setSkill(technique, EnumSkillStates.UNLOCKED, 0);
		assertFalse("legacy unlocked level zero state is repaired", progress.isUnlocked(technique));
		progress.setSkill(technique, EnumSkillStates.UNLOCKED, 1);
		assertTrue("paid unlock enables technique", progress.isEnabled(technique));
	}

	private static void sharedSiphonRedirectsRatherThanDuplicatesBlood() {
		assertClose("shared siphon keeps three quarters personal", 9.0D,
				ToggleableAbsorptionRules.personalBlood(true, 12.0D));
		assertClose("shared siphon redirects one quarter", 3.0D,
				ToggleableAbsorptionRules.sharedBlood(true, 12.0D));
		assertClose("disabled siphon keeps everything personal", 12.0D,
				ToggleableAbsorptionRules.personalBlood(false, 12.0D));
		assertClose("disabled siphon shares nothing", 0.0D,
				ToggleableAbsorptionRules.sharedBlood(false, 12.0D));
	}

	private static void toggleableSkillsAreEnabledWhenFirstUnlocked() {
		SkillPoint parent = new SkillPoint(899, "test_parent", 0, 1,
				EnumSkillStates.UNLOCKED, null);
		SkillPoint technique = new SkillPoint(900, "test_technique", 0, 1,
				EnumSkillStates.LOCKED, parent).setToggleable(true);
		SkillProgress progress = new SkillProgress();
		assertFalse("locked technique is disabled", progress.isEnabled(technique));
		progress.setSkill(technique, EnumSkillStates.UNLOCKED, 1);
		assertTrue("newly unlocked technique defaults on", progress.isEnabled(technique));
		assertTrue("toggle changes enabled state", progress.toggleEnabled(technique));
		assertFalse("second state is off", progress.isEnabled(technique));
	}

	private static void distributedSiphonChangesTargetCountWithoutMultiplyingThroughput() {
		assertEquals("focused mode has one target", 1,
				ToggleableAbsorptionRules.targetCount(false, 6, 10));
		assertEquals("distributed mode honors cap", 6,
				ToggleableAbsorptionRules.targetCount(true, 6, 10));
		assertClose("damage is divided", 2.0D,
				ToggleableAbsorptionRules.damagePerTarget(true, 12.0D, 6));
		assertClose("focused damage is unchanged", 12.0D,
				ToggleableAbsorptionRules.damagePerTarget(false, 12.0D, 1));
	}

	private static void vascularMercyStopsOrdinaryDamageAtOneHealth() {
		assertClose("mercy clamps lethal drain", 4.0D,
				ToggleableAbsorptionRules.clampDamageForMercy(true, false, 5.0D, 12.0D));
		assertClose("boss absorption remains authored", 12.0D,
				ToggleableAbsorptionRules.clampDamageForMercy(true, true, 5.0D, 12.0D));
	}

	private static void bloodReserveProtectsItsFloorFromPassiveSpending() {
		assertClose("reserve limits drain", 100.0D,
				ToggleableSkillRules.allowedBloodDrain(true, 1000.0D, 900.0D, 200.0D));
		assertClose("disabled reserve allows full drain", 200.0D,
				ToggleableSkillRules.allowedBloodDrain(false, 1000.0D, 900.0D, 200.0D));
	}

	private static void symbioticMetabolismSplitsUpkeepAndProtectsLowHunger() {
		MorphlingMetabolismRules.Upkeep split = MorphlingMetabolismRules.splitUpkeep(true, false,
				20, 20, 10.0D);
		assertClose("half blood", 5.0D, split.blood());
		assertClose("half hunger", 5.0D, split.hungerEquivalent());
		MorphlingMetabolismRules.Upkeep protectedPlayer = MorphlingMetabolismRules.splitUpkeep(true, false,
				6, 20, 10.0D);
		assertClose("low hunger falls back to blood", 10.0D, protectedPlayer.blood());
		assertClose("low hunger is not consumed", 0.0D, protectedPlayer.hungerEquivalent());
	}

	private static void dormantSymbioteSuspendsUpkeepOutsideUsefulActivity() {
		assertTrue("dormant skill suspends idle upkeep",
				MorphlingMetabolismRules.suspendUpkeep(true, false, false));
		assertFalse("combat remains active",
				MorphlingMetabolismRules.suspendUpkeep(true, true, false));
		assertFalse("bonding remains active",
				MorphlingMetabolismRules.suspendUpkeep(true, false, true));
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) throw new AssertionError(label);
	}

	private static void assertFalse(String label, boolean value) {
		if (value) throw new AssertionError(label);
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) throw new AssertionError(label + ": expected " + expected + " but got " + actual);
	}

	private static void assertClose(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.0001D) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
