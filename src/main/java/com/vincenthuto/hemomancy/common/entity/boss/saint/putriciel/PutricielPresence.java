package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Putriciel's presence (design section 5). In the rot it colonises: every 20 ticks each challenger within 7, 8 or 9
 * blocks (by rot phase) gains 1, 1.5 or 2; spores drift round pillars, so it needs no sight. In the fire the purging
 * heat replaces it: within 4 blocks, every 10 ticks, colonisation -2, and 1 cleansing damage every 20. Nothing during
 * the Embering's slump or before the Ignition commits.
 */
public final class PutricielPresence implements SaintPresence {
	public static final int ROT_CADENCE = 20;

	@Override
	public double radius(SaintTrialEntity saint) {
		return saint instanceof PutricielEntity putriciel ? putriciel.presenceRadius() : 0.0D;
	}

	@Override
	public int cadence(SaintTrialEntity saint) {
		return saint instanceof PutricielEntity putriciel && putriciel.stage().fire()
				? PutricielColonisationRules.HEAT_EVERY : ROT_CADENCE;
	}

	@Override
	public boolean needsSight(SaintTrialEntity saint) {
		return false;
	}

	@Override
	public void pulse(SaintTrialEntity saint, Player player, float strength) {
		if (saint instanceof PutricielEntity putriciel) putriciel.presencePulse(player);
	}
}
