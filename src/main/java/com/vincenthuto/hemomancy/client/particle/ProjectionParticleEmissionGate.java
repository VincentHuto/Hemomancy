package com.vincenthuto.hemomancy.client.particle;

import java.util.HashMap;
import java.util.Map;

public final class ProjectionParticleEmissionGate {
	private static final int CLEANUP_THRESHOLD = 256;
	private static final long RETAIN_TICKS = 200L;

	private final Map<Integer, Long> lastEmissionTicks = new HashMap<>();

	public boolean tryAcquire(int emitterId, long gameTime) {
		Long previousTick = this.lastEmissionTicks.put(emitterId, gameTime);
		if (this.lastEmissionTicks.size() > CLEANUP_THRESHOLD) {
			this.lastEmissionTicks.entrySet().removeIf(entry -> gameTime - entry.getValue() > RETAIN_TICKS);
		}
		return previousTick == null || previousTick.longValue() != gameTime;
	}
}
