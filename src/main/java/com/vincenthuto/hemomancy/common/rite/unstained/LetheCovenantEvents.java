package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Lethe Covenant domains from the Rites of the Lethe Covenant and the Lethean Font.
 * <ol>
 *   <li><b>Purity growth</b>: every minute, Unstained players inside a domain
 *       gain +0.2 purity.</li>
 *   <li><b>Bleed immunity</b>: players inside a domain who have Silver Ward
 *       active take zero magic damage.</li>
 *   <li><b>Spawn suppression</b>: half of natural spawns are denied, and
 *       Sanguine Fervor never forces spawns inside a domain. The rule lives in
 *       {@link com.vincenthuto.hemomancy.common.rite.zone.RiteZoneSpawnRules}.</li>
 * </ol>
 * Each level processes the domains cast in its own dimension.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class LetheCovenantEvents {

	/** Purity interval in ticks. 1200 = 1 minute. */
	private static final int PURITY_TICK_INTERVAL = 1200;

	/** Purity awarded per minute to Unstained players in the domain. */
	private static final float PURITY_PER_MINUTE = 0.2f;

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		long tick = sLevel.getGameTime();
		if (tick % PURITY_TICK_INTERVAL != 0) return;

		LetheCovenantSavedData data = LetheCovenantSavedData.get(sLevel.getServer().overworld());
		if (data.zones().isEmpty()) return;

		for (ServerPlayer player : sLevel.players()) {
			if (!data.contains(player.blockPosition(), sLevel.dimension(), tick)) continue;

			var progress = HemoCapabilityAccess.getUnstainedProgress(player);
			if (!progress.hasBegunPurification() || progress.isPurified()) continue;
			progress.addPurity(PURITY_PER_MINUTE);
			UnstainedProgressEvents.syncProgress(player, progress);
		}
	}

	@SubscribeEvent
	public static void onLivingHurt(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (!(player.level() instanceof ServerLevel sLevel)) return;
		if (!event.getSource().is(DamageTypes.MAGIC)) return;
		if (!player.hasEffect(EffectInit.silver_ward)) return;

		LetheCovenantSavedData data = LetheCovenantSavedData.get(sLevel.getServer().overworld());
		if (data.contains(player.blockPosition(), sLevel.dimension(), sLevel.getGameTime())) {
			// Silver Ward is absolute inside the Covenant: all magic (bleed) damage is cancelled.
			event.setCanceled(true);
		}
	}
}
