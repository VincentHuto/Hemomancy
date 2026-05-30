package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffUtilitySelectionRules {
	private LivingStaffUtilitySelectionRules() {
	}

	public static boolean isSelectedUtility(String selectedManipName, String utilityManipName) {
		return selectedManipName != null
				&& !selectedManipName.isEmpty()
				&& selectedManipName.equals(utilityManipName);
	}
}
