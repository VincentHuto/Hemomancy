package com.vincenthuto.hemomancy.common.manipulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class ManipulationCombatHelper {
	private ManipulationCombatHelper() {
	}

	public static List<LivingEntity> hostileTargets(Player player, Level world, double range) {
		return world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(range),
				target -> target != player && target.isAlive() && !player.isAlliedTo(target));
	}

	public static LivingEntity aimedTarget(Player player, Level world, double range, double dot) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		return hostileTargets(player, world, range).stream().filter(target -> {
			Vec3 delta = target.getEyePosition().subtract(eye);
			return delta.lengthSqr() > 0.001D && look.dot(delta.normalize()) >= dot && player.hasLineOfSight(target);
		}).min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
	}

	public static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
		Vec3 line = end.subtract(start);
		double t = Math.max(0, Math.min(1, point.subtract(start).dot(line) / line.lengthSqr()));
		return point.distanceTo(start.add(line.scale(t)));
	}

	public static boolean hurt(BloodManipulation manipulation, Player player, LivingEntity target,
			ServerLevel level, float amount) {
		float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, manipulation, amount);
		return target.hurt(level.damageSources().magic(), adjusted);
	}
}
