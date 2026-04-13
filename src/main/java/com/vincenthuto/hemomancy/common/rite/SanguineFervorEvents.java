package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles tick-based cleanup and spawn-rate boosting for active Sanguine Fervor
 * zones established by the Rite of Sanguine Fervor.
 * <p>
 * When a fervor zone is active the natural mob-cap check is bypassed for any
 * mob that spawns within the zone's chunk radius, effectively increasing local
 * mob density for the duration of the rite.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SanguineFervorEvents {

	/** How often (in ticks) the cleanup pass runs to remove expired entries. */
	private static final int CLEANUP_INTERVAL_TICKS = 100;

	// ── Level tick: clean up expired fervor zones ──────────────────────────────

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		// Run cleanup only from the overworld to avoid duplicate passes
		if (sLevel != sLevel.getServer().overworld()) return;
		if (sLevel.getGameTime() % CLEANUP_INTERVAL_TICKS != 0) return;

		SanguineFervorSavedData data = SanguineFervorSavedData.get(sLevel);
		data.removeExpired(sLevel.getGameTime());
	}

	// ── Spawn check: force-allow spawns inside active fervor zones ─────────────

	@SubscribeEvent
	public static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
		// Skip spawner-block spawns — only boost naturally-spawned mobs
		if (event.getSpawnType() == MobSpawnType.SPAWNER) return;

		net.minecraft.world.level.ServerLevelAccessor levelAccessor = event.getLevel();
		ServerLevel sLevel = levelAccessor.getLevel();

		ServerLevel overworld = sLevel.getServer().overworld();
		SanguineFervorSavedData data = SanguineFervorSavedData.get(overworld);

		// If the data has no entries at all, skip the check for performance
		if (data.getEntries().isEmpty()) return;

		BlockPos spawnPos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
		String dimension = sLevel.dimension().location().toString();
		long currentTick = sLevel.getGameTime();

		if (data.isInFervorRange(spawnPos, dimension, currentTick)) {
			// Force the spawn through even if the mob cap has been reached.
			// This is the Forge-recommended way to bypass the cap for a specific position.
			event.setResult(Event.Result.ALLOW);
		}
	}
}
