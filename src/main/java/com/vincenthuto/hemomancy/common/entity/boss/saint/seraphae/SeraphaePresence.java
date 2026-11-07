package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Her presence over-heals (design section 5): 9 blocks from her, in sight, every 20 ticks (14 under No Rest Given).
 * The kit runs it over her challengers only; {@link SeraphaeEntity#presencePulse} does the healing and the growth.
 */
public final class SeraphaePresence implements SaintPresence {
	@Override
	public double radius(SaintTrialEntity saint) {
		return SeraphaeGrowthRules.RADIUS;
	}

	@Override
	public int cadence(SaintTrialEntity saint) {
		return SeraphaeGrowthRules.cadence(SeraphaeDeeds.noRest(saint.deeds().mask()));
	}

	@Override
	public void pulse(SaintTrialEntity saint, Player player, float strength) {
		if (saint instanceof SeraphaeEntity seraphae) seraphae.presencePulse(player);
	}
}
