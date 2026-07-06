package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowSnapshot;

public final class BloodFlowDiagnosticsClientData {
	private static BloodFlowSnapshot snapshot = BloodFlowSnapshot.EMPTY;

	private BloodFlowDiagnosticsClientData() {
	}

	public static void set(BloodFlowSnapshot nextSnapshot) {
		snapshot = nextSnapshot == null ? BloodFlowSnapshot.EMPTY : nextSnapshot;
	}

	public static BloodFlowSnapshot get() {
		return snapshot;
	}

	public static void clear() {
		snapshot = BloodFlowSnapshot.EMPTY;
	}
}
