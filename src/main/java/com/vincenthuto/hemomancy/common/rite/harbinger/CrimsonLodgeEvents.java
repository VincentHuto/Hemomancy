package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;

/**
 * Handles tick-based effects for Crimson Lodges established by the Rite of the
 * Crimson Lodge (Illuminatus degree rite). Players within a lodge's radius
 * receive:
 * <ul>
 *   <li>Strength I effect (melee damage bonus)</li>
 *   <li>Enhanced blood regeneration (+5 blood/tick)</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class CrimsonLodgeEvents {

	/** Interval in ticks between effect application (40 ticks = 2 seconds). */
	private static final int EFFECT_APPLICATION_INTERVAL_TICKS = 40;
	/** Duration of the Strength effect in ticks (slightly longer than interval for seamless coverage). */
	private static final int STRENGTH_DURATION = 50;
	/** Extra blood regeneration per tick when within a lodge's radius. */
	private static final double BLOOD_REGEN_PER_TICK = 5.0;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;

		// Only process every EFFECT_APPLICATION_INTERVAL_TICKS ticks for performance
		if (sLevel.getGameTime() % EFFECT_APPLICATION_INTERVAL_TICKS != 0) return;

		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();

		for (CrimsonLodgeSavedData.LodgeEntry lodge : data.getLodges()) {
			if (!lodge.dimension().equals(dimension)) continue;

			int blockRadius = lodge.chunkRadius() * 16;
			AABB lodgeBounds = new AABB(lodge.center()).inflate(blockRadius, 64, blockRadius);

			List<ServerPlayer> players = sLevel.getEntitiesOfClass(ServerPlayer.class, lodgeBounds,
					player -> player.isAlive());

			for (ServerPlayer player : players) {
				// Grant Strength I for melee damage bonus
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,
						STRENGTH_DURATION, 0, true, false, true));

				// Grant enhanced blood regeneration (blood virility)
				HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
					if (volume.isActive()) {
						double maxBlood = volume.getMaxBloodVolume();
						if (volume.getBloodVolume() < maxBlood) {
							volume.fill(BLOOD_REGEN_PER_TICK);
							BloodVolumeEvents.syncVolume(player, volume);
						}
					}
				});
			}
		}
	}

	/**
	 * Checks if the given player is within any Crimson Lodge's radius.
	 * Used for NPC summoning validation in the ancestral ledger.
	 */
	public static boolean isInCrimsonLodge(ServerPlayer player) {
		ServerLevel overworld = player.server.overworld();
		CrimsonLodgeSavedData data = CrimsonLodgeSavedData.get(overworld);
		String dimension = player.level().dimension().location().toString();
		return data.isInLodgeRange(player.blockPosition(), dimension);
	}
}
