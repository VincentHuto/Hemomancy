package com.vincenthuto.hemomancy.common.entity.npc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

/**
 * O-3: a quest NPC that ordinary damage cannot hurt. Only {@code /kill} and a Creative-mode player get through;
 * an NPC with further exceptions (a Vicar in a fight, a Scout whose dialogue cleared its invulnerability) adds
 * them beside this check in its own {@code hurt}.
 */
public interface ProtectedNpc {
	default boolean admitsDamage(DamageSource source) {
		return isKillCommand(source) || source.getEntity() instanceof Player player && player.isCreative();
	}

	/** {@code /kill}: the administrative removal every protected NPC admits. */
	static boolean isKillCommand(DamageSource source) {
		return source.is(DamageTypes.GENERIC_KILL);
	}
}
