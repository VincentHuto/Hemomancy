package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper.Reach;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;

/**
 * Who a Morphling's group powers reach. A "bloodline ally or tamed companion" is anything
 * {@link ManipulationCombatHelper#allied} counts for the host (teammates, the host's pets and puppets, mobs it has
 * impressed) other than the host itself; hostiles are what the host may harm and that is hunting or monstrous.
 */
public final class MorphlingAllies {
	private MorphlingAllies() {
	}

	public static boolean isAlly(Player host, LivingEntity other) {
		return other != host && other.isAlive() && !other.isSpectator()
				&& ManipulationCombatHelper.allied(host, other);
	}

	public static List<LivingEntity> allies(Player host, double range) {
		AABB area = host.getBoundingBox().inflate(range);
		return host.level().getEntitiesOfClass(LivingEntity.class, area,
				other -> isAlly(host, other) && other.distanceToSqr(host) <= range * range);
	}

	/** Hostiles the host may harm within range of a point's box. */
	public static List<LivingEntity> hostiles(Player host, AABB area) {
		return host.level().getEntitiesOfClass(LivingEntity.class, area,
				other -> ManipulationCombatHelper.isHostileTarget(host, other));
	}

	public static List<LivingEntity> hostiles(Player host, double range) {
		return hostiles(host, host.getBoundingBox().inflate(range)).stream()
				.filter(other -> other.distanceToSqr(host) <= range * range).toList();
	}

	public static LivingEntity nearestHostile(Player host, double range) {
		return hostiles(host, range).stream()
				.min(Comparator.comparingDouble(other -> other.distanceToSqr(host))).orElse(null);
	}

	/** True for monsters and anything hunting the host: the hostiles a power aimed at "hostiles" means. */
	public static boolean isHostile(Player host, LivingEntity other) {
		return ManipulationCombatHelper.canHarm(host, other, Reach.STRICT)
				&& (other instanceof Enemy || other instanceof net.minecraft.world.entity.Mob mob && mob.getTarget() == host);
	}
}
