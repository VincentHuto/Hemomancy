package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Applies passive buffs to Harbingers standing inside any active Founding Sanctum.
 * Sanctum locations are persisted in {@link FoundingSanctumSavedData}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class FoundingSanctumEvents {

	private static final int EFFECT_INTERVAL_TICKS = 40;
	private static final int EFFECT_DURATION = 60;

	/** Damage dealt to non-Harbinger mobs that breach a sanctum boundary during a Blood Moon. */
	private static final float SANCTUM_BARRIER_DAMAGE = 4.0f;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		if (sLevel.getGameTime() % EFFECT_INTERVAL_TICKS != 0) return;

		FoundingSanctumSavedData data = FoundingSanctumSavedData.get(sLevel);
		Map<UUID, BlockPos> sanctums = data.getAllSanctums();
		if (sanctums.isEmpty()) return;

		boolean bloodMoonActive = BloodMoonEvents.isBloodMoonActive(sLevel);

		for (ServerPlayer player : sLevel.getPlayers(p -> p.isAlive())) {
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			if (degree < 1) continue;

			for (BlockPos center : sanctums.values()) {
				if (isInSanctum(player, center)) {
					applyBuffs(player);
					break;
				}
			}
		}

		// Blood Moon sealing: hostile mobs that enter a sanctum boundary take damage
		// and are knocked back. The consecrated ground actively repels the blood-tide.
		if (bloodMoonActive) {
			for (BlockPos center : sanctums.values()) {
				double radiusSq = FoundingSanctumSavedData.SANCTUM_RADIUS
						* FoundingSanctumSavedData.SANCTUM_RADIUS;
				net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(center)
						.inflate(FoundingSanctumSavedData.SANCTUM_RADIUS + 2,
								FoundingSanctumSavedData.SANCTUM_RADIUS + 2,
								FoundingSanctumSavedData.SANCTUM_RADIUS + 2);
				for (LivingEntity mob : sLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
						e -> e instanceof net.minecraft.world.entity.monster.Monster
								&& !(e instanceof ServerPlayer))) {
					double dx = mob.getX() - (center.getX() + 0.5);
					double dz = mob.getZ() - (center.getZ() + 0.5);
					if (dx * dx + dz * dz <= radiusSq) {
						mob.hurt(sLevel.damageSources().magic(), SANCTUM_BARRIER_DAMAGE);
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist < 0.1) { dx = 1.0; dz = 0.0; dist = 1.0; }
						mob.setDeltaMovement(dx / dist * 0.8, 0.3, dz / dist * 0.8);
						mob.hurtMarked = true;
					}
				}
			}
		}
	}

	private static boolean isInSanctum(ServerPlayer player, BlockPos center) {
		double dx = player.getX() - (center.getX() + 0.5);
		double dz = player.getZ() - (center.getZ() + 0.5);
		return (dx * dx + dz * dz) <= FoundingSanctumSavedData.SANCTUM_RADIUS * FoundingSanctumSavedData.SANCTUM_RADIUS;
	}

	private static void applyBuffs(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_DURATION, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 0, true, false, true));
	}
}
