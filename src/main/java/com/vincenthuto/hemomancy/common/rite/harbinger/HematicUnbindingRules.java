package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.UUID;

/** Two-performance warning gate for permanently dissolving a bloodline. */
public final class HematicUnbindingRules {
	public static final long CONFIRMATION_TICKS = 12_000L;

	private HematicUnbindingRules() {
	}

	public static Decision decision(UUID bloodline, UUID warnedBloodline,
			long warningExpiresAt, long gameTime) {
		if (bloodline != null && bloodline.equals(warnedBloodline)
				&& gameTime <= warningExpiresAt) {
			return Decision.CONFIRM;
		}
		return Decision.WARN;
	}

	public enum Decision {
		WARN,
		CONFIRM
	}
}
