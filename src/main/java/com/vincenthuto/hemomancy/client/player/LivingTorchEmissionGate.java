package com.vincenthuto.hemomancy.client.player;

import java.util.HashMap;
import java.util.Map;

/** Allows render-driven effects once per entity and logical client tick. */
public final class LivingTorchEmissionGate {
	private final Map<Integer, Long> lastTickByEntity = new HashMap<>();

	public boolean tryAcquire(int entityId, long gameTime) {
		Long previous = lastTickByEntity.put(entityId, gameTime);
		return previous == null || previous.longValue() != gameTime;
	}

	public void clear() {
		lastTickByEntity.clear();
	}
}
