package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Handles tick-based effects for Qliphoth Blooms established by the Bloom of
 * the Qliphoth rite. Players within the bloom's radius receive:
 * <ul>
 *   <li>Regeneration I effect (health regen)</li>
 *   <li>Enhanced blood regeneration (+5 blood/tick)</li>
 * </ul>
 * Manipulation cost reduction is handled directly in
 * {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation#performAction}.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QliphothBloomEvents {

	/** Interval in ticks between effect application (40 ticks = 2 seconds). */
	private static final int EFFECT_INTERVAL = 40;
	/** Duration of the Regeneration effect in ticks (slightly longer than interval for seamless coverage). */
	private static final int REGEN_DURATION = 50;
	/** Extra blood regeneration per tick when within a bloom's radius. */
	private static final double BLOOD_REGEN_PER_TICK = 5.0;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		// Only process every EFFECT_INTERVAL ticks for performance
		if (sLevel.getGameTime() % EFFECT_INTERVAL != 0) return;

		QliphothBloomSavedData data = QliphothBloomSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();

		for (QliphothBloomSavedData.BloomEntry bloom : data.getBlooms()) {
			if (!bloom.dimension().equals(dimension)) continue;

			int blockRadius = bloom.chunkRadius() * 16;
			AABB bloomBounds = new AABB(bloom.center()).inflate(blockRadius, 64, blockRadius);

			List<ServerPlayer> players = sLevel.getEntitiesOfClass(ServerPlayer.class, bloomBounds,
					player -> player.isAlive());

			for (ServerPlayer player : players) {
				// Grant Regeneration I for seamless health regen
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
						REGEN_DURATION, 0, true, false, true));

				// Grant enhanced blood regeneration
				player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
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
	 * Checks if the given player is within any Qliphoth Bloom's radius.
	 * Used by manipulation cost reduction logic in BloodManipulation.
	 */
	public static boolean isInQliphothBloom(ServerPlayer player) {
		ServerLevel overworld = player.server.overworld();
		QliphothBloomSavedData data = QliphothBloomSavedData.get(overworld);
		String dimension = player.level().dimension().location().toString();
		return data.isInBloomRange(player.blockPosition(), dimension);
	}
}
