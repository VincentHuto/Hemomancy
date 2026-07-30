package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class CardinalRiteFogState {
	private static final float FADE_IN_TICKS = 10.0F;
	private static final float FADE_OUT_TICKS = 16.0F;

	private final Map<BlockPos, TrackedRite> trackedRites = new HashMap<>();

	public List<Sample> update(List<ActiveRiteClientData.RiteEntry> activeRites, float time) {
		for (TrackedRite tracked : trackedRites.values()) {
			tracked.active = false;
		}
		for (ActiveRiteClientData.RiteEntry rite : activeRites) {
			if (rite.isUnstained()) {
				trackedRites.remove(rite.getCenter());
				continue;
			}
			TrackedRite tracked = trackedRites.computeIfAbsent(
					rite.getCenter(), ignored -> new TrackedRite(rite, time));
			tracked.rite = rite;
			tracked.active = true;
		}

		List<Sample> samples = new ArrayList<>(trackedRites.size());
		Iterator<TrackedRite> iterator = trackedRites.values().iterator();
		while (iterator.hasNext()) {
			TrackedRite tracked = iterator.next();
			float elapsed = Math.max(0.0F, time - tracked.lastTime);
			float step = elapsed / (tracked.active ? FADE_IN_TICKS : FADE_OUT_TICKS);
			tracked.opacity = tracked.active
					? Math.min(1.0F, tracked.opacity + step)
					: Math.max(0.0F, tracked.opacity - step);
			tracked.lastTime = time;
			if (!tracked.active && tracked.opacity <= 0.001F) {
				iterator.remove();
			} else {
				samples.add(new Sample(tracked.rite, tracked.opacity));
			}
		}
		return List.copyOf(samples);
	}

	public void clear() {
		trackedRites.clear();
	}

	public record Sample(ActiveRiteClientData.RiteEntry rite, float opacity) {
	}

	private static final class TrackedRite {
		private ActiveRiteClientData.RiteEntry rite;
		private float opacity;
		private float lastTime;
		private boolean active;

		private TrackedRite(ActiveRiteClientData.RiteEntry rite, float time) {
			this.rite = rite;
			this.lastTime = time;
		}
	}
}
