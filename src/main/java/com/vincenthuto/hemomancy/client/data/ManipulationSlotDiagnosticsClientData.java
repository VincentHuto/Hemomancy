package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationSlotSnapshot;

public final class ManipulationSlotDiagnosticsClientData {
	private static ManipulationSlotSnapshot snapshot = ManipulationSlotSnapshot.EMPTY;

	private ManipulationSlotDiagnosticsClientData() {
	}

	public static void set(ManipulationSlotSnapshot nextSnapshot) {
		snapshot = nextSnapshot == null ? ManipulationSlotSnapshot.EMPTY : nextSnapshot;
	}

	public static ManipulationSlotSnapshot get() {
		return snapshot;
	}

	public static void clear() {
		snapshot = ManipulationSlotSnapshot.EMPTY;
	}
}
