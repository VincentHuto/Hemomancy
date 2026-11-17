package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import net.minecraft.world.entity.player.Player;

/**
 * Core loop Track 4: a GameTest caster that carries Temper. Casts and pulses then cost and hit as they did before
 * Temper existed, which is what the older balance pins measure. A test of dry casting leaves the pool empty instead.
 */
public final class TemperFixtures {
	private TemperFixtures() {
	}

	/** Fills every school to the largest cap, whatever the player's alignment; returns the player. */
	public static <P extends Player> P carry(P player) {
		var temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, TemperRules.MAX_CAP, TemperRules.MAX_CAP);
		return player;
	}
}
