package com.vincenthuto.hemomancy.common.recipe;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffFocusRules;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VesperStaffUpgradeRules {
	private VesperStaffUpgradeRules() {
	}

	public static boolean matchesUpgrade(int staffCount, int memoryCount, int otherCount) {
		return staffCount == 1 && memoryCount == 1 && otherCount == 0;
	}

	public static Map<String, Object> copyAndAwaken(Map<String, Object> original) {
		Map<String, Object> upgraded = new LinkedHashMap<>(original);
		upgraded.put(LivingStaffFocusRules.VESPER_MEMORY_AWAKENED_KEY, Boolean.TRUE);
		return upgraded;
	}
}
