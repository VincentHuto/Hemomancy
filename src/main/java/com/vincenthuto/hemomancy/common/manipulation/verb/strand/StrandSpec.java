package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import javax.annotation.Nullable;

/**
 * What one manipulation's strand is, as data on its registration: the form, the blood each pulse costs (one pulse
 * every {@link #CADENCE} ticks, before the cost ledger), how hard it hits against the form's base numbers, how long
 * its accent lays the school's state, and the extra blood a snap costs.
 *
 * @param form          the strand's shape
 * @param bloodPerPulse blood per pulse while the strand is out
 * @param damageScale   multiplies the form's base damage (a Humilis teaching lash hits softer)
 * @param stateTicks    the school state's long duration, laid by the form's accent
 * @param snapBlood     extra blood a snap costs; a reversal crack is free beyond the upkeep
 * @param lash          a lash's tap and charge numbers; null for every other form
 */
public record StrandSpec(StrandForm form, double bloodPerPulse, float damageScale, int stateTicks, double snapBlood,
		@Nullable LashProfile lash) {
	/** Every strand pulses on the hurt cooldown: blood is paid twice a second while it is out. */
	public static final int CADENCE = 10;
	/** The long duration an accent lays a school state for, unless a spec says otherwise. */
	public static final int LONG_STATE = 240;

	public StrandSpec {
		if (form == null) throw new IllegalArgumentException("a strand needs a form");
		if (form == StrandForm.LASH && lash == null) throw new IllegalArgumentException("a lash needs its profile");
		bloodPerPulse = Math.max(0.0D, bloodPerPulse);
		damageScale = Math.max(0.0F, damageScale);
		stateTicks = Math.max(1, stateTicks);
		snapBlood = Math.max(0.0D, snapBlood);
	}

	/** Blood a second while the strand is out, at the base price. */
	public double bloodPerSecond() {
		return bloodPerPulse * 20.0D / CADENCE;
	}
}
