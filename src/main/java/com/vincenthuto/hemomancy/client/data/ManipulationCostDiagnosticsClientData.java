package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostSnapshot;

public final class ManipulationCostDiagnosticsClientData {
	private static ManipulationCostSnapshot snapshot = ManipulationCostSnapshot.EMPTY;

	private ManipulationCostDiagnosticsClientData() {
	}

	public static void set(ManipulationCostSnapshot nextSnapshot) {
		snapshot = nextSnapshot == null ? ManipulationCostSnapshot.EMPTY : nextSnapshot;
	}

	public static ManipulationCostSnapshot get() {
		return snapshot;
	}

	public static void clear() {
		snapshot = ManipulationCostSnapshot.EMPTY;
	}
}
