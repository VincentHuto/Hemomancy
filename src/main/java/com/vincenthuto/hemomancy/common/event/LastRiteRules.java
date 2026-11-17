package com.vincenthuto.hemomancy.common.event;

/**
 * Pure rules for the shared "Last Rite" death-save group: the blood may
 * refuse the return only once per shared cooldown, no matter which source
 * (Ink Mantle Reprieve, Last-Light Mantle, Silent Archon refusal, future
 * Cryptobiosis) does the refusing.
 *
 * <p>The armed-source id is updated when eligible sources are equipped or
 * changed. Only that most-recently armed source may request the save once the
 * shared cooldown has elapsed.</p>
 *
 * <p>No Minecraft imports on purpose — adapter is LastRiteHelper.</p>
 */
public final class LastRiteRules {

	public static final int DEFAULT_SHARED_COOLDOWN_TICKS = 12_000;
	/** The one source that yields: it arms only while nothing else is armed, and any other source arms over it. */
	public static final String CURVED_HORN_ID = "hemomancy:curved_horn";

	private LastRiteRules() {
	}

	/**
	 * A rite may fire only when the shared cooldown has elapsed. The armed id
	 * participates defensively: while the cooldown is active, only the source
	 * that armed it could conceivably re-fire, and the cooldown blocks that
	 * too — so any non-empty mismatch inside the window is a hard no.
	 */
	public static boolean canFire(String armedSourceId, String requestSourceId, long now, long sharedCooldownUntil) {
		if (requestSourceId == null || requestSourceId.isEmpty()) {
			return false;
		}
		if (armedSourceId == null || armedSourceId.isEmpty()) {
			return false;
		}
		if (!armedSourceId.equals(requestSourceId)) {
			return false;
		}
		return sharedCooldownUntil <= now;
	}

	/** Whether another source may arm now: nothing is armed, or only the yielding Curved Horn is. */
	public static boolean isOpenToArm(String armedSourceId) {
		return armedSourceId == null || armedSourceId.isEmpty() || CURVED_HORN_ID.equals(armedSourceId);
	}

	public static long nextSharedCooldownUntil(long now, int cooldownTicks) {
		return now + Math.max(0, cooldownTicks);
	}
}
