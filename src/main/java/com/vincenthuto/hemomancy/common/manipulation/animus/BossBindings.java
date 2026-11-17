package com.vincenthuto.hemomancy.common.manipulation.animus;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The boss binding limit at work (LP-16): the count of bindings a boss has taken, saved on the boss itself so a relog
 * does not refill it, the slip roll, and the held form's wait after a slip. See {@link BossBindingRules}.
 */
public final class BossBindings {
	static final String TAKEN_KEY = "hemomancy:boss_bindings_taken";
	/** Player then boss: the game time the held form may try that boss again. */
	private static final Map<UUID, Map<UUID, Long>> WAITS = new ConcurrentHashMap<>();

	private BossBindings() {
	}

	public static int taken(LivingEntity boss) {
		return data(boss).getInt(TAKEN_KEY);
	}

	public static boolean spent(LivingEntity boss) {
		return taken(boss) >= BossBindingRules.total(boss.getMaxHealth());
	}

	/** Whether {@code player}'s held form is still waiting out a slip on {@code boss}. */
	public static boolean waiting(Player player, LivingEntity boss) {
		Map<UUID, Long> waits = WAITS.get(player.getUUID());
		Long until = waits == null ? null : waits.get(boss.getUUID());
		return until != null && player.level().getGameTime() < until;
	}

	/**
	 * One attempt by {@code player} to bind {@code boss}: refused when the body is spent, else the slip roll. A hold that
	 * lands counts one. The binder is told when it slips or the body is spent.
	 */
	public static boolean tryTake(Player player, LivingEntity boss) {
		return tryTake(player, boss, boss.getRandom().nextDouble());
	}

	/** {@link #tryTake} with the slip roll given, in [0, 1): the attempt slips when the roll is under the chance. */
	public static boolean tryTake(Player player, LivingEntity boss, double roll) {
		if (spent(boss)) {
			player.displayClientMessage(Component.translatable("message.hemomancy.blood_binding.boss_spent", boss.getDisplayName())
					.withStyle(ChatFormatting.DARK_RED), true);
			return false;
		}
		if (roll < BossBindingRules.failChance(boss.getMaxHealth(), boss.getHealth())) {
			WAITS.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>())
					.put(boss.getUUID(), player.level().getGameTime() + BossBindingRules.RETRY_WAIT_TICKS);
			player.displayClientMessage(Component.translatable("message.hemomancy.blood_binding.boss_slipped", boss.getDisplayName())
					.withStyle(ChatFormatting.RED), true);
			return false;
		}
		data(boss).putInt(TAKEN_KEY, taken(boss) + 1);
		return true;
	}

	private static CompoundTag data(LivingEntity boss) {
		return boss.getPersistentData();
	}

	public static void clearSessionState() {
		WAITS.clear();
	}

	/** Called by {@code TransientServerStateEvents} when the player logs out. */
	public static void logout(Player player) {
		WAITS.remove(player.getUUID());
	}
}
