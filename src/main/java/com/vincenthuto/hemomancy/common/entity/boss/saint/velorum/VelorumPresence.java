package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintPresence;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.player.Player;

/**
 * The chill (design section 5): 10 blocks round his feet, 4 up or down, in his sight. Every tick it multiplies the
 * stillness fill by 1.5 and the drain by 0.8 (through {@link VelorumStillness#presence}); every 20 ticks it refreshes
 * one level of the RIME school state for 40 ticks (never past two, {@link VelorumMoves#RIME_CAP}), so the existing
 * rime visuals show and Searing burns it off. The figures and the rank pillars break its sight: the congregation is
 * cover.
 */
public final class VelorumPresence implements SaintPresence {
	public static final double RADIUS = 10.0D;
	public static final double VERTICAL = 4.0D;
	public static final int CADENCE = 20;
	public static final int RIME_TICKS = 40;

	@Override
	public double radius(SaintTrialEntity saint) {
		return RADIUS;
	}

	@Override
	public double verticalReach(SaintTrialEntity saint) {
		return VERTICAL;
	}

	@Override
	public int cadence(SaintTrialEntity saint) {
		return CADENCE;
	}

	@Override
	public void everyTick(SaintTrialEntity saint, Player player, float strength) {
		if (saint instanceof VelorumEntity velorum) velorum.stillness().presence(player);
	}

	@Override
	public void pulse(SaintTrialEntity saint, Player player, float strength) {
		VelorumMoves.rime(saint, player, RIME_TICKS, 1);
	}
}
