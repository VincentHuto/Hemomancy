package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.harbinger.SanguineFervorSavedData;
import com.vincenthuto.hemomancy.common.rite.unstained.LetheCovenantSavedData;
import com.vincenthuto.hemomancy.common.rite.unstained.PaleConsecrationSavedData;
import com.vincenthuto.hemomancy.common.rite.unstained.StillWatersSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Shared upkeep for every timed rite zone: expiry, client sync and the spawn
 * precedence between Lethe Covenant and Sanguine Fervor. Zone-specific
 * effects stay in each zone's own events class.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class RiteZoneEvents {
	private static final int EXPIRY_INTERVAL_TICKS = 100;
	private static final int SYNC_INTERVAL_TICKS = 20;

	private RiteZoneEvents() {}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		// Every zone is stored in the Overworld, so one level runs the shared upkeep.
		if (level != level.getServer().overworld()) return;
		long gameTime = level.getGameTime();
		if (gameTime % EXPIRY_INTERVAL_TICKS == 0) {
			StillWatersSavedData.get(level).removeExpired(gameTime);
			PaleConsecrationSavedData.get(level).removeExpired(gameTime);
			LetheCovenantSavedData.get(level).removeExpired(gameTime);
			SanguineFervorSavedData.get(level).removeExpired(gameTime);
		}
		if (gameTime % SYNC_INTERVAL_TICKS == 0 && RiteZoneSync.consumeChanged()) {
			RiteZoneSync.broadcast(level.getServer());
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			RiteZoneSync.sendTo(player, player.level().dimension());
		}
	}

	@SubscribeEvent
	public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			RiteZoneSync.sendTo(player, event.getTo());
		}
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			RiteZoneSync.sendTo(player, player.level().dimension());
		}
	}

	/** Natural spawns only; the precedence lives in {@link RiteZoneSpawnRules}. */
	@SubscribeEvent
	public static void onSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
		if (event.getSpawnType() == MobSpawnType.SPAWNER) return;
		ServerLevel level = event.getLevel().getLevel();
		ServerLevel overworld = level.getServer().overworld();
		LetheCovenantSavedData lethe = LetheCovenantSavedData.get(overworld);
		SanguineFervorSavedData fervor = SanguineFervorSavedData.get(overworld);
		if (lethe.zones().isEmpty() && fervor.zones().isEmpty()) return;

		BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
		long gameTime = level.getGameTime();
		boolean inLethe = lethe.contains(pos, level.dimension(), gameTime);
		boolean inFervor = !inLethe && fervor.contains(pos, level.dimension(), gameTime);
		switch (RiteZoneSpawnRules.resolve(inLethe, inFervor, inLethe && level.getRandom().nextBoolean())) {
			case DENY -> event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
			case FORCE -> event.setResult(MobSpawnEvent.PositionCheck.Result.SUCCEED);
			case DEFAULT -> {
			}
		}
	}
}
