package com.vincenthuto.hemomancy.common.mission.shared;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

/** The Blood Beneath Blood ledger bar: nine pomes, Communion, a chosen path, Vesper's wound or defeat, then the finished path. */
public final class BloodBeneathProgress {
	public static final int TOTAL = 13;

	private BloodBeneathProgress() {}

	public static int movements(int pomesConsumed, boolean communionComplete, EnumArchonPath path,
			boolean severedPortalOpen, boolean vesperDefeated) {
		if (path == EnumArchonPath.APOTHEOS || path == EnumArchonPath.SILENTIUM) return TOTAL;
		boolean chosen = path == EnumArchonPath.SILENT_PENDING || path == EnumArchonPath.APOTHEOS_PENDING;
		return Math.min(9, pomesConsumed) + (communionComplete ? 1 : 0) + (chosen ? 1 : 0)
				+ (severedPortalOpen || vesperDefeated ? 1 : 0);
	}
}
