package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

public final class SynapticLoadoutSlotHelperTest {
	private SynapticLoadoutSlotHelperTest() {
	}

	public static void main(String[] args) {
		baseAndMaximumSlotCountsMatchPlan();
		skillLevelsAreClampedToFourBonusSlots();
		slotIndexChecksUseUnlockedCount();
	}

	private static void baseAndMaximumSlotCountsMatchPlan() {
		assertEquals("base slots", 3, SynapticLoadoutSlotHelper.BASE_LOADOUT_SLOTS);
		assertEquals("max slots", 7, SynapticLoadoutSlotHelper.MAX_LOADOUT_SLOTS);
	}

	private static void skillLevelsAreClampedToFourBonusSlots() {
		assertEquals("negative levels use base", 3, SynapticLoadoutSlotHelper.slotsForSkillLevel(-1));
		assertEquals("zero levels use base", 3, SynapticLoadoutSlotHelper.slotsForSkillLevel(0));
		assertEquals("two levels add two slots", 5, SynapticLoadoutSlotHelper.slotsForSkillLevel(2));
		assertEquals("four levels reach max", 7, SynapticLoadoutSlotHelper.slotsForSkillLevel(4));
		assertEquals("excess levels stay capped", 7, SynapticLoadoutSlotHelper.slotsForSkillLevel(12));
	}

	private static void slotIndexChecksUseUnlockedCount() {
		assertTrue("first slot unlocked", SynapticLoadoutSlotHelper.isSlotUnlocked(0, 0));
		assertTrue("third base slot unlocked", SynapticLoadoutSlotHelper.isSlotUnlocked(2, 0));
		assertFalse("fourth slot locked before skill", SynapticLoadoutSlotHelper.isSlotUnlocked(3, 0));
		assertTrue("fourth slot unlocked by one level", SynapticLoadoutSlotHelper.isSlotUnlocked(3, 1));
		assertFalse("negative slot invalid", SynapticLoadoutSlotHelper.isSlotUnlocked(-1, 4));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
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
