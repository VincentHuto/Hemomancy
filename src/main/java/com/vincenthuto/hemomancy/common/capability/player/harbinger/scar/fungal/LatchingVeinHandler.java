package com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.fungal.AnastocordycepsNexusItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

/**
 * Manages the Latching Vein (Anastocordyceps nexus) tether mechanic.
 *
 * Tether state is stored in each entity's persistent data rather than a global
 * map, so it survives server ticks cleanly and doesn't leak when entities unload.
 *
 * Keys stored on tethered entity:
 *   hemomancy_tether_player  — UUID string of the owning player
 *   hemomancy_tether_expiry  — absolute game-time tick when tether expires
 */
public final class LatchingVeinHandler {

	private static final String KEY_PLAYER = "hemomancy_tether_player";
	private static final String KEY_EXPIRY  = "hemomancy_tether_expiry";

	/** Marker used in damage source ID to flag echo damage and prevent recursion. */
	public static final String ECHO_SOURCE_ID = "hemomancy_latching_vein_echo";

	private LatchingVeinHandler() {}

	/**
	 * Called when the player deals damage. Applies a tether to the struck entity
	 * and up to {@link AnastocordycepsNexusItem#MAX_TETHER_TARGETS} nearby enemies.
	 */
	public static void applyTether(LivingEntity player, LivingEntity struck, long gameTime) {
		UUID playerUUID = player.getUUID();
		long expiry = gameTime + AnastocordycepsNexusItem.TETHER_DURATION_TICKS;

		setTether(struck, playerUUID, expiry);

		AABB area = struck.getBoundingBox().inflate(AnastocordycepsNexusItem.TETHER_RADIUS);
		List<Monster> nearby = player.level().getEntitiesOfClass(Monster.class, area,
				e -> e != struck && e != player);

		int count = 0;
		for (Monster mob : nearby) {
			if (count >= AnastocordycepsNexusItem.MAX_TETHER_TARGETS - 1) break;
			setTether(mob, playerUUID, expiry);
			count++;
		}
	}

	/**
	 * Called when any living entity takes damage. If the entity is tethered and
	 * the damage is not itself an echo, propagates {@link AnastocordycepsNexusItem#ECHO_FRACTION}
	 * of the damage to all other currently-tethered entities in range.
	 *
	 * @param damaged     the entity that was hurt
	 * @param amount      the damage amount
	 * @param sourceId    the damage source descriptor id — skip if equals ECHO_SOURCE_ID
	 * @param gameTime    current level game time
	 */
	public static void onEntityDamaged(LivingEntity damaged, float amount, String sourceId, long gameTime) {
		if (ECHO_SOURCE_ID.equals(sourceId)) return;

		CompoundTag data = damaged.getPersistentData();
		if (!data.contains(KEY_PLAYER) || !data.contains(KEY_EXPIRY)) return;
		if (gameTime > data.getLong(KEY_EXPIRY)) {
			clearTether(damaged);
			return;
		}

		UUID ownerUUID = UUID.fromString(data.getString(KEY_PLAYER));
		float echoDamage = amount * AnastocordycepsNexusItem.ECHO_FRACTION;

		AABB area = damaged.getBoundingBox().inflate(AnastocordycepsNexusItem.TETHER_RADIUS * 2);
		List<LivingEntity> candidates = damaged.level().getEntitiesOfClass(LivingEntity.class, area,
				e -> e != damaged && isTetheredTo(e, ownerUUID, gameTime));

		for (LivingEntity target : candidates) {
			target.hurt(damaged.level().damageSources().magic(), echoDamage);
		}
	}

	private static void setTether(LivingEntity entity, UUID playerUUID, long expiry) {
		CompoundTag data = entity.getPersistentData();
		data.putString(KEY_PLAYER, playerUUID.toString());
		data.putLong(KEY_EXPIRY, expiry);
	}

	private static void clearTether(LivingEntity entity) {
		CompoundTag data = entity.getPersistentData();
		data.remove(KEY_PLAYER);
		data.remove(KEY_EXPIRY);
	}

	private static boolean isTetheredTo(LivingEntity entity, UUID playerUUID, long gameTime) {
		CompoundTag data = entity.getPersistentData();
		if (!data.contains(KEY_PLAYER) || !data.contains(KEY_EXPIRY)) return false;
		if (gameTime > data.getLong(KEY_EXPIRY)) {
			clearTether(entity);
			return false;
		}
		try {
			return playerUUID.equals(UUID.fromString(data.getString(KEY_PLAYER)));
		} catch (IllegalArgumentException e) {
			return false;
		}
	}
}
