package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

public final class MonolithicDislocationClientState {
	private static final Map<Integer, Long> ACTIVE_UNTIL_BY_ENTITY_ID = new HashMap<>();

	private MonolithicDislocationClientState() {
	}

	public static void markActive(int entityId, int durationTicks) {
		if (entityId < 0) {
			return;
		}
		long until = currentTick() + Math.max(1, durationTicks);
		ACTIVE_UNTIL_BY_ENTITY_ID.merge(entityId, until, Math::max);
	}

	public static boolean isActive(LivingEntity entity) {
		if (entity.hasEffect(EffectInit.monolithic_dislocation)) {
			return true;
		}
		Long until = ACTIVE_UNTIL_BY_ENTITY_ID.get(entity.getId());
		if (until == null) {
			return false;
		}
		if (until <= currentTick()) {
			ACTIVE_UNTIL_BY_ENTITY_ID.remove(entity.getId());
			return false;
		}
		return true;
	}

	public static void tick() {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null) {
			ACTIVE_UNTIL_BY_ENTITY_ID.clear();
			return;
		}
		long now = level.getGameTime();
		ACTIVE_UNTIL_BY_ENTITY_ID.entrySet().removeIf(entry -> entry.getValue() <= now);
	}

	private static long currentTick() {
		ClientLevel level = Minecraft.getInstance().level;
		return level == null ? 0L : level.getGameTime();
	}
}
