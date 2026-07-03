package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public final class WillBloodUtilityInteractions {
	private static final double PROJECTION_RANGE = 5.5D;
	private static final float BLOOD_ABSORPTION_HEALTH_PER_TICK = 0.25F;

	private WillBloodUtilityInteractions() {
	}

	public static double tryAbsorbFalteringWill(Level level, LivingEntity user, double range) {
		if (level.isClientSide || !(user instanceof ServerPlayer player)) {
			return 0.0D;
		}
		return findNearestFalteringWill(user, range)
				.filter(WillEntity::canBloodUtilityBend)
				.map(will -> (double) will.absorbFalteringWithBlood(player, BLOOD_ABSORPTION_HEALTH_PER_TICK))
				.orElse(0.0D);
	}

	public static double tryProjectBanishFalteringWill(Level level, LivingEntity user, double bloodCost) {
		if (level.isClientSide || !(user instanceof ServerPlayer player)) {
			return 0.0D;
		}
		HitResult blockTrace = user.pick(PROJECTION_RANGE, 0.0F, true);
		double projectionRange = blockTrace.getType() == HitResult.Type.BLOCK
				? Math.sqrt(user.getEyePosition().distanceToSqr(blockTrace.getLocation()))
				: PROJECTION_RANGE;
		Optional<WillEntity> target = findLookedAtFalteringWill(level, user, projectionRange);
		if (target.isEmpty()) {
			return 0.0D;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		double cost = Math.max(1.0D, bloodCost);
		if (volume == null || !volume.isActive() || volume.isEmpty() || !volume.drain(cost)) {
			return 0.0D;
		}
		if (!target.get().projectBanishWithBlood(player)) {
			return 0.0D;
		}
		PacketHandler.sendToPlayer(player, new BloodVolumeServerPacket(volume));
		return cost;
	}

	private static Optional<WillEntity> findNearestFalteringWill(LivingEntity user, double range) {
		return user.level().getEntitiesOfClass(WillEntity.class, user.getBoundingBox().inflate(range),
						WillEntity::canBloodUtilityBend)
				.stream()
				.min(Comparator.comparingDouble(user::distanceToSqr));
	}

	private static Optional<WillEntity> findLookedAtFalteringWill(Level level, LivingEntity user, double range) {
		Vec3 eye = user.getEyePosition();
		Vec3 look = user.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(range));
		AABB searchBox = user.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, user, eye, end, searchBox,
				entity -> entity instanceof WillEntity will && will.canBloodUtilityBend());
		if (hit == null || !(hit.getEntity() instanceof WillEntity will)) {
			return Optional.empty();
		}
		return Optional.of(will);
	}
}
