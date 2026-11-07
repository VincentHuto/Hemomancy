package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The heal multipliers a Saint's presence holds on its challengers, read by {@code HealModifier.SAINT_PRESENCE}. Each
 * entry lasts its ticks; the lowest live entry wins, and a player with none heals at 1. Session state:
 * {@code TransientServerStateEvents} clears it and ends a player's entries on logout.
 */
public final class SaintHealing {
	private record Entry(float multiplier, long expires) {
	}

	private static final Map<UUID, List<Entry>> ENTRIES = new HashMap<>();

	private SaintHealing() {
	}

	public static void set(Player player, float multiplier, int ticks) {
		set(player.getUUID(), multiplier, ticks, player.level().getGameTime());
	}

	public static void set(UUID player, float multiplier, int ticks, long now) {
		if (ticks <= 0) return;
		List<Entry> entries = ENTRIES.computeIfAbsent(player, ignored -> new ArrayList<>());
		entries.removeIf(entry -> entry.expires() <= now);
		entries.add(new Entry(Math.max(0.0F, multiplier), now + ticks));
	}

	/** The multiplier a heal on {@code entity} takes now; 1 when none is held. */
	public static float multiplier(LivingEntity entity) {
		if (!(entity instanceof Player)) return 1.0F;
		return multiplier(entity.getUUID(), entity.level().getGameTime());
	}

	public static float multiplier(UUID player, long now) {
		List<Entry> entries = ENTRIES.get(player);
		if (entries == null) return 1.0F;
		float lowest = Float.MAX_VALUE;
		boolean live = false;
		for (Entry entry : entries) {
			if (entry.expires() <= now) continue;
			live = true;
			lowest = Math.min(lowest, entry.multiplier());
		}
		if (!live) {
			ENTRIES.remove(player);
			return 1.0F;
		}
		return lowest;
	}

	public static void clear() {
		ENTRIES.clear();
	}

	/** Logout, or the trial's end for that player. */
	public static void end(Player player) {
		end(player.getUUID());
	}

	public static void end(UUID player) {
		ENTRIES.remove(player);
	}
}
