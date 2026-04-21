package com.vincenthuto.hemomancy.common.worldevent;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketSyncBloodMoon;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/**
 * Handles the Blood Moon weather event.
 *
 * <p>A Blood Moon rarely triggers when a new night begins in the Overworld
 * (roughly a 1-in-7 chance each night). While active it:
 * <ul>
 *   <li>Spawns Thirsters and Fargones near players in the Overworld</li>
 *   <li>Grants Harbingers (degree &gt; 0) Strength II + Night Vision; non-Harbingers receive Weakness I</li>
 *   <li>Reduces blood-manipulation cost by 25% (handled in
 *       {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation})</li>
 * </ul>
 *
 * <p>State is persisted via {@link BloodMoonSavedData}; clients are notified
 * via {@link PacketSyncBloodMoon}.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class BloodMoonEvents {

	/** Ticks per in-game day. */
	private static final long DAY_LENGTH = 24000L;

	/** In-game tick at which night begins. */
	private static final long NIGHT_START_TICK = 12542L;

	/** Duration (ticks) of a blood moon — roughly one in-game night. */
	private static final long BLOOD_MOON_DURATION = 11900L;

	/**
	 * Chance a blood moon triggers each night: 1 in BLOOD_MOON_CHANCE.
	 * Lower values = more frequent. Default: 1-in-7 ≈ weekly at normal play speed.
	 */
	private static final int BLOOD_MOON_CHANCE = 7;

	/** How often (ticks) to attempt a mob spawn during a blood moon. */
	private static final int SPAWN_INTERVAL_TICKS = 120;

	/** Max number of blood-moon mobs per player before suppressing new spawns. */
	private static final int MAX_NEARBY_MOBS = 6;

	/** Block radius around a player to search for existing mobs. */
	private static final double MOB_SEARCH_RADIUS = 40.0;

	/** Block radius within which blood-moon mobs may spawn. */
	private static final int SPAWN_RADIUS = 24;

	/** Duration of ambient effects applied every 2 seconds (50 ticks + buffer). */
	private static final int EFFECT_DURATION = 60;

	/** How often (ticks) ambient effects are reapplied. */
	private static final int EFFECT_INTERVAL_TICKS = 40;

	// ---------------------------------------------------------------------------
	// Public query API (used by BloodManipulation)
	// ---------------------------------------------------------------------------

	/**
	 * Returns {@code true} if a Blood Moon is currently active in the given level.
	 * Safe to call server-side from any context that has access to the level.
	 */
	public static boolean isBloodMoonActive(Level level) {
		if (level.isClientSide()) {
			return BloodMoonClientState.isActive();
		}
		if (!(level instanceof ServerLevel sl)) return false;
		ServerLevel overworld = sl.getServer().overworld();
		return BloodMoonSavedData.get(overworld).isActive();
	}

	// ---------------------------------------------------------------------------
	// Player login sync
	// ---------------------------------------------------------------------------

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		ServerLevel overworld = player.server.overworld();
		boolean active = BloodMoonSavedData.get(overworld).isActive();
		PacketHandler.sendToPlayer(player, new PacketSyncBloodMoon(active));
	}

	// ---------------------------------------------------------------------------
	// Server tick handler
	// ---------------------------------------------------------------------------

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		if (!sLevel.dimension().equals(Level.OVERWORLD)) return;

		long gameTime = sLevel.getGameTime();
		long dayTime = sLevel.getDayTime() % DAY_LENGTH;

		BloodMoonSavedData data = BloodMoonSavedData.get(sLevel);

		// Check if a currently-active blood moon has expired naturally
		if (data.isActive() && gameTime >= data.getEndTick()) {
			endBloodMoon(sLevel, data, gameTime);
			return;
		}

		// Cancel if the time was manually set into daytime (e.g. /time set day).
		// Natural overnight expiry fires at dayTime ~442 via the check above,
		// so the 1000-tick threshold avoids cancelling that brief post-dawn window.
		if (data.isActive() && dayTime >= 1000L && dayTime < NIGHT_START_TICK) {
			endBloodMoon(sLevel, data, gameTime);
			return;
		}

		// Roll for blood moon at the start of each night (once per day)
		if (!data.isActive() && dayTime == NIGHT_START_TICK) {
			if (sLevel.random.nextInt(BLOOD_MOON_CHANCE) == 0) {
				startBloodMoon(sLevel, data, gameTime);
			}
		}

		if (!data.isActive()) return;

		// Apply ambient player effects every EFFECT_INTERVAL_TICKS
		// Harbingers (degree >= 1) are empowered; uninitiated players are weakened.
		if (gameTime % EFFECT_INTERVAL_TICKS == 0) {
			for (ServerPlayer player : sLevel.getPlayers(p -> p.isAlive())) {
				applyBloodMoonEffects(player);
			}
		}

		// Attempt to spawn blood-moon mobs
		if (gameTime % SPAWN_INTERVAL_TICKS == 0 && sLevel.getDifficulty() != Difficulty.PEACEFUL) {
			trySpawnBloodMoonMobs(sLevel);
		}
	}

	// ---------------------------------------------------------------------------
	// Blood moon lifecycle
	// ---------------------------------------------------------------------------

	private static void startBloodMoon(ServerLevel sLevel, BloodMoonSavedData data, long gameTime) {
		data.start(gameTime + BLOOD_MOON_DURATION);
		broadcastToPlayers(sLevel, Component.translatable("hemomancy.blood_moon.start")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
		syncToAllPlayers(sLevel, true);
	}

	private static void endBloodMoon(ServerLevel sLevel, BloodMoonSavedData data, long gameTime) {
		data.stop();
		broadcastToPlayers(sLevel, Component.translatable("hemomancy.blood_moon.end")
				.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		syncToAllPlayers(sLevel, false);
	}

	// ---------------------------------------------------------------------------
	// Mob spawning
	// ---------------------------------------------------------------------------

	private static void trySpawnBloodMoonMobs(ServerLevel sLevel) {
		List<ServerPlayer> players = sLevel.getPlayers(p -> p.isAlive());
		for (ServerPlayer player : players) {
			BlockPos playerPos = player.blockPosition();

			// Count existing blood-moon mobs nearby
			AABB searchBox = new AABB(playerPos).inflate(MOB_SEARCH_RADIUS, MOB_SEARCH_RADIUS, MOB_SEARCH_RADIUS);
			long nearbyCount = sLevel.getEntitiesOfClass(
					net.minecraft.world.entity.LivingEntity.class, searchBox,
					e -> e instanceof com.vincenthuto.hemomancy.common.entity.mob.monster.ThirsterEntity
						|| e instanceof com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneEntity).size();

			if (nearbyCount >= MAX_NEARBY_MOBS) continue;

			// Attempt to spawn 1-2 mobs
			int spawnCount = 1 + sLevel.random.nextInt(2);
			for (int i = 0; i < spawnCount; i++) {
				trySpawnOneMob(sLevel, playerPos);
			}
		}
	}

	private static void trySpawnOneMob(ServerLevel sLevel, BlockPos playerPos) {
		for (int attempt = 0; attempt < 10; attempt++) {
			int dx = sLevel.random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
			int dz = sLevel.random.nextInt(SPAWN_RADIUS * 2) - SPAWN_RADIUS;
			BlockPos spawnPos = sLevel.getHeightmapPos(
					net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
					playerPos.offset(dx, 0, dz));

			// Only spawn in darkness (sky light ≤ 4 or night)
			if (sLevel.getBrightness(LightLayer.SKY, spawnPos) > 4) continue;
			if (!sLevel.getBlockState(spawnPos).isAir()) continue;
			if (!sLevel.getBlockState(spawnPos.below()).isSolidRender(sLevel, spawnPos.below())) continue;

			// Randomly pick thirster or fargone
			net.minecraft.world.entity.Mob mob;
			if (sLevel.random.nextBoolean()) {
				mob = EntityInit.thirster.get().create(sLevel);
			} else {
				mob = EntityInit.fargone.get().create(sLevel);
			}
			if (mob == null) continue;

			mob.moveTo(spawnPos, sLevel.random.nextFloat() * 360f, 0f);
			mob.finalizeSpawn(sLevel, sLevel.getCurrentDifficultyAt(spawnPos),
					MobSpawnType.EVENT, null, null);
			sLevel.addFreshEntity(mob);
			break;
		}
	}

	// ---------------------------------------------------------------------------
	// Helpers
	// ---------------------------------------------------------------------------

	/**
	 * Applies Blood Moon effects based on whether the player is an initiated
	 * Harbinger (degree > 0) or not.
	 * Harbingers: Strength II + Night Vision (the moon favours the blood-bound).
	 * Non-Harbingers: Weakness I (the blood tide unsettles the uninitiated).
	 */
	private static void applyBloodMoonEffects(ServerPlayer player) {
		boolean isHarbinger = InitiatoryDegreeProvider.getPlayerDegreeNumber(player) > 0;
		if (isHarbinger) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_DURATION, 1, true, false, true));
			player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
		} else {
			player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EFFECT_DURATION, 0, true, false, true));
		}
	}

	private static void broadcastToPlayers(ServerLevel sLevel, Component message) {
		for (ServerPlayer player : sLevel.getPlayers(ServerPlayer::isAlive)) {
			player.sendSystemMessage(message);
		}
	}

	private static void syncToAllPlayers(ServerLevel sLevel, boolean active) {
		PacketSyncBloodMoon packet = new PacketSyncBloodMoon(active);
		PacketDistributor.sendToAllPlayers(packet);
	}
}
