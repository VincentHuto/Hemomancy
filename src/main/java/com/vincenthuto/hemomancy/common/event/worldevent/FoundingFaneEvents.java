package com.vincenthuto.hemomancy.common.event.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFaneBoundaries;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteThreatRules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

/**
 * Applies passive buffs to Harbingers standing inside any active Founding Fane.
 * Fane locations are persisted in {@link FoundingFaneSavedData}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class FoundingFaneEvents {

	private static final int EFFECT_INTERVAL_TICKS = 40;
	private static final int EFFECT_DURATION = 60;
	private static final double FANE_SYNC_DISTANCE = 256.0D;
	private static final Map<UUID, FaneBoundaryRelation> FANE_BOUNDARY_PREVIEWS = new HashMap<>();

	/** Damage dealt to non-Harbinger mobs that breach a fane boundary during a Blood Moon. */
	private static final float FANE_BARRIER_DAMAGE = 4.0f;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		if (sLevel.getGameTime() % EFFECT_INTERVAL_TICKS != 0) return;

		FoundingFaneSavedData data = FoundingFaneSavedData.get(sLevel);
		Map<UUID, FaneFootprint> fanes = data.getAllFootprints();
		if (fanes.isEmpty()) {
			for (ServerPlayer player : sLevel.getPlayers(p -> p.isAlive())) {
				PacketHandler.sendToPlayer(player, new PacketSyncFaneBoundaries(List.of()));
			}
			return;
		}

		boolean bloodMoonActive = BloodMoonEvents.isBloodMoonActive(sLevel);
		BloodlineSavedData bloodlineData = BloodlineSavedData.get(sLevel.getServer().overworld());

		for (ServerPlayer player : sLevel.getPlayers(p -> p.isAlive())) {
			int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
			boolean unstained = hasBegunUnstainedPath(player);
			syncFaneBoundaries(player, fanes, bloodlineData, degree, unstained);
			if (degree < 1) continue;

			for (Map.Entry<UUID, FaneFootprint> fane : fanes.entrySet()) {
				if (fane.getValue().contains(player.blockPosition())) {
					applyBuffs(player, data.effectStrength(fane.getKey(), player.blockPosition()));
					break;
				}
			}
		}

		// Blood Moon sealing: hostile mobs that enter a fane boundary take damage
		// and are knocked back. The consecrated ground actively repels the blood-tide.
		if (bloodMoonActive) {
			for (FaneFootprint footprint : fanes.values()) {
				BlockPos center = footprint.heart();
				if (center == null) continue;
				double radiusSq = FoundingFaneSavedData.FANE_RADIUS * FoundingFaneSavedData.FANE_RADIUS;
				net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(center)
						.inflate(FoundingFaneSavedData.FANE_RADIUS + FaneFootprint.STAKE_RADIUS + 64,
								FoundingFaneSavedData.FANE_RADIUS + 2,
								FoundingFaneSavedData.FANE_RADIUS + FaneFootprint.STAKE_RADIUS + 64);
				for (LivingEntity mob : sLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
						e -> e instanceof net.minecraft.world.entity.monster.Monster
								&& !(e instanceof ServerPlayer)
								&& !CardinalRiteThreatRules.isProtectedFromPassiveRiteDamage(
										e.getPersistentData().getBoolean(
												CardinalRiteThreatRules.RITE_BOUND_TAG)))) {
					double dx = mob.getX() - (center.getX() + 0.5);
					double dz = mob.getZ() - (center.getZ() + 0.5);
					if (footprint.contains(mob.blockPosition())) {
						mob.hurt(sLevel.damageSources().magic(), FANE_BARRIER_DAMAGE);
						double dist = Math.sqrt(dx * dx + dz * dz);
						if (dist < 0.1) { dx = 1.0; dz = 0.0; dist = 1.0; }
						mob.setDeltaMovement(dx / dist * 0.8, 0.3, dz / dist * 0.8);
						mob.hurtMarked = true;
					}
				}
			}
		}
	}

	private static void applyBuffs(ServerPlayer player, double strength) {
		int amplifier = strength >= 0.78D ? 1 : 0;
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_DURATION, amplifier, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, amplifier, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 0, true, false, true));
	}

	private static void syncFaneBoundaries(ServerPlayer viewer, Map<UUID, FaneFootprint> fanes,
			BloodlineSavedData bloodlineData, int viewerDegree, boolean viewerHasBegunUnstainedPath) {
		List<PacketSyncFaneBoundaries.Entry> entries = new ArrayList<>();
		double syncDistanceSq = (FANE_SYNC_DISTANCE + FoundingFaneSavedData.FANE_RADIUS)
				* (FANE_SYNC_DISTANCE + FoundingFaneSavedData.FANE_RADIUS);

		for (Map.Entry<UUID, FaneFootprint> fane : fanes.entrySet()) {
			BlockPos center = fane.getValue().heart();
			if (center == null) continue;
			double dx = viewer.getX() - (center.getX() + 0.5D);
			double dz = viewer.getZ() - (center.getZ() + 0.5D);
			if (dx * dx + dz * dz > syncDistanceSq) {
				continue;
			}

			boolean member = isViewerInOwnerBloodline(viewer.getUUID(), fane.getKey(), bloodlineData);
			FaneBoundaryRelation relation = previewRelation(viewer).orElseGet(() ->
					FaneBoundaryVisibilityRules.classifyViewer(member, viewerDegree, viewerHasBegunUnstainedPath));
			entries.add(new PacketSyncFaneBoundaries.Entry(center, fane.getValue().stakes(),
					(float) FaneFootprint.STAKE_RADIUS, fane.getKey(), relation));
		}

		PacketHandler.sendToPlayer(viewer, new PacketSyncFaneBoundaries(entries));
	}

	private static boolean hasBegunUnstainedPath(ServerPlayer viewer) {
		return HemoCapabilityAccess.getUnstainedProgress(viewer)
				.map(progress -> progress.hasBegunPurification() || progress.hasClarityUnlocked())
				.orElse(false);
	}

	private static boolean isViewerInOwnerBloodline(UUID viewerUuid, UUID ownerUuid, BloodlineSavedData bloodlineData) {
		if (viewerUuid.equals(ownerUuid)) {
			return true;
		}
		Bloodline ownerLine = bloodlineData.getBloodlineForPlayer(ownerUuid);
		return ownerLine != null && ownerLine.hasMember(viewerUuid);
	}

	public static void setPreviewRelation(ServerPlayer viewer, FaneBoundaryRelation relation) {
		FANE_BOUNDARY_PREVIEWS.put(viewer.getUUID(), relation);
	}

	public static void clearPreviewRelation(ServerPlayer viewer) {
		FANE_BOUNDARY_PREVIEWS.remove(viewer.getUUID());
	}

	public static Optional<FaneBoundaryRelation> previewRelation(ServerPlayer viewer) {
		return Optional.ofNullable(FANE_BOUNDARY_PREVIEWS.get(viewer.getUUID()));
	}
}
