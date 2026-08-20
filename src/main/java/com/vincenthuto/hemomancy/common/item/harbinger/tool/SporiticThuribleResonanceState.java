package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
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

	public static void grant(Player player, EnumBloodTendency tendency, long expiresAtTick) {
		if (player != null && tendency != null) {
			ACTIVE.put(player.getUUID(), new Entry(tendency, expiresAtTick));
		}
	}

	public static Optional<EnumBloodTendency> getActiveTendency(Player player) {
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
		return Optional.of(entry.tendency());
	}

	public static double getCostMultiplier(Player player, EnumBloodTendency manipulationTendency) {
		return getActiveTendency(player)
				.map(tendency -> SporiticThuribleResonanceRules.costMultiplier(tendency, manipulationTendency))
				.orElse(1.0);
	}

	public static double getCooldownMultiplier(Player player, EnumBloodTendency manipulationTendency) {
		return getActiveTendency(player)
				.map(tendency -> SporiticThuribleResonanceRules.cooldownMultiplier(tendency, manipulationTendency))
				.orElse(1.0);
	}

	private record Entry(EnumBloodTendency tendency, long expiresAtTick) {
	}
}
