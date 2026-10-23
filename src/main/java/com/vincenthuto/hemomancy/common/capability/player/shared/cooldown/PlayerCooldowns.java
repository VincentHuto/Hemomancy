package com.vincenthuto.hemomancy.common.capability.player.shared.cooldown;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The persisted cooldown tier (S-7): the game time each named cooldown ends at. It is saved with the player, so it
 * survives relog, and never copied to a respawned or End-returned player, so it resets as the root keys it replaced
 * did. A cooldown that should survive death belongs to its system's own attachment; one that should end at logout or
 * server stop is a UUID-keyed map cleared through {@code TransientServerStateEvents}.
 */
public class PlayerCooldowns implements INBTSerializable<CompoundTag> {
	public static final String SILENT_SLIPPING = "silent_slipping";
	public static final String SILENT_SLIPPING_SHARD_PULSE = "silent_slipping_shard_pulse";
	public static final String PHANTASMAL_REACTIVE = "phantasmal_reactive";
	public static final String SILENT_ARCHON_REFUSAL = "silent_archon_refusal";
	public static final String PRISMATIC_FLASH = "prismatic_flash";
	public static final String SCAR_PULSE = "scar_pulse";
	public static final String SCRIPTORIUM_BACKLASH = "scriptorium_backlash";
	/** Followed by the armour set ability id. */
	public static final String ARMOR_SET_ABILITY_PREFIX = "armor_set_ability:";

	private static final String UNTIL = "Until";
	private static final String LEGACY_ARMOR_SET_ABILITY_PREFIX = "hemomancy:armor_set_ability_cooldown:";
	/** Root keys these cooldowns lived under before the attachment (sunset 2027-04-23). */
	private static final Map<String, String> LEGACY_KEYS = Map.of(
			"hemomancy:silent_slipping_cooldown_until", SILENT_SLIPPING,
			"hemomancy:silent_slipping_shard_pulse_cooldown_until", SILENT_SLIPPING_SHARD_PULSE,
			"hemomancy:phantasmal_reactive_until", PHANTASMAL_REACTIVE,
			"hemomancy:silent_archon_refusal_until", SILENT_ARCHON_REFUSAL,
			"hemomancy:prismatic_flash_until", PRISMATIC_FLASH,
			"hemomancy:scar_pulse_until", SCAR_PULSE,
			"ScriptoriumBacklashUntil", SCRIPTORIUM_BACKLASH);

	private final Map<String, Long> until = new TreeMap<>();

	public static PlayerCooldowns of(Player player) {
		return HemoCapabilityAccess.cooldowns(player);
	}

	/** The game time the cooldown ends at, or 0 if it was never started. */
	public long until(String id) {
		return until.getOrDefault(id, 0L);
	}

	public void setUntil(String id, long gameTime) {
		until.put(id, gameTime);
	}

	/**
	 * A {@code PlayerDataMigrations} entry: moves the cooldown root keys into the attachment and removes them. A root
	 * key that is present wins.
	 */
	public static void migrateLegacyRootKeys(ServerPlayer player) {
		CompoundTag data = player.getPersistentData();
		PlayerCooldowns cooldowns = of(player);
		for (String key : List.copyOf(data.getAllKeys())) {
			String id = key.startsWith(LEGACY_ARMOR_SET_ABILITY_PREFIX)
					? ARMOR_SET_ABILITY_PREFIX + key.substring(LEGACY_ARMOR_SET_ABILITY_PREFIX.length())
					: LEGACY_KEYS.get(key);
			if (id == null) continue;
			cooldowns.setUntil(id, data.getLong(key));
			data.remove(key);
		}
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		CompoundTag entries = new CompoundTag();
		until.forEach(entries::putLong);
		tag.put(UNTIL, entries);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		until.clear();
		CompoundTag entries = tag.getCompound(UNTIL);
		for (String id : entries.getAllKeys()) until.put(id, entries.getLong(id));
	}
}
