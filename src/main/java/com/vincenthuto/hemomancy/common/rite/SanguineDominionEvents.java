package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Handles tick-based effects for Blood Domains established by the Rite of
 * Sanguine Dominion. Non-allied entities within the domain take slow bleed
 * damage every 2 seconds.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SanguineDominionEvents {

	/** Damage dealt to non-allied entities every BLEED_INTERVAL ticks. */
	private static final float BLEED_DAMAGE = 0.5f;
	/** Interval in ticks between bleed damage ticks (40 ticks = 2 seconds). */
	private static final int BLEED_INTERVAL = 40;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;

		// Only process every BLEED_INTERVAL ticks for performance
		if (sLevel.getGameTime() % BLEED_INTERVAL != 0) return;

		SanguineDominionSavedData data = SanguineDominionSavedData.get(sLevel.getServer().overworld());
		String dimension = sLevel.dimension().location().toString();

		for (SanguineDominionSavedData.DominionEntry dom : data.getDominions()) {
			if (!dom.dimension().equals(dimension)) continue;

			int blockRadius = dom.chunkRadius() * 16;
			AABB domainBounds = new AABB(dom.center()).inflate(blockRadius, 64, blockRadius);

			// Get the owner's bloodline for alliance checks
			Bloodline ownerBloodline = null;
			BloodlineSavedData bloodlineData = BloodlineSavedData.get(sLevel.getServer().overworld());
			Bloodline foundLine = bloodlineData.getBloodlineForPlayer(dom.ownerUUID());
			if (foundLine != null && foundLine.isValid()) {
				ownerBloodline = foundLine;
			}

			List<LivingEntity> entities = sLevel.getEntitiesOfClass(LivingEntity.class, domainBounds,
					entity -> entity.isAlive() && !HemoEntityPredicates.NOBLOOD.test(entity));

			for (LivingEntity entity : entities) {
				// Skip the domain owner
				if (entity instanceof Player player && player.getUUID().equals(dom.ownerUUID())) {
					continue;
				}

				// Skip bloodline allies
				if (ownerBloodline != null && entity instanceof Player player
						&& ownerBloodline.hasMember(player.getUUID())) {
					continue;
				}

				// Apply slow bleed damage
				if (entity instanceof ServerPlayer targetPlayer) {
					entity.hurt(targetPlayer.damageSources().magic(), BLEED_DAMAGE);
				} else {
					entity.hurt(sLevel.damageSources().magic(), BLEED_DAMAGE);
				}
			}
		}
	}

	/**
	 * Checks if the given player owns the Blood Domain at their current position.
	 * Used by manipulation cost reduction logic.
	 */
	public static boolean isInOwnDomain(ServerPlayer player) {
		ServerLevel overworld = player.server.overworld();
		SanguineDominionSavedData data = SanguineDominionSavedData.get(overworld);
		String dimension = player.level().dimension().location().toString();
		return data.isOwnerAt(player.getUUID(), player.blockPosition(), dimension);
	}
}
