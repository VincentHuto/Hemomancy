package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kinbud's Second Head: a polyp head that buds from the host's shoulder for {@link KinbudRules#HEAD_TICKS} and
 * strikes the nearest hostile within {@link KinbudRules#HEAD_RANGE} once a second. It is the host's body, not a
 * summon: no summon registry, no command, no Puppet Interference. It strikes as the host's own body would, so its
 * targets go through {@link ManipulationCombatHelper#canHarmFor} with the host as the source. Its visible state rides
 * {@link MorphlingActiveState#SECOND_HEAD} (and {@link MorphlingActiveState#SECOND_HEAD_TWIN} for a Kindled pair).
 * Session state.
 */
public final class KinbudSecondHead {
	private static final Map<UUID, Bud> BUDS = new ConcurrentHashMap<>();

	private static final class Bud {
		final long until;
		final int heads;
		long nextStrike;

		Bud(long until, int heads, long nextStrike) {
			this.until = until;
			this.heads = heads;
			this.nextStrike = nextStrike;
		}
	}

	private KinbudSecondHead() {
	}

	public static void bud(Player host, int heads, int ticks) {
		long now = host.level().getGameTime();
		BUDS.put(host.getUUID(), new Bud(now + ticks, Math.max(1, heads), now));
	}

	/** Heads budded right now: 0, 1 or 2. */
	public static int heads(Player host) {
		Bud bud = BUDS.get(host.getUUID());
		if (bud == null) return 0;
		if (host.level().getGameTime() >= bud.until) {
			BUDS.remove(host.getUUID());
			return 0;
		}
		return bud.heads;
	}

	/**
	 * Strikes once a second while budded: each head hits the nearest hostile it may harm for
	 * {@link KinbudRules#HEAD_DAMAGE} and stings it. Returns the number of strikes landed.
	 */
	public static int tick(Player host, int stingTicks, int stingAmplifier) {
		int heads = heads(host);
		Bud bud = BUDS.get(host.getUUID());
		long now = host.level().getGameTime();
		if (heads == 0 || bud == null || now < bud.nextStrike) return 0;
		bud.nextStrike = now + KinbudRules.HEAD_STRIKE_INTERVAL;
		var targets = host.level().getEntitiesOfClass(LivingEntity.class,
						host.getBoundingBox().inflate(KinbudRules.HEAD_RANGE),
						target -> ManipulationCombatHelper.canHarmFor(host, target)
								&& MorphlingAllies.isHostile(host, target)
								&& target.distanceToSqr(host) <= KinbudRules.HEAD_RANGE * KinbudRules.HEAD_RANGE)
				.stream().sorted(Comparator.comparingDouble(target -> target.distanceToSqr(host))).toList();
		int landed = 0;
		for (int i = 0; i < heads && !targets.isEmpty(); i++) {
			LivingEntity target = targets.get(Math.min(i, targets.size() - 1));
			target.invulnerableTime = 0;
			if (target.hurt(host.damageSources().mobAttack(host), KinbudRules.HEAD_DAMAGE)) {
				landed++;
				if (stingTicks > 0) {
					target.addEffect(new MobEffectInstance(EffectInit.stung, stingTicks, stingAmplifier, false, true, true),
							host);
				}
			}
		}
		return landed;
	}

	public static void clear(UUID host) {
		BUDS.remove(host);
	}

	public static void clearSessionState() {
		BUDS.clear();
	}
}
