package com.vincenthuto.hemomancy.common.event;

/**
 * Pure rules for the shared "Last Rite" death-save group: the blood may
 * refuse the return only once per shared cooldown, no matter which source
 * (Ink Mantle Reprieve, Last-Light Mantle, Silent Archon refusal, future
 * Cryptobiosis) does the refusing.
 *
 * <p>The armed-source id is recorded on consumption (arm-on-use) so tooling
 * and tooltips can report which rite last fired; within the shared cooldown
 * every source is blocked regardless, which is the doctrine's whole point.
 * A future most-recently-equipped arming pass can tighten canFire further
 * without changing call sites.</p>
 *
 * <p>No Minecraft imports on purpose — adapter is LastRiteHelper.</p>
 */
public final class LastRiteRules {

	public static final int DEFAULT_SHARED_COOLDOWN_TICKS = 12_000;

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
		if (sharedCooldownUntil > now) {
			return false;
		}
		return true;
	}

	public static long nextSharedCooldownUntil(long now, int cooldownTicks) {
		return now + Math.max(0, cooldownTicks);
	}
}
