package com.vincenthuto.hemomancy.client.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public final class CrimsonFireClientState {
	private static final Map<Integer, Long> ACTIVE_UNTIL_BY_ENTITY_ID = new HashMap<>();

	private CrimsonFireClientState() {
	}

	public static void markActive(int entityId, int durationTicks) {
		if (entityId < 0) {
			return;
		}
		long until = currentTick() + Math.max(1, durationTicks);
		ACTIVE_UNTIL_BY_ENTITY_ID.merge(entityId, until, Math::max);
	}

	public static boolean isActive(Entity entity) {
		if (entity == null || !entity.isOnFire()) {
			return false;
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
		ACTIVE_UNTIL_BY_ENTITY_ID.entrySet().removeIf(entry -> {
			if (entry.getValue() <= now) {
				return true;
			}
			Entity entity = level.getEntity(entry.getKey());
			return entity == null || !entity.isOnFire();
		});
	}

	private static long currentTick() {
		ClientLevel level = Minecraft.getInstance().level;
		return level == null ? 0L : level.getGameTime();
	}
}
