package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Feeds fractional hunger (Sun-Fed's quarter points, Bloom's half points) by carrying the remainder per player until
 * it makes a whole point. Session state; a lost remainder is at most one point of food.
 */
public final class MorphlingFeeding {
	private static final Map<UUID, Double> CARRY = new ConcurrentHashMap<>();

	private MorphlingFeeding() {
	}

	/**
	 * Adds {@code hunger} points; with {@code matchSaturation} each whole point also brings one point of saturation
	 * ("matching saturation"). Returns the whole points fed.
	 */
	public static int feed(Player player, double hunger, boolean matchSaturation) {
		if (hunger <= 0.0D) return 0;
		double total = CARRY.getOrDefault(player.getUUID(), 0.0D) + hunger;
		int whole = (int) Math.floor(total);
		CARRY.put(player.getUUID(), total - whole);
		if (whole > 0) {
			// FoodData.eat adds nutrition and saturation = nutrition * modifier * 2.
			player.getFoodData().eat(whole, matchSaturation ? 0.5F : 0.0F);
		}
		return whole;
	}

	/** Whole-point remainder arithmetic, for tests: returns {whole points, carried remainder}. */
	public static double[] split(double carried, double hunger) {
		double total = carried + Math.max(0.0D, hunger);
		double whole = Math.floor(total);
		return new double[] { whole, total - whole };
	}

	public static void clear(UUID player) {
		CARRY.remove(player);
	}

	public static void clearSessionState() {
		CARRY.clear();
	}
}
