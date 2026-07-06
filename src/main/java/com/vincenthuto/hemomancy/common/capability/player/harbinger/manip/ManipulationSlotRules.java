package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.List;

public final class ManipulationSlotRules {
	private ManipulationSlotRules() {
	}

	public static ManipulationSlotSnapshot snapshot(List<ManipulationSlotContribution> contributions, int maxSlots) {
		List<ManipulationSlotContribution> safeContributions =
				contributions == null ? List.of() : contributions;
		int requested = 0;
		for (ManipulationSlotContribution contribution : safeContributions) {
			requested += contribution.amount();
		}
		int cap = Math.max(0, maxSlots);
		int applied = Math.min(Math.max(0, requested), cap);
		return new ManipulationSlotSnapshot(safeContributions, requested, cap, applied,
				Math.max(0, requested - applied));
	}

	public static int excessSkillLevels(int baseSlots, int degreeBonus, int skillBonus, int maxSlots) {
		int baseAndDegree = Math.max(0, baseSlots + degreeBonus);
		int safeSkill = Math.max(0, skillBonus);
		int safeMax = Math.max(0, maxSlots);
		if (baseAndDegree >= safeMax) {
			return safeSkill;
		}
		int room = safeMax - baseAndDegree;
		return Math.max(0, safeSkill - room);
	}
}
