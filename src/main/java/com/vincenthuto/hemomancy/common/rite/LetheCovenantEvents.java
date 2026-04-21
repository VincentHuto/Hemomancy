package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressProvider;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

/**
 * Server-side event handler for Lethe Covenant domains established by the
 * Rite of the Lethe Covenant.
 * <p>
 * Three concurrent effects while a domain is active:
 * <ol>
 *   <li><b>Spawn suppression</b>: natural mob spawns inside the domain are denied
 *       at a 50% rate, countering Sanguine Fervor.</li>
 *   <li><b>Bleed immunity</b>: players inside the domain who have Silver Ward
 *       active take zero magic damage.</li>
 *   <li><b>Purity growth</b>: every minute, Unstained players inside the domain
 *       gain +0.2 purity.</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class LetheCovenantEvents {

	/** How often the cleanup and purity-tick run (in ticks). 1200 = 1 minute. */
	private static final int PURITY_TICK_INTERVAL = 1200;

	/** Purity awarded per minute to Unstained players in the domain. */
	private static final float PURITY_PER_MINUTE = 0.2f;

	/** How often (in ticks) the cleanup pass runs. */
	private static final int CLEANUP_INTERVAL = 100;

	// ── Level tick: cleanup + purity growth ──────────────────────────────────

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		if (sLevel != sLevel.getServer().overworld()) return;

		long tick = sLevel.getGameTime();

		if (tick % CLEANUP_INTERVAL == 0) {
			LetheCovenantSavedData.get(sLevel).removeExpired(tick);
		}

		if (tick % PURITY_TICK_INTERVAL != 0) return;

		LetheCovenantSavedData data = LetheCovenantSavedData.get(sLevel);
		if (data.getEntries().isEmpty()) return;

		String dimension = sLevel.dimension().location().toString();

		for (ServerPlayer player : sLevel.getServer().getPlayerList().getPlayers()) {
			if (!player.level().equals(sLevel)) continue;
			BlockPos pos = player.blockPosition();

			if (!data.isInDomain(pos, dimension, tick)) continue;

			player.getCapability(UnstainedProgressProvider.UNSTAINED_CAPA).ifPresent(progress -> {
				if (!progress.hasBegunPurification() || progress.isPurified()) return;
				progress.addPurity(PURITY_PER_MINUTE);
				UnstainedProgressEvents.syncProgress(player, progress);
			});
		}
	}

	// ── Spawn check: 50% suppression inside covenant domains ─────────────────

	@SubscribeEvent
	public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
		// Only suppress natural spawns
		if (event.getSpawnType() == MobSpawnType.SPAWNER) return;

		net.minecraft.world.level.ServerLevelAccessor levelAccessor = event.getLevel();
		ServerLevel sLevel = levelAccessor.getLevel();
		ServerLevel overworld = sLevel.getServer().overworld();

		LetheCovenantSavedData data = LetheCovenantSavedData.get(overworld);
		if (data.getEntries().isEmpty()) return;

		BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
		String dimension = sLevel.dimension().location().toString();
		long tick = sLevel.getGameTime();

		if (data.isInDomain(spawnPos, dimension, tick)) {
			// 50% chance to block the spawn
			if (sLevel.getRandom().nextBoolean()) {
				event.setResult(Event.Result.DENY);
			}
		}
	}

	// ── Hurt event: bleed immunity for Silver Ward players in the domain ──────

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;
		if (!event.getSource().is(DamageTypes.MAGIC)) return;

		// Only protect players who have Silver Ward active
		if (!player.hasEffect(EffectInit.silver_ward.get())) return;

		ServerLevel sLevel = (ServerLevel) player.level();
		ServerLevel overworld = sLevel.getServer().overworld();
		LetheCovenantSavedData data = LetheCovenantSavedData.get(overworld);
		if (data.getEntries().isEmpty()) return;

		BlockPos pos = player.blockPosition();
		String dimension = sLevel.dimension().location().toString();
		long tick = sLevel.getGameTime();

		if (data.isInDomain(pos, dimension, tick)) {
			// Cancel all magic (bleed) damage — Silver Ward is absolute inside the Covenant
			event.setCanceled(true);
		}
	}
}
