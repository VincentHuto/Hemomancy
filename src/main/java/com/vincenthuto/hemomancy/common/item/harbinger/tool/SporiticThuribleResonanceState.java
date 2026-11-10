package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SporiticThuribleResonanceState {
	private static final Map<UUID, Entry> ACTIVE = new ConcurrentHashMap<>();

	private SporiticThuribleResonanceState() {
	}

	public static void clearSessionState() {
		ACTIVE.clear();
	}

	public static void grant(Player player, EnumBloodTemper temper, long expiresAtTick) {
		if (player != null && temper != null) {
			ACTIVE.put(player.getUUID(), new Entry(temper, expiresAtTick));
		}
	}

	public static Optional<EnumBloodTemper> getActiveTemper(Player player) {
		if (player == null || !player.hasEffect(EffectInit.sporitic_resonance)) {
			if (player != null) {
				ACTIVE.remove(player.getUUID());
			}
			return Optional.empty();
		}
		Entry entry = ACTIVE.get(player.getUUID());
		if (entry == null) {
			return Optional.empty();
		}
		if (player.level().getGameTime() > entry.expiresAtTick()) {
			ACTIVE.remove(player.getUUID());
			return Optional.empty();
		}
		return Optional.of(entry.temper());
	}

	public static double getCostMultiplier(Player player, EnumBloodTemper manipulationTemper) {
		return getActiveTemper(player)
				.map(temper -> SporiticThuribleResonanceRules.costMultiplier(temper, manipulationTemper))
				.orElse(1.0);
	}

	public static double getCooldownMultiplier(Player player, EnumBloodTemper manipulationTemper) {
		return getActiveTemper(player)
				.map(temper -> SporiticThuribleResonanceRules.cooldownMultiplier(temper, manipulationTemper))
				.orElse(1.0);
	}

	private record Entry(EnumBloodTemper temper, long expiresAtTick) {
	}
}
