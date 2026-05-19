package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.List;

public final class ManipulationEquipHelper {
	private ManipulationEquipHelper() {
	}

	public static boolean equipNameIfPossible(List<String> equippedNames, String manipName, int maxSlots) {
		if (equippedNames == null || manipName == null || manipName.isEmpty()
				|| equippedNames.contains(manipName)
				|| equippedNames.size() >= maxSlots) {
			return false;
		}
		equippedNames.add(manipName);
		return true;
	}
}
