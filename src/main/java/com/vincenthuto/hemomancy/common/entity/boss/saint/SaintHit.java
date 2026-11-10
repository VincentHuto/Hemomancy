package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.damage.SchoolHitContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * One hit on a kit Saint, as its damage pipeline classified it: who struck (a player, or null), the source and the
 * amount asked, how it arrived, the school of a cast, whether it was fire, and the staged school context.
 */
public record SaintHit(@Nullable Player attacker, DamageSource source, float amount, SaintOpening.Delivery delivery,
		@Nullable EnumBloodTemper school, boolean fire, @Nullable SchoolHitContext context) {
}
