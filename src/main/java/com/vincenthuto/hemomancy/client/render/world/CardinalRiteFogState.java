package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import net.minecraft.core.BlockPos;

import java.util.*;

public final class CardinalRiteFogState {
	private static final float FADE_IN_TICKS = 10.0F;
	/** Fade-out when a rite simply stops being synced; endings may ask for a longer one. */
	public static final float FADE_OUT_TICKS = 16.0F;

	private final Map<BlockPos, TrackedRite> trackedRites = new HashMap<>();

	public List<Sample> update(List<ActiveRiteClientData.RiteEntry> activeRites, float time) {
		return update(activeRites, time, center -> FADE_OUT_TICKS);
	}

	/**
	 * @param fadeOut ticks a vanished rite's fog takes to thin away, by centre;
	 *                read once when the rite leaves the sync list so endings can
	 *                keep the fog for as long as their own choreography runs
	 */
	public List<Sample> update(List<ActiveRiteClientData.RiteEntry> activeRites, float time, FadeOut fadeOut) {
		for (TrackedRite tracked : trackedRites.values()) {
			tracked.active = false;
		}
		for (ActiveRiteClientData.RiteEntry rite : activeRites) {
			if (rite.isUnstained() || !rite.hasFog()) {
				trackedRites.remove(rite.getCenter());
				continue;
			}
			TrackedRite tracked = trackedRites.computeIfAbsent(
					rite.getCenter(), ignored -> new TrackedRite(rite, time));
			tracked.rite = rite;
			tracked.active = true;
			tracked.fadeOutTicks = -1.0F;
		}

		List<Sample> samples = new ArrayList<>(trackedRites.size());
		Iterator<TrackedRite> iterator = trackedRites.values().iterator();
		while (iterator.hasNext()) {
			TrackedRite tracked = iterator.next();
			float elapsed = Math.max(0.0F, time - tracked.lastTime);
			float targetOpacity = fogOpacity(tracked.rite.getFogProfile());
			if (tracked.active) {
				tracked.opacity = Math.min(targetOpacity,
						tracked.opacity + elapsed / FADE_IN_TICKS * targetOpacity);
			} else {
				if (tracked.fadeOutTicks < 0.0F) {
					tracked.fadeOutTicks = Math.max(1.0F, fadeOut.ticks(tracked.rite.getCenter()));
				}
				tracked.opacity = Math.max(0.0F, tracked.opacity - elapsed / tracked.fadeOutTicks);
			}
			tracked.lastTime = time;
			if (!tracked.active && tracked.opacity <= 0.001F) {
				iterator.remove();
			} else {
				samples.add(new Sample(tracked.rite, tracked.opacity, tracked.active));
			}
		}
		return samples;
	}

	private static float fogOpacity(String profile) {
		return switch (profile) {
			case "faint" -> 0.38F;
			case "dense" -> 0.70F;
			case "storm" -> 1.0F;
			default -> 0.0F;
		};
	}

	public void clear() {
		trackedRites.clear();
	}

	@FunctionalInterface
	public interface FadeOut {
		float ticks(BlockPos center);
	}

	public record Sample(ActiveRiteClientData.RiteEntry rite, float opacity, boolean active) {
	}

	private static final class TrackedRite {
		private ActiveRiteClientData.RiteEntry rite;
		private float opacity;
		private float lastTime;
		private boolean active;
		private float fadeOutTicks = -1.0F;

		private TrackedRite(ActiveRiteClientData.RiteEntry rite, float time) {
			this.rite = rite;
			this.lastTime = time;
		}
	}
}
