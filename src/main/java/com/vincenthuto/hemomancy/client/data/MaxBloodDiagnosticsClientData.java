package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodSnapshot;

public final class MaxBloodDiagnosticsClientData {
	private static MaxBloodSnapshot snapshot = MaxBloodSnapshot.EMPTY;

	private MaxBloodDiagnosticsClientData() {
	}

	public static void set(MaxBloodSnapshot nextSnapshot) {
		snapshot = nextSnapshot == null ? MaxBloodSnapshot.EMPTY : nextSnapshot;
	}

	public static MaxBloodSnapshot get() {
		return snapshot;
	}

	public static void clear() {
		snapshot = MaxBloodSnapshot.EMPTY;
	}
}
