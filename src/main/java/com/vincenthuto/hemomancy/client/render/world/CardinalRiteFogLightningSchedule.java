package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import net.minecraft.core.BlockPos;

import java.util.*;

public final class CardinalRiteFogLightningSchedule {
	private static final int MIN_INITIAL_DELAY = 6;
	private static final int MIN_REPEAT_DELAY = 12;

	private final Map<BlockPos, Long> nextStrikeTicks = new HashMap<>();

	public List<Strike> update(List<ActiveRiteClientData.RiteEntry> activeRites, long gameTick) {
		Set<BlockPos> activeCenters = new HashSet<>();
		for (ActiveRiteClientData.RiteEntry rite : activeRites) {
			if (!rite.isUnstained() && rite.hasFogLightning()) {
				activeCenters.add(rite.getCenter());
			}
		}
		nextStrikeTicks.keySet().removeIf(center -> !activeCenters.contains(center));

		List<Strike> strikes = new ArrayList<>();
		for (ActiveRiteClientData.RiteEntry rite : activeRites) {
			if (rite.isUnstained() || !rite.hasFogLightning()) {
				continue;
			}
			BlockPos center = rite.getCenter();
			long dueTick = nextStrikeTicks.computeIfAbsent(center,
					ignored -> gameTick + initialDelay(center));
			if (gameTick < dueTick) {
				continue;
			}
			long seed = mix(center.asLong() ^ gameTick * 0x9E3779B97F4A7C15L);
			nextStrikeTicks.put(center, gameTick + repeatDelay(seed));
			strikes.add(new Strike(rite, seed));
		}
		return List.copyOf(strikes);
	}

	public void clear() {
		nextStrikeTicks.clear();
	}

	private static int initialDelay(BlockPos center) {
		return MIN_INITIAL_DELAY + Math.floorMod(center.asLong(), 10);
	}

	private static int repeatDelay(long seed) {
		return MIN_REPEAT_DELAY + (int) Math.floorMod(seed ^ (seed >>> 32), 17L);
	}

	private static long mix(long value) {
		value ^= value >>> 30;
		value *= 0xBF58476D1CE4E5B9L;
		value ^= value >>> 27;
		value *= 0x94D049BB133111EBL;
		return value ^ (value >>> 31);
	}

	public record Strike(ActiveRiteClientData.RiteEntry rite, long seed) {
	}
}
