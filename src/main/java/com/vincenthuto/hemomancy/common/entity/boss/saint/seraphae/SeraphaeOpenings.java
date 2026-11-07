package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;

import java.util.EnumSet;
import java.util.Set;

/**
 * Seraphae's four punish windows (design section 6). Her health is never the bar, so each takes damage at 1 and feeds
 * integrity through {@link SeraphaeIntegrityRules#hitGain}; each counts melee, projectile and school-cast hits up to a
 * cap that grows by one per challenger past the first. The No Rest Given deed (Repose) shortens the Recoils.
 */
public final class SeraphaeOpenings {
	public static final int RECOIL_ID = 1;
	public static final int SEIZED_ID = 2;
	public static final int CONDENSING_ID = 3;
	public static final int LAPSE_ID = 4;

	public static final int RECOIL_TICKS = 40;
	public static final int RECOIL_REPOSE_TICKS = 28;
	public static final int STRAINING_RECOIL_TICKS = 60;
	public static final int STRAINING_RECOIL_REPOSE_TICKS = 40;
	public static final int BROKEN_HOLD_TICKS = 20;
	public static final int SEIZED_TICKS = 30;
	/** Ten ticks of gathering, then sixty of the window proper. */
	public static final int CONDENSING_GATHER = 10;
	public static final int CONDENSING_TICKS = CONDENSING_GATHER + 60;
	public static final int LAPSE_TICKS = 50;

	private static final Set<SaintOpening.Delivery> HITS = EnumSet.of(SaintOpening.Delivery.MELEE,
			SaintOpening.Delivery.PROJECTILE, SaintOpening.Delivery.SCHOOL_CAST);

	public static final SaintOpening.Spec RECOIL = spec(RECOIL_ID, "recoil", RECOIL_TICKS, 4);
	public static final SaintOpening.Spec RECOIL_REPOSE = spec(RECOIL_ID, "recoil", RECOIL_REPOSE_TICKS, 4);
	public static final SaintOpening.Spec STRAINING_RECOIL = spec(RECOIL_ID, "recoil", STRAINING_RECOIL_TICKS, 4);
	public static final SaintOpening.Spec STRAINING_RECOIL_REPOSE = spec(RECOIL_ID, "recoil",
			STRAINING_RECOIL_REPOSE_TICKS, 4);
	/** A hold broken by the held player or by allies: a short Recoil, two hits. */
	public static final SaintOpening.Spec BROKEN_HOLD = spec(RECOIL_ID, "recoil", BROKEN_HOLD_TICKS, 2);
	public static final SaintOpening.Spec SEIZED = spec(SEIZED_ID, "seized", SEIZED_TICKS, 3);
	public static final SaintOpening.Spec CONDENSING = spec(CONDENSING_ID, "condensing", CONDENSING_TICKS, 5);
	public static final SaintOpening.Spec LAPSE = spec(LAPSE_ID, "lapse", LAPSE_TICKS, 4);

	private SeraphaeOpenings() {
	}

	/** The Recoil a whiffed reach opens: the Straining Reach's is longer; No Rest Given shortens both. */
	public static SaintOpening.Spec recoil(boolean straining, boolean noRest) {
		if (straining) return noRest ? STRAINING_RECOIL_REPOSE : STRAINING_RECOIL;
		return noRest ? RECOIL_REPOSE : RECOIL;
	}

	private static SaintOpening.Spec spec(int id, String key, int ticks, int cap) {
		return new SaintOpening.Spec(id, key, ticks, 1.0F, cap, 1, HITS);
	}
}
