package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPhaseTable;
import net.minecraft.world.BossEvent;

/**
 * Her three phases by containment integrity (design section 3), ratcheted: once integrity first reaches 40 or 75 the
 * phase is kept even if it falls back. The vanilla bar stays white; her painted frame carries the look.
 */
public final class SeraphaePhases {
	public static final int REACHING = 1;
	public static final int RESTRAINT = 2;
	public static final int ESCAPING_LIGHT = 3;
	public static final String REACHING_KEY = "boss.hemomancy.seraphae.phase.reaching";
	public static final String RESTRAINT_KEY = "boss.hemomancy.seraphae.phase.restraint";
	public static final String ESCAPING_LIGHT_KEY = "boss.hemomancy.seraphae.phase.escaping_light";

	private SeraphaePhases() {
	}

	public static SaintPhaseTable table() {
		return SaintPhaseTable.byMeter(
				new SaintPhaseTable.Phase(REACHING, REACHING_KEY, BossEvent.BossBarColor.WHITE, 0.0F, null),
				new SaintPhaseTable.Phase(RESTRAINT, RESTRAINT_KEY, BossEvent.BossBarColor.WHITE,
						SeraphaeIntegrityRules.PHASE_TWO, BossPresentationIds.SCENE_SERAPHAE_CHAINS_TIGHTEN_CARD),
				new SaintPhaseTable.Phase(ESCAPING_LIGHT, ESCAPING_LIGHT_KEY, BossEvent.BossBarColor.WHITE,
						SeraphaeIntegrityRules.PHASE_THREE, BossPresentationIds.SCENE_SERAPHAE_LIGHT_ESCAPES_CARD))
				.ratchet();
	}
}
