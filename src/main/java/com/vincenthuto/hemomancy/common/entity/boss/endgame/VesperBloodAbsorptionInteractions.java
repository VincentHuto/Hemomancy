package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Comparator;

/** Routes the ordinary Blood Absorption channel into Vesper's required final defeat. */
public final class VesperBloodAbsorptionInteractions {
	private VesperBloodAbsorptionInteractions() {
	}

	public static double tryAbsorb(Level level, LivingEntity user, double range, float progressPerTick) {
		if (level.isClientSide || !(user instanceof ServerPlayer player)) {
			return 0.0D;
		}
		return level.getEntitiesOfClass(VesperTheEveningStarEntity.class,
				player.getBoundingBox().inflate(range), vesper -> vesper.canBeAbsorbedBy(player))
				.stream()
				.min(Comparator.comparingDouble(player::distanceToSqr))
				.map(vesper -> (double) vesper.absorbWithBlood(player, progressPerTick))
				.orElse(0.0D);
	}
}
