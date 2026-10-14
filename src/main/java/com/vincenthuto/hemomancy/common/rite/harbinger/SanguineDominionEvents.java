package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.entity.HemoEntityPredicates;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZone;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Blood Domains from the Rite of Sanguine Dominion. Non-allied entities within
 * a domain take slow bleed damage every 2 seconds, and the owner's
 * manipulations are discounted through {@link #isInOwnDomain}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class SanguineDominionEvents {

	/** Damage dealt to non-allied entities every BLEED_INTERVAL ticks. */
	private static final float BLEED_DAMAGE = 0.5f;
	/** Interval in ticks between bleed damage ticks (40 ticks = 2 seconds). */
	private static final int BLEED_INTERVAL = 40;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		if (sLevel.getGameTime() % BLEED_INTERVAL != 0) return;

		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineDominionSavedData data = SanguineDominionSavedData.get(overworld);
		if (data.zones().isEmpty()) return;

		for (RiteZone dom : data.activeIn(sLevel.dimension(), sLevel.getGameTime())) {
			int blockRadius = dom.blockRadius();
			AABB domainBounds = new AABB(dom.center()).inflate(blockRadius, 64, blockRadius);

			Bloodline foundLine = BloodlineSavedData.get(overworld).getBloodlineForPlayer(dom.owner());
			Bloodline ownerBloodline = foundLine != null && foundLine.isValid() ? foundLine : null;

			for (LivingEntity entity : sLevel.getEntitiesOfClass(LivingEntity.class, domainBounds,
					candidate -> candidate.isAlive()
							&& !HemoEntityPredicates.NOBLOOD.test(candidate)
							&& !CardinalRiteThreatRules.isProtectedFromPassiveRiteDamage(
									candidate.getPersistentData().getBoolean(
											CardinalRiteThreatRules.RITE_BOUND_TAG)))) {
				if (entity instanceof Player player && (player.getUUID().equals(dom.owner())
						|| ownerBloodline != null && ownerBloodline.hasMember(player.getUUID()))) {
					continue;
				}
				entity.hurt(sLevel.damageSources().magic(), BLEED_DAMAGE);
			}
		}
	}

	/** True when the player stands inside a Blood Domain they own. */
	public static boolean isInOwnDomain(ServerPlayer player) {
		SanguineDominionSavedData data = SanguineDominionSavedData.get(player.server.overworld());
		return !data.zones().isEmpty()
				&& data.isOwnerAt(player.getUUID(), player.blockPosition(), player.level().dimension());
	}
}
