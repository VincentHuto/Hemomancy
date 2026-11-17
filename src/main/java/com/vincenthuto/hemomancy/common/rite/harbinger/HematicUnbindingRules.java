package com.vincenthuto.hemomancy.common.rite.harbinger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Two-activation warning gate for permanently dissolving a bloodline. The first activation only warns and takes no
 * offering; a second inside the window starts the rite. The window is session state, cleared by
 * {@code TransientServerStateEvents} when a server starts and stops.
 */
public final class HematicUnbindingRules {
	public static final long CONFIRMATION_TICKS = 12_000L;

	private static final Map<UUID, Warning> WARNINGS = new HashMap<>();

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

	/** One activation by {@code leader}: a WARN opens the window, a CONFIRM spends it. */
	public static Decision activate(UUID leader, UUID bloodline, long gameTime) {
		Warning warning = WARNINGS.get(leader);
		Decision decision = warning == null ? decision(bloodline, null, 0, gameTime)
				: decision(bloodline, warning.bloodline(), warning.expiresAt(), gameTime);
		if (decision == Decision.CONFIRM) {
			WARNINGS.remove(leader);
		} else {
			WARNINGS.put(leader, new Warning(bloodline, gameTime + CONFIRMATION_TICKS));
		}
		return decision;
	}

	public static void clearSessionState() {
		WARNINGS.clear();
	}

	private record Warning(UUID bloodline, long expiresAt) {
	}

	public enum Decision {
		WARN,
		CONFIRM
	}
}
